//
//  AgoraChatRoomBaseRtcUserView.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/30.
//

import UIKit
import SnapKit

public enum AgoraChatRoomBaseUserCellType {
   case AgoraChatRoomBaseUserCellTypeAdd
   case AgoraChatRoomBaseUserCellTypeMute
   case AgoraChatRoomBaseUserCellTypeForbidden
   case AgoraChatRoomBaseUserCellTypeLock
   case AgoraChatRoomBaseUserCellTypeNormalUser
   case AgoraChatRoomBaseUserCellTypeMuteAndLock
   case AgoraChatRoomBaseUserCellTypeAlienNonActive
   case AgoraChatRoomBaseUserCellTypeAlienActive
}

protocol RtcUserViewDelegate: NSObjectProtocol {
    func didRtcUserViewClicked(tag: Int)
}

class AgoraChatRoomBaseRtcUserView: UIView {

    public var cellType: AgoraChatRoomBaseUserCellType = .AgoraChatRoomBaseUserCellTypeAdd {
        didSet {
            
            if cellType == .AgoraChatRoomBaseUserCellTypeAlienActive || cellType == .AgoraChatRoomBaseUserCellTypeAlienNonActive {
                self.bgColor = .white
            } else {
                self.bgColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)
            }
            
            switch cellType {
            case .AgoraChatRoomBaseUserCellTypeAdd:
                self.iconView.isHidden = true
                self.micView.isHidden = true
                self.bgIconView.image = UIImage("icons／solid／add")
            case .AgoraChatRoomBaseUserCellTypeMute:
                self.iconView.isHidden = false
                self.micView.isHidden = false
                self.micView.setState(.forbidden)
                self.bgIconView.image = UIImage("icons／solid／mute")
            case .AgoraChatRoomBaseUserCellTypeForbidden:
                self.iconView.isHidden = false
                self.micView.isHidden = false
                self.micView.setState(.forbidden)
            case .AgoraChatRoomBaseUserCellTypeLock:
                self.iconView.isHidden = true
                self.micView.isHidden = true
                self.bgIconView.image = UIImage("icons／solid／lock")
            case .AgoraChatRoomBaseUserCellTypeNormalUser:
                self.iconView.isHidden = false
                self.micView.isHidden = false
                self.micView.setState(.on)
                self.nameBtn.setImage(UIImage(""), for: .normal)
            case .AgoraChatRoomBaseUserCellTypeMuteAndLock:
                self.iconView.isHidden = true
                self.micView.isHidden = false
                self.micView.setState(.forbidden)
                self.bgIconView.image = UIImage("icons／solid／lock")
            case .AgoraChatRoomBaseUserCellTypeAlienNonActive:
                self.iconView.isHidden = false
                self.micView.isHidden = false
                self.micView.setState(.on)
                self.micView.isHidden = true
                self.nameBtn.setImage(UIImage("guanfang"), for: .normal)
                self.coverView.isHidden = false
                self.activeButton.isHidden = false
            case .AgoraChatRoomBaseUserCellTypeAlienActive:
                self.iconView.isHidden = false
                self.micView.isHidden = false
                self.nameBtn.setImage(UIImage("guanfang"), for: .normal)
                self.coverView.isHidden = true
                self.activeButton.isHidden = true
            }
            
        }
    }
    
    public var iconImgUrl: String = "" {
        didSet {
            self.iconView.image = UIImage(iconImgUrl)
        }
    }
    
    public var ownerIcon: String = "" {
        didSet {
            self.nameBtn.setImage(UIImage(ownerIcon), for: .normal)
        }
    }
    
    public var showMicView: Bool = false {
        didSet {
            if showMicView {
                micView.isHidden = false
                micView.setState(.on)
                micView.setVolume(100)
            } else {
                micView.isHidden = true
            }
        }
    }
    
    public var iconWidth: CGFloat = 60~ {
        didSet {
            self.iconView.layer.cornerRadius = (iconWidth / 2.0)~
            self.iconView.layer.masksToBounds = true
            self.iconView.snp.updateConstraints { make in
                make.width.height.equalTo(iconWidth)
            }
        }
    }
    
    public var nameStr: String = "" {
        didSet {
            self.nameBtn.setTitle(nameStr, for: .normal)
        }
    }
    
    public var bgColor: UIColor = .black {
        didSet {
            self.bgView.backgroundColor = bgColor
        }
    }
    
    public var volume: Int = 0 {
        didSet {
            self.micView.setVolume(volume)
        }
    }

    private var bgView: UIView = UIView()
    public var iconView: UIImageView = UIImageView()
    public var bgIconView: UIImageView = UIImageView()
    public var micView: AgoraMicVolView = AgoraMicVolView()
    public var nameBtn: UIButton = UIButton()
    private var coverView: UIView = UIView()
    private var activeButton: UIButton = UIButton()
    private var targetBtn: UIButton = UIButton()
    
    var clickBlock: (() -> Void)?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        SwiftyFitsize.reference(width: 375, iPadFitMultiple: 0.6)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate func layoutUI() {
        self.bgView.layer.cornerRadius = 30;
        self.bgView.layer.masksToBounds = true
        self.bgView.backgroundColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)
        self.addSubview(self.bgView)

        self.bgIconView.image = UIImage("icons／solid／add")
        self.bgIconView.layer.cornerRadius = 15
        self.bgIconView.layer.masksToBounds = true
        self.bgView.addSubview(self.bgIconView)
        
        self.iconView.image = UIImage("avatar1")
        self.iconView.layer.cornerRadius = 30
        self.iconView.layer.masksToBounds = true
        self.bgView.addSubview(self.iconView)
        
        self.addSubview(micView)
        
        coverView.backgroundColor = .black
        coverView.alpha = 0.5
        coverView.layer.cornerRadius = 30
        coverView.layer.masksToBounds = true
        self.bgView.addSubview(coverView)
        self.coverView.isHidden = true
        
        let alienTap = UITapGestureRecognizer(target: self, action: #selector(alienTap))
        coverView.addGestureRecognizer(alienTap)
        coverView.isUserInteractionEnabled = true

        activeButton.layer.cornerRadius = 8
        activeButton.layer.masksToBounds = true
        activeButton.setTitle(LanguageManager.localValue(key: "active"), for: .normal)
        activeButton.setTitleColor(.white, for: .normal)
        activeButton.setBackgroundImage(UIImage("active"), for: .normal)
        activeButton.titleLabel?.font = UIFont.systemFont(ofSize: 9)
        activeButton.addTargetFor(self, action: #selector(active), for: .touchUpInside)
        self.addSubview(activeButton)
        self.activeButton.isHidden = true
        
        self.nameBtn.setTitleColor(.white, for: .normal)
        self.nameBtn.titleLabel?.font = UIFont.systemFont(ofSize: 11)
        self.nameBtn.titleLabel?.lineBreakMode = .byTruncatingTail
        self.nameBtn.setTitle("", for: .normal)
        self.nameBtn.isUserInteractionEnabled = false;
        self.addSubview(self.nameBtn)
        
        self.targetBtn.addTargetFor(self, action: #selector(tapClick), for: .touchUpInside)
        self.addSubview(self.targetBtn)
        
        self.bgView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self).offset(20);
            make.width.height.equalTo(60)
        }
        
        self.bgIconView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.centerY.equalTo(self.bgView)
            make.width.height.equalTo(22)
        }
        
        self.iconView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self).offset(20);
            make.width.height.equalTo(60)
        }
        
        self.micView.snp.makeConstraints { make in
            make.right.equalTo(self.iconView).offset(-3)
            make.width.height.equalTo(18)
            make.bottom.equalTo(self.iconView.snp.bottom).offset(-2)
        }
        
        coverView.snp.makeConstraints { make in
            make.top.bottom.left.right.equalTo(iconView)
            make.height.width.equalTo(60)
        }
        
        activeButton.snp.makeConstraints { make in
            make.centerX.equalTo(iconView)
            make.bottom.equalTo(iconView)
            make.width.equalTo(40)
            make.height.equalTo(16)
        }
        
        self.nameBtn.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self.iconView.snp.bottom).offset(10)
            make.height.equalTo(20)
            make.left.equalTo(10)
            make.right.equalTo(-10)
        }
        
        self.targetBtn.snp.makeConstraints { make in
            make.left.right.bottom.equalTo(self.nameBtn)
            make.top.equalTo(self.bgView)
        }
    }
   
    @objc private func tapClick(sender: UIButton) {
        guard let clickBlock = clickBlock else {
            return
        }
        clickBlock()
    }
    
    @objc private func active() {
        guard let clickBlock = clickBlock else {
            return
        }
        clickBlock()
    }
    
    @objc private func alienTap() {
        guard let clickBlock = clickBlock else {
            return
        }
        clickBlock()
    }
}
