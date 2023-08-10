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

class BroadcasterViewController: BaseRoomViewController {
    var videoLoader: IVideoLoaderApi?
    
    var currentUser: ShowTo1v1UserInfo?
    override var roomInfo: ShowTo1v1RoomInfo? {
        didSet {
            let createdAt = roomInfo?.createdAt ?? 0
            roomInfoView.setRoomInfo(avatar: roomInfo?.avatar ?? "",
                                     name: roomInfo?.roomName ?? "",
                                     id: roomInfo?.userName ?? "",
                                     time: Int64(createdAt > 0 ? createdAt : Int64(Date().timeIntervalSince1970) * 1000))
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
        button.aui_size = CGSize(width: 32, height: 32)
        button.setImage(UIImage.sceneImage(name: "live_close"), for: .normal)
        button.addTarget(self, action: #selector(onBackAction), for: .touchUpInside)
        return button
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
        
        closeButton.aui_right = view.aui_width - 15
        closeButton.aui_centerY = roomInfoView.aui_centerY
        view.addSubview(closeButton)
        
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
                                   joinSuccess: { channelId, uid, elapsed in
                showTo1v1Print("broadcaster joinChannel[\(channelId)] success:  \(uid)")
            })
            
            _setupCanvas(view: bigCanvasView)
            
            rtcEngine?.delegate = self.realTimeView
        } else {
            guard let token = broadcasterToken else {
                assert(false, "render fail")
                return
            }
            let room = roomInfo.createRoomInfo(token: token)
            let container = VideoCanvasContainer()
            container.container = bigCanvasView
            container.uid = roomInfo.getUIntUserId()
            videoLoader?.renderVideo(roomInfo: room, container: container)
            
            videoLoader?.addRTCListener(roomId: room.channelName, listener: self.realTimeView)
        }
    }
    
    private func _leaveRTCChannel() {
        guard let currentUser = currentUser, let roomInfo = roomInfo, let uid = UInt(currentUser.userId) else {return}
        if currentUser.userId == roomInfo.userId {
            rtcEngine?.leaveChannel()
            
            rtcEngine?.delegate = nil
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
        super.onBackAction()
        
        _leaveRTCChannel()
    }
}

extension BroadcasterViewController {
    override func onCallStateChanged(with state: CallStateType,
                            stateReason: CallReason,
                            eventReason: String,
                            elapsed: Int,
                            eventInfo: [String : Any]) {
        let publisher = eventInfo[kPublisher] as? String ?? currentUser?.userId
        guard publisher == currentUser?.userId else {
            return
        }
        
        switch state {
        case .calling:
            _publishMedia(false)
            _setupCanvas(view: nil)
            break
        case .prepared, .idle, .failed:
            _publishMedia(true)
            _setupCanvas(view: bigCanvasView)
            break
        default:
            break
        }
    }
}
