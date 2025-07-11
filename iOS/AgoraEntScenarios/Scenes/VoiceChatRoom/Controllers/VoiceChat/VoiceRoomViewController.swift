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
import AgoraCommon
public enum ROLE_TYPE {
    case owner
    case audience
}

let giftMap = [["gift_id": "VoiceRoomGift1", "gift_name": LanguageManager.localValue(key: "voice_sweet_heart"), "gift_price": "1", "gift_count": "1", "selected": true], ["gift_id": "VoiceRoomGift2", "gift_name": LanguageManager.localValue(key: "voice_flower"), "gift_price": "5", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift3", "gift_name": LanguageManager.localValue(key: "voice_crystal_box"), "gift_price": "10", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift4", "gift_name": LanguageManager.localValue(key: "voice_super_agora"), "gift_price": "20", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift5", "gift_name": LanguageManager.localValue(key: "voice_star"), "gift_price": "50", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift6", "gift_name": LanguageManager.localValue(key: "voice_lollipop"), "gift_price": "100", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift7", "gift_name": LanguageManager.localValue(key: "voice_diamond"), "gift_price": "500", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift8", "gift_name": LanguageManager.localValue(key: "voice_crown"), "gift_price": "1000", "gift_count": "1", "selected": false], ["gift_id": "VoiceRoomGift9", "gift_name": LanguageManager.localValue(key: "voice_rocket"), "gift_price": "1500", "gift_count": "1", "selected": false]]

let parmVals: [Double] = AgoraConfig.parmVals

let parKeys = AgoraConfig.parmKeys


fileprivate let ownerMic = ["index":0,"status":0,"member":["uid":VoiceRoomUserInfo.shared.user?.uid ?? "","chat_uid":VoiceRoomUserInfo.shared.user?.chat_uid ?? "","name":VoiceRoomUserInfo.shared.user?.name ?? "","portrait":VoiceRoomUserInfo.shared.user?.portrait ?? "","rtc_uid":VoiceRoomUserInfo.shared.user?.rtc_uid ?? "","mic_index":0]] as [String : Any]

class VoiceRoomViewController: VRBaseViewController {
    private var isEnterSeatNotFirst: Bool = false
    lazy var toastPoint: CGPoint = .init(x: self.view.center.x, y: self.view.center.y + 70)

    override var preferredStatusBarStyle: UIStatusBarStyle {
        .lightContent
    }

    var headerView: AgoraChatRoomHeaderView!
    var rtcView: AgoraChatRoomNormalRtcView!
    
    @UserDefault("VoiceRoomUserAvatar", defaultValue: "") var userAvatar

    lazy var chatView: VoiceRoomChatView = .init(frame: CGRect(x: 0, y: ScreenHeight - CGFloat(ZBottombarHeight) - (ScreenHeight / 667) * 210 - 50, width: ScreenWidth, height: (ScreenHeight / 667) * 210))

    lazy var chatBar: VoiceRoomChatBar = .init(frame: CGRect(x: 0, y: ScreenHeight - CGFloat(ZBottombarHeight) - 50, width: ScreenWidth, height: 50), style: self.roomInfo?.room?.type ?? 0 == 1 ? .spatialAudio : .normal)

    lazy var inputBar: VoiceRoomInputBar = .init(frame: CGRect(x: 0, y: ScreenHeight, width: ScreenWidth, height: 60)).backgroundColor(.white)

    var preView: VMPresentView!
    private lazy var noticeView = VMNoticeView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 230))
    
    private lazy var actionView = ActionSheetManager()

    private lazy var debugButton: UIButton = {
        let button = UIButton()
        button.setTitle("Debug", for: .normal)
        button.setTitleColor(.black, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 12)
        button.cornerRadius(25)
        button.backgroundColor = .white
        button.addTarget(self, action: #selector(onTapDebugButton), for: .touchUpInside)
        button.isHidden = !AppContext.shared.isDeveloperMode
        return button
    }()
    
    var isShowPreSentView: Bool = false
    var rtckit: VoiceRoomRTCManager = VoiceRoomRTCManager.getSharedInstance()
    var isOwner: Bool = false
    var ains_state: AINS_STATE = .mid
    var aed_state: AED_STATE = .off
    var aspt_state: ASPT_STATE = .off
    var local_index: Int? {
        didSet {
            if local_index == nil {
                self.rtckit.setClientRole(role: .audience)
            } else {
                self.rtckit.setClientRole(role: .owner)
            }
        }
    }
    var vmType: VMMUSIC_TYPE = .social
    var isHeaderBack = false

    public var roomInfo: VRRoomInfo? {
        didSet {
            VoiceRoomUserInfo.shared.currentRoomOwner = self.roomInfo?.room?.owner
            if let mics = roomInfo?.mic_info {
                if let type = roomInfo?.room?.type {
                    if type == 0 {
                        self.rtcView.micInfos = mics
                    }
                }
            }
        }
    }
    
    //虚拟声卡的属性
    public var soundcardPresenter = VirtualSoundcardPresenter()
    
    // ai降噪选项状态记录
    public var ainsLevelChecked = false

    convenience init(info: VRRoomInfo) {
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
        ChatRoomServiceImp.getSharedInstance().subscribeEvent(with: self)
        soundcardPresenter.setupDefault()
        guard let user = VoiceRoomUserInfo.shared.user else { return }
        guard let owner = roomInfo?.room?.owner else { return }
        guard let type = roomInfo?.room?.sound_effect else { return }
        isOwner = user.uid == owner.uid
        local_index = isOwner ? 0 : nil
        vmType = getSceneType(type)
        // 布局UI
        layoutUI()
        // 加载RTC+IM
        loadKit()
        // 处理底部事件
        charBarEvents()
        self.subscribeSceneRoom()
        NotificationCenter.default.addObserver(self, selector: #selector(leaveRoom), name: Notification.Name("terminate"), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateMicInfo), name: Notification.Name("updateMicInfo"), object: nil)
        
        if isOwner {
            checkAudioAuthorized()
        }
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            WarmAlertView.show { v in
                if let alert = v as? WarmAlertView {
                    alert.sceneSeconds = AppContext.shared.sceneConfig?.chat ?? 10 * 60
                }
            }
        }
    }
    
    private func subscribeSceneRoom() {
        SyncUtil.scene(id: self.roomInfo?.room?.room_id ?? "")?.subscribe(key: "",onDeleted: { 
            
            if self.isHeaderBack == false,$0.getId() == self.roomInfo?.room?.room_id ?? "" {
                self.view.window?.makeToast("voice_time_limit_desc".voice_localized())
                self.quitRoom()
            }
        })
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        navigation.isHidden = false
        UIApplication.shared.isIdleTimerDisabled = false
    }
    
    deinit {
        print("VoiceRoomVC deinit")
    }

}

extension VoiceRoomViewController {
    
    @objc
    private func onTapDebugButton() {
        actionView
            .title(title: "Dump数据类型")
            .switchCell(iconName: "icons／set／jiqi", title: "APM全链路音频", isOn: AppContext.shared.isVRApmOn)
            .tfCell(title: parKeys[0], value: parmVals[0], desc: "")
            .tfCell(title: parKeys[1], value: parmVals[1], desc: "")
            .tfCell(title: parKeys[2], value: parmVals[2], desc: "")
            .tfCell(title: parKeys[3], value: parmVals[3], desc: "")
            .tfCell(title: parKeys[4], value: parmVals[4], desc: "")
            .tfCell(title: parKeys[5], value: parmVals[5], desc: "")
            .tfCell(title: parKeys[6], value: parmVals[6], desc: "")
            .tfCell(title: parKeys[7], value: parmVals[7], desc: "")
            .tfCell(title: parKeys[8], value: parmVals[8], desc: "")
            .tfCell(title: parKeys[9], value: parmVals[9], desc: "")
            .tfCell(title: parKeys[10], value: parmVals[10], desc: "")
            .tfCell(title: parKeys[11], value: parmVals[11], desc: "")
            .tfCell(title: parKeys[12], value: parmVals[12], desc: "")
            .tfCell(title: parKeys[13], value: parmVals[13], desc: "")
            .tfCell(title: parKeys[14], value: parmVals[14], desc: "")
            .config()
        actionView.didSwitchValueChangeClosure = { [weak self] _, isOn in
            AppContext.shared.isVRApmOn = isOn
            self?.rtckit.setAPMOn(isOn: isOn)
        }
        actionView.didCustomModeSetClosure = {[weak self] mode in
            if mode == .ns {
                self?.rtckit.setAINS(with: .custom)
            } else if mode == .aed {
                self?.rtckit.setAed(with: .custom)
            } else {
                self?.rtckit.setASPT(with: .custom)
            }
        }
        
        actionView.show()
    }
    
    // 加载RTC
    func loadKit() {
        guard let channel_id = roomInfo?.room?.channel_id else { return }
        guard let roomId = roomInfo?.room?.chatroom_id else { return }
        let rtcUid = VLUserCenter.user.id
        rtckit.setClientRole(role: isOwner ? .owner : .audience)
        rtckit.delegate = self
        rtckit.playerDelegate = self
        
        if isOwner {
            checkEnterSeatAudioAuthorized()
        }
        soundcardPresenter.setupEngine(rtckit.rtcKit)

        var rtcJoinSuccess = false
        var IMJoinSuccess = false

        let VMGroup = DispatchGroup()
        let imQueue = DispatchQueue(label: "com.im.vm.www")
        let rtcQueue = DispatchQueue(label: "com.rtc.vm.www")

        VMGroup.enter()
        rtcQueue.async { [weak self] in
            rtcJoinSuccess = self?.rtckit.joinVoicRoomWith(with: "\(channel_id)",token: AppContext.shared.agoraRTCToken, rtcUid: Int(rtcUid) ?? 0, type: self?.vmType ?? .social) == 0
            VMGroup.leave()
        }

        VMGroup.enter()
        imQueue.async {
            if VoiceRoomIMManager.shared?.isLogin ?? false {
                VoiceRoomIMManager.shared?.joinedChatRoom(roomId: roomId, completion: { room, error in
                    IMJoinSuccess = error == nil
                    VMGroup.leave()
                })
            } else {
                IMJoinSuccess = false
                VMGroup.leave()
            }
        }

        VMGroup.notify(queue: .main) { [weak self] in
            let joinSuccess = rtcJoinSuccess && IMJoinSuccess
            guard let `self` = self else { return }
            if !joinSuccess {
                if !IMJoinSuccess {
                    self.view.window?.makeToast("voice_join_IM_failed")
                } else {
                    self.view.window?.makeToast("Join RTC failed!")
                }
                self.quitRoom()
            } else {
                if self.isOwner == true {
                    //房主更新环信KV
                    self.setChatroomAttributes()
                } else {
                    //观众更新拉取详情后更新kv
                    self.requestRoomDetail()
                    Throttler.throttle(queue: .main,delay: .seconds(0.5),shouldRunLatest: true) {
                        self.sendJoinedMessage()
                    }
                    self.requestAnnouncement()
                }
            }
        }
        // 收集APM全链路音频
        rtckit.setAPMOn(isOn: AppContext.shared.isVRApmOn)
    }
    
    private func setChatroomAttributes() {
        // 初始化成员列表
        if self.roomInfo?.room?.member_list == nil {
            self.roomInfo?.room?.member_list = [VRUser]()
        }
        
        // 初始化排行榜列表
        if self.roomInfo?.room?.ranking_list == nil {
            self.roomInfo?.room?.ranking_list = [VRUser]()
        }
        
        // 将房主添加到成员列表
        if let owner = VoiceRoomUserInfo.shared.user {
            self.roomInfo?.room?.member_list?.append(owner)
        }
        
        // 更新麦克风信息
        VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: ChatRoomServiceImp.getSharedInstance().createMics() , completion: { error in
            if error == nil {
                if let info = self.roomInfo {
                    info.mic_info = ChatRoomServiceImp.getSharedInstance().mics
                    self.roomInfo = info
                    self.headerView.updateHeader(with: info.room)
                    
                }
            } else {
                self.view.makeToast("Set chatroom attributes failed!")
            }
        })
        
        // 更新点击次数
        VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: ["click_count":"3"], completion: { error in
        })
        
        // 更新成员列表到IM系统
        let memberListAttributes = ["member_list":self.roomInfo?.room?.member_list?.kj.JSONString() ?? ""]
        print("[ChatRoom] 更新成员列表属性: \(memberListAttributes)")
        VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: memberListAttributes, completion: { error in
            ChatRoomServiceImp.getSharedInstance().userList = self.roomInfo?.room?.member_list
            if error != nil {
                self.view.makeToast("update member_list failed!\(error?.errorDescription ?? "")")
                print("[ChatRoom] 更新成员列表失败: \(error?.errorDescription ?? "")")
            } else {
                print("[ChatRoom] 更新成员列表成功")
            }
        })
    }
    
    private func sendJoinedMessage() {
        guard let user = VoiceRoomUserInfo.shared.user else {return}
        user.mic_index = -1
        VoiceRoomIMManager.shared?.sendCustomMessage(roomId: self.roomInfo?.room?.chatroom_id ?? "", event: VoiceRoomJoinedMember, customExt: ["user" : user.kj.JSONString()], completion: { message, error in
            if error != nil {
                self.view.makeToast("Send joined chatroom message failed!")
            }
        })
    }

    func getSceneType(_ type: Int) -> VMMUSIC_TYPE {
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
        ChatRoomServiceImp.getSharedInstance().fetchRoomDetail(entity: self.roomInfo?.room ?? VRRoomEntity()) { [weak self] error, room_info in
            if error == nil {
                guard let info = room_info else { return }
                self?.roomInfoUpdateUI(info: info)
            } else {
                self?.fetchDetailError()
            }
        }
    }
    
    func requestAnnouncement() {
        VoiceRoomIMManager.shared?.announcement(completion: { room, error in
            if error == nil, room != nil {
                guard let announcement = room?.announcement,!announcement.isEmpty  else { return }
                self.noticeView.noticeStr = announcement
            }
        })
    }
    
    func roomInfoUpdateUI(info: VRRoomInfo) {
        self.roomInfo = info
        self.headerView.updateHeader(with: info.room)
        guard let mics = self.roomInfo?.mic_info else { return }
        if self.roomInfo?.room?.member_list == nil {
            self.roomInfo?.room?.member_list = [VRUser]()
        }
        self.roomInfo?.room?.member_list?.append(VoiceRoomUserInfo.shared.user!)
        let attributes = ["member_list":self.roomInfo?.room?.member_list?.kj.JSONString() ?? ""]
        print("[ChatRoom] setChatroomAttributes: \(attributes)")
        VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: attributes, completion: { error in
            if error != nil {
                self.view.makeToast("update member_list failed!\(error?.errorDescription ?? "")")
            }
        })
        if let click_count = self.roomInfo?.room?.click_count {
            let count = click_count + 1
            self.roomInfo?.room?.click_count = count
            self.headerView.updateHeader(with: self.roomInfo?.room)
            VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: ["click_count":"\(count)"], completion: { error in
            })
        }
        ChatRoomServiceImp.getSharedInstance().mics = mics
        ChatRoomServiceImp.getSharedInstance().userList = self.roomInfo?.room?.member_list
        self.roomInfo?.room?.ranking_list = info.room?.ranking_list
        if let first = info.room?.ranking_list?.first(where: { $0.chat_uid == VLUserCenter.user.chat_uid
        }) {
            VoiceRoomUserInfo.shared.user?.amount = first.amount
        }
        if let enable = info.room?.use_robot {
            guard let mic = info.mic_info?[6] else { return }
            let mic_info = mic
            mic_info.status = enable ? 5 : -2
            self.roomInfo?.room?.use_robot = enable
            self.roomInfo?.mic_info![6] = mic_info
            self.rtcView.updateAlien(mic_info.status)
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
        ChatRoomServiceImp.getSharedInstance().fetchGiftContribute { error, users in
            if error == nil, users != nil {
                let info = self.roomInfo
                info?.room?.ranking_list = users
                self.headerView.updateHeader(with: info?.room)
            }
        }
    }

    func layoutUI() {

        let bgImgView = UIImageView()
        bgImgView.image = UIImage.sceneImage(name: "bg-main", bundleName: "VoiceChatRoomResource")
        view.addSubview(bgImgView)

        headerView = AgoraChatRoomHeaderView() 
        headerView.completeBlock = { [weak self] action in
            self?.didHeaderAction(with: action, destroyed: false)
        }
        view.addSubview(headerView)

        rtcView = AgoraChatRoomNormalRtcView()
        rtcView.isOwner = isOwner
        rtcView.clickBlock = { [weak self] type, tag in
            self?.didRtcAction(with: type, tag: tag)
        }
        view.addSubview(rtcView)
        
        if let entity = roomInfo?.room {
            rtcView.isHidden = entity.type == 1
            headerView.updateHeader(with: entity)
        }

        bgImgView.snp.makeConstraints { make in
            make.left.right.top.bottom.equalTo(self.view)
        }

        let isHairScreen =  Screen.isFullScreen
        headerView.snp.makeConstraints { make in
            make.left.top.right.equalTo(self.view)
            make.height.equalTo(isHairScreen ? 140 : 140 - 25)
        }

        rtcView.snp.makeConstraints { make in
            make.top.equalTo(self.headerView.snp.bottom)
            make.left.right.equalTo(self.view)
            make.height.equalTo(240)
        }
        if roomInfo?.room?.type ?? 0 == 1 {
            view.addSubViews([chatBar])
            inputBar.isHidden = true
        } else {
            let pan = UIPanGestureRecognizer(target: self, action: #selector(resignKeyboard))
            pan.minimumNumberOfTouches = 1
            rtcView.addGestureRecognizer(pan)
            view.addSubViews([chatView, giftList(), chatBar, inputBar])
            inputBar.isHidden = true
        }
        chatView.messages?.append(startMessage())
        
        view.addSubview(debugButton)
        debugButton.translatesAutoresizingMaskIntoConstraints = false
        debugButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -35).isActive = true
        debugButton.bottomAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
        debugButton.widthAnchor.constraint(equalToConstant: 50).isActive = true
        debugButton.heightAnchor.constraint(equalToConstant: 50).isActive = true
    }


    func didHeaderAction(with action: HEADER_ACTION, destroyed: Bool) {
        if action == .back || action == .popBack {
            self.isHeaderBack = true
            if isOwner && action != .popBack {
                if destroyed != true {
                    showEndLive()
                } else {
                    self.quitRoom()
                }
            } else {
                self.quitRoom()
            }
        } else if action == .notice {
            showNoticeView(with: isOwner ? .owner : .audience)
        } else if action == .rank {
            // 展示土豪榜
            showUsers(position: .left)
        } else if action == .soundClick {
            showSoundView()
        } else if action == .members {
            showUsers(position: .right)
        } else if action == .more {
            let dialog = AUiMoreDialog(frame: view.bounds)
            view.addSubview(dialog)
            dialog.show()
        }
    }

    func didRtcAction(with type: AgoraChatRoomBaseUserCellType, tag: Int) {
        let index: Int = tag - 200
        guard let mic: VRRoomMic = ChatRoomServiceImp.getSharedInstance().mics[safe:index] else { return }
        if index == 6 { // 操作机器人
            if roomInfo?.room?.use_robot == false {
                showActiveAlienView(true)
            } else {
                self.showEQOperation()
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
                        Throttler.throttle(queue:.main,shouldRunLatest: true) {
                            self.changeMic(from: self.local_index!, to: tag - 200)
                        }
                    } else {
                        userApplyAlert(tag - 200)
                    }
                }
            }
        }
    }
    
    func showEQOperation() {
        if !isOwner {
            view.makeToast("voice_host_bot".voice_localized())
            return
        }
        let confirmView = VREQOperationAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: (254/375.0)*ScreenWidth)).backgroundColor(.white).cornerRadius(20, [.topLeft, .topRight], .white, 0)
        var compent = PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: (254/375.0)*ScreenWidth))
        compent.destination = .bottomBaseline
        let vc = VoiceRoomAlertViewController(compent: compent, custom: confirmView)
        confirmView.actionClosure = { [weak self] in
            self?.dismiss(animated: true)
            switch $0 {
            case .close:
                self?.activeAlien(false)
            default:
                self?.showEQView()
            }
        }
        presentViewController(vc)
    }

    func notifySeverLeave(complete: (() -> Void)? = nil) {
        guard let index = self.local_index else {
            VoiceChatLog.info("User has no mic index, completing leave process directly")
            complete?()
            return
        }
        VoiceChatLog.info("Notifying server that user is leaving mic at index \(index)")
        ChatRoomServiceImp.getSharedInstance().leaveMic(mic_index: index) { error, result in
            if let error = error {
                VoiceChatLog.info("Failed to leave mic with error: \(error.localizedDescription)")
            } else {
                VoiceChatLog.info("Successfully left mic position")
            }
            complete?()
        }
    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        inputBar.hiddenInputBar()
        if isShowPreSentView {
            UIView.animate(withDuration: 0.5, animations: {
                self.preView.frame = CGRect(x: 0, y: ScreenHeight, width: ScreenWidth, height: 450)
            }) { _ in
                if self.preView == nil {return}
                self.preView.removeFromSuperview()
                self.preView = nil
                self.rtcView.isUserInteractionEnabled = true
                self.headerView.isUserInteractionEnabled = true
                self.isShowPreSentView = false
            }
        }
    }

    func showNoticeView(with role: ROLE_TYPE) {
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
        var component = PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: 220))
        component.destination = .bottomBaseline
        component.keyboardTranslationType = .unabgeschirmt(compress: true)
        let vc = VoiceRoomAlertViewController(compent: component, custom: noticeView)
        presentViewController(vc)
    }

    func showSoundView() {
        let soundView = VMSoundView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 180 + getDetailTextHeight(roomInfo?.room?.sound_effect ?? 1)), soundEffect: roomInfo?.room?.sound_effect ?? 1)
        let vc = VoiceRoomAlertViewController(compent: PresentedViewComponent(contentSize: CGSize(width: ScreenWidth, height: 180 + getDetailTextHeight(roomInfo?.room?.sound_effect ?? 1))), custom: soundView)
        presentViewController(vc)
    }

    func showActiveAlienView(_ active: Bool) {
        if !isOwner {
            view.makeToast("voice_host_bot".voice_localized())
            return
        }
        let confirmView = VMConfirmView(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 40, height: 220), type: .addbot)
        var compent = PresentedViewComponent(contentSize: CGSize(width: ScreenWidth - 40, height: 220))
        compent.destination = .center
        let vc = VoiceRoomAlertViewController(compent: compent, custom: confirmView)
        confirmView.resBlock = { [weak self] flag in
            self?.dismiss(animated: true)
            if flag == false { return }
            self?.activeAlien(active)
        }
        presentViewController(vc)
    }

    func activeAlien(_ flag: Bool) {
        if isOwner == false {
            view.makeToast("voice_host_bot".voice_localized())
            return
        }
        guard let mic: VRRoomMic = roomInfo?.mic_info?[6] else { return }
        ChatRoomServiceImp.getSharedInstance().enableRobot(enable: flag) { error in
            if error == nil {
                if flag {
                    self.rtckit.adjustAudioMixingVolume(with: 50)
                    self.rtckit.playMusic(with: .alien)
                } else {
                    self.rtckit.stopPlayMusic()
                }

                let mic_info = mic
                mic_info.status = flag == true ? 5 : -2
                self.roomInfo?.room?.use_robot = flag
                self.roomInfo?.mic_info![6] = mic_info
                self.rtcView.updateAlien(mic_info.status)
            } else {
                print("激活机器人失败")
            }
        }
    }

    // announcement
    func updateNotice(with str: String) {
        ChatRoomServiceImp.getSharedInstance().updateAnnouncement(content: str) { result in
            if result {
                // 如果返回的结果为true 表示上麦成功
                self.view.makeToast("voice_notice_posted".voice_localized())
                self.roomInfo?.room?.announcement = str
            } else {
                self.view.makeToast("voice_post_failed".voice_localized())
            }
        }
    }

    func updateVolume(_ Vol: Int) {
        if isOwner == false { return }
        ChatRoomServiceImp.getSharedInstance().updateRobotVolume(value: Vol) { error in
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
        chatBar.raiseKeyboard = { [weak self] in
            self?.inputBar.isHidden = false
            self?.inputBar.inputField.becomeFirstResponder()
        }
        inputBar.sendClosure = { [weak self] in
            self?.sendTextMessage(text: $0)
        }
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
        chatBar.micStateChangeClosure = { [weak self] state in
            guard let self = self else { return }
            self.rtckit.enableinearmonitoring(enable: state == .selected ? false : self.roomInfo?.room?.turn_InEar ?? false)
        }
    }

    
    func showEndLive() {
        var compent = PresentedViewComponent(contentSize: CGSize(width: ScreenWidth - 70, height: 190))
        compent.destination = .center
        inputBar.hiddenInputBar()
        let micAlert = VoiceRoomEndLiveAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 70, height: 190), title: LanguageManager.localValue(key: "voice_end_live"), content: LanguageManager.localValue(key: "voice_the_room_will_close_after_you_leave."), cancel: LanguageManager.localValue(key: "voice_cancel"), confirm: LanguageManager.localValue(key: "voice_confirm")).cornerRadius(16).backgroundColor(.white)
        micAlert.cancel.accessibilityIdentifier = "voice_chat_room_end_live_cancel"
        micAlert.confirm.accessibilityIdentifier = "voice_chat_room_end_live_confirm"
        let vc = VoiceRoomAlertViewController(compent: compent, custom: micAlert)
        micAlert.actionEvents = { [weak self] in
            vc.dismiss(animated: true)
            if $0 != 30 {
                self?.quitRoom()
            }
        }
        presentViewController(vc)
    }
    
    func quitRoom(pop: Bool = true) {
        VoiceChatLog.info("Starting quit room process, pop=\(pop)")
        self.notifySeverLeave {
            VoiceChatLog.info("Leave Room")
            self.rtckit.leaveChannel()
            self.leaveRoom()
            VoiceRoomUserInfo.shared.currentRoomOwner = nil
            VoiceRoomUserInfo.shared.user?.amount = 0
            ChatRoomServiceImp.getSharedInstance().unsubscribeEvent()
            ChatRoomServiceImp.getSharedInstance().cleanCache()
            self.rtckit.stopPlayMusic()
            if pop {
                self.dismiss(animated: false)
                self.ownerBack()
            }
        }
    }

    func ownerBack() {
        self.leaveRoom()
        if let vc = navigationController?.viewControllers.filter({ $0 is VRRoomsViewController
        }).first {
            navigationController?.popToViewController(vc, animated: true)
        }
    }

    func showInviteMicAlert(index: Int?) {
        VoiceRoomPresentView.shared.dismiss()
        inputBar.hiddenInputBar()
        var compent = PresentedViewComponent(contentSize: CGSize(width: ScreenWidth - 75, height: 200))
        compent.destination = .center
        let micAlert = VoiceRoomApplyAlert(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 75, height: 200), content: "voice_anchor_invited_you_on_stage", cancel: "voice_decline", confirm: "voice_accept", position: .center).cornerRadius(16).backgroundColor(.white)
        let vc = VoiceRoomAlertViewController(compent: compent, custom: micAlert)
        micAlert.actionEvents = { [weak self] in
            if $0 == 30 {
                self?.refuse()
            } else {
                self?.agreeInvite(index: index)
            }
            vc.dismiss(animated: true)
        }
        presentViewController(vc)
    }
    
    @objc func updateMicInfo(noti: Notification){
        guard let obj: VRRoomMic = noti.object as? VRRoomMic else {return}
        self.rtcView.updateUser(obj)
    }
    
    func textHeight(text: String, fontSize: CGFloat, width: CGFloat) -> CGFloat {
        return text.boundingRect(with: CGSize(width: width, height: CGFloat(MAXFLOAT)), options: .usesLineFragmentOrigin, attributes: [.font: UIFont.systemFont(ofSize: fontSize)], context: nil).size.height + 5
    }

    private func getDetailTextHeight(_ effect: Int) -> CGFloat{
        var detailStr: String = ""
        switch effect {
        case 1:
            detailStr = "voice_chatroom_social_chat_introduce".voice_localized()
        case 2:
            detailStr = "voice_chatroom_karaoke_introduce".voice_localized()
        case 3:
            detailStr = "voice_chatroom_gaming_buddy_introduce".voice_localized()
        default:
            detailStr = "voice_chatroom_professional_broadcaster_introduce".voice_localized()
        }
        return textHeight(text: detailStr, fontSize: 13, width: self.view.bounds.size.width - 40)
    }
}

extension VoiceRoomViewController: VMMusicPlayerDelegate {
    func didMPKChangedTo(state: AgoraMediaPlayerState, reason: AgoraMediaPlayerReason) {
        
    }
}

// MARK: - SVGAPlayerDelegate

extension VoiceRoomViewController: SVGAPlayerDelegate {
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

extension VoiceRoomViewController: VMManagerDelegate {
    func didRtcLocalUserJoinedOfUid(uid: UInt) {
        
    }

    func didRtcRemoteUserJoinedOfUid(uid: UInt) {
        
    }

    func didRtcUserOfflineOfUid(uid: UInt) {}

    func reportAlien(with type: ALIEN_TYPE, musicType: VMMUSIC_TYPE) {
        rtcView.updateAlienMic(type)
    }

    func reportAudioVolumeIndicationOfSpeakers(speakers: [AgoraRtcAudioVolumeInfo]) {
        let micinfo = ChatRoomServiceImp.getSharedInstance().mics
        for speaker in speakers {
            for mic in micinfo where mic.member != nil{
                let user = mic.member
                guard let rtcUid = Int(user?.rtc_uid ?? "0") else { return }
                if rtcUid == speaker.uid {
                    DispatchQueue.main.async {
                        self.rtcView.updateVolume(with: mic.mic_index, vol: Int(speaker.volume))
                    }
                    break
                }
            }
        }
    }
}


extension VoiceRoomViewController {
    func checkEnterSeatAudioAuthorized() {
        //trigger once
        if isEnterSeatNotFirst {
            return
        }
        
        isEnterSeatNotFirst = true
        
        checkAudioAuthorized()
    }
    
    func checkAudioAuthorized(completion: ((Bool) -> Void)? = nil) {
        AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self, completion: completion)
    }
}
