//
//  VoiceRoomAudioSettingViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/1/31.
//

import UIKit

class VoiceRoomAudioSettingViewController: UIViewController {
    
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

//    private var settingName: [String] = ["\(LanguageManager.localValue(key: "blue")) & \(LanguageManager.localValue(key: "red"))", LanguageManager.localValue(key: "Robot Volume"), LanguageManager.localValue(key: "Best Sound"), "AINS", "Spatial Audio"]
//    private var settingImage: [String] = ["icons／set／jiqi", "icons／set／laba", "icons／set／zuijia", "icons／set／AINS", "icons／set／3D"]
    
    private var settingName: [String] = [LanguageManager.localValue(key: "voice_AINS"),LanguageManager.localValue(key: "voice_AIAEC"),LanguageManager.localValue(key: "voice_AGC"),LanguageManager.localValue(key: "voice_agora_blue_and_red_bot"), LanguageManager.localValue(key: "voice_robot_volume"), LanguageManager.localValue(key: "voice_best_agora_sound"), "voice_spatial_audio".voice_localized()]
    
    
    
    private var settingImage: [String] = ["AINS", "AIAEC", "AGC", "jiqi", "icons／set／laba", "icons／set／zuijia", "icons／set／AINS", "icons／set／3D"]


    private var soundTitle: [String] = []
    private var ainsTitle: [String] = []

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
        tableView.frame = CGRect(x: 0, y: 70, width: ScreenWidth, height: 430)
        tableView.registerCell(VMSwitchTableViewCell.self, forCellReuseIdentifier: swIdentifier)
        tableView.registerCell(VMSliderTableViewCell.self, forCellReuseIdentifier: slIdentifier)
        tableView.registerCell(VMNorSetTableViewCell.self, forCellReuseIdentifier: nIdentifier)
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
            return 3
        }  else if section == 1 {
            return 2
        } else {
            return 1
        }
//        if section == 0 {
//            return 2
//        } else {
//            return roomInfo?.room?.type == 1 ? 3 : 2
//        }
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
            imgView.image = UIImage.sceneImage(name: "new", bundleName: "VoiceChatRoomResource")
            headerView.addSubview(imgView)

            return headerView
        } else {
            return nil
        }
        /*
        if section == 0 {
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 32))
            headerView.backgroundColor = UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 2, width: 300, height: 30))
            titleLabel.text = LanguageManager.localValue(key: "Bot Settings")
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            headerView.addSubview(titleLabel)
            return headerView
        } else {
            let width = textAutoWidth(height: 300, font: UIFont.systemFont(ofSize: 13), text: LanguageManager.localValue(key: "voice_ACEQ"))
            let headerView: UIView = .init(frame: CGRect(x: 0, y: 0, width: screenWidth, height: 32))
            headerView.backgroundColor = UIColor(red: 247 / 255.0, green: 248 / 255.0, blue: 251 / 255.0, alpha: 1)
            let titleLabel: UILabel = .init(frame: CGRect(x: 20, y: 2, width: width, height: 30))
            titleLabel.font = UIFont.systemFont(ofSize: 13)
            titleLabel.textColor = UIColor(red: 108 / 255.0, green: 113 / 255.0, blue: 146 / 255.0, alpha: 1)
            titleLabel.text = LanguageManager.localValue(key: "voice_ACEQ")
            headerView.addSubview(titleLabel)

            let imgView: UIImageView = .init(frame: CGRect(x: width + 30, y: 6, width: 30, height: 20))
            imgView.image =UIImage.sceneImage(name: "new")
            headerView.addSubview(imgView)

            return headerView
        }
         
         */
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.section == 0 {
//            let cell: VMNorSetTableViewCell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as! VMNorSetTableViewCell
            guard let cell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as? VMNorSetTableViewCell else {
                // handle the failure case here
                return UITableViewCell()
            }

            guard !settingImage.isEmpty else { return cell}

            cell.iconView.image = UIImage.voice_image(settingImage[0 + indexPath.row])
            cell.titleLabel.text = settingName[0 + indexPath.row]
            if indexPath.row == 0 {
                //cell.contentLabel.text = getSoundType(with: roomInfo?.room?.sound_effect ?? 1)
                switch ains_state {
                case .high:
                    cell.contentLabel.text = "voice_High".voice_localized()
                case .mid:
                    cell.contentLabel.text = "voice_Medium".voice_localized()
                case .off:
                    cell.contentLabel.text = "voice_off".voice_localized()
                }
                
                
            } else if indexPath.row == 1 {
                if roomInfo?.room?.turn_AIAEC == true {
                    cell.contentLabel.text = "voice_on".voice_localized()
                } else {
                    cell.contentLabel.text = "voice_off".voice_localized()
                }
            } else if indexPath.row == 2 {
                if roomInfo?.room?.turn_AGC == true {
                    cell.contentLabel.text = "voice_on".voice_localized()
                } else {
                    cell.contentLabel.text = "voice_off".voice_localized()
                }
            } else {
                cell.contentLabel.text = "Other".voice_localized()

            }
//            else {
//                if roomInfo?.room?.t
//                cell.contentLabel.text = "voice_off".localized()
//
//            }
//            else if indexPath.row == 1 {
//                switch ains_state {
//                case .high:
//                    cell.contentLabel.text = "High".localized()
//                case .mid:
//                    cell.contentLabel.text = "Middle".localized()
//                case .off:
//                    cell.contentLabel.text = "voice_off".localized()
//                }
//            }
            return cell
            /*
            if indexPath.row == 0 {
                let cell: VMNorSetTableViewCell = tableView.dequeueReusableCell(withIdentifier: nIdentifier) as! VMNorSetTableViewCell
                cell.iconView.image = UIImage(settingImage[0])
                cell.titleLabel.text = settingName[0]
                if indexPath.row == 0 {
                    cell.contentLabel.text = getSoundType(with: roomInfo?.room?.sound_effect ?? 1)
                } else if indexPath.row == 1 {
                    switch ains_state {
                    case .high:
                        cell.contentLabel.text = "voice_High".localized()
                    case .mid:
                        cell.contentLabel.text = "Middle".localized()
                    case .off:
                        cell.contentLabel.text = "voice_off".localized()
                    }
                }
                return cell
                
            } else if indexPath.row == 1 {
                let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
                cell.iconView.image = UIImage(settingImage[1])
                cell.titleLabel.text = settingName[1]
                cell.isAudience = isAudience
                cell.selectionStyle = .none
                cell.swith.isOn = roomInfo?.room?.use_robot ?? false
                cell.useRobotBlock = { [weak self] flag in
                    guard let useRobotBlock = self?.useRobotBlock else { return }
                    useRobotBlock(flag)
                }
                return cell
            }
            else {
                let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
                cell.iconView.image = UIImage(settingImage[2])
                cell.titleLabel.text = settingName[2]
                cell.isAudience = isAudience
                cell.selectionStyle = .none
                cell.swith.isOn = roomInfo?.room?.use_robot ?? false
                cell.useRobotBlock = { [weak self] flag in
                    guard let useRobotBlock = self?.useRobotBlock else { return }
                    useRobotBlock(flag)
                }
                return cell
            }
             */
        } else if indexPath.section == 1 {
            if indexPath.row == 0 {
                let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
                guard !settingImage.isEmpty else { return cell}
                cell.iconView.image = UIImage.voice_image(settingImage[3])
                cell.titleLabel.text = settingName[3]
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

                cell.iconView.image = UIImage.voice_image(settingImage[4])
                cell.titleLabel.text = settingName[4]
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

            cell.iconView.image = UIImage.voice_image(settingImage[5])
            cell.titleLabel.text = settingName[5]
      

            if indexPath.row == 0 {
                cell.contentLabel.text = getSoundType(with: roomInfo?.room?.sound_effect ?? 1)
            } else if indexPath.row == 1 {
                switch ains_state {
                case .high:
                    cell.contentLabel.text = "voice_High".voice_localized()
                case .mid:
                    cell.contentLabel.text = "voice_Medium".voice_localized()
                case .off:
                    cell.contentLabel.text = "voice_off".voice_localized()
                }
            }
            return cell
            
//            if indexPath.row == 0 {
//                let cell: VMSwitchTableViewCell = tableView.dequeueReusableCell(withIdentifier: swIdentifier) as! VMSwitchTableViewCell
//                cell.iconView.image = UIImage(settingImage[0])
//                cell.titleLabel.text = settingName[0]
//                cell.isAudience = isAudience
//                cell.selectionStyle = .none
//                cell.swith.isOn = roomInfo?.room?.use_robot ?? false
//                cell.useRobotBlock = { [weak self] flag in
//                    guard let useRobotBlock = self?.useRobotBlock else { return }
//                    useRobotBlock(flag)
//                }
//                return cell
//            } else if indexPath.row == 1 {
//                let cell: VMSliderTableViewCell = tableView.dequeueReusableCell(withIdentifier: slIdentifier) as! VMSliderTableViewCell
//                cell.iconView.image = UIImage(settingImage[1])
//                cell.titleLabel.text = settingName[1]
//                cell.isAudience = isAudience
//                cell.selectionStyle = .none
//                cell.volBlock = { [weak self] vol in
//                    guard let volBlock = self?.volBlock else { return }
//                    volBlock(vol)
//                }
//
//                let volume = roomInfo?.room?.robot_volume ?? 50
//                cell.slider.value = Float(volume) / 100.0
//                cell.countLabel.text = "\(volume)"
//                return cell
//            }

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
            default:
                state = .Spatial
            }
        }  else if indexPath.section == 2 {
            switch indexPath.row {
            case 0:
                //最佳音效
                state = .effect
                heightType = .EFFECT
            default:
                state = .Spatial
            }
        } else if indexPath.section == 1 {
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
