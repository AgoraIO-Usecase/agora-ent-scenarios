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

private let kShowGuideAlreadyKey = "already_show_guide_show1v1"
class RoomListViewController: UIViewController {
    var appId: String = ""
    var appCertificate: String = ""
    var userInfo: ShowTo1v1UserInfo? {
        didSet {
            callVC.currentUser = userInfo
        }
    }
    
    private weak var callDialog: ShowTo1v1Dialog?
    private var connectedUserId: UInt?
    private weak var createRoomDialog: CreateRoomDialog?
    private let tokenConfig: CallTokenConfig = CallTokenConfig()
    private var videoLoaderApi: IVideoLoaderApi = VideoLoaderApiImpl()
    private lazy var rtcEngine: AgoraRtcEngineKit = _createRtcEngine()
    private var callState: CallStateType = .idle
    private lazy var callVC: CallViewController = {
        let vc = CallViewController()
        vc.modalPresentationStyle = .fullScreen
        vc.callApi = callApi
        return vc
    }()
    private let callApi = CallApiImpl()
    private lazy var naviBar = NaviBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.aui_width, height: 44))
    private lazy var service: ShowTo1v1ServiceProtocol = ShowTo1v1ServiceImp(appId: appId, user: userInfo)
    private lazy var noDataView: RoomNoDataView = {
        let view = RoomNoDataView(frame: self.view.bounds)
        return view
    }()
    private lazy var listView: RoomPagingListView = {
        let listView = RoomPagingListView(frame: self.view.bounds)
        listView.delegate = self
        listView.callClosure = { [weak self] roomInfo in
            guard let roomInfo = roomInfo else {return}
            self?._call(room: roomInfo)
        }
        listView.tapClosure = { [weak self] roomInfo in
            guard let roomInfo = roomInfo else {return}
            self?._showBroadcasterVC(roomInfo: roomInfo)
        }
        return listView
    }()
    
    private lazy var createButton: UIButton = {
        let button = UIButton(type: .custom)
        button.frame = CGRect(x: (self.view.aui_width - 175) / 2,
                              y: self.view.aui_height - UIDevice.current.aui_SafeDistanceBottom - 42 - 19,
                              width: 175,
                              height: 42)
        button.backgroundColor = UIColor(hexString: "#345dff")
        button.setCornerRadius(21)
        button.setTitle("user_list_create_room".showTo1v1Localization(), for: .normal)
        button.setImage(UIImage.sceneImage(name: "create_room"), for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
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
        
        view.addSubview(noDataView)
        view.addSubview(listView)
        view.addSubview(naviBar)
        view.addSubview(createButton)
        naviBar.backButton.addTarget(self, action: #selector(_backAction), for: .touchUpInside)
        naviBar.refreshButton.addTarget(self, action: #selector(_refreshAction), for: .touchUpInside)
        naviBar.refreshButton.isHidden = true
        naviBar.refreshButton.isHidden = false
        _refreshAction()
        
        _setupAPI()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
//        videoLoaderApi.cleanCache()
        listView.reloadCurrentItem()
    }
    
    private func _showGuideIfNeed() {
        guard listView.roomList.count > 1 else {return}
        if UserDefaults.standard.bool(forKey: kShowGuideAlreadyKey) == true {return}
        let guideView = RoomListGuideView(frame: self.view.bounds)
        self.view.addSubview(guideView)
        UserDefaults.standard.set(true, forKey: kShowGuideAlreadyKey)
    }
    
    private func _setupCallApi() {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            return
        }
        
        if tokenConfig.rtcToken.count > 0, tokenConfig.rtmToken.count > 0 {
            return
        }
        //设置主叫频道
        tokenConfig.roomId = userInfo.get1V1ChannelId()
        NetworkManager.shared.generateTokens(appId: appId,
                                             appCertificate: appCertificate,
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
            
//            self._initCallAPI(tokenConfig: self.tokenConfig)
        }
    }
}

extension RoomListViewController: UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        guard let cell = cell as? RoomListCell, let room = cell.roomInfo else {return}
        let roomInfo = room.createRoomInfo(token: tokenConfig.rtcToken)
        videoLoaderApi.switchRoomState(newState: .joined, roomInfo: roomInfo, tagId: roomInfo.channelName)
        let container = VideoCanvasContainer()
        container.uid = roomInfo.uid
        container.container = cell.canvasView
        videoLoaderApi.renderVideo(roomInfo: roomInfo, container: container)
        guard let connection = videoLoaderApi.getConnectionMap()[room.roomId] else {return}
        let options = AgoraRtcChannelMediaOptions()
        options.autoSubscribeAudio = false
        rtcEngine.updateChannelEx(with: options, connection: connection)
    }
    
    func collectionView(_ collectionView: UICollectionView, didEndDisplaying cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        guard let cell = cell as? RoomListCell, let room = cell.roomInfo else {return}
        let roomInfo = room.createRoomInfo(token: tokenConfig.rtcToken)
        videoLoaderApi.switchRoomState(newState: .prejoined, roomInfo: roomInfo, tagId: roomInfo.channelName)
        let container = VideoCanvasContainer()
        container.uid = roomInfo.uid
        container.container = nil
        videoLoaderApi.renderVideo(roomInfo: roomInfo, container: container)
    }
}

extension RoomListViewController {
    private func _setupAPI() {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            return
        }
        
        tokenConfig.roomId = userInfo.get1V1ChannelId()
        NetworkManager.shared.generateTokens(appId: appId,
                                             appCertificate: appCertificate,
                                             channelName: ""/*tokenConfig.roomId*/,
                                             uid: userInfo.userId,
                                             tokenGeneratorType: .token007,
                                             tokenTypes: [.rtc, .rtm]) {[weak self] tokens in
            guard let self = self else {return}
            self.tokenConfig.rtcToken = tokens[AgoraTokenType.rtc.rawValue]!
            self.tokenConfig.rtmToken = tokens[AgoraTokenType.rtm.rawValue]!
        }
        
        let config = VideoLoaderConfig()
        config.rtcEngine = rtcEngine
        config.userId = UInt(userInfo.userId) ?? 0
        videoLoaderApi.setup(config: config)
        videoLoaderApi.addListener(listener: self)
    }
    
    private func _reinitCallerAPI(tokenConfig: CallTokenConfig, room: ShowTo1v1RoomInfo) {
        callApi.deinitialize {
        }
        
        let config = CallConfig()
        config.role = .caller  // Pure 1v1 can only be set as the caller
        config.mode = .showTo1v1
        config.appId = appId
        config.userId = userInfo!.getUIntUserId()
        config.rtcEngine = _createRtcEngine()
        config.localView = callVC.smallCanvasView
        config.remoteView = callVC.bigCanvasView
        config.ownerRoomId = room.roomId
        if let userExtension = userInfo?.yy_modelToJSONObject() as? [String: Any] {
            config.userExtension = userExtension
        }
        
        callApi.initialize(config: config, token: tokenConfig) {[weak self] error in
        }
        callApi.addListener(listener: self)
        
        //reset callVC
        callVC.callApi = callApi
        callVC.roomInfo = room
    }
    
    private func _reinitCalleeAPI(tokenConfig: CallTokenConfig, room: ShowTo1v1RoomInfo) {
        let config = CallConfig()
        config.role = .callee  // Pure 1v1 can only be set as the caller
        config.mode = .showTo1v1
        config.appId = appId
        config.userId = userInfo!.getUIntUserId()
        config.rtcEngine = _createRtcEngine()
        config.localView = callVC.smallCanvasView
        config.remoteView = callVC.bigCanvasView
        config.ownerRoomId = room.roomId
        
        callApi.initialize(config: config, token: tokenConfig) {[weak self] error in
        }
        callApi.addListener(listener: self)
        
        //reset callVC
        callVC.callApi = callApi
        callVC.roomInfo = room
    }
    
    private func _createRtcEngine() ->AgoraRtcEngineKit {
        let config = AgoraRtcEngineConfig()
        config.appId = appId
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        let engine = AgoraRtcEngineKit.sharedEngine(with: config,
                                                    delegate: callVC)
        
        engine.setClientRole(.broadcaster)
        return engine
    }
    
    private func _call(room: ShowTo1v1RoomInfo) {
        AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self, completion: nil)
        AgoraEntAuthorizedManager.checkCameraAuthorized(parent: self)
        
        self._reinitCallerAPI(tokenConfig: self.tokenConfig, room: room)
        callApi.call(roomId: room.roomId, remoteUserId: room.getUIntUserId()) { err in
        }
    }
}

extension RoomListViewController {
    @objc func _backAction() {
        callApi.deinitialize {
        }
        self.navigationController?.popViewController(animated: true)
    }
    
    @objc func _refreshAction() {
        naviBar.startRefreshAnimation()
        service.getRoomList {[weak self] list in
            guard let self = self else {return}
            self.naviBar.stopRefreshAnimation()
            let roomList = list
            let oldList = self.listView.roomList
            self.listView.roomList = roomList
            self._showGuideIfNeed()
            self.naviBar.style = roomList.count > 0 ? .light : .dark
            self.videoLoaderApi.cleanCache()
            oldList.forEach { info in
                self.videoLoaderApi.removeRTCListener(roomId: info.roomId, listener: self)
            }
            roomList.forEach { info in
                self.videoLoaderApi.addRTCListener(roomId: info.roomId, listener: self)
            }
            
            self.rtcEngine(self.rtcEngine, tokenPrivilegeWillExpire: "")
        }
    }
    
    @objc private func _createAction() {
        guard let userInfo = userInfo else {return}
        createRoomDialog =
        CreateRoomDialog.show(user: userInfo) {[weak self] roomName in
            if roomName.count == 0 {
                AUIToast.show(text: "create_room_name_empty_tips".showTo1v1Localization())
                return 
            }
            self?.createRoomDialog?.isUserInteractionEnabled = false
            self?.createRoomDialog?.isLoading = true
            self?.service.createRoom(roomName: roomName) { roomInfo, error in
                guard let self = self else {return}
                self.createRoomDialog?.isUserInteractionEnabled = true
                self.createRoomDialog?.isLoading = false
                if let error = error {
                    AUIToast.show(text: error.localizedDescription)
                    return
                }
                guard let roomInfo = roomInfo else {return}
                self._showBroadcasterVC(roomInfo: roomInfo)
                CreateRoomDialog.hidden()
            }
        }
    }
    
    private func _showBroadcasterVC(roomInfo: ShowTo1v1RoomInfo) {
        if roomInfo.userId == userInfo?.userId {
            self._reinitCalleeAPI(tokenConfig: self.tokenConfig, room: roomInfo)
        } else {
            self._reinitCallerAPI(tokenConfig: self.tokenConfig, room: roomInfo)
        }
        
        let vc = BroadcasterViewController()
        vc.modalPresentationStyle = .fullScreen
        vc.videoLoader = self.videoLoaderApi
        vc.callApi = self.callApi
        vc.currentUser = self.userInfo
        vc.roomInfo = roomInfo
        vc.rtcEngine = self.rtcEngine
        vc.broadcasterToken = self.tokenConfig.rtcToken
        vc.onBackClosure = {[weak self] in
            self?.service.leaveRoom(roomInfo: roomInfo, completion: { err in
            })
        }
        self.present(vc, animated: false)
    }
}

extension RoomListViewController: CallApiListenerProtocol {
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
        showTo1v1Print("onCallStateChanged state: \(state.rawValue), stateReason: \(stateReason.rawValue), eventReason: \(eventReason), elapsed: \(elapsed) ms, eventInfo: \(eventInfo) publisher: \(publisher) / \(currentUid)")
        
        self.callState = state
        
        switch state {
            case .calling:
            if presentedViewController == callVC {
                return
            }
            
            let fromUserId = eventInfo[kFromUserId] as? UInt ?? 0
            let fromRoomId = eventInfo[kFromRoomId] as? String ?? ""
            let toUserId = eventInfo[kRemoteUserId] as? UInt ?? 0
            showTo1v1Print("calling: fromUserId: \(fromUserId) fromRoomId: \(fromRoomId) currentId: \(currentUid) toUserId: \(toUserId)")
            if let connectedUserId = connectedUserId, connectedUserId != fromUserId {
                callApi.reject(roomId: fromRoomId, remoteUserId: fromUserId, reason: "already calling") { err in
                }
                return
            }
            // 触发状态的用户是自己才处理
            if currentUid == "\(toUserId)" {
                connectedUserId = fromUserId
                
                //被叫不一定在userList能查到，需要从callapi里读取发送用户的user extension
                var user: ShowTo1v1UserInfo? = listView.roomList.first {$0.userId == "\(fromUserId)"}
                if user == nil, let userDic = (eventInfo[kFromUserExtension] as? [String: Any]) {
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
                connectedUserId = toUserId
                //主叫userlist一定会有，因为需要点击
                if let user = listView.roomList.first {$0.userId == "\(toUserId)"} {
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
            break
        case .connected:
            callDialog?.hiddenAnimation()
            callVC.dismiss(animated: false)
            _topViewController().present(callVC, animated: false)
            break
        case .prepared, .failed:
            callDialog?.hiddenAnimation()
            connectedUserId = nil
            switch stateReason {
            case .localHangup, .remoteHangup:
                callVC.dismiss(animated: false)
                AUIToast.show(text: "call_toast_hangup".showTo1v1Localization())
            default:
                break
            }
            break
        default:
            break
        }
    }
    
    func debugInfo(message: String) {
        showTo1v1Print(message, context: "CallApi")
    }
    func debugWarning(message: String) {
        showTo1v1Print(message, context: "CallApi")
    }
}

extension RoomListViewController: IVideoLoaderApiListener {
    func onStateDidChange(newState: RoomStatus, oldState: RoomStatus, channelName: String) {
    }
    
    func debugInfo(_ message: String) {
        showTo1v1Print(message)
    }
    
    func debugWarning(_ message: String) {
        showTo1v1Warn(message)
    }
    
    func debugError(_ message: String) {
        showTo1v1Error(message)
    }
}

extension RoomListViewController: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, tokenPrivilegeWillExpire token: String) {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            return
        }
        showTo1v1Print("tokenPrivilegeWillExpire")
        NetworkManager.shared.generateTokens(appId: appId,
                                             appCertificate: appCertificate,
                                             channelName: ""/*tokenConfig.roomId*/,
                                             uid: userInfo.userId,
                                             tokenGeneratorType: .token007,
                                             tokenTypes: [.rtc, .rtm]) {[weak self, weak engine] tokens in
            guard let self = self, let engine = engine else {return}
            guard tokens.count == 2 else {
                self.rtcEngine(engine, tokenPrivilegeWillExpire: token)
                return
            }
            self.tokenConfig.rtcToken = tokens[AgoraTokenType.rtc.rawValue]!
            self.tokenConfig.rtmToken = tokens[AgoraTokenType.rtm.rawValue]!
            //renew callapi
            self.callApi.renewToken(with: self.tokenConfig)
            
            //renew videoloader
            self.videoLoaderApi.getConnectionMap().forEach { (channelId, connection) in
                let mediaOptions = AgoraRtcChannelMediaOptions()
                mediaOptions.token = self.tokenConfig.rtcToken
                let ret = self.rtcEngine.updateChannelEx(with: mediaOptions, connection: connection)
                showTo1v1Print("renew token tokenPrivilegeWillExpire: \(channelId) \(ret)")
            }
        }
    }
}


extension UIViewController {
    func _topViewController() -> UIViewController {
        let viewController = self

        if let presentedController = viewController.presentedViewController {
            return presentedController._topViewController()
        }
        return viewController
    }
}
