//
//  VMManagerView.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/7.
//

import UIKit
import ZSwiftBaseLib

public enum SAADMIN_ACTION {
    case invite
    case mute
    case lock
}

class SAVMManagerView: UIView {
    private lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 56)).backgroundColor(.clear).setGradient([UIColor(red: 0.929, green: 0.906, blue: 1, alpha: 1), UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)])
    }()

    private var lineImgView: UIImageView = .init()
    private var bgView: UIView = .init()
    private var addView: UIImageView = .init()
    private var iconView: UIImageView = .init()
    private var nameLabel: UILabel = .init()
    private var roleBtn: UIButton = .init()
    private var lineView: UIView = .init()
    private var sepView: UIView = .init()
    private var sep2View: UIView = .init()
    private var inviteBtn: UIButton = .init()
    private var muteBtn: UIButton = .init()
    private var lockBtn: UIButton = .init()
    private var kfBtn: UIButton = .init()
    // private var micView: AgoraMicVolView = AgoraMicVolView()
    public var isOwner: Bool = false

    public var micInfo: SARoomMic? {
        didSet {
            // 0:正常状态 1:闭麦 2:禁言 3:锁麦 4:锁麦和禁言 -1:空闲
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
                addView.image = UIImage.sceneImage(name: "icons／solid／lock")
                lockBtn.setTitle(sceneLocalized( "Unblock"), for: .normal)
                inviteBtn.setTitleColor(UIColor(hex: "0x979cbb"), for: .normal)
                inviteBtn.isUserInteractionEnabled = false
                nameLabel.text = username
            } else if m_type == 4 {
                iconView.isHidden = true
                roleBtn.isHidden = true
//                micView.isHidden = false
//                micView.setState(.forbidden)
                addView.image = UIImage.sceneImage(name:"icons／solid／lock")
                lockBtn.setTitle(sceneLocalized( "Unblock"), for: .normal)
                muteBtn.setTitle(sceneLocalized( "Unmute"), for: .normal)
                inviteBtn.setTitleColor(UIColor(hex: "0x979cbb"), for: .normal)
                inviteBtn.isUserInteractionEnabled = false
                nameLabel.text = username
            } else if m_type == 1 {
                iconView.isHidden = true
                roleBtn.isHidden = true
//                micView.isHidden = false
//                micView.setState(.forbidden)
                muteBtn.setTitle(sceneLocalized( "Unmute"), for: .normal)
                nameLabel.text = username
            } else if m_type == 0 {
                iconView.isHidden = false
                iconView.sd_setImage(with: URL(string: iconStr), placeholderImage: UIImage.sceneImage(name: "mine_avatar_placeHolder"), context: nil)
                nameLabel.text = username
//                micView.setState(.on)
//                micView.setVolume(100)
                // micView.isHidden = false
                inviteBtn.setTitle(sceneLocalized( "Kick"), for: .normal)
            } else if m_type == 2 {
                iconView.isHidden = false
                iconView.sd_setImage(with: URL(string: iconStr), placeholderImage: UIImage.sceneImage(name: "mine_avatar_placeHolder"), context: nil)
                nameLabel.text = username
//                micView.setState(.forbidden)
//                micView.isHidden = false
                inviteBtn.setTitle(sceneLocalized( micInfo?.member != nil ? "Kick" : "Invite"), for: .normal)
                muteBtn.setTitle(sceneLocalized( "Unmute"), for: .normal)
            }

            roleBtn.isHidden = !isOwner
        }
    }

    var resBlock: ((SAADMIN_ACTION, Bool) -> Void)?

    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .white
        layoutUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func layoutUI() {
        let path = UIBezierPath(roundedRect: bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer = CAShapeLayer()
        layer.path = path.cgPath
        self.layer.mask = layer

        addSubview(cover)

        lineImgView.frame = CGRect(x: ScreenWidth / 2.0 - 20, y: 8, width: 40, height: 4)
        lineImgView.image = UIImage.sceneImage(name:"pop_indicator")
        addSubview(lineImgView)

        bgView.frame = CGRect(x: ScreenWidth / 2 - 32, y: 40, width: 64, height: 64)
        bgView.backgroundColor = UIColor(hex: "0xdad9e3")
        bgView.layer.cornerRadius = 32
        bgView.layer.masksToBounds = true
        addSubview(bgView)

        addView.frame = CGRect(x: ScreenWidth / 2 - 11, y: 61, width: 22, height: 22)
        addView.image = UIImage.sceneImage(name:"icons／solid／add")
        addSubview(addView)

        iconView.frame = CGRect(x: ScreenWidth / 2 - 32, y: 40, width: 64, height: 64)
        iconView.image = UIImage(named:"avatar1")
        iconView.layer.cornerRadius = 32
        iconView.layer.masksToBounds = true
        addSubview(iconView)
        iconView.layer.borderColor = UIColor(hex: "0x979797").cgColor
        iconView.layer.borderWidth = 0.5
        iconView.isHidden = true

        nameLabel.frame = CGRect(x: ScreenWidth / 2.0 - 100, y: 110, width: 200, height: 20)
        nameLabel.text = ""
        nameLabel.textAlignment = .center
        nameLabel.font = UIFont.systemFont(ofSize: 14, weight: .bold)
        nameLabel.textColor = UIColor(hex: "0x333333")
        addSubview(nameLabel)

        roleBtn.frame = CGRect(x: ScreenWidth / 2.0 - 50, y: 135, width: 100, height: 20)
        roleBtn.setImage(UIImage.sceneImage(name:"Landlord"), for: .normal)
        roleBtn.setTitle(sceneLocalized( "host"), for: .normal)
        roleBtn.setTitleColor(.black, for: .normal)
        roleBtn.font(UIFont.systemFont(ofSize: 11))
        addSubview(roleBtn)
        roleBtn.isHidden = true

        lineView.frame = CGRect(x: 0, y: 160, width: ScreenWidth, height: 1)
        lineView.backgroundColor = UIColor(hex: "0x979797")
        addSubview(lineView)

        inviteBtn.frame = CGRect(x: 20, y: 170, width: ScreenWidth / 3.0 - 40, height: 40)
        inviteBtn.setTitleColor(UIColor(hex:"0x156ef3"), for: .normal)
        inviteBtn.setTitle(sceneLocalized( "Invite"), for: .normal)
        inviteBtn.font(UIFont.systemFont(ofSize: 14))
        inviteBtn.tag = 300
        inviteBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        addSubview(inviteBtn)

//        micView.frame = CGRect(x: self.bounds.size.width / 2.0 + 10, y: 85, width: 20, height: 20)
//        micView.setState(.on)
//        micView.setVolume(100)
//        self.addSubview(micView)
//        micView.isHidden = true

        muteBtn.frame = CGRect(x: ScreenWidth / 3.0 + 20, y: 170, width: ScreenWidth / 3.0 - 40, height: 40)
        muteBtn.setTitleColor(UIColor(hex: "0x156ef3"), for: .normal)
        muteBtn.setTitle(sceneLocalized( "Mute"), for: .normal)
        muteBtn.font(UIFont.systemFont(ofSize: 14))
        muteBtn.tag = 301
        muteBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        addSubview(muteBtn)

        lockBtn.frame = CGRect(x: ScreenWidth / 3.0 * 2 + 20, y: 170, width: ScreenWidth / 3.0 - 40, height: 40)
        lockBtn.setTitleColor(UIColor(hex:"0x156ef3"), for: .normal)
        lockBtn.setTitle(sceneLocalized( "Block"), for: .normal)
        lockBtn.font(UIFont.systemFont(ofSize: 14))
        lockBtn.tag = 302
        lockBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        addSubview(lockBtn)

        sepView.frame = CGRect(x: ScreenWidth / 3.0, y: 180, width: 1, height: 20)
        sepView.backgroundColor = UIColor(hex: "0x979797")
        addSubview(sepView)

        sep2View.frame = CGRect(x: ScreenWidth / 3.0 * 2, y: 180, width: 1, height: 20)
        sep2View.backgroundColor = UIColor(hex: "0x979797")
        addSubview(sep2View)
    }

    @objc private func click(sender: UIButton) {
        var state: SAADMIN_ACTION = .invite
        var flag = false
        guard let micInfo = micInfo else {
            return
        }
        guard let resBlock = resBlock else {
            return
        }

        // 0:正常状态 1:闭麦 2:禁言 3:锁麦 4:锁麦和禁言 -1:空闲
        let m_type = micInfo.status
        let user: SAUser? = micInfo.member
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
