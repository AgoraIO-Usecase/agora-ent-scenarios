//
//  VoiceRoomAudioSettingDetailViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/1/31.
//

import UIKit
//不同场景的视图高度
public enum TV_TYPE_HEIGHT: CGFloat {
    case ANS = 920
    case AEC = 455
    case AGC = 454
    case InEar = 400
    case EFFECT = 921
    case Music = 500
}

class VoiceRoomAudioSettingDetailViewController: UIViewController {
    private var screenWidth: CGFloat = UIScreen.main.bounds.size.width
    private var screenHeight: CGFloat = UIScreen.main.bounds.size.height
    private var lineImgView: UIImageView = .init()
    private var titleLabel: UILabel = .init()
    private var tableView: UITableView = .init(frame: .zero, style: .grouped)
    private var backBtn: UIButton = .init()
    public var roomInfo: VRRoomInfo?
    lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 56)).backgroundColor(.clear).setGradient([UIColor(red: 0.929, green: 0.906, blue: 1, alpha: 1), UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)])
    }()
    let presentView: VoiceRoomPresentView = VoiceRoomPresentView.shared
    private let swIdentifier = "switch"
    private let slIdentifier = "slider"
    private let nIdentifier = "normal"
    private let pIdentifier = "sup"
    private let sIdentifier = "set"
    private let soIdentifier = "sound"
    private let tIdentifier = "tv"
    private var effectHeight: [CGFloat] = [0, 0, 0, 0]
    private var effectType: [SOUND_TYPE] = [.chat, .karaoke, .game, .anchor]
    var effectClickBlock: ((SOUND_TYPE) -> Void)?
    var visitBlock: (() -> Void)?
    var isTouchAble: Bool = false
    var isAudience: Bool = false
    var ains_state: AINS_STATE = .off {
        didSet {
            tableView.reloadData()
        }
    }
    
    public var aed_state: AED_STATE = .off
    public var aspt_state: ASPT_STATE = .off

    var soundEffect: Int = 1 {
        didSet {

            let socialH: CGFloat = textHeight(text: LanguageManager.localValue(key: "voice_chatroom_social_chat_introduce"), fontSize: 13, width: self.view.bounds.size.width - 80)
            let ktvH: CGFloat = textHeight(text: LanguageManager.localValue(key: "voice_chatroom_karaoke_introduce"), fontSize: 13, width: self.view.bounds.size.width - 80)
            let gameH: CGFloat = textHeight(text: LanguageManager.localValue(key: "voice_chatroom_gaming_buddy_introduce"), fontSize: 13, width: self.view.bounds.size.width - 80)
            let anchorH: CGFloat = textHeight(text: LanguageManager.localValue(key: "voice_chatroom_professional_broadcaster_introduce"), fontSize: 13, width: self.view.bounds.size.width - 80)
            print("\(soundEffect)-----")
            switch soundEffect {
            case 1:
                effectHeight = [socialH, ktvH, gameH, anchorH]
                effectType = [.chat, .karaoke, .game, .anchor]
            case 2:
                effectHeight = [ktvH, socialH, gameH, anchorH]
                effectType = [.karaoke, .chat, .game, .anchor]
            case 3:
                effectHeight = [gameH, socialH, ktvH, anchorH]
                effectType = [.game, .chat, .karaoke, .anchor]
            case 4:
                effectHeight = [anchorH, socialH, ktvH, gameH]
                effectType = [.anchor, .chat, .karaoke, .game]
            default:
                break
            }
        }
    }

    var selBlock: ((AINS_STATE) -> Void)?
    var aedBlock: ((AED_STATE) -> Void)?
    var asptBlock: ((ASPT_STATE) -> Void)?
    var soundBlock: ((Int) -> Void)?
    var turnAIAECBlock:((Bool) ->Void)?
    var turnAGCBlock:((Bool) ->Void)?

    var selTag: Int? {
        didSet {
            if self.selTag != nil {
                self.tableView.reloadData()
            }
        }
    }

    private let settingName: [String] = ["voice_spatial_audio", "Attenuation factor", "Air absorb", "Voice blur"]
    
    private let AIAECSettingName: [String] = ["voice_turn_on_AIAEC".voice_localized()]
    
    private let AGCSettingName: [String] = ["voice_turn_on_AGC".voice_localized()]

    
    private let soundType: [String] = ["voice_TV_sound".voice_localized(), "voice_kitchen_sound".voice_localized(), "voice_street_sound".voice_localized(), "voice_mashine_sound".voice_localized(), "voice_office_sound".voice_localized(), "voice_home_sound".voice_localized(), "voice_construction_sound".voice_localized(), "voice_alert_sound/Music".voice_localized(), "voice_applause".voice_localized(), "voice_wind_sound".voice_localized(), "voice_mic_pop_filter".voice_localized(), "voice_audio_feedback".voice_localized(), "voice_microphone_finger_rub_sound".voice_localized(), "voice_screen_tap_sound".voice_localized()]
    
    private let soundDetail: [String] = ["voice_ex_bird_car_subway_sounds".voice_localized(),
                                         "voice_ex_fan_air_conditioner_vacuum_cleaner_printer_sounds".voice_localized(),
                                         "voice_ex_keyboard_tapping_mouse_clicking_sounds".voice_localized(),
                                         "voice_ex_door_closing_chair_squeaking_baby_crying_sounds".voice_localized(),
                                         "voice_ex_knocking_sound".voice_localized()]

    var settingType: AUDIO_SETTING_TYPE = .Spatial {
        didSet {
            if settingType == .Spatial {
                titleLabel.text = "Spatial Setting".voice_localized()
                backBtn.accessibilityIdentifier = "voice_chat_room_audio_setting_back_Spatial"
            } else if settingType == .Noise {
                titleLabel.text = "voice_noise_setting".voice_localized()
                backBtn.accessibilityIdentifier = "voice_chat_room_audio_setting_back_Noise"
            } else if settingType == .effect {
                titleLabel.text = "voice_effect_setting".voice_localized()
                backBtn.accessibilityIdentifier = "voice_chat_room_audio_setting_back_effect"
            } else if settingType == .AIAEC {
                titleLabel.text = "voice_AIAEC".voice_localized()
                backBtn.accessibilityIdentifier = "voice_chat_room_audio_setting_back_AIAEC"
            } else if settingType == .AGC {
                titleLabel.text = "voice_AGC".voice_localized()
                backBtn.accessibilityIdentifier = "voice_chat_room_audio_setting_back_AGC"
            }
            print("\(titleLabel.text!)  \(backBtn.accessibilityIdentifier!)")
            if tableView.tableFooterView == nil {
                switch settingType {
                case .AGC,.AIAEC:
                    tableView.tableFooterView = AuditionEffectView(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 162), type: self.settingType)
                default:
                    tableView.tableFooterView = UIView()
                }
            }
            tableView.reloadData()
        }
    }
    
    var tableViewHeight: CGFloat = 0 {
        didSet {
            
        }
    }

    lazy var otherSoundHeaderHeight: CGFloat = textHeight(text: "voice_otherSound".voice_localized(), fontSize: 12, width: ScreenWidth - 100)

    var resBlock: ((AUDIO_SETTING_TYPE) -> Void)?

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        layoutUI()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        tableView.frame = CGRect(x: 0, y: 70, width: ScreenWidth, height: tableViewHeight > screenHeight ? screenHeight - 70 : tableViewHeight)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
    }
    
    private func layoutUI() {
        let path = UIBezierPath(roundedRect: self.view.bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer = CAShapeLayer()
        layer.path = path.cgPath
        self.view.layer.mask = layer

        view.addSubview(cover)

        backBtn.setImage(UIImage.sceneImage(name: "back", bundleName: "VoiceChatRoomResource"), for: .normal)
        backBtn.addTargetFor(self, action: #selector(back), for: .touchUpInside)
        view.addSubview(backBtn)
        backBtn.translatesAutoresizingMaskIntoConstraints = false

        lineImgView.frame = CGRect(x: ScreenWidth / 2.0 - 20, y: 8, width: 40, height: 4)
        lineImgView.image = UIImage.sceneImage(name: "pop_indicator", bundleName: "VoiceChatRoomResource")
        view.addSubview(lineImgView)

        titleLabel.frame = CGRect(x: ScreenWidth / 2.0 - 60, y: 25, width: 120, height: 30)
        titleLabel.textAlignment = .center
        titleLabel.text = "voice_spatial_audio".voice_localized()
        titleLabel.textColor = UIColor(hex: 0x040925, alpha: 1)
        titleLabel.font = UIFont.systemFont(ofSize: 16, weight: .bold)
        view.addSubview(titleLabel)
        
        backBtn.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10).isActive = true
        backBtn.centerYAnchor.constraint(equalTo: titleLabel.centerYAnchor).isActive = true

        tableView.frame = .zero
        tableView.registerCell(VMSwitchTableViewCell.self, forCellReuseIdentifier: swIdentifier)
        tableView.registerCell(VMSliderTableViewCell.self, forCellReuseIdentifier: slIdentifier)
        tableView.registerCell(VMNorSetTableViewCell.self, forCellReuseIdentifier: nIdentifier)
        tableView.registerCell(VMANISSUPTableViewCell.self, forCellReuseIdentifier: pIdentifier)
        tableView.registerCell(VMANISSetTableViewCell.self, forCellReuseIdentifier: sIdentifier)
        tableView.registerCell(VMSoundSelTableViewCell.self, forCellReuseIdentifier: soIdentifier)
        tableView.registerCell(UITableViewCell.self, forCellReuseIdentifier: tIdentifier)
        tableView.dataSource = self
        tableView.delegate = self
        view.addSubview(tableView)
        tableView.isScrollEnabled = false
        tableView.showsVerticalScrollIndicator = false

        if #available(iOS 15.0, *) {
            tableView.sectionHeaderTopPadding = 0
        } else {
            // Fallback on earlier versions
        }
    }

    @objc private func back() {
        self.presentView.pop()
    }

}

extension VoiceRoomAudioSettingDetailViewController: UITableViewDelegate, UITableViewDataSource, UITextViewDelegate {
    func numberOfSections(in tableView: UITableView) -> Int {
        if settingType == .Noise {
            return 3
        } else if settingType == .AIAEC {
            return 1;
        } else if settingType == .AGC {
            return 1;
        } else {
            return 2;
        }
//        return settingType == .Noise ? 3 : 2
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        switch settingType {
        case .effect:
            if indexPath.section == 0 {
                return effectHeight[0] + 132
            } else {
                return effectHeight[indexPath.row + 1] + 132
            }

        case .Noise:
            if indexPath.row > 1 && indexPath.row < 7 && indexPath.section != 0 {
                return 74
            } else {
                return 54
            }
        case .AIAEC:
            return 54
        case .AGC:
            return 54
        case .Spatial:
            return 54
        case .InEar:
            return 54
        case .SoundCard:
            return 54
        }
    }

    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if settingType == .Noise && section == 2 {
            return textHeight(text: "voice_AINS_sup".voice_localized(), fontSize: 13, width: ScreenWidth - 40) + 15
        } else if settingType == .effect && section == 1 {
            return 40 + 12 + otherSoundHeaderHeight + 10
        } else if settingType == .AIAEC || settingType == .AGC {
            return 0;
        }
        else {
            return 40
        }
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if settingType == .effect {
            return section == 0 ? 1 : 3
        } else if settingType == .Spatial {
            return 4
        } else if settingType == .AIAEC {
            return 1;
        } else if settingType == .AGC {
            return 1;
        }
        else {
            switch section {
            case 0:
                return ains_state != .off ? 3 : 1
            case 1:
                return 1
            default:
                return 14
            }
        }
    }

    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        if settingType == .effect && section == 1 {
            return 40
        } else if settingType == .AIAEC || settingType == .AGC {
            var detailStr: String = ""
            if settingType == .AIAEC {
                detailStr = "voice_AIAEC_desc".voice_localized()
            } else {
                detailStr = "voice_AGC_desc".voice_localized()
            }
            return textHeight(text: detailStr, fontSize: 13, width: screenWidth - 40) + 20;
        }
        return 0
    }

    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        if settingType == .AIAEC {
            let height: CGFloat = textHeight(text: "voice_AIAEC_desc".voice_localized(), fontSize: 13, width: screenWidth - 40) + 10
            let footer: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: height))
            footer.backgroundColor = settingType == .AIAEC ? .white : UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 0, width: screenWidth-40, height: height))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.numberOfLines = 0
            titleLabel.text = "voice_AIAEC_desc".voice_localized()
            titleLabel.textColor = UIColor(red: 60 / 255.0, green: 66 / 255.0, blue: 103 / 255.0, alpha: 1)
            footer.addSubview(titleLabel)
            return footer
        } else if settingType == .AGC {
            let height: CGFloat = textHeight(text: "voice_AGC_desc".voice_localized(), fontSize: 13, width: screenWidth - 40) + 10
            let footer: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: height))
            footer.backgroundColor = settingType == .AIAEC ? .white : UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 0, width: screenWidth-40, height: height))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.numberOfLines = 0
            titleLabel.text = "voice_AGC_desc".voice_localized()
            titleLabel.textColor = UIColor(red: 60 / 255.0, green: 66 / 255.0, blue: 103 / 255.0, alpha: 1)
            footer.addSubview(titleLabel)
            return footer
            
        } else {
            let footer = UIView(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 40))
            footer.backgroundColor = .white
            let textView = UITextView(frame: CGRect(x: 30, y: 0, width: screenWidth - 60, height: 40))

            let text = NSMutableAttributedString(string: "voice_visit_more".voice_localized())
            text.addAttribute(NSAttributedString.Key.font,
                              value: UIFont.systemFont(ofSize: 13),
                              range: NSRange(location: 0, length: text.length))
            text.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor(hex: 0x3C4267, alpha: 1), range: NSRange(location: 0, length: text.length))

            let interactableText = NSMutableAttributedString(string: "www.shengwang.cn")
            interactableText.addAttribute(NSAttributedString.Key.font,
                                          value: UIFont.systemFont(ofSize: 12, weight: .bold),
                                          range: NSRange(location: 0, length: interactableText.length))

            interactableText.addAttribute(NSAttributedString.Key.underlineStyle, value: 1, range: NSRange(location: 0, length: interactableText.length))

            // Adding the link interaction to the interactable text
            interactableText.addAttribute(NSAttributedString.Key.link,
                                          value: "SignInPseudoLink",
                                          range: NSRange(location: 0, length: interactableText.length))
            interactableText.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor(hex: 0x3C4267, alpha: 1), range: NSRange(location: 0, length: interactableText.length))
            text.append(interactableText)
            let paragraph = NSMutableParagraphStyle()
            paragraph.alignment = .center
            text.addAttribute(.paragraphStyle, value: paragraph, range: NSRange(location: 0, length: text.length))
            textView.attributedText = text

            textView.isEditable = false
            textView.isSelectable = true
            textView.delegate = self

            footer.addSubview(textView)

            return footer
        }
    }

    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if section == 0 {
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 40))

            headerView.backgroundColor = settingType == .effect ? .white : UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 5, width: 300, height: 30))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            if settingType == .effect {
                titleLabel.text = "voice_current_sound".voice_localized()
                titleLabel.textColor = UIColor(red: 60 / 255.0, green: 66 / 255.0, blue: 103 / 255.0, alpha: 1)
            } else if settingType == .Spatial {
                titleLabel.text = "voice_blue".voice_localized()
                titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            } else if settingType == .AIAEC {
                
            } else if settingType == .AGC {
        

            } else {
                titleLabel.text = "voice_AINS_settings".voice_localized()
                titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            }
            headerView.addSubview(titleLabel)
            return headerView
        } else if section == 1 {
            let headerHeight: CGFloat = (section == 1 && settingType == .effect) ? 40 + 12 + otherSoundHeaderHeight + 10 : 40
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: headerHeight))
            headerView.backgroundColor = settingType == .effect ? .white : UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 5, width: 300, height: 30))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            if settingType == .effect {
                titleLabel.textColor = UIColor(red: 60 / 255.0, green: 66 / 255.0, blue: 103 / 255.0, alpha: 1)
                titleLabel.text = "voice_other_sound".voice_localized()
                headerView.addSubview(titleLabel)

                if section == 1 {
                    let warningView = UIView(frame: CGRect(x: 20, y: 40, width: screenWidth - 40, height: 12 + otherSoundHeaderHeight))
                    warningView.layer.cornerRadius = 5
                    warningView.layer.masksToBounds = true
                    warningView.backgroundColor = UIColor(hex: 0xFFF7DC, alpha: 1)
                    headerView.addSubview(warningView)

                    let iconView: UIImageView = .init(frame: CGRect(x: 8, y: 7, width: 16, height: 16))
                    iconView.image = UIImage.sceneImage(name: "zhuyi", bundleName: "VoiceChatRoomResource")
                    warningView.addSubview(iconView)

                    let warningLabel = UILabel(frame: CGRect(x: 30, y: 6, width: screenWidth - 100, height: otherSoundHeaderHeight))
                    warningLabel.text = "voice_otherSound".voice_localized()
                    warningLabel.numberOfLines = 0
                    warningLabel.lineBreakMode = .byCharWrapping
                    warningLabel.font = UIFont.systemFont(ofSize: 12)
                    warningLabel.textColor = UIColor(hex: 0xE76D21, alpha: 1)
                    warningView.addSubview(warningLabel)
                }
            } else {
                titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
                titleLabel.text = settingType == .Spatial ? "voice_red".voice_localized() : "voice_AINS_definition".voice_localized()
                headerView.addSubview(titleLabel)
            }

            return headerView
        } else {
            let height = textHeight(text: "voice_AINS_sup".voice_localized(), fontSize: 13, width: ScreenWidth - 40)
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: height + 15))
            headerView.backgroundColor = UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 5, width: screenWidth - 40, height: height))
            titleLabel.numberOfLines = 0
            titleLabel.lineBreakMode = .byCharWrapping
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            titleLabel.text = "voice_AINS_sup".voice_localized()
            headerView.addSubview(titleLabel)
            return headerView
        }
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
         if settingType == .effect {
             tableView.separatorStyle = .none
             var cellType: SOUND_TYPE = .chat
             var cellHeight: CGFloat = 0
             if indexPath.section == 0 {
                 cellType = effectType[0]
                 cellHeight = effectHeight[0]
             } else {
                 cellType = effectType[indexPath.row + 1]
                 cellHeight = effectHeight[indexPath.row + 1]
             }

             let cell: VMSoundSelTableViewCell = .init(style: .default, reuseIdentifier: soIdentifier, cellType: cellType, cellHeight: cellHeight)
             cell.isSel = indexPath.section == 0
             return cell
         } else if settingType == .Spatial {
             if indexPath.row == 1 {
                 let cell: VMSliderTableViewCell = tableView.dequeueReusableCell(withIdentifier: slIdentifier) as! VMSliderTableViewCell
                 cell.isNoiseSet = true
                 cell.titleLabel.text = settingName[indexPath.row]
                 return cell
             } else {
                 let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
                 cell.isNoiseSet = true
                 cell.titleLabel.text = settingName[indexPath.row]
                 cell.swith.accessibilityIdentifier = "voice_chat_room_audio_setting_switch_Spatial"
                 return cell
             }
         } else if settingType == .AIAEC {
             let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
             cell.titleLabel.text = AIAECSettingName[indexPath.row]
             cell.selectionStyle = .none
             cell.swith.isOn = roomInfo?.room?.turn_AIAEC ?? false
             cell.useRobotBlock = { [weak self] flag in
                 guard let turnAIAECBlock = self?.turnAIAECBlock else { return }
                 turnAIAECBlock(flag)
//                 guard let backBlock = self?.backBlock else {return}
//                 backBlock();
             }
             cell.swith.accessibilityIdentifier = "voice_chat_room_audio_setting_switch_AIAEC"
             return cell

         } else if settingType == .InEar {
             let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
             cell.titleLabel.text = AIAECSettingName[indexPath.row]
             cell.selectionStyle = .none
             cell.swith.isOn = roomInfo?.room?.turn_AIAEC ?? false
             cell.useRobotBlock = { [weak self] flag in
                 guard let turnAIAECBlock = self?.turnAIAECBlock else { return }
                 turnAIAECBlock(flag)
//                 guard let backBlock = self?.backBlock else {return}
//                 backBlock();
             }
             cell.swith.accessibilityIdentifier = "voice_chat_room_audio_setting_switch_InEar"
             return cell

         }  else if settingType == .AGC {
             let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
             cell.titleLabel.text = AGCSettingName[indexPath.row]
             cell.selectionStyle = .none
             cell.swith.isOn = roomInfo?.room?.turn_AGC ?? false
             cell.useRobotBlock = { [weak self] flag in
                 guard let turnAGCBlock = self?.turnAGCBlock else { return }
                 turnAGCBlock(flag)
//                 guard let backBlock = self?.backBlock else {return}
//                 backBlock();
             }
             cell.swith.accessibilityIdentifier = "voice_chat_room_audio_setting_switch_AGC"
             return cell
         }
        else {
             if indexPath.section == 0 {
                 let cell: VMANISSetTableViewCell = tableView.dequeueReusableCell(withIdentifier: sIdentifier) as! VMANISSetTableViewCell
                 if indexPath.row == 0 {
                     cell.cellType = .ns
                     cell.ains_state = ains_state
                     cell.selectionStyle = .none
                     cell.isTouchAble = isTouchAble
                     cell.selBlock = { [weak self] state in
                         self?.ains_state = state
                         guard let block = self?.selBlock else { return }
                         block(state)
                     }
                     cell.selectionStyle = .none
                 } else if indexPath.row == 1 {
                     cell.cellType = .music
                     cell.aed_state = aed_state
                     cell.selectionStyle = .none
                     cell.aedBlock = {  [weak self] state in
                         self?.aed_state = state
                         guard let block = self?.aedBlock else { return }
                         block(state)
                     }
                 } else if indexPath.row == 2 {
                     cell.cellType = .voice
                     cell.aspt_state = aspt_state
                     cell.selectionStyle = .none
                     cell.asptBlock = {  [weak self] state in
                         self?.aspt_state = state
                         guard let block = self?.asptBlock else { return }
                         block(state)
                     }
                 }
                 return cell
             } else if indexPath.section == 1 {
                 let cell: UITableViewCell = tableView.dequeueReusableCell(withIdentifier: tIdentifier)!
                 cell.textLabel?.text = "voice_AINS_AI_noise_suppression".voice_localized()
                 cell.textLabel?.font = UIFont.systemFont(ofSize: 13)
                 cell.textLabel?.textColor = UIColor(hex: 0x3C4267, alpha: 1)
                 cell.isUserInteractionEnabled = false
                 cell.selectionStyle = .none
                 return cell
             } else {
                 let cell: VMANISSUPTableViewCell = tableView.dequeueReusableCell(withIdentifier: pIdentifier)! as! VMANISSUPTableViewCell
                 if indexPath.row > 1 && indexPath.row < 7 {
                     cell.detailLabel.text = soundDetail[indexPath.row - 2]
                     cell.cellType = .detail
                 } else {
                     cell.cellType = .normal
                 }
                 cell.isTouchAble = isTouchAble
                 cell.isAudience = isAudience
                 cell.selectionStyle = .none
                 cell.titleLabel.text = soundType[indexPath.row]
                 cell.cellTag = 1000 + indexPath.row * 10
                 if selTag == nil {
                     cell.btn_state = .none
                 } else {
                     let index = (selTag! - 1000) / 10
                     let tag = (selTag! - 1000) % 10
                     if index == indexPath.row {
                         cell.btn_state = tag == 1 ? .off : .middle
                     } else {
                         cell.btn_state = .none
                     }
                 }
                 cell.resBlock = { [weak self] index in
                     if cell.isTouchAble {
                         self?.selTag = index
                         self?.tableView.reloadData()
                     }
                     self?.soundBlock!(index)
                 }
                 return cell
             }
         }
     }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if settingType == .effect {
            guard let effectClickBlock = effectClickBlock else { return }
            if indexPath.section == 0 {
                effectClickBlock(effectType[0])
            } else {
                effectClickBlock(.none)
            }
        } else if settingType == .AIAEC {
            
        } else if settingType == .AGC {
            
        } else {
            print("other settingType")
        }
    }

    func textHeight(text: String, fontSize: CGFloat, width: CGFloat) -> CGFloat {
        return text.boundingRect(with: CGSize(width: width, height: CGFloat(MAXFLOAT)), options: .usesLineFragmentOrigin, attributes: [.font: UIFont.systemFont(ofSize: fontSize)], context: nil).size.height + 5
    }

    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange) -> Bool {
        guard let visitBlock = visitBlock else { return true }
        visitBlock()

        return true
    }
}
