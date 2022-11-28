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
    
    lazy var agoraKitManager: ShowAgoraKitManager = {
        return ShowAgoraKitManager()
    }()
    
    private var settingManager: ShowSettingManager?
    
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
        guard let agorakit = agoraKitManager.agoraKit else { return nil }
        return ShowMusicManager(agoraKit: agorakit)
    }()
    
    private lazy var liveView: ShowRoomLiveView = {
        let view = ShowRoomLiveView(isBroadcastor: role == .broadcaster)
        view.delegate = self
        return view
    }()
    
    private lazy var beautyVC = ShowBeautySettingVC()
    //TODO: 实时数据View, 逻辑已处理完,  没找到弹窗的Button
    private lazy var realTimeView = ShowRealTimeDataView(isLocal: false)
    private lazy var applyAndInviteView = ShowApplyAndInviteView(roomId: room?.roomId)
    private lazy var applyView = ShowApplyView()
    
    //PK popup list view
    private lazy var pkInviteView = ShowPKInviteView()
    
    private var pkUserInvitationList: [ShowPKUserInfo]? {
        didSet {
            self.pkInviteView.pkUserInvitationList = pkUserInvitationList ?? []
        }
    }
    
    private var interactionList: [ShowInteractionInfo]? {
        didSet {
            self.pkInviteView.interactionList = interactionList ?? []
        }
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.layer.contents = UIImage.show_sceneImage(name: "show_live_pkbg")?.cgImage
        setupUI()
        joinChannel()
        _subscribeServiceEvent()
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
    
    private func leaveRoom(){
        ByteBeautyManager.shareManager.destroy()
        agoraKitManager.leaveChannel()
        dismiss(animated: true) {
            AppContext.showServiceImp.leaveRoom { error in
                print("error == \(error.debugDescription)")
            }
        }
    }
    
    private func joinChannel() {
        agoraKitManager.delegate = self
        guard let channelName = room?.roomId, let uid: UInt = UInt(currentUserId), let ownerId = room?.ownerId else {
            return
        }
        let ret = agoraKitManager.joinChannel(channelName: channelName, uid: uid, ownerId: ownerId, canvasView: liveView.canvasView)
        if ret == 0 {
            print("进入房间")
            settingManager = ShowSettingManager(agoraKit: agoraKitManager.agoraKit)
        }else{
            print("进入房间失败=====\(ret.debugDescription)")
            showError(title: "Join room failed", errMsg: "Error \(ret.debugDescription) occur")
        }
        
//        let canvas = AgoraRtcVideoCanvas()
//        canvas.view = liveView.canvasView.localView
//        canvas.renderMode = .hidden
//        canvas.uid = uid
        liveView.canvasView.setLocalUserInfo(name: VLUserCenter.user.name)
//        if role == .broadcaster {
//            canvas.mirrorMode = .disabled
//            agoraKitManager.agoraKit?.setupLocalVideo(canvas)
//        } else {
//            agoraKitManager.agoraKit?.setupRemoteVideo(canvas)
//        }
//        agoraKitManager.agoraKit?.startPreview()
        
        sendMessageWithText("join_live_room".show_localized)
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
}

//MARK: service subscribe
extension ShowLiveViewController: ShowSubscribeServiceProtocol {
    private func _subscribeServiceEvent() {
        let service = AppContext.showServiceImp
        
        service.subscribeEvent(delegate: self)
        
        //TODO: migration
        applyAndInviteView.applyStatusClosure = { [weak self] status in
            self?.liveView.canvasView.canvasType = status == .onSeat ? .joint_broadcasting : .none
        }
        
        _refreshInvitationList()
        _refreshInteractionList()
    }
    
    private func _refreshInvitationList() {
        AppContext.showServiceImp.getAllPKUserList { [weak self] (error, pkUserList) in
            self?.pkUserInvitationList = pkUserList
        }
    }
    
    private func _refreshInteractionList() {
        AppContext.showServiceImp.getAllInterationList { [weak self] (error, interactionList) in
            self?.interactionList = interactionList
        }
    }
    
    
    //MARK: ShowSubscribeServiceProtocol
    func onUserJoinedRoom(user: ShowUser) {
        
    }
    
    func onUserLeftRoom(user: ShowUser) {
        if user.userId == room?.ownerId {
            //TODO: leave query dialog
        }
    }
    
    func onMessageDidAdded(message: ShowMessage) {
        if let text = message.message {
            let model = ShowChatModel(userName: message.userName ?? "", text: text)
            self.liveView.addChatModel(model)
        }
    }
    
    func onMicSeatApplyUpdated(apply: ShowMicSeatApply) {
        
    }
    
    func onMicSeatApplyDeleted(apply: ShowMicSeatApply) {
        guard  apply.userId == VLUserCenter.user.id else { return }
        ToastView.show(text: "seat apply \(apply.userName ?? "") did reject")
    }
    
    func onMicSeatApplyAccepted(apply: ShowMicSeatApply) {
        
    }
    
    func onMicSeatApplyRejected(apply: ShowMicSeatApply) {
        guard  apply.userId == VLUserCenter.user.id else { return }
        ToastView.show(text: "seat apply \(apply.userName ?? "") did reject")
    }
    
    func onMicSeatInvitationUpdated(invitation: ShowMicSeatInvitation) {
        guard invitation.userId == VLUserCenter.user.id else { return }
        if invitation.status == .waitting {
            let vc = ShowReceivePKAlertVC()
            vc.name = invitation.userName ?? ""
            vc.dismissWithResult { result in
                let imp = AppContext.showServiceImp
                switch result {
                case .accept:
                    AppContext.showServiceImp.acceptMicSeatInvitation { error in
                        
                    }
                    break
                default:
                    imp.rejectMicSeatInvitation { error in
                        
                    }
                    break
                }
            }
            
            self.present(vc, animated: true)
        }
    }
    
    func onMicSeatInvitationDeleted(invitation: ShowMicSeatInvitation) {
        guard  invitation.userId == VLUserCenter.user.id else { return }
        ToastView.show(text: "seat invitation \(invitation.userName ?? "") did reject")
    }
    
    func onMicSeatInvitationAccepted(invitation: ShowMicSeatInvitation) {
        //nothing todo, see onInteractionBegan
        guard  invitation.userId == VLUserCenter.user.id else { return }
        ToastView.show(text: "seat invitation \(invitation.userId ?? "") did accept")
    }
    
    func onMicSeatInvitationRejected(invitation: ShowMicSeatInvitation) {
        guard  invitation.userId == VLUserCenter.user.id else { return }
        ToastView.show(text: "seat invitation \(invitation.userName ?? "") did reject")
    }
    
    func onPKInvitationUpdated(invitation: ShowPKInvitation) {
        if invitation.status == .waitting {
            let vc = ShowReceivePKAlertVC()
            vc.name = invitation.fromName ?? ""
            vc.dismissWithResult { result in
                let imp = AppContext.showServiceImp
                switch result {
                case .accept:
                    AppContext.showServiceImp.acceptPKInvitation { error in
                        
                    }
                    break
                default:
                    imp.rejectPKInvitation { error in
                        
                    }
                    break
                }
            }
            
            self.present(vc, animated: true)
        }
    }
    
    func onPKInvitationAccepted(invitation: ShowPKInvitation) {
        //nothing todo, see onInteractionBegan
        guard  invitation.fromUserId == VLUserCenter.user.id else { return }
        ToastView.show(text: "pk invitation \(invitation.roomId ?? "") did accept")
        _refreshInvitationList()
    }
    
    func onPKInvitationRejected(invitation: ShowPKInvitation) {
        guard  invitation.fromUserId == VLUserCenter.user.id else { return }
        ToastView.show(text: "pk invitation \(invitation.roomId ?? "") did reject")
        _refreshInvitationList()
    }
    
    func onInteractionBegan(interaction: ShowInteractionInfo) {
        switch interaction.interactStatus {
        case .pking:
            break
        default:
            break
        }
    }
    
    func onInterationEnded(interaction: ShowInteractionInfo) {
        
        switch interaction.interactStatus {
        case .pking:
            break
        default:
            break
        }
    }
}


extension ShowLiveViewController: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
//        LogUtils.log(message: "warning: \(warningCode.description)", level: .warning)
    }
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
//        LogUtils.log(message: "error: \(errorCode)", level: .error)
        showError(title: "Error", errMsg: "Error \(errorCode.rawValue) occur")

    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
//        LogUtils.log(message: "Join \(channel) with uid \(uid) elapsed \(elapsed)ms", level: .info)
        print("-----didJoinChannel")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = liveView.canvasView.remoteView
        videoCanvas.renderMode = .hidden
        agoraKitManager.agoraKit?.setupRemoteVideo(videoCanvas)
        liveView.canvasView.setRemoteUserInfo(name: "\(uid)")
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
//        LogUtils.log(message: "remote user leval: \(uid) reason \(reason)", level: .info)
//        didOfflineOfUid(uid: uid)
//        if roomOwnerId == uid {
//            let vc = ShowReceiveLiveFinishAlertVC()
//            vc.dismissAlert { [weak self] in
//                self?.leaveRoom()
//            }
//            present(vc, animated: true)
//        }
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = nil
        videoCanvas.renderMode = .hidden
        agoraKitManager.agoraKit?.setupRemoteVideo(videoCanvas)
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
        if role == .broadcaster {
            showAlert(message: "show_alert_live_end_title".show_localized) {[weak self] in
                self?.leaveRoom()
            }
        }else {
            leaveRoom()
        }
    }
    
    func onClickPKButton(_ button: ShowRedDotButton) {
        AlertManager.show(view: pkInviteView, alertPostion: .bottom)
        _refreshInvitationList()
        _refreshInteractionList()
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
        vc.musicManager = musicManager
        present(vc, animated: true)
    }
    
    func onClickSettingButton() {
        let settingVC = ShowAdvancedSettingVC()
        settingVC.mode = .signle // 根据当前模式设置
        settingVC.isBroadcaster = role == .broadcaster
        settingVC.settingManager = settingManager
        navigationController?.pushViewController(settingVC, animated: true)
    }
    
}


extension ShowLiveViewController {
    private func showError(title: String, errMsg: String) {
        showAlert(title: title, message: errMsg) { [weak self] in
            self?.leaveRoom()
        }
    }
}
