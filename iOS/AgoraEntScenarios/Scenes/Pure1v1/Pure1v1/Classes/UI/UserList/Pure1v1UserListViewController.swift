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


private let kShowGuideAlreadyKey = "already_show_guide"
class Pure1v1UserListViewController: UIViewController {
    var userInfo: Pure1v1UserInfo?
    private let prepareConfig = PrepareConfig()
    
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
    private var callState: CallStateType = .idle
    private var connectedUserId: UInt?
    private var connectedChannelId: String?
    private lazy var callVC: Pure1v1CallViewController = {
        let vc = Pure1v1CallViewController()
        vc.modalPresentationStyle = .fullScreen
        return vc
    }()
    private let callApi = CallApiImpl()
    private lazy var naviBar: Pure1v1NaviBar = Pure1v1NaviBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.aui_width, height: 44))
    private lazy var service: Pure1v1ServiceProtocol = Pure1v1ServiceImp(appId: pure1V1AppId!, user: userInfo)
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
        _refreshAction()
        
        listView.localUserInfo = userInfo
        
        _setupCallApi()
        
        callVC.currentUser = userInfo
        callVC.callApi = callApi
        callVC.rtcEngine = rtcEngine
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        listView.reloadData()
    }
    
    private func _setupCallApi() {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            return
        }
        
        if rtcToken.count > 0, rtmToken.count > 0 {
            _initCallAPI(rtcToken: rtcToken, rtmToken: rtmToken)
            return
        }
        
        _generateTokens {[weak self] rtcToken, rtmToken in
            guard let self = self else {return}
            guard let rtcToken = rtcToken, let rtmToken = rtmToken else { return }
            self.rtcToken = rtcToken
            self.rtmToken = rtmToken
            self._initCallAPI(rtcToken: rtcToken, rtmToken: rtmToken)
        }
    }
    
    private func _showGuideIfNeed() {
        guard listView.userList.count > 1 else {return}
        if UserDefaults.standard.bool(forKey: kShowGuideAlreadyKey) == true {return}
        let guideView = Pure1v1GuideView(frame: self.view.bounds)
        self.view.addSubview(guideView)
        UserDefaults.standard.set(true, forKey: kShowGuideAlreadyKey)
    }
}

extension Pure1v1UserListViewController {
    private func _initCallAPI(rtcToken: String, rtmToken: String) {
        pure1v1Print("_initCallAPI")
        let config = CallConfig()
        config.appId = pure1V1AppId!
        config.userId = UInt(userInfo?.userId ?? "")!
        config.rtcEngine = rtcEngine
        config.rtmClient = nil
        callApi.deinitialize {
        }
        callApi.initialize(config: config)
        callApi.addListener(listener: self)
        
        prepareConfig.rtcToken = rtcToken
        prepareConfig.rtmToken = rtmToken
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
            _setupCallApi()
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
}

extension Pure1v1UserListViewController {
    @objc func _backAction() {
        AgoraRtcEngineKit.destroy()
        callApi.deinitialize {
        }
        service.leaveRoom { err in
        }
        self.navigationController?.popViewController(animated: true)
    }
    
    @objc func _refreshAction() {
        service.enterRoom {[weak self] error in
            if let error = error {
                self?.listView.endRefreshing()
                AUIToast.show(text: error.localizedDescription)
                return
            }
            self?.service.getUserList { list, error in
                guard let self = self else {return}
                self.listView.endRefreshing()
                if let error = error {
                    AUIToast.show(text: error.localizedDescription)
                    return
                }
                let userList = list.filter({$0.userId != self.userInfo?.userId})
                self.listView.userList = userList
                self.noDataView.isHidden = userList.count > 0
                self._showGuideIfNeed()
                AUIToast.show(text: "user_list_refresh_tips".pure1v1Localization())
            }
        }
    }
    
    private func _generateTokens(completion: @escaping (String?, String?) -> ()) {
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
            
            completion(rtcToken, rtmToken)
        }
    }
    
    private func _updateCallChannel() {
        prepareConfig.roomId = userInfo?.getRoomId() ?? NSString.withUUID()
        callApi.prepareForCall(prepareConfig: prepareConfig) { _ in
        }
    }
}

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
                    stopRing()
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
            case .remoteRejected:
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
            if stateReason == .rtmLost {
                AUIToast.show(text: "call_toast_disconnect".pure1v1Localization())
                _setupCallApi()
            }
            break
        default:
            break
        }
    }
    
    func tokenPrivilegeWillExpire() {
        pure1v1Warn("tokenPrivilegeWillExpire")
        guard let userInfo = userInfo else {return}
        
        _generateTokens {[weak self] rtcToken, rtmToken in
            guard let self = self else {return}
            guard let rtcToken = rtcToken, let rtmToken = rtmToken else { return }
            self.rtcToken = rtcToken
            self.rtmToken = rtmToken
            self.callApi.renewToken(with: rtcToken, rtmToken: rtmToken)
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
        module.interval = 30
//        module.type = .imageModeration
//        config.modules = [module]
//        let ret = rtcEngine.enableContentInspectEx(enable, config: config, connection: connection)
//        pure1v1Print("setupContentInspectConfig[\(enable)]: uid:\(connection.localUid) channelId: \(connection.channelId) ret:\(ret)")
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
        _refreshAction()
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

extension Pure1v1UserListViewController: AgoraRtcMediaPlayerDelegate {
    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        if state == .openCompleted {
            playerKit.play()
        }
    }
}
