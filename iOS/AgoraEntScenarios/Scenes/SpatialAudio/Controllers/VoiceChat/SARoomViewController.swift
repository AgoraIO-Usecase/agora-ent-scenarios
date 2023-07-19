//
//  VoiceRoomViewController.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/6.
//

import AgoraChat
import AgoraRtcKit
import KakaJSON
import SnapKit
import SVGAPlayer
import UIKit
import ZSwiftBaseLib

public enum SAROLE_TYPE {
    case owner
    case audience
}

let sa_giftMap = [["gift_id": "VoiceRoomGift1", "gift_name": sceneLocalized( "spatial_voice_sweet_heart"), "gift_price": "1", "gift_count": "1", "selected": true], ["gift_id": "VoiceRoomGift2", "gift_name": sceneLocalized( "spatial_voice_flower"), "gift_price": "5", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift3", "gift_name": sceneLocalized( "spatial_voice_crystal_box"), "gift_price": "10", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift4", "gift_name": sceneLocalized( "spatial_voice_super_agora"), "gift_price": "20", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift5", "gift_name": sceneLocalized( "spatial_voice_star"), "gift_price": "50", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift6", "gift_name": sceneLocalized( "spatial_voice_lollipop"), "gift_price": "100", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift7", "gift_name": sceneLocalized( "spatial_voice_diamond"), "gift_price": "500", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift8", "gift_name": sceneLocalized( "spatial_voice_crown"), "gift_price": "1000", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift9", "gift_name": sceneLocalized( "spatial_voice_rocket"), "gift_price": "1500", "gift_count": "1", "selected": false]]

fileprivate let ownerMic = ["index":0,"status":0,"member":["uid":SAUserInfo.shared.user?.uid ?? "","chat_uid":SAUserInfo.shared.user?.chat_uid ?? "","name":SAUserInfo.shared.user?.name ?? "","portrait":SAUserInfo.shared.user?.portrait ?? "","rtc_uid":SAUserInfo.shared.user?.rtc_uid ?? "","mic_index":0]] as [String : Any]

class SARoomViewController: SABaseViewController {
    lazy var toastPoint: CGPoint = .init(x: self.view.center.x, y: self.view.center.y + 70)

    override public var preferredStatusBarStyle: UIStatusBarStyle {
        .lightContent
    }

    var headerView: SARoomHeaderView!
   // var rtcView: SANormalRtcView!
    var sRtcView: SA3DRtcView!

    @UserDefault("VoiceRoomUserAvatar", defaultValue: "") var userAvatar

    lazy var chatBar: SAChatBar = .init(frame: CGRect(x: 0,
                                                      y: ScreenHeight - CGFloat(ZBottombarHeight) - 50,
                                                      width: ScreenWidth,
                                                      height: 50),
                                        style: .spatialAudio)
    private lazy var debugButton: UIButton = {
        let button = UIButton()
        button.setTitle("Debug", for: .normal)
        button.setTitleColor(.black, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 12)
        button.cornerRadius(25)
        button.backgroundColor = .white
        button.addTarget(self, action: #selector(onTapDebugButton), for: .touchUpInside)
        button.isHidden = !AppContext.shared.isDebugMode
        return button
    }()
    private lazy var actionView = ActionSheetManager()
    
    private lazy var tipsView = SASpatialTipsView()

    var preView: SAVMPresentView!
    var noticeView: SANoticeView!
    var isShowPreSentView: Bool = false
    var rtckit = SARTCManager.getSharedInstance()
    var isOwner: Bool {
        SAUserInfo.shared.user?.uid == roomInfo?.room?.owner?.uid
    }
    var ains_state: SARtcType.AINS_STATE = .mid
    var local_index: Int?
    var alienCanPlay: Bool = false
    var vmType: SARtcType.VMMUSIC_TYPE = .social

    public var roomInfo: SARoomInfo? {
        didSet {
            SAUserInfo.shared.currentRoomOwner = roomInfo?.room?.owner
            if let mics = roomInfo?.mic_info {
                sRtcView.micInfos = mics
            }
        }
    }

    convenience init(info: SARoomInfo) {
        self.init()
        roomInfo = info
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigation.isHidden = true
        UIApplication.shared.isIdleTimerDisabled = true
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setNeedsStatusBarAppearanceUpdate()

        guard let type = roomInfo?.room?.sound_effect else { return }
        local_index = isOwner ? 0 : nil
        vmType = getSceneType(type)
        AppContext.saServiceImp().subscribeEvent(with: self)
        // 布局UI
        layoutUI()
        // 加载RTC+IM
        loadKit()
        // 处理底部事件
        charBarEvents()
        NotificationCenter.default.addObserver(self, selector: #selector(leaveRoom), name: Notification.Name("terminate"), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateMicInfo), name: Notification.Name("updateMicInfo"), object: nil)
        
        if isOwner {
            AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self, completion: nil)
        }
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        tipsView.show()
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        navigation.isHidden = false
//        UIApplication.shared.isIdleTimerDisabled = false
    }

    deinit {
        SAUserInfo.shared.currentRoomOwner = nil
        SAUserInfo.shared.user?.amount = 0
//        AppContext.saServiceImp().cleanCache()
        AppContext.saServiceImp().unsubscribeEvent()
        navigationController?.interactivePopGestureRecognizer?.isEnabled = true
    }
}

extension SARoomViewController {
    // 加载RTC
    func loadKit() {
        guard let channel_id = roomInfo?.room?.channel_id else { return }
        let rtcUid = VLUserCenter.user.id
        rtckit.setClientRole(role: isOwner ? .owner : .audience)
        rtckit.delegate = self
        let _ = self.rtckit.joinVoicRoomWith(with: "\(channel_id)",token: VLUserCenter.user.agoraRTCToken, rtcUid: Int(rtcUid) ?? 0, type: self.vmType ) == 0
        rtckit.initSpatialAudio(recvRange: 15)
        // 收集APM全链路音频
        rtckit.setAPMOn(isOn: true)
    }
    
    func refreshRoomInfo() {
        roomInfo?.room?.member_list = AppContext.saTmpServiceImp().userList//[SAUser]()
        roomInfo?.room?.ranking_list = [SAUser]()
        if let info = roomInfo {
            info.mic_info = AppContext.saTmpServiceImp().mics
            roomInfo = info
            headerView.updateHeader(with: info.room)
//            AppContext.saTmpServiceImp().userList = roomInfo?.room?.member_list ?? []
        }
    }

    func getSceneType(_ type: Int) -> SARtcType.VMMUSIC_TYPE {
        switch type {
        case 2: return .ktv
        case 3: return .game
        case 4: return .anchor
        default: return .social
        }
    }

    // 加入房间获取房间详情
    func requestRoomDetail() {
        // 如果不是房主。需要主动获取房间详情
        AppContext.saServiceImp().fetchRoomDetail(entity: roomInfo?.room ?? SARoomEntity()) { [weak self] error, room_info in
            if error == nil {
                guard let info = room_info else { return }
                 self?.roomInfoUpdateUI(info: info)
            } else {
                self?.fetchDetailError()
            }
        }
    }
    
    func roomInfoUpdateUI(info: SARoomInfo) {
        roomInfo = info
        headerView.updateHeader(with: info.room)
        guard let mics = roomInfo?.mic_info else { return }
        roomInfo?.room?.member_list = AppContext.saTmpServiceImp().userList
        onRobotUpdate(robotInfo: info.robotInfo)
        
        AppContext.saTmpServiceImp().mics = mics
        roomInfo?.room?.ranking_list = info.room?.ranking_list
        if let first = info.room?.ranking_list?.first(where: { $0.chat_uid == VLUserCenter.user.chat_uid }) {
            SAUserInfo.shared.user?.amount = first.amount
        }
    }
    
    func fetchDetailError() {
        DispatchQueue.main.async {
            self.notifySeverLeave()
            self.rtckit.leaveChannel()
            self.leaveRoom()
            self.isOwner ? self.ownerBack():self.backAction()
        }
    }

    func requestRankList() {
        AppContext.saServiceImp().fetchGiftContribute {[weak self] error, users in
            if error == nil, users != nil, let self = self {
                let info = self.roomInfo
                info?.room?.ranking_list = users
                self.headerView.updateHeader(with: info?.room)
            }
        }
    }

    func layoutUI() {
        let bgImgView = UIImageView()
        bgImgView.image = UIImage.sceneImage(name: "lbg", bundleName: "VoiceChatRoomResource")
        view.addSubview(bgImgView)

        headerView = SARoomHeaderView()
        headerView.completeBlock = { [weak self] action in
            self?.didHeaderAction(with: action, destroyed: false)
        }
        view.addSubview(headerView)

        sRtcView = SA3DRtcView(rtcKit: rtckit)
        view.addSubview(sRtcView)

        if let entity = roomInfo?.room {
            sRtcView.isHidden = entity.type == 0
            headerView.updateHeader(with: entity)
        }
        sRtcView.clickBlock = {[weak self] type, tag in
            self?.didRtcAction(with: type, tag: tag)
        }

        bgImgView.snp.makeConstraints { make in
            make.left.right.top.bottom.equalTo(view)
        }

        let isHairScreen =  Screen.isFullScreen
        headerView.snp.makeConstraints { make in
            make.left.top.right.equalTo(view)
            make.height.equalTo(isHairScreen ? 140 : 140 - 25)
        }

        sRtcView.snp.makeConstraints { make in
            make.top.equalTo(self.headerView.snp.bottom)
            make.left.right.equalTo(self.view)
            make.bottom.equalTo(self.view.snp.bottom).offset(isHairScreen ? -84 : -50)
        }
        view.addSubViews([chatBar])
        
        view.addSubview(debugButton)
        debugButton.translatesAutoresizingMaskIntoConstraints = false
        debugButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -35).isActive = true
        debugButton.bottomAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
        debugButton.widthAnchor.constraint(equalToConstant: 50).isActive = true
        debugButton.heightAnchor.constraint(equalToConstant: 50).isActive = true
        
        view.layoutIfNeeded()
    }


    @objc
    private func onTapDebugButton() {
        actionView.section(section: 1)
            .row(row: 1)
            .title(title: "Dump数据类型")
            .switchCell(iconName: "icons／set／jiqi", title: "APM全链路音频", isOn: true)
            .config()
        actionView.didSwitchValueChangeClosure = { [weak self] _, isOn in
            self?.rtckit.setAPMOn(isOn: isOn)
        }
        actionView.show()
    }
    
    func didHeaderAction(with action: SAHEADER_ACTION, destroyed: Bool) {
        if action == .back || action == .popBack {
            if isOwner && action != .popBack {
                if destroyed != true {
                    showEndLive()
                } else {
                    notifySeverLeave()
                    rtckit.leaveChannel()
                    ownerBack()
                }
            } else {
                notifySeverLeave()
                rtckit.leaveChannel()
                self.leaveRoom()
                backAction()
            }
        } else if action == .notice {
            showNoticeView(with: isOwner ? .owner : .audience)
        } else if action == .rank {
            // 展示土豪榜
            showUsers()
        } else if action == .beginnersGuide {
            showSoundView()
        }else if action == .more {
            let dialog = AUiMoreDialog(frame: view.bounds)
            view.addSubview(dialog)
            dialog.show()
        }
    }

    func didRtcAction(with type: SABaseUserCellType, tag: Int) {
        let index: Int = tag - 200
        //TODO: remove as!
        guard let mic: SARoomMic = AppContext.saTmpServiceImp().mics[safe:index] else { return }
        if index == 6 || index == 3 { // 操作机器人
            if roomInfo?.robotInfo.use_robot == false {
                showActiveAlienView(true)
            }
        } else {
            if isOwner {
                if index == 0 {
                    showMuteView(with: index)
                } else {
                    showApplyAlert(index)
                }
            } else {
                /*
                 1.如果当前麦位有用户，普通人只能操作自己
                 2.如果麦位没人 需要先判端是否是换麦还是申请上卖
                 */
                if let _ = mic.member, mic.status != -1 {
                    if local_index == index {
                        showMuteView(with: index)
                    }
                } else {
                    if local_index != nil {
                        SAThrottler.throttle(delay: .seconds(1)) {
                            DispatchQueue.main.async {
                                self.changeMic(from: self.local_index!, to: tag - 200)
                            }
                        }
                    } else {
                        userApplyAlert(tag - 200)
                    }
                }
            }
        }
    }

    func notifySeverLeave() {
        guard let roomId = roomInfo?.room?.room_id else { return }
        if self.local_index == nil {
            AppContext.saServiceImp().leaveRoom(roomId) { error, flag in }
        } else {
            AppContext.saServiceImp().leaveMic(mic_index: self.local_index ?? AppContext.saTmpServiceImp().findMicIndex()) { error, result in
            }
        }

    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        if isShowPreSentView {
            UIView.animate(withDuration: 0.5, animations: {
                self.preView.frame = CGRect(x: 0, y: ScreenHeight, width: ScreenWidth, height: 450)
            }) { _ in
                if self.preView == nil {return}
                self.preView.removeFromSuperview()
                self.preView = nil
               // self.rtcView.isUserInteractionEnabled = true
                self.headerView.isUserInteractionEnabled = true
                self.isShowPreSentView = false
            }
        }
    }

    func showNoticeView(with role: SAROLE_TYPE) {
        let noticeView = SANoticeView(frame: CGRect(x: 0,
                                                    y: 0,
                                                    width: ScreenWidth,
                                                    height: 220))
        noticeView.roleType = role
        noticeView.noticeStr = roomInfo?.room?.announcement ?? ""
        noticeView.resBlock = { [weak self] flag, str in
            self?.dismiss(animated: true)
            guard let str = str else { return }
            // 修改群公告
            self?.updateNotice(with: str)
        }
        let noticeStr = roomInfo?.room?.announcement ?? ""
        noticeView.noticeStr = noticeStr
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth,
                                                                                             height: 220)),
                                       custom: noticeView)
        sa_presentViewController(vc)
    }

    func showSoundView() {
        tipsView.show()
    }

    func showActiveAlienView(_ active: Bool) {
        if !isOwner {
            view.makeToast("spatial_voice_host_bot".spatial_localized())
            return
        }
        let confirmView = SAConfirmView(frame: CGRect(x: 0,
                                                      y: 0,
                                                      width: ScreenWidth - 40,
                                                      height: 220),
                                        type: .addbot)
        var compent = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth - 40,
                                                                   height: 220))
        compent.destination = .center
        let vc = SAAlertViewController(compent: compent, custom: confirmView)
        confirmView.resBlock = { [weak self] flag in
            self?.dismiss(animated: true)
            if flag == false { return }
            self?.activeAlien(active)
        }
        sa_presentViewController(vc)
    }

    func activeAlien(_ flag: Bool) {
        if isOwner == false {
            view.makeToast("spatial_voice_host_bot".spatial_localized())
            return
        }
        guard let mic_blue: SARoomMic = roomInfo?.mic_info?[3] else { return }
        guard let mic_red: SARoomMic = roomInfo?.mic_info?[6] else { return }
        let robotInfo = roomInfo?.robotInfo ?? SARobotAudioInfo()
        robotInfo.use_robot = flag
        AppContext.saServiceImp().updateRobotInfo(info: robotInfo) {[weak self] error in
            guard let self = self else {return}
            if error == nil {
                mic_blue.status = flag == true ? 5 : -2
                mic_red.status = flag == true ? 5 : -2
                self.roomInfo?.robotInfo.use_robot = flag
                self.roomInfo?.mic_info?[3] = mic_blue
                self.roomInfo?.mic_info?[6] = mic_red
                self.sRtcView.updateUser(mic_blue)
                self.sRtcView.updateUser(mic_red)

            } else {
                print("激活机器人失败")
            }
        }
    }

    // announcement
    func updateNotice(with str: String) {
        AppContext.saServiceImp().updateAnnouncement(content: str) {[weak self] result in
            guard let self = self else {return}
            if result {
                // 如果返回的结果为true 表示上麦成功
                self.view.makeToast("spatial_voice_notice_posted".spatial_localized())
                self.roomInfo?.room?.announcement = str
            } else {
                self.view.makeToast("spatial_voice_post_failed".spatial_localized())
            }
        }
    }

    func updateVolume(_ Vol: Int) {
        if isOwner == false { return }
        let robotInfo = roomInfo?.robotInfo ?? SARobotAudioInfo()
        robotInfo.robot_volume = UInt(Vol)
        AppContext.saServiceImp().updateRobotInfo(info: robotInfo) {[weak self] error in
            guard let self = self else {return}
            if error == nil {
                // 如果返回的结果为true 表示上麦成功
                guard let robotInfo = self.roomInfo?.robotInfo else { return }
                robotInfo.robot_volume = UInt(Vol)
                self.roomInfo?.robotInfo = robotInfo
                self.rtckit.updatePlayerVolume(value: Double(Vol))
            }
        }
    }

    func charBarEvents() {
        chatBar.events = { [weak self] in
            guard let self = self else { return }
            switch $0 {
            case .eq: self.showEQView()
            case .mic: self.changeMicState()
            case .gift: self.showGiftAlert()
            case .handsUp: self.changeHandsUpState()
            default: break
            }
        }
    }

    
    func showEndLive() {
        var compent = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth - 70, height: 190))
        compent.destination = .center
        let micAlert = SAEndLiveAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 70, height: 190), title: "spatial_voice_end_live".spatial_localized(), content: "spatial_voice_the_room_will_close_after_you_leave.".spatial_localized(), cancel: "spatial_voice_cancel".spatial_localized(), confirm: "spatial_voice_confirm".spatial_localized()).cornerRadius(16).backgroundColor(.white)
        let vc = SAAlertViewController(compent: compent, custom: micAlert)
        micAlert.actionEvents = { [weak self] in
            vc.dismiss(animated: true)
            if $0 != 30 {
                self?.notifySeverLeave()
                self?.rtckit.leaveChannel()
                // giveupStage()
                self?.ownerBack()
            }
        }
        sa_presentViewController(vc)
    }

    private func ownerBack() {
        self.leaveRoom()
        if let vc = navigationController?.viewControllers.filter({ $0 is SARoomsViewController
        }).first {
            navigationController?.popToViewController(vc, animated: true)
        }
    }

    func showInviteMicAlert(user: SAUser) {
        dismiss(animated: false)
        var compent = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth - 75, height: 200))
        compent.destination = .center
        let micAlert = SAApplyAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 75, height: 200), content: "spatial_voice_anchor_invited_you_on_stage".spatial_localized(),
                                    cancel: "spatial_voice_decline".spatial_localized(),
                                    confirm: "spatial_voice_accept".spatial_localized(),
                                    position: .center).cornerRadius(16).backgroundColor(.white)
        let vc = SAAlertViewController(compent: compent, custom: micAlert)
        micAlert.actionEvents = { [weak self] in
            if $0 == 30 {
                self?.refuse()
            } else {
                self?.agreeInvite(user: user)
            }
            vc.dismiss(animated: true)
        }
        sa_presentViewController(vc)
    }
    
    @objc func updateMicInfo(noti: Notification){
        guard let obj: SARoomMic = noti.object as? SARoomMic else {return}
        self.sRtcView.updateUser(obj)
    }
    
    func textHeight(text: String, fontSize: CGFloat, width: CGFloat) -> CGFloat {
        return text.boundingRect(with: CGSize(width: width, height: CGFloat(MAXFLOAT)), options: .usesLineFragmentOrigin, attributes: [.font: UIFont.systemFont(ofSize: fontSize)], context: nil).size.height + 5
    }

    private func getDetailTextHeight(_ effect: Int) -> CGFloat{
        var detailStr: String = ""
        switch effect {
        case 1:
            detailStr = "spatial_voice_chatroom_social_chat_introduce".spatial_localized()
        case 2:
            detailStr = "spatial_voice_chatroom_karaoke_introduce".spatial_localized()
        case 3:
            detailStr = "spatial_voice_chatroom_gaming_buddy_introduce".spatial_localized()
        default:
            detailStr = "spatial_voice_chatroom_professional_broadcaster_introduce".spatial_localized()
        }
        return textHeight(text: detailStr, fontSize: 13, width: self.view.bounds.size.width - 40)
    }
}

// MARK: - SVGAPlayerDelegate

extension SARoomViewController: SVGAPlayerDelegate {
    func svgaPlayerDidFinishedAnimation(_ player: SVGAPlayer!) {
        let animation = view.viewWithTag(199)
        UIView.animate(withDuration: 0.3) {
            animation?.alpha = 0
        } completion: { finished in
            if finished { animation?.removeFromSuperview() }
        }
    }
}

// MARK: - ASManagerDelegate

extension SARoomViewController: SAManagerDelegate {
    func didRtcLocalUserJoinedOfUid(uid: UInt) {
        if self.isOwner == true {
            self.refreshRoomInfo()
        } else {
            //观众更新拉取详情
            self.requestRoomDetail()
        }
    }

    func didRtcRemoteUserJoinedOfUid(uid: UInt) {
        
    }

    func didRtcUserOfflineOfUid(uid: UInt) {}
    
    func didOccurError(with code: AgoraErrorCode) {
        self.view.makeToast("Join failed!")
//        self.didHeaderAction(with: .back, destroyed: true)
    }

    func reportAlien(with type: SARtcType.ALIEN_TYPE, musicType: SARtcType.VMMUSIC_TYPE) {
        print("musicPath:\(type.rawValue)")
        sRtcView.updateAlienMic(with: type)
    }

    func reportAudioVolumeIndicationOfSpeakers(speakers: [AgoraRtcAudioVolumeInfo]) {
        guard let micinfo = roomInfo?.mic_info else { return }
        for speaker in speakers {
            for mic in micinfo where mic.member != nil{
                let user = mic.member
                guard let rtcUid = Int(user?.rtc_uid ?? "0") else { return }
                if rtcUid == speaker.uid {
                    sRtcView.updateVolume(with: mic.mic_index, vol: Int(speaker.volume))
                    break
                }
            }
        }
    }
}
