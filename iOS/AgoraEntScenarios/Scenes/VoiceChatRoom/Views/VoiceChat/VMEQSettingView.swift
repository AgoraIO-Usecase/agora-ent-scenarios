//
//  VMEQSettingView.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/8.
//

import UIKit
import SwiftUI

class VMEQSettingView: UIView, UITextViewDelegate {
    private var screenWidth: CGFloat = UIScreen.main.bounds.size.width
    private var lineImgView: UIImageView = .init()
    private var titleLabel: UILabel = .init()
    private var tableView: UITableView = .init(frame: .zero, style: .grouped)
    private var backBtn: UIButton = .init()
    public var roomInfo: VRRoomInfo?
    lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 56)).backgroundColor(.clear).setGradient([UIColor(red: 0.929, green: 0.906, blue: 1, alpha: 1), UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)])
    }()

    private let swIdentifier = "switch"
    private let slIdentifier = "slider"
    private let nIdentifier = "normal"
    private let pIdentifier = "sup"
    private let sIdentifier = "set"
    private let soIdentifier = "sound"
    private let tIdentifier = "tv"
    private var effectHeight: [CGFloat] = [0, 0, 0, 0]
    private var effectType: [SOUND_TYPE] = [.chat, .karaoke, .game, .anchor]
    var backBlock: (() -> Void)?
    var effectClickBlock: ((SOUND_TYPE) -> Void)?
    var visitBlock: (() -> Void)?
    var isTouchAble: Bool = false
    var isAudience: Bool = false
    var ains_state: AINS_STATE = .off {
        didSet {
            tableView.reloadData()
        }
    }

    var soundEffect: Int = 1 {
        didSet {

            let socialH: CGFloat = textHeight(text: LanguageManager.localValue(key: "voice_chatroom_social_chat_introduce"), fontSize: 13, width: bounds.size.width - 80)
            let ktvH: CGFloat = textHeight(text: LanguageManager.localValue(key: "voice_chatroom_karaoke_introduce"), fontSize: 13, width: bounds.size.width - 80)
            let gameH: CGFloat = textHeight(text: LanguageManager.localValue(key: "voice_chatroom_gaming_buddy_introduce"), fontSize: 13, width: bounds.size.width - 80)
            let anchorH: CGFloat = textHeight(text: LanguageManager.localValue(key: "voice_chatroom_professional_broadcaster_introduce"), fontSize: 13, width: bounds.size.width - 80)
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
    var soundBlock: ((Int) -> Void)?
    var turnAIAECBlock:((Bool) ->Void)?
    var turnAGCBlock:((Bool) ->Void)?

    private var selTag: Int?

    private let settingName: [String] = ["voice_spatial_audio", "Attenuation factor", "Air absorb", "Voice blur"]
    
    private let AIAECSettingName: [String] = ["voice_turn_on_AIAEC".localized()]
    private let AGCSettingName: [String] = ["voice_turn_on_AGC".localized()]

    
    private let soundType: [String] = ["voice_TV_sound".localized(), "voice_kitchen_sound".localized(), "voice_street_sound".localized(), "voice_mashine_sound".localized(), "voice_office_sound".localized(), "voice_home_sound".localized(), "voice_construction_sound".localized(), "voice_alert_sound/Music".localized(), "voice_applause".localized(), "voice_wind_sound".localized(), "voice_mic_pop_filter".localized(), "voice_audio_feedback".localized(), "voice_microphone_finger_rub_sound".localized(), "voice_screen_tap_sound".localized()]
    
    private let soundDetail: [String] = ["voice_ex_bird_car_subway_sounds".localized(),
                                         "voice_ex_fan_air_conditioner_vacuum_cleaner_printer_sounds".localized(),
                                         "voice_ex_keyboard_tapping_mouse_clicking_sounds".localized(),
                                         "voice_ex_door_closing_chair_squeaking_baby_crying_sounds".localized(),
                                         "voice_ex_knocking_sound".localized()]

    var settingType: AUDIO_SETTING_TYPE = .Spatial {
        didSet {
            if settingType == .Spatial {
                titleLabel.text = "Spatial Setting".localized()
            } else if settingType == .Noise {
                titleLabel.text = "voice_noise_setting".localized()
            } else if settingType == .effect {
                titleLabel.text = "voice_effect_setting".localized()
            } else if settingType == .AIAEC {
                titleLabel.text = "voice_AIAEC".localized()
            } else if settingType == .AGC {
                titleLabel.text = "voice_AGC".localized()

            }
            tableView.reloadData()
        }
    }

    lazy var otherSoundHeaderHeight: CGFloat = textHeight(text: "voice_otherSound".localized(), fontSize: 12, width: ScreenWidth - 100)

    var resBlock: ((AUDIO_SETTING_TYPE) -> Void)?

    override func draw(_ rect: CGRect) {
        super.draw(rect)
        backgroundColor = .white
        layoutUI()
    }

    private func layoutUI() {
        let path = UIBezierPath(roundedRect: bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer = CAShapeLayer()
        layer.path = path.cgPath
        self.layer.mask = layer

        addSubview(cover)

        backBtn.frame = CGRect(x: 10, y: 30, width: 20, height: 30)
        backBtn.setImage(UIImage.sceneImage(name: "back", bundleName: "VoiceChatRoomResource"), for: .normal)
        backBtn.addTargetFor(self, action: #selector(back), for: .touchUpInside)
        addSubview(backBtn)

        lineImgView.frame = CGRect(x: ScreenWidth / 2.0 - 20, y: 8, width: 40, height: 4)
        lineImgView.image = UIImage.sceneImage(name: "pop_indicator", bundleName: "VoiceChatRoomResource")
        addSubview(lineImgView)

        titleLabel.frame = CGRect(x: ScreenWidth / 2.0 - 60, y: 25, width: 120, height: 30)
        titleLabel.textAlignment = .center
        titleLabel.text = "Spatial Audio"
        titleLabel.textColor = UIColor.HexColor(hex: 0x040925, alpha: 1)
        titleLabel.font = UIFont.systemFont(ofSize: 16, weight: .bold)
        addSubview(titleLabel)

        tableView.frame = CGRect(x: 0, y: 70, width: ScreenWidth, height: 280)
        tableView.registerCell(VMSwitchTableViewCell.self, forCellReuseIdentifier: swIdentifier)
        tableView.registerCell(VMSliderTableViewCell.self, forCellReuseIdentifier: slIdentifier)
        tableView.registerCell(VMNorSetTableViewCell.self, forCellReuseIdentifier: nIdentifier)
        tableView.registerCell(VMANISSUPTableViewCell.self, forCellReuseIdentifier: pIdentifier)
        tableView.registerCell(VMANISSetTableViewCell.self, forCellReuseIdentifier: sIdentifier)
        tableView.registerCell(VMSoundSelTableViewCell.self, forCellReuseIdentifier: soIdentifier)
        tableView.registerCell(UITableViewCell.self, forCellReuseIdentifier: tIdentifier)
        tableView.dataSource = self
        tableView.delegate = self
        addSubview(tableView)

        if #available(iOS 15.0, *) {
            tableView.sectionHeaderTopPadding = 0
        } else {
            // Fallback on earlier versions
        }
    }

    @objc private func back() {
        guard let backBlock = backBlock else {
            return
        }
        backBlock()
    }
}

extension VMEQSettingView: UITableViewDelegate, UITableViewDataSource {
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
            if indexPath.row > 1 && indexPath.row < 7 {
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
        }
    }

    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if settingType == .Noise && section == 2 {
            return textHeight(text: "voice_AINS_sup".localized(), fontSize: 13, width: ScreenWidth - 40) + 15
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
                return 1
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
            return 80;
        }
        return 0
    }

    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        if settingType == .AIAEC {
            let footer: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 66))

            footer.backgroundColor = settingType == .AIAEC ? .white : UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 10, y: 5, width: screenWidth-20, height: 66))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.numberOfLines = 0
            titleLabel.text = "voice_AIAEC_desc".localized()
            titleLabel.textColor = UIColor(red: 60 / 255.0, green: 66 / 255.0, blue: 103 / 255.0, alpha: 1)
            footer.addSubview(titleLabel)
            return footer
        } else if settingType == .AGC {
            let footer: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 60))

            footer.backgroundColor = settingType == .AIAEC ? .white : UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 10, y: 5, width: screenWidth-20, height: 60))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.numberOfLines = 0
            titleLabel.text = "voice_AGC_desc".localized()
            titleLabel.textColor = UIColor(red: 60 / 255.0, green: 66 / 255.0, blue: 103 / 255.0, alpha: 1)
            footer.addSubview(titleLabel)
            return footer
            
        } else {
            let footer = UIView(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 40))
            footer.backgroundColor = .white
            let textView = UITextView(frame: CGRect(x: 30, y: 0, width: screenWidth - 60, height: 40))

            let text = NSMutableAttributedString(string: "voice_visit_more".localized())
            text.addAttribute(NSAttributedString.Key.font,
                              value: UIFont.systemFont(ofSize: 13),
                              range: NSRange(location: 0, length: text.length))
            text.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.HexColor(hex: 0x3C4267, alpha: 1), range: NSRange(location: 0, length: text.length))

            let interactableText = NSMutableAttributedString(string: "www.agora.io")
            interactableText.addAttribute(NSAttributedString.Key.font,
                                          value: UIFont.systemFont(ofSize: 12, weight: .bold),
                                          range: NSRange(location: 0, length: interactableText.length))

            interactableText.addAttribute(NSAttributedString.Key.underlineStyle, value: 1, range: NSRange(location: 0, length: interactableText.length))

            // Adding the link interaction to the interactable text
            interactableText.addAttribute(NSAttributedString.Key.link,
                                          value: "SignInPseudoLink",
                                          range: NSRange(location: 0, length: interactableText.length))
            interactableText.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor.HexColor(hex: 0x3C4267, alpha: 1), range: NSRange(location: 0, length: interactableText.length))
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
                titleLabel.text = "voice_current_sound".localized()
                titleLabel.textColor = UIColor(red: 60 / 255.0, green: 66 / 255.0, blue: 103 / 255.0, alpha: 1)
            } else if settingType == .Spatial {
                titleLabel.text = "voice_blue".localized()
                titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            } else if settingType == .AIAEC {
                
            } else if settingType == .AGC {
        

            } else {
                titleLabel.text = "voice_AINS_settings".localized()
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
                titleLabel.text = "voice_other_sound".localized()
                headerView.addSubview(titleLabel)

                if section == 1 {
                    let warningView = UIView(frame: CGRect(x: 20, y: 40, width: screenWidth - 40, height: 12 + otherSoundHeaderHeight))
                    warningView.layer.cornerRadius = 5
                    warningView.layer.masksToBounds = true
                    warningView.backgroundColor = UIColor.HexColor(hex: 0xFFF7DC, alpha: 1)
                    headerView.addSubview(warningView)

                    let iconView: UIImageView = .init(frame: CGRect(x: 8, y: 7, width: 16, height: 16))
                    iconView.image = UIImage.sceneImage(name: "zhuyi", bundleName: "VoiceChatRoomResource")
                    warningView.addSubview(iconView)

                    let warningLabel = UILabel(frame: CGRect(x: 30, y: 6, width: screenWidth - 100, height: otherSoundHeaderHeight))
                    warningLabel.text = "voice_otherSound".localized()
                    warningLabel.numberOfLines = 0
                    warningLabel.lineBreakMode = .byCharWrapping
                    warningLabel.font = UIFont.systemFont(ofSize: 12)
                    warningLabel.textColor = UIColor.HexColor(hex: 0xE76D21, alpha: 1)
                    warningView.addSubview(warningLabel)
                }
            } else {
                titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
                titleLabel.text = settingType == .Spatial ? "Agora Red Bot" : "voice_AINS_definition".localized()
                headerView.addSubview(titleLabel)
            }

            return headerView
        } else {
            let height = textHeight(text: "voice_AINS_sup".localized(), fontSize: 13, width: ScreenWidth - 40)
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: height + 15))
            headerView.backgroundColor = UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 5, width: screenWidth - 40, height: height))
            titleLabel.numberOfLines = 0
            titleLabel.lineBreakMode = .byCharWrapping
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            titleLabel.text = "voice_AINS_sup".localized()
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
                 return cell
             }
         } else if settingType == .AIAEC {
             let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
             cell.titleLabel.text = AIAECSettingName[indexPath.row]
//             cell.isAudience = isAudience
             cell.selectionStyle = .none
             cell.swith.isOn = roomInfo?.room?.turn_AIAEC ?? false
             cell.useRobotBlock = { [weak self] flag in
                 guard let turnAIAECBlock = self?.turnAIAECBlock else { return }
                 turnAIAECBlock(flag)
//                 guard let backBlock = self?.backBlock else {return}
//                 backBlock();
             }
             return cell

             
             
//             let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
//             cell.isNoiseSet = true
//             cell.titleLabel.text = AIAECSettingName[indexPath.row]

//             return cell
         } else if settingType == .AGC {
             let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
             cell.titleLabel.text = AGCSettingName[indexPath.row]
//             cell.isAudience = isAudience
             cell.selectionStyle = .none
             cell.swith.isOn = roomInfo?.room?.turn_AGC ?? false
             cell.useRobotBlock = { [weak self] flag in
                 guard let turnAGCBlock = self?.turnAGCBlock else { return }
                 turnAGCBlock(flag)
//                 guard let backBlock = self?.backBlock else {return}
//                 backBlock();
             }
             return cell         }
        else {
             if indexPath.section == 0 {
                 let cell: VMANISSetTableViewCell = tableView.dequeueReusableCell(withIdentifier: sIdentifier) as! VMANISSetTableViewCell
                 cell.ains_state = ains_state
                 cell.selectionStyle = .none
                 cell.isTouchAble = isTouchAble
                 cell.selBlock = { [weak self] state in
                     self?.ains_state = state
                     guard let block = self?.selBlock else { return }
                     block(state)
                 }
                 cell.selectionStyle = .none
                 return cell
             } else if indexPath.section == 1 {
                 let cell: UITableViewCell = tableView.dequeueReusableCell(withIdentifier: tIdentifier)!
                 cell.textLabel?.text = "voice_AINS_AI_noise_suppression".localized()
                 cell.textLabel?.font = UIFont.systemFont(ofSize: 13)
                 cell.textLabel?.textColor = UIColor.HexColor(hex: 0x3C4267, alpha: 1)
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
