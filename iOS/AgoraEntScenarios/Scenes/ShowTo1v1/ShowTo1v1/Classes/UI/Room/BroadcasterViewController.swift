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
import AgoraCommon

class BroadcasterViewController: BaseRoomViewController {
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
            bgImageView.sd_setImage(with: URL(string: roomInfo?.bgImage() ?? ""), placeholderImage: nil)
        }
    }
    var broadcasterToken: String? {
        didSet {
            guard let broadcasterToken = broadcasterToken else {return}
            rtcEngine?.renewToken(broadcasterToken)
        }
    }
    
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
        ShowTo1v1Logger.info("deinit-- BroadcasterViewController")
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        ShowTo1v1Logger.info("init-- BroadcasterViewController")
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
        
        moreBtn.aui_size = kNormalIconSize
        moreBtn.aui_right = closeButton.aui_left - 8
        moreBtn.aui_centerY = closeButton.aui_centerY
        view.addSubview(moreBtn)
        
        userCountView.aui_right = moreBtn.aui_left - 8
        userCountView.aui_centerY = moreBtn.aui_centerY
        view.addSubview(userCountView)
        
        joinRTCChannel()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        realTimeView.cleanLocalDescription()
        realTimeView.cleanRemoteDescription()
    }
    
    private func joinRTCChannel() {
        guard let currentUser = currentUser,
              let roomInfo = roomInfo,
              let uid = UInt(currentUser.uid) else {return}
        if currentUser.uid == roomInfo.uid {
            VideoLoaderApiImpl.shared.cleanCache()
            let channelId = roomInfo.roomId
            rtcEngine?.setClientRole(.broadcaster)
            rtcEngine?.enableLocalVideo(true)
            rtcEngine?.enableLocalAudio(true)
            let mediaOptions = AgoraRtcChannelMediaOptions()
            mediaOptions.publishCameraTrack = true
            mediaOptions.publishMicrophoneTrack = true
            ShowTo1v1Logger.info("broadcaster joinChannel[\(channelId)] \(uid)")
            rtcEngine?.joinChannel(byToken: broadcasterToken,
                                   channelId: channelId,
                                   uid: uid,
                                   mediaOptions: mediaOptions,
                                   joinSuccess: {[weak self] channelId, uid, elapsed in
                ShowTo1v1Logger.info("broadcaster joinChannel[\(channelId)] success:  \(uid)")
                guard let self = self, let rtcEngine = self.rtcEngine else {return}
                self.callApi?.setupContentInspectConfig(rtcEngine: rtcEngine, enable: true, uid: "\(uid)", channelId: channelId)
                self.callApi?.moderationAudio(channelName: channelId)
            })
            
            
            let config = AgoraVideoEncoderConfiguration()
            config.dimensions = CGSize(width: 720, height: 1280)
            config.frameRate = .fps24
            rtcEngine?.setVideoEncoderConfiguration(config)
            
            _setupCanvas(view: remoteCanvasView)
            //主播的直播数据面板
            rtcEngine?.addDelegate(self.realTimeView)
            
            bottomBar.buttonTypes = [.more]
        } else {
            guard let token = broadcasterToken else {
                assert(false, "render fail")
                return
            }
            roomInfo.token = token
            let room = roomInfo.anchorInfoList.first!
            let container = VideoCanvasContainer()
            container.container = remoteCanvasView
            container.uid = roomInfo.getUIntUserId()
            VideoLoaderApiImpl.shared.renderVideo(anchorInfo: room, container: container)
            //观众的直播数据面板
//            let connection = AgoraRtcConnection(channelId: roomInfo.roomId, localUid: Int(uid))
//            rtcEngine?.addDelegateEx(self.realTimeView, connection: connection)
            VideoLoaderApiImpl.shared.addRTCListener(anchorId: room.channelName, listener: self.realTimeView)
            
            bottomBar.buttonTypes = [.call, .more]
            
            VideoLoaderApiImpl.shared.switchAnchorState(newState: .joinedWithAudioVideo, localUid: UInt(uid), anchorInfo: room, tagId: roomInfo.roomId)
        }
    }
    
    private func _leaveRTCChannel() {
        guard let currentUser = currentUser, let roomInfo = roomInfo, let uid = UInt(currentUser.uid) else {return}
        if currentUser.uid == roomInfo.uid {
            rtcEngine?.leaveChannel()
            rtcEngine?.removeDelegate(self.realTimeView)
        } else {
            //观众不需要离开频道，交给场景化api处理，需要移除画布并静音
//            let connection = AgoraRtcConnection(channelId: roomInfo.roomId, localUid: Int(uid))
//            rtcEngine?.removeDelegateEx(self.realTimeView, connection: connection)
            VideoLoaderApiImpl.shared.removeRTCListener(anchorId: roomInfo.channelName(), listener: self.realTimeView)
            
            let room = roomInfo.anchorInfoList.first!
            let container = VideoCanvasContainer()
            container.setupMode = .remove
            container.container = remoteCanvasView
            container.uid = roomInfo.getUIntUserId()
            VideoLoaderApiImpl.shared.renderVideo(anchorInfo: room, container: container)
            
            VideoLoaderApiImpl.shared.switchAnchorState(newState: .joinedWithVideo, localUid: UInt(uid), anchorInfo: room, tagId: roomInfo.roomId)
        }
    }
    
    private func _publishMedia(_ publish: Bool) {
        guard let currentUser = currentUser, let roomInfo = roomInfo, let uid = UInt(currentUser.uid) else {return}
        if publish {
            rtcEngine?.enableLocalVideo(true)
            rtcEngine?.enableLocalAudio(true)
        }
        if currentUser.uid == roomInfo.uid {
            let mediaOptions = AgoraRtcChannelMediaOptions()
            mediaOptions.publishCameraTrack = publish
            mediaOptions.publishMicrophoneTrack = publish
            rtcEngine?.updateChannel(with: mediaOptions)
        }
    }
    
    private func _setupCanvas(view: UIView?) {
        guard let currentUser = currentUser, let roomInfo = roomInfo, let uid = UInt(currentUser.uid) else {return}
        if currentUser.uid == roomInfo.uid {
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
            callApi?.call(remoteUserId: roomInfo!.getUIntUserId(), completion: {[weak self] err in
                guard let err = err else { return }
                self?.callApi?.cancelCall(completion: { _ in
                })
                
                let msg = "\("call_toast_callfail".showTo1v1Localization()): \(err.code)"
                AUIToast.show(text: msg)
            })
            return
        }
        super.onClick(actionType: actionType)
    }
}

extension BroadcasterViewController {
    override func onCallStateChanged(with state: CallStateType, 
                                     stateReason: CallStateReason,
                                     eventReason: String,
                                     eventInfo: [String : Any]) {
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
