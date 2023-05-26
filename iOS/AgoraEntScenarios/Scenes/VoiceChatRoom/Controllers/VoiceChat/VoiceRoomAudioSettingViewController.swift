//
//  VoiceRoomAudioSettingViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/1/31.
//

import UIKit

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
    public var isTouchAble: Bool = false
    private let swIdentifier = "switch"
    private let slIdentifier = "slider"
    private let nIdentifier = "normal"
    private lazy var inEarView = VoiceRealInEarView()

//    private var settingName: [String] = ["\(LanguageManager.localValue(key: "blue")) & \(LanguageManager.localValue(key: "red"))", LanguageManager.localValue(key: "Robot Volume"), LanguageManager.localValue(key: "Best Sound"), "AINS", "Spatial Audio"]
//    private var settingImage: [String] = ["icons／set／jiqi", "icons／set／laba", "icons／set／zuijia", "icons／set／AINS", "icons／set／3D"]
    
    private var settingName: [String] = [LanguageManager.localValue(key: "AINS"),
                                         LanguageManager.localValue(key: "AIAEC"),
                                         LanguageManager.localValue(key: "AGC"),
                                         LanguageManager.localValue(key: "In-Ear Monitor"),
                                         LanguageManager.localValue(key: "Agora Blue & Red Bot"),
                                         LanguageManager.localValue(key: "Robot Volume"),
                                         LanguageManager.localValue(key: "Best Agora Sound"),
                                         LanguageManager.localValue(key: "Background Music"),
                                         "Spatial Audio"]
    
    
    
    private var settingImage: [String] = ["AINS",
                                          "AIAEC",
                                          "AGC",
                                          "InEar",
                                          "jiqi",
                                          "icons／set／laba",
                                          "icons／set／zuijia",
                                          "Music"]


    private var soundTitle: [String] = []
    private var ainsTitle: [String] = []
    private var rtcKit: VoiceRoomRTCManager?
    private lazy var musicListView: VoiceMusicListView = {
        let view = VoiceMusicListView(rtcKit: rtcKit,
                                      currentMusic: roomInfo?.room?.backgroundMusic,
                                      isOrigin: roomInfo?.room?.musicIsOrigin ?? true)
        view.backgroundMusicPlaying = { [weak self] model in
            self?.backgroundMusicPlaying?(model)
            self?.roomInfo?.room?.backgroundMusic = model
            self?.tableView.reloadData()
        }
        view.onClickAccompanyButtonClosure = { [weak self] isOrigin in
            self?.roomInfo?.room?.musicIsOrigin = isOrigin
            self?.onClickAccompanyButtonClosure?(isOrigin)
        }
        return view
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
    var soundBlock: ((Int) -> Void)?
    var turnAIAECBlock:((Bool) ->Void)?
    var turnAGCBlock:((Bool) ->Void)?
    var turnInearBlock: ((Bool) -> Void)?
    var setInEarVolumnBlock: ((Int) -> Void)?
    var setInEarModeBlock: ((INEAR_MODE) -> Void)?
    var backgroundMusicPlaying: ((VoiceMusicModel) -> Void)?
    var onClickAccompanyButtonClosure: ((Bool) -> Void)?
    
    
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
        VoiceRoomRTCManager.getSharedInstance().rtcKit.stopAudioMixing()
    }
    
    private func layoutUI() {
        navigation.back.isHidden = true
        
        let path = UIBezierPath(roundedRect: self.view.bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer = CAShapeLayer()
        layer.path = path.cgPath
        self.view.layer.mask = layer

        view.addSubview(cover)

        lineImgView.frame = CGRect(x: ScreenWidth / 2.0 - 20, y: 8, width: 40, height: 4)
        lineImgView.image = UIImage("pop_indicator")
        view.addSubview(lineImgView)

        titleLabel.frame = CGRect(x: ScreenWidth / 2.0 - 60, y: 30, width: 120, height: 30)
        titleLabel.textAlignment = .center
        titleLabel.text = LanguageManager.localValue(key: "Audio Settings")
        titleLabel.textColor = UIColor(red: 0.016, green: 0.035, blue: 0.145, alpha: 1)
        titleLabel.font = UIFont.systemFont(ofSize: 16, weight: .bold)
        view.addSubview(titleLabel)
        print("\(self.view.bounds.size.height)")
        tableView.frame = CGRect(x: 0, y: 70, width: ScreenWidth, height: 600)
        tableView.registerCell(VMSwitchTableViewCell.self, forCellReuseIdentifier: swIdentifier)
        tableView.registerCell(VMSliderTableViewCell.self, forCellReuseIdentifier: slIdentifier)
        tableView.registerCell(VMNorSetTableViewCell.self, forCellReuseIdentifier: nIdentifier)
        tableView.dataSource = self
        tableView.delegate = self
        view.addSubview(tableView)
        tableView.tableFooterView = UIView()
        tableView.isScrollEnabled = false

        tableView.separatorColor = UIColor.HexColor(hex: 0xF6F6F6, alpha: 1)

        if #available(iOS 15.0, *) {
            tableView.sectionHeaderTopPadding = 0
        } else {
            // Fallback on earlier versions
        }
        
        HeadSetUtil.addHeadsetObserver { hasHeadset in
            self.roomInfo?.room?.turn_InEar = hasHeadset == false ? false : self.roomInfo?.room?.turn_InEar
        }
    }
}

extension VoiceRoomAudioSettingViewController: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        return 3
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 54
    }

    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 32
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == 0 {
            return 4
        } else if section == 1 {
            return 2
        } else {
            return roomInfo?.room?.owner?.uid == VLUserCenter.user.id ? 2 : 1
        }
    }

    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if section == 0 {
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 32))
            headerView.backgroundColor = UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 2, width: 300, height: 30))
            titleLabel.text = LanguageManager.localValue(key: "Personal audio Settings")
            
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            headerView.addSubview(titleLabel)
            return headerView
        } else  if section == 1{
            let width = textAutoWidth(height: 300, font: UIFont.systemFont(ofSize: 13), text: LanguageManager.localValue(key: "Bot Settings"))
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 32))
            headerView.backgroundColor = UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 2, width: width, height: 30))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            titleLabel.text = LanguageManager.localValue(key: "Bot Settings")
            headerView.addSubview(titleLabel)

            let imgView: UIImageView = .init(frame: CGRect(x: width + 30, y: 6, width: 30, height: 20))
            imgView.contentMode = .scaleAspectFit
            imgView.image = UIImage("new")
            headerView.addSubview(imgView)

            return headerView
        } else if section == 2 {
            let width = textAutoWidth(height: 300, font: UIFont.systemFont(ofSize: 13), text: LanguageManager.localValue(key: "Room Audio Settings"))
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 32))
            headerView.backgroundColor = UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 2, width: width, height: 30))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            titleLabel.text = LanguageManager.localValue(key: "Room Audio Settings")
            headerView.addSubview(titleLabel)

            let imgView: UIImageView = .init(frame: CGRect(x: width + 30, y: 6, width: 30, height: 20))
            imgView.contentMode = .scaleAspectFit
            imgView.image = UIImage("new")
            headerView.addSubview(imgView)

            return headerView
        } else {
            return nil
        }
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        guard section == 2 else { return nil }
        let view = UIView()
        let label = UILabel()
        label.text = "声网凤鸣AI引擎提供支持"
        label.textColor = UIColor(hexString: "#6C7192")
        label.font = .systemFont(ofSize: 12)
        label.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(label)
        label.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        label.centerYAnchor.constraint(equalTo: view.centerYAnchor, constant: -5).isActive = true
        
        let logoImageView = UIImageView(image: UIImage.sceneImage(name: "AI_logo", bundleName: "VoiceChatRoomResource"))
        logoImageView.contentMode = .scaleAspectFit
        logoImageView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(logoImageView)
        logoImageView.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
        logoImageView.trailingAnchor.constraint(equalTo: label.leadingAnchor, constant: -10).isActive = true
        
        let lineView = UIView()
        lineView.backgroundColor = UIColor(hexString: "#F6F6F6")
        lineView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(lineView)
        lineView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20).isActive = true
        lineView.topAnchor.constraint(equalTo: view.topAnchor, constant: 10).isActive = true
        lineView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
        return view
    }
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        section == 2 ? 80.0 : 0.1
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.section == 0 {
            guard let cell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as? VMNorSetTableViewCell else {
                return UITableViewCell()
            }
            guard !settingImage.isEmpty else { return cell }

            cell.iconView.image = UIImage(settingImage[0 + indexPath.row])
            cell.titleLabel.text = settingName[0 + indexPath.row]
            if indexPath.row == 0 {
                //cell.contentLabel.text = getSoundType(with: roomInfo?.room?.sound_effect ?? 1)
                switch ains_state {
                case .high:
                    cell.contentLabel.text = "High".localized()
                case .mid:
                    cell.contentLabel.text = "Medium".localized()
                case .off:
                    cell.contentLabel.text = "Off".localized()
                }
                
                
            } else if indexPath.row == 1 {
                if roomInfo?.room?.turn_AIAEC == true {
                    cell.contentLabel.text = "On".localized()
                } else {
                    cell.contentLabel.text = "Off".localized()
                }
            } else if indexPath.row == 2 {
                if roomInfo?.room?.turn_AGC == true {
                    cell.contentLabel.text = "On".localized()
                } else {
                    cell.contentLabel.text = "Off".localized()
                }
            } else if indexPath.row == 3 {
                if roomInfo?.room?.turn_InEar == true {
                    cell.contentLabel.text = "On".localized()
                } else {
                    cell.contentLabel.text = "Off".localized()
                }
            } else {
                cell.contentLabel.text = "Other".localized()

            }
            return cell
            
        } else if indexPath.section == 1 {
            if indexPath.row == 0 {
                let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
                guard !settingImage.isEmpty else { return cell}
                cell.iconView.image = UIImage(settingImage[4])
                cell.titleLabel.text = settingName[4]
                cell.swith.alpha = isAudience ? 0.5 : 1
                cell.swith.isUserInteractionEnabled = !isAudience
                cell.selectionStyle = .none
                cell.swith.isOn = roomInfo?.room?.use_robot ?? false
                cell.useRobotBlock = { [weak self] flag in
                    guard let useRobotBlock = self?.useRobotBlock else { return }
                    self?.isTouchAble = flag
                    useRobotBlock(flag)
                }
                return cell
            } else if indexPath.row == 1 {
                let cell: VMSliderTableViewCell = tableView.dequeueReusableCell(withIdentifier: slIdentifier) as! VMSliderTableViewCell
                guard !settingImage.isEmpty else { return cell}

                cell.iconView.image = UIImage(settingImage[5])
                cell.titleLabel.text = settingName[5]
                cell.isAudience = isAudience
                cell.selectionStyle = .none
                cell.volBlock = { [weak self] vol in
                    guard let volBlock = self?.volBlock else { return }
                    volBlock(vol)
                }
                
                let volume = roomInfo?.room?.robot_volume ?? 50
                cell.slider.value = Float(volume) / 100.0
                cell.countLabel.text = "\(volume)"
                return cell
            }
        } else if indexPath.section == 2 {
            
//            let cell: VMNorSetTableViewCell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as! VMNorSetTableViewCell
            guard let cell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as? VMNorSetTableViewCell else {
                // handle the failure case here
                return UITableViewCell()
            }
            guard !settingImage.isEmpty else { return cell}

            cell.iconView.image = UIImage(settingImage[6 + indexPath.row])
            cell.titleLabel.text = settingName[6 + indexPath.row]
      
            if indexPath.row == 0 {
                cell.contentLabel.text = getSoundType(with: roomInfo?.room?.sound_effect ?? 1)
            } else if indexPath.row == 1 {
                cell.contentLabel.text = "\(roomInfo?.room?.backgroundMusic?.name ?? "")-\(roomInfo?.room?.backgroundMusic?.singer ?? "")"
            }
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
                
                let isOn = (roomInfo?.room?.turn_InEar ?? false)
                inEarView.isHidden = !isOn
                var inEar_volume = Double((roomInfo?.room?.inEar_volume ?? 0)) / 100.0
                var inEarMode = roomInfo?.room?.inEarMode ?? ""
                let actionView = ActionSheetManager()
                let earModes = ["自动".show_localized, "强制OpenSL".show_localized, "强制Oboe".show_localized]
                var inEarModeIndex = earModes.firstIndex(where: { $0 == inEarMode }) ?? 0
                let hasHeadset = HeadSetUtil.hasHeadset()
                let tipsTextColor = hasHeadset ? UIColor(hex: "#979CBB") : UIColor(hex: "#FF1216")
                let tipsText = hasHeadset ? "开启耳返可实时听到自己的声音, 唱歌的时候及时调整".show_localized : "使用耳返必须插入耳机，当前未检测到耳机".show_localized
                actionView.title(title: "耳返".show_localized)
                    .switchCell(title: "开启耳返".show_localized, isOn: isOn, isEnabel: hasHeadset)
                    .tipsCell(iconName: "inEra_tips_icon", title: tipsText, titleColor: tipsTextColor)
                    .sectionHeader(title: "耳返设置".show_localized, desc: nil)
                    .sliderCell(title: "耳返音量".show_localized, value: inEar_volume, isEnable: isOn)
//                    .segmentCell(title: "耳返模式", items: earModes, selectedIndex: inEarModeIndex, isEnable: isOn)
//                    .customCell(customView: inEarView, viewHeight: 150)
                    .config()
                actionView.show_voice()
                actionView.didSwitchValueChangeClosure = { [weak self] _, isOn in
                    self?.roomInfo?.room?.turn_InEar = isOn
                    actionView.updateSliderValue(indexPath: IndexPath(row: 0, section: 1), value: inEar_volume, isEnable: isOn)
                    actionView.updateSegmentStatus(indexPath: IndexPath(row: 1, section: 1), selectedIndex: inEarModeIndex, isEnable: isOn)
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
                    self.showCustomAlert(title: "提示", message: String(format: "切换后将强制使用%@模式,确认?".show_localized, mode), confirm: {
                        inEarModeIndex = earModes.firstIndex(where: { $0 == mode }) ?? 0
                        self.roomInfo?.room?.inEarMode = mode
                        inEarMode = mode
                        self.setInEarModeBlock?(INEAR_MODE(rawValue: index) ?? .auto)
                    }, cancel: {
                        let index = earModes.firstIndex(where: { $0 == inEarMode }) ?? 0
                        actionView.updateSegmentStatus(indexPath: indexPath, selectedIndex: index)
                    })
                }
                return
            default:
                state = .Spatial
            }
        }  else if indexPath.section == 2 {
            switch indexPath.row {
            case 0:
                //最佳音效
                state = .Music
                heightType = .EFFECT
                
            case 1:
                state = .Music
                heightType = .Music
                guard roomInfo?.room?.owner?.uid == VLUserCenter.user.id else { return }
                musicListView.show()
                return
                
            default:
                state = .Spatial
            }
        } else if indexPath.section == 1 {
            return
        }
        tableViewHeight = heightType.rawValue - 70
        let detailVC: VoiceRoomAudioSettingDetailViewController = VoiceRoomAudioSettingDetailViewController()
        detailVC.roomInfo = roomInfo
        detailVC.isAudience = isAudience
        detailVC.soundEffect = roomInfo?.room?.sound_effect ?? 1
        detailVC.settingType = state
        detailVC.ains_state = ains_state
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
        var soundType: String = "Social Chat".localized()
        switch index {
        case 1:
            soundType = "Social Chat".localized()
        case 2:
            soundType = "Karaoke".localized()
        case 3:
            soundType = "Gaming Buddy".localized()
        case 4:
            soundType = "Professional Podcaster".localized()
        default:
            soundType = "Social Chat".localized()
        }
        return soundType
    }
}
