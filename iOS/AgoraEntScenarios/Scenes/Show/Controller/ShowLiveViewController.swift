//
//  ShowLiveViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/7.
//

import UIKit
import AgoraRtcKit
import IQKeyboardManager

class ShowLiveViewController: UIViewController {

    var room: ShowRoomListModel?
    
    var selectedResolution = 1
    
    private lazy var settingMenuVC: ShowToolMenuViewController = {
        let settingMenuVC = ShowToolMenuViewController()
        settingMenuVC.type = ShowMenuType.idle_audience
        settingMenuVC.delegate = self
        return settingMenuVC
    }()
    
    lazy var agoraKitManager: ShowAgoraKitManager = {
        return ShowAgoraKitManager()
    }()
    
//    private var settingManager: ShowSettingManager?
    
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
//    private lazy var musicManager: ShowMusicManager? = {
//        guard let agorakit = agoraKitManager.agoraKit else { return nil }
//        return ShowMusicManager(agoraKit: agorakit)
//    }()
    
    private lazy var liveView: ShowRoomLiveView = {
        let view = ShowRoomLiveView(isBroadcastor: role == .broadcaster)
        view.delegate = self
        return view
    }()
    
    private lazy var beautyVC = ShowBeautySettingVC()
    private lazy var realTimeView = ShowRealTimeDataView(isLocal: role == .broadcaster)
    private lazy var applyAndInviteView = ShowApplyAndInviteView(roomId: room?.roomId)
    private lazy var applyView = ShowApplyView()
    
    //PK popup list view
    private lazy var pkInviteView = ShowPKInviteView()
    
    //pk user list (room list)
    private var pkUserInvitationList: [ShowPKUserInfo]? {
        didSet {
            self.pkInviteView.pkUserInvitationList = pkUserInvitationList ?? []
        }
    }
    
    //interaction list
    private var interactionList: [ShowInteractionInfo]? {
        didSet {
            self.pkInviteView.interactionList = interactionList ?? []
        }
    }
    
    //pk invitation request map
    private var createPKInvitationMap: [String: ShowPKInvitation] = [String: ShowPKInvitation]()
    
    //get current interaction status
    private var interactionStatus: ShowInteractionStatus {
        return interactionList?.filter({ $0.interactStatus != .idle }).first?.interactStatus ?? .idle
    }
    
    private var currentInteraction: ShowInteractionInfo? {
        didSet {
            if oldValue == currentInteraction {
                return
            }
            
            if let info = oldValue {
                _stopInteraction(interaction: info)
            }
            if let info = currentInteraction {
                _startInteraction(interaction: info)
            }
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
        if role == .audience {
            liveView.roomUserCount += 1
        }
        view.addSubview(liveView)
        liveView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    private func leaveRoom(){
        ByteBeautyManager.shareManager.destroy()
        agoraKitManager.leaveChannel()
        AppContext.showServiceImp.unsubscribeEvent(delegate: self)
        dismiss(animated: true) {
            AppContext.showServiceImp.leaveRoom { error in
                print("error == \(error.debugDescription)")
            }
        }
    }
    
    private func joinChannel() {
        agoraKitManager.delegate = self
//        agoraKitManager.defaultSetting()
        guard let channelName = room?.roomId, let uid: UInt = UInt(currentUserId), let ownerId = room?.ownerId else {
            return
        }
        let ret = agoraKitManager.joinChannel(channelName: channelName, uid: uid, ownerId: ownerId, canvasView: liveView.canvasView.localView)
        if ret == 0 {
            print("进入房间")
//            settingManager = ShowSettingManager(agoraKit: agoraKitManager.agoraKit)
        }else{
            print("进入房间失败=====\(ret.debugDescription)")
            showError(title: "Join room failed", errMsg: "Error \(ret.debugDescription) occur")
        }
        liveView.canvasView.setLocalUserInfo(name: VLUserCenter.user.name)
        
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
        
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    private func _refreshPKUserList() {
        AppContext.showServiceImp.getAllPKUserList { [weak self] (error, pkUserList) in
            self?.pkUserInvitationList = pkUserList
        }
    }
    
    private func _refreshInteractionList() {
        AppContext.showServiceImp.getAllInterationList { [weak self] (error, interactionList) in
            guard let self = self, error == nil else { return }
            if self.interactionList == nil, let interaction = interactionList?.first {
                // first load
                if self.role == .broadcaster {
                    AppContext.showServiceImp.stopInteraction(interaction: interaction) { err in
                    }
                } else {
                    self.onInteractionBegan(interaction: interaction)
                }
            }
            
            self.interactionList = interactionList
        }
    }
    
    //MARK: ShowSubscribeServiceProtocol
    func onConnectStateChanged(state: ShowServiceConnectState) {
        guard state == .open else {
            ToastView.show(text: "net work error: \(state)")
            return
        }
        
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    func onRoomExpired() {
        let vc = ShowReceiveLiveFinishAlertVC()
        vc.dismissAlert { [weak self] in
            self?.leaveRoom()
        }
        
        self.present(vc, animated: true)
    }
    
    func onUserCountChanged(userCount: Int) {
        self.liveView.roomUserCount = userCount
    }
    
    func onUserJoinedRoom(user: ShowUser) {
        
    }
    
    func onUserLeftRoom(user: ShowUser) {
        if user.userId == room?.ownerId {
            ShowReceiveLiveFinishAlertVC.present { [weak self] in
                if self?.presentedViewController != nil {
                    self?.presentedViewController?.dismiss(animated: false)
                }
                self?.leaveRoom()
            }
        }
    }
    
    func onMessageDidAdded(message: ShowMessage) {
        if let text = message.message {
            let model = ShowChatModel(userName: message.userName ?? "", text: text)
            self.liveView.addChatModel(model)
        }
    }
    
    func onMicSeatApplyUpdated(apply: ShowMicSeatApply) {
        if apply.status == .waitting && role == .broadcaster {
            liveView.bottomBar.linkButton.isShowRedDot = true

        } else if apply.status == .accepted {
            liveView.canvasView.canvasType = .joint_broadcasting
            liveView.canvasView.setRemoteUserInfo(name: apply.userName ?? "")
            agoraKitManager.switchRole(role: .broadcaster,
                                       uid: apply.userId,
                                       canvasView: liveView.canvasView.remoteView)
            liveView.bottomBar.linkButton.isSelected = true
            liveView.bottomBar.linkButton.isShowRedDot = false
            
        } else if apply.status == .rejected {
            applyView.getAllMicSeatList(autoApply: false)
            liveView.bottomBar.linkButton.isShowRedDot = false
            liveView.bottomBar.linkButton.isSelected = false
            
        } else {
            liveView.canvasView.canvasType = .none
            liveView.bottomBar.linkButton.isSelected = false
        }
    }
    
    func onMicSeatApplyDeleted(apply: ShowMicSeatApply) {
        guard  apply.userId == VLUserCenter.user.id else { return }
//        ToastView.show(text: "seat apply \(apply.userName ?? "") did reject")
        if role == .broadcaster {
            applyAndInviteView.reloadData()
        }
    }
    
    func onMicSeatApplyAccepted(apply: ShowMicSeatApply) {
        applyAndInviteView.reloadData()
        liveView.canvasView.canvasType = .joint_broadcasting
        liveView.canvasView.setRemoteUserInfo(name: apply.userName ?? "")
    }
    
    func onMicSeatApplyRejected(apply: ShowMicSeatApply) {
        guard  apply.userId == VLUserCenter.user.id else { return }
//        ToastView.show(text: "seat apply \(apply.userName ?? "") did reject")
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
        guard "\(roomOwnerId)" == invitation.userId else { return }
//        ToastView.show(text: "seat invitation \(invitation.userName ?? "") did reject")
    }

    func onMicSeatInvitationAccepted(invitation: ShowMicSeatInvitation) {
        liveView.canvasView.canvasType = .joint_broadcasting
        liveView.canvasView.setRemoteUserInfo(name: invitation.userName ?? "")
//        ToastView.show(text: "seat invitation \(invitation.userId ?? "") did accept")
        guard invitation.userId == VLUserCenter.user.id else { return }
        agoraKitManager.switchRole(role: .broadcaster,
                                   uid: invitation.userId,
                                   canvasView: liveView.canvasView.remoteView)
    }
    
    func onMicSeatInvitationRejected(invitation: ShowMicSeatInvitation) {
        guard role == .broadcaster else { return }
        AlertManager.hiddenView()
        let alertVC = UIAlertController(title: "\(invitation.userName ?? "")拒绝了您的连麦邀请", message: nil, preferredStyle: .alert)
        let agree = UIAlertAction(title: "确定", style: .default, handler: nil)
        alertVC.addAction(agree)
        present(alertVC, animated: true, completion: nil)
    }
    
    func onPKInvitationUpdated(invitation: ShowPKInvitation) {
        if invitation.fromRoomId == room?.roomId {
            //send invitation
            createPKInvitationMap[invitation.roomId ?? ""] = invitation
            pkInviteView.createPKInvitationMap = createPKInvitationMap
            
            return
        }
        
        //recv invitation
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
        
        if invitation.fromRoomId == room?.roomId {
            //send invitation
            createPKInvitationMap[invitation.roomId ?? ""] = invitation
            pkInviteView.createPKInvitationMap = createPKInvitationMap
            
            return
        }
        
        //recv invitation
//        ToastView.show(text: "pk invitation \(invitation.roomId ?? "") did accept")
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    func onPKInvitationRejected(invitation: ShowPKInvitation) {
        guard  invitation.fromUserId == VLUserCenter.user.id else { return }
        
        if invitation.fromRoomId == room?.roomId {
            //send invitation
            createPKInvitationMap[invitation.roomId ?? ""] = nil
            pkInviteView.createPKInvitationMap = createPKInvitationMap
            return
        }
        
        //recv invitation
//        ToastView.show(text: "pk invitation \(invitation.roomId ?? "") did reject")
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    func onInterationUpdated(interaction: ShowInteractionInfo) {
        liveView.canvasView.isLocalMuteMic = interaction.ownerMuteAudio
        liveView.canvasView.isRemoteMuteMic = interaction.muteAudio
    }
    
    func onInteractionBegan(interaction: ShowInteractionInfo) {
        self.currentInteraction = interaction
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    func onInterationEnded(interaction: ShowInteractionInfo) {
        self.currentInteraction = nil
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    private func _startInteraction(interaction: ShowInteractionInfo) {
        switch interaction.interactStatus {
        case .pking:
            if room?.roomId != interaction.roomId {
                agoraKitManager.joinChannelEx(channelName: interaction.roomId,
                                              ownerId: interaction.userId,
                                              view: liveView.canvasView.remoteView,
                                              role: role)
                liveView.canvasView.canvasType = .pk
                liveView.canvasView.setRemoteUserInfo(name: interaction.userName ?? "")
            }
            
            break
            
        case .onSeat:
            liveView.canvasView.canvasType = .joint_broadcasting
            liveView.canvasView.setRemoteUserInfo(name: interaction.userName ?? "")
            agoraKitManager.switchRole(role: .broadcaster,
                                            uid: interaction.userId,
                                            canvasView: self.liveView.canvasView.remoteView)
            liveView.bottomBar.linkButton.isSelected = true
            liveView.bottomBar.linkButton.isShowRedDot = false
            
        default:
            break
        }
    }
    
    private func _stopInteraction(interaction: ShowInteractionInfo) {
        let options = AgoraRtcChannelMediaOptions()
        switch interaction.interactStatus {
        case .pking:
            agoraKitManager.leaveChannelEx()
            liveView.canvasView.canvasType = .none
            liveView.canvasView.setRemoteUserInfo(name: "")
            if interaction.userId == VLUserCenter.user.id {
                options.publishCameraTrack = false
                options.publishMicrophoneTrack = false
            }
            
        case .onSeat:
            let videoCanvas = AgoraRtcVideoCanvas()
            videoCanvas.uid = UInt(interaction.userId ?? "") ?? 0
            videoCanvas.view = nil
            videoCanvas.renderMode = .hidden
            agoraKitManager.agoraKit?.setupRemoteVideo(videoCanvas)
            liveView.canvasView.setRemoteUserInfo(name: "")
            liveView.canvasView.canvasType = .none
            applyView.getAllMicSeatList(autoApply: false)
            liveView.bottomBar.linkButton.isShowRedDot = false
            liveView.bottomBar.linkButton.isSelected = false
            if interaction.userId == VLUserCenter.user.id {
                options.publishMicrophoneTrack = false
            }
            
        default:
            break
        }
        let text = interaction.interactStatus == .pking ? "PK已断开哦".show_localized : "连麦已断开哦".show_localized
        ToastView.show(text: text)
        agoraKitManager.agoraKit.updateChannel(with: options)
        
    }
}


extension ShowLiveViewController: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
//        LogUtils.log(message: "warning: \(warningCode.description)", level: .warning)
    }
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
//        LogUtils.log(message: "error: \(errorCode)", level: .error)
//        showError(title: "Error", errMsg: "Error \(errorCode.rawValue) occur")
        print("errorCode == \(errorCode.rawValue)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
//        LogUtils.log(message: "Join \(channel) with uid \(uid) elapsed \(elapsed)ms", level: .info)
        print("-----didJoinChannel")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        print("join Uid === \(uid)")
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
//        let videoCanvas = AgoraRtcVideoCanvas()
//        videoCanvas.uid = uid
//        videoCanvas.view = nil
//        videoCanvas.renderMode = .hidden
//        agoraKitManager.agoraKit?.setupRemoteVideo(videoCanvas)
//        liveView.canvasView.setRemoteUserInfo(name: "")
//        liveView.canvasView.canvasType = .none
//        print("didOfflineOfUid: \(reason) \(uid) \(self.currentInteraction?.userId)")
        if let interaction = self.currentInteraction {
            let isRoomOwner: Bool = role == .broadcaster ? true : false
            let isInteractionLeave: Bool = interaction.userId == "\(uid)"
            let roomOwnerExit: Bool = room?.ownerId ?? "" == "\(uid)"
            if roomOwnerExit {
                //room owner exit
                AppContext.showServiceImp.stopInteraction(interaction: interaction) { err in
                }
            } else if isRoomOwner, isInteractionLeave {
                //room owner found interaction(pk/onseat) user offline
                AppContext.showServiceImp.stopInteraction(interaction: interaction) { err in
                }
            }
        }
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, reportRtcStats stats: AgoraChannelStats) {
        realTimeView.statsInfo?.updateChannelStats(stats)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
        realTimeView.statsInfo?.updateLocalAudioStats(stats)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, localVideoStats stats: AgoraRtcLocalVideoStats, sourceType: AgoraVideoSourceType) {
        realTimeView.statsInfo?.updateLocalVideoStats(stats)
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
    func onClickRemoteCanvas() {
        guard let info = interactionList?.first else { return }
        let menuVC = ShowToolMenuViewController()
        menuVC.type = ShowMenuType.managerMic
        menuVC.menuTitle = "对观众\(info.userName ?? "")"
        menuVC.delegate = self
        present(menuVC, animated: true)
    }
    
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
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    func onClickLinkButton(_ button: ShowRedDotButton) {
        if role == .broadcaster {
            applyAndInviteView.reloadData()
            AlertManager.show(view: applyAndInviteView, alertPostion: .bottom)
            
        } else {
            applyView.getAllMicSeatList(autoApply: role == .audience)
            AlertManager.show(view: applyView, alertPostion: .bottom)
        }
    }
    
    func onClickBeautyButton() {
        present(beautyVC, animated: true)
    }
    
    func onClickMusicButton() {
        let vc = ShowMusicEffectVC()
        vc.musicManager = agoraKitManager
        present(vc, animated: true)
    }
    
    func onClickSettingButton() {
        if interactionStatus == .idle {
            settingMenuVC.type = role == .broadcaster ? .idle_broadcaster : .idle_audience
        }else{
            settingMenuVC.type = .pking
            settingMenuVC.menuTitle = "show_setting_menu_on_pk_title".show_localized
        }
        present(settingMenuVC, animated: true)
    }
    
}


extension ShowLiveViewController {
    private func showError(title: String, errMsg: String) {
        showAlert(title: title, message: errMsg) { [weak self] in
            self?.leaveRoom()
        }
    }
}

extension ShowLiveViewController: ShowToolMenuViewControllerDelegate {
    
    // 开关摄像头
    func onClickCameraButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        if selected {
            agoraKitManager.agoraKit.stopPreview()
            agoraKitManager.agoraKit.enableLocalVideo(false)
        }else{
            agoraKitManager.agoraKit.startPreview()
            agoraKitManager.agoraKit.enableLocalVideo(true)
        }
    }
    
    // 画质
    func onClickHDButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        settingMenuVC.dismiss(animated: false)
        
        let vc = ShowSelectQualityVC()
        vc.defalutSelectIndex = selectedResolution
        present(vc, animated: false)
        vc.dismissed = { [weak self] in
            guard let wSelf = self else { return }
            wSelf.present(wSelf.settingMenuVC, animated: false)
        }
        vc.selectedItem = {[weak self] resolution,index in
            guard let wSelf = self else { return }
            wSelf.selectedResolution = index
            wSelf.agoraKitManager.setCaptureVideoDimensions(CGSize(width: resolution.width, height: resolution.height))
        }
    }
    
    // 结束连麦
    func onClickEndPkButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        guard let info = interactionList?.first else { return }
        AppContext.showServiceImp.stopInteraction(interaction: info) { _ in
        }
    }
    
    // 麦克风开关
    func onClickMicButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        agoraKitManager.agoraKit.muteRecordingSignal(selected)
//        AppContext.showServiceImp.muteAudio(mute: selected, userId: VLUserCenter.user.id) { err in
//        }
    }
    
    // 静音
    func onClickMuteMicButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        agoraKitManager.agoraKit.muteAllRemoteAudioStreams(selected)
        let uid = menu.type == .managerMic ? currentInteraction?.userId ?? "" : VLUserCenter.user.id
        AppContext.showServiceImp.muteAudio(mute: selected, userId: uid) { err in
        }
    }
    
    func onClickRealTimeDataButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        AlertManager.show(view: realTimeView, alertPostion: .top)
    }
    
    func onClickSwitchCameraButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        agoraKitManager.switchCamera()
    }
    
    func onClickSettingButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        settingMenuVC.dismiss(animated: true) {[weak self] in
            guard let wSelf = self else { return }
            let vc = ShowAdvancedSettingVC()
            vc.isOutside = false
            vc.mode = wSelf.interactionStatus == .pking ? .pk : .signle // 根据当前模式设置
            vc.isBroadcaster = wSelf.role == .broadcaster
            vc.settingManager = wSelf.agoraKitManager
            wSelf.navigationController?.pushViewController(vc, animated: true)
        }
    }
    
}

