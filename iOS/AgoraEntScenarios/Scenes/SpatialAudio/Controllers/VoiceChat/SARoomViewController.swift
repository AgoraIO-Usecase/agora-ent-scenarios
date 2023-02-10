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

let sa_giftMap = [["gift_id": "VoiceRoomGift1", "gift_name": sceneLocalized( "Sweet Heart"), "gift_price": "1", "gift_count": "1", "selected": true], ["gift_id": "VoiceRoomGift2", "gift_name": sceneLocalized( "Flower"), "gift_price": "5", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift3", "gift_name": sceneLocalized( "Crystal Box"), "gift_price": "10", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift4", "gift_name": sceneLocalized( "Super Agora"), "gift_price": "20", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift5", "gift_name": sceneLocalized( "Star"), "gift_price": "50", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift6", "gift_name": sceneLocalized( "Lollipop"), "gift_price": "100", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift7", "gift_name": sceneLocalized( "Diamond"), "gift_price": "500", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift8", "gift_name": sceneLocalized( "Crown"), "gift_price": "1000", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift9", "gift_name": sceneLocalized( "Rocket"), "gift_price": "1500", "gift_count": "1", "selected": false]]

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
    var alienCanPlay: Bool = true
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
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        tipsView.show()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        navigation.isHidden = false
        UIApplication.shared.isIdleTimerDisabled = false
    }

    deinit {
        print("\(String(describing: self.swiftClassName)) is destroyed!")
        SAUserInfo.shared.currentRoomOwner = nil
        SAUserInfo.shared.user?.amount = 0
//        AppContext.saServiceImp().cleanCache()
        AppContext.saServiceImp().unsubscribeEvent()
    }
}

extension SARoomViewController {
    // 加载RTC
    func loadKit() {
        guard let channel_id = roomInfo?.room?.channel_id else { return }
        guard let roomId = roomInfo?.room?.chatroom_id else { return }
        let rtcUid = VLUserCenter.user.id
        rtckit.setClientRole(role: isOwner ? .owner : .audience)
        rtckit.delegate = self
        rtckit.initSpatialAudio(recvRange: 10)

        var rtcJoinSuccess = false
        var IMJoinSuccess = false

        let VMGroup = DispatchGroup()
        let imQueue = DispatchQueue(label: "com.im.vm.www")
        let rtcQueue = DispatchQueue(label: "com.rtc.vm.www")

        VMGroup.enter()
        rtcQueue.async { [weak self] in
            rtcJoinSuccess = self?.rtckit.joinVoicRoomWith(with: "\(channel_id)",token: VLUserCenter.user.agoraRTCToken, rtcUid: Int(rtcUid) ?? 0, type: self?.vmType ?? .social) == 0
            VMGroup.leave()
        }

        VMGroup.enter()
        imQueue.async {
            SAIMManager.shared?.joinedChatRoom(roomId: roomId, completion: { room, error in
                IMJoinSuccess = error == nil
                VMGroup.leave()
            })
        }

        VMGroup.notify(queue: .main) { [weak self] in
            let joinSuccess = rtcJoinSuccess && IMJoinSuccess
            guard let `self` = self else { return }
            if !joinSuccess {
                self.view.makeToast("Join failed!")
                self.didHeaderAction(with: .back, destroyed: true)
            } else {
                if self.isOwner == true {
                    //房主更新环信KV
                    self.setChatroomAttributes()
                } else {
                    //观众更新拉取详情后更新kv
                    self.requestRoomDetail()
                    self.sendJoinedMessage()
                }
            }
        }
    }
    
    private func setChatroomAttributes() {
//        SAIMManager.shared?.setChatroomAttributes(attributes: AppContext.saTmpServiceImp().createMics() , completion: { error in
//            if error == nil {
                self.refreshRoomInfo()
//            } else {
//                self.view.makeToast("Set chatroom attributes failed!")
//            }
//        })
    }
    
    private func sendJoinedMessage() {
        guard let user = SAUserInfo.shared.user else {return}
        user.mic_index = -1
        SAIMManager.shared?.sendCustomMessage(roomId: roomInfo?.room?.chatroom_id ?? "",
                                              event: SAJoinedMember,
                                              customExt: ["user" : user.kj.JSONString()],
                                              completion: { message, error in
            if error != nil {
                self.view.makeToast("Send joined chatroom message failed!")
            }
        })
    }
    
    func refreshRoomInfo() {
        roomInfo?.room?.member_list = [SAUser]()
        roomInfo?.room?.ranking_list = [SAUser]()
        if let info = roomInfo {
            info.mic_info = AppContext.saTmpServiceImp().mics
            roomInfo = info
            headerView.updateHeader(with: info.room)
            AppContext.saTmpServiceImp().userList = roomInfo?.room?.member_list ?? []
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
        if roomInfo?.room?.member_list == nil {
            roomInfo?.room?.member_list = [SAUser]()
        }
        roomInfo?.room?.member_list?.append(SAUserInfo.shared.user!)
        SAIMManager.shared?.setChatroomAttributes(attributes: ["member_list": roomInfo?.room?.member_list?.kj.JSONString() ?? ""], completion: { error in
            if error != nil {
                self.view.makeToast("update member_list failed!\(error?.errorDescription ?? "")")
            }
        })
        
        AppContext.saTmpServiceImp().mics = mics
        AppContext.saTmpServiceImp().userList = roomInfo?.room?.member_list ?? []
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
        AppContext.saServiceImp().fetchGiftContribute { error, users in
            if error == nil, users != nil {
                let info = self.roomInfo
                info?.room?.ranking_list = users
                self.headerView.updateHeader(with: info?.room)
            }
        }
    }

    func layoutUI() {
        SwiftyFitsize.reference(width: 375, iPadFitMultiple: 0.6)

        let bgImgView = UIImageView()
        bgImgView.image = UIImage("lbg")
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

        let isHairScreen = SwiftyFitsize.isFullScreen
        headerView.snp.makeConstraints { make in
            make.left.top.right.equalTo(view)
            make.height.equalTo(isHairScreen ? 140~ : 140~ - 25)
        }

        sRtcView.snp.makeConstraints { make in
            make.top.equalTo(self.headerView.snp.bottom)
            make.left.right.equalTo(self.view)
            make.bottom.equalTo(self.view.snp.bottom).offset(isHairScreen ? -84 : -50)
        }
        view.addSubViews([chatBar])
        
        view.layoutIfNeeded()
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
        } else if action == .soundClick {
            showSoundView()
        }
    }

    func didRtcAction(with type: SABaseUserCellType, tag: Int) {
        let index: Int = tag - 200
        //TODO: remove as!
        guard let mic: SARoomMic = AppContext.saTmpServiceImp().mics[safe:index] else { return }
        if index == 6 || index == 5 { // 操作机器人
            if roomInfo?.room?.use_robot == false {
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
                if let _ = mic.member {
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
                AppContext.saServiceImp().leaveRoom(roomId) { error, flag in }
            }
        }

    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        if isShowPreSentView {
            UIView.animate(withDuration: 0.5, animations: {
                self.preView.frame = CGRect(x: 0, y: ScreenHeight, width: ScreenWidth, height: 450~)
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

    func showNoticeView(with role: ROLE_TYPE) {
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
        let soundView = SASoundView(frame: CGRect(x: 0,
                                                  y: 0,
                                                  width: ScreenWidth,
                                                  height: 180 + getDetailTextHeight(roomInfo?.room?.sound_effect ?? 1)),
                                    soundEffect: roomInfo?.room?.sound_effect ?? 1)
        let height = 180 + getDetailTextHeight(roomInfo?.room?.sound_effect ?? 1)
        let vc = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth,
                                                                                             height: height)),
                                       custom: soundView)
        sa_presentViewController(vc)
    }

    func showActiveAlienView(_ active: Bool) {
        if !isOwner {
            view.makeToast("Host Bot".localized())
            return
        }
        let confirmView = SAConfirmView(frame: CGRect(x: 0,
                                                      y: 0,
                                                      width: ScreenWidth - 40~,
                                                      height: 220~),
                                        type: .addbot)
        var compent = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth - 40~,
                                                                   height: 220~))
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
            view.makeToast("Host Bot".localized())
            return
        }
        guard let mic: SARoomMic = roomInfo?.mic_info![6] else { return }
        AppContext.saServiceImp().enableRobot(enable: flag) { error in
            if error == nil {
                if self.alienCanPlay {
                    self.rtckit.adjustAudioMixingVolume(with: 50)
                    self.rtckit.playMusic(with: .alien)
                    self.alienCanPlay = false
                }

                let mic_info = mic
                mic_info.status = flag == true ? 5 : -2
                self.roomInfo?.room?.use_robot = flag
                self.roomInfo?.mic_info![6] = mic_info
                
                let blue_micInfo = mic_info
                let red_micInfo = mic_info
                
                //更新红蓝机器人的信息
                let blue: SAUser = SAUser()
                blue.portrait = "blue"
                blue.name = "Agora Blue"
                blue_micInfo.member = blue
                self.sRtcView.updateUser(blue_micInfo)
                
                let red: SAUser = SAUser()
                red.portrait = "blue"
                red.name = "Agora Blue"
                red_micInfo.member = blue
                self.sRtcView.updateUser(red_micInfo)
            } else {
                print("激活机器人失败")
            }
        }
    }

    // announcement
    func updateNotice(with str: String) {
        AppContext.saServiceImp().updateAnnouncement(content: str) { result in
            if result {
                // 如果返回的结果为true 表示上麦成功
                self.view.makeToast("Notice Posted".localized())
                self.roomInfo?.room?.announcement = str
            } else {
                self.view.makeToast("Post Failed".localized())
            }
        }
    }

    func updateVolume(_ Vol: Int) {
        if isOwner == false { return }
        AppContext.saServiceImp().updateRobotVolume(value: Vol) { error in
            if error == nil {
                // 如果返回的结果为true 表示上麦成功
                guard let room = self.roomInfo?.room else { return }
                let newRoom = room
                newRoom.robot_volume = UInt(Vol)
                self.roomInfo?.room = newRoom
                self.rtckit.adjustAudioMixingVolume(with: Vol)
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
        let micAlert = SAEndLiveAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 70, height: 190), title: sceneLocalized( "End Live"), content: sceneLocalized( "The room will close after you leave."), cancel: sceneLocalized( "Cancel"), confirm: sceneLocalized( "Confirm")).cornerRadius(16).backgroundColor(.white)
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

    func showInviteMicAlert() {
        var compent = SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth - 75, height: 200))
        compent.destination = .center
        let micAlert = SAApplyAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 75, height: 200), content: "Anchor Invited You On-Stage", cancel: "Decline", confirm: "Accept", position: .center).cornerRadius(16).backgroundColor(.white)
        let vc = SAAlertViewController(compent: compent, custom: micAlert)
        micAlert.actionEvents = { [weak self] in
            if $0 == 30 {
                self?.refuse()
            } else {
                self?.agreeInvite()
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
            detailStr = "This sound effect focuses on solving the voice call problem of the Social Chat scene, including noise cancellation and echo suppression of the anchor's voice. It can enable users of different network environments and models to enjoy ultra-low delay and clear and beautiful voice in multi-person chat.".localized()
        case 2:
            detailStr = "This sound effect focuses on solving all kinds of problems in the Karaoke scene of single-person or multi-person singing, including the balance processing of accompaniment and voice, the beautification of sound melody and voice line, the volume balance and real-time synchronization of multi-person chorus, etc. It can make the scenes of Karaoke more realistic and the singers' songs more beautiful.".localized()
        case 3:
            detailStr = "This sound effect focuses on solving all kinds of problems in the game scene where the anchor plays with him, including the collaborative reverberation processing of voice and game sound, the melody of sound and the beautification of sound lines. It can make the voice of the accompanying anchor more attractive and ensure the scene feeling of the game voice. ".localized()
        default:
            detailStr = "This sound effect focuses on solving the problems of poor sound quality of mono anchors and compatibility with mainstream external sound cards. The sound network stereo collection and high sound quality technology can greatly improve the sound quality of anchors using sound cards and enhance the attraction of live broadcasting rooms. At present, it has been adapted to mainstream sound cards in the market. ".localized()
        }
        return textHeight(text: detailStr, fontSize: 13, width: self.view.bounds.size.width - 40~)
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
        
    }

    func didRtcRemoteUserJoinedOfUid(uid: UInt) {
        
    }

    func didRtcUserOfflineOfUid(uid: UInt) {}

    func reportAlien(with type: SARtcType.ALIEN_TYPE, musicType: SARtcType.VMMUSIC_TYPE) {
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
