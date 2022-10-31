//
//  VMManagerView.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/7.
//

import UIKit
import ZSwiftBaseLib

public enum ADMIN_ACTION {
    case invite
    case mute
    case lock
}

class VMManagerView: UIView {
    
    private lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 56)).backgroundColor(.clear).setGradient([UIColor(red: 0.929, green: 0.906, blue: 1, alpha: 1),UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)], [CGPoint(x: 0, y: 0),CGPoint(x: 0, y: 1)])
    }()

    private var lineImgView: UIImageView = UIImageView()
    private var bgView: UIView = UIView()
    private var addView: UIImageView = UIImageView()
    private var iconView: UIImageView = UIImageView()
    private var nameLabel: UILabel = UILabel()
    private var roleBtn: UIButton = UIButton()
    private var lineView: UIView = UIView()
    private var sepView: UIView = UIView()
    private var sep2View: UIView = UIView()
    private var inviteBtn: UIButton = UIButton()
    private var muteBtn: UIButton = UIButton()
    private var lockBtn: UIButton = UIButton()
    private var kfBtn: UIButton = UIButton()
   // private var micView: AgoraMicVolView = AgoraMicVolView()
    public var isOwner: Bool = false
    
    public var micInfo: VRRoomMic? {
        didSet {
            //0:正常状态 1:闭麦 2:禁言 3:锁麦 4:锁麦和禁言 -1:空闲
            let m_type = micInfo?.status
            var username: String = "\(micInfo?.mic_index ?? 0)"
            var iconStr: String = ""
            if let user = micInfo?.member {
                username = user.name ?? "\(String(describing: micInfo?.index))"
                iconStr = user.portrait ?? ""
            }
            if m_type == -1 {
                iconView.isHidden = true
                roleBtn.isHidden = true
               // micView.isHidden = true
                nameLabel.text = username
            } else if m_type == 3 {
                iconView.isHidden = true
                roleBtn.isHidden = true
               // micView.isHidden = true
                addView.image = UIImage("icons／solid／lock")
                lockBtn.setTitle(LanguageManager.localValue(key: "Unblock"), for: .normal)
                inviteBtn.setTitleColor(UIColor.HexColor(hex: 0x979cbb, alpha: 1), for: .normal)
                inviteBtn.isUserInteractionEnabled = false
                nameLabel.text = username
            } else if m_type == 4 {
                iconView.isHidden = true
                roleBtn.isHidden = true
//                micView.isHidden = false
//                micView.setState(.forbidden)
                addView.image = UIImage("icons／solid／lock")
                lockBtn.setTitle(LanguageManager.localValue(key: "Unblock"), for: .normal)
                muteBtn.setTitle(LanguageManager.localValue(key: "Unmute"), for: .normal)
                inviteBtn.setTitleColor(UIColor.HexColor(hex: 0x979cbb, alpha: 1), for: .normal)
                inviteBtn.isUserInteractionEnabled = false
                nameLabel.text = username
            } else if m_type == 1 {
                iconView.isHidden = true
                roleBtn.isHidden = true
//                micView.isHidden = false
//                micView.setState(.forbidden)
                muteBtn.setTitle(LanguageManager.localValue(key: "Unmute"), for: .normal)
                nameLabel.text = username
            } else if m_type == 0 {
                iconView.isHidden = false
                iconView.image = UIImage(iconStr)
                nameLabel.text = username
//                micView.setState(.on)
//                micView.setVolume(100)
               // micView.isHidden = false
                inviteBtn.setTitle(LanguageManager.localValue(key: "Kick"), for: .normal)
            } else if m_type == 2 {
                iconView.isHidden = false
                iconView.image = UIImage(iconStr)
                nameLabel.text = username
//                micView.setState(.forbidden)
//                micView.isHidden = false
                inviteBtn.setTitle(LanguageManager.localValue(key: micInfo?.member != nil ? "Kick" : "Invite"), for: .normal)
                muteBtn.setTitle(LanguageManager.localValue(key: "Unmute"), for: .normal)
            }
            
            roleBtn.isHidden = !isOwner
        }
    }
    
    var resBlock: ((ADMIN_ACTION, Bool) -> Void)?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .white
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        
        let path: UIBezierPath = UIBezierPath(roundedRect: self.bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer: CAShapeLayer = CAShapeLayer()
        layer.path = path.cgPath
        self.layer.mask = layer
        
        self.addSubview(cover)
        
        lineImgView.frame = CGRect(x: ScreenWidth / 2.0 - 20, y: 8, width: 40, height: 4)
        lineImgView.image = UIImage("pop_indicator")
        self.addSubview(lineImgView)
        
        bgView.frame = CGRect(x: ScreenWidth / 2 - 32, y: 40, width: 64, height: 64)
        bgView.backgroundColor = UIColor.HexColor(hex: 0xdad9e3, alpha: 1)
        bgView.layer.cornerRadius = 32
        bgView.layer.masksToBounds = true
        self.addSubview(bgView)
        
        addView.frame = CGRect(x: ScreenWidth / 2 - 11, y: 61, width: 22, height: 22)
        addView.image = UIImage("icons／solid／add")
        self.addSubview(addView)
        
        iconView.frame = CGRect(x: ScreenWidth / 2 - 32, y: 40, width: 64, height: 64)
        iconView.image = UIImage("avatar1")
        iconView.layer.cornerRadius = 32
        iconView.layer.masksToBounds = true
        self.addSubview(iconView)
        iconView.layer.borderColor = UIColor.HexColor(hex: 0x979797, alpha: 0.12).cgColor
        iconView.layer.borderWidth = 0.5
        iconView.isHidden = true
        
        nameLabel.frame = CGRect(x: ScreenWidth/2.0 - 100, y: 110, width: 200, height: 20)
        nameLabel.text = ""
        nameLabel.textAlignment = .center
        nameLabel.font = UIFont.systemFont(ofSize: 14, weight: .bold)
        nameLabel.textColor = UIColor.HexColor(hex: 0x3333, alpha: 1)
        self.addSubview(nameLabel)
        
        roleBtn.frame = CGRect(x: ScreenWidth/2.0 - 50, y: 135, width: 100, height: 20)
        roleBtn.setImage(UIImage("Landlord"), for: .normal)
        roleBtn.setTitle(LanguageManager.localValue(key: "host"), for: .normal)
        roleBtn.setTitleColor(.black, for: .normal)
        roleBtn.font(UIFont.systemFont(ofSize: 11))
        self.addSubview(roleBtn)
        self.roleBtn.isHidden = true
        
        lineView.frame = CGRect(x: 0, y: 160, width: ScreenWidth, height: 1)
        lineView.backgroundColor = UIColor.HexColor(hex: 0x979797, alpha: 0.12)
        self.addSubview(lineView)

        inviteBtn.frame = CGRect(x: 20, y: 170, width: ScreenWidth / 3.0 - 40, height: 40)
        inviteBtn.setTitleColor(UIColor.HexColor(hex: 0x156ef3, alpha: 1), for: .normal)
        inviteBtn.setTitle(LanguageManager.localValue(key: "Invite"), for: .normal)
        inviteBtn.font(UIFont.systemFont(ofSize: 14))
        inviteBtn.tag = 300
        inviteBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        self.addSubview(inviteBtn)
        
//        micView.frame = CGRect(x: self.bounds.size.width / 2.0 + 10, y: 85, width: 20, height: 20)
//        micView.setState(.on)
//        micView.setVolume(100)
//        self.addSubview(micView)
//        micView.isHidden = true
        
        muteBtn.frame = CGRect(x: ScreenWidth / 3.0 + 20, y: 170, width: ScreenWidth / 3.0 - 40, height: 40)
        muteBtn.setTitleColor(UIColor.HexColor(hex: 0x156ef3, alpha: 1), for: .normal)
        muteBtn.setTitle(LanguageManager.localValue(key: "Mute"), for: .normal)
        muteBtn.font(UIFont.systemFont(ofSize: 14))
        muteBtn.tag = 301
        muteBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        self.addSubview(muteBtn)
        
        lockBtn.frame = CGRect(x: ScreenWidth / 3.0 * 2 + 20, y: 170, width: ScreenWidth / 3.0 - 40, height: 40)
        lockBtn.setTitleColor(UIColor.HexColor(hex: 0x156ef3, alpha: 1), for: .normal)
        lockBtn.setTitle(LanguageManager.localValue(key: "Block"), for: .normal)
        lockBtn.font(UIFont.systemFont(ofSize: 14))
        lockBtn.tag = 302
        lockBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        self.addSubview(lockBtn)
        
        sepView.frame = CGRect(x: ScreenWidth / 3.0, y: 180, width: 1, height: 20)
        sepView.backgroundColor = UIColor.HexColor(hex: 0x979797, alpha: 0.12)
        self.addSubview(sepView)
        
        sep2View.frame = CGRect(x: ScreenWidth / 3.0 * 2, y: 180, width: 1, height: 20)
        sep2View.backgroundColor = UIColor.HexColor(hex: 0x979797, alpha: 0.12)
        self.addSubview(sep2View)
    }
    
    @objc private func click(sender: UIButton) {
        var state: ADMIN_ACTION = .invite
        var flag: Bool = false
        guard let micInfo = micInfo else {
            return
        }
        guard let resBlock = resBlock else {
            return
        }

        //0:正常状态 1:闭麦 2:禁言 3:锁麦 4:锁麦和禁言 -1:空闲
        let m_type = micInfo.status
        let user: VRUser? = micInfo.member
        switch m_type {
        case -1:
            if sender.tag == 300 {
                state = .invite
                flag = true
            } else if sender.tag == 301 {
                state = .mute
                flag = true
            } else if sender.tag == 302 {
                state = .lock
                flag = true
            }
        case 0:
            if sender.tag == 300 {
                state = .invite
                flag = false
            } else if sender.tag == 301 {
                state = .mute
                flag = true
            } else if sender.tag == 302 {
                state = .lock
                flag = true
            }
        case 2:
            if sender.tag == 300 {
                state = .invite
                flag = user != nil ? false : true
            } else if sender.tag == 301 {
                state = .mute
                flag = false
            } else if sender.tag == 302 {
                state = .lock
                flag = true
            }
        case 3:
            if sender.tag == 300 {
                state = .invite
                flag = true
            } else if sender.tag == 301 {
                state = .mute
                flag = true
            } else if sender.tag == 302 {
                state = .lock
                flag = false
            }
        case 4:
            if sender.tag == 300 {
    
            } else if sender.tag == 301 {
                state = .mute
                flag = false
            } else if sender.tag == 302 {
                state = .lock
                flag = false
            }
        case 1:
            if sender.tag == 300 {
                state = .invite
                flag = user != nil ? false : true
            } else if sender.tag == 301 {
                state = .mute
                flag = false
            } else if sender.tag == 302 {
                state = .lock
                flag = false
            }
        default:
            break
        }
        
        resBlock(state, flag)
    }

}
