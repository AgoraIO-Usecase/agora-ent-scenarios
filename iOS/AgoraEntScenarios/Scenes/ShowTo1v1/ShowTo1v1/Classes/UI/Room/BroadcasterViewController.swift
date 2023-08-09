//
//  BroadcasterViewController.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/7/28.
//

import UIKit
import AgoraRtcKit
import VideoLoaderAPI

class BroadcasterViewController: BaseRoomViewController {
    var videoLoader: IVideoLoaderApi?
    
    var currentUser: ShowTo1v1UserInfo?
    var roomInfo: ShowTo1v1RoomInfo? {
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
            let channelId = "broadcaster_\(uid)"
            rtcEngine?.setClientRole(.broadcaster)
            rtcEngine?.enableAudio()
            rtcEngine?.enableVideo()
            let mediaOptions = AgoraRtcChannelMediaOptions()
            mediaOptions.publishCameraTrack = true
            mediaOptions.publishMicrophoneTrack = true
            showTo1v1Print("broadcaster joinChannel: \(channelId) \(uid)")
            rtcEngine?.joinChannel(byToken: broadcasterToken,
                                   channelId: channelId,
                                   uid: uid,
                                   mediaOptions: mediaOptions, joinSuccess: { channelId, uid, elapsed in
                showTo1v1Print("broadcaster joinChannel success: \(channelId) \(uid)")
            })
            let canvas = AgoraRtcVideoCanvas()
            canvas.view = bigCanvasView
            canvas.uid = uid
            rtcEngine?.setupLocalVideo(canvas)
            
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
    
    override func onBackAction() {
        super.onBackAction()
        
        _leaveRTCChannel()
    }
}
