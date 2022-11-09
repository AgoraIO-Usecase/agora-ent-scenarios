//
//  ShowLiveViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/7.
//

import UIKit
import AgoraRtcKit

class ShowLiveViewController: UIViewController {

    var room: ShowRoomListModel?
    var agoraKit: AgoraRtcEngineKit?
    
    private var currentUserId: String {
        get{
            VLUserCenter.user.id
        }
    }

    private var role: AgoraClientRole {
        return room?.ownerId == VLUserCenter.user.userNo ? .broadcaster : .audience
    }
    
    private lazy var liveView: ShowRoomLiveView = {
        let view = ShowRoomLiveView(isBroadcastor: role == .broadcaster)
        view.delegate = self
        return view
    }()
    
    private lazy var rtcEngineConfig: AgoraRtcEngineConfig = {
        let config = AgoraRtcEngineConfig()
        config.appId = KeyCenter.AppId
        config.channelProfile = .liveBroadcasting
        config.areaCode = .global
        return config
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        setupUI()
        setupAgoraKit()
        joinChannel()
    }
    
    private func setupUI(){
        liveView.room = room
        view.addSubview(liveView)
        liveView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    private func setupAgoraKit() {
        if agoraKit == nil {
            agoraKit = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: self)
        }else{
            agoraKit?.delegate = self
        }
        let roleOptions = AgoraClientRoleOptions()
        roleOptions.audienceLatencyLevel = role == .audience ? .ultraLowLatency : .lowLatency
        agoraKit?.setClientRole(role, options: roleOptions)
        agoraKit?.enableVideo()
        agoraKit?.enableAudio()
        agoraKit?.setDefaultAudioRouteToSpeakerphone(true)
    }
    
    private func joinChannel() {
        guard let channelName = room?.roomNo, let uid: UInt = UInt(currentUserId) else {
            return
        }
        
        let result = agoraKit?.joinChannel(byToken: KeyCenter.Token, channelId: channelName, info: nil, uid: uid, joinSuccess: nil)
        if result == 0 {
            print("进入房间")
        }
        let canvas = AgoraRtcVideoCanvas()
        canvas.view = liveView.canvasView
        canvas.renderMode = .hidden
        canvas.uid = uid
        if role == .broadcaster {
            agoraKit?.setupLocalVideo(canvas)
        } else {
            agoraKit?.setupRemoteVideo(canvas)
        }
        agoraKit?.startPreview()
//        liveView.sendMessage(userName: UserInfo.uid, message: "Join_Live_Room".localized, messageType: .message)
    }
}


extension ShowLiveViewController: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
//        LogUtils.log(message: "warning: \(warningCode.description)", level: .warning)
    }
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
//        LogUtils.log(message: "error: \(errorCode)", level: .error)
//        showAlert(title: "Error", message: "Error \(errorCode.description) occur")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
//        LogUtils.log(message: "Join \(channel) with uid \(uid) elapsed \(elapsed)ms", level: .info)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
//        LogUtils.log(message: "remote user join: \(uid) \(elapsed)ms", level: .info)
//        liveView.sendMessage(userName: "\(uid)", message: "Join_Live_Room".localized, messageType: .message)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
//        LogUtils.log(message: "remote user leval: \(uid) reason \(reason)", level: .info)
//        didOfflineOfUid(uid: uid)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, reportRtcStats stats: AgoraChannelStats) {
//        localVideo.statsInfo?.updateChannelStats(stats)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
//        localVideo.statsInfo?.updateLocalAudioStats(stats)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
//        remoteVideo.statsInfo?.updateVideoStats(stats)
//        LogUtils.log(message: "remoteVideoWidth== \(stats.width) Height == \(stats.height)", level: .info)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteAudioStats stats: AgoraRtcRemoteAudioStats) {
//        remoteVideo.statsInfo?.updateAudioStats(stats)
    }
}


extension ShowLiveViewController: ShowRoomLiveViewDelegate {
    func onClickSendMsgButton(text: String) {
        let model = ShowChatModel(userName: VLUserCenter.user.name, text: text)
        liveView.addChatModel(model)
    }
    
    func onClickCloseButton() {
        dismiss(animated: true) {
            AppContext.showServiceImp.leaveRoom { error in
                print("error == \(error.debugDescription)")
            }
        }
    }
    
    func onClickPKButton(_ button: ShowRedDotButton) {
        
    }
    
    func onClickLinkButton(_ button: ShowRedDotButton) {
        
    }
    
    func onClickBeautyButton() {
        
    }
    
    func onClickMusicButton() {
        
    }
    
    func onClickSettingButton() {
        
    }
    
    
}
