//
//  CallApiImpl.swift
//  CallAPI
//
//  Created by Agora on 2023/5/29.
//

import Foundation
import AgoraRtcKit
import AgoraRtmKit

private let kRetryCount: Int = 3
private let kCallTimeoutInterval = 15.0

private let kCurrentMessageVersion = "1.0"
public let kMessageAction = "message_action"
public let kMessageVersion = "message_version"
public let kMessageTs = "message_timestamp"

public let kCallId = "callId"
public let kRemoteUserId = "remoteUserId"
public let kFromUserId = "fromUserId"
public let kFromUserExtension = "fromUserExtension"
public let kFromRoomId = "fromRoomId"
public let kCalleeState = "state"      //当前呼叫状态
public let kPublisher = "publisher"    //状态触发的用户uid，目前可以抛出当前用户和主叫的状态，如果无publisher，默认是当前用户

public let kDebugInfo = "debugInfo"    //测试信息，目前是会在主叫onBegin时抛出分步耗时
public let kDebugInfoMap = "debugInfoMap"    //测试信息，目前是会在主叫onBegin时抛出分步耗时

enum CallAction: UInt {
    case call = 0
    case cancelCall = 1
    case accept = 2
    case reject = 3
    case hangup = 4
}

enum CallCostType: String {
    case recvCalling = "recvCalling"
    case acceptCall = "acceptCall"
    case localUserJoinChannel = "localUserJoinChannel"
    case remoteUserJoinChannel = "remoteUserJoinChannel"
    case recvFirstFrame = "recvFirstFrame"
}

public class CallApiImpl: NSObject {
    private let delegates:NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private let rtcProxy: CallAgoraExProxy = CallAgoraExProxy()
    private var config: CallConfig?
    private var tokenConfig: CallTokenConfig?
    private var messageManager: CallMessageManager?
    private var prepareConfig: PrepareConfig? = nil
    
    private var callId: String = ""
    
    private var recvMessageTsMap: [UInt: Int] = [:]
    
    private var reportInfoList: [CallReportInfo] = []
    private var isChannelJoined: Bool = false {
        didSet {
            guard isChannelJoined else {return}
            _flushReport()
        }
    }
    
    /// 当前状态
    private var state: CallStateType = .idle {
        didSet {
            if oldValue == state {
                return
            }
            
            switch state {
            case .calling:
                //开启定时器，如果超时无响应，调用no response
                timer = Timer.scheduledTimer(withTimeInterval: kCallTimeoutInterval, repeats: false, block: {[weak self] timer in
                    self?._notifyState(state: .prepared, stateReason: .callingTimeout)
                    self?._notifyEvent(event: .callingTimeout)
                })
            case .idle, .prepared, .failed, .connected:
                timer = nil
            default:
                break
            }
        }
    }
    
    /// join channel ex的connection，用来leave channel ex和判断是否已经加入ex channel
    private var rtcConnection: AgoraRtcConnection?
    //加入RTC完成回调
    private var joinRtcCompletion: ((NSError?)->Void)?
    //首帧出图回调
    private var firstFrameCompletion: (()->Void)?
    //呼叫中的频道名
    private var callingRoomId: String?
    //呼叫中的远端用户
    private var callingUserId: UInt?
    //是否正在Prepare，目前比较粗暴直接返回错误，后续看是否需要每个closure都存下来等完成后分发
    private var isPreparing: Bool = false
    private var preparedTs: Int = 0
    //发起呼叫的定时器，用来处理超时
    private var timer: Timer? {
        didSet {
            oldValue?.invalidate()
        }
    }
    //呼叫开始的时间
    private var callTs: Int? {
        didSet {
            callCost = ""
            callCostMap.removeAll()
        }
    }
    //呼叫耗时的debug信息
    private var callCost: String = ""
    private var callCostMap: [String: Int] = [:]
    
    //local canvas
    private lazy var canvas: AgoraRtcVideoCanvas = {
        let canvas = AgoraRtcVideoCanvas()
        canvas.mirrorMode = .disabled
        return canvas
    }()
    
    deinit {
        callPrint("deinit-- CallApiImpl")
        rtcProxy.removeListener(self)
    }
    
    public override init() {
        super.init()
        callPrint("init-- CallApiImpl")
        addRTCListener(listener: self)
    }
    
    private func _messageDic(action: CallAction) -> [String: Any] {
        var dic: [String: Any] = [:]
        dic[kMessageAction] = action.rawValue
        dic[kMessageVersion] = kCurrentMessageVersion
        dic[kMessageTs] = _getNtpTimeInMs()
        dic[kFromUserId] = config?.userId ?? 0
        if  callId.count > 0 {
            dic[kCallId] = callId
        }
        if let userExtension = config?.userExtension {
            dic[kFromUserExtension] = userExtension
        }
        return dic
    }
    
    //获取ntp时间
    private func _getNtpTimeInMs() -> Int {
        let ntpTime: UInt64 = config?.rtcEngine.getNtpWallTimeInMs() ?? 0
//        let ntpTime: UInt64 = UInt64(config?.rtcEngine.getNtpTimeInMs() ?? 0)
        var localNtpTime: Int = Int(ntpTime > Int.max ? 0 : ntpTime)

        if localNtpTime == 0 {
            localNtpTime = Int(round(Date().timeIntervalSince1970 * 1000.0))
        }
        return localNtpTime
    }
    
    private func _getCost(ts: Int? = nil) -> Int {
        guard let callTs = callTs else { return 0 }
        var cost = 0
        if let ts = ts {
            cost = ts - callTs
        } else {
            cost = _getNtpTimeInMs() - callTs
        }
        return cost
    }
    
    private func timeProfiling(message: String, ts: Int? = nil) {
        let cost = _getCost(ts:ts)
        callCostMap[message] = callCostMap[message] ?? 0 + cost
        let msg = "\(message): \(cost) ms"
        callProfilePrint(msg)
        callCost = "\(callCost)\n\(msg)"
    }
}

//MARK: private method
extension CallApiImpl {
    private func _processState(prevState: CallStateType,
                               state: CallStateType,
                               stateReason: CallReason,
                               eventReason: String,
                               elapsed: Int) {
        if prevState != state, state == .idle {
            _leaveRTC(force: true)
            _cleanCallCache()
            config = nil
            tokenConfig = nil
            messageManager?.logout()
            messageManager = nil
            delegates.removeAllObjects()
        } else if prevState != .idle, state == .prepared {
            _leaveRTC()
            _cleanCallCache()
        }
    }
    
    private func _notifyTokenPrivilegeWillExpire() {
        for element in delegates.allObjects {
            (element as? CallApiListenerProtocol)?.tokenPrivilegeWillExpire?()
        }
    }
    
    //外部状态通知
    private func _notifyState(state: CallStateType,
                              stateReason: CallReason = .none,
                              eventReason: String = "",
                              elapsed: Int = 0,
                              eventInfo: [String: Any] = [:]) {
        callPrint("_notifyState  state: \(state.rawValue), stateReason: '\(stateReason.rawValue)', eventReason: \(eventReason), elapsed: \(elapsed) ms, eventInfo: \(eventInfo)")
        
        _processState(prevState: self.state,
                      state: state,
                      stateReason: stateReason,
                      eventReason: eventReason,
                      elapsed: elapsed)
        self.state = state
        for element in delegates.allObjects {
            (element as? CallApiListenerProtocol)?.onCallStateChanged(with: state,
                                                                      stateReason: stateReason,
                                                                      eventReason: eventReason,
                                                                      elapsed: elapsed,
                                                                      eventInfo: eventInfo)
        }
    }
    
    private func _notifyEvent(event: CallEvent, elapsed: Int = 0) {
        if let config = config {
            _reportEvent(key: "event=\(event.rawValue)&userId=\(config.userId)&role=\(config.role.rawValue)&state=\(state.rawValue)", value: _getNtpTimeInMs(), messageId: "")
        } else {
            callWarningPrint("_notifyEvent config == nil")
        }
        
        _notifyOptionalFunc { listener in
            listener.onCallEventChanged?(with: event, elapsed: elapsed)
        }
        
        switch event {
        case .remoteJoin:
            _reportCostEvent(type: .remoteUserJoinChannel)
        case .localJoin:
            _reportCostEvent(type: .localUserJoinChannel)
        case .remoteAccepted:
            _reportCostEvent(type: .acceptCall)
        case .onCalling:
            _reportCostEvent(type: .recvCalling)
        case .recvRemoteFirstFrame:
            _reportCostEvent(type: .recvFirstFrame)
        default:
            break
        }
    }
    
    private func _notifyOptionalFunc(closure: ((CallApiListenerProtocol)->())) {
        for element in delegates.allObjects {
            if let target = element as? CallApiListenerProtocol {
                closure(target)
            }
        }
    }
    
    private func _prepareForCall(prepareConfig: PrepareConfig, retryCount: Int = kRetryCount, completion: ((NSError?) -> ())?) {
        if state == .prepared {
            let reason = "is already in 'prepared' state"
            callWarningPrint(reason)
            completion?(NSError(domain: reason, code: -1))
            return
        }
        
        guard let messageManager = messageManager else {
            let reason = "not init"
            callWarningPrint(reason)
            completion?(NSError(domain: reason, code: -1))
            return
        }
        
        if isPreparing {
            let reason = "is already in preparing"
            callWarningPrint(reason)
            completion?(NSError(domain: reason, code: -1))
            return
        }
        
        self.prepareConfig = prepareConfig
        let group = DispatchGroup()
        var rtmError: NSError? = nil
        var rtcError: NSError? = nil
        let date = Date()
        isPreparing = true
        let tag = UUID().uuidString
        callPrint("prepareForCall[\(tag)]")
        group.enter()
        messageManager.rtmInitialize(prepareConfig: prepareConfig, tokenConfig: tokenConfig) {[weak self] err in
            guard let self = self else { return }
            rtmError = err
            self.callWarningPrint("prepareForCall[\(tag)] rtmInitialize completion: \(err?.localizedDescription ?? "success")")
            group.leave()
        }
        
        if prepareConfig.autoJoinRTC {
            group.enter()
            _joinRTC(roomId: tokenConfig?.roomId ?? "", token: tokenConfig?.rtcToken ?? "", joinOnly: true) { err in
                rtcError = err
                self.callWarningPrint("prepareForCall[\(tag)] joinRTC completion: \(err?.localizedDescription ?? "success")")
                group.leave()
            }
        }
        
        group.notify(queue: DispatchQueue.main) { [weak self] in
            guard let self = self else {return}
            if let err = rtmError ?? rtcError {
                if retryCount <= 1 {
                    self.isPreparing = false
                    completion?(err)
                    self._notifyState(state: .failed,
                                      stateReason: err == rtmError ? .rtmSetupFailed : .joinRTCFailed,
                                      eventReason: err.localizedDescription)
                } else {
                    self.isPreparing = false
                    self._prepareForCall(prepareConfig: prepareConfig, retryCount: retryCount - 1, completion: completion)
                    self._notifyEvent(event: .rtmSetupSuccessed)
                }
                return
            }
            self.preparedTs = self._getNtpTimeInMs()
            self.isPreparing = false
            self._notifyState(state: .prepared, elapsed: Int(-date.timeIntervalSinceNow * 1000))
            completion?(nil)
        }
    }
    
    private func _deinitialize() {
        isPreparing = false
        _notifyState(state: .idle)
        _notifyEvent(event: .deinitialize)
    }
    
    //设置远端画面
    private func _setupRemoteVideo(roomId: String, uid: UInt, canvasView: UIView) {
        guard let connection = rtcConnection, let engine = config?.rtcEngine else {
            callWarningPrint("_setupRemoteVideo fail: connection or engine is empty")
            return
        }
        
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = canvasView
        videoCanvas.renderMode = .hidden
        let ret = engine.setupRemoteVideoEx(videoCanvas, connection: connection)
        callPrint("_setupRemoteVideo ret = \(ret) roomId: \(roomId) uid: \(uid)")
    }
    
    //设置本地画面
    private func _setupLocalVideo(uid: UInt, canvasView: UIView) {
        guard let engine = config?.rtcEngine else {
            callWarningPrint("_setupLocalVideo fail: engine is empty")
            return
        }
        canvas.view = canvasView
        canvas.uid = uid
        canvas.mirrorMode = .auto
//        engine.setVideoFrameDelegate(self)
        engine.setDefaultAudioRouteToSpeakerphone(true)
        engine.setupLocalVideo(canvas)
        engine.startPreview()
    }
    
    private func _joinRTCAndNotify(roomId: String,
                                   token: String,
                                   retryCount: Int = 3,
                                   joinOnly: Bool = false,
                                   completion: ((NSError?) -> ())? = nil) {
        _joinRTC(roomId: roomId, token: token, retryCount: retryCount, joinOnly: joinOnly) {[weak self] err in
            guard let self = self else { return }
            if let err = err {
                let errReason = err.localizedDescription
                self._notifyState(state: .failed, stateReason:.joinRTCFailed, eventReason: errReason)
                self._notifyEvent(event: .joinRTCFailed)
            } else {
                self._notifyEvent(event: .joinRTCSuccessed)
            }
            completion?(err)
        }
    }
    
    //加入RTC频道
    private func _joinRTC(roomId: String,
                          token: String,
                          retryCount: Int = 3,
                          joinOnly: Bool = false,
                          completion: ((NSError?) -> ())?) {
        guard let config = self.config else {
            completion?(NSError(domain: "config is Empty", code: -1))
            return
        }
        
        if let connection = rtcConnection {
            if connection.channelId == roomId {
                callWarningPrint("rtc join already")
                let mediaOptions = AgoraRtcChannelMediaOptions()
                mediaOptions.clientRoleType = .broadcaster
                mediaOptions.publishCameraTrack = !joinOnly
                mediaOptions.publishMicrophoneTrack = !joinOnly
                mediaOptions.autoSubscribeAudio = !joinOnly
                mediaOptions.autoSubscribeVideo = !joinOnly
                config.rtcEngine.updateChannelEx(with: mediaOptions, connection: connection)
                
                let errReason = "rtc join already"
                completion?(NSError(domain: errReason, code: -1))
                _setupLocalVideo(uid: config.userId, canvasView: config.localView)
                return
            }
            
            callWarningPrint("mismatch channel, leave first! tqarget: \(roomId) current: \(connection.channelId)")
            config.rtcEngine.leaveChannelEx(connection)
            rtcConnection = nil
        }
        
        //需要先开启音视频，使用enableLocalAudio而不是enableAudio，否则会导致外部mute的频道变成unmute
        config.rtcEngine.enableLocalAudio(true)
        config.rtcEngine.enableLocalVideo(true)
        
        let connection = AgoraRtcConnection()
        connection.channelId = roomId
        connection.localUid = config.userId
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.clientRoleType = .broadcaster
        mediaOptions.publishCameraTrack = !joinOnly
        mediaOptions.publishMicrophoneTrack = !joinOnly
        mediaOptions.autoSubscribeAudio = !joinOnly
        mediaOptions.autoSubscribeVideo = !joinOnly
        let ret =
        config.rtcEngine.joinChannelEx(byToken: token,
                                       connection: connection,
                                       delegate: rtcProxy,
                                       mediaOptions: mediaOptions)
        callPrint("joinRTC channel roomId: \(roomId) uid: \(config.userId) ret = \(ret)")
        rtcConnection = connection
        joinRtcCompletion = { [weak self] err in
            guard let self = self else {return}
            guard let error = err else {
                completion?(nil)
                self.timeProfiling(message: "4.呼叫-加入房间成功")
                return
            }
            
            if error.code == AgoraErrorCode.tokenExpired.rawValue {
                completion?(error)
                self._notifyTokenPrivilegeWillExpire()
                return
            }
            
            self.rtcConnection = nil
            if retryCount <= 1 {
                completion?(error)
            } else {
                self._joinRTCAndNotify(roomId: roomId, token: token, retryCount: retryCount - 1, completion: completion)
            }
        }
        let date = Date()
        firstFrameCompletion = { [weak self] in
            guard let self = self else { return }
            let eventInfo = [
                kFromRoomId: self.callingRoomId ?? "",
                kFromUserId: self.callingUserId ?? 0,
                kRemoteUserId: config.userId,
                kDebugInfo : self.callCost,
                kDebugInfoMap: self.callCostMap
            ]
            let elapsed = Int(-date.timeIntervalSinceNow * 1000)
            self._notifyState(state: .connecting,
                              stateReason: .recvRemoteFirstFrame,
                              elapsed: elapsed)
            self._notifyState(state: .connected,
                              stateReason: .recvRemoteFirstFrame,
                              elapsed: elapsed,
                              eventInfo: eventInfo)
            self._notifyEvent(event: .recvRemoteFirstFrame, elapsed: elapsed)
        }
        
        if ret != 0 {
            rtcConnection = nil
            joinRtcCompletion?(NSError(domain: "join rtc fail!", code: Int(ret)))
            return
        }
        _setupLocalVideo(uid: config.userId, canvasView: config.localView)
    }
    
    //离开RTC频道
    private func _leaveRTC(force: Bool = false) {
        joinRtcCompletion = nil
        guard let rtcConnection = self.rtcConnection else {
            callWarningPrint("leave RTC channel failed, not joined the channel")
            return
        }
        //没有connection表示没有进行1v1
        config?.rtcEngine.stopPreview()
        
        if !force, prepareConfig?.autoJoinRTC ?? false {
            //如果默认加入RTC，则不退出，只停止收发流
            let mediaOptions = AgoraRtcChannelMediaOptions()
            mediaOptions.autoSubscribeVideo = false
            mediaOptions.autoSubscribeAudio = false
            mediaOptions.publishMicrophoneTrack = false
            mediaOptions.publishCustomAudioTrack = false
            config?.rtcEngine.updateChannelEx(with: mediaOptions, connection: rtcConnection)
        } else {
            let ret = config?.rtcEngine.leaveChannelEx(rtcConnection)
            callPrint("leave RTC channel[\(ret ?? -1)]")
            self.rtcConnection = nil
        }
    }
    
    private func _cleanCallCache() {
        callingRoomId = nil
        callingUserId = nil
        callTs = nil
        timer = nil
        callId = ""
    }
    
    private func _flushReport() {
        reportInfoList.forEach { info in
            self._sendCustomReportMessage(msgId: info.msgId,
                                          category: info.category,
                                          event: info.event,
                                          label: info.label,
                                          value: info.value)
        }
        reportInfoList.removeAll()
    }
    
    private func _reportCostEvent(type: CallCostType) {
        _reportEvent(key: type.rawValue, value: _getCost(), messageId: "")
    }
    
    private func _reportMethod(event: String, label: String = "") {
        let msgId = "scenarioAPI"
        let category = "3_iOS_0.2.0"
        callPrint("_reportMethod event\(event)")
        if isChannelJoined {
            _sendCustomReportMessage(msgId: msgId, category: category, event: event, label: label, value: 0)
            return
        }
        
        let info = CallReportInfo(msgId: msgId, category: category, event: event, label: label, value: 0)
        reportInfoList.append(info)
//        callPrint("sendCustomReportMessage not join channel cache it! event: \(event) label: \(label)")
    }
    
    private func _reportEvent(key: String, value: Int, messageId: String) {
        guard let config = config else { return }
        let msgId = "uid=\(config.userId)&messageId=\(messageId)"
        let category = "\(config.mode.rawValue)"
        if isChannelJoined {
            _sendCustomReportMessage(msgId: msgId, category: category, event: key, label: callId, value: value)
            return
        }
        
        let info = CallReportInfo(msgId: msgId, category: category, event: key, label: callId, value: value)
        reportInfoList.append(info)
        callPrint("sendCustomReportMessage not join channel cache it! msgId: \(msgId) category: \(category) event: \(key) label: \(callId) value: \(value)")
    }
    
    private func _sendCustomReportMessage(msgId: String,
                                          category: String,
                                          event: String,
                                          label: String,
                                          value: Int) {
        guard let config = config, isChannelJoined, let rtcConnection = rtcConnection else { return }
        let ret = config.rtcEngine.sendCustomReportMessageEx(msgId, category: category, event: event, label: label, value: value, connection: rtcConnection)
        callPrint("sendCustomReportMessage msgId: \(msgId) category: \(category) event: \(event) : \(ret)")
    }
}

//MARK: on Message
extension CallApiImpl {
    private func _process(reason: CallAction, message: [String: Any]) {
        switch reason {
        case .call:
            _onCall(message: message)
            return
        case .cancelCall:
            _onCancel(message: message)
        case .reject:
            _onReject(message: message)
        case .accept:
            _onAccept(message: message)
            return
        case .hangup:
            _onHangup(message: message)
        }
    }
    
    private func _reject(roomId: String, remoteUserId: UInt, reason: String?, completion: ((NSError?, [String: Any]) -> ())? = nil) {
        guard let fromRoomId = tokenConfig?.roomId else {
            completion?(NSError(domain: "reject fail! current userId or roomId is empty", code: -1), [:])
            callWarningPrint("reject fail! current userId or roomId is empty")
            return
        }
        var message: [String: Any] = _messageDic(action: .reject)
        message[kRemoteUserId] = remoteUserId
        message[kFromRoomId] = fromRoomId
        messageManager?.sendMessage(roomId: roomId, fromRoomId: fromRoomId, message: message) { error in
            completion?(error, message)
        }
        _notifyEvent(event: .localRejected)
    }
    
    private func _hangup(roomId: String, completion: ((NSError?, [String: Any]) -> ())? = nil) {
        guard let fromRoomId = tokenConfig?.roomId else {
            completion?(NSError(domain: "reject fail! current roomId is empty", code: -1), [:])
            callWarningPrint("reject fail! current roomId is empty")
            return
        }
        let message: [String: Any] = _messageDic(action: .hangup)
        messageManager?.sendMessage(roomId: roomId, fromRoomId: fromRoomId, message: message) { err in
            completion?(err, message)
        }
    }
    
    //收到呼叫消息
    private func _onCall(fromRoomId: String, fromUserId: UInt, callId: String, userExtension: [String: Any]) {
        //如果不是prepared状态或者不是接收的正在接听的用户的呼叫
        guard state == .prepared || callingUserId == fromUserId else {
            _reject(roomId: fromRoomId, remoteUserId: fromUserId, reason: "callee is currently on call")
            return
        }
        
        if callingUserId == fromUserId, callingRoomId != fromRoomId {
            //如果主叫换了room id呼叫
            _reject(roomId: fromRoomId, remoteUserId: fromUserId, reason: "callee is being occupied by another channel of the caller")
            _notifyState(state: .prepared, stateReason: .cancelByCallerRecall)
            _notifyEvent(event: .cancelByCallerRecall)
            return
        }
        
        self.callId = callId
        let eventInfo = [kFromRoomId: fromRoomId, kFromUserId: fromUserId, kRemoteUserId: config?.userId ?? 0, kFromUserExtension: userExtension] as [String : Any]
        _notifyState(state: .calling, stateReason: .none, eventInfo: eventInfo)
        _notifyEvent(event: .onCalling)
        guard config?.autoAccept ?? false else {
            //fix receive '_onCall' twice will reject
            callingRoomId = fromRoomId
            callingUserId = fromUserId
            return
        }
        
        accept(roomId: fromRoomId, remoteUserId: fromUserId, rtcToken: tokenConfig?.rtcToken ?? "") { err in
        }
    }
    
    private func _onCall(message: [String: Any]) {
        let fromRoomId = message[kFromRoomId] as? String ?? ""
        let fromUserId = message[kFromUserId] as? UInt ?? 0
        let userExtension = message[kFromUserExtension] as? [String: Any] ?? [:]
        let callId = message[kCallId] as? String ?? ""
        _onCall(fromRoomId: fromRoomId, fromUserId: fromUserId, callId: callId, userExtension: userExtension)
    }
    
    //收到取消呼叫消息
    private func _onCancel(message: [String: Any]) {
        let fromUserId = message[kFromUserId] as? UInt ?? 0
        //如果不是接收的正在接听的用户的呼叫
        guard callingUserId == fromUserId else {
            return
        }
        
        _notifyState(state: .prepared, stateReason: .remoteCancel, eventInfo: message)
        _notifyEvent(event: .remoteCancel)
    }
    
    //收到拒绝消息
    private func _onReject(message: [String: Any]) {
        _notifyState(state: .prepared, stateReason: .remoteRejected, eventInfo: message)
        _notifyEvent(event: .remoteRejected)
    }
    
    //收到接受消息
    private func _onAccept(message: [String: Any]) {
        timeProfiling(message: "2.呼叫-被叫收到呼叫", ts: message[kMessageTs] as? Int)
        timeProfiling(message: "3.呼叫-收到被叫同意")
        guard state == .calling else {
            return
        }
        let elapsed = _getNtpTimeInMs() - (callTs ?? 0)
        _notifyState(state: .connecting, stateReason: .remoteAccepted, elapsed: elapsed)
        _notifyEvent(event: .remoteAccepted, elapsed: elapsed)
    }
    
    //收到挂断消息
    private func _onHangup(message: [String: Any]) {
        guard let fromUserId = message[kFromUserId] as? UInt, fromUserId == callingUserId else {
            return
        }
        _notifyState(state: .prepared, stateReason: .remoteHangup)
        _notifyEvent(event: .remoteHangup)
    }
}

//MARK: CallApiProtocol
extension CallApiImpl: CallApiProtocol {
    public func getCallId() -> String {
        _reportMethod(event: "\(#function)")
        return callId
    }
    
    public func initialize(config: CallConfig,
                           token: CallTokenConfig,
                           completion: @escaping ((NSError?)->())) {
        _reportMethod(event: "\(#function)", label: "appId=\(config.appId)&userId=\(config.userId)&mode=\(config.mode.rawValue)&role=\(config.role.rawValue)&autoAccept=\(config.autoAccept)&roomId=\(token.roomId)&rtcToken=\(token.rtcToken)&rtmToken=\(token.rtmToken)")
        if state != .idle {
            callWarningPrint("must invoke 'deinitialize' to clean state")
            return
        }
        self.config = config
        self.tokenConfig = token
        self.messageManager?.logout()
        self.messageManager = CallMessageManager(config: config, rtmDelegate: self, delegate: self)
        
        //纯1v1需要设置成caller
        if config.mode == .pure1v1, config.role == .callee {
            config.role = .caller
        }
        
        //被叫需要自动加入rtm
        guard config.role == .callee else {
            completion(nil)
            return
        }
        
        prepareForCall(prepareConfig: PrepareConfig.calleeConfig()) { error in
            completion(error)
        }
    }
    
    public func deinitialize(completion: @escaping (()->())) {
        _reportMethod(event: "\(#function)")
        
        if let callingRoomId = self.callingRoomId {
            var roomId = callingRoomId
            //秀场转1v1主叫挂断的是房主id
//            if config?.mode == .showTo1v1, config?.role == .caller {
//                roomId = config?.ownerRoomId ?? ""
//            }
            _hangup(roomId: roomId) {[weak self] err, message in
                self?._deinitialize()
                completion()
            }
        } else {
            self._deinitialize()
            completion()
        }
    }
    
    public func renewToken(with config: CallTokenConfig) {
        _reportMethod(event: "\(#function)", label: "roomId=\(config.roomId)&rtcToken=\(config.rtcToken)&rtmToken=\(config.rtmToken)")
        if let roomId = tokenConfig?.roomId, roomId != config.roomId {
            callWarningPrint("renewToken failed, roomid missmatch")
        }
        callPrint("renewToken with roomId[\(config.roomId)]")
        self.tokenConfig = config
        messageManager?.renewToken(rtmToken: config.rtmToken)
        guard let connection = rtcConnection, connection.channelId == config.roomId else {
            callWarningPrint("renewToken fail! connection.channelId[\(rtcConnection?.channelId)] != config.roomId[\(config.roomId)]")
            return
        }
        let options = AgoraRtcChannelMediaOptions()
        options.token = config.rtcToken
        let ret =
        self.config?.rtcEngine.updateChannelEx(with: options, connection: connection)
        callPrint("rtc[\(config.roomId)] renewToken ret = \(ret)")
    }
    
    public func renewRemoteCallerChannelToken(roomId: String, token: String) {
        _reportMethod(event: "\(#function)", label: "roomId=\(roomId)&token=\(token)")
        guard let connection = rtcConnection, connection.channelId == roomId, let rtcEngine = self.config?.rtcEngine else { return }
        let options = AgoraRtcChannelMediaOptions()
        options.token = token
        let ret = rtcEngine.updateChannelEx(with: options, connection: connection)
        callPrint("rtc[\(roomId)] renewRemoteCallerChannelToken ret = \(ret)")
    }
    
    public func prepareForCall(prepareConfig: PrepareConfig, completion: ((NSError?) -> ())?) {
        _reportMethod(event: "\(#function)", label: "autoLoginRTM=\(prepareConfig.autoLoginRTM)&autoSubscribeRTM=\(prepareConfig.autoSubscribeRTM)&autoJoinRTC=\(prepareConfig.autoJoinRTC)")
        _prepareForCall(prepareConfig: prepareConfig, completion: completion)
    }
    
    public func addListener(listener: CallApiListenerProtocol) {
        _reportMethod(event: "\(#function)")
        if delegates.contains(listener) {
            return
        }
        delegates.add(listener)
    }
    
    public func removeListener(listener: CallApiListenerProtocol) {
        _reportMethod(event: "\(#function)")
        delegates.remove(listener)
    }
    
    //呼叫
    public func call(roomId: String, remoteUserId: UInt, completion: ((NSError?) -> ())?) {
        _reportMethod(event: "\(#function)", label: "roomId=\(roomId)&remoteUserId=\(remoteUserId)")
        guard let fromRoomId = tokenConfig?.roomId else {
            completion?(NSError(domain: "call fail! config or roomId is empty", code: -1))
            callWarningPrint("call fail! config or roomId is empty")
            return
        }
        
        guard messageManager?.isSubscribedRTM ?? false else {
            callPrint("call need to init rtm")
            //如果没有初始化rtm，需要先做一遍
            _prepareForCall(prepareConfig: PrepareConfig.calleeConfig()) {[weak self] error in
                if let error = error {
                    completion?(error)
                    return
                }
                self?.call(roomId: roomId, remoteUserId: remoteUserId, completion: completion)
            }
            return
        }
        
        callTs = _getNtpTimeInMs()
        //发送呼叫消息
        callId = UUID().uuidString
        var message: [String: Any] = _messageDic(action: .call)
        message[kRemoteUserId] = remoteUserId
        message[kFromRoomId] = fromRoomId
        messageManager?.sendMessage(roomId: roomId, fromRoomId: fromRoomId, message: message) {[weak self] err in
            guard let self = self else { return }
            if let error = err {
                self._notifyState(state: .prepared, stateReason: .messageFailed, eventReason: error.localizedDescription)
                self._notifyEvent(event: .messageFailed)
                return
            }
            self.timeProfiling(message: "1.呼叫-呼叫回调")
        }
        
        _notifyState(state: .calling, eventInfo: message)
        _notifyEvent(event: .onCalling)
        
        callingRoomId = roomId
        callingUserId = remoteUserId
        //不等响应即加入频道，加快join速度，失败则leave
        _joinRTCAndNotify(roomId: fromRoomId, token: tokenConfig?.rtcToken ?? "") {[weak self] error in
            guard let error = error else {return}
            self?.cancelCall(completion: { err in
            })
        }
    }
    
    //取消呼叫
    public func cancelCall(completion: ((NSError?) -> ())?) {
        _reportMethod(event: "\(#function)")
        guard let roomId = callingRoomId, let fromRoomId = tokenConfig?.roomId else {
            completion?(NSError(domain: "cancelCall fail! callingRoomId is empty", code: -1))
            callWarningPrint("cancelCall fail! callingRoomId is empty")
            return
        }
        let message: [String: Any] = _messageDic(action: .cancelCall)
        messageManager?.sendMessage(roomId: roomId, fromRoomId: fromRoomId, message: message) { err in
        }
        
        _notifyState(state: .prepared, stateReason: .localCancel)
        _notifyEvent(event: .localCancel)
    }
    
    //接受
    public func accept(roomId: String, remoteUserId: UInt, rtcToken: String, completion: ((NSError?) -> ())?) {
        _reportMethod(event: "\(#function)", label: "roomId=\(roomId)&remoteUserId=\(remoteUserId)&rtcToken=\(rtcToken)")
        guard let fromRoomId = tokenConfig?.roomId else {
            let errReason = "accept fail! current userId or roomId is empty"
            completion?(NSError(domain: errReason, code: -1))
            callWarningPrint(errReason)
            _notifyState(state: .prepared, stateReason: .messageFailed, eventReason: errReason)
            _notifyEvent(event: .messageFailed)
            return
        }
        
        //查询是否是calling状态，如果是prapared，表示可能被主叫取消了
        guard state == .calling else {
            let errReason = "accept fail! current state is not calling"
            completion?(NSError(domain: errReason, code: -1))
            callWarningPrint(errReason)
            _notifyState(state: .prepared, stateReason: .none, eventReason: errReason)
            _notifyEvent(event: .stateMismatch)
            return
        }
        
        var message: [String: Any] = _messageDic(action: .accept)
        message[kRemoteUserId] = remoteUserId
        message[kFromRoomId] = fromRoomId
        messageManager?.sendMessage(roomId: roomId, fromRoomId: fromRoomId, message: message) { err in
        }
        _notifyState(state: .connecting, stateReason: .localAccepted, eventInfo: message)
        _notifyEvent(event: .localAccepted)
        
        callingRoomId = roomId
        callingUserId = remoteUserId
        
        callTs = _getNtpTimeInMs()
        //不等响应即加入频道，加快join速度，失败则leave
        _joinRTCAndNotify(roomId: roomId, token: rtcToken) {[weak self] error in
            guard let error = error else {return}
            self?.cancelCall(completion: { err in
            })
        }
    }
    
    //拒绝
    public func reject(roomId: String, remoteUserId: UInt, reason: String?, completion: ((NSError?) -> ())?) {
        _reportMethod(event: "\(#function)", label: "roomId=\(roomId)&remoteUserId=\(remoteUserId)&reason=\(reason)")
        _reject(roomId: roomId, remoteUserId: remoteUserId, reason: reason)
        _notifyState(state: .prepared, stateReason: .localRejected)
        _notifyEvent(event: .localRejected)
    }
    
    //挂断
    public func hangup(roomId: String, completion: ((NSError?) -> ())?) {
        _reportMethod(event: "\(#function)", label: "roomId=\(roomId)")
        _hangup(roomId: roomId)
        _notifyState(state: .prepared, stateReason: .localHangup)
        _notifyEvent(event: .localHangup)
    }
    
    public func addRTCListener(listener: AgoraRtcEngineDelegate) {
        _reportMethod(event: "\(#function)")
        rtcProxy.addListener(listener)
    }
    
    public func removeRTCListener(listener: AgoraRtcEngineDelegate) {
        _reportMethod(event: "\(#function)")
        rtcProxy.removeListener(listener)
    }
}

//MARK: AgoraRtmClientDelegate
extension CallApiImpl: AgoraRtmClientDelegate {
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, onTokenPrivilegeWillExpire channel: String?) {
        _notifyTokenPrivilegeWillExpire()
    }
    
    //收到RTM消息
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, on event: AgoraRtmMessageEvent) {
        let message = event.message
        guard let data = message.getData() as? Data,
              let dic = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let messageAction = CallAction(rawValue: dic[kMessageAction] as? UInt ?? 0),
              let msgTs = dic[kMessageTs] as? Int,
              let userId = dic[kFromUserId] as? UInt,
              let messageVersion = dic[kMessageVersion] as? String else {
            callWarningPrint("fail to parse message: \(message.getType().rawValue)")
            return
        }
        
        //TODO: compatible other message version
        guard kCurrentMessageVersion == messageVersion else { return }
        
        callPrint("on event message: \(dic)")
        let origMsgTs = recvMessageTsMap[userId] ?? 0
        //对应用户的消息拦截老的消息
        if origMsgTs > msgTs {
            callWarningPrint("ignore old message by user[\(userId)], msg: \(message)")
            return
        }
        //prepared完成之前的消息全部忽略
        if preparedTs > msgTs {
            callWarningPrint("ignore old message before prepare state, msg: \(message)")
            return
        }
        
        recvMessageTsMap[userId] = msgTs
        _process(reason: messageAction, message: dic)
    }
}

//MARK: AgoraRtcEngineDelegate
extension CallApiImpl: AgoraRtcEngineDelegate {
    public func rtcEngine(_ engine: AgoraRtcEngineKit, tokenPrivilegeWillExpire token: String) {
        _notifyTokenPrivilegeWillExpire()
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, connectionChangedTo state: AgoraConnectionState, reason: AgoraConnectionChangedReason) {
        callPrint("connectionChangedTo state: \(state.rawValue) reason: \(reason.rawValue)")
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        callPrint("didJoinedOfUid: \(uid) elapsed: \(elapsed)")
        guard callingUserId == uid, let roomId = callingRoomId, let config = config else {
            return
        }
        
        self.timeProfiling(message: "5.呼叫-对端[\(uid)] 加入房间")
        self._setupRemoteVideo(roomId: roomId, uid: uid, canvasView: config.remoteView)
        
        _notifyEvent(event: .remoteJoin, elapsed: _getNtpTimeInMs() - (callTs ?? 0))
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        callPrint("didOfflineOfUid: \(uid)")
        guard callingUserId == uid else {
            return
        }
        
        _notifyEvent(event: .remoteLeave)
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didLeaveChannelWith stats: AgoraChannelStats) {
        callPrint("didLeaveChannelWith")
        isChannelJoined = false
        _notifyEvent(event: .localLeave)
    }

    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        callPrint("join RTC channel, didJoinChannel: \(uid), channel: \(channel) elapsed: \(elapsed)")
        guard uid == config?.userId ?? 0 else {
            return
        }
        isChannelJoined = true
        joinRtcCompletion?(nil)
        joinRtcCompletion = nil
        
        _notifyEvent(event: .localJoin, elapsed: _getNtpTimeInMs() - (callTs ?? 0))
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        callWarningPrint("didOccurError: \(errorCode.rawValue)")
//        joinRtcCompletion?(NSError(domain: "join RTC fail", code: errorCode.rawValue))
//        joinRtcCompletion = nil
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit,
                          remoteVideoStateChangedOfUid uid: UInt,
                          state: AgoraVideoRemoteState,
                          reason: AgoraVideoRemoteReason,
                          elapsed: Int) {
        let channelId = tokenConfig?.roomId ?? ""
        guard uid == callingUserId else {return}
        callPrint("didLiveRtcRemoteVideoStateChanged channelId: \(channelId)/\(callingRoomId ?? "") uid: \(uid)/\(callingUserId ?? 0) state: \(state.rawValue) reason: \(reason.rawValue)")
        if state == .decoding /*2*/,
           ( reason == .remoteUnmuted /*6*/ || reason == .localUnmuted /*4*/ || reason == .localMuted /*3*/ )   {
            DispatchQueue.main.async {
                self.timeProfiling(message: "6.呼叫-收到对端[\(uid)] 首帧")
                self.firstFrameCompletion?()
            }
        }
    }
}

extension CallApiImpl: CallMessageDelegate {
    func debugInfo(message: String) {
        callPrint(message)
    }
    
    func debugWarning(message: String) {
        callWarningPrint(message)
    }
    
    func onMissReceipts(message: [String : Any]) {
        callWarningPrint("onMissReceipts: \(message)")
        _notifyEvent(event: .missingReceipts)
    }
}

#if DEBUG
let formatter = DateFormatter()
#endif
func debugPrint(_ message: String) {
#if DEBUG
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    let timeString = formatter.string(from: Date())
    print("\(timeString) \(message)")
#endif
}

extension CallApiImpl {
    func callPrint(_ message: String) {
        for element in delegates.allObjects {
            (element as? CallApiListenerProtocol)?.callDebugInfo?(message: message)
        }
        guard delegates.count == 0 else {return}
    
        debugPrint("[CallApi]\(message)")
    }

    func callWarningPrint(_ message: String) {
        for element in delegates.allObjects {
            (element as? CallApiListenerProtocol)?.callDebugWarning?(message: message)
        }
        callPrint("[Warning]\(message)")
    }

    func callProfilePrint(_ message: String) {
        callPrint("[Profile]\(message)")
    }
}
