//
//  Pure1v1UserListViewController.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/7/20.
//

import UIKit
import YYCategories
import CallAPI
import AgoraRtcKit
import AgoraCommon
import AgoraRtmKit

//当前api设置的状态
struct Pure1v1APISetupStatus: OptionSet {
    let rawValue: Int
    
    static let idle = Pure1v1APISetupStatus(rawValue: 1 << 0)
    static let token = Pure1v1APISetupStatus(rawValue: 1 << 1)
    static let rtm = Pure1v1APISetupStatus(rawValue: 1 << 2)
    static let syncService = Pure1v1APISetupStatus(rawValue: 1 << 3)
    static let callApi = Pure1v1APISetupStatus(rawValue: 1 << 4)
}

private let kShowGuideAlreadyKey = "already_show_guide"
class Pure1v1UserListViewController: UIViewController {
    var userInfo: Pure1v1UserInfo?
    private let prepareConfig = PrepareConfig()
    private var setupStatus: Pure1v1APISetupStatus = .idle
    
    private lazy var player: AgoraRtcMediaPlayerProtocol? = {
        let player = rtcEngine.createMediaPlayer(with: self)
        player?.mute(true)
        player?.setLoopCount(-1)
        player?.adjustPlayoutVolume(0)
        player?.setRenderMode(.hidden)
        return player
    }()
    
    private var rtcToken: String = ""
    private var rtmToken: String = ""
    private lazy var rtcEngine = _createRtcEngine()
    private lazy var rtmClient: AgoraRtmClientKit = createRtmClient(appId: pure1V1AppId!, userId: userInfo!.userId)
    private var rtmManager: CallRtmManager?
    private var callState: CallStateType = .idle
    private var connectedUserId: UInt?
    private var connectedChannelId: String?
    private lazy var callVC: Pure1v1CallViewController = {
        let vc = Pure1v1CallViewController()
        vc.modalPresentationStyle = .fullScreen
        return vc
    }()
    private let callApi = CallApiImpl()
    
    private var userList: [Pure1v1UserInfo] = [] {
        didSet {
            let list = userList.filter({$0.userId != self.userInfo?.userId})
            self.listView.userList = list
            self.noDataView.isHidden = list.count > 0
            self._showGuideIfNeed()
        }
    }
    
    //UI
    private lazy var naviBar: Pure1v1NaviBar = Pure1v1NaviBar(frame: CGRect(x: 0,
                                                                            y: UIDevice.current.aui_SafeDistanceTop,
                                                                            width: self.view.aui_width,
                                                                            height: 44))
    private var service: Pure1v1ServiceProtocol?
    private lazy var noDataView: Pure1v1UserNoDataView = Pure1v1UserNoDataView(frame: self.view.bounds)
    private lazy var listView: Pure1v1UserPagingListView = {
        let listView = Pure1v1UserPagingListView(frame: self.view.bounds)
        listView.callClosure = { [weak self] user in
            guard let user = user else {return}
            self?._call(user: user)
        }
        listView.refreshBeginClousure = { [weak self] in
            self?._refreshAction()
        }
        return listView
    }()
    
    private lazy var bgImgView: UIImageView = {
        let imgView = UIImageView()
        imgView.image = UIImage.scene1v1Image(name: "roomList")
        imgView.frame = self.view.bounds
        return imgView
    }()
    
    private weak var callDialog: Pure1v1Dialog?
    
    
    //life cycle
    deinit {
        pure1v1Print("deinit-- Pure1v1UserListViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        view.addSubview(bgImgView)
        view.addSubview(noDataView)
        view.addSubview(listView)
        view.addSubview(naviBar)
        naviBar.backButton.addTarget(self, action: #selector(_backAction), for: .touchUpInside)
        naviBar.refreshButton.addTarget(self, action: #selector(_refreshAction), for: .touchUpInside)
        naviBar.refreshButton.isHidden = true
        
        listView.localUserInfo = userInfo
        
        callVC.currentUser = userInfo
        callVC.callApi = callApi
        callVC.rtcEngine = rtcEngine
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        _autoRefrshAction()
    }
}

//MARK: setup & invoke api/service
extension Pure1v1UserListViewController {
    private func _generateTokens(completion: @escaping (String?, String?) -> ()) {
        if setupStatus.contains(.token), rtcToken.count > 0, rtmToken.count > 0 {
            completion(rtcToken, rtmToken)
            return
        }
        
        NetworkManager.shared.generateTokens(appId: pure1V1AppId!,
                                             appCertificate: pure1V1AppCertificate!,
                                             channelName: "",
                                             uid: userInfo?.userId ?? "",
                                             tokenGeneratorType: .token007,
                                             tokenTypes: [.rtc, .rtm]) {[weak self] tokens in
            guard let self = self else {return}
            guard let rtcToken = tokens[AgoraTokenType.rtc.rawValue],
                  let rtmToken = tokens[AgoraTokenType.rtm.rawValue] else {
                completion(nil, nil)
                return
            }
            self.rtcToken = rtcToken
            self.rtmToken = rtmToken
            completion(rtcToken, rtmToken)
        }
    }
    
    private func _setupAPIConfig(completion: @escaping (NSError?) -> Void) {
        _generateTokens {[weak self] rtcToken, rtmToken in
            guard let self = self else { return }
            guard let rtcToken = rtcToken, let rtmToken = rtmToken else {
                completion(NSError(domain: "generate token fail", code: -1))
                return
            }
            self.setupStatus = [self.setupStatus, .token]
            self._setupRtm(completion: {[weak self] err in
                guard let self = self else { return }
                if let err = err {
                    completion(err)
                    return
                }
                self.setupStatus = [self.setupStatus, .rtm]
                self._setupService()
                self.setupStatus = [self.setupStatus, .syncService]
                self._setupCallApi()
                self.setupStatus = [self.setupStatus, .callApi]
                
                completion(nil)
            })
        }
    }
    
    private func _setupRtm(completion: @escaping (NSError?) -> Void) {
        if setupStatus.contains(.rtm) {
            completion(nil)
            return
        }
        
        rtmClient.logout()
        rtmClient.login(self.rtmToken) { resp, err in
            var error: NSError? = nil
            if let err = err {
                error = NSError(domain: err.reason, code: err.errorCode.rawValue)
            }
            completion(error)
        }
    }
    
    private func _setupService() {
        guard setupStatus.contains(.rtm), let userInfo = userInfo else {
            pure1v1Error("_setupService fail! rtm not initizlized or userInfo == nil")
            return
        }
        if setupStatus.contains(.syncService) { 
            pure1v1Warn("_setupService fail! service already setup")
            return
        }
        let service = Pure1v1ServiceImp(appId: pure1V1AppId!, user: userInfo, rtmClient: rtmClient)
        service.subscribeUserListChanged {[weak self] userList in
            self?.userList = userList
        }
        self.service = service
    }
    
    private func _setupCallApi() {
        guard setupStatus.contains(.rtm), let userInfo = userInfo else {
            pure1v1Error("_setupCallApi fail! rtm not initizlized or userInfo == nil")
            return
        }
        if setupStatus.contains(.callApi) {
            pure1v1Warn("_setupCallApi fail! service already setup")
            return
        }
            
        //初始化rtm manager并login
        let userId = self.userInfo?.userId ?? ""
        let rtmManager = CallRtmManager(appId: pure1V1AppId!,
                                        userId: userId,
                                        rtmClient: rtmClient)
        rtmManager.delegate = self
        self.rtmManager = rtmManager
        _initCallAPI(rtcToken: self.rtcToken, rtmToken: self.rtmToken)
    }
    
    private func _initCallAPI(rtcToken: String, rtmToken: String) {
        pure1v1Print("_initCallAPI")
        
        let signalClient = CallRtmSignalClient(rtmClient: self.rtmManager!.getRtmClient())
        
        let config = CallConfig()
        config.appId = pure1V1AppId!
        config.userId = UInt(userInfo?.userId ?? "")!
        config.rtcEngine = rtcEngine
        config.signalClient = signalClient
        callApi.deinitialize {
        }
        callApi.initialize(config: config)
        callApi.addListener(listener: self)
        
        prepareConfig.rtcToken = rtcToken
//        prepareConfig.rtmToken = rtmToken
        prepareConfig.roomId = userInfo?.getRoomId() ?? NSString.withUUID()
        prepareConfig.localView = callVC.localCanvasView.canvasView
        prepareConfig.remoteView = callVC.remoteCanvasView.canvasView
        prepareConfig.autoJoinRTC = false  // 如果期望立即加入自己的RTC呼叫频道，则需要设置为true
        prepareConfig.userExtension = userInfo?.yy_modelToJSONObject() as? [String: Any]
        callApi.prepareForCall(prepareConfig: prepareConfig) { err in
            // 成功即可以开始进行呼叫
        }
    }
    
    private func _createRtcEngine() ->AgoraRtcEngineKit {
        let config = AgoraRtcEngineConfig()
        config.appId = pure1V1AppId!
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        let engine = AgoraRtcEngineKit.sharedEngine(with: config,
                                                    delegate: nil)
        
        engine.setClientRole(.broadcaster)
        return engine
    }
    
    private func _call(user: Pure1v1UserInfo) {
        pure1v1Print("_call with state:\(callState.rawValue)")
        if callState == .idle || callState == .failed {
            _setupAPIConfig { _ in
            }
            AUIToast.show(text: "call_not_init".pure1v1Localization())
            return
        }
        guard let remoteUserId = UInt(user.userId) else {
            pure1v1Warn("_call fail, userId invalid: \(user.userId) \(user.userName)")
            return
        }
        AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self, completion: nil)
        AgoraEntAuthorizedManager.checkCameraAuthorized(parent: self)
        callApi.call(remoteUserId: remoteUserId) {[weak self] err in
            guard let err = err else {return}
            self?.callApi.cancelCall(completion: { err in
            })
        }
    }
    
    private func _updateCallChannel() {
        prepareConfig.roomId = userInfo?.getRoomId() ?? NSString.withUUID()
        callApi.prepareForCall(prepareConfig: prepareConfig) { _ in
        }
    }
}

// MARK: UI action
extension Pure1v1UserListViewController {
    private func _showGuideIfNeed() {
        guard listView.userList.count > 1 else {return}
        if UserDefaults.standard.bool(forKey: kShowGuideAlreadyKey) == true {return}
        let guideView = Pure1v1GuideView(frame: self.view.bounds)
        self.view.addSubview(guideView)
        UserDefaults.standard.set(true, forKey: kShowGuideAlreadyKey)
    }
    
    @objc func _backAction() {
        AgoraRtcEngineKit.destroy()
        callApi.deinitialize {
        }
        service?.leaveRoom { err in
        }
        
        AgoraRtcEngineKit.destroy()
        rtmManager?.logout()
        self.navigationController?.popViewController(animated: true)
    }
    
    func _autoRefrshAction(){
        self.listView.autoRefreshing()
    }
    
    @objc func _refreshAction() {
        let date = Date()
        _setupAPIConfig {[weak self] error in
            if let error = error {
                pure1v1Error("refresh _setupAPIConfig fail: \(error.localizedDescription)")
                self?.listView.endRefreshing()
                AUIToast.show(text: error.localizedDescription)
                return
            }
            pure1v1Print("refresh setupAPI cost: \(Int(-date.timeIntervalSinceNow * 1000))ms")
            self?.service?.enterRoom {[weak self] error in
                if let error = error {
                    pure1v1Error("refresh enterRoom fail: \(error.localizedDescription)")
                    self?.listView.endRefreshing()
                    AUIToast.show(text: error.localizedDescription)
                    return
                }
                pure1v1Print("refresh enterRoom cost: \(Int(-date.timeIntervalSinceNow * 1000))ms")
                self?.service?.getUserList { list, error in
                    guard let self = self else {return}
                    self.listView.endRefreshing()
                    if let error = error {
                        pure1v1Error("refresh getUserList fail: \(error.localizedDescription)")
                        AUIToast.show(text: error.localizedDescription)
                        return
                    }
                    pure1v1Print("refresh getUserList cost: \(Int(-date.timeIntervalSinceNow * 1000))ms")
                    self.userList = list
                    if error != nil {
                        AUIToast.show(text: error!.description)
                    }
                }
            }
        }
    }
}

//MARK: CallApiListenerProtocol
extension Pure1v1UserListViewController: CallApiListenerProtocol {
    func onCallStateChanged(with state: CallStateType,
                            stateReason: CallStateReason,
                            eventReason: String,
                            eventInfo: [String : Any]) {
        let currentUid = userInfo?.userId ?? ""
        pure1v1Print("onCallStateChanged state: \(state.rawValue), stateReason: \(stateReason.rawValue), eventReason: \(eventReason), eventInfo: \(eventInfo)")
        
        self.callState = state
        
        switch state {
        case .calling:
            if presentedViewController is Pure1v1CallViewController {
                return
            }
            
            let fromUserId = eventInfo[kFromUserId] as? UInt ?? 0
            let fromRoomId = eventInfo[kFromRoomId] as? String ?? ""
            let toUserId = eventInfo[kRemoteUserId] as? UInt ?? 0
            pure1v1Print("calling: fromUserId: \(fromUserId) fromRoomId: \(fromRoomId) currentId: \(currentUid) toUserId: \(toUserId)")
            if let connectedUserId = connectedUserId, connectedUserId != fromUserId {
                callApi.reject(remoteUserId: fromUserId, reason: "already calling") { err in
                }
                return
            }
            
            // 触发状态的用户是自己才处理
            if currentUid == "\(toUserId)" {
                connectedUserId = fromUserId
                connectedChannelId = fromRoomId
                
                //被叫不一定在userList能查到，需要从callapi里读取发送用户的user extension
                var user = listView.userList.first {$0.userId == "\(fromUserId)"}
                if user == nil, let userDic = (eventInfo[kFromUserExtension] as? [String: Any]) {
                    user = Pure1v1UserInfo.yy_model(with: userDic)
                }
                if let user = user {
                    callDialog?.hiddenAnimation()
                    let dialog = Pure1v1CalleeDialog.show(user: user)
                    assert(dialog != nil, "dialog = nil")
                    dialog?.acceptClosure = { [weak self] in
                        guard let self = self else {return}
                        AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self, completion: nil)
                        AgoraEntAuthorizedManager.checkCameraAuthorized(parent: self)
                        self.callApi.accept(remoteUserId: fromUserId) { err in
                        }
                    }
                    
                    dialog?.rejectClosure = { [weak self] in
                        self?.callApi.reject(remoteUserId: fromUserId, reason: "reject by user") {err in
                        }
                    }
                    
                    callDialog = dialog
                    callVC.targetUser = user
                    startRing()
                } else {
                    pure1v1Print("callee user not found1")
                }
                
            } else if currentUid == "\(fromUserId)" {
                connectedUserId = toUserId
                connectedChannelId = fromRoomId
                //主叫userlist一定会有，因为需要点击
                if let user = listView.userList.first {$0.userId == "\(toUserId)"} {
                    let dialog = Pure1v1CallerDialog.show(user: user)
                    dialog?.cancelClosure = {[weak self] in
                        self?.callApi.cancelCall(completion: { err in
                        })
                    }
                    callDialog = dialog
                    callVC.targetUser = user
                    startDail()
                } else {
                    pure1v1Print("caller user not found1")
                }
            }
            break
        case .connecting:
            if let dialog = callDialog as? Pure1v1CalleeDialog {
                dialog.stateTitle = "call_state_connecting".pure1v1Localization()
            } else if let dialog = callDialog as? Pure1v1CallerDialog {
                dialog.stateTitle = "call_state_connecting".pure1v1Localization()
            }
            break
        case .connected:
            callDialog?.hiddenAnimation()
            hangUp()
            guard let uid = connectedUserId else {
                assert(false, "user not fount")
                return
            }
            
            callVC.rtcChannelName = connectedChannelId
            callVC.dismiss(animated: false)
            present(callVC, animated: false)
            
            //setup content inspect
            let connection = AgoraRtcConnection()
            connection.channelId = "\(connectedChannelId ?? "")"
            connection.localUid = UInt(userInfo?.userId ?? "") ?? 0
            setupContentInspectConfig(true, connection: connection)
            moderationAudio(channelName: connection.channelId)
            break
        case .prepared:
            switch stateReason {
            case .localHangup:
                _updateCallChannel()
            case .remoteHangup:
                _updateCallChannel()
                AUIToast.show(text: "call_toast_hangup".pure1v1Localization())
            case .remoteRejected, .remoteCallBusy:
                AUIToast.show(text: "call_toast_reject".pure1v1Localization())
//            case .callingTimeout:
//                AUIToast.show(text: "无应答")
//            case .localCancel, .remoteCancel:
//                AUIToast.show(text: "通话被取消")
            default:
                break
            }
            
            callVC.rtcChannelName = nil
//            AUIAlertManager.hiddenView()
            connectedUserId = nil
            connectedChannelId = nil
            callDialog?.hiddenAnimation()
            callVC.dismiss(animated: false)
            hangUp()
            break
        case .failed:
            AUIToast.show(text: eventReason)
//            AUIAlertManager.hiddenView()
            connectedUserId = nil
            connectedChannelId = nil
            callDialog?.hiddenAnimation()
            callVC.dismiss(animated: false)
            hangUp()
            break
        default:
            break
        }
    }
    
    func tokenPrivilegeWillExpire() {
        pure1v1Warn("tokenPrivilegeWillExpire")
        guard let userInfo = userInfo else {return}
        self.setupStatus.remove(.token)
        _generateTokens {[weak self] rtcToken, rtmToken in
            guard let self = self else {return}
            guard let rtcToken = rtcToken, let rtmToken = rtmToken else { return }
            self.callApi.renewToken(with: rtcToken)
            self.rtmClient.renewToken(rtmToken)
        }
    }
    
    func callDebugInfo(message: String, logLevel: CallLogLevel) {
        if logLevel == .normal {
            pure1v1Print(message, context: "CallApi")
        } else {
            pure1v1Warn(message, context: "CallApi")
        }
    }
}

//MARK: audio/video content inspect
extension Pure1v1UserListViewController {
    private func setupContentInspectConfig(_ enable: Bool, connection: AgoraRtcConnection) {
        let config = AgoraContentInspectConfig()
        let dic: [String: String] = [
            "id": "\(connection.localUid)",
            "sceneName": "Pure1v1",
            "userNo": "\(connection.localUid)"
        ]
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: dic, options: .prettyPrinted) else {
            pure1v1Error("setupContentInspectConfig fail")
            return
        }
        let jsonStr = String(data: jsonData, encoding: .utf8)
        config.extraInfo = jsonStr
        let module = AgoraContentInspectModule()
        module.interval = 60
        module.type = .imageModeration
        config.modules = [module]
        let ret = rtcEngine.enableContentInspectEx(enable, config: config, connection: connection)
        pure1v1Print("setupContentInspectConfig[\(enable)]: uid:\(connection.localUid) channelId: \(connection.channelId) ret:\(ret)")
    }
    
    /// 语音审核
    private func moderationAudio(channelName: String) {
        let userInfo = ["id": userInfo?.userId ?? "",
                        "sceneName": "Pure1v1",
                        "userNo": userInfo?.userId ?? "",
                        "userName": userInfo?.userName ?? ""] as NSDictionary
        let parasm: [String: Any] = ["appId": pure1V1AppId!,
                                     "channelName": channelName,
                                     "channelType": AgoraChannelProfile.liveBroadcasting.rawValue,
                                     "traceId": NSString.withUUID().md5(),
                                     "src": "iOS",
                                     "payload": userInfo.yy_modelToJSONString()]
        NetworkManager.shared.postRequest(urlString: "https://toolbox.bj2.shengwang.cn/v1/moderation/audio",
                                          params: parasm) { response in
            pure1v1Print("moderationAudio response === \(response)")
        } failure: { errr in
            pure1v1Error(errr)
        }
    }
}


private let VideoResources = [
    "https://download.agora.io/demo/test/calling_show_1.mp4",
    "https://download.agora.io/demo/test/calling_show_2.mp4",
    "https://download.agora.io/demo/test/calling_show_4.mp4",
]

private let RingURL = "https://download.agora.io/demo/test/1v1_bgm1.wav"

//MARK: ring and video play when call
extension Pure1v1UserListViewController {
    
    // 开始拨打
    private func startDail(){
        startRing()
        startVideoPlayer()
    }
    
    // 挂断
    private func hangUp(){
        stopRing()
        stopVideoPlayer()
    }
    
    // 响铃
    private func startRing(){
        let ret = rtcEngine.startAudioMixing(RingURL, loopback: false, cycle: -1)
        pure1v1Print(" startAudioMixing ret = \(ret)")
    }
    
    // 停止响铃
    private func stopRing(){
        rtcEngine.stopAudioMixing()
//        _autoRefrshAction()
    }
    
    private func randomVideoURL() -> String {
        VideoResources[Int(arc4random()) % 3]
    }
    
    // 播放视频
    private func startVideoPlayer(){
        let musicPath = randomVideoURL()
        let source = AgoraMediaSource()
        source.autoPlay = true
        source.enableCache = true
        source.url = musicPath
        if let callDialog = callDialog as? Pure1v1CallerDialog {
            player?.setView(callDialog.videoView)
            player?.open(with: source)
        }
    }
    
    // 停止视频
    func stopVideoPlayer(){
        player?.stop()
    }
}

//MARK: AgoraRtcMediaPlayerDelegate
extension Pure1v1UserListViewController: AgoraRtcMediaPlayerDelegate {
    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        if state == .openCompleted {
            playerKit.play()
        }
    }
}

//MARK: ICallRtmManagerListener
extension Pure1v1UserListViewController: ICallRtmManagerListener {
    func onConnected() {
        pure1v1Print("onConnected")
    }
    
    func onDisconnected() {
        pure1v1Print("onDisconnected")
    }
    
    func onConnectionLost() {
        pure1v1Print("onConnectionLost")
        AUIToast.show(text: "call_toast_disconnect".pure1v1Localization())
        self.setupStatus.remove(.rtm)
        //掉线了，需要重新enter，否则对端看不到
        self.service?.leaveRoom(completion: { _ in
        })
        _autoRefrshAction()
    }
    
    func onTokenPrivilegeWillExpire(channelName: String) {
        pure1v1Print("onTokenPrivilegeWillExpire")
        self.tokenPrivilegeWillExpire()
    }
}
