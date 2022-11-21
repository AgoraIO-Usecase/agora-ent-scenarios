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
    
    private var roomOwnerId: UInt {
        get{
            UInt(room?.ownerId ?? "0") ?? 0
        }
    }
    
    private var currentUserId: String {
        get{
            VLUserCenter.user.id
        }
    }

    private var role: AgoraClientRole {
        return room?.ownerId == VLUserCenter.user.id ? .broadcaster : .audience
    }
    
    // 音乐
    private lazy var musicManager: ShowMusicManager? = {
        guard let agorakit = agoraKit else { return nil }
        return ShowMusicManager(agoraKit: agorakit)
    }()
    
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
        subscribeChatMsg()
        UIApplication.shared.isIdleTimerDisabled = true
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
        agoraKit?.enableVideo()
        agoraKit?.enableAudio()
        agoraKit?.setDefaultAudioRouteToSpeakerphone(true)
    }
    
    private func joinChannel() {
        guard let channelName = room?.roomNo, let uid: UInt = UInt(currentUserId),let ownerId = room?.ownerId else {
            return
        }
        
        let ret = agoraKit?.joinChannel(byToken: KeyCenter.Token, channelId: channelName, info: nil, uid: uid)
        print("=======chnnelId == \(channelName), uid === \(uid)")
        if ret == 0 {
            print("进入房间")
        }else{
            print("进入房间失败=====\(ret.debugDescription)")
            showAlert(title: "进入房间失败", message: "Error \(ret.debugDescription) occur")
        }
        let canvas = AgoraRtcVideoCanvas()
        canvas.view = liveView.canvasView
        canvas.renderMode = .hidden
        if role == .broadcaster {
            canvas.uid = uid
            agoraKit?.setupLocalVideo(canvas)
        } else {
            canvas.uid = UInt(ownerId) ?? 0
            agoraKit?.setupRemoteVideo(canvas)
        }
        agoraKit?.startPreview()
        sendMessageWithText("join_live_room".show_localized)
    }
    
    private func subscribeChatMsg(){
        
        AppContext.showServiceImp.subscribeMessageChanged(subscribeClosure: { [weak self] status, msg in
            if let text = msg.message {
                let model = ShowChatModel(userName: msg.userName ?? "", text: text)
                self?.liveView.addChatModel(model)
            }
        })
    }
    
    private func sendMessageWithText(_ text: String) {
        let showMsg = ShowMessage()
        showMsg.userId = VLUserCenter.user.id
        showMsg.userName = VLUserCenter.user.name
        showMsg.message = text
        showMsg.createAt = Date().millionsecondSince1970()
        
        AppContext.showServiceImp.sendChatMessage(message: showMsg) { error in
            print("发送消息状态 \(error?.localizedDescription ?? "") text = \(text)")
        }
    }
    
    func showAlert(title: String? = nil, message: String) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        let action = UIAlertAction(title: "OK", style: .cancel) { action in
//            self.dismiss(animated: true)
        }
        alertController.addAction(action)
        present(alertController, animated: true, completion: nil)
    }
    
}


extension ShowLiveViewController: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
//        LogUtils.log(message: "warning: \(warningCode.description)", level: .warning)
    }
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
//        LogUtils.log(message: "error: \(errorCode)", level: .error)
        showAlert(title: "Error", message: "Error \(errorCode.rawValue) occur")

    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
//        LogUtils.log(message: "Join \(channel) with uid \(uid) elapsed \(elapsed)ms", level: .info)
        print("-----didJoinChannel")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
//        LogUtils.log(message: "remote user join: \(uid) \(elapsed)ms", level: .info)
//        liveView.sendMessage(userName: "\(uid)", message: "Join_Live_Room".localized, messageType: .message)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
//        LogUtils.log(message: "remote user leval: \(uid) reason \(reason)", level: .info)
//        didOfflineOfUid(uid: uid)
        if roomOwnerId == uid {
            let vc = ShowReceiveLiveFinishAlertVC()
            vc.dismissAlert { [weak self] in
                self?.dismiss(animated: true)
            }
            present(vc, animated: true)
        }
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
        sendMessageWithText(text)
    }
    
    func onClickCloseButton() {
        agoraKit?.leaveChannel()
        agoraKit?.disableAudio()
        agoraKit?.disableVideo()
        AgoraRtcEngineKit.destroy()
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
        let beautyVC = ShowBeautySettingVC()
        present(beautyVC, animated: true)
    }
    
    func onClickMusicButton() {
        let vc = ShowMusicEffectVC()
        vc.musicManager = musicManager
        present(vc, animated: true)
    }
    
    func onClickSettingButton() {
        let settingVC = ShowAdvancedSettingVC()
        settingVC.agoraKit = agoraKit
        navigationController?.pushViewController(settingVC, animated: true)
    }
    
}
