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
    private lazy var beautyVC = ShowBeautySettingVC()
    //TODO: 实时数据View, 逻辑已处理完,  没找到弹窗的Button
    private lazy var realTimeView = ShowRealTimeDataView(isLocal: false)
    private lazy var applyAndInviteView = ShowApplyAndInviteView()
    private lazy var applyView = ShowApplyView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.layer.contents = UIImage.show_sceneImage(name: "show_live_pkbg")?.cgImage
        setupUI()
        setupAgoraKit()
        joinChannel()
        subscribeChatMsg()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
    }
    
    private func setupUI(){
        navigationController?.isNavigationBarHidden = true
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
        agoraKit?.setVideoFrameDelegate(self)
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
        canvas.view = liveView.canvasView.localView
        canvas.renderMode = .hidden
        canvas.uid = uid
        liveView.canvasView.setLocalUserInfo(name: VLUserCenter.user.name)
        if role == .broadcaster {
            canvas.mirrorMode = .disabled
            agoraKit?.setupLocalVideo(canvas)
        } else {
            agoraKit?.setupRemoteVideo(canvas)
        }
        agoraKit?.startPreview()
        sendMessageWithText("join_live_room".show_localized)
    }
    
    private func subscribeChatMsg(){
        /*
        AppContext.showServiceImp.subscribeMicSeatInvitation { [weak self] status, msg in
            if let text = msg.message {
                let model = ShowChatModel(userName: msg.userName ?? "", text: text)
                self?.liveView.addChatModel(model)
            }
        }
         */
    }
    
    private func sendMessageWithText(_ text: String) {
        let showMsg = ShowMessage()
        showMsg.userId = VLUserCenter.user.id
        showMsg.userName = VLUserCenter.user.name
        showMsg.message = text
        showMsg.createAt = Date().timeIntervalSince1970
        /*
        AppContext.showServiceImp.sendChatMessage(message: showMsg) { error in
            print("发送消息状态 \(error.localizedDescription) text = \(text)")
        }
         */
        let model = ShowChatModel(userName: VLUserCenter.user.name, text: text)
        liveView.addChatModel(model)
    }
    
    
}

extension ShowLiveViewController: AgoraVideoFrameDelegate {
    func onCapture(_ videoFrame: AgoraOutputVideoFrame) -> Bool {
        videoFrame.pixelBuffer = ByteBeautyManager.shareManager.processFrame(pixelBuffer: videoFrame.pixelBuffer)
        return true
    }
    
    func getVideoFormatPreference() -> AgoraVideoFormat {
        .cvPixelBGRA
    }
    
    func getVideoFrameProcessMode() -> AgoraVideoFrameProcessMode {
        .readWrite
    }
    
    func getMirrorApplied() -> Bool {
        true
    }
    
    func getRotationApplied() -> Bool {
        false
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
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = liveView.canvasView.remoteView
        videoCanvas.renderMode = .hidden
        agoraKit?.setupRemoteVideo(videoCanvas)
        liveView.canvasView.setRemoteUserInfo(name: "\(uid)")
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = nil
        videoCanvas.renderMode = .hidden
        agoraKit?.setupRemoteVideo(videoCanvas)
        liveView.canvasView.setRemoteUserInfo(name: "")
        liveView.canvasView.canvasType = .none
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, reportRtcStats stats: AgoraChannelStats) {
        realTimeView.statsInfo?.updateChannelStats(stats)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
        realTimeView.statsInfo?.updateLocalAudioStats(stats)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
        realTimeView.statsInfo?.updateVideoStats(stats)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteAudioStats stats: AgoraRtcRemoteAudioStats) {
        realTimeView.statsInfo?.updateAudioStats(stats)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, uplinkNetworkInfoUpdate networkInfo: AgoraUplinkNetworkInfo) {
        realTimeView.statsInfo?.updateUplinkNetworkInfo(networkInfo)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, downlinkNetworkInfoUpdate networkInfo: AgoraDownlinkNetworkInfo) {
        realTimeView.statsInfo?.updateDownlinkNetworkInfo(networkInfo)
    }
}


extension ShowLiveViewController: ShowRoomLiveViewDelegate {
    func onClickSendMsgButton(text: String) {
        sendMessageWithText(text)
    }
    
    func onClickCloseButton() {
        ByteBeautyManager.shareManager.destroy()
        dismiss(animated: true) {
            AppContext.showServiceImp.leaveRoom { error in
                print("error == \(error.debugDescription)")
            }
        }
    }
    
    func onClickPKButton(_ button: ShowRedDotButton) {
        let pkInviteView = ShowPKInviteView()
        AlertManager.show(view: pkInviteView, alertPostion: .bottom)
    }
    
    func onClickLinkButton(_ button: ShowRedDotButton) {
        if role == .broadcaster {
            AlertManager.show(view: applyAndInviteView, alertPostion: .bottom)
            
        } else {
            AlertManager.show(view: applyView, alertPostion: .bottom)
        }
    }
    
    func onClickBeautyButton() {
        present(beautyVC, animated: true)
    }
    
    func onClickMusicButton() {
        let vc = ShowMusicEffectVC()
        present(vc, animated: true)
    }
    
    func onClickSettingButton() {
        let settingVC = ShowAdvancedSettingVC()
        navigationController?.pushViewController(settingVC, animated: true)
    }
    
}
