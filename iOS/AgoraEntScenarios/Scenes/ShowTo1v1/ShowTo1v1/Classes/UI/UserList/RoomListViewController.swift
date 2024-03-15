//
//  RoomListViewController.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/28.
//

import UIKit
import YYCategories
import CallAPI
import AgoraRtcKit
import VideoLoaderAPI
import AgoraCommon
import AgoraRtmKit
import AudioScenarioApi

private let randomRoomName = [
    "show_create_room_name1".showTo1v1Localization(),
    "show_create_room_name2".showTo1v1Localization(),
    "show_create_room_name3".showTo1v1Localization(),
    "show_create_room_name4".showTo1v1Localization(),
    "show_create_room_name5".showTo1v1Localization(),
    "show_create_room_name6".showTo1v1Localization(),
    "show_create_room_name7".showTo1v1Localization(),
    "show_create_room_name8".showTo1v1Localization(),
    "show_create_room_name9".showTo1v1Localization(),
    "show_create_room_name10".showTo1v1Localization(),
]

//当前api设置的状态
struct ShowTo1v1APISetupStatus: OptionSet {
    let rawValue: Int
    
    static let idle = ShowTo1v1APISetupStatus(rawValue: 1 << 0)
    static let token = ShowTo1v1APISetupStatus(rawValue: 1 << 1)
    static let rtm = ShowTo1v1APISetupStatus(rawValue: 1 << 2)
    static let syncService = ShowTo1v1APISetupStatus(rawValue: 1 << 3)
    static let api = ShowTo1v1APISetupStatus(rawValue: 1 << 4)
}

private let kShowGuideAlreadyKey = "already_show_guide_show1v1"
class RoomListViewController: UIViewController {
    var userInfo: ShowTo1v1UserInfo?
    private let prepareConfig = PrepareConfig()
    private var setupStatus: ShowTo1v1APISetupStatus = .idle
    
    private weak var preJoinRoom: ShowTo1v1RoomInfo?
    private var connectedUserId: UInt?
    private var connectedChannelId: String?
    private var rtcToken = "" {
        didSet {
            //refresh room info token
            let list = roomList
            self.roomList = list
        }
    }
    private var rtmToken = ""
    private lazy var rtcEngine: AgoraRtcEngineKit = _createRtcEngine()
    private lazy var rtmClient: AgoraRtmClientKit = createRtmClient(appId: showTo1v1AppId!, userId: userInfo!.uid)
    private var rtmManager: CallRtmManager?
    private var callState: CallStateType = .idle
    private let callApi = CallApiImpl()
    private lazy var audioApi: AudioScenarioApi = AudioScenarioApi(rtcEngine: rtcEngine)
    
    private var roomList: [ShowTo1v1RoomInfo] = [] {
        didSet {
            roomList.forEach { info in
                info.token = self.rtcToken
            }
            self.listView.roomList = roomList
            noDataView.isHidden = roomList.count > 0
            self._showGuideIfNeed()
        }
    }
    
    //UI
    private weak var callDialog: ShowTo1v1Dialog?
    private weak var createRoomDialog: CreateRoomDialog?
    private lazy var callVC: CallViewController = CallViewController()
    private lazy var naviBar = NaviBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.aui_width, height: 44))
    private var service: ShowTo1v1ServiceProtocol?
    private lazy var noDataView: RoomNoDataView = {
        let view = RoomNoDataView(frame: self.view.bounds)
        return view
    }()
    private lazy var listView: RoomPagingListView = {
        let listView = RoomPagingListView(frame: self.view.bounds, localUserInfo: userInfo!)
        listView.callClosure = { [weak self] roomInfo in
            guard let roomInfo = roomInfo else {return}
            self?._call(room: roomInfo)
        }
        listView.tapClosure = { [weak self] roomInfo in
            guard let roomInfo = roomInfo, let self = self else {return}
            
            let date = Date()
            self.preJoinRoom = roomInfo
            self._setupAPIConfig {[weak self] error in
                guard let self = self else {return}
                if let error = error {
                    AUIToast.show(text: error.localizedDescription)
                    showTo1v1Error("tapClosure fail! setupApi error: \(error.localizedDescription)")
                    return
                }
                
                showTo1v1Print("[setupApi]join broadcaster vc cost: \(Int(-date.timeIntervalSinceNow * 1000))ms")
                self.service?.joinRoom(roomInfo: roomInfo, completion: {[weak self] err in
                    guard let self = self else {return}
                    if let error = err {
                        if self.preJoinRoom?.roomId == roomInfo.roomId {
                            self.navigationController?.popToViewController(self, animated: false)
                            AUIToast.show(text: error.localizedDescription)
                            showTo1v1Error("tapClosure fail! joinRoom error: \(error.localizedDescription)")
                        }
                        return
                    }
                    showTo1v1Print("[create scene]join broadcaster vc cost: \(Int(-date.timeIntervalSinceNow * 1000))ms")
                    self._showBroadcasterVC(roomInfo: roomInfo)
                })
            }
        }
        listView.refreshBeginClousure = { [weak self] in
            self?._refreshAction()
        }
        return listView
    }()
    
    private lazy var bgImgView: UIImageView = {
        let imgView = UIImageView()
        imgView.image = UIImage.sceneImage(name: "roomList")
        imgView.frame = self.view.bounds
        return imgView
    }()
    
    private lazy var createButton: UIButton = {
        let button = UIButton(type: .custom)
        let width: CGFloat = 148
        let height: CGFloat = 46
        button.frame = CGRect(x: (self.view.aui_width - width) / 2,
                              y: self.view.aui_height - UIDevice.current.aui_SafeDistanceBottom - height - 19,
                              width: width,
                              height: height)
//        button.backgroundColor = UIColor(hexString: "#345dff")
        button.setCornerRadius(21)
        button.setTitle("user_list_create_room".showTo1v1Localization(), for: .normal)
        button.setImage(UIImage.sceneImage(name: "create_room"), for: .normal)
        button.setBackgroundImage(UIImage.sceneImage(name: "create_room_bg"), for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 18, weight: .bold)
        button.setTitleColor(.white, for: .normal)
        button.adjustHorizonAlign(spacing: 10)
        button.addTarget(self, action: #selector(_createAction), for: .touchUpInside)
        return button
    }()
    
    deinit {
        showTo1v1Print("deinit-- ShowTo1v1RoomListViewController")
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        showTo1v1Print("init-- ShowTo1v1RoomListViewController")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        view.addSubview(bgImgView)
        view.addSubview(noDataView)
        view.addSubview(listView)
        view.addSubview(naviBar)
        view.addSubview(createButton)
        naviBar.backButton.addTarget(self, action: #selector(_backAction), for: .touchUpInside)
        naviBar.refreshButton.addTarget(self, action: #selector(_refreshAction), for: .touchUpInside)
        naviBar.refreshButton.isHidden = true
//        naviBar.refreshButton.isHidden = false
//        _refreshAction()
        
        
        _setupAPI()
        callVC.callApi = callApi
        callVC.currentUser = userInfo
        callVC.rtcEngine = rtcEngine
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
//        videoLoaderApi.cleanCache()
        listView.reloadCurrentItem()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        _autoRefreshAction()
    }
    
    private func _showGuideIfNeed() {
        guard listView.roomList.count > 1 else {return}
        if UserDefaults.standard.bool(forKey: kShowGuideAlreadyKey) == true {return}
        let guideView = RoomListGuideView(frame: self.view.bounds)
        self.view.addSubview(guideView)
        UserDefaults.standard.set(true, forKey: kShowGuideAlreadyKey)
    }
}

extension RoomListViewController {
    private func _setupAPIConfig(completion: @escaping (NSError?) -> Void) {
        renewTokens {[weak self] success in
            guard let self = self else { return }
            guard success else {
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
                self._setupAPI()
                self.setupStatus = [self.setupStatus, .api]
                
                completion(nil)
            })
        }
    }
    
    private func renewTokens(completion: ((Bool)->Void)?) {
        if setupStatus.contains(.token), rtcToken.count > 0, rtmToken.count > 0 {
            completion?(true)
            return
        }
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            debugError("renewTokens fail,userInfo == nil")
            completion?(false)
            return
        }
        debugInfo("renewTokens")
        NetworkManager.shared.generateTokens(appId: showTo1v1AppId!,
                                             appCertificate: showTo1v1AppCertificate!,
                                             channelName: "",
                                             uid: userInfo.uid,
                                             tokenGeneratorType: .token007,
                                             tokenTypes: [.rtc, .rtm]) {[weak self] tokens in
            guard let self = self else {return}
            guard let rtcToken = tokens[AgoraTokenType.rtc.rawValue],
                  let rtmToken = tokens[AgoraTokenType.rtm.rawValue] else {
                self.debugInfo("renewTokens fail")
                completion?(false)
                return
            }
            self.rtcToken = rtcToken
            self.rtmToken = rtmToken
            self.debugInfo("renewTokens success")
            completion?(true)
        }
    }
    
    private func _setupRtm(completion: @escaping (NSError?) -> Void) {
        if setupStatus.contains(.rtm) {
            showTo1v1Error("_setupRtm fail! rtm already setup")
            completion(nil)
            return
        }
        rtmClient.logout()
        rtmClient.login(self.rtmToken) { resp, err in
            var error: NSError? = nil
            if let err = err {
                error = NSError(domain: err.reason, code: err.errorCode.rawValue)
                showTo1v1Error("_setupRtm fail! rtm login fail: \(err.localizedDescription)")
                return
            }
            completion(error)
        }
    }
    
    private func _setupService() {
        guard setupStatus.contains(.rtm), let userInfo = userInfo else {
            showTo1v1Error("_setupService fail! rtm not initizlized or userInfo == nil")
            return
        }
        if setupStatus.contains(.syncService) {
            showTo1v1Warn("_setupService fail! service already setup")
            return
        }
        let service = ShowTo1v1ServiceImp(appId: showTo1v1AppId!, user: userInfo, rtmClient: rtmClient)
        self.service = service
    }
    
    private func _setupAPI() {
        guard setupStatus.contains(.rtm), let userInfo = userInfo else {
            showTo1v1Error("_setupAPI fail! rtm not initizlized or userInfo == nil")
            return
        }
        if setupStatus.contains(.api) {
            showTo1v1Warn("_setupAPI fail! service already setup")
            return
        }
        
        let userId = self.userInfo?.getUIntUserId() ?? 0
        let rtmManager = CallRtmManager(appId: showTo1v1AppId!,
                                        userId: "\(userId)",
                                        rtmClient: rtmClient)
        rtmManager.delegate = self
        self.rtmManager = rtmManager
        _initCallAPI(completion: { err in
        })
        
        let config = VideoLoaderConfig()
        config.rtcEngine = rtcEngine
        VideoLoaderApiImpl.shared.setup(config: config)
        VideoLoaderApiImpl.shared.addListener(listener: self)
    }
    
    private func _initCallAPI(completion: @escaping ((Error?)->())) {
        let signalClient = CallRtmSignalClient(rtmClient: self.rtmManager!.getRtmClient())
        
        callApi.deinitialize {
        }
        
        let config = CallConfig()
        config.appId = showTo1v1AppId!
        config.userId = userInfo!.getUIntUserId()
        config.rtcEngine = rtcEngine
        config.signalClient = signalClient
        
        callApi.initialize(config: config)
        callApi.addListener(listener: self)
        
        prepareConfig.rtcToken = rtcToken
//        prepareConfig.rtmToken = rtmToken
        prepareConfig.roomId = NSString.withUUID()
        prepareConfig.localView =  callVC.localCanvasView.canvasView
        prepareConfig.remoteView = callVC.remoteCanvasView.canvasView
        prepareConfig.autoJoinRTC = false  // 如果期望立即加入自己的RTC呼叫频道，则需要设置为true
        prepareConfig.userExtension = userInfo?.yy_modelToJSONObject() as? [String: Any]
        
        callApi.prepareForCall(prepareConfig: prepareConfig) { err in
            // 成功即可以开始进行呼叫
        }
    }
    
    private func _createRtcEngine() ->AgoraRtcEngineKit {
        let config = AgoraRtcEngineConfig()
        config.appId = showTo1v1AppId!
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        let engine = AgoraRtcEngineKit.sharedEngine(with: config,
                                                    delegate: callVC)
        
        engine.setClientRole(.broadcaster)
        return engine
    }
    
    private func _call(room: ShowTo1v1RoomInfo) {
        if room.uid == userInfo?.uid {return}
        if callState == .idle || callState == .failed {
            _setupAPIConfig { _ in
            }
            AUIToast.show(text: "call_not_init".showTo1v1Localization())
            return
        }
        
        AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self, completion: nil)
        AgoraEntAuthorizedManager.checkCameraAuthorized(parent: self)
        
        callApi.call(remoteUserId: room.getUIntUserId()) {[weak self] err in
            guard let err = err else {return}
            self?.callApi.cancelCall(completion: { err in
            })
        }
        
        callVC.roomInfo = room
    }
}

extension RoomListViewController {
    @objc func _backAction() {
        callApi.deinitialize {
        }
        rtcEngine.leaveChannel()
        AgoraRtcEngineKit.destroy()
        rtmClient.logout()
        rtmClient.destroy()
        VideoLoaderApiImpl.shared.cleanCache()
        self.navigationController?.popViewController(animated: true)
    }
    
    func _autoRefreshAction(){
        self.listView.autoRefreshing()
    }
    
    @objc func _refreshAction() {
        let date = Date()
        _setupAPIConfig {[weak self] err in
            guard let self = self else {return}
            if let err = err {
                showTo1v1Print("refresh _setupAPIConfig fail: \(err.localizedDescription)")
                self.listView.endRefreshing()
                return
            }
            showTo1v1Print("refresh setup api cost: \(Int64(-date.timeIntervalSinceNow * 1000))ms")
            self.service?.getRoomList {[weak self] list in
                guard let self = self else {return}
                showTo1v1Print("refresh get room list cost: \(Int64(-date.timeIntervalSinceNow * 1000))ms")
                self.listView.endRefreshing()
                let oldList = self.roomList
                self.roomList = list
                VideoLoaderApiImpl.shared.cleanCache()
                oldList.forEach { info in
                    VideoLoaderApiImpl.shared.removeRTCListener(anchorId: info.roomId, listener: self)
                }
                roomList.forEach { info in
                    VideoLoaderApiImpl.shared.addRTCListener(anchorId: info.roomId, listener: self)
                }
            }
        }
    }
    
    @objc private func _createAction() {
        guard let userInfo = userInfo else {return}
        createRoomDialog =
        CreateRoomDialog.show(user: userInfo, createClosure: {[weak self] roomName in
            guard let self = self else {return}
            if roomName.count == 0 {
                AUIToast.show(text: "create_room_name_empty_tips".showTo1v1Localization())
                return
            }
            self.preJoinRoom = nil
            self.createRoomDialog?.isUserInteractionEnabled = false
            self.createRoomDialog?.isLoading = true
            
            let date = Date()
            self._setupAPIConfig {[weak self] error in
                guard let self = self else { return }
                showTo1v1Print("[setupApi]create broadcaster vc cost: \(Int(-date.timeIntervalSinceNow * 1000))ms")
                if let error = error {
                    showTo1v1Error("createAction fail! setupAPIConfig error: \(error.localizedDescription)")
                    self.createRoomDialog?.isLoading = false
                    self.createRoomDialog?.isUserInteractionEnabled = true
                    CreateRoomDialog.hidden()
                    return
                }
                self.service?.createRoom(roomName: roomName) {[weak self] roomInfo, error in
                    guard let self = self else {return}
                    
                    self.createRoomDialog?.isLoading = false
                    self.createRoomDialog?.isUserInteractionEnabled = true
                    CreateRoomDialog.hidden()
                    
                    if let error = error {
                        AUIToast.show(text: error.localizedDescription)
                        showTo1v1Error("createAction fail! createRoom error: \(error.localizedDescription)")
                        return
                    }
                    showTo1v1Print("[create scene]create broadcaster vc cost: \(Int(-date.timeIntervalSinceNow * 1000))ms")
                    guard let roomInfo = roomInfo else {
                        showTo1v1Error("createAction fail! roomInfo == nil")
                        return
                    }
                    self._showBroadcasterVC(roomInfo: roomInfo)
                }
            }
        }, randomClosure: {
            let roomNameIdx = Int(arc4random()) % randomRoomName.count
            let roomName = randomRoomName[roomNameIdx]
            return "\(roomName)\(Int(arc4random()) % 1000000)"
        })
    }
    
    private func _showBroadcasterVC(roomInfo: ShowTo1v1RoomInfo) {
        audioApi.setAudioScenario(sceneType: .Show, audioScenarioType: .Show_Host)
        let isBroadcaster = roomInfo.uid == userInfo?.uid
        let vc = BroadcasterViewController()
        vc.callApi = self.callApi
        vc.currentUser = self.userInfo
        vc.roomInfo = roomInfo
        vc.rtcEngine = self.rtcEngine
        vc.broadcasterToken = self.rtcToken
        vc.onBackClosure = {[weak self] in
            self?.service?.subscribeListener(listener: nil)
            self?.service?.leaveRoom(roomInfo: roomInfo, completion: { err in
            })
            self?.preJoinRoom = nil
        }
        service?.subscribeListener(listener: vc)
        self.navigationController?.pushViewController(vc, animated: false)
    }
    
    private func _updateCallChannel() {
        prepareConfig.roomId = NSString.withUUID()
        callApi.prepareForCall(prepareConfig: prepareConfig) { _ in
        }
    }
}

extension RoomListViewController: CallApiListenerProtocol {
    func onCallStateChanged(with state: CallStateType, 
                            stateReason: CallStateReason,
                            eventReason: String,
                            eventInfo: [String : Any]) {
        let currentUid = userInfo?.uid ?? ""
        showTo1v1Print("onCallStateChanged state: \(state.rawValue), stateReason: \(stateReason.rawValue), eventReason: \(eventReason), eventInfo: \(eventInfo)")
        
        self.callState = state
        
        switch state {
            case .calling:
            //已经在通话页面，不允许呼叫
//            if navigationController?.visibleViewController is CallViewController {
//                return
//            }
            
            let fromUserId = eventInfo[kFromUserId] as? UInt ?? 0
            let fromRoomId = eventInfo[kFromRoomId] as? String ?? ""
            let toUserId = eventInfo[kRemoteUserId] as? UInt ?? 0
            showTo1v1Print("calling: fromUserId: \(fromUserId) fromRoomId: \(fromRoomId) currentId: \(currentUid) toUserId: \(toUserId)")
            if let connectedUserId = connectedUserId, connectedUserId != fromUserId {
                //如果已经通话页面了，不应该能呼叫
                callApi.reject(remoteUserId: fromUserId, reason: "already calling") { err in
                }
                return
            }
            
            if currentUid == "\(toUserId)" {
                //被叫
                audioApi.setAudioScenario(sceneType: .Chat, audioScenarioType: .Chat_Callee)
                guard navigationController?.visibleViewController is BroadcasterViewController else {
                    //被叫不在直播页面，不能呼叫
                    callApi.reject(remoteUserId: fromUserId, reason: "not in broadcaster view") { _ in
                    }
                    return
                }
                connectedUserId = fromUserId
                
                callApi.accept(remoteUserId: fromUserId, completion: { err in
                })
                
                //被叫不一定在userList能查到，需要从callapi里读取发送用户的user extension
                var user: ShowTo1v1UserInfo? = listView.roomList.first {$0.uid == "\(fromUserId)"}
                if let userDic = (eventInfo[kFromUserExtension] as? [String: Any]) {
                    user = ShowTo1v1UserInfo.yy_model(with: userDic) as! ShowTo1v1UserInfo
                }
                if let user = user {
                    //TODO: search parent
                    AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self, completion: nil)
                    AgoraEntAuthorizedManager.checkCameraAuthorized(parent: self)
                    
                    callVC.targetUser = user
                } else {
                    showTo1v1Print("callee user not found1")
                }
            } else if currentUid == "\(fromUserId)" {
                //主叫
                audioApi.setAudioScenario(sceneType: .Chat, audioScenarioType: .Chat_Caller)
                connectedUserId = toUserId
                //主叫userlist一定会有，因为需要点击
                if let user = listView.roomList.first {$0.uid == "\(toUserId)"} {
                    let dialog = CallerDialog.show(user: user)
                    dialog?.cancelClosure = {[weak self] in
                        self?.callApi.cancelCall(completion: { err in
                        })
                    }
                    callDialog = dialog
                    callVC.targetUser = user
                } else {
                    showTo1v1Print("caller user not found1")
                }
            }
            connectedChannelId = fromRoomId
            break
        case .connected:
            callDialog?.hiddenAnimation()
            if navigationController?.visibleViewController is CallViewController {
                return
            }
            callVC.rtcChannelName = connectedChannelId
            navigationController?.pushViewController(callVC, animated: false)
            break
        case .prepared, .failed:
            callDialog?.hiddenAnimation()
            connectedUserId = nil
            switch stateReason {
            case .localHangup:
                _updateCallChannel()
            case .remoteHangup:
                _updateCallChannel()
                AUIToast.show(text: "call_toast_hangup".showTo1v1Localization())
            case .remoteRejected:
                AUIToast.show(text: "call_user_busy_tips".showTo1v1Localization())
            default:
                break
            }
            break
        default:
            break
        }
    }
    
    func callDebugInfo(message: String, logLevel: CallLogLevel) {
        if logLevel == .normal {
            showTo1v1Print(message, context: "CallApi")
        } else {
            showTo1v1Print(message, context: "CallApi")
        }
    }
}

extension RoomListViewController: IVideoLoaderApiListener {
    func onStateDidChange(newState: AnchorState, oldState: AnchorState, channelName: String) {
    }
    
    func debugInfo(_ message: String) {
        showTo1v1Print(message, context: "VideoLoaderApi")
    }
    
    func debugWarning(_ message: String) {
        showTo1v1Warn(message, context: "VideoLoaderApi")
    }
    
    func debugError(_ message: String) {
        showTo1v1Error(message, context: "VideoLoaderApi")
    }
}

extension RoomListViewController: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, tokenPrivilegeWillExpire token: String) {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            return
        }
        self.setupStatus.remove(.token)
        showTo1v1Print("tokenPrivilegeWillExpire")
        renewTokens {[weak self, weak engine] success in
            guard let self = self, let engine = engine else {return}
            guard success else {
                self.rtcEngine(engine, tokenPrivilegeWillExpire: token)
                return
            }
            
            //renew callapi
            self.callApi.renewToken(with: self.rtcToken)
            self.rtmClient.renewToken(self.rtmToken)
            //renew videoloader
            VideoLoaderApiImpl.shared.getConnectionMap().forEach { (channelId, connection) in
                let mediaOptions = AgoraRtcChannelMediaOptions()
                mediaOptions.token = self.rtcToken
                let ret = self.rtcEngine.updateChannelEx(with: mediaOptions, connection: connection)
                showTo1v1Print("renew token tokenPrivilegeWillExpire: \(channelId) \(ret)")
            }
        }
    }
}

extension RoomListViewController: ICallRtmManagerListener {
    func onConnected() {
        showTo1v1Warn("onConnected")
    }
    
    func onDisconnected() {
        showTo1v1Warn("onDisconnected")
    }
    
    func onConnectionLost() {
        showTo1v1Warn("onConnectionLost")
        AUIToast.show(text: "call_toast_disconnect".showTo1v1Localization())
        self.setupStatus.remove(.rtm)
        _setupRtm { _ in
        }
    }
    
    func onTokenPrivilegeWillExpire(channelName: String) {
        showTo1v1Warn("onTokenPrivilegeWillExpire")
        self.rtcEngine(rtcEngine, tokenPrivilegeWillExpire: "")
    }
}
