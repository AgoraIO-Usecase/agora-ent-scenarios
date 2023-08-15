//
//  ShowLiveViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/7.
//

import UIKit
import AgoraRtcKit
import SwiftUI
import VideoLoaderAPI

protocol ShowLiveViewControllerDelegate: NSObjectProtocol {
    func currentUserIsOnSeat()
    func currentUserIsOffSeat()
}

class ShowLiveViewController: UIViewController {
    weak var delegate: ShowLiveViewControllerDelegate?
    var room: ShowRoomListModel?
    var loadingType: RoomStatus = .prejoined {
        didSet {
            if loadingType == oldValue {
                return
            }
            if (loadingType == .joined) {// 秒开计时
                ShowAgoraKitManager.shared.callTimestampStart(clean: false)
            }
            updateLoadingType(playState: loadingType)
            remoteVideoWidth = nil
            currentMode = nil
        }
    }
    private var currentChannelId: String?
        
    private var checking = false
    
    private var roomId: String {
        get {
            guard let roomId = room?.roomId else {
                assert(false, "room == nil")
                return ""
            }
            
            return roomId
        }
    }
    
    private var remoteVideoWidth: UInt?
    private var currentMode: ShowMode?
    
    private var interruptInteractionReason: String?
    
    //TODO: remove
    private lazy var settingMenuVC: ShowToolMenuViewController = {
        let settingMenuVC = ShowToolMenuViewController()
        settingMenuVC.type = ShowMenuType.idle_audience
        settingMenuVC.delegate = self
        return settingMenuVC
    }()
    
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
    
    let channelOptions:AgoraRtcChannelMediaOptions = AgoraRtcChannelMediaOptions()
    
    // 音乐
    private lazy var musicPresenter: ShowMusicPresenter? = {
        return ShowMusicPresenter()
    }()
    
    private lazy var liveView: ShowRoomLiveView = {
        let view = ShowRoomLiveView(isBroadcastor: role == .broadcaster)
        view.delegate = self
        return view
    }()
    
    private lazy var beautyVC = ShowBeautySettingVC()
    private lazy var realTimeView: ShowRealTimeDataView = {
        let realTimeView = ShowRealTimeDataView(isLocal: role == .broadcaster)
        view.addSubview(realTimeView)
        realTimeView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(Screen.safeAreaTopHeight() + 50)
        }
        return realTimeView
    }()
    
    private lazy var applyAndInviteView = ShowApplyAndInviteView(roomId: roomId)
    private lazy var applyView = ShowApplyView(roomId: roomId)
    
    //PK popup list view
    private lazy var pkInviteView = ShowPKInviteView(roomId: roomId)
    
    private lazy var panelPresenter = ShowDataPanelPresenter()
    
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
            self.currentInteraction = interactionList?.first
        }
    }
    
    //pk invitation request map
    private var createPKInvitationMap: [String: ShowPKInvitation] = [String: ShowPKInvitation]()
    
    //get current interaction status
    private var interactionStatus: ShowInteractionStatus {
        return currentInteraction?.interactStatus ?? .idle
    }
    
    private var seatInteraction: ShowInteractionInfo? {
        get {
            if currentInteraction?.interactStatus == .onSeat {
                return currentInteraction
            }
            return nil
        }
    }
    
    private var currentInteraction: ShowInteractionInfo? {
        didSet {
            //update audio status
            if let interaction = currentInteraction {
                liveView.canvasView.isLocalMuteMic = interaction.ownerMuteAudio
                liveView.canvasView.isRemoteMuteMic = interaction.muteAudio
                
                if role == .broadcaster {
                    self.muteLocalAudio = interaction.ownerMuteAudio
                } else if interaction.userId == VLUserCenter.user.id {
                    self.muteLocalAudio = interaction.muteAudio
                }
            } else if role == .broadcaster {
                //unmute if interaction did stop
                self.muteLocalAudio = false
            }
            
            //update menu
            if role == .broadcaster {
                applyAndInviteView.seatMicModel = seatInteraction
                applyView.interactionModel = nil
            } else {
                if currentInteraction?.userId == VLUserCenter.user.id {
                    applyView.interactionModel = seatInteraction
                } else {
                    applyView.interactionModel = nil
                }
                applyAndInviteView.seatMicModel = nil
            }
            
            //stop or start interaction
            if currentInteraction == oldValue {
                return
            }
            
            var toastTitle = ""
            if let info = oldValue {
                _onStopInteraction(interaction: info)
                toastTitle = info.interactStatus.toastTitle
            }
            if let info = currentInteraction {
                _onStartInteraction(interaction: info)
                return
            }
            
            ToastView.show(text: interruptInteractionReason ?? toastTitle)
            interruptInteractionReason = nil
        }
    }
    
    private var muteLocalAudio: Bool = false {
        didSet {
            let options = self.channelOptions
            options.publishMicrophoneTrack = !muteLocalAudio
            ShowAgoraKitManager.shared.updateChannelEx(channelId: self.room?.roomId ?? "", options: options)
        }
    }
    
    private var muteLocalVideo: Bool = false {
        didSet {
            let options = self.channelOptions
            options.publishCameraTrack = !muteLocalVideo
            ShowAgoraKitManager.shared.updateChannelEx(channelId: self.room?.roomId ?? "", options: options)
        }
    }
    
    deinit {
        let roomId = room?.roomId ?? ""
        showLogger.info("deinit-- ShowLiveViewController \(roomId)")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setupUI()
        defaultConfig()
        guard let room = room else {return}
        if room.ownerId == VLUserCenter.user.id {// 自己的房间
            self.joinChannel()
            self._subscribeServiceEvent()
            AgoraEntAuthorizedManager.checkMediaAuthorized(parent: self)
        } else { // 自己是观众
            self.joinChannel(needUpdateCavans: self.loadingType == .joined)
            AppContext.showServiceImp(room.roomId).joinRoom(room: room) {[weak self] error, detailModel in
                guard let self = self else { return }
                showLogger.info("joinRoom[\(room.roomId)] error: \(error?.code ?? 0)")
                if let err = error {
                    if err.code == -1 {
                        self.onRoomExpired()
                    } else {
                        ToastView.show(text: err.localizedDescription)
                    }
                    return
                }
                self._subscribeServiceEvent()
            }
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        UIApplication.shared.isIdleTimerDisabled = true
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        UIApplication.shared.isIdleTimerDisabled = false
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        .lightContent
    }
    
    private func defaultConfig(){
        if AppContext.shared.isDebugMode {
            guard let room = room else {return}
            if room.ownerId != VLUserCenter.user.id {
                ShowAgoraKitManager.shared.debugDefaultAudienceSetting()
            }
        }
    }
    
    private func setupUI(){
        view.layer.contents = UIImage.show_sceneImage(name: "show_live_pkbg")?.cgImage
        navigationController?.isNavigationBarHidden = true
        liveView.room = room
//        if role == .audience {
//            liveView.roomUserCount += 1
//        }
        view.addSubview(liveView)
        liveView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    func leaveRoom(){
        ShowAgoraKitManager.shared.removeRtcDelegate(delegate: self, roomId: roomId)
        ShowAgoraKitManager.shared.cleanCapture()
        ShowAgoraKitManager.shared.leaveChannelEx(roomId: roomId, channelId: roomId)
        AppContext.showServiceImp(roomId).unsubscribeEvent(delegate: self)
        
        AppContext.showServiceImp(roomId).leaveRoom {_ in
        }
        if role == .broadcaster {
            BeautyManager.shareManager.destroy()
        }
    }
    
    private func joinChannel(needUpdateCavans: Bool = true) {
        ShowAgoraKitManager.shared.addRtcDelegate(delegate: self, roomId: roomId)
        guard let channelId = room?.roomId, let ownerId = room?.ownerId,  let uid: UInt = UInt(ownerId) else {
            return
        }
        currentChannelId = channelId
        if needUpdateCavans {
            if self.role == .audience {
                ShowAgoraKitManager.shared.setupRemoteVideo(channelId: channelId,
                                                            uid: uid,
                                                            canvasView: self.liveView.canvasView.localView)
            } else {
                ShowAgoraKitManager.shared.setupLocalVideo(uid: uid, canvasView: self.liveView.canvasView.localView)
            }
        }
        ShowAgoraKitManager.shared.joinChannelEx(currentChannelId: channelId,
                                                 targetChannelId: channelId,
                                                 ownerId: uid,
                                                 options: self.channelOptions,
                                                 role: role) {
        }
        
        liveView.canvasView.setLocalUserInfo(name: room?.ownerName ?? "", img: room?.ownerAvatar ?? "")
        
        self.muteLocalVideo = false
        self.muteLocalAudio = false
    }
    
    private func sendMessageWithText(_ text: String) {
        let showMsg = ShowMessage()
        showMsg.userId = VLUserCenter.user.id
        showMsg.userName = VLUserCenter.user.name
        showMsg.message = text
        showMsg.createAt = Date().millionsecondSince1970()
        
        AppContext.showServiceImp(roomId).sendChatMessage(message: showMsg) { error in
//            showLogger.info("发送消息状态 \(error?.localizedDescription ?? "") text = \(text)")
        }
    }
}

//MARK: private
extension ShowLiveViewController {
    private func _updateApplyMenu() {
        if role == .broadcaster {
            applyAndInviteView.reloadData()
            AppContext.showServiceImp(roomId).getAllMicSeatApplyList {[weak self] _, list in
                guard let list = list?.filterDuplicates({ $0.userId }) else { return }
                self?.liveView.bottomBar.linkButton.isShowRedDot = list.count > 0
            }
        } else {
            applyView.getAllMicSeatList(autoApply: false)
        }
    }
    

    func updateLoadingType(playState: RoomStatus) {
        ShowAgoraKitManager.shared.updateLoadingType(roomId: roomId, channelId: roomId, playState: playState)
        if let targetRoomId = currentInteraction?.roomId, targetRoomId != roomId {
            ShowAgoraKitManager.shared.updateLoadingType(roomId: roomId, channelId: targetRoomId, playState: playState)
        }
        if playState == .joined {
            AppContext.showServiceImp(roomId).initRoom { error in
                
            }
            sendMessageWithText("join_live_room".show_localized)
            updateRemoteCavans()
        } else if playState == .prejoined {
            AppContext.showServiceImp(roomId).deinitRoom { error in
                
            }
            sendMessageWithText("leave_live_room".show_localized)
            updateRemoteCavans()
        } else {
            leaveRoom()
        }
    }
    
    func updateRemoteCavans() {
        guard role == .audience, loadingType == .joined else { return }
        let uid: UInt = UInt(room?.ownerId ?? "0") ?? 0
        ShowAgoraKitManager.shared.setupRemoteVideo(channelId: roomId,
                                                    uid: uid,
                                                    canvasView: liveView.canvasView.localView)
        if let targetRoomId = currentInteraction?.roomId, targetRoomId != roomId {
            let uid = UInt(currentInteraction?.userId ?? "")!
            ShowAgoraKitManager.shared.setupRemoteVideo(channelId: targetRoomId,
                                                        uid: uid,
                                                        canvasView: liveView.canvasView.remoteView)
        }
    }
}

//MARK: service subscribe
extension ShowLiveViewController: ShowSubscribeServiceProtocol {
    private func _subscribeServiceEvent() {
        let service = AppContext.showServiceImp(roomId)
        
        service.subscribeEvent(delegate: self)
        
        //TODO: migration
        applyAndInviteView.applyStatusClosure = { [weak self] status in
            self?.liveView.canvasView.canvasType = status == .onSeat ? .joint_broadcasting : .none
        }
        
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    private func _refreshPKUserList() {
        AppContext.showServiceImp(roomId).getAllPKUserList { [weak self] (error, pkUserList) in
            self?.pkUserInvitationList = pkUserList
        }
    }
    
    private func _refreshInteractionList() {
        AppContext.showServiceImp(roomId).getAllInterationList { [weak self] (error, interactionList) in
            guard let self = self, error == nil else { return }
            if self.interactionList == nil, let interaction = interactionList?.first {
                // first load
                if self.role == .broadcaster {
                    AppContext.showServiceImp(self.roomId).stopInteraction(interaction: interaction) { err in
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
//            ToastView.show(text: "net work error: \(state)")
            return
        }
        
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    func onRoomExpired() {
        leaveRoom()
        
        let finishView = ShowReceiveFinishView()
        finishView.headImg = room?.ownerAvatar ?? ""
        finishView.headName = room?.ownerName ?? ""
        finishView.delegate = self
        self.view.addSubview(finishView)
        finishView.snp.makeConstraints { make in
            make.left.right.top.bottom.equalToSuperview()
        }
    }
    
    func onUserCountChanged(userCount: Int) {
        self.liveView.roomUserCount = userCount
    }
    
    func onUserJoinedRoom(user: ShowUser) {
        
    }
    
    func onUserLeftRoom(user: ShowUser) {
        if user.userId == room?.ownerId {
            showLogger.info(" finishAlertVC onUserLeftRoom : roomid = \(roomId)")
            onRoomExpired()
        }
    }
    
    func onMessageDidAdded(message: ShowMessage) {
        if let text = message.message {
            let model = ShowChatModel(userName: message.userName ?? "", text: text)
            self.liveView.addChatModel(model)
        }
    }
    
    func onMicSeatApplyUpdated(apply: ShowMicSeatApply) {
        _updateApplyMenu()
        if apply.status == .waitting, role == .broadcaster {
            liveView.bottomBar.linkButton.isShowRedDot = true
        }
        guard apply.userId == VLUserCenter.user.id else { return }
        //TODO: migration to interaction did start
        if apply.status == .accepted {
            liveView.canvasView.canvasType = .joint_broadcasting
            liveView.canvasView.setRemoteUserInfo(name: apply.userName ?? "", img: apply.avatar)
            liveView.bottomBar.linkButton.isSelected = true
            liveView.bottomBar.linkButton.isShowRedDot = false
        } else if apply.status == .rejected {
            applyView.getAllMicSeatList(autoApply: false)
            liveView.bottomBar.linkButton.isShowRedDot = false
            liveView.bottomBar.linkButton.isSelected = false
            
        } else {
//            liveView.canvasView.canvasType = .none
            liveView.bottomBar.linkButton.isSelected = false
        }
    }
    
    func onMicSeatApplyDeleted(apply: ShowMicSeatApply) {
        _updateApplyMenu()
    }
    
    func onMicSeatApplyAccepted(apply: ShowMicSeatApply) {
        _updateApplyMenu()
    }
    
    func onMicSeatApplyRejected(apply: ShowMicSeatApply) {
        _updateApplyMenu()
    }
    
    func onMicSeatInvitationUpdated(invitation: ShowMicSeatInvitation) {
        guard invitation.userId == VLUserCenter.user.id else { return }
        if invitation.status == .waitting {
            let imp = AppContext.showServiceImp(roomId)
            ShowReceivePKAlertVC.present(name: invitation.userName, style: .mic) { result in
                switch result {
                case .accept:
                    ToastView.showWait(text: "show_is_onseat_doing".show_localized)
                    // 解决多人同时点击同意连麦导致的问题, 正常项目应该由后台处理
                    DispatchQueue.global().asyncAfter(deadline: .now() + Double.random(in: 0.1...2.0)) {
                        imp.getAllInterationList { _, list in
                            ToastView.hidden()
                            guard let list = list?.filterDuplicates({ $0.userId }) else { return }
                            let isLink = !list.filter({ $0.interactStatus == .onSeat }).isEmpty
                            if isLink {
                                imp.rejectMicSeatInvitation { _ in }
                                ToastView.show(text: "show_broadcastor_is_onseat".show_localized)
                                return
                            }
                            imp.acceptMicSeatInvitation { error in }
                        }
                    }

                default:
                    imp.rejectMicSeatInvitation { error in
                    }
                    break
                }
            }
        }
    }
    
    func onMicSeatInvitationDeleted(invitation: ShowMicSeatInvitation) {
        guard "\(roomOwnerId)" == invitation.userId else { return }
//        ToastView.show(text: "seat invitation \(invitation.userName ?? "") did reject")
    }

    func onMicSeatInvitationAccepted(invitation: ShowMicSeatInvitation) {
        liveView.canvasView.setRemoteUserInfo(name: invitation.userName ?? "", img: invitation.avatar)
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
        if invitation.status == .ended, invitation.userId == VLUserCenter.user.id {
            ToastView.show(text: "PK已断开哦".show_localized)
        }
        if invitation.fromRoomId == room?.roomId {
            //send invitation
            createPKInvitationMap[invitation.roomId] = invitation
            pkInviteView.createPKInvitationMap = createPKInvitationMap
            
            return
        }
        
        //recv invitation
        if invitation.status == .waitting {
            let imp = AppContext.showServiceImp(roomId)
            ShowReceivePKAlertVC.present(name: invitation.fromName) { result in
                switch result {
                case .accept:
                    imp.acceptPKInvitation { error in
                        
                    }
                    break
                default:
                    imp.rejectPKInvitation { error in
                        
                    }
                    break
                }
            }
        }
    }
    
    func onPKInvitationAccepted(invitation: ShowPKInvitation) {
        //nothing todo, see onInteractionBegan
        guard  invitation.fromUserId == VLUserCenter.user.id else { return }
        
        if invitation.fromRoomId == room?.roomId {
            //send invitation
            createPKInvitationMap[invitation.roomId] = invitation
            pkInviteView.createPKInvitationMap = createPKInvitationMap
            
            return
        }
        
        //recv invitation
//        ToastView.show(text: "pk invitation \(invitation.roomId ?? "") did accept")
        _refreshPKUserList()
    }
    
    func onPKInvitationRejected(invitation: ShowPKInvitation) {
        guard  invitation.fromUserId == VLUserCenter.user.id else { return }
        
        if invitation.fromRoomId == room?.roomId {
            //send invitation
            createPKInvitationMap[invitation.roomId] = nil
            pkInviteView.createPKInvitationMap = createPKInvitationMap
            return
        }
        
        //recv invitation
//        ToastView.show(text: "pk invitation \(invitation.roomId ?? "") did reject")
        //TODO:
        _refreshPKUserList()
    }
    
    func onInterationUpdated(interaction: ShowInteractionInfo) {
        guard let index = interactionList?.firstIndex(where: { $0.objectId == interaction.objectId}) else {
            return
        }
        var list = interactionList
        list?.remove(at: index)
        list?.insert(interaction, at: index)
        interactionList = list
    }
    
    func onInteractionBegan(interaction: ShowInteractionInfo) {
        self.currentInteraction = interaction
        //TODO:
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    func onInterationEnded(interaction: ShowInteractionInfo) {
        self.currentInteraction = nil
        //TODO:
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    private func _onStartInteraction(interaction: ShowInteractionInfo) {
        switch interaction.interactStatus {
        case .pking:
            self.muteLocalVideo = false
            self.muteLocalAudio = false
            let interactionRoomId = interaction.roomId
            if interactionRoomId.isEmpty { return }
            if roomId != interaction.roomId {
                let uid = UInt(interaction.userId)!
                ShowAgoraKitManager.shared.updateVideoProfileForMode(.pk)
                currentChannelId = roomId
                ShowAgoraKitManager.shared.joinChannelEx(currentChannelId: roomId,
                                              targetChannelId: interactionRoomId,
                                              ownerId: uid,
                                              options: self.channelOptions,
                                                         role: .audience) {
                    showLogger.info("\(self.roomId) updateLoadingType _onStartInteraction---------- \(self.roomId)")
                    if self.role == .broadcaster {
                        ShowAgoraKitManager.shared.setupRemoteVideo(channelId: interactionRoomId,
                                                              uid: uid,
                                                              canvasView: self.liveView.canvasView.remoteView)
                    }else{
                        self.updateLoadingType(playState: self.loadingType)
                    }
                }
                liveView.canvasView.canvasType = .pk
                liveView.canvasView.setRemoteUserInfo(name: interaction.userName ?? "")
            }
        case .onSeat:
            self.muteLocalVideo = false
            self.muteLocalAudio = false
            liveView.canvasView.canvasType = .joint_broadcasting
            liveView.canvasView.setRemoteUserInfo(name: interaction.userName ?? "")
            if role == .audience {
                ShowAgoraKitManager.shared.updateAudienceProfile()
            }
            let toRole: AgoraClientRole = (role == .broadcaster || interaction.userId == VLUserCenter.user.id) ? .broadcaster : .audience
            ShowAgoraKitManager.shared.switchRole(role: toRole,
                                       channelId: roomId,
                                       options: self.channelOptions,
                                       uid: interaction.userId,
                                       canvasView: liveView.canvasView.remoteView)
            liveView.bottomBar.linkButton.isSelected = true
            liveView.bottomBar.linkButton.isShowRedDot = false
            AlertManager.hiddenView()
            if toRole == .broadcaster {
                self.delegate?.currentUserIsOnSeat()
                // 创建默认美颜效果
                ShowBeautyFaceVC.beautyData.forEach({
                    BeautyManager.shareManager.setBeauty(path: $0.path,
                                                             key: $0.key,
                                                             value: $0.value)
                })
            }
        default:
            break
        }
    }
    
    private func _onStopInteraction(interaction: ShowInteractionInfo) {
        switch interaction.interactStatus {
        case .pking:
            self.muteLocalVideo = false
            self.muteLocalAudio = false
            ShowAgoraKitManager.shared.updateVideoProfileForMode(.single)
            ShowAgoraKitManager.shared.leaveChannelEx(roomId: self.roomId, channelId: interaction.roomId)
            liveView.canvasView.canvasType = .none
            liveView.canvasView.setRemoteUserInfo(name: interaction.userName ?? "")
        case .onSeat:
            self.muteLocalVideo = false
            self.muteLocalAudio = false
            ShowAgoraKitManager.shared.updateVideoProfileForMode(.single)
            liveView.canvasView.setRemoteUserInfo(name: interaction.userName ?? "")
            liveView.canvasView.canvasType = .none
            liveView.bottomBar.linkButton.isShowRedDot = false
            liveView.bottomBar.linkButton.isSelected = false
            currentInteraction?.ownerMuteAudio = false
            let canvasView = role == .broadcaster ? nil : UIView()
            let uid = role == .broadcaster ? VLUserCenter.user.id : interaction.userId
            ShowAgoraKitManager.shared.switchRole(role: role,
                                       channelId: room?.roomId ?? "",
                                       options: self.channelOptions,
                                       uid: uid,
                                       canvasView: canvasView)
            self.delegate?.currentUserIsOffSeat()
            
        default:
            break
        }
    }
}


extension ShowLiveViewController: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
        showLogger.warning("rtcEngine warningCode == \(warningCode.rawValue)")
    }
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        showLogger.warning("rtcEngine errorCode == \(errorCode.rawValue)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        showLogger.info("rtcEngine didJoinChannel \(channel) with uid \(uid) elapsed \(elapsed)ms")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        showLogger.info("rtcEngine didJoinedOfUid \(uid) channelId: \(roomId)", context: kShowLogBaseContext)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        showLogger.info("rtcEngine didOfflineOfUid === \(uid)")
        if let interaction = self.currentInteraction {
            let isRoomOwner: Bool = role == .broadcaster
            let isInteractionLeave: Bool = interaction.userId == "\(uid)"
            let roomOwnerExit: Bool = room?.ownerId ?? "" == "\(uid)"
            if roomOwnerExit {
                //room owner exit
                AppContext.showServiceImp(roomId).stopInteraction(interaction: interaction) { err in
                }
            } else if isRoomOwner, isInteractionLeave {
                //room owner found interaction(pk/onseat) user offline
                AppContext.showServiceImp(roomId).stopInteraction(interaction: interaction) { err in
                }
            }
        }
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, reportRtcStats stats: AgoraChannelStats) {
        panelPresenter.updateChannelStats(stats)
        throttleRefreshRealTimeInfo()
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
        panelPresenter.updateLocalAudioStats(stats)
        throttleRefreshRealTimeInfo()
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, localVideoStats stats: AgoraRtcLocalVideoStats, sourceType: AgoraVideoSourceType) {
        panelPresenter.updateLocalVideoStats(stats)
        throttleRefreshRealTimeInfo()
        showLogger.info("localVideoStats  width = \(stats.encodedFrameWidth), height = \(stats.encodedFrameHeight)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
        panelPresenter.updateVideoStats(stats)
        if let ts = ShowAgoraKitManager.shared.callTimestampEnd() {
            panelPresenter.updateTimestamp(ts)
        }
        throttleRefreshRealTimeInfo()
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteAudioStats stats: AgoraRtcRemoteAudioStats) {
        panelPresenter.updateAudioStats(stats)
        throttleRefreshRealTimeInfo()
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, uplinkNetworkInfoUpdate networkInfo: AgoraUplinkNetworkInfo) {
        panelPresenter.updateUplinkNetworkInfo(networkInfo)
        throttleRefreshRealTimeInfo()
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, downlinkNetworkInfoUpdate networkInfo: AgoraDownlinkNetworkInfo) {
        panelPresenter.updateDownlinkNetworkInfo(networkInfo)
        throttleRefreshRealTimeInfo()
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, contentInspectResult result: AgoraContentInspectResult) {
        showLogger.warning("contentInspectResult: \(result.rawValue)")
        guard result != .neutral else { return }
        ToastView.show(text: "监测到当前内容存在违规行为")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, videoSizeChangedOf sourceType: AgoraVideoSourceType, uid: UInt, size: CGSize, rotation: Int) {

    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   remoteVideoStateChangedOfUid uid: UInt,
                   state: AgoraVideoRemoteState,
                   reason: AgoraVideoRemoteReason,
                   elapsed: Int) {
        DispatchQueue.main.async {
            let channelId = self.room?.roomId ?? ""
            showLogger.info("didLiveRtcRemoteVideoStateChanged channelId: \(channelId) uid: \(uid) state: \(state.rawValue) reason: \(reason.rawValue)",
                            context: kShowLogBaseContext)
            if state == .decoding /*2*/,
               ( reason == .remoteUnmuted /*6*/ || reason == .localUnmuted /*4*/ || reason == .localMuted /*3*/ )   {
                showLogger.info("show first frame (\(channelId))", context: kShowLogBaseContext)
                if let ts = ShowAgoraKitManager.shared.callTimestampEnd() {
                    self.panelPresenter.updateTimestamp(ts)
                    self.throttleRefreshRealTimeInfo()
                }
            }
//            if self.loadingType != .joined {
//                self.updateLoadingType(playState: self.loadingType)
//            }
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstLocalVideoFramePublishedWithElapsed elapsed: Int, sourceType: AgoraVideoSourceType) {
        showLogger.info("firstLocalVideoFramePublishedWithElapsed: \(elapsed)ms \(sourceType.rawValue)",
                        context: kShowLogBaseContext)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, tokenPrivilegeWillExpire token: String) {
        showLogger.warning("tokenPrivilegeWillExpire: \(roomId)",
                           context: kShowLogBaseContext)
        if let channelId = currentChannelId {
            ShowAgoraKitManager.shared.renewToken(channelId: channelId)
        }
    }
}


extension ShowLiveViewController: ShowRoomLiveViewDelegate {
    func onPKDidTimeout() {
        guard let info = currentInteraction else { return }
        AppContext.showServiceImp(roomId).stopInteraction(interaction: info) { _ in
        }
        
        interruptInteractionReason = "show_pk_end_timeout".show_localized
    }
    
    func onClickRemoteCanvas() {
        guard let info = currentInteraction else { return }
        if role == .audience, info.userId != VLUserCenter.user.id {
            return
        }
        let menuVC = ShowToolMenuViewController()
        menuVC.type = ShowMenuType.managerMic
        menuVC.selectedMap = [.mute_mic: info.muteAudio]
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
                self?.dismiss(animated: true)
            }
        }else {
            updateLoadingType(playState: .idle)
            dismiss(animated: true)
        }
    }
    
    func onClickMoreButton() {
        let dialog = AUiMoreDialog(frame: view.bounds)
        view.addSubview(dialog)
        dialog.show()
    }
    
    func onClickPKButton(_ button: ShowRedDotButton) {
        AlertManager.show(view: pkInviteView, alertPostion: .bottom)
        _refreshPKUserList()
    }
    
    func onClickLinkButton(_ button: ShowRedDotButton) {
        if role == .broadcaster {
            applyAndInviteView.reloadData()
            AlertManager.show(view: applyAndInviteView, alertPostion: .bottom)
            
        } else {
            AgoraEntAuthorizedManager.checkMediaAuthorized(parent: self) { granted in
                guard granted else { return }
                self.applyView.getAllMicSeatList(autoApply: self.role == .audience)
                AlertManager.show(view: self.applyView, alertPostion: .bottom)
            }
        }
    }
    
    func onClickBeautyButton() {
        present(beautyVC, animated: true)
    }
    
    func onClickMusicButton() {
        let vc = ShowMusicEffectVC()
        vc.musicManager = musicPresenter
        vc.currentChannelId = currentChannelId
        present(vc, animated: true)
    }
    
    func onClickSettingButton() {
        var muteAudio: Bool = self.muteLocalAudio
        if let info = currentInteraction, info.userId == VLUserCenter.user.id {
            muteAudio = info.muteAudio
        }
        settingMenuVC.selectedMap = [.camera: self.muteLocalVideo, .mic: muteAudio, .mute_mic: muteAudio]
        
        if interactionStatus == .idle {
            settingMenuVC.type = role == .broadcaster ? .idle_broadcaster : .idle_audience
        }else{
            settingMenuVC.type = role == .broadcaster ? .pking : (currentInteraction?.userId == VLUserCenter.user.id ? .pking : .idle_audience)
            settingMenuVC.menuTitle = "show_setting_menu_on_pk_title".show_localized
        }
        present(settingMenuVC, animated: true)
    }
    
}

extension ShowLiveViewController {
    
    private func throttleRefreshRealTimeInfo() {
        ShowThrottler.throttle(delay: .seconds(1)) { [weak self] in
            guard let `self` = self else {
                return
            }
            DispatchQueue.main.async {
                var receive = true
                var send = true
                if self.role == .broadcaster && self.interactionStatus != .pking && self.interactionStatus != .onSeat {
                    receive = false
                }
                if self.role == .audience && self.interactionStatus != .pking && self.interactionStatus != .onSeat {
                    send = false
                }
                let data = self.panelPresenter.generatePanelData(audioOnly: false, send: send, receive: receive)
                self.realTimeView.update(left: data.left, right: data.right)
            }
        }
    }
}


extension ShowLiveViewController {
    private func showError(title: String, errMsg: String) {
        showAlert(title: title, message: errMsg) { [weak self] in
            self?.leaveRoom()
            self?.dismiss(animated: true)
        }
    }
}

extension ShowLiveViewController: ShowToolMenuViewControllerDelegate {
    
    // 开关摄像头
    func onClickCameraButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        AgoraEntAuthorizedManager.checkCameraAuthorized(parent: self) { granted in
            guard granted else { return }
            self.muteLocalVideo = selected
            if selected {
                ShowAgoraKitManager.shared.engine?.stopPreview()
            } else {
                ShowAgoraKitManager.shared.engine?.startPreview()
            }
        }
    }
    
    // 结束连麦
    func onClickEndPkButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        guard let info = currentInteraction else { return }
        AppContext.showServiceImp(roomId).stopInteraction(interaction: info) { _ in
        }
    }
    
    // 麦克风开关
    func onClickMicButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self) { granted in
            guard granted else { return }
            let uid = menu.type == .managerMic ? self.currentInteraction?.userId ?? "" : VLUserCenter.user.id
            AppContext.showServiceImp(self.roomId).muteAudio(mute: selected, userId: uid) { err in
            }
            self.muteLocalAudio = selected
        }
    }
    
    // 静音
    func onClickMuteMicButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        let uid = menu.type == .managerMic ? currentInteraction?.userId ?? "" : VLUserCenter.user.id
        AppContext.showServiceImp(roomId).muteAudio(mute: selected, userId: uid) { err in
        }
        self.muteLocalAudio = selected
    }
    
    func onClickRealTimeDataButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        view.addSubview(realTimeView)
        realTimeView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(Screen.safeAreaTopHeight() + 50)
        }
    }
    
    func onClickSwitchCameraButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        AgoraEntAuthorizedManager.checkCameraAuthorized(parent: self) { granted in
            guard granted else { return }
            ShowAgoraKitManager.shared.switchCamera(self.roomId)
        }
    }
    
    func onClickSettingButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        settingMenuVC.dismiss(animated: true) {[weak self] in
            guard let wSelf = self else { return }
            if AppContext.shared.isDebugMode {
                let vc = ShowDebugSettingVC()
                vc.isBroadcastor = wSelf.role == .broadcaster
                wSelf.navigationController?.pushViewController(vc, animated: true)
            }else {
                let vc = ShowAdvancedSettingVC()
                vc.mode = wSelf.interactionStatus == .pking ? .pk : .single // 根据当前模式设置
                vc.isBroadcaster = wSelf.role == .broadcaster
                vc.musicManager = wSelf.musicPresenter
                vc.currentChannelId = wSelf.currentChannelId
                wSelf.navigationController?.pushViewController(vc, animated: true)
            }
        }
    }
    
}
// MARK: - ShowReceiveFinishViewDelegate
extension ShowLiveViewController: ShowReceiveFinishViewDelegate {
    func onClickFinishButton() {
        onClickCloseButton()
    }
}
