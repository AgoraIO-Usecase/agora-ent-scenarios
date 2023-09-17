//
//  BroadcasterViewController.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/7/28.
//

import UIKit
import AgoraRtcKit
import VideoLoaderAPI
import CallAPI


class CallAgoraExProxy: CallApiProxy, AgoraRtcEngineDelegate {
}

private let kNormalIconSize = CGSize(width: 32, height: 32)
class BroadcasterViewController: BaseRoomViewController {
    var videoLoader: IVideoLoaderApi?
    var currentUser: ShowTo1v1UserInfo?
    override var roomInfo: ShowTo1v1RoomInfo? {
        didSet {
            let createdAt = roomInfo?.createdAt ?? 0
            roomInfoView.setRoomInfo(avatar: roomInfo?.avatar ?? "",
                                     name: roomInfo?.roomName ?? "",
                                     id: roomInfo?.userName ?? "")
            roomInfoView.startTime(Int64(createdAt > 0 ? createdAt : Int64(Date().timeIntervalSince1970) * 1000))
            roomInfoView.timerCallBack = {[weak self] duration in
                if duration < 60 * 20 {
                    return
                }
                self?.roomInfoView.stopTime()
                self?.onBackAction()
            }
            bgImageView.image = roomInfo?.bgImage()
        }
    }
    var broadcasterToken: String? {
        didSet {
            guard let broadcasterToken = broadcasterToken else {return}
            rtcEngine?.renewToken(broadcasterToken)
        }
    }
    private lazy var rtcProxy: CallAgoraExProxy = CallAgoraExProxy()
    private lazy var closeButton: UIButton = {
        let button = UIButton(type: .custom)
        button.aui_size = kNormalIconSize
        button.setImage(UIImage.sceneImage(name: "live_close"), for: .normal)
        button.addTarget(self, action: #selector(onBackAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var userCountView: RoomMembersCountView = RoomMembersCountView(frame: CGRect(origin: .zero, size: kNormalIconSize))
    
    // 背景图
    private lazy var bgImageView: UIImageView = {
        let view = UIImageView()
        view.contentMode = .scaleAspectFill
        return view
    }()
    
    private lazy var emptyTipsLabel: UILabel = {
        let info1 = AUILabelAttrInfo(size: CGSize(width: 7, height: 38),
                                     content: UIImage(color: UIColor.clear))
        let info2 = AUILabelAttrInfo(size: CGSize(width: 28, height: 28),
                                     content: UIImage.sceneImage(name: "icon_user_leave")!)
        let info3 = AUILabelAttrInfo(size: CGSize(width: 16, height: 38),
                                     content: UIImage(color: UIColor.clear))
        let text = AUILabelAttrInfo(size: .zero, content: "call_user_empty_tips".showTo1v1Localization())
        
        let label = UILabel.createAttrLabel(font: UIFont.systemFont(ofSize: 13), attrInfos: [info1, info2, info3, text])
        label.textAlignment = .left
        label.textColor = .white
        label.backgroundColor = UIColor(red: 0, green: 0.22, blue: 1, alpha: 0.5)
        label.clipsToBounds = true
        label.layer.cornerRadius = 13.5
        label.layer.borderColor = UIColor(red: 0.419, green: 0.513, blue: 0.846, alpha: 1).cgColor
        label.layer.borderWidth = 1.5
        return label
    }()
    
    deinit {
        showTo1v1Print("deinit-- BroadcasterViewController")
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        showTo1v1Print("init-- BroadcasterViewController")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        emptyTipsLabel.frame = CGRect(x: 15, y: roomInfoView.aui_bottom + 8, width: view.aui_width - 30, height: 38)
        
        bgImageView.frame = view.bounds
        view.insertSubview(bgImageView, at: 0)
        bgImageView.addSubview(emptyTipsLabel)
        
        closeButton.aui_right = view.aui_width - 15
        closeButton.aui_centerY = roomInfoView.aui_centerY
        view.addSubview(closeButton)
        
        userCountView.aui_right = closeButton.aui_left - 15
        userCountView.aui_centerY = closeButton.aui_centerY
        view.addSubview(userCountView)
        
        joinRTCChannel()
    }
    
    private func joinRTCChannel() {
        guard let currentUser = currentUser, let roomInfo = roomInfo, let uid = UInt(currentUser.userId) else {return}
        if currentUser.userId == roomInfo.userId {
            videoLoader?.cleanCache()
            let channelId = roomInfo.roomId
            rtcEngine?.setClientRole(.broadcaster)
            rtcEngine?.enableLocalVideo(true)
            rtcEngine?.enableLocalAudio(true)
            let mediaOptions = AgoraRtcChannelMediaOptions()
            mediaOptions.publishCameraTrack = true
            mediaOptions.publishMicrophoneTrack = true
            showTo1v1Print("broadcaster joinChannel[\(channelId)] \(uid)")
            rtcEngine?.joinChannel(byToken: broadcasterToken,
                                   channelId: channelId,
                                   uid: uid,
                                   mediaOptions: mediaOptions,
                                   joinSuccess: {[weak self] channelId, uid, elapsed in
                showTo1v1Print("broadcaster joinChannel[\(channelId)] success:  \(uid)")
                guard let self = self, let rtcEngine = self.rtcEngine else {return}
//                self.callApi?.setupContentInspectConfig(rtcEngine: rtcEngine, enable: true, uid: "\(uid)", channelId: channelId)
//                self.callApi?.moderationAudio(appId: showTo1v1AppId!, channelName: channelId, user: self.currentUser!)
                self.callApi?.setupContentInspectConfig(rtcEngine: rtcEngine, enable: true, uid: "\(uid)", channelId: channelId)
                self.callApi?.moderationAudio(appId: showTo1v1AppId!, channelName: channelId, user: self.currentUser!)
            })
            
            _setupCanvas(view: remoteCanvasView)
            
            rtcEngine?.delegate = rtcProxy
            rtcProxy.addListener(self.realTimeView)
            rtcProxy.addListener(callApi!)
            
            bottomBar.buttonTypes = [.more]
        } else {
            guard let token = broadcasterToken else {
                assert(false, "render fail")
                return
            }
            let room = roomInfo.createRoomInfo(token: token)
            let container = VideoCanvasContainer()
            container.container = remoteCanvasView
            container.uid = roomInfo.getUIntUserId()
            videoLoader?.renderVideo(roomInfo: room, container: container)
            
            videoLoader?.addRTCListener(roomId: room.channelName, listener: self.realTimeView)
            
            
            bottomBar.buttonTypes = [.call, .more]
        }
    }
    
    private func _leaveRTCChannel() {
        guard let currentUser = currentUser, let roomInfo = roomInfo, let uid = UInt(currentUser.userId) else {return}
        if currentUser.userId == roomInfo.userId {
            rtcEngine?.leaveChannel()
            
            rtcEngine?.delegate = nil
            rtcProxy.removeAllListener()
        } else {
            let room = roomInfo.createRoomInfo(token: "")
            let container = VideoCanvasContainer()
            container.container = nil
            container.uid = roomInfo.getUIntUserId()
            videoLoader?.renderVideo(roomInfo: room, container: container)
            
            videoLoader?.removeRTCListener(roomId: room.channelName, listener: self.realTimeView)
        }
    }
    
    private func _publishMedia(_ publish: Bool) {
        guard let currentUser = currentUser, let roomInfo = roomInfo, let uid = UInt(currentUser.userId) else {return}
        if currentUser.userId == roomInfo.userId {
            let mediaOptions = AgoraRtcChannelMediaOptions()
            mediaOptions.publishCameraTrack = publish
            mediaOptions.publishMicrophoneTrack = publish
            rtcEngine?.updateChannel(with: mediaOptions)
        }
    }
    
    private func _setupCanvas(view: UIView?) {
        guard let currentUser = currentUser, let roomInfo = roomInfo, let uid = UInt(currentUser.userId) else {return}
        if currentUser.userId == roomInfo.userId {
            let canvas = AgoraRtcVideoCanvas()
            canvas.view = view
            canvas.uid = uid
            rtcEngine?.setupLocalVideo(canvas)
        }
    }
    
    override func onBackAction() {
        guard let navigationController = navigationController else {return}
        var parentVC: UIViewController? = nil
        for vc in navigationController.viewControllers {
            if vc == self {
                guard let parentVC = parentVC else {return}
                navigationController.popToViewController(parentVC, animated: false)
            }
            parentVC = vc
        }
        
        super.onBackAction()
        _leaveRTCChannel()
    }
    
    override func onClick(actionType: RoomBottomBarType) {
        if actionType == .call {
            AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self, completion: nil)
            AgoraEntAuthorizedManager.checkCameraAuthorized(parent: self)
            
            callApi?.call(roomId: roomInfo!.roomId, remoteUserId: roomInfo!.getUIntUserId()) { err in
            }
            return
        }
        super.onClick(actionType: actionType)
    }
}

extension BroadcasterViewController {
    override func onCallStateChanged(with state: CallStateType,
                            stateReason: CallReason,
                            eventReason: String,
                            elapsed: Int,
                            eventInfo: [String : Any]) {
        let publisher = eventInfo[kPublisher] as? String ?? currentUser?.userId
        guard publisher == currentUser?.userId else {return}
        
        switch state {
        case .calling:
            _publishMedia(false)
            _setupCanvas(view: nil)
            break
        case .prepared, .idle, .failed:
            _publishMedia(true)
            _setupCanvas(view: remoteCanvasView)
            break
        default:
            break
        }
    }
}

extension BroadcasterViewController: ShowTo1v1ServiceListenerProtocol {
    func onRoomDidDestroy(roomInfo: ShowTo1v1RoomInfo) {
        onBackAction()
    }
    
    func onNetworkStatusChanged(status: ShowTo1v1ServiceNetworkStatus) {
    }
    
    func onUserListDidChanged(userList: [ShowTo1v1UserInfo]) {
        userCountView.count = userList.count
    }
}
