//
//  VoiceRoomAudioSettingViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/1/31.
//

import UIKit

private enum AudioSettingRowType {
    case AINS
    case AIAEC
    case AGC
    case EarBack
    case SoundCard
    case Robots
    case RobotsVolume
    case BesetSoundEffect
    case Engine
}


class VoiceRoomAudioSettingViewController: VRBaseViewController {
    
    lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 56)).backgroundColor(.clear).setGradient([UIColor(red: 0.929, green: 0.906, blue: 1, alpha: 1), UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)])
    }()
    let presentView: VoiceRoomPresentView = VoiceRoomPresentView.shared
    private var screenWidth: CGFloat = UIScreen.main.bounds.size.width
    private var lineImgView: UIImageView = .init()
    private var titleLabel: UILabel = .init()
    public var tableView: UITableView = .init()
    public var isAudience: Bool = false
    public var isPrivate: Bool = false
    public var isTouchAble: Bool = false {
        willSet {
            self.detailVC?.isTouchAble = newValue
        }
    }
    var selTag: Int? {
        willSet {
            self.detailVC?.selTag = newValue
        }
    }
    weak var detailVC: VoiceRoomAudioSettingDetailViewController?
    private let swIdentifier = "switch"
    private let slIdentifier = "slider"
    private let nIdentifier = "normal"
    private let spdentifier = "surpport"
    private lazy var inEarView = VoiceRealInEarView()

//    private var settingName: [String] = ["\(LanguageManager.localValue(key: "blue")) & \(LanguageManager.localValue(key: "red"))", LanguageManager.localValue(key: "Robot Volume"), LanguageManager.localValue(key: "Best Sound"), "AINS", "Spatial Audio"]
//    private var settingImage: [String] = ["icons／set／jiqi", "icons／set／laba", "icons／set／zuijia", "icons／set／AINS", "icons／set／3D"]
    
    private let dataSource: [[AudioSettingRowType]] = 
    [
        [.AINS, .AIAEC, .AGC, .EarBack, .SoundCard],
        [.Robots, .RobotsVolume],
        [.BesetSoundEffect, .Engine]
    ]

    private var soundTitle: [String] = []
    private var ainsTitle: [String] = []
    private var rtcKit: VoiceRoomRTCManager?
    
    private lazy var actionView: ActionSheetManager = {
        let actionView = ActionSheetManager()
        let isOn = (roomInfo?.room?.turn_InEar ?? false)
        inEarView.isHidden = !isOn
        // 设置耳返音量
        setInEarVolumnBlock?(roomInfo?.room?.inEar_volume ?? 100)
        var inEar_volume = Double((roomInfo?.room?.inEar_volume ?? 100)) / 100.0
        var inEarMode = roomInfo?.room?.inEarMode ?? ""
        let earModes = ["自动".show_localized, "强制OpenSL".show_localized, "强制Oboe".show_localized]
        var inEarModeIndex = earModes.firstIndex(where: { $0 == inEarMode }) ?? 0
        let hasHeadset = HeadSetUtil.hasHeadset()
        let tipsTextColor = hasHeadset ? UIColor(hex: "#979CBB") : UIColor(hex: "#FF1216")
        // 查询自己有没有在麦上
        let seatUser = ChatRoomServiceImp.getSharedInstance().mics.first(where: { $0.member?.uid == VLUserCenter.user.id && $0.status != -1 })
        let tipsText = hasHeadset ? "开启耳返可实时听到自己的声音, 唱歌的时候及时调整".show_localized : "使用耳返必须插入耳机，当前未检测到耳机".show_localized
        actionView.title(title: "耳返".show_localized)
            .switchCell(title: "开启耳返".show_localized, isOn: hasHeadset ? isOn : false, isEnabel: hasHeadset && seatUser != nil, accessibilityIdentifier: "voice_chat_room_audio_setting_action_switch_inEar")
            .tipsCell(iconName: "inEra_tips_icon", title: tipsText, titleColor: tipsTextColor)
            .sectionHeader(title: "耳返设置".show_localized, desc: nil)
            .sliderCell(title: "耳返音量".show_localized, value: inEar_volume, isEnable: isOn, accessibilityIdentifier: "voice_chat_room_audio_setting_action_slider_inEar")
//                    .segmentCell(title: "耳返模式", items: earModes, selectedIndex: inEarModeIndex, isEnable: isOn)
//                    .customCell(customView: inEarView, viewHeight: 150)
            .config()
        actionView.backButtonAccessibilityIdentifier = "voice_chat_room_audio_setting_action_back_btn"
        actionView.didSwitchValueChangeClosure = { [weak self] _, isOn in
            self?.roomInfo?.room?.turn_InEar = isOn
            self?.actionView.updateSliderValue(indexPath: IndexPath(row: 0, section: 1), value: inEar_volume, isEnable: isOn)
//                    actionView.updateSegmentStatus(indexPath: IndexPath(row: 1, section: 1), selectedIndex: inEarModeIndex, isEnable: isOn)
            self?.inEarView.isHidden = !isOn
            self?.tableView.reloadData()
            self?.turnInearBlock?(isOn)

        }
        actionView.didSliderValueChangeClosure = { [weak self] _, value in
            let v = Int(value * 100)
            self?.roomInfo?.room?.inEar_volume = v
            inEar_volume = value
            self?.setInEarVolumnBlock?(v)
        }
        actionView.didSegmentValueChangeClosure = { [weak self] indexPath, mode, index in
            guard let self = self else { return }
            AUiAlertView.showCustomAlert(title: "提示".voice_localized(), message: String(format: "切换后将强制使用%@模式,确认?".voice_localized(), mode), confirm: {
                inEarModeIndex = earModes.firstIndex(where: { $0 == mode }) ?? 0
                self.roomInfo?.room?.inEarMode = mode
                inEarMode = mode
                self.setInEarModeBlock?(INEAR_MODE(rawValue: index) ?? .auto)
            }, cancel: {
                let index = earModes.firstIndex(where: { $0 == inEarMode }) ?? 0
                self.actionView.updateSegmentStatus(indexPath: indexPath, selectedIndex: index)
            })
        }
        
        // 监听耳机插入
        HeadSetUtil.addHeadsetObserver { hasHeadset in
            let isOn = (self.roomInfo?.room?.turn_InEar ?? false)
            // 查询自己有没有在麦上
            let seatUser = ChatRoomServiceImp.getSharedInstance().mics.first(where: { $0.member?.uid == VLUserCenter.user.id && $0.status != -1 })
            let switchIndexPath = IndexPath(row: 0, section: 0)
            actionView.updateSwitchStatus(indexPath: switchIndexPath, isOn: hasHeadset ? isOn : false, isEnable: hasHeadset && seatUser != nil)
            let tipsIndexPath = IndexPath(row: 1, section: 0)
            let tipsTextColor = hasHeadset ? UIColor(hex: "#979CBB") : UIColor(hex: "#FF1216")
            let tipsText = hasHeadset ? "开启耳返可实时听到自己的声音, 唱歌的时候及时调整".show_localized : "使用耳返必须插入耳机，当前未检测到耳机".show_localized
            actionView.updateTipsCellTitle(indexPath: tipsIndexPath, title: tipsText, titleColor: tipsTextColor)
            let sliderIndexPath = IndexPath(row: 0, section: 1)
            let inEar_volume = Double((self.roomInfo?.room?.inEar_volume ?? 0)) / 100.0
            actionView.updateSliderValue(indexPath: sliderIndexPath, value: inEar_volume, isEnable: hasHeadset && isOn)
            if hasHeadset == false && isOn {
                self.roomInfo?.room?.turn_InEar = false
            }
            self.turnInearBlock?(isOn == hasHeadset)
        }
        return actionView
    }()
    
    public var roomInfo: VRRoomInfo?
    public var ains_state: AINS_STATE = .mid {
        didSet {
            tableView.reloadData()
        }
    }
    
    var resBlock: ((AUDIO_SETTING_TYPE) -> Void)?
    var useRobotBlock: ((Bool) -> Void)?
    var volBlock: ((Int) -> Void)?
    var effectClickBlock: ((SOUND_TYPE) -> Void)?
    var visitBlock: (() -> Void)?
    var selBlock: ((AINS_STATE) -> Void)?
    var aedBlock: ((AED_STATE) -> Void)?
    var asptBlock: ((ASPT_STATE) -> Void)?
    var soundBlock: ((Int) -> Void)?
    var turnAIAECBlock:((Bool) ->Void)?
    var turnAGCBlock:((Bool) ->Void)?
    var turnInearBlock: ((Bool) -> Void)?
    var setInEarVolumnBlock: ((Int) -> Void)?
    var setInEarModeBlock: ((INEAR_MODE) -> Void)?
    
    //虚拟声卡相关
    public var soundOpen: Bool?
    
    public var aed_state: AED_STATE = .off
    public var aspt_state: ASPT_STATE = .off
    
    deinit {
        soundcardPresenter?.removeDelegate(self)
    }
    
    init(rtcKit: VoiceRoomRTCManager?) {
        super.init(nibName: nil, bundle: nil)
        self.rtcKit = rtcKit
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .white
        layoutUI()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.navigationBar.isHidden = true
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
//        VoiceRoomRTCManager.getSharedInstance().rtcKit.stopAudioMixing()
    }
    
    private func layoutUI() {
        navigation.back.isHidden = true
        
        let path = UIBezierPath(roundedRect: self.view.bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer = CAShapeLayer()
        layer.path = path.cgPath
        self.view.layer.mask = layer

        view.addSubview(cover)

        lineImgView.frame = CGRect(x: ScreenWidth / 2.0 - 20, y: 8, width: 40, height: 4)
        lineImgView.image = UIImage.sceneImage(name: "pop_indicator", bundleName: "VoiceChatRoomResource")
        view.addSubview(lineImgView)

        titleLabel.frame = CGRect(x: ScreenWidth / 2.0 - 60, y: 30, width: 120, height: 30)
        titleLabel.textAlignment = .center
        titleLabel.text = LanguageManager.localValue(key: "voice_audio_settings")
        titleLabel.textColor = UIColor(red: 0.016, green: 0.035, blue: 0.145, alpha: 1)
        titleLabel.font = UIFont.systemFont(ofSize: 16, weight: .bold)
        view.addSubview(titleLabel)
        print("\(self.view.bounds.size.height)")
        tableView.frame = CGRect(x: 0, y: 70, width: ScreenWidth, height: 650)
        tableView.registerCell(VMSwitchTableViewCell.self, forCellReuseIdentifier: swIdentifier)
        tableView.registerCell(VMSliderTableViewCell.self, forCellReuseIdentifier: slIdentifier)
        tableView.registerCell(VMNorSetTableViewCell.self, forCellReuseIdentifier: nIdentifier)
        tableView.registerCell(VMAudioSetEngineSurpportCell.self, forCellReuseIdentifier: spdentifier)
        tableView.dataSource = self
        tableView.delegate = self
        view.addSubview(tableView)
        tableView.tableFooterView = UIView()
        tableView.isScrollEnabled = false

        tableView.separatorColor = UIColor(hex: 0xF6F6F6, alpha: 1)

        if #available(iOS 15.0, *) {
            tableView.sectionHeaderTopPadding = 0
        } else {
            // Fallback on earlier versions
        }
        
        HeadSetUtil.addHeadsetObserver { hasHeadset in
            self.roomInfo?.room?.turn_InEar = hasHeadset == false ? false : self.roomInfo?.room?.turn_InEar
        }
    }
    
    private var soundcardPresenter: VirtualSoundcardPresenter? = nil
    public func setSoundCardPresenter(_ p: VirtualSoundcardPresenter) {
        soundcardPresenter = p
        soundOpen = p.getSoundCardEnable()
        p.addDelegate(self)
    }
}

extension VoiceRoomAudioSettingViewController: VirtualSoundcardPresenterDelegate {
    func onSoundcardPresenterValueChanged(isEnabled: Bool, presetValue: Int, gainValue: Int, presetSoundType: Int) {
        soundOpen = isEnabled
        tableView.reloadData()
    }
}

extension VoiceRoomAudioSettingViewController: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        return dataSource.count
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 54
    }

    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 32
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let items = dataSource[section]
        return items.count
    }

    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if section == 0 {
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 32))
            headerView.backgroundColor = UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 2, width: 300, height: 30))
            titleLabel.text = LanguageManager.localValue(key: "voice_personal_audio_settings")
            
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            headerView.addSubview(titleLabel)
            return headerView
        } else  if section == 1{
            let width = textAutoWidth(height: 300, font: UIFont.systemFont(ofSize: 13), text: LanguageManager.localValue(key: "voice_bot_settings"))
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 32))
            headerView.backgroundColor = UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 2, width: width, height: 30))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            titleLabel.text = LanguageManager.localValue(key: "voice_bot_settings")
            headerView.addSubview(titleLabel)

            let imgView: UIImageView = .init(frame: CGRect(x: width + 30, y: 6, width: 30, height: 20))
            imgView.contentMode = .scaleAspectFit
            imgView.image = UIImage.sceneImage(name: "new", bundleName: "VoiceChatRoomResource")
            headerView.addSubview(imgView)

            return headerView
        } else if section == 2 {
            let width = textAutoWidth(height: 300, font: UIFont.systemFont(ofSize: 13), text: LanguageManager.localValue(key: "voice_room_audio_settings"))
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 32))
            headerView.backgroundColor = UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 2, width: width, height: 30))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            titleLabel.text = LanguageManager.localValue(key: "voice_room_audio_settings")
            headerView.addSubview(titleLabel)

            let imgView: UIImageView = .init(frame: CGRect(x: width + 30, y: 6, width: 30, height: 20))
            imgView.contentMode = .scaleAspectFit
            imgView.image = UIImage.sceneImage(name: "new", bundleName: "VoiceChatRoomResource")
            headerView.addSubview(imgView)

            return headerView
        } else {
            return nil
        }
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        section == 2 ? 80.0 : 0.1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let items = dataSource[indexPath.section]
        let type = items[indexPath.row]
        if (type == .AINS) {
            guard let cell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as? VMNorSetTableViewCell else {
                return UITableViewCell()
            }
            cell.accessibilityIdentifier = "voice_chat_room_audio_setting_\(indexPath.section)_\(indexPath.row)"
            cell.iconView.image = UIImage.voice_image("AINS")
            cell.titleLabel.text = LanguageManager.localValue(key: "voice_AINS")
            switch ains_state {
            case .high:
                cell.contentLabel.text = "voice_ains_legacy_high".voice_localized()
            case .mid:
                cell.contentLabel.text = "voice_ains_legacy_low".voice_localized()
            case .off:
                cell.contentLabel.text = "voice_ains_off".voice_localized()
            case .aiMid:
                cell.contentLabel.text = "voice_ains_ai_low".voice_localized()
            case .aiHigh:
                cell.contentLabel.text = "voice_ains_ai_high".voice_localized()
            case .custom:
                cell.contentLabel.text = "voice_ains_custom".voice_localized()
            }
            return cell
        } else if (type == .AIAEC) {
            guard let cell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as? VMNorSetTableViewCell else {
                return UITableViewCell()
            }
            cell.accessibilityIdentifier = "voice_chat_room_audio_setting_\(indexPath.section)_\(indexPath.row)"
            cell.iconView.image = UIImage.voice_image("AIAEC")
            cell.titleLabel.text = LanguageManager.localValue(key: "voice_AIAEC")
            if roomInfo?.room?.turn_AIAEC == true {
                cell.contentLabel.text = "voice_on".voice_localized()
            } else {
                cell.contentLabel.text = "voice_off".voice_localized()
            }
            return cell
        } else if (type == .AGC) {
            guard let cell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as? VMNorSetTableViewCell else {
                return UITableViewCell()
            }
            cell.accessibilityIdentifier = "voice_chat_room_audio_setting_\(indexPath.section)_\(indexPath.row)"
            cell.iconView.image = UIImage.voice_image("AGC")
            cell.titleLabel.text = LanguageManager.localValue(key: "voice_AGC")
            if roomInfo?.room?.turn_AGC == true {
                cell.contentLabel.text = "voice_on".voice_localized()
            } else {
                cell.contentLabel.text = "voice_off".voice_localized()
            }
            return cell
        } else if (type == .EarBack) {
            guard let cell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as? VMNorSetTableViewCell else {
                return UITableViewCell()
            }
            cell.accessibilityIdentifier = "voice_chat_room_audio_setting_\(indexPath.section)_\(indexPath.row)"
            cell.iconView.image = UIImage.voice_image("InEar")
            cell.titleLabel.text = LanguageManager.localValue(key: "In-Ear Monitor")
            if roomInfo?.room?.turn_InEar == true {
                cell.contentLabel.text = "voice_on".voice_localized()
            } else {
                cell.contentLabel.text = "voice_off".voice_localized()
            }
            return cell
        } else if (type == .SoundCard) {
            guard let cell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as? VMNorSetTableViewCell else {
                return UITableViewCell()
            }
            cell.accessibilityIdentifier = "voice_chat_room_audio_setting_\(indexPath.section)_\(indexPath.row)"
            cell.iconView.image = UIImage.voice_image("icon-park-solid_people-speak")
            cell.titleLabel.text = LanguageManager.localValue(key: "voice_SoundCard")
            cell.contentLabel.text = self.soundOpen == true ? "voice_on".voice_localized() : "voice_off".voice_localized()
            
            return cell
        } else if (type == .Robots) {
            let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
            cell.accessibilityIdentifier = "voice_chat_room_audio_setting_\(indexPath.section)_\(indexPath.row)"
            cell.iconView.image = UIImage.voice_image("jiqi")
            cell.titleLabel.text = LanguageManager.localValue(key: "voice_agora_blue_and_red_bot")
            cell.swith.alpha = isAudience ? 0.5 : 1
            cell.swith.isUserInteractionEnabled = !isAudience
            cell.selectionStyle = .none
            cell.swith.isOn = roomInfo?.room?.use_robot ?? false
            cell.swith.accessibilityIdentifier = "voice_chat_room_audio_setting_agora_blue_red_bot_switch"
            cell.useRobotBlock = { [weak self] flag in
                guard let useRobotBlock = self?.useRobotBlock else { return }
                self?.isTouchAble = flag
                useRobotBlock(flag)
            }
            return cell
        } else if (type == .RobotsVolume) {
            let cell: VMSliderTableViewCell = tableView.dequeueReusableCell(withIdentifier: slIdentifier) as! VMSliderTableViewCell
            cell.accessibilityIdentifier = "voice_chat_room_audio_setting_\(indexPath.section)_\(indexPath.row)"
            cell.iconView.image = UIImage.voice_image("icons／set／laba")
            cell.titleLabel.text = LanguageManager.localValue(key: "voice_robot_volume")
            cell.isAudience = isAudience
            cell.selectionStyle = .none
            cell.volBlock = { [weak self] vol in
                guard let volBlock = self?.volBlock else { return }
                volBlock(vol)
            }
            
            let volume = roomInfo?.room?.robot_volume ?? 50
            cell.slider.value = Float(volume) / 100.0
            cell.countLabel.text = "\(volume)"
            cell.slider.accessibilityIdentifier = "voice_chat_room_audio_setting_robot_volume_slider"
            return cell
        } else if (type == .BesetSoundEffect) {
            guard let cell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as? VMNorSetTableViewCell else {
                return UITableViewCell()
            }
            cell.accessibilityIdentifier = "voice_chat_room_audio_setting_\(indexPath.section)_\(indexPath.row)"
            cell.iconView.image = UIImage.voice_image("icons／set／zuijia")
            cell.titleLabel.text = LanguageManager.localValue(key: "voice_best_agora_sound")
            cell.contentLabel.text = getSoundType(with: roomInfo?.room?.sound_effect ?? 1)
            return cell
        } else if (type == .Engine) {
            let cell = tableView.dequeueReusableCell(withIdentifier: spdentifier) ?? UITableViewCell()
            cell.accessibilityIdentifier = "voice_chat_room_audio_setting_\(indexPath.section)_\(indexPath.row)"
            return cell
        }
        return UITableViewCell()
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        var state: AUDIO_SETTING_TYPE = .Noise
        var heightType: TV_TYPE_HEIGHT = .AEC
        var tableViewHeight: CGFloat = 0
        if indexPath.section == 0 {
            switch indexPath.row {
            case 0:
                //AINS
                state = .Noise
                heightType = .ANS
                
            case 1:
                state = .AIAEC
                heightType = .AEC
            case 2:
                state = .AGC
                heightType = .AGC
            case 3:
                state = .InEar
                heightType = .InEar
                // 查询自己有没有在麦上
                let seatUser = ChatRoomServiceImp.getSharedInstance().mics.first(where: { $0.member?.uid == VLUserCenter.user.id && $0.status != -1 })
                if seatUser != nil {
                    actionView.show_voice()
                } else {
                    ToastView.show(text: "仅上麦用户可以使用耳返".voice_localized())
                }
                return
            default:
                state = .Spatial
            }
        }  else if indexPath.section == 2 {
            switch indexPath.row {
            case 0:
                //最佳音效
                state = .effect
                heightType = .EFFECT
                
            case 1: return
            default:
                state = .Spatial
            }
        } else if indexPath.section == 1 {
            return
        }
        
        //处理虚拟声卡的业务
        if indexPath.section == 0 && indexPath.row == 4 {
            
            let seatUser = ChatRoomServiceImp.getSharedInstance().mics.first(where: { $0.member?.uid == VLUserCenter.user.id && $0.status != -1 })
            if seatUser == nil {
                ToastView.show(text: "请上麦后使用该音效".voice_localized())
                return
            }
            
            let soundCardVC = VRSoundCardViewController()
            soundCardVC.soundcardPresenter = self.soundcardPresenter
            DispatchQueue.main.async {[weak self] in
                self?.presentView.push(with: soundCardVC, frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 400), maxHeight: 400)
            }
            return
        }
        
        tableViewHeight = heightType.rawValue - 70
        let detailVC: VoiceRoomAudioSettingDetailViewController = VoiceRoomAudioSettingDetailViewController()
        self.detailVC = detailVC
        detailVC.roomInfo = roomInfo
        detailVC.isAudience = isAudience
        detailVC.soundEffect = roomInfo?.room?.sound_effect ?? 1
        detailVC.settingType = state
        detailVC.ains_state = ains_state
        detailVC.aed_state = aed_state
        detailVC.aspt_state = aspt_state
        detailVC.isTouchAble = isTouchAble
        detailVC.tableViewHeight = tableViewHeight
        detailVC.selBlock = { [weak self] state in
            guard let selBlock = self?.selBlock else {
                return
            }
            self?.ains_state = state
            self?.tableView.reloadData()
            selBlock(state)
        }
        
        detailVC.aedBlock = { [weak self] state in
            guard let selBlock = self?.aedBlock else {
                return
            }
            self?.aed_state = state
            selBlock(state)
        }
        
        detailVC.asptBlock = { [weak self] state in
            guard let selBlock = self?.asptBlock else {
                return
            }
            self?.aspt_state = state
            selBlock(state)
        }
        
        detailVC.turnAIAECBlock = { [weak self] flag in
            guard let turnAIAECBlock = self?.turnAIAECBlock else {
                return
            }

            self?.roomInfo?.room?.turn_AIAEC = flag;
            self?.tableView.reloadData()
            turnAIAECBlock(flag)

            
        }
        detailVC.turnAGCBlock = { [weak self] flag in
            guard let turnAGCBlock = self?.turnAGCBlock else {
                return
            }
            self?.roomInfo?.room?.turn_AGC = flag;
            self?.tableView.reloadData()
            turnAGCBlock(flag);

        }
        detailVC.soundBlock = { [weak self] index in
            guard let soundBlock = self?.soundBlock else {
                return
            }
            soundBlock(index)
        }
        
        detailVC.effectClickBlock = { [weak self] type in
            guard let effectClickBlock = self?.effectClickBlock else {
                return
            }
            effectClickBlock(type)
        }
        detailVC.visitBlock = {[weak self] in
            guard let visitBlock = self?.visitBlock else {
                return
            }
            visitBlock()
        }
        DispatchQueue.main.async {[weak self] in
            self?.presentView.push(with: detailVC, frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 454), maxHeight: heightType.rawValue)
        }
    }
    
    func textAutoWidth(height: CGFloat, font: UIFont, text: String) -> CGFloat {
        let origin = NSStringDrawingOptions.usesLineFragmentOrigin
        let lead = NSStringDrawingOptions.usesFontLeading
        let rect = text.boundingRect(with: CGSize(width: 0, height: height), options: [origin, lead], attributes: [NSAttributedString.Key.font: font], context: nil)
        return rect.width
    }
    
    private func getSoundType(with index: Int) -> String {
        var soundType: String = "voice_social_chat".voice_localized()
        switch index {
        case 1:
            soundType = "voice_social_chat".voice_localized()
        case 2:
            soundType = "voice_karaoke".voice_localized()
        case 3:
            soundType = "voice_gaming_buddy".voice_localized()
        case 4:
            soundType = "voice_professional_podcaster".voice_localized()
        default:
            soundType = "voice_social_chat".voice_localized()
        }
        return soundType
    }
}
