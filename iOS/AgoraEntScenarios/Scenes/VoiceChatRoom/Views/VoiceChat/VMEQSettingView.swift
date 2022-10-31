//
//  VMEQSettingView.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/8.
//

import UIKit

class VMEQSettingView: UIView, UITextViewDelegate {
    private var screenWidth: CGFloat = UIScreen.main.bounds.size.width
    private var lineImgView: UIImageView = UIImageView()
    private var titleLabel: UILabel = UILabel()
    private var tableView: UITableView = UITableView(frame: .zero, style: .grouped)
    private var backBtn: UIButton = UIButton()
    
    lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 56~)).backgroundColor(.clear).setGradient([UIColor(red: 0.929, green: 0.906, blue: 1, alpha: 1),UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)], [CGPoint(x: 0, y: 0),CGPoint(x: 0, y: 1)])
    }()
    
    private let swIdentifier = "switch"
    private let slIdentifier = "slider"
    private let nIdentifier = "normal"
    private let pIdentifier = "sup"
    private let sIdentifier = "set"
    private let soIdentifier = "sound"
    private let tIdentifier = "tv"
    private var effectHeight:[CGFloat] = [0, 0, 0, 0]
    private var effectType: [SOUND_TYPE] = [.chat, .karaoke, .game, .anchor]
    var backBlock: (() -> Void)?
    var effectClickBlock:((SOUND_TYPE) -> Void)?
    var visitBlock: (() -> Void)?
    var isTouchAble: Bool = false
    var isAudience: Bool = false
    var ains_state: AINS_STATE = .off {
        didSet {
            tableView.reloadData()
        }
    }
    
    var soundEffect: String?{
        didSet {
            guard let soundEffect = soundEffect else {
                return
            }
              let socialH: CGFloat = textHeight(text: LanguageManager.localValue(key: "This sound effect focuses on solving the voice call problem of the Social Chat scene, including noise cancellation and echo suppression of the anchor's voice. It can enable users of different network environments and models to enjoy ultra-low delay and clear and beautiful voice in multi-person chat."), fontSize: 13, width: self.bounds.size.width - 80~)
            let ktvH: CGFloat = textHeight(text: LanguageManager.localValue(key: "This sound effect focuses on solving all kinds of problems in the Karaoke scene of single-person or multi-person singing, including the balance processing of accompaniment and voice, the beautification of sound melody and voice line, the volume balance and real-time synchronization of multi-person chorus, etc. It can make the scenes of Karaoke more realistic and the singers' songs more beautiful."), fontSize: 13, width: self.bounds.size.width - 80~)
            let gameH: CGFloat = textHeight(text: LanguageManager.localValue(key: "This sound effect focuses on solving all kinds of problems in the game scene where the anchor plays with him, including the collaborative reverberation processing of voice and game sound, the melody of sound and the beautification of sound lines. It can make the voice of the accompanying anchor more attractive and ensure the scene feeling of the game voice. "), fontSize: 13, width: self.bounds.size.width - 80~)
            let anchorH: CGFloat = textHeight(text: LanguageManager.localValue(key: "This sound effect focuses on solving the problems of poor sound quality of mono anchors and compatibility with mainstream external sound cards. The sound network stereo collection and high sound quality technology can greatly improve the sound quality of anchors using sound cards and enhance the attraction of live broadcasting rooms. At present, it has been adapted to mainstream sound cards in the market. "), fontSize: 13, width: self.bounds.size.width - 80~)
            print("\(soundEffect)-----")
            switch soundEffect {
            case "Social Chat":
                effectHeight = [socialH, ktvH, gameH, anchorH]
                effectType = [.chat, .karaoke, .game, .anchor]
            case "Karaoke":
                effectHeight = [ktvH, socialH, gameH, anchorH]
                effectType = [.karaoke, .chat, .game, .anchor]
            case "Gaming Buddy":
                effectHeight = [gameH, socialH, ktvH, anchorH]
                effectType = [.game, .chat, .karaoke, .anchor]
            case "Professional podcaster":
                effectHeight = [anchorH, socialH, ktvH, gameH]
                effectType = [.anchor, .chat, .karaoke, .game]
            default:
                break
            }
        }
    }
    
    var selBlock: ((AINS_STATE)->Void)?
    var soundBlock: ((Int)->Void)?
    private var selTag: Int?
    
    private let settingName: [String] = ["Spatial Audio", "Attenuation factor", "Air absorb", "Voice blur"]
    private let soundType: [String] = ["TV Sound".localized(), "Kitchen Sound".localized(), "Street Sound".localized(), "Mashine Sound".localized(), "Office Sound".localized(), "Home Sound".localized(), "Construction Sound".localized(),"Alert Sound/Music".localized(),"Applause".localized(),"Wind Sound".localized(),"Mic Pop Filter".localized(),"Audio Feedback".localized(),"Microphone Finger Rub Sound".localized(),"Screen Tap Sound".localized()]
    private let soundDetail: [String] = ["Ex. Bird, car, subway sounds".localized(), "Ex. Fan, air conditioner, vacuum cleaner, printer sounds".localized(), "Ex. Keyboard tapping, mouse clicking sounds".localized(), "Ex. Door closing, chair squeaking, baby crying sounds".localized(), "Ex. Knocking sound".localized()]
    
    var settingType: AUDIO_SETTING_TYPE = .Spatial {
        didSet {
            if settingType == .Spatial {
                titleLabel.text = "Spatial Setting".localized()
            } else if settingType == .Noise {
                titleLabel.text = "Noise Setting".localized()
            }else if settingType == .effect {
                titleLabel.text = "Effect Setting".localized()
            }
            tableView.reloadData()
        }
    }
    
    lazy var otherSoundHeaderHeight: CGFloat = textHeight(text: "otherSound".localized(), fontSize: 12, width: ScreenWidth - 100)
    
    var resBlock: ((AUDIO_SETTING_TYPE) -> Void)?
    
    override func draw(_ rect: CGRect) {
        super.draw(rect)
        self.backgroundColor = .white
        layoutUI()
    }
    
    private func layoutUI() {
        let path: UIBezierPath = UIBezierPath(roundedRect: self.bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer: CAShapeLayer = CAShapeLayer()
        layer.path = path.cgPath
        self.layer.mask = layer
        
        self.addSubview(cover)
        
        backBtn.frame = CGRect(x: 10~, y: 30~, width: 20~, height: 30~)
        backBtn.setImage(UIImage("back"), for: .normal)
        backBtn.addTargetFor(self, action: #selector(back), for: .touchUpInside)
        self.addSubview(backBtn)
        
        lineImgView.frame = CGRect(x: ScreenWidth / 2.0 - 20~, y: 8, width: 40~, height: 4)
        lineImgView.image = UIImage("pop_indicator")
        self.addSubview(lineImgView)
        
        titleLabel.frame = CGRect(x: ScreenWidth / 2.0 - 60~, y: 25~, width: 120~, height: 30~)
        titleLabel.textAlignment = .center
        titleLabel.text = "Spatial Audio"
        titleLabel.textColor = UIColor.HexColor(hex: 0x040925, alpha: 1)
        titleLabel.font = UIFont.systemFont(ofSize: 16,weight: .bold)
        self.addSubview(titleLabel)
        
        tableView.frame = CGRect(x: 0, y: 70~, width: ScreenWidth, height: 280~)
        tableView.registerCell(VMSwitchTableViewCell.self, forCellReuseIdentifier: swIdentifier)
        tableView.registerCell(VMSliderTableViewCell.self, forCellReuseIdentifier: slIdentifier)
        tableView.registerCell(VMNorSetTableViewCell.self, forCellReuseIdentifier: nIdentifier)
        tableView.registerCell(VMANISSUPTableViewCell.self, forCellReuseIdentifier: pIdentifier)
        tableView.registerCell(VMANISSetTableViewCell.self, forCellReuseIdentifier: sIdentifier)
        tableView.registerCell(VMSoundSelTableViewCell.self, forCellReuseIdentifier: soIdentifier)
        tableView.registerCell(UITableViewCell.self, forCellReuseIdentifier: tIdentifier)
        tableView.dataSource = self
        tableView.delegate = self
        self.addSubview(tableView)
        
        if #available(iOS 15.0, *) {
            tableView.sectionHeaderTopPadding = 0
        } else {
            // Fallback on earlier versions
        };
        
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
        return settingType == .Noise ? 3 : 2
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        switch settingType{
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
        case .Spatial:
            return 54
        }
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if settingType == .Noise && section == 2 {
            return textHeight(text: "AINS Sup".localized(), fontSize: 13, width: ScreenWidth - 40) + 15
        } else if settingType == .effect && section == 1 {
            return 40~ + 12~ + otherSoundHeaderHeight + 10~
        } else {
            return 40~
        }
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if settingType == .effect {
            return section == 0 ? 1 : 3
        } else if settingType == .Spatial {
            return 4
        } else {
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
            return  40
        }
        return 0
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        let footer: UIView = UIView(frame: CGRect(x: 0, y: 0, width: screenWidth , height: 40))
        footer.backgroundColor = .white
        let textView: UITextView = UITextView(frame: CGRect(x: 30, y: 0, width: screenWidth - 60, height: 40))
            
        let text = NSMutableAttributedString(string: "Visit More".localized())
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
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if section == 0 {
            let headerView: UIView = UIView(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 40~))
            headerView.backgroundColor = settingType == .effect ? .white : UIColor(red: 247/255.0, green: 248/255.0, blue: 251/255.0, alpha: 1)
            let titleLabel: UILabel = UILabel(frame: CGRect(x: 20~, y: 5~, width: 300~, height: 30~))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            if settingType == .effect {
                titleLabel.text = "Current Sound".localized()
                titleLabel.textColor = UIColor(red: 60/255.0, green: 66/255.0, blue: 103/255.0, alpha: 1)
            } else {
                titleLabel.text = settingType == .Spatial ? "Agora Blue Bot" : "AINS Settings".localized()
                titleLabel.textColor = UIColor(red: 108/255.0, green: 113/255.0, blue: 146/255.0, alpha: 1)
            }
            headerView.addSubview(titleLabel)
            return headerView
        } else if section == 1  {
            let headerHeight:CGFloat = (section == 1 && settingType == .effect) ? 40~ + 12 + otherSoundHeaderHeight + 10~ : 40~
            let headerView: UIView = UIView(frame: CGRect(x: 0, y: 0, width: screenWidth, height: headerHeight))
            headerView.backgroundColor = settingType == .effect ? .white : UIColor(red: 247/255.0, green: 248/255.0, blue: 251/255.0, alpha: 1)
            let titleLabel: UILabel = UILabel(frame: CGRect(x: 20~, y: 5~, width: 300~, height: 30~))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            if settingType == .effect {
                titleLabel.textColor = UIColor(red: 60/255.0, green: 66/255.0, blue: 103/255.0, alpha: 1)
                titleLabel.text = "Other Sound".localized()
                headerView.addSubview(titleLabel)
                
                if section == 1 {
                    let warningView = UIView(frame: CGRect(x: 20, y: 40~, width: screenWidth - 40~, height: 12 + otherSoundHeaderHeight))
                    warningView.layer.cornerRadius = 5
                    warningView.layer.masksToBounds = true
                    warningView.backgroundColor = UIColor.HexColor(hex: 0xFFF7DC, alpha: 1)
                    headerView.addSubview(warningView)
                    
                    let iconView: UIImageView = UIImageView(frame: CGRect(x: 8, y: 7, width: 16, height: 16))
                    iconView.image = UIImage("zhuyi")
                    warningView.addSubview(iconView)
                    
                    let warningLabel = UILabel(frame: CGRect(x: 30, y: 6, width: screenWidth - 100, height: otherSoundHeaderHeight))
                    warningLabel.text = "otherSound".localized()
                    warningLabel.numberOfLines = 0
                    warningLabel.lineBreakMode = .byCharWrapping
                    warningLabel.font = UIFont.systemFont(ofSize: 12)
                    warningLabel.textColor = UIColor.HexColor(hex: 0xE76D21, alpha: 1)
                    warningView.addSubview(warningLabel)
                }
            } else {
                titleLabel.textColor = UIColor(red: 108/255.0, green: 113/255.0, blue: 146/255.0, alpha: 1)
                titleLabel.text = settingType == .Spatial ? "Agora Red Bot" : "AINS Definition".localized()
                headerView.addSubview(titleLabel)
            }

            return headerView
        } else {
            let height = textHeight(text: "AINS Sup".localized(), fontSize: 13, width: ScreenWidth - 40)
            let headerView: UIView = UIView(frame: CGRect(x: 0, y: 0, width: screenWidth, height: height + 15))
            headerView.backgroundColor = UIColor(red: 247/255.0, green: 248/255.0, blue: 251/255.0, alpha: 1)
            let titleLabel: UILabel = UILabel(frame: CGRect(x: 20~, y: 5~, width: screenWidth - 40, height: height))
            titleLabel.numberOfLines = 0
            titleLabel.lineBreakMode = .byCharWrapping
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.textColor = UIColor(red: 108/255.0, green: 113/255.0, blue: 146/255.0, alpha: 1)
            titleLabel.text = "AINS Sup".localized()
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

            let cell: VMSoundSelTableViewCell = VMSoundSelTableViewCell.init(style: .default, reuseIdentifier: soIdentifier, cellType: cellType, cellHeight: cellHeight)
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
        } else {
            if indexPath.section == 0 {
                let cell: VMANISSetTableViewCell = tableView.dequeueReusableCell(withIdentifier: sIdentifier) as! VMANISSetTableViewCell
                cell.ains_state = ains_state
                cell.selectionStyle = .none
                cell.isTouchAble = isTouchAble
                cell.selBlock = {[weak self] state in
                    self?.ains_state = state
                    guard let block = self?.selBlock else {return}
                    block(state)
                }
                cell.selectionStyle = .none
                return cell
            } else if indexPath.section == 1 {
                let cell: UITableViewCell = tableView.dequeueReusableCell(withIdentifier: tIdentifier)!
                cell.textLabel?.text = "AINS: AI Noise Suppression".localized()
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
                cell.resBlock = {[weak self] index in
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
            guard let effectClickBlock = effectClickBlock else {return}
            if indexPath.section == 0 {
                effectClickBlock(effectType[0])
            } else {
                effectClickBlock(.none)
            }
        }
    }
    
    func textHeight(text: String, fontSize: CGFloat, width: CGFloat) -> CGFloat {
        return text.boundingRect(with:CGSize(width: width, height:CGFloat(MAXFLOAT)), options: .usesLineFragmentOrigin, attributes: [.font:UIFont.systemFont(ofSize: fontSize)], context:nil).size.height+5
        
    }
    
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange) -> Bool {
        
        guard let visitBlock = visitBlock else {return true}
        visitBlock()
        
        return true
    }
    
}

