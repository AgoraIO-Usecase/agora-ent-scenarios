//
//  CallApiImpl.swift
//  CallAPI
//
//  Created by Agora on 2023/5/29.
//

import Foundation
import AgoraRtcKit
import AgoraRtmKit


let kReportCategory = "2_iOS_1.0.0"

private let kCurrentMessageVersion = "1.0"
private let kMessageAction = "message_action"
private let kMessageVersion = "message_version"
private let kMessageTs = "message_timestamp"

private let kCallId = "callId"

public let kRemoteUserId = "remoteUserId"
public let kFromUserId = "fromUserId"
public let kFromUserExtension = "fromUserExtension"
public let kFromRoomId = "fromRoomId"
public let kCalleeState = "state"      //当前呼叫状态
public let kPublisher = "publisher"    //状态触发的用户uid，目前可以抛出当前用户和主叫的状态，如果无publisher，默认是当前用户

//⚠️不允许修改下列两项值，客户可能会根据该rejectReason/call busy 来做业务判断(例如用户忙)
public let kRejectReason = "rejectReason"
public let kRejectReasonCallBusy = "The user is currently busy"


public let kHangupReason = "hangupReason"

//是否内部拒绝，收到内部拒绝目前标记为对端call busy
public let kRejectByInternal = "rejectByInternal"

public let kCostTimeMap = "costTimeMap"    //呼叫时的耗时信息，会在connected时抛出分步耗时

enum CallAutoSubscribeType: Int {
    case none = 0
    case video = 1
    case audioVideo = 2
}

enum CallAction: UInt {
    case call = 0
    case cancelCall = 1
    case accept = 2
    case reject = 3
    case hangup = 4
}

/// 被叫呼叫中加入RTC的策略
enum CalleeJoinRTCPolicy: Int {
    case calling    //在接到呼叫时即加入频道并推送音视频流，被叫时费用较高但出图更快
    case accepted   //在点击接受后才加入频道并推送音视频流，被叫时费用较低但出图较慢
}

let calleeJoinRTCPolicy: CalleeJoinRTCPolicy = .calling

public class CallApiImpl: NSObject {
    private let delegates:NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private let rtcProxy: CallAgoraExProxy = CallAgoraExProxy()
    private lazy var localFrameProxy: CallLocalFirstFrameProxy = CallLocalFirstFrameProxy(delegate: self)
    private var config: CallConfig?
    private var prepareConfig: PrepareConfig? = nil
    private var messageManager: CallMessageManager? {
        didSet {
            if oldValue == messageManager {
                return
            }
            oldValue?.deinitialize()
        }
    }
    
    private var connectInfo = CallConnectInfo()
        
    private var reportInfoList: [CallReportInfo] = []
    
    /// 是否加入频道
    var isChannelJoined: Bool = false
    
    private let tempRemoteCanvasView: UIView = UIView()
    
    /// 当前状态
    private var state: CallStateType = .idle {
        didSet {
            let prevState = oldValue
            if prevState == state { return }
            tempRemoteCanvasView.removeFromSuperview()
            switch state {
            case .calling:
                //开启定时器，如果超时无响应，调用no response
                let timeooutSecond = prepareConfig?.callTimeoutMillisecond ?? 0
                if timeooutSecond == 0 {return}
                let timeooutInterval = Double(timeooutSecond) / 1000
                connectInfo.timer = Timer.scheduledTimer(withTimeInterval: timeooutInterval,
                                                         repeats: false,
                                                         block: {[weak self] timer in
                    self?._cancelCall(completion: { err in
                    })
                    self?._updateAndNotifyState(state: .prepared, stateReason: .callingTimeout)
                    self?._notifyEvent(event: .callingTimeout)
                })
            case .prepared:
                connectInfo.timer = nil
                
                if prevState != .idle {
                    _prepareForCall(prepareConfig: prepareConfig!) { _ in
                    }
                }
            case .connecting:
                _updateAutoSubscribe(type: .audioVideo)
            case .connected:
                connectInfo.timer = nil
                if let remoteView = prepareConfig?.remoteView {
                    tempRemoteCanvasView.frame = remoteView.bounds
                    remoteView.addSubview(tempRemoteCanvasView)
                    tempRemoteCanvasView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
                } else {
                    callWarningPrint("remote view not found in connected state!")
                }
            case .idle, .failed:
                _leaveRTC()
                connectInfo.clean()
                config = nil
                isPreparing = false
                self.messageManager = nil
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
    //是否正在Prepare，目前比较粗暴直接返回错误，后续看是否需要每个closure都存下来等完成后分发
    private var isPreparing: Bool = false
    
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
    
    //获取ntp时间
    private func _getTimeInMs() -> Int {
        return Date().millisecondsSince1970()
    }
    
    private func _getCost(ts: Int? = nil) -> Int {
        guard let callTs = connectInfo.callTs else { return 0 }
        var cost = 0
        if let ts = ts {
            cost = ts - callTs
        } else {
            cost = _getTimeInMs() - callTs
        }
        return cost
    }
}

//MARK: message Dictionary
extension CallApiImpl {
    private func _messageDic(action: CallAction) -> [String: Any] {
        var dic: [String: Any] = [:]
        dic[kMessageAction] = action.rawValue
        dic[kMessageVersion] = kCurrentMessageVersion
        dic[kMessageTs] = _getTimeInMs()
        dic[kFromUserId] = config?.userId ?? 0
        dic[kCallId] = connectInfo.callId
        if let userExtension = prepareConfig?.userExtension {
            dic[kFromUserExtension] = userExtension
        }
        return dic
    }
    
    private func _callMessageDic(remoteUserId: UInt, fromRoomId: String) -> [String: Any] {
        var message: [String: Any] = _messageDic(action: .call)
        message[kRemoteUserId] = remoteUserId
        message[kFromRoomId] = fromRoomId
        return message
    }
    
    private func _rejectMessageDic(reason: String?, rejectByInternal: Bool) -> [String: Any] {
        var message: [String: Any] = _messageDic(action: .reject)
        message[kRejectReason] = reason
        message[kRejectByInternal] = rejectByInternal ? 1 : 0
        return message
    }
    
    private func _hangupMessageDic(reason: String?) -> [String: Any] {
        var message: [String: Any] = _messageDic(action: .hangup)
        message[kHangupReason] = reason
        return message
    }
}

//MARK: private method
extension CallApiImpl {
    private func _notifyTokenPrivilegeWillExpire() {
        for element in delegates.allObjects {
            (element as? CallApiListenerProtocol)?.tokenPrivilegeWillExpire?()
        }
    }
    
    private func checkConnectedSuccess(reason: CallStateReason) {
        guard connectInfo.isRetrieveFirstFrame, state == .connecting else {return}
        /*
         1.因为被叫提前加频道并订阅流和推流，导致双端收到视频首帧可能会比被叫点accept(变成connecting)比更早
         2.由于匹配1v1时双端都会收到onCall，此时A发起accept，B收到了onAccept+A首帧，会导致B未接受即进入了connected状态
         因此:
         变成connecting: 需要同时检查是否变成了“远端已接受” + “本地已接受(或已发起呼叫)”
         变成connected: 需要同时检查是否是"connecting状态" + “收到首帧”
         */
        _changeToConnectedState(reason: reason)
    }
    
    private func _changeToConnectedState(reason: CallStateReason) {
        let eventInfo: [String : Any] = [
            kFromRoomId: self.connectInfo.callingRoomId ?? "",
            kFromUserId: self.connectInfo.callingUserId ?? 0,
            kRemoteUserId: config?.userId ?? 0,
            kCostTimeMap: self.connectInfo.callCostMap
        ]
//        let elapsed = connectInfo.startRetrieveFirstFrame?.getCostMilliseconds() ?? 0
//        self._updateAndNotifyState(state: .connecting,
//                                   stateReason: .recvRemoteFirstFrame,
//                                   elapsed: elapsed)
        self._updateAndNotifyState(state: .connected,
                                   stateReason: reason,
//                                   elapsed: elapsed,
                                   eventInfo: eventInfo)
//        self._notifyEvent(event: .recvRemoteFirstFrame, elapsed: elapsed)
    }
    
    //外部状态通知
    private func _updateAndNotifyState(state: CallStateType,
                                       stateReason: CallStateReason = .none,
                                       eventReason: String = "",
//                                       elapsed: Int = 0,
                                       eventInfo: [String: Any] = [:]) {
        callPrint("call change[\(connectInfo.callId)] state: \(state.rawValue), stateReason: \(stateReason.rawValue), eventReason: '\(eventReason)'")
        self.state = state
        for element in delegates.allObjects {
            (element as? CallApiListenerProtocol)?.onCallStateChanged(with: state,
                                                                      stateReason: stateReason,
                                                                      eventReason: eventReason,
                                                                      eventInfo: eventInfo)
        }
    }
    
    private func _notifySendMessageErrorEvent(error: NSError, reason: String?) {
        _notifyErrorEvent(with: .sendMessageFail,
                          errorType: .rtm,
                          errorCode: error.code,
                          message: "\(reason ?? "")\(error.localizedDescription)")
    }
    
    private func _notifyRtcOccurErrorEvent(errorCode: Int, message: String? = nil) {
        _notifyErrorEvent(with: .rtcOccurError,
                          errorType: .rtc,
                          errorCode: errorCode,
                          message: message)
    }
    
    private func _notifyErrorEvent(with errorEvent: CallErrorEvent,
                                   errorType: CallErrorCodeType,
                                   errorCode: Int,
                                   message: String?) {
        callPrint("call change[\(connectInfo.callId)] errorEvent: '\(errorEvent.rawValue)', errorType: '\(errorType.rawValue)', errorCode: '\(errorCode)', message: '\(message ?? "")'")
        for element in delegates.allObjects {
            (element as? CallApiListenerProtocol)?.onCallError?(with: errorEvent,
                                                                errorType: errorType,
                                                                errorCode: errorCode,
                                                                message: message)
        }
    }
    
    private func _notifyEvent(event: CallEvent, eventReason: String? = nil) {
        callPrint("call change[\(connectInfo.callId)] event: \(event.rawValue) reason: '\(eventReason ?? "")'")
        if let config = config {
            var reason = ""
            if let eventReason = eventReason {
                reason = "&reason=\(eventReason)"
            }
            _reportEvent(key: "event=\(event.rawValue)&userId=\(config.userId)&state=\(state.rawValue)\(reason)", value: 0)
        } else {
            callWarningPrint("_notifyEvent config == nil")
        }
        
        _notifyOptionalFunc { listener in
            listener.onCallEventChanged?(with: event)
        }
        
        switch event {
        case .remoteUserRecvCall:
            _reportCostEvent(type: .remoteUserRecvCall)
        case .remoteJoin:
            _reportCostEvent(type: .remoteUserJoinChannel)
        case .localJoin:
            _reportCostEvent(type: .localUserJoinChannel)
        case .remoteAccepted:
            _reportCostEvent(type: .acceptCall)
            checkConnectedSuccess(reason: .remoteAccepted)
        case .localAccepted:
            _reportCostEvent(type: .acceptCall)
            checkConnectedSuccess(reason: .localAccepted)
        case .recvRemoteFirstFrame:
            _reportCostEvent(type: .recvFirstFrame)
            checkConnectedSuccess(reason: .recvRemoteFirstFrame)
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
    
    private func _prepareForCall(prepareConfig: PrepareConfig, completion: ((NSError?) -> ())?) {
        guard let config = self.config else {
            let reason = "config is Empty"
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
        
        var enableLoginRtm = true
        switch state {
        case .calling, .connecting, .connected:
            let reason = "currently busy"
            callWarningPrint(reason)
            completion?(NSError(domain: reason, code: -1))
            return
        case .prepared:
            enableLoginRtm = false
        case.failed, .idle:
            break
        }
        connectInfo.clean()
        
        let tag = UUID().uuidString
        callPrint("prepareForCall[\(tag)]")
        self.prepareConfig = prepareConfig.cloneConfig()
        
        //join rtc if need
        if prepareConfig.autoJoinRTC {
            _joinRTCWithMediaOptions(roomId: prepareConfig.roomId,
                                     role: .audience,
                                     subscribeType: .video) {[weak self] err in
                guard let self = self else { return }
                self.callWarningPrint("prepareForCall[\(tag)] joinRTC completion: \(err?.localizedDescription ?? "success")")
                if let err = err {
                    self._notifyRtcOccurErrorEvent(errorCode: err.code, message: err.localizedDescription)
                } else {
                    self._notifyEvent(event: .joinRTCSuccessed)
                }
            }
        } else {
            _leaveRTC()
        }
        
        //login rtm if need
        if enableLoginRtm {
            isPreparing = true
            let messageManager = CallMessageManager(config: config, delegate: self)
            self.messageManager = messageManager
            
            messageManager.initialize(prepareConfig: prepareConfig) {[weak self] err in
                guard let self = self else { return }
                self.isPreparing = false
                self.callWarningPrint("prepareForCall[\(tag)] rtmInitialize completion: \(err?.localizedDescription ?? "success")")
                if let err = err {
                    self._notifyErrorEvent(with: .rtmSetupFail,
                                           errorType: .rtm,
                                           errorCode: err.code,
                                           message: err.localizedDescription)
                } else {
                    self._notifyEvent(event: .rtmSetupSuccessed)
                }
                completion?(err)
            }
        } else {
            completion?(nil)
        }
    }
    
    private func _deinitialize() {
        _updateAndNotifyState(state: .idle)
        _notifyEvent(event: .deinitialize)
    }
    
    //设置远端画面
    private func _setupRemoteVideo(uid: UInt, canvasView: UIView) {
        guard let connection = rtcConnection, let engine = config?.rtcEngine else {
            callWarningPrint("_setupRemoteVideo fail: connection or engine is empty")
            return
        }
        
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = canvasView
        videoCanvas.renderMode = .hidden
        let ret = engine.setupRemoteVideoEx(videoCanvas, connection: connection)
        callPrint("_setupRemoteVideo ret = \(ret) channelId: \(connection.channelId) uid: \(uid)")
    }
    
    //设置本地画面
    private func _setupLocalVideo(uid: UInt, canvasView: UIView) {
        guard let engine = config?.rtcEngine else {
            callWarningPrint("_setupLocalVideo fail: engine is empty")
            return
        }
        config?.rtcEngine.addDelegate(self.localFrameProxy)
        
        canvas.view = canvasView
        canvas.uid = uid
        canvas.mirrorMode = .auto
        engine.setDefaultAudioRouteToSpeakerphone(true)
        engine.setupLocalVideo(canvas)
        let ret = engine.startPreview()
        
        if ret != 0 {
            _notifyErrorEvent(with: .startCaptureFail,
                              errorType: .rtc,
                              errorCode: Int(ret),
                              message: nil)
        }
    }
    
    /// 判断当前加入的RTC频道和传入的房间id是否一致
    /// - Parameter roomId: <#roomId description#>
    /// - Returns: <#description#>
    private func _isCurrentRTCChannel(roomId: String) -> Bool {
        return rtcConnection?.channelId == roomId ? true : false
    }
    
    /// 当前RTC频道是否加入成功或者正在加入中
    /// - Returns: <#description#>
    private func _isChannelJoinedOrJoining() -> Bool {
        return rtcConnection == nil ? false : true
    }
    
    /// 是否初始化完成
    /// - Returns: <#description#>
    private func _isInitialized() -> Bool {
        switch state {
        case .idle, .failed:
            return false
        default:
            return true
        }
    }
    
    /// 是否可以继续呼叫
    /// - Parameter callerUserId: <#callerUserId description#>
    /// - Returns: <#description#>
    private func _isCallActive(callerUserId: UInt) -> Bool {
        switch state {
        case .prepared:
            return true
        case .idle, .failed:
            return false
        case .calling, .connecting, .connected:
            if connectInfo.callingUserId ?? 0 == callerUserId {
                return true
            }
        }
        
        return false
    }
    
    private func _isCallingUser(message: [String: Any]) -> Bool {
        guard let fromUserId = message[kFromUserId] as? UInt,
              fromUserId == connectInfo.callingUserId else { return false }
        return true
    }
    
    private func _joinRTCWithMediaOptions(roomId: String,
                                          role: AgoraClientRole,
                                          subscribeType: CallAutoSubscribeType,
                                          completion:@escaping ((NSError?) -> ())) {
        if !_isCurrentRTCChannel(roomId: roomId) {
            _leaveRTC()
        }
        let isChannelJoinedOrJoining = _isChannelJoinedOrJoining()
        if isChannelJoinedOrJoining {
            completion(nil)
        } else {
            _joinRTC(roomId: roomId){  error in
                completion(error)
            }
        }
        //没有加入频道又是观众的情况下，不需要update role，join默认就是观众和不推流
        if isChannelJoinedOrJoining == true || role == .broadcaster {
            _updateRole(role: role)
        }
        _updateAutoSubscribe(type: subscribeType)
    }
    
    private func _joinRTCAsBroadcaster(roomId: String) {
        _joinRTCWithMediaOptions(roomId: roomId, 
                                 role: .broadcaster,
                                 subscribeType: .video) {[weak self] error in
            guard let self = self else {return}
            if let err = error {
                self._notifyRtcOccurErrorEvent(errorCode: err.code, message: err.localizedDescription)
            } else {
                self._notifyEvent(event: .joinRTCSuccessed)
            }
        }
        setupCanvas()
    }
    
    /// 以观众身份加入RTC
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - completion: <#completion description#>
    private func _joinRTC(roomId: String, completion:@escaping ((NSError?) -> ())) {
        guard let config = self.config, let rtcToken = prepareConfig?.rtcToken else {
            completion(NSError(domain: "config is Empty", code: -1))
            return
        }
        let connection = AgoraRtcConnection(channelId: roomId, localUid: Int(config.userId))
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.publishCameraTrack = false
        mediaOptions.publishMicrophoneTrack = false
        mediaOptions.autoSubscribeAudio = false
        mediaOptions.autoSubscribeVideo = false
        let ret = config.rtcEngine.joinChannelEx(byToken: rtcToken,
                                                 connection: connection,
                                                 delegate: rtcProxy,
                                                 mediaOptions: mediaOptions)
        callPrint("joinRTC channel roomId: \(roomId) uid: \(config.userId) ret = \(ret)")
        rtcConnection = connection
        joinRtcCompletion = { _ in
            completion(nil)
        }
//        let date = Date()
        firstFrameCompletion = { [weak self] in
            guard let self = self else { return }
            connectInfo.isRetrieveFirstFrame = true
            self._notifyEvent(event: .recvRemoteFirstFrame)
        }
        
        if ret != 0 {
            _notifyRtcOccurErrorEvent(errorCode: Int(ret))
        }
    }
    
    /// 切换主播和观众角色
    /// - Parameter role: <#role description#>
    private func _updateRole(role: AgoraClientRole) {
        guard let config = self.config, let connection = rtcConnection else { return }
        callPrint("_updateRole: \(role.rawValue)")
        
        //需要先开启音视频，使用enableLocalAudio而不是enableAudio，否则会导致外部mute的频道变成unmute
        if role == .broadcaster {
            config.rtcEngine.enableLocalAudio(true)
            config.rtcEngine.enableLocalVideo(true)
        } else {
            config.rtcEngine.enableLocalAudio(false)
            config.rtcEngine.enableLocalVideo(false)
        }
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.clientRoleType = role
        let isBroadcaster = role == .broadcaster
        mediaOptions.publishCameraTrack = isBroadcaster
        mediaOptions.publishMicrophoneTrack = isBroadcaster
        config.rtcEngine.updateChannelEx(with: mediaOptions, connection: connection)
    }
    
    /// 更换订阅音视频流策略
    /// - Parameter type: <#type description#>
    private func _updateAutoSubscribe(type: CallAutoSubscribeType) {
        guard let config = self.config else { return }
        guard let connection = rtcConnection else {return}
        callPrint("_updateAutoSubscribe: \(type.rawValue)")
        let mediaOptions = AgoraRtcChannelMediaOptions()
        switch type {
        case .none:
            mediaOptions.autoSubscribeAudio = false
            mediaOptions.autoSubscribeVideo = false
        case .video:
            mediaOptions.autoSubscribeAudio = false
            mediaOptions.autoSubscribeVideo = true
        case .audioVideo:
            mediaOptions.autoSubscribeAudio = true
            mediaOptions.autoSubscribeVideo = true
        }
        
        config.rtcEngine.updateChannelEx(with: mediaOptions, connection: connection)
    }
    
    //离开RTC频道
    private func _leaveRTC() {
        joinRtcCompletion = nil
        guard let rtcConnection = self.rtcConnection else {
            callWarningPrint("leave RTC channel failed, not joined the channel")
            return
        }
        
        config?.rtcEngine.stopPreview()
        let ret = config?.rtcEngine.leaveChannelEx(rtcConnection)
        callPrint("leave RTC channel[\(ret ?? -1)]")
        self.rtcConnection = nil
    }
    
    /// 设置画布
    private func setupCanvas() {
        guard let config = self.config, let prepareConfig = prepareConfig else {
            return
        }
        _setupLocalVideo(uid: config.userId, canvasView: prepareConfig.localView)
        guard let callingUserId = connectInfo.callingUserId else {
            callWarningPrint("join rtc fail: callingUserId == nil")
            return
        }
        _setupRemoteVideo(uid: callingUserId, canvasView: tempRemoteCanvasView)
    }
    
    /// 发送report message
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
    
    private func _reportCostEvent(type: CallConnectCostType) {
        let cost = _getCost()
        connectInfo.callCostMap[type.rawValue] = cost
        _reportEvent(key: type.rawValue, value: cost)
    }
    
    private func _reportMethod(event: String, label: String = "") {
        let msgId = "scenarioAPI"
        callPrint("_reportMethod event: \(event) label: \(label)")
        var subEvent = event
        if let range = event.range(of: "(") {
            subEvent = String(event[..<range.lowerBound])
        }
        var labelValue = "callId=\(connectInfo.callId)&ts=\(_getTimeInMs())"
        if !label.isEmpty {
            labelValue = "\(label)&\(labelValue)"
        }
        if isChannelJoined {
            _sendCustomReportMessage(msgId: msgId, category: kReportCategory, event: subEvent, label: labelValue, value: 0)
            return
        }
        
        let info = CallReportInfo(msgId: msgId, category: kReportCategory, event: subEvent, label: labelValue, value: 0)
        reportInfoList.append(info)
        reportInfoList = reportInfoList.suffix(10)
//        callPrint("sendCustomReportMessage not join channel cache it! event: \(subEvent) label: \(labelValue)")
    }
    
    private func _reportEvent(key: String, value: Int) {
        guard let config = config else { return }
        let msgId = "uid=\(config.userId)&roomId=\(connectInfo.callingRoomId ?? "")"
        let label = "callId=\(connectInfo.callId)&ts=\(_getTimeInMs())"
        if isChannelJoined {
            _sendCustomReportMessage(msgId: msgId, category: kReportCategory, event: key, label: label, value: value)
            return
        }
        
        let info = CallReportInfo(msgId: msgId, category: kReportCategory, event: key, label: label, value: value)
        reportInfoList.append(info)
        reportInfoList = reportInfoList.suffix(10)
//        callPrint("sendCustomReportMessage not join channel cache it! msgId:\(msgId) category:\(kReportCategory) event: \(key) label:\(connectInfo.callId) value:\(value)")
    }
    
    private func _sendCustomReportMessage(msgId: String,
                                          category: String,
                                          event: String,
                                          label: String,
                                          value: Int) {
        guard let config = config, isChannelJoined, let rtcConnection = rtcConnection else { return }
        let _ = config.rtcEngine.sendCustomReportMessageEx(msgId, category: category, event: event, label: label, value: value, connection: rtcConnection)
        #if DEBUG
//        callPrint("sendCustomReportMessage[\(ret)] msgId:\(msgId) event:\(event) label:\(label) value: \(value)")
        #endif
    }
}

//MARK: on Message
extension CallApiImpl {
    private func _processRespEvent(reason: CallAction, message: [String: Any]) {
        switch reason {
        case .call:
            _onCall(message: message)
        case .cancelCall:
            _onCancel(message: message)
        case .reject:
            _onReject(message: message)
        case .accept:
            _onAccept(message: message)
        case .hangup:
            _onHangup(message: message)
        }
    }
    
    //取消呼叫
    private func _cancelCall(message: [String: Any]? = nil, completion: ((NSError?) -> ())?) {
        guard let userId = connectInfo.callingUserId else {
            completion?(NSError(domain: "cancelCall fail! callingUserId is empty", code: -1))
            callWarningPrint("cancelCall fail! callingUserId is empty")
            return
        }
        let message: [String: Any] = message ?? _messageDic(action: .cancelCall)
        messageManager?.sendMessage(userId: "\(userId)", message: message) { err in
            completion?(err)
            guard let error = err else { return }
            self._notifySendMessageErrorEvent(error: error, reason: "cancelCall fail: ")
        }
    }
    
    private func _reject(remoteUserId: UInt, 
                         reason: String?,
                         rejectByInternal: Bool = false,
                         completion: ((NSError?) -> ())? = nil) {
        let message = _rejectMessageDic(reason: reason, rejectByInternal: rejectByInternal)
        _reject(remoteUserId: remoteUserId, message: message, completion: completion)
    }
    
    private func _reject(remoteUserId: UInt,
                         message: [String: Any],
                         completion: ((NSError?) -> ())? = nil) {
        messageManager?.sendMessage(userId: "\(remoteUserId)",
                                    message: message,
                                    completion: completion)
    }
    
    private func _hangup(remoteUserId: UInt,
                         message: [String: Any]? = nil,
                         completion: ((NSError?) -> ())? = nil) {
        messageManager?.sendMessage(userId: "\(remoteUserId)",
                                    message: message ?? _messageDic(action: .hangup),
                                    completion: completion)
    }
}

//MARK: on resp
extension CallApiImpl {
    //收到呼叫消息
    private func _onCall(message: [String: Any]) {
        let fromRoomId = message[kFromRoomId] as? String ?? ""
        let fromUserId = message[kFromUserId] as? UInt ?? 0
        let callId = message[kCallId] as? String ?? ""
        
        var enableNotify: Bool = true
        var autoAccept = false//prepareConfig?.autoAccept ?? false
        switch state {
        case .prepared:
            break
        case .idle, .failed:
            // not reachable
//            _reject(remoteUserId: fromUserId, reason: kRejectReasonCallBusy, rejectByInternal: true)
            return
        case .calling, .connecting, .connected:
            if connectInfo.callingUserId ?? 0 != fromUserId {
                _reject(remoteUserId: fromUserId, reason: kRejectReasonCallBusy, rejectByInternal: true)
                return
            }
            if state == .calling {
                enableNotify = false
            } else {
                autoAccept = true
            }
        }
        
        connectInfo.set(userId: fromUserId, roomId: fromRoomId, callId: callId)
        if enableNotify {
            _updateAndNotifyState(state: .calling, stateReason: .none, eventInfo: message)
            _notifyEvent(event: .onCalling)
        }
        
        if calleeJoinRTCPolicy == .calling {
            _joinRTCAsBroadcaster(roomId: fromRoomId)
        }
        
        guard autoAccept else {
            return
        }
        
        accept(remoteUserId: fromUserId) { err in
        }
    }
    
    //收到取消呼叫消息
    fileprivate func _onCancel(message: [String: Any]) {
        //如果不是来自的正在呼叫的用户的操作，不处理
        guard _isCallingUser(message: message) else { return }
        
        _updateAndNotifyState(state: .prepared, stateReason: .remoteCancel, eventInfo: message)
        _notifyEvent(event: .remoteCancel)
    }
    
    //收到拒绝消息
    fileprivate func _onReject(message: [String: Any]) {
        guard _isCallingUser(message: message) else { return }
        var stateReason: CallStateReason =  .remoteRejected
        var callEvent: CallEvent =  .remoteRejected
        if let rejectByInternal = message[kRejectByInternal] as? Int, rejectByInternal == 1 {
            stateReason = .remoteCallBusy
            callEvent = .remoteCallBusy
        }
        _updateAndNotifyState(state: .prepared, stateReason: stateReason, eventInfo: message)
        _notifyEvent(event: callEvent)
    }
    
    //收到接受消息
    fileprivate func _onAccept(message: [String: Any]) {
        //需要是calling状态，并且来自呼叫的用户的请求
        guard state == .calling, _isCallingUser(message: message) else { return }
//        let elapsed = _getTimeInMs() - (connectInfo.callTs ?? 0)
        //TODO: 如果已经connected
        //并且是isLocalAccepted（发起呼叫或者已经accept过了），否则认为本地没有同意
        if connectInfo.isLocalAccepted {
            _updateAndNotifyState(state: .connecting, stateReason: .remoteAccepted, eventInfo: message)
        }
        _notifyEvent(event: .remoteAccepted)
    }
    
    //收到挂断消息
    fileprivate func _onHangup(message: [String: Any]) {
        guard _isCallingUser(message: message) else { return }

        _updateAndNotifyState(state: .prepared, stateReason: .remoteHangup, eventInfo: message)
        _notifyEvent(event: .remoteHangup)
    }
}

//MARK: CallApiProtocol
extension CallApiImpl: CallApiProtocol {
    public func getCallId() -> String {
        _reportMethod(event: "\(#function)")
        return connectInfo.callId
    }
    
    public func initialize(config: CallConfig) {
        _reportMethod(event: "\(#function)", label: "appId=\(config.appId)&userId=\(config.userId)")
        if state != .idle {
            callWarningPrint("must invoke 'deinitialize' to clean state")
            return
        }
        
        self.config = config.cloneConfig()
    }
    
    public func deinitialize(completion: @escaping (()->())) {
        _reportMethod(event: "\(#function)")
        switch state {
        case .calling:
            cancelCall {[weak self] err in
                self?._deinitialize()
                completion()
            }
        case .connecting, .connected:
            let callingUserId = connectInfo.callingUserId ?? 0
            _hangup(remoteUserId: callingUserId) {[weak self] err in
                self?._deinitialize()
                completion()
            }
        default:
            self._deinitialize()
            completion()
        }
    }
    
    public func renewToken(with rtcToken: String, rtmToken: String) {
        _reportMethod(event: "\(#function)", label: "rtcToken=\(rtcToken)&rtmToken=\(rtmToken)")
        guard let roomId = prepareConfig?.roomId else {
            callWarningPrint("renewToken failed, roomid missmatch")
            return
        }
        self.prepareConfig?.rtcToken = rtcToken
        self.prepareConfig?.rtmToken = rtmToken
        callPrint("renewToken with roomId[\(roomId)]")
        messageManager?.renewToken(rtcToken: rtcToken, rtmToken: rtmToken)
        guard let connection = rtcConnection else {
            return
        }
        let options = AgoraRtcChannelMediaOptions()
        options.token = rtcToken
        let ret = self.config?.rtcEngine.updateChannelEx(with: options, connection: connection)
        callPrint("rtc[\(roomId)] renewToken ret = \(ret ?? -1)")
    }
    
    public func prepareForCall(prepareConfig: PrepareConfig, completion: ((NSError?) -> ())?) {
        _reportMethod(event: "\(#function)", label: "roomId=\(prepareConfig.roomId)&autoJoinRTC=\(prepareConfig.autoJoinRTC)")
//        let date = Date()
        _prepareForCall(prepareConfig: prepareConfig) { err in
            if let err = err {
                self._updateAndNotifyState(state: .failed,
                                           stateReason: .rtmSetupFailed,
                                           eventReason: err.localizedDescription)
                completion?(err)
                return
            }
            self._updateAndNotifyState(state: .prepared)
            completion?(nil)
        }
    }
    
    public func addListener(listener: CallApiListenerProtocol) {
        _reportMethod(event: "\(#function)")
        if delegates.contains(listener) { return }
        delegates.add(listener)
    }
    
    public func removeListener(listener: CallApiListenerProtocol) {
        _reportMethod(event: "\(#function)")
        delegates.remove(listener)
    }
    
    //呼叫
    public func call(remoteUserId: UInt, completion: ((NSError?) -> ())?) {
        guard let fromRoomId = prepareConfig?.roomId else {
            _reportMethod(event: "\(#function)", label: "remoteUserId=\(remoteUserId)")
            let reason = "call fail! config or roomId is empty"
            completion?(NSError(domain: reason, code: -1))
            callWarningPrint(reason)
            return
        }
        
        guard state == .prepared else {
            _reportMethod(event: "\(#function)", label: "remoteUserId=\(remoteUserId)")
            let reason = "call fail! state busy or not initialized"
            completion?(NSError(domain: reason, code: -1))
            callWarningPrint(reason)
            return
        }
        
        //发送呼叫消息
        connectInfo.set(userId: remoteUserId, roomId: fromRoomId, callId: UUID().uuidString, isLocalAccepted: true)
        //ensure that the report log contains a call
        _reportMethod(event: "\(#function)", label: "remoteUserId=\(remoteUserId)")
        
        let message: [String: Any] = _callMessageDic(remoteUserId: remoteUserId, fromRoomId: fromRoomId)
        messageManager?.sendMessage(userId: "\(remoteUserId)", message: message) {[weak self] err in
            guard let self = self else { return }
            completion?(err)
            if let error = err {
//                self._updateAndNotifyState(state: .prepared, stateReason: .messageFailed, eventReason: error.localizedDescription)
                self._notifySendMessageErrorEvent(error: error, reason: "call fail: ")
            } else {
                self._notifyEvent(event: .remoteUserRecvCall)
            }
        }
        
        _updateAndNotifyState(state: .calling, eventInfo: message)
        _notifyEvent(event: .onCalling)
        
        _joinRTCAsBroadcaster(roomId: fromRoomId)
    }
    
    //取消呼叫
    public func cancelCall(completion: ((NSError?) -> ())?) {
        _reportMethod(event: "\(#function)")
        let message: [String: Any] = _messageDic(action: .cancelCall)
        _cancelCall(message: message, completion: completion)
        _updateAndNotifyState(state: .prepared, stateReason: .localCancel, eventInfo: message)
        _notifyEvent(event: .localCancel)
    }
    
    //接受
    public func accept(remoteUserId: UInt, completion: ((NSError?) -> ())?) {
        _reportMethod(event: "\(#function)", label: "remoteUserId=\(remoteUserId)")
        guard let roomId = connectInfo.callingRoomId else {
            let errReason = "accept fail! current userId or roomId is empty"
            completion?(NSError(domain: errReason, code: -1))
            return
        }
        
        //查询是否是calling状态，如果是prapared，表示可能被主叫取消了
        guard state == .calling else {
            let errReason = "accept fail! current state[\(state.rawValue)] is not calling"
            completion?(NSError(domain: errReason, code: -1))
            _notifyEvent(event: .stateMismatch, eventReason: errReason)
            return
        }
        
        connectInfo.set(userId: remoteUserId, roomId: roomId, isLocalAccepted: true)
        
        let message: [String: Any] = _messageDic(action: .accept)
        messageManager?.sendMessage(userId: "\(remoteUserId)", message: message) { err in
            completion?(err)
            guard let error = err else { return }
            self._notifySendMessageErrorEvent(error: error, reason: "accept fail: ")
        }
        
        _updateAndNotifyState(state: .connecting, stateReason: .localAccepted, eventInfo: message)
        _notifyEvent(event: .localAccepted)
        
        if calleeJoinRTCPolicy == .accepted {
            _joinRTCAsBroadcaster(roomId: roomId)
        }
    }
    
    //拒绝
    public func reject(remoteUserId: UInt, reason: String?, completion: ((NSError?) -> ())?) {
        _reportMethod(event: "\(#function)", label: "remoteUserId=\(remoteUserId)&reason=\(reason ?? "")")
        let message = _rejectMessageDic(reason: reason, rejectByInternal: false)
        _reject(remoteUserId: remoteUserId, message: message) {[weak self] err in
            completion?(err)
            guard let self = self, let error = err else { return }
            self._notifySendMessageErrorEvent(error: error, reason: "reject fail: ")
        }
        _updateAndNotifyState(state: .prepared, stateReason: .localRejected, eventInfo: message)
        _notifyEvent(event: .localRejected)
    }
    
    //挂断
    public func hangup(remoteUserId: UInt, reason: String?, completion: ((NSError?) -> ())?) {
        _reportMethod(event: "\(#function)", label: "remoteUserId=\(remoteUserId)")
        let message = _hangupMessageDic(reason: reason)
        _hangup(remoteUserId: remoteUserId, message: message) {[weak self] err in
            completion?(err)
            guard let self = self, let error = err else { return }
            self._notifySendMessageErrorEvent(error: error, reason: "hangup fail: ")
        }
        _updateAndNotifyState(state: .prepared, stateReason: .localHangup, eventInfo: message)
        _notifyEvent(event: .localHangup)
    }
    
    private func addRTCListener(listener: AgoraRtcEngineDelegate) {
        _reportMethod(event: "\(#function)")
        rtcProxy.addListener(listener)
    }
    
    private func removeRTCListener(listener: AgoraRtcEngineDelegate) {
        _reportMethod(event: "\(#function)")
        rtcProxy.removeListener(listener)
    }
}

//MARK: CallMessageDelegate
extension CallApiImpl: CallMessageDelegate {
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, tokenPrivilegeWillExpire channel: String?) {
        _notifyTokenPrivilegeWillExpire()
    }
    
    public func onConnectionFail() {
        //TODO: 内部重试，rtm 2.2.0支持
        _updateAndNotifyState(state: .failed, stateReason: .rtmLost)
        _notifyEvent(event: .rtmLost)
    }
    
    //收到RTM消息
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, didReceiveMessageEvent event: AgoraRtmMessageEvent) {
        let message = event.message
        guard let data = message.rawData,
              let dic = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let messageAction = CallAction(rawValue: dic[kMessageAction] as? UInt ?? 0),
//              let msgTs = dic[kMessageTs] as? Int,
//              let userId = dic[kFromUserId] as? UInt,
              let messageVersion = dic[kMessageVersion] as? String else {
            callWarningPrint("fail to parse message: \(message.rawData?.count ?? 0)")
            return
        }
        
        //TODO: compatible other message version
        guard kCurrentMessageVersion == messageVersion else { return }
        callPrint("on event message: \(String(data: data, encoding: .utf8) ?? "")")
        _processRespEvent(reason: messageAction, message: dic)
    }
    
    func debugInfo(message: String, logLevel: Int) {
        callPrint(message)
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
        guard connectInfo.callingUserId == uid/*, let config = config*/ else { return }
        _notifyEvent(event: .remoteJoin)
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        callPrint("didOfflineOfUid: \(uid)")
        guard connectInfo.callingUserId == uid else { return }
        _notifyEvent(event: .remoteLeave)
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didLeaveChannelWith stats: AgoraChannelStats) {
        callPrint("didLeaveChannelWith")
        isChannelJoined = false
        /*
         由于leave rtc到didLeaveChannelWith是异步的
         这里rtcConnection = nil会导致leave之后马上join，didLeaveChannelWith会在join之后错误的置空了rtc connection
         */
//        rtcConnection = nil
        _notifyEvent(event: .localLeave)
    }

    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        callPrint("join RTC channel, didJoinChannel: \(uid), channel: \(channel) elapsed: \(elapsed)")
        guard uid == config?.userId ?? 0 else { return }
        isChannelJoined = true
        //TODO: 因为频道外上报需要指定固定频道id(即sdk根据id自己缓存了n条记录)，换了id和后续加入的频道id不一致上传会失败
        _flushReport()
        joinRtcCompletion?(nil)
        joinRtcCompletion = nil
        _notifyEvent(event: .localJoin)
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
//        callWarningPrint("didOccurError: \(errorCode.rawValue)")
//        joinRtcCompletion?(NSError(domain: "join RTC fail", code: errorCode.rawValue))
//        joinRtcCompletion = nil
        _notifyRtcOccurErrorEvent(errorCode: errorCode.rawValue)
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit,
                          remoteVideoStateChangedOfUid uid: UInt,
                          state: AgoraVideoRemoteState,
                          reason: AgoraVideoRemoteReason,
                          elapsed: Int) {
        let channelId = prepareConfig?.roomId ?? ""
        guard uid == connectInfo.callingUserId else {return}
        callPrint("didLiveRtcRemoteVideoStateChanged channelId: \(channelId)/\(connectInfo.callingRoomId ?? "") uid: \(uid)/\(connectInfo.callingUserId ?? 0) state: \(state.rawValue) reason: \(reason.rawValue)")
        if state == .decoding /*2*/,
           ( reason == .remoteUnmuted /*6*/ || reason == .localUnmuted /*4*/ || reason == .localMuted /*3*/ )   {
            DispatchQueue.main.async {
                self.firstFrameCompletion?()
            }
        }
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit,
                          firstLocalVideoFramePublishedWithElapsed elapsed: Int,
                          sourceType: AgoraVideoSourceType) {
        _notifyEvent(event: .publishFirstLocalVideoFrame, eventReason: "elapsed: \(elapsed)ms")
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, firstLocalVideoFrameWith size: CGSize, elapsed: Int, sourceType: AgoraVideoSourceType) {
        _notifyEvent(event: .captureFirstLocalVideoFrame, eventReason: "elapsed: \(elapsed)ms")
        config?.rtcEngine.removeDelegate(self.localFrameProxy)
    }
}

let formatter = DateFormatter()
#if DEBUG
func debugApiPrint(_ message: String) {
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    let timeString = formatter.string(from: Date())
    print("\(timeString) \(message)")
}
#endif

extension CallApiImpl {
    func callPrint(_ message: String, _ logLevel: CallLogLevel = .normal) {
//        #if DEBUG
//        debugApiPrint("[CallApi]\(message)")
//        #else
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
        let timeString = formatter.string(from: Date())
        for element in delegates.allObjects {
            (element as? CallApiListenerProtocol)?.callDebugInfo?(message: "\(timeString) \(message)", logLevel: logLevel)
        }
//        #endif
    }
    
    func callWarningPrint(_ message: String) {
        callPrint(message, .warning)
    }

    func callProfilePrint(_ message: String) {
        callPrint("[Profile]\(message)")
    }
}
