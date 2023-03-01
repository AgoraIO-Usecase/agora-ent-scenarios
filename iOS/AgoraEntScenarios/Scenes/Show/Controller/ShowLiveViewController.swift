//
//  ShowLiveViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/7.
//

import UIKit
import AgoraRtcKit
import SwiftUI

protocol ShowLiveViewControllerDelegate: NSObjectProtocol {
    func currentUserIsOnSeat()
    func currentUserIsOffSeat()
}

class ShowLiveViewController: UIViewController {
    weak var delegate: ShowLiveViewControllerDelegate?
    var room: ShowRoomListModel?
    var loadingType: ShowRTCLoadingType = .preload {
        didSet {
            if loadingType == oldValue {
                return
            }
            
            self.joinStartDate = Date()
            updateLoadingType(loadingType: loadingType)
            remoteVideoWidth = nil
            currentMode = nil
        }
    }
    private var currentChannelId: String?
    
    private var joinStartDate: Date?
    
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
    
//    var selectedResolution = ShowSettingKey.captureVideoSize.intValue
    
    var audiencePresetType: ShowPresetType?
    
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
    
    var agoraKitManager: ShowAgoraKitManager!
    
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
    
    //TODO:
    let channelOptions:AgoraRtcChannelMediaOptions = AgoraRtcChannelMediaOptions()
    
    // 音乐
    private lazy var musicManager: ShowMusicManager? = {
         let agorakit = agoraKitManager.agoraKit
        return ShowMusicManager(agoraKit: agorakit)
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
//        return interactionList?.filter({ $0.interactStatus != .idle }).first?.interactStatus ?? .idle
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
                
//                let options = self.channelOptions
                if role == .broadcaster {
//                    options.publishMicrophoneTrack = !interaction.ownerMuteAudio
//                    agoraKitManager.agoraKit.updateChannel(with: options)
                    self.muteLocalAudio = interaction.ownerMuteAudio
                    
                } else if interaction.userId == VLUserCenter.user.id {
//                    options.publishMicrophoneTrack = !interaction.muteAudio
//                    agoraKitManager.agoraKit.updateChannel(with: options)
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
            agoraKitManager.updateChannelEx(channelId: self.room?.roomId ?? "", options: options)
        }
    }
    
    private var muteLocalVideo: Bool = false {
        didSet {
            let options = self.channelOptions
            options.publishCameraTrack = !muteLocalVideo
            agoraKitManager.updateChannelEx(channelId: self.room?.roomId ?? "", options: options)
        }
    }
    
    deinit {
        let roomId = room?.roomId ?? ""
        showLogger.info("deinit-- ShowLiveViewController \(roomId)")
    }
    
    init(agoraKitManager:ShowAgoraKitManager) {
        self.agoraKitManager = agoraKitManager
        super.init(nibName: nil, bundle: nil)
        showLogger.info("init-- ShowLiveViewController")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.layer.contents = UIImage.show_sceneImage(name: "show_live_pkbg")?.cgImage
        setupUI()
        defaultConfig()
        guard let room = room else {return}
        if room.ownerId == VLUserCenter.user.id {
            self.joinChannel()
            if self.loadingType == .loading {
                self.updateLoadingType(loadingType: self.loadingType)
            }
            self._subscribeServiceEvent()
            UIApplication.shared.isIdleTimerDisabled = true
        } else {
            AppContext.showServiceImp(room.roomId).joinRoom(room: room) {[weak self] error, detailModel in
                guard let self = self else { return }
                showLogger.info("joinRoom: roomid = \(room.roomId)")
                if let err = error {
    //                ToastView.show(text: error.localizedDescription)
                    showLogger.info(" finishAlertVC joinRoom : roomid = \(room.roomId), error = \(err) ")
//                    self.onRoomExpired()
                    self._ensureRoomIsExst(roomId: room.roomId)
                    return
                }
                self.joinChannel(needUpdateCavans: self.loadingType == .loading)
                if self.loadingType == .loading {
                    self.updateLoadingType(loadingType: self.loadingType)
                }
                self._subscribeServiceEvent()
                UIApplication.shared.isIdleTimerDisabled = true
            }
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
    }
    
    private func defaultConfig(){
        if AppContext.shared.isDebugMode {
            guard let room = room else {return}
            if room.ownerId != VLUserCenter.user.id {
                agoraKitManager.debugDefaultAudienceSetting()
            }
        }
    }
    
    private func setupUI(){
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
        agoraKitManager.setRtcDelegate(delegate: nil, roomId: roomId)
        agoraKitManager.cleanCapture()
        agoraKitManager.leaveChannelEx(roomId: roomId, channelId: roomId)
        AppContext.showServiceImp(roomId).unsubscribeEvent(delegate: self)
        
        AppContext.showServiceImp(roomId).leaveRoom {_ in
//            self?.dismiss(animated: true) {
//            }
        }
        if role == .broadcaster {
            BeautyManager.shareManager.destroy()
        }
    }
    
    private func joinChannel(needUpdateCavans: Bool = true) {
        agoraKitManager.setRtcDelegate(delegate: self, roomId: roomId)
//        agoraKitManager.defaultSetting()
        guard let channelId = room?.roomId, let ownerId = room?.ownerId else {
            return
        }
        currentChannelId = channelId
        self.joinStartDate = Date()
        let uid: UInt = UInt(ownerId)!
        agoraKitManager.joinChannelEx(currentChannelId: channelId,
                                      targetChannelId: channelId,
                                      ownerId: uid,
                                      options: self.channelOptions,
                                      role: role) { [weak self] in
            guard let self = self else { return }
            if needUpdateCavans {
                if self.role == .audience {
                    self.agoraKitManager.setupRemoteVideo(channelId: channelId,
                                                          uid: uid,
                                                          canvasView: self.liveView.canvasView.localView)
                } else {
                    self.agoraKitManager.setupLocalVideo(uid: uid, canvasView: self.liveView.canvasView.localView)
                }
            }
        }
        liveView.canvasView.setLocalUserInfo(name: VLUserCenter.user.name, img: VLUserCenter.user.headUrl)
        
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
        } else {
            applyView.getAllMicSeatList(autoApply: false)
        }
    }
    

    func updateLoadingType(loadingType: ShowRTCLoadingType) {
        agoraKitManager.updateLoadingType(roomId: roomId, channelId: roomId, loadingType: loadingType)
        if let targetRoomId = currentInteraction?.roomId, targetRoomId != roomId {
            agoraKitManager.updateLoadingType(roomId: roomId, channelId: targetRoomId, loadingType: loadingType)
        }
        if loadingType == .loading {
            AppContext.showServiceImp(roomId).initRoom { error in
                
            }
            sendMessageWithText("join_live_room".show_localized)
            
            //TODO: need to optimize
            updateVideoCavans()
        } else if loadingType == .preload {
            AppContext.showServiceImp(roomId).deinitRoom { error in
                
            }
            sendMessageWithText("leave_live_room".show_localized)
        } else {
            leaveRoom()
        }
    }
    
    func updateVideoCavans() {
        if role == .audience {
            let uid: UInt = UInt(room?.ownerId ?? "")!
            agoraKitManager.setupRemoteVideo(channelId: roomId,
                                                  uid: uid,
                                                  canvasView: liveView.canvasView.localView)
            if let targetRoomId = currentInteraction?.roomId, targetRoomId != roomId {
                let uid = UInt(currentInteraction?.userId ?? "")!
                agoraKitManager.setupRemoteVideo(channelId: targetRoomId,
                                                uid: uid,
                                                canvasView: liveView.canvasView.remoteView)
            }
            guard let audiencePresetType = audiencePresetType else { return }
            agoraKitManager.setDefaultSuperResolutionForAudienceType(presetType: audiencePresetType)
        }
    }
}

//MARK: service subscribe
extension ShowLiveViewController: ShowSubscribeServiceProtocol {
    
    private func _joinRoom() {
        self.joinChannel(needUpdateCavans: self.loadingType == .loading)
        if self.loadingType == .loading {
            self.updateLoadingType(loadingType: self.loadingType)
        }
        self._subscribeServiceEvent()
        UIApplication.shared.isIdleTimerDisabled = true
    }
    
    private func _ensureRoomIsExst(roomId: String) {
        print("finishAlertVC joinRoom : roomid = \(roomId) ,_ensureRoomIsExst , checking = \(checking) ")
        if checking { return }
        checking = true
        AppContext.showServiceImp("").getRoomList(page: 1) { [weak self] error, roomList in
            guard let self = self else { return }
            self.checking = false
            guard let list = roomList else {
                self.onRoomExpired()
                print("finishAlertVC joinRoom : roomid = \(roomId) ,_ensureRoomIsExst , roomlist is empty ")
                return
            }
            for item in list {
                let aRoomId = item.roomId
                if aRoomId == roomId {
                    self._joinRoom()
                    print("finishAlertVC joinRoom : roomid = \(roomId) _joinRoom 房间实时上存在")
                    return
                }
            }
            print("finishAlertVC joinRoom : roomid = \(roomId) _ensureRoomIsExst 房间确实不存在")
            self.onRoomExpired()
        }
    }
    
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
            ToastView.show(text: "net work error: \(state)")
            return
        }
        
        _refreshPKUserList()
        _refreshInteractionList()
    }
    
    func onRoomExpired() {
        leaveRoom()
        ShowReceiveLiveFinishAlertVC.show(topVC: self,
                                          ownerUrl: room?.ownerAvatar ?? "",
                                          ownerName: room?.ownerName ?? "") { [weak self] in
            if self?.presentedViewController != nil {
                self?.presentedViewController?.dismiss(animated: false)
            }
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
//            if apply.userId == VLUserCenter.user.id {
//                agoraKitManager.switchRole(role: .broadcaster,
//                                           uid: apply.userId,
//                                           canvasView: liveView.canvasView.remoteView)
//            }
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
                    ToastView.showWait(text: "连麦中...".show_localized)
                    // 解决多人同时点击同意连麦导致的问题, 正常项目应该由后台处理
                    DispatchQueue.global().asyncAfter(deadline: .now() + Double.random(in: 0.1...2.0)) {
                        imp.getAllInterationList { _, list in
                            ToastView.hidden()
                            guard let list = list?.filterDuplicates({ $0.userId }) else { return }
                            let isLink = !list.filter({ $0.interactStatus == .onSeat }).isEmpty
                            if isLink {
                                imp.rejectMicSeatInvitation { _ in }
                                ToastView.show(text: "主播已在连麦中, 暂时无法连麦".show_localized)
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
//        if invitation.status == .ended, invitation.userId == VLUserCenter.user.id {
//            ToastView.show(text: "连麦已断开哦".show_localized)
//        }
    }
    
    func onMicSeatInvitationDeleted(invitation: ShowMicSeatInvitation) {
        guard "\(roomOwnerId)" == invitation.userId else { return }
//        ToastView.show(text: "seat invitation \(invitation.userName ?? "") did reject")
    }

    func onMicSeatInvitationAccepted(invitation: ShowMicSeatInvitation) {
//        liveView.canvasView.canvasType = .joint_broadcasting
        liveView.canvasView.setRemoteUserInfo(name: invitation.userName ?? "", img: invitation.avatar)
//        ToastView.show(text: "seat invitation \(invitation.userId ?? "") did accept")
//        guard invitation.userId == VLUserCenter.user.id else { return }
//        agoraKitManager.switchRole(role: .broadcaster,
//                                   uid: invitation.userId,
//                                   canvasView: liveView.canvasView.remoteView)
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
                agoraKitManager.updateVideoProfileForMode(.pk)
                currentChannelId = roomId
                agoraKitManager.joinChannelEx(currentChannelId: roomId,
                                              targetChannelId: interactionRoomId,
                                              ownerId: uid,
                                              options: self.channelOptions,
                                              role: role) {
                    showLogger.info("\(self.roomId) updateLoadingType _onStartInteraction---------- \(self.roomId)")
                    if self.role == .broadcaster {
                        self.agoraKitManager.setupRemoteVideo(channelId: interactionRoomId,
                                                              uid: uid,
                                                              canvasView: self.liveView.canvasView.remoteView)
                    }else{
                        self.updateLoadingType(loadingType: self.loadingType)
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
            let role: AgoraClientRole = (role == .broadcaster || interaction.userId == VLUserCenter.user.id) ? .broadcaster : .audience
            if role == .broadcaster {
                agoraKitManager.updateVideoProfileForMode(.pk)
            }
            agoraKitManager.switchRole(role: role,
                                       channelId: roomId,
                                       options: self.channelOptions,
                                       uid: interaction.userId,
                                       canvasView: liveView.canvasView.remoteView)
            liveView.bottomBar.linkButton.isSelected = true
            liveView.bottomBar.linkButton.isShowRedDot = false
            AlertManager.hiddenView()
            if role == .broadcaster {
                self.delegate?.currentUserIsOnSeat()
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
            agoraKitManager.updateVideoProfileForMode(.single)
            agoraKitManager.leaveChannelEx(roomId: self.roomId, channelId: interaction.roomId)
            liveView.canvasView.canvasType = .none
            liveView.canvasView.setRemoteUserInfo(name: interaction.userName ?? "")
//            if interaction.userId == VLUserCenter.user.id {
//                let options = self.channelOptions
//                options.publishCameraTrack = false
//                options.publishMicrophoneTrack = false
//                options.clientRoleType = .audience
//                agoraKitManager.agoraKit.updateChannel(with: options)
//                self.muteLocalVideo = true
//            }
        case .onSeat:
            self.muteLocalVideo = false
            self.muteLocalAudio = false
            agoraKitManager.updateVideoProfileForMode(.single)
            liveView.canvasView.setRemoteUserInfo(name: interaction.userName ?? "")
            liveView.canvasView.canvasType = .none
            liveView.bottomBar.linkButton.isShowRedDot = false
            liveView.bottomBar.linkButton.isSelected = false
            currentInteraction?.ownerMuteAudio = false
            let canvasView = role == .broadcaster ? nil : UIView()
            let uid = role == .broadcaster ? VLUserCenter.user.id : interaction.userId
            agoraKitManager.switchRole(role: role,
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
//        realTimeView.statsInfo?.updateChannelStats(stats)
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateChannelStats(stats)
            self?.realTimeView.receiveStatsInfo?.updateChannelStats(stats)
        }
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
//        realTimeView.statsInfo?.updateLocalAudioStats(stats)
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateLocalAudioStats(stats)
            self?.realTimeView.receiveStatsInfo?.updateLocalAudioStats(stats)
        }
        
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, localVideoStats stats: AgoraRtcLocalVideoStats, sourceType: AgoraVideoSourceType) {
//        realTimeView.statsInfo?.updateLocalVideoStats(stats)
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateLocalVideoStats(stats)
            self?.realTimeView.receiveStatsInfo?.updateLocalVideoStats(stats)
        }
        
        showLogger.info("localVideoStats  width = \(stats.encodedFrameWidth), height = \(stats.encodedFrameHeight)")

    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
//        realTimeView.statsInfo?.updateVideoStats(stats)
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateVideoStats(stats)
            self?.realTimeView.receiveStatsInfo?.updateVideoStats(stats)
        }
        

        showLogger.info("room.ownderid = \(String(describing: room?.ownerId.debugDescription)) width = \(stats.width), height = \(stats.height)")
        if let audiencePresetType = audiencePresetType {
            let mode: ShowMode = interactionStatus == .idle ? .single : .pk
            // 防止多次调用
            if mode != currentMode || stats.width != remoteVideoWidth {
                showLogger.info(" [\(Date())] ----- setSuperResolutionOn remoteVideoStats roomId = \(roomId) = \(String(describing: room?.ownerId.debugDescription)) width = \(stats.width), height = \(stats.height)")
                agoraKitManager.setSuperResolutionForAudienceType(presetType: audiencePresetType, videoWidth: Int(stats.width), mode: mode)
                currentMode = mode
                remoteVideoWidth = stats.width
                if stats.width >= 1080 && ShowSettingKey.SR.boolValue == true {
                    ToastView.show(text: "show_presetting_alert_will_change_sr_value_message".show_localized)
                }
            }
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteAudioStats stats: AgoraRtcRemoteAudioStats) {
//        realTimeView.statsInfo?.updateAudioStats(stats)
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateAudioStats(stats)
            self?.realTimeView.receiveStatsInfo?.updateAudioStats(stats)
        }
        
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, uplinkNetworkInfoUpdate networkInfo: AgoraUplinkNetworkInfo) {
//        realTimeView.statsInfo?.updateUplinkNetworkInfo(networkInfo)
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateUplinkNetworkInfo(networkInfo)
            self?.realTimeView.receiveStatsInfo?.updateUplinkNetworkInfo(networkInfo)
        }
        
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, downlinkNetworkInfoUpdate networkInfo: AgoraDownlinkNetworkInfo) {
//        realTimeView.statsInfo?.updateDownlinkNetworkInfo(networkInfo)
        delayRefreshRealTimeInfo { [weak self] in
            self?.realTimeView.sendStatsInfo?.updateDownlinkNetworkInfo(networkInfo)
            self?.realTimeView.receiveStatsInfo?.updateDownlinkNetworkInfo(networkInfo)
        }
        
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, contentInspectResult result: AgoraContentInspectResult) {
        showLogger.warning("contentInspectResult: \(result.rawValue)")
        guard result == .porn else { return }
        ToastView.show(text: "监测到当前内容存在违规行为")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, videoSizeChangedOf sourceType: AgoraVideoSourceType, uid: UInt, size: CGSize, rotation: Int) {
        showLogger.info(" [\(Date())] ----- setSuperResolutionOn videoSizeChangedOf roomId = \(roomId) = \(String(describing: room?.ownerId.debugDescription)) width = \(size.width), height = \(size.height), sourceType = \(sourceType.rawValue)")
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
                let costTs = -(self.joinStartDate?.timeIntervalSinceNow ?? 0) * 1000
                showLogger.info("show first frame (\(channelId)) cost: \(Int(costTs)) ms", context: kShowLogBaseContext)
            }
            if self.loadingType != .loading {
                self.updateLoadingType(loadingType: self.loadingType)
            }
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstLocalVideoFramePublishedWithElapsed elapsed: Int, sourceType: AgoraVideoSourceType) {
        showLogger.info("firstLocalVideoFramePublishedWithElapsed: \(elapsed)ms \(sourceType.rawValue)",
                        context: kShowLogBaseContext)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, tokenPrivilegeWillExpire token: String) {
        showLogger.warning("tokenPrivilegeWillExpire: \(roomId)",
                           context: kShowLogBaseContext)
        agoraKitManager.renewToken(origToken: token)
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
            leaveRoom()
            dismiss(animated: true)
        }
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
            applyView.getAllMicSeatList(autoApply: role == .audience)
            AlertManager.show(view: applyView, alertPostion: .bottom)
        }
    }
    
    func onClickBeautyButton() {
        present(beautyVC, animated: true)
    }
    
    func onClickMusicButton() {
        let vc = ShowMusicEffectVC()
        vc.musicManager = musicManager
        vc.agorakitManager = agoraKitManager
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
    
    private func resetRealTimeIfNeeded() {
        if role == .broadcaster && interactionStatus != .pking && interactionStatus != .onSeat {
            realTimeView.cleanRemoteDescription()
        }
        if role == .audience && interactionStatus != .pking && interactionStatus != .onSeat {
            realTimeView.cleanLocalDescription()
        }
    }
    
    private func delayRefreshRealTimeInfo(_ task: (()->())?) {
        ShowThrottler.throttle(delay: .seconds(1)) { [weak self] in
            DispatchQueue.main.async {
                task?()
                self?.resetRealTimeIfNeeded()
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
//        let option = self.channelOptions
//        option.publishCameraTrack = !selected
//        agoraKitManager.agoraKit.updateChannel(with: option)
        self.muteLocalVideo = selected
        if selected {
            agoraKitManager.agoraKit.stopPreview()
        } else {
            agoraKitManager.agoraKit.startPreview()
        }
    }
    
    // 画质
    func onClickHDButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        settingMenuVC.dismiss(animated: false)
        
        let vc = ShowSelectQualityVC()
//        vc.defalutSelectIndex = selectedResolution
        present(vc, animated: false)
        vc.dismissed = { [weak self] in
            guard let wSelf = self else { return }
            wSelf.present(wSelf.settingMenuVC, animated: false)
        }
        vc.selectedItem = {[weak self] resolution,index in
            guard let wSelf = self else { return }
//            wSelf.selectedResolution = index
//            wSelf.agoraKitManager.setCaptureVideoDimensions(CGSize(width: resolution.width, height: resolution.height))
            wSelf.agoraKitManager.selectCaptureVideoDimensions(index: index)
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
//        let options = self.channelOptions
//        options.publishMicrophoneTrack = !selected
//        if role == .broadcaster {
//            agoraKitManager.agoraKit.updateChannel(with: options)
//        }
//        guard let info = currentInteraction else { return }
//        if info.userId == VLUserCenter.user.id {
//            agoraKitManager.agoraKit.updateChannel(with: options)
//        }
        let uid = menu.type == .managerMic ? currentInteraction?.userId ?? "" : VLUserCenter.user.id
        AppContext.showServiceImp(roomId).muteAudio(mute: selected, userId: uid) { err in
        }
        self.muteLocalAudio = selected
    }
    
    // 静音
    func onClickMuteMicButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        let uid = menu.type == .managerMic ? currentInteraction?.userId ?? "" : VLUserCenter.user.id
        AppContext.showServiceImp(roomId).muteAudio(mute: selected, userId: uid) { err in
        }
        
        self.muteLocalAudio = selected
    }
    
    func onClickRealTimeDataButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
//        AlertManager.show(view: realTimeView, alertPostion: .top)
        view.addSubview(realTimeView)
        realTimeView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(Screen.safeAreaTopHeight() + 50)
        }
    }
    
    func onClickSwitchCameraButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        agoraKitManager.switchCamera()
    }
    
    func onClickSettingButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool) {
        settingMenuVC.dismiss(animated: true) {[weak self] in
            guard let wSelf = self else { return }
            if AppContext.shared.isDebugMode {
                let vc = ShowDebugSettingVC()
                vc.isBroadcastor = wSelf.role == .broadcaster
                vc.settingManager = wSelf.agoraKitManager
                wSelf.navigationController?.pushViewController(vc, animated: true)
            }else {
                let vc = ShowAdvancedSettingVC()
                vc.isOutside = false
                vc.mode = wSelf.interactionStatus == .pking ? .pk : .single // 根据当前模式设置
                vc.isBroadcaster = wSelf.role == .broadcaster
                vc.settingManager = wSelf.agoraKitManager
                vc.audiencePresetType = wSelf.audiencePresetType
                vc.currentChannelId = wSelf.currentChannelId
                wSelf.navigationController?.pushViewController(vc, animated: true)
            }
        }
    }
    
}

