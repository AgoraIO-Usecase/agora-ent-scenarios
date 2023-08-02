//
//  ShowTo1v1RoomListViewController.swift
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
class ShowTo1v1RoomListViewController: UIViewController {
    var appId: String = ""
    var appCertificate: String = ""
    var userInfo: ShowTo1v1UserInfo?
    
    private var videoLoaderApi: IVideoLoaderApi = VideoLoaderApiImpl()
    private var rtcToken: String = ""
    private lazy var rtcEngine: AgoraRtcEngineKit = _createRtcEngine()
    private var callState: CallStateType = .idle
    private lazy var callVC: ShowTo1v1CallViewController = {
        let vc = ShowTo1v1CallViewController()
        vc.callApi = callApi
        return vc
    }()
    private let callApi = CallApiImpl()
    private lazy var naviBar: ShowTo1v1NaviBar = ShowTo1v1NaviBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.aui_width, height: 44))
    private lazy var service: ShowTo1v1ServiceProtocol = ShowTo1v1ServiceImp(appId: appId, user: userInfo)
    private lazy var noDataView: Pure1v1UserNoDataView = {
        let view = Pure1v1UserNoDataView(frame: self.view.bounds)
        view.noDataDialogView.createClosure = {[weak self] in
            
        }
        return view
    }()
    private lazy var listView: ShowTo1v1UserPagingListView = {
        let listView = ShowTo1v1UserPagingListView(frame: self.view.bounds)
        listView.delegate = self
        listView.callClosure = { [weak self] user in
            guard let user = user else {return}
            self?._call(user: user)
        }
        return listView
    }()
    
    deinit {
        showTo1v1Print("deinit-- Pure1v1UserListViewController")
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
        
        _setupAPI()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        listView.reloadData()
    }
    
    private func _showGuideIfNeed() {
        guard listView.roomList.count > 1 else {return}
        if UserDefaults.standard.bool(forKey: kShowGuideAlreadyKey) == true {return}
        let guideView = ShowTo1v1GuideView(frame: self.view.bounds)
        self.view.addSubview(guideView)
        UserDefaults.standard.set(true, forKey: kShowGuideAlreadyKey)
    }
}

extension ShowTo1v1RoomListViewController: UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        guard let cell = cell as? ShowTo1v1RoomCell, let room = cell.roomInfo else {return}
        let roomInfo = room.createRoomInfo(token: rtcToken)
        videoLoaderApi.switchRoomState(newState: .joined, roomInfo: roomInfo, tagId: roomInfo.channelName)
        let container = VideoCanvasContainer()
        container.uid = roomInfo.uid
        container.container = cell.canvasView
        videoLoaderApi.renderVideo(roomInfo: roomInfo, container: container)
    }
    
    func collectionView(_ collectionView: UICollectionView, didEndDisplaying cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        guard let cell = cell as? ShowTo1v1RoomCell, let room = cell.roomInfo else {return}
        let roomInfo = room.createRoomInfo(token: rtcToken)
        videoLoaderApi.switchRoomState(newState: .prejoined, roomInfo: roomInfo, tagId: roomInfo.channelName)
        let container = VideoCanvasContainer()
        container.uid = roomInfo.uid
        container.container = cell.canvasView
        videoLoaderApi.renderVideo(roomInfo: roomInfo, container: container)
    }
}

extension ShowTo1v1RoomListViewController {
    private func _setupAPI() {
        guard let userInfo = userInfo else {
            assert(false, "userInfo == nil")
            return
        }
        
        let tokenConfig: CallTokenConfig = CallTokenConfig()
        tokenConfig.roomId = userInfo.userId
        NetworkManager.shared.generateTokens(appId: appId,
                                             appCertificate: appCertificate,
                                             channelName: ""/*tokenConfig.roomId*/,
                                             uid: userInfo.userId,
                                             tokenGeneratorType: .token007,
                                             tokenTypes: [.rtc, .rtm]) {[weak self] tokens in
            tokenConfig.rtcToken = tokens[AgoraTokenType.rtc.rawValue]!
            tokenConfig.rtmToken = tokens[AgoraTokenType.rtm.rawValue]!
            
            self?._initCallerAPI(tokenConfig: tokenConfig)
            self?.rtcToken = tokenConfig.rtcToken
        }
        
        let config = VideoLoaderConfig()
        config.rtcEngine = rtcEngine
        config.userId = UInt(userInfo.userId) ?? 0
        videoLoaderApi.setup(config: config)
        videoLoaderApi.addListener(listener: self)
    }
    
    private func _initCallerAPI(tokenConfig: CallTokenConfig) {
        let config = CallConfig()
        config.role = .caller  // Pure 1v1 can only be set as the caller
        config.mode = .showTo1v1
        config.appId = appId
        config.userId = UInt(userInfo?.userId ?? "")!
        config.rtcEngine = _createRtcEngine()
        config.localView = callVC.smallCanvasView
        config.remoteView = callVC.bigCanvasView
        
        callApi.initialize(config: config, token: tokenConfig) {[weak self] error in
        }
        callApi.addListener(listener: self)
    }
    
    private func _initCalleeAPI(tokenConfig: CallTokenConfig) {
        let config = CallConfig()
        config.role = .callee  // Pure 1v1 can only be set as the caller
        config.mode = .showTo1v1
        config.appId = appId
        config.userId = UInt(userInfo?.userId ?? "")!
        config.rtcEngine = _createRtcEngine()
        config.localView = callVC.smallCanvasView
        config.remoteView = callVC.bigCanvasView
        
        callApi.initialize(config: config, token: tokenConfig) {[weak self] error in
        }
        callApi.addListener(listener: self)
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
    
    private func _call(user: ShowTo1v1UserInfo) {
        callApi.call(roomId: user.userId, remoteUserId: UInt(user.userId)!) { err in
            
        }
    }
}

extension ShowTo1v1RoomListViewController {
    @objc func _backAction() {
        callApi.deinitialize {
        }
        service.leaveRoom { err in
        }
        self.navigationController?.popViewController(animated: true)
    }
    
    @objc func _refreshAction() {
        naviBar.startRefreshAnimation()
        service.getRoomList {[weak self] list in
            guard let self = self else {return}
            self.naviBar.stopRefreshAnimation()
            let roomList = list.filter({$0.userId != self.userInfo?.userId})
            self.listView.roomList = roomList
            self._showGuideIfNeed()
            self.naviBar.style = roomList.count > 0 ? .light : .dark
        }
    }
}

extension ShowTo1v1RoomListViewController: CallApiListenerProtocol {
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
        case .connected:
            var connectedUserId:String? = ""
            guard let uid = connectedUserId, let user = listView.roomList.first(where: {$0.userId == "\(uid)"}) else {
                assert(false, "user not fount")
                return
            }
            callVC.targetUser = user
            navigationController?.pushViewController(callVC, animated: false)
            break
        case .prepared:
            switch stateReason {
            case .localHangup, .remoteHangup:
                if navigationController?.viewControllers.last == callVC {
                    navigationController?.popViewController(animated: false)
                }
                AUIToast.show(text: "call_toast_hangup".showTo1v1Localization())
//            case .localRejected, .remoteRejected:
//                AUIToast.show(text: "通话被拒绝")
//            case .callingTimeout:
//                AUIToast.show(text: "无应答")
//            case .localCancel, .remoteCancel:
//                AUIToast.show(text: "通话被取消")
            default:
                break
            }
//            AUIAlertManager.hiddenView()
            break
        case .failed:
//            AUIToast.show(text: eventReason, postion: .bottom)
//            AUIAlertManager.hiddenView()
            break
        default:
            break
        }
    }
}

extension ShowTo1v1RoomListViewController: IVideoLoaderApiListener {
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
