
//
//  VMMuteView.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/7.
//

import UIKit
import ZSwiftBaseLib

public enum MUTE_ACTION {
    case unmute //unmute自己
    case mute //mute自己
    case leave //下麦
}

class VMMuteView: UIView {
    
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
    private var muteBtn: UIButton = UIButton()
    private var leaveBtn: UIButton = UIButton()
    private var sepView: UIView = UIView()
  
  //  private var micView: AgoraMicVolView = AgoraMicVolView()
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

            leaveBtn.isHidden = isOwner
            sepView.isHidden = isOwner
            roleBtn.isHidden = !isOwner
            muteBtn.frame = isOwner ? CGRect(x: 0, y: 170, width: self.bounds.size.width, height: 40) : CGRect(x: self.bounds.size.width / 2.0, y: 170, width: self.bounds.size.width / 2.0, height: 40)
            iconView.image = UIImage(iconStr)
            if m_type == 0 {
                iconView.isHidden = false
                nameLabel.text = username
//                micView.setState(.on)
//                micView.setVolume(100)
//                micView.isHidden = false
            } else if m_type == 1 || m_type == 2{
                iconView.isHidden = false
//                micView.isHidden = false
//                micView.setState(.forbidden)
                muteBtn.setTitle(LanguageManager.localValue(key: "Unmute"), for: .normal)
                nameLabel.text = username
            }
        }
    }
    
    var resBlock: ((MUTE_ACTION) -> Void)?
    
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
        bgView.backgroundColor = UIColor.HexColor(hex: 0xdad9e9, alpha: 1)
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
        iconView.isHidden = true
        
        nameLabel.frame = CGRect(x: ScreenWidth/2.0 - 100, y: 110, width: 200, height: 20)
        nameLabel.text = ""
        nameLabel.textColor = UIColor.HexColor(hex: 0x333333, alpha: 1)
        nameLabel.font = UIFont.systemFont(ofSize: 14, weight: .bold)
        nameLabel.textAlignment = .center
        self.addSubview(nameLabel)
        
        roleBtn.frame = CGRect(x: ScreenWidth/2.0 - 50, y: 135, width: 100, height: 20)
        roleBtn.setImage(UIImage("Landlord"), for: .normal)
        roleBtn.setTitle(" \(LanguageManager.localValue(key: "host"))", for: .normal)
        roleBtn.setTitleColor(UIColor(red: 0.2, green: 0.2, blue: 0.2, alpha: 1), for: .normal)
        roleBtn.font(UIFont.systemFont(ofSize: 11))
        self.addSubview(roleBtn)
        self.roleBtn.isHidden = true
        
        lineView.frame = CGRect(x: 0, y: 160, width: ScreenWidth, height: 1)
        lineView.backgroundColor = UIColor.HexColor(hex: 0x979797, alpha: 0.12)
        self.addSubview(lineView)
        
//        micView.frame = CGRect(x: self.bounds.size.width / 2.0 + 10, y: 85, width: 20, height: 20)
//        micView.setState(.on)
//        micView.setVolume(100)
//        self.addSubview(micView)
//        micView.isHidden = true
        
        leaveBtn.frame = CGRect(x: 0, y: 170, width: ScreenWidth / 2.0, height: 40)
        leaveBtn.setTitleColor(UIColor(red: 21/255.0, green: 110/255.0, blue: 243/255.0, alpha: 1), for: .normal)
        leaveBtn.setTitle(LanguageManager.localValue(key: "Off-Stage"), for: .normal)
        leaveBtn.font(UIFont.systemFont(ofSize: 14))
        leaveBtn.tag = 400
        leaveBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        self.addSubview(leaveBtn)
        
        muteBtn.frame = CGRect(x: ScreenWidth / 2, y: 170, width: ScreenWidth / 2.0, height: 40)
        muteBtn.setTitleColor(UIColor(red: 21/255.0, green: 110/255.0, blue: 243/255.0, alpha: 1), for: .normal)
        muteBtn.setTitle(LanguageManager.localValue(key: "Mute"), for: .normal)
        muteBtn.font(UIFont.systemFont(ofSize: 14))
        muteBtn.tag = 401
        muteBtn.addTargetFor(self, action: #selector(click), for: .touchUpInside)
        self.addSubview(muteBtn)
        
        sepView.frame = CGRect(x: ScreenWidth / 2.0, y: 180, width: 1, height: 20)
        sepView.backgroundColor = .separator
        self.addSubview(sepView)

    }
    
    @objc private func click(sender: UIButton) {
        var state: MUTE_ACTION = .mute
        guard let micInfo = micInfo else {
            return
        }
        guard let resBlock = resBlock else {
            return
        }
        
        let m_type = micInfo.status
        if sender.tag == 400 {
            state = .leave
        } else {
            state = m_type == 0 ? .mute : .unmute
        }

        resBlock(state)
    }

}

