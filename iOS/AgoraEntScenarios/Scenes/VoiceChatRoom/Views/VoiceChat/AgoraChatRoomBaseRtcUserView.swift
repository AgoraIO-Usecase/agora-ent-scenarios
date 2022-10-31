//
//  AgoraChatRoomBaseRtcUserView.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/30.
//

import SnapKit
import UIKit

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
                bgColor = .white
            } else {
                bgColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)
            }

            switch cellType {
            case .AgoraChatRoomBaseUserCellTypeAdd:
                iconView.isHidden = true
                micView.isHidden = true
                bgIconView.image = UIImage("icons／solid／add")
            case .AgoraChatRoomBaseUserCellTypeMute:
                iconView.isHidden = false
                micView.isHidden = false
                micView.setState(.forbidden)
                bgIconView.image = UIImage("icons／solid／mute")
            case .AgoraChatRoomBaseUserCellTypeForbidden:
                iconView.isHidden = false
                micView.isHidden = false
                micView.setState(.forbidden)
            case .AgoraChatRoomBaseUserCellTypeLock:
                iconView.isHidden = true
                micView.isHidden = true
                bgIconView.image = UIImage("icons／solid／lock")
            case .AgoraChatRoomBaseUserCellTypeNormalUser:
                iconView.isHidden = false
                micView.isHidden = false
                micView.setState(.on)
                nameBtn.setImage(UIImage(""), for: .normal)
            case .AgoraChatRoomBaseUserCellTypeMuteAndLock:
                iconView.isHidden = true
                micView.isHidden = false
                micView.setState(.forbidden)
                bgIconView.image = UIImage("icons／solid／lock")
            case .AgoraChatRoomBaseUserCellTypeAlienNonActive:
                iconView.isHidden = false
                micView.isHidden = false
                micView.setState(.on)
                micView.isHidden = true
                nameBtn.setImage(UIImage("guanfang"), for: .normal)
                coverView.isHidden = false
                activeButton.isHidden = false
            case .AgoraChatRoomBaseUserCellTypeAlienActive:
                iconView.isHidden = false
                micView.isHidden = false
                nameBtn.setImage(UIImage("guanfang"), for: .normal)
                coverView.isHidden = true
                activeButton.isHidden = true
            }
        }
    }

    public var iconImgUrl: String = "" {
        didSet {
            iconView.image = UIImage(iconImgUrl)
        }
    }

    public var ownerIcon: String = "" {
        didSet {
            nameBtn.setImage(UIImage(ownerIcon), for: .normal)
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
            nameBtn.setTitle(nameStr, for: .normal)
        }
    }

    public var bgColor: UIColor = .black {
        didSet {
            bgView.backgroundColor = bgColor
        }
    }

    public var volume: Int = 0 {
        didSet {
            micView.setVolume(volume)
        }
    }

    private var bgView: UIView = .init()
    public var iconView: UIImageView = .init()
    public var bgIconView: UIImageView = .init()
    public var micView: AgoraMicVolView = .init()
    public var nameBtn: UIButton = .init()
    private var coverView: UIView = .init()
    private var activeButton: UIButton = .init()
    private var targetBtn: UIButton = .init()

    var clickBlock: (() -> Void)?

    override init(frame: CGRect) {
        super.init(frame: frame)
        SwiftyFitsize.reference(width: 375, iPadFitMultiple: 0.6)
        layoutUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    fileprivate func layoutUI() {
        bgView.layer.cornerRadius = 30
        bgView.layer.masksToBounds = true
        bgView.backgroundColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.3)
        addSubview(bgView)

        bgIconView.image = UIImage("icons／solid／add")
        bgIconView.layer.cornerRadius = 15
        bgIconView.layer.masksToBounds = true
        bgView.addSubview(bgIconView)

        iconView.image = UIImage("avatar1")
        iconView.layer.cornerRadius = 30
        iconView.layer.masksToBounds = true
        bgView.addSubview(iconView)

        addSubview(micView)

        coverView.backgroundColor = .black
        coverView.alpha = 0.5
        coverView.layer.cornerRadius = 30
        coverView.layer.masksToBounds = true
        bgView.addSubview(coverView)
        coverView.isHidden = true

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
        addSubview(activeButton)
        activeButton.isHidden = true

        nameBtn.setTitleColor(.white, for: .normal)
        nameBtn.titleLabel?.font = UIFont.systemFont(ofSize: 11)
        nameBtn.titleLabel?.lineBreakMode = .byTruncatingTail
        nameBtn.setTitle("", for: .normal)
        nameBtn.isUserInteractionEnabled = false
        addSubview(nameBtn)

        targetBtn.addTargetFor(self, action: #selector(tapClick), for: .touchUpInside)
        addSubview(targetBtn)

        bgView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self).offset(20)
            make.width.height.equalTo(60)
        }

        bgIconView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.centerY.equalTo(self.bgView)
            make.width.height.equalTo(22)
        }

        iconView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self).offset(20)
            make.width.height.equalTo(60)
        }

        micView.snp.makeConstraints { make in
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

        nameBtn.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self.iconView.snp.bottom).offset(10)
            make.height.equalTo(20)
            make.left.equalTo(10)
            make.right.equalTo(-10)
        }

        targetBtn.snp.makeConstraints { make in
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
