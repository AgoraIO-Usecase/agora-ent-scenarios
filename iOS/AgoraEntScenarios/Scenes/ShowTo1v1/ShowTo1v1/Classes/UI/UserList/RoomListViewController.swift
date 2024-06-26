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

extension CallTokenConfig {
    func tokenIsEmpty() -> Bool {
        if rtcToken.count > 0, rtmToken.count > 0 {
            return false
        }
        
        return true
    }
}

private let kShowGuideAlreadyKey = "already_show_guide_show1v1"
class RoomListViewController: UIViewController {
    var userInfo: ShowTo1v1UserInfo?
    
    private weak var preJoinRoom: ShowTo1v1RoomInfo?
    private weak var callDialog: ShowTo1v1Dialog?
    private var connectedUserId: UInt?
    private weak var createRoomDialog: CreateRoomDialog?
    private let tokenConfig: CallTokenConfig = CallTokenConfig()
    private lazy var rtcEngine: AgoraRtcEngineKit = _createRtcEngine()
    private var callState: CallStateType = .idle
    private lazy var callVC: CallViewController = CallViewController()
    private let callApi = CallApiImpl()
    private lazy var naviBar = NaviBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.aui_width, height: 44))
    private lazy var service: ShowTo1v1ServiceProtocol = ShowTo1v1ServiceImp(appId: showTo1v1AppId!, user: userInfo)
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
            var success1: Bool = false
            var success2: Bool = false
            
            self.preJoinRoom = roomInfo
            
            self.renewTokens { success in
                success1 = success
                if success1, success2 {
                    self._showBroadcasterVC(roomInfo: roomInfo)
                }
            }
            
            self.service.joinRoom(roomInfo: roomInfo, completion: {[weak self] err in
                guard let self = self else {return}
                success2 = err == nil ? true : false
                if let error = err {
                    if self.preJoinRoom?.roomId == roomInfo.roomId {
                        self.navigationController?.popToViewController(self, animated: false)
                        AUIToast.show(text: error.localizedDescription)
                    }
                    return
                }
                if success1, success2 {
                    self._showBroadcasterVC(roomInfo: roomInfo)
                }
            })
        }
        return listView
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
        view.addSubview(noDataView)
        view.addSubview(listView)
        view.addSubview(naviBar)
        view.addSubview(createButton)
        naviBar.backButton.addTarget(self, action: #selector(_backAction), for: .touchUpInside)
        naviBar.refreshButton.addTarget(self, action: #selector(_refreshAction), for: .touchUpInside)
        naviBar.refreshButton.isHidden = true
        naviBar.refreshButton.isHidden = false
        _refreshAction()
        
        callVC.currentUser = userInfo
        
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
}

extension RoomListViewController {
    private func renewTokens(completion: ((Bool)->Void)?) {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            debugError("renewTokens fail,userInfo == nil")
            completion?(false)
            return
        }
        debugInfo("renewTokens")
        NetworkManager.shared.generateTokens(appId: showTo1v1AppId!,
                                             appCertificate: showTo1v1AppCertificate!,
                                             channelName: ""/*tokenConfig.roomId*/,
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
            self.tokenConfig.rtcToken = rtcToken
            self.tokenConfig.rtmToken = rtmToken
            self.listView.roomList.forEach { info in
                info.token = rtcToken
            }
            self.listView.roomList = self.listView.roomList
            self.debugInfo("renewTokens success")
            completion?(true)
        }
    }
    private func _setupAPI() {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            return
        }
        
        renewTokens {[weak self] flag in
            self?._reinitCallerAPI(completion: { err in
            })
        }
        
        let config = VideoLoaderConfig()
        config.rtcEngine = rtcEngine
        VideoLoaderApiImpl.shared.setup(config: config)
        VideoLoaderApiImpl.shared.addListener(listener: self)
        
        callVC.rtcEngine = rtcEngine
    }
    
    private func _reinitCallerAPI(completion: @escaping ((Error?)->())) {
        tokenConfig.roomId = userInfo!.get1V1ChannelId()
        callApi.deinitialize {
        }
        callVC.callApi = callApi
        
        let config = CallConfig()
        config.role = .caller  // Pure 1v1 can only be set as the caller
        config.mode = .showTo1v1
        config.appId = showTo1v1AppId!
        config.userId = userInfo!.getUIntUserId()
        config.rtcEngine = rtcEngine
        config.localView = callVC.localCanvasView.canvasView
        config.remoteView = callVC.remoteCanvasView.canvasView
        if let userExtension = userInfo?.yy_modelToJSONObject() as? [String: Any] {
            config.userExtension = userExtension
        }
        
        callApi.addListener(listener: self)
        callApi.initialize(config: config, token: tokenConfig) {[weak self] error in
            if let err = error {
                showTo1v1Error("_reinitCallerAPI initialize api fail: \(err.localizedDescription)")
            }
            completion(error)
        }
    }
    
    private func _reinitCalleeAPI(room: ShowTo1v1RoomInfo, completion: @escaping ((Error?)->())) {
        tokenConfig.roomId = room.roomId
        callApi.deinitialize {
        }
        callVC.callApi = callApi
        
        let config = CallConfig()
        config.role = .callee  // Pure 1v1 can only be set as the caller
        config.mode = .showTo1v1
        config.appId = showTo1v1AppId!
        config.userId = userInfo!.getUIntUserId()
        config.rtcEngine = rtcEngine
        config.localView = callVC.localCanvasView.canvasView
        config.remoteView = callVC.remoteCanvasView.canvasView
        
        callApi.initialize(config: config, token: tokenConfig) {[weak self] error in
            if let err = error {
                showTo1v1Error("_reinitCalleeAPI initialize api fail: \(err.localizedDescription)")
            }
            completion(error)
        }
        callApi.addListener(listener: self)
        
        //reset callVC
        callVC.callApi = callApi
        callVC.roomInfo = room
        callVC.rtcEngine = rtcEngine
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
        if self.tokenConfig.tokenIsEmpty() {
            renewTokens { success in
                self._call(room: room)
            }
            return
        }
        
        AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self, completion: nil)
        AgoraEntAuthorizedManager.checkCameraAuthorized(parent: self)
        
        callApi.call(roomId: room.roomId, remoteUserId: room.getUIntUserId()) { err in
        }
        
        //reset callVC
        callVC.callApi = callApi
        callVC.roomInfo = room
        callVC.rtcEngine = rtcEngine
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
            roomList.forEach { info in
                info.token = self.tokenConfig.rtcToken
            }
            self.listView.roomList = roomList
            self._showGuideIfNeed()
            self.naviBar.style = roomList.count > 0 ? .light : .dark
            VideoLoaderApiImpl.shared.cleanCache()
            oldList.forEach { info in
                VideoLoaderApiImpl.shared.removeRTCListener(anchorId: info.roomId, listener: self)
            }
            roomList.forEach { info in
                VideoLoaderApiImpl.shared.addRTCListener(anchorId: info.roomId, listener: self)
            }
            
            self.rtcEngine(self.rtcEngine, tokenPrivilegeWillExpire: "")
            
            AUIToast.show(text: "room_list_refresh_tips".showTo1v1Localization())
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
            var success1: Bool = false
            var createRoomInfo: ShowTo1v1RoomInfo? = nil
            let group = DispatchGroup()
            group.enter()
            self.renewTokens { success in
                success1 = success
                group.leave()
            }
            group.enter()
            self.service.createRoom(roomName: roomName) {[weak self] roomInfo, error in
                defer {
                    group.leave()
                }
                guard let self = self else {return}
                if let error = error {
                    AUIToast.show(text: error.localizedDescription)
                    return
                }
                createRoomInfo = roomInfo
            }
            group.notify(queue: DispatchQueue.main) {[weak self] in
                self?.createRoomDialog?.isUserInteractionEnabled = true
                self?.createRoomDialog?.isLoading = false
                CreateRoomDialog.hidden()
                guard let roomInfo = createRoomInfo else {return}
                guard success1 else {
                    self?.service.leaveRoom(roomInfo: roomInfo, completion: { err in
                    })
                    return
                }
                self?._showBroadcasterVC(roomInfo: roomInfo)
            }
            
            
        }, randomClosure: {
            let roomNameIdx = Int(arc4random()) % randomRoomName.count
            let roomName = randomRoomName[roomNameIdx]
            return "\(roomName)\(Int(arc4random()) % 1000000)"
        })
    }
    
    private func _showBroadcasterVC(roomInfo: ShowTo1v1RoomInfo) {
        let isBroadcaster = roomInfo.uid == userInfo?.uid
        if isBroadcaster {
            self._reinitCalleeAPI(room: roomInfo) {[weak self] err in
                if let _ = err {
                    //失败默认重试一次
                    self?.renewTokens(completion: { success in
                        guard success else {return}
                        self?._reinitCalleeAPI(room: roomInfo, completion: { err in
                        })
                    })
                }
            }
        }
        
        let vc = BroadcasterViewController()
        vc.callApi = self.callApi
        vc.currentUser = self.userInfo
        vc.roomInfo = roomInfo
        vc.rtcEngine = self.rtcEngine
        vc.broadcasterToken = self.tokenConfig.rtcToken
        vc.onBackClosure = {[weak self] in
            self?.service.subscribeListener(listener: nil)
            self?.service.leaveRoom(roomInfo: roomInfo, completion: { err in
            })
            self?.preJoinRoom = nil
            //主播回到列表页面要从callee变成caller
            if isBroadcaster {
                self?._reinitCallerAPI { err in
                    if let _ = err {
                        //失败默认重试一次
                        self?.renewTokens(completion: { success in
                            guard success else {return}
                            self?._reinitCalleeAPI(room: roomInfo, completion: { err in
                            })
                        })
                    }
                }
            }
        }
        service.subscribeListener(listener: vc)
        self.navigationController?.pushViewController(vc, animated: false)
    }
}

extension RoomListViewController: CallApiListenerProtocol {
    func onCallStateChanged(with state: CallStateType,
                            stateReason: CallReason,
                            eventReason: String,
                            elapsed: Int,
                            eventInfo: [String : Any]) {
        let currentUid = userInfo?.uid ?? ""
        let publisher = eventInfo[kPublisher] as? String ?? currentUid
        guard publisher == currentUid else {
            return
        }
        showTo1v1Print("onCallStateChanged state: \(state.rawValue), stateReason: \(stateReason.rawValue), eventReason: \(eventReason), elapsed: \(elapsed) ms, eventInfo: \(eventInfo) publisher: \(publisher) / \(currentUid)")
        
        self.callState = state
        
        switch state {
            case .calling:
            if navigationController?.visibleViewController == callVC {
                return
            }
            
            let fromUserId = eventInfo[kFromUserId] as? UInt ?? 0
            let fromRoomId = eventInfo[kFromRoomId] as? String ?? ""
            let toUserId = eventInfo[kRemoteUserId] as? UInt ?? 0
            showTo1v1Print("calling: fromUserId: \(fromUserId) fromRoomId: \(fromRoomId) currentId: \(currentUid) toUserId: \(toUserId)")
            if let connectedUserId = connectedUserId, connectedUserId != fromUserId {
                callApi.reject(remoteUserId: fromUserId, reason: "already calling") { err in
                }
                return
            }
            // 触发状态的用户是自己才处理
            if currentUid == "\(toUserId)" {
                connectedUserId = fromUserId
                
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
            break
        case .connected:
            callDialog?.hiddenAnimation()
            if navigationController?.visibleViewController == callVC {
                return
            }
            navigationController?.pushViewController(callVC, animated: false)
            break
        case .prepared, .failed:
            callDialog?.hiddenAnimation()
            connectedUserId = nil
            switch stateReason {
            case .remoteHangup:
                callVC.dismiss(animated: false)
                AUIToast.show(text: "call_toast_hangup".showTo1v1Localization())
            case .remoteRejected:
                AUIToast.show(text: "call_user_busy_tips".showTo1v1Localization())
            case .rtmLost:
                callVC.dismiss(animated: false)
                AUIToast.show(text: "call_toast_disconnect".showTo1v1Localization())
                if let vc = navigationController?.visibleViewController as? BroadcasterViewController,
                   let roomInfo = vc.roomInfo,
                   roomInfo.uid == userInfo?.uid {
                    _reinitCalleeAPI(room: roomInfo) { err in
                    }
                    return
                }
                _reinitCallerAPI { err in
                }
            default:
                break
            }
            break
        default:
            break
        }
    }
    
    func callDebugInfo(message: String) {
        showTo1v1Print(message, context: "CallApi")
    }
    func callDebugWarning(message: String) {
        showTo1v1Print(message, context: "CallApi")
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
        showTo1v1Print("tokenPrivilegeWillExpire")
        renewTokens {[weak self, weak engine] success in
            guard let self = self, let engine = engine else {return}
            guard success else {
                self.rtcEngine(engine, tokenPrivilegeWillExpire: token)
                return
            }
            
            //renew callapi
            self.callApi.renewToken(with: self.tokenConfig)
            
            //renew videoloader
            VideoLoaderApiImpl.shared.getConnectionMap().forEach { (channelId, connection) in
                let mediaOptions = AgoraRtcChannelMediaOptions()
                mediaOptions.token = self.tokenConfig.rtcToken
                let ret = self.rtcEngine.updateChannelEx(with: mediaOptions, connection: connection)
                showTo1v1Print("renew token tokenPrivilegeWillExpire: \(channelId) \(ret)")
            }
        }
    }
}
