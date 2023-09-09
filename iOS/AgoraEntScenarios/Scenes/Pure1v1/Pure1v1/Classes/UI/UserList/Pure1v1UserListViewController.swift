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

private let kShowGuideAlreadyKey = "already_show_guide"
class Pure1v1UserListViewController: UIViewController {
    var userInfo: Pure1v1UserInfo? {
        didSet {
            callVC.currentUser = userInfo
        }
    }
    
    private let tokenConfig: CallTokenConfig = CallTokenConfig()
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
        return listView
    }()
    
    private weak var callDialog: Pure1v1Dialog?
    
    deinit {
        pure1v1Print("deinit-- Pure1v1UserListViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.addSubview(noDataView)
        view.addSubview(listView)
        view.addSubview(naviBar)
        naviBar.backButton.addTarget(self, action: #selector(_backAction), for: .touchUpInside)
        naviBar.refreshButton.addTarget(self, action: #selector(_refreshAction), for: .touchUpInside)
        naviBar.refreshButton.isHidden = true
        naviBar.refreshButton.isHidden = false
        _refreshAction()
        
        _setupCallApi()
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
        
        if tokenConfig.rtcToken.count > 0, tokenConfig.rtmToken.count > 0 {
            _initCallAPI(tokenConfig: self.tokenConfig)
            return
        }
        
        tokenConfig.roomId = userInfo.getRoomId()
        NetworkManager.shared.generateTokens(appId: pure1V1AppId!,
                                             appCertificate: pure1V1AppCertificate!,
                                             channelName: tokenConfig.roomId,
                                             uid: userInfo.userId,
                                             tokenGeneratorType: .token007,
                                             tokenTypes: [.rtc, .rtm]) {[weak self] tokens in
            guard let self = self else {return}
            guard let rtcToken = tokens[AgoraTokenType.rtc.rawValue],
                  let rtmToken = tokens[AgoraTokenType.rtm.rawValue] else {
                return
            }
            self.tokenConfig.rtcToken = rtcToken
            self.tokenConfig.rtmToken = rtmToken
            
            self._initCallAPI(tokenConfig: self.tokenConfig)
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
    private func _initCallAPI(tokenConfig: CallTokenConfig) {
        let config = CallConfig()
        config.role = .caller  // Pure 1v1 can only be set as the caller
        config.mode = .pure1v1
        config.appId = pure1V1AppId!
        config.userId = UInt(userInfo?.userId ?? "")!
        config.autoAccept = false
        config.rtcEngine = rtcEngine
        config.localView = callVC.localCanvasView.canvasView
        config.remoteView = callVC.remoteCanvasView.canvasView
        if let userExtension = userInfo?.yy_modelToJSONObject() as? [String: Any] {
            config.userExtension = userExtension
        }
        callApi.initialize(config: config, token: tokenConfig) {[weak self] error in
            guard let self = self else {return}
            // Requires active call to prepareForCall
            let prepareConfig = PrepareConfig.calleeConfig()
            self.callApi.prepareForCall(prepareConfig: prepareConfig) { err in
            }
        }
        callApi.addListener(listener: self)
        
        callVC.callApi = callApi
        callVC.rtcEngine = rtcEngine
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
        if callState == .idle {
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
        callApi.call(roomId: user.getRoomId(), remoteUserId: remoteUserId) { err in
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
        guard naviBar.refreshAnimationEnable() else {return}
        naviBar.startRefreshAnimation()
        service.enterRoom {[weak self] error in
            if let error = error {
                self?.naviBar.stopRefreshAnimation()
                AUIToast.show(text: error.localizedDescription)
                return
            }
            self?.service.getUserList { list, error in
                guard let self = self else {return}
                self.naviBar.stopRefreshAnimation()
                if let error = error {
                    AUIToast.show(text: error.localizedDescription)
                    return
                }
                let userList = list.filter({$0.userId != self.userInfo?.userId})
                self.listView.userList = userList
                self._showGuideIfNeed()
                self.naviBar.style = userList.count > 0 ? .light : .dark
                AUIToast.show(text: "user_list_refresh_tips".pure1v1Localization())
            }
        }
    }
}

extension Pure1v1UserListViewController: CallApiListenerProtocol {
    func onCallStateChanged(with state: CallStateType,
                            stateReason: CallReason,
                            eventReason: String,
                            elapsed: Int,
                            eventInfo: [String : Any]) {
        let currentUid = userInfo?.userId ?? ""
        let publisher = eventInfo[kPublisher] as? String ?? currentUid
        guard publisher == currentUid else {
            return
        }
        pure1v1Print("onCallStateChanged state: \(state.rawValue), stateReason: \(stateReason.rawValue), eventReason: \(eventReason), elapsed: \(elapsed) ms, eventInfo: \(eventInfo) publisher: \(publisher) / \(currentUid)")
        
        self.callState = state
        
        switch state {
        case .calling:
            if presentedViewController == callVC {
                return
            }
            
            let fromUserId = eventInfo[kFromUserId] as? UInt ?? 0
            let fromRoomId = eventInfo[kFromRoomId] as? String ?? ""
            let toUserId = eventInfo[kRemoteUserId] as? UInt ?? 0
            pure1v1Print("calling: fromUserId: \(fromUserId) fromRoomId: \(fromRoomId) currentId: \(currentUid) toUserId: \(toUserId)")
            if let connectedUserId = connectedUserId, connectedUserId != fromUserId {
                callApi.reject(roomId: fromRoomId, remoteUserId: fromUserId, reason: "already calling") { err in
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
                        NetworkManager.shared.generateTokens(appId: pure1V1AppId!,
                                                             appCertificate: pure1V1AppCertificate!,
                                                             channelName: fromRoomId,
                                                             uid: "\(toUserId)",
                                                             tokenGeneratorType: .token007,
                                                             tokenTypes: [.rtc]) { tokens in
                            guard let self = self else {return}
                            guard tokens.count == 1 else {
                                pure1v1Print("generateTokens fail")
                                self.view.isUserInteractionEnabled = true
                                return
                            }
                            let rtcToken = tokens[AgoraTokenType.rtc.rawValue]!
                            
                            AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self, completion: nil)
                            AgoraEntAuthorizedManager.checkCameraAuthorized(parent: self)
                            self.callApi.accept(roomId: fromRoomId, remoteUserId: fromUserId, rtcToken: rtcToken) { err in
                            }
                        }
                    }
                    
                    dialog?.rejectClosure = { [weak self] in
                        self?.callApi.reject(roomId: fromRoomId, remoteUserId: fromUserId, reason: "reject by user") {err in
                        }
                    }
                    
                    callDialog = dialog
                    callVC.targetUser = user
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
//            AUIToast.show(text: "通话开始\(eventInfo[kDebugInfo] as? String ?? "")", postion: .bottom)
//            AUIAlertManager.hiddenView()
            callDialog?.hiddenAnimation()
            guard let uid = connectedUserId else {
                assert(false, "user not fount")
                return
            }
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
            case .remoteHangup:
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
//            AUIAlertManager.hiddenView()
            connectedUserId = nil
            connectedChannelId = nil
            callDialog?.hiddenAnimation()
            callVC.dismiss(animated: false)
            break
        case .failed:
            AUIToast.show(text: eventReason)
//            AUIAlertManager.hiddenView()
            connectedUserId = nil
            connectedChannelId = nil
            callDialog?.hiddenAnimation()
            break
        default:
            break
        }
    }
    
    func tokenPrivilegeWillExpire() {
        pure1v1Warn("tokenPrivilegeWillExpire")
        guard let userInfo = userInfo else {return}
        
        //renew token, include caller token(current room)
        NetworkManager.shared.generateTokens(appId: pure1V1AppId!,
                                             appCertificate: pure1V1AppCertificate!,
                                             channelName: tokenConfig.roomId,
                                             uid: userInfo.userId,
                                             tokenGeneratorType: .token007,
                                             tokenTypes: [.rtc, .rtm]) {[weak self] tokens in
            guard let self = self else {return}
            guard let rtcToken = tokens[AgoraTokenType.rtc.rawValue],
                  let rtmToken = tokens[AgoraTokenType.rtm.rawValue] else {
                return
            }
            self.tokenConfig.rtcToken = rtcToken
            self.tokenConfig.rtmToken = rtmToken
            self.callApi.renewToken(with: self.tokenConfig)
        }
            
        //renew other caller room(current user is callee)
        if let channelName = connectedChannelId {
            //calling token
            NetworkManager.shared.generateTokens(appId: pure1V1AppId!,
                                                 appCertificate: pure1V1AppCertificate!,
                                                 channelName: channelName,
                                                 uid: userInfo.userId,
                                                 tokenGeneratorType: .token007,
                                                 tokenTypes: [.rtc]) {[weak self] tokens in
                guard let self = self else {return}
                guard let rtcToken = tokens[AgoraTokenType.rtc.rawValue] else {
                    return
                }
                
                self.callApi.renewRemoteCallerChannelToken(roomId: channelName, token: rtcToken)
            }
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
        NetworkManager.shared.postRequest(urlString: "https://toolbox.bj2.agoralab.co/v1/moderation/audio",
                                          params: parasm) { response in
            pure1v1Print("moderationAudio response === \(response)")
        } failure: { errr in
            pure1v1Error(errr)
        }
    }
}
