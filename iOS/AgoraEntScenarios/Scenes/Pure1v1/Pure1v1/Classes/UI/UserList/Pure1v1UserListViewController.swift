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
import AudioScenarioApi


func pure1v1CreateRtmClient(appId: String, userId: String) -> AgoraRtmClientKit {
    let rtmConfig = AgoraRtmClientConfig(appId: appId, userId: userId)
    rtmConfig.presenceTimeout = 30
    let logConfig = AgoraRtmLogConfig()
    logConfig.filePath = AgoraEntLog.rtmSdkLogPath()
    logConfig.fileSizeInKB = 1024
    logConfig.level = .info
    rtmConfig.logConfig = logConfig
    var rtmClient: AgoraRtmClientKit? = nil
    do {
        rtmClient = try AgoraRtmClientKit(rtmConfig, delegate: nil)
    } catch {
        Pure1v1Logger.info("create rtm client fail: \(error.localizedDescription)")
    }
    return rtmClient!
}

//当前api设置的状态
struct Pure1v1APISetupStatus: OptionSet {
    let rawValue: Int
    
    static let idle = Pure1v1APISetupStatus(rawValue: 1 << 0)
    static let token = Pure1v1APISetupStatus(rawValue: 1 << 1)
    static let rtm = Pure1v1APISetupStatus(rawValue: 1 << 2)
    static let syncService = Pure1v1APISetupStatus(rawValue: 1 << 3)
    static let callApi = Pure1v1APISetupStatus(rawValue: 1 << 4)
}

class TokenObject {
    var rtmToken: String = ""
    var rtcToken: String = ""
    var updateTime: Date
    
    init(rtmToken: String, rtcToken: String) {
        self.rtmToken = rtmToken
        self.rtcToken = rtcToken
        self.updateTime = Date()
    }
    
    func isValid() -> Bool {
        return rtmToken.count > 0 && rtcToken.count > 0
    }
    
    func checkExpired() {
        if Int64(-updateTime.timeIntervalSinceNow) < 20 * 60 * 60 {
            return
        }
        rtmToken = ""
        rtcToken = ""
    }
}

private let kShowGuideAlreadyKey = "already_show_guide"
class Pure1v1UserListViewController: UIViewController {
    var userInfo: Pure1v1UserInfo?
    private let prepareConfig = PrepareConfig()
    private var setupStatus: Pure1v1APISetupStatus = .idle
    
    private lazy var audioApi: AudioScenarioApi = AudioScenarioApi(rtcEngine: rtcEngine)
    
    private lazy var player: AgoraRtcMediaPlayerProtocol? = {
        let player = rtcEngine.createMediaPlayer(with: self)
        player?.mute(true)
        player?.setLoopCount(-1)
        player?.adjustPlayoutVolume(0)
        player?.setRenderMode(.hidden)
        return player
    }()
    
    private var tokenObj = TokenObject(rtmToken: "", rtcToken: "") {
        didSet {
            self.prepareConfig.rtcToken = tokenObj.rtcToken
        }
    }
    private lazy var stateManager: AppStateManager = {
        let manager = AppStateManager()
        manager.appStateChangeHandler = { [weak self] isInBackground in
            if isInBackground { return }
            self?.checkTokenValid()
        }
        
        manager.networkStatusChangeHandler = { [weak self] isAvailable in
            guard isAvailable else { return }
            self?.checkTokenValid()
        }
        
        return manager
    }()
    
    private lazy var rtcEngine = _createRtcEngine()
    private lazy var rtmClient: AgoraRtmClientKit = pure1v1CreateRtmClient(appId: AppContext.shared.appId, userId: userInfo!.userId)
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
            Pure1v1Logger.info("update userList: \(userList.count)")
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
        Pure1v1Logger.info("deinit-- Pure1v1UserListViewController")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        _ = stateManager
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
        
        if AppContext.shared.isDeveloperMode {
            //如果开启了debug模式
            let debugBtn = UIButton(frame: CGRect(x: 20, y: view.height - 100, width: 80, height: 80))
            debugBtn.backgroundColor = .blue
            debugBtn.layer.cornerRadius = 40;
            debugBtn.layer.masksToBounds = true;
            debugBtn.setTitleColor(.white, for: .normal)
            debugBtn.setTitle("Debug", for: .normal)
            debugBtn.addTarget(self, action: #selector(onDebugAction), for: .touchUpInside)
            view.addSubview(debugBtn)
            
            AppContext.shared.resetDebugConfig(engine: rtcEngine)
        }
        _autoRefrshAction()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
//        _autoRefrshAction()
        checkTokenValid()
    }
    
    @objc func onDebugAction() {
        let vc = DebugSettingViewController(engine: rtcEngine)
        navigationController?.pushViewController(vc, animated: true)
    }
}

//MARK: setup & invoke api/service
extension Pure1v1UserListViewController {
    private func checkTokenValid() {
        tokenObj.checkExpired()
        if tokenObj.isValid() { return }
        tokenPrivilegeWillExpire()
    }
    
    private func _generateTokens(completion: @escaping (String?, String?) -> ()) {
        if setupStatus.contains(.token), tokenObj.isValid() {
            completion(tokenObj.rtcToken, tokenObj.rtmToken)
            return
        }
        let date = Date()
        NetworkManager.shared.generateToken(channelName: "",
                                            uid: userInfo?.userId ?? "",
                                            tokenTypes: [.rtc, .rtm]) {[weak self] token in
            guard let self = self else {return}
            Pure1v1Logger.info("generateTokens cost: \(-Int(date.timeIntervalSinceNow * 1000))ms")
            guard let rtcToken = token, let rtmToken = token else {
                completion(nil, nil)
                return
            }
            RttApiManager.shared.setBasicAuth(token: rtcToken)
            self.tokenObj = TokenObject(rtmToken: rtmToken, rtcToken: rtcToken)
            completion(rtcToken, rtmToken)
        }
    }
    
    func _generateRttTokens(channelName: String, completion: @escaping (Bool) -> ()) {
        if (RttManager.shared.subBotToken != "" && RttManager.shared.pubBotToken != "") {
            completion(true)
            return
        }
        
        var rtcToken: String?
        var rtcToken2: String?

        let dispatchGroup = DispatchGroup()

        // 2. 生成 token for subBotUid
        dispatchGroup.enter()
        NetworkManager.shared.generateToken(channelName: channelName,
                                             uid: RttManager.shared.subBotUid,
                                             tokenTypes: [.rtc]) { [weak self] token in
            defer { dispatchGroup.leave() }
            guard let self = self else { return }
            rtcToken = token // 将 token 赋值给 rtcToken
            print("RttManager subbot token: \(rtcToken)")
        }

        // 3. 生成 token for pubBotUid
        dispatchGroup.enter()
        NetworkManager.shared.generateToken(channelName: channelName,
                                             uid: RttManager.shared.pubBotUid,
                                             tokenTypes: [.rtc]) { [weak self] token in
            defer { dispatchGroup.leave() }
            guard let self = self else { return }
            rtcToken2 = token // 将 token 赋值给 rtcToken2
            print("RttManager pubbot token: \(rtcToken2)")
        }

        // 4. 通知在主队列上执行
        dispatchGroup.notify(queue: .main) {
            // 在这里访问 rtcToken 和 rtcToken2
            guard let rtcToken = rtcToken, let rtcToken2 = rtcToken2 else {
                // 如果 rtcToken 或 rtcToken2 为空，处理错误情况
                completion(false)
                return
            }
            
            RttManager.shared.subBotToken = rtcToken
            RttManager.shared.pubBotToken = rtcToken2
            completion(true)
            return
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
        
        let date = Date()
        rtmClient.logout()
        rtmClient.login(tokenObj.rtmToken) { resp, err in
            Pure1v1Logger.info("rtm login cost: \(-Int(date.timeIntervalSinceNow * 1000))ms")
            var error: NSError? = nil
            if let err = err {
                error = NSError(domain: err.reason, code: err.errorCode.rawValue)
            }
            completion(error)
        }
    }
    
    private func _setupService() {
        guard setupStatus.contains(.rtm), let userInfo = userInfo else {
            Pure1v1Logger.error("_setupService fail! rtm not initizlized or userInfo == nil")
            return
        }
        if setupStatus.contains(.syncService) { 
            Pure1v1Logger.warn("_setupService fail! service already setup")
            return
        }
        let service = Pure1v1ServiceImp(user: userInfo, rtmClient: rtmClient)
        service.subscribeUserListChanged {[weak self] userList in
            self?.userList = userList
        }
        self.service = service
    }
    
    private func _setupCallApi() {
        guard setupStatus.contains(.rtm), let userInfo = userInfo else {
            Pure1v1Logger.error("_setupCallApi fail! rtm not initizlized or userInfo == nil")
            return
        }
        if setupStatus.contains(.callApi) {
            Pure1v1Logger.warn("_setupCallApi fail! service already setup")
            return
        }
            
        //初始化rtm manager并login
        let userId = self.userInfo?.userId ?? ""
        let rtmManager = CallRtmManager(appId: AppContext.shared.appId,
                                        userId: userId,
                                        rtmClient: rtmClient)
        rtmManager.delegate = self
        self.rtmManager = rtmManager
        _initCallAPI()
    }
    
    private func _initCallAPI() {
        Pure1v1Logger.info("_initCallAPI")
        
        let signalClient = CallRtmSignalClient(rtmClient: self.rtmManager!.getRtmClient())
        
        let config = CallConfig()
        config.appId = AppContext.shared.appId
        config.userId = UInt(userInfo?.userId ?? "")!
        config.rtcEngine = rtcEngine
        config.signalClient = signalClient
        callApi.deinitialize {
        }
        callApi.initialize(config: config)
        callApi.addListener(listener: self)
        
        prepareConfig.roomId = NSString.withUUID()
        prepareConfig.localView = callVC.localCanvasView.canvasView
        prepareConfig.remoteView = callVC.remoteCanvasView.canvasView
        prepareConfig.userExtension = userInfo?.yy_modelToJSONObject() as? [String: Any]
        callApi.prepareForCall(prepareConfig: prepareConfig) { err in
            // 成功即可以开始进行呼叫
        }
    }
    
    private func _createRtcEngine() ->AgoraRtcEngineKit {
        let config = AgoraRtcEngineConfig()
        config.appId = AppContext.shared.appId
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        let logConfig = AgoraLogConfig()
        logConfig.filePath = AgoraEntLog.sdkLogPath()
        config.logConfig = logConfig
        let engine = AgoraRtcEngineKit.sharedEngine(with: config,
                                                    delegate: nil)
        
        engine.setClientRole(.broadcaster)
        return engine
    }
    
    private func _call(user: Pure1v1UserInfo) {
        Pure1v1Logger.info("_call with state:\(callState.rawValue)")
        if callState == .idle || callState == .failed {
            _setupAPIConfig { _ in
            }
            AUIToast.show(text: "call_not_init".pure1v1Localization())
            return
        }
        guard let remoteUserId = UInt(user.userId) else {
            Pure1v1Logger.warn("_call fail, userId invalid: \(user.userId) \(user.userName)")
            return
        }
        AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self, completion: nil)
        AgoraEntAuthorizedManager.checkCameraAuthorized(parent: self)
        callApi.call(remoteUserId: remoteUserId) {[weak self] err in
            guard let self = self else {return}
            guard let err = err, self.callState == .calling else {return}
            self.callApi.cancelCall(completion: { err in
            })
            
            let msg = "\("call_toast_callfail".pure1v1Localization()): \(err.code)"
            AUIToast.show(text: msg)
        }
    }
    
    private func _updateCallChannel() {
        prepareConfig.roomId = NSString.withUUID()
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
        callApi.deinitialize {
        }
        service?.leaveRoom { err in
        }
        
        rtmManager?.logout()
        rtmClient.destroy()
        AgoraRtcEngineKit.destroy()
        AgoraEntLog.autoUploadLog(scene: Pure1v1Logger.kLogKey)
        self.navigationController?.popViewController(animated: true)
    }
    
    func _autoRefrshAction(){
        self.listView.autoRefreshing()
    }
    
    @objc func _refreshAction() {
        let date = Date()
        _setupAPIConfig {[weak self] error in
            guard let self = self else { return }
            if let error = error {
                Pure1v1Logger.error("refresh _setupAPIConfig fail: \(error.localizedDescription)")
                self.listView.endRefreshing()
                AUIToast.show(text: "\("user_list_get_fail".pure1v1Localization())\(error.code)")
                return
            }
            guard let service = self.service else { return }
            Pure1v1Logger.info("refresh setupAPI cost: \(Int(-date.timeIntervalSinceNow * 1000))ms")
            service.enterRoom {[weak self] error in
                guard let self = self, let service = self.service else { return }
                if let error = error {
                    Pure1v1Logger.error("refresh enterRoom fail: \(error.localizedDescription)")
                    self.listView.endRefreshing()
                    AUIToast.show(text: "\("user_list_get_fail".pure1v1Localization())\(error.code)")
                    return
                }
                Pure1v1Logger.info("refresh enterRoom cost: \(Int(-date.timeIntervalSinceNow * 1000))ms")
                service.getUserList {[weak self] list, error in
                    guard let self = self else {return}
                    self.listView.endRefreshing()
                    if let error = error {
                        Pure1v1Logger.error("refresh getUserList fail: \(error.localizedDescription)")
                        AUIToast.show(text: "\("user_list_get_fail".pure1v1Localization())\(error.code)")
                        return
                    }
                    Pure1v1Logger.info("refresh getUserList cost: \(Int(-date.timeIntervalSinceNow * 1000))ms")
                    self.userList = list
                }
            }
        }
    }
    
    private func showWarmAlertView() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            WarmAlertView.show { v in
                if let alert = v as? WarmAlertView {
                    alert.sceneSeconds = AppContext.shared.sceneConfig?.oneToOne ?? 10 * 60
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
        Pure1v1Logger.info("onCallStateChanged state: \(state.rawValue), stateReason: \(stateReason.rawValue), eventReason: \(eventReason), eventInfo: \(eventInfo)")
        
        self.callState = state
        
        switch state {
        case .calling:
            if presentedViewController is Pure1v1CallViewController {
                return
            }
            
            let fromUserId = eventInfo[kFromUserId] as? UInt ?? 0
            let fromRoomId = eventInfo[kFromRoomId] as? String ?? ""
            let toUserId = eventInfo[kRemoteUserId] as? UInt ?? 0
            Pure1v1Logger.info("calling: fromUserId: \(fromUserId) fromRoomId: \(fromRoomId) currentId: \(currentUid) toUserId: \(toUserId)")
            if let connectedUserId = connectedUserId, connectedUserId != fromUserId {
                callApi.reject(remoteUserId: fromUserId, reason: "already calling") { err in
                }
                return
            }
            update1v1VideoEncoder(engine: rtcEngine, roomId: fromRoomId, userId: Int(currentUid) ?? 0)
            // 触发状态的用户是自己才处理
            if currentUid == "\(toUserId)" {
                audioApi.setAudioScenario(sceneType: .Chat, audioScenarioType: .Chat_Callee)
                resetRttSettings(isCaller: false)
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
                        self.callApi.accept(remoteUserId: fromUserId) {[weak self] err in
                            guard let err = err else { return }
                            self?.callApi.reject(remoteUserId: fromUserId, reason: "", completion: { _ in
                            })
                            let msg = "\("call_toast_acceptfail".pure1v1Localization()): \(err.code)"
                            AUIToast.show(text: msg)
                        }
                    }
                    
                    dialog?.rejectClosure = { [weak self] in
                        self?.callApi.reject(remoteUserId: fromUserId, reason: "reject by user") {err in
                        }
                    }
                    
                    callDialog = dialog
                    callVC.targetUser = user
                    RttManager.shared.targetUid = user.userId
                    startRing()
                } else {
                    Pure1v1Logger.info("callee user not found1")
                }
                
            } else if currentUid == "\(fromUserId)" {
                audioApi.setAudioScenario(sceneType: .Chat, audioScenarioType: .Chat_Caller)
                resetRttSettings(isCaller: true)
                connectedUserId = toUserId
                connectedChannelId = fromRoomId
                //主叫userlist一定会有，因为需要点击
                if let user = listView.userList.first {$0.userId == "\(toUserId)"} {
                    let dialog = Pure1v1CallerDialog.show(user: user) { [weak self] in
                        guard let self = self, self.callState == .calling else {return}
                        self.startDail()
                    }
                    dialog?.cancelClosure = {[weak self] in
                        self?.callApi.cancelCall(completion: { err in
                        })
                    }
                    callDialog = dialog
                    callVC.targetUser = user
                    RttManager.shared.targetUid = user.userId
                } else {
                    Pure1v1Logger.info("caller user not found1")
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
            _generateRttTokens(channelName: connectedChannelId!) { success in }
            showWarmAlertView()
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
            case .remoteCallBusy:
                AUIToast.show(text: "call_toast_busy".pure1v1Localization())
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
        Pure1v1Logger.warn("tokenPrivilegeWillExpire")
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
            Pure1v1Logger.info(message, tag: "CallApi")
        } else {
            Pure1v1Logger.warn(message, tag: "CallApi")
        }
    }
    
    func resetRttSettings(isCaller: Bool) {
        if (isCaller) {
            RttManager.shared.subBotUid = "30000"
            RttManager.shared.pubBotUid = "40000"
        } else {
            RttManager.shared.subBotUid = "10000"
            RttManager.shared.pubBotUid = "20000"
        }
        RttManager.shared.subBotToken = ""
        RttManager.shared.pubBotToken = ""
        RttManager.shared.selectedSourceLanguageIndex = 29
        RttManager.shared.selectedTargetLanguageIndex = 21
        RttManager.shared.disableRtt(force: true) { Bool in }
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
            Pure1v1Logger.error("setupContentInspectConfig fail")
            return
        }
        let jsonStr = String(data: jsonData, encoding: .utf8)
        config.extraInfo = jsonStr
        let module = AgoraContentInspectModule()
        module.interval = 60
        module.type = .imageModeration
        config.modules = [module]
        let ret = rtcEngine.enableContentInspectEx(enable, config: config, connection: connection)
        Pure1v1Logger.info("setupContentInspectConfig[\(enable)]: uid:\(connection.localUid) channelId: \(connection.channelId) ret:\(ret)")
    }
    
    /// 语音审核
    private func moderationAudio(channelName: String) {
        NetworkManager.shared.voiceIdentify(channelName: channelName,
                                            channelType: AgoraChannelProfile.liveBroadcasting.rawValue,
                                            sceneType: "Pure1v1") { errStr in
            guard let errStr = errStr else {return}
            Pure1v1Logger.error("moderationAudio fail === \(errStr)")
        }
//        let userInfo = ["id": userInfo?.userId ?? "",
//                        "sceneName": "Pure1v1",
//                        "userNo": userInfo?.userId ?? "",
//                        "userName": userInfo?.userName ?? ""] as NSDictionary
//        let parasm: [String: Any] = ["appId": pure1V1AppId!,
//                                     "channelName": channelName,
//                                     "channelType": AgoraChannelProfile.liveBroadcasting.rawValue,
//                                     "traceId": NSString.withUUID().md5(),
//                                     "src": "iOS",
//                                     "payload": userInfo.yy_modelToJSONString()]
//        NetworkManager.shared.postRequest(urlString: "https://toolbox.bj2.shengwang.cn/v1/moderation/audio",
//                                          params: parasm) { response in
//            Pure1v1Logger.info("moderationAudio response === \(response)")
//        } failure: { errr in
//            Pure1v1Logger.error(errr)
//        }
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
        Pure1v1Logger.info(" startAudioMixing ret = \(ret)")
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
    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, reason: AgoraMediaPlayerReason) {
        if state == .openCompleted {
            playerKit.play()
        }
    }
}

//MARK: ICallRtmManagerListener
extension Pure1v1UserListViewController: ICallRtmManagerListener {
    func onConnected() {
        Pure1v1Logger.info("onConnected")
    }
    
    func onDisconnected() {
        Pure1v1Logger.info("onDisconnected")
    }
    
    func onTokenPrivilegeWillExpire(channelName: String) {
        Pure1v1Logger.info("onTokenPrivilegeWillExpire")
        self.tokenPrivilegeWillExpire()
    }
}
