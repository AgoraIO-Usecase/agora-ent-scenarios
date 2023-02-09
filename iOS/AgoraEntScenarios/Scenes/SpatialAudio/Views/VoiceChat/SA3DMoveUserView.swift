//
//  AgoraChatRoom3DMoveUserView.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/9/2.
//

import SnapKit
import SVGAPlayer
import UIKit
class SA3DMoveUserView: UIView {
    public var cellType: SABaseUserCellType = .AgoraChatRoomBaseUserCellTypeAdd {
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
                bgIconView.image = UIImage.sceneImage(name: "icons／solid／add")
            case .AgoraChatRoomBaseUserCellTypeMute:
                iconView.isHidden = false
                micView.isHidden = false
                micView.setState(.forbidden)
            case .AgoraChatRoomBaseUserCellTypeForbidden:
                iconView.isHidden = false
                micView.isHidden = false
                micView.setState(.forbidden)
            case .AgoraChatRoomBaseUserCellTypeLock:
                iconView.isHidden = true
                micView.isHidden = true
                bgIconView.image = UIImage.sceneImage(name: "icons／solid／lock")
            case .AgoraChatRoomBaseUserCellTypeNormalUser:
                iconView.isHidden = false
                micView.isHidden = false
                micView.setState(.on)
                nameBtn.setImage(UIImage(named: ""), for: .normal)
            case .AgoraChatRoomBaseUserCellTypeMuteAndLock:
                iconView.isHidden = true
                micView.isHidden = false
                micView.setState(.forbidden)
                bgIconView.image = UIImage.sceneImage(name: "icons／solid／lock")
            case .AgoraChatRoomBaseUserCellTypeAlienNonActive:
                iconView.isHidden = false
                micView.isHidden = false
                micView.setState(.on)
                micView.isHidden = true
                nameBtn.setImage(UIImage.sceneImage(name: "guanfang"), for: .normal)
                coverView.isHidden = false
                activeButton.isHidden = false
            case .AgoraChatRoomBaseUserCellTypeAlienActive:
                iconView.isHidden = false
                micView.isHidden = false
                nameBtn.setImage(UIImage.sceneImage(name: "guanfang"), for: .normal)
                coverView.isHidden = true
                activeButton.isHidden = true
            }
        }
    }

    public var user: SAUser? {
        didSet {
            iconImgUrl = user?.portrait ?? ""
            nameStr = user?.name ?? "\(tag - 200)"
            volume = user?.volume ?? 0
        }
    }

    public var iconImgUrl: String = "" {
        didSet {
            iconView.image = UIImage.sceneImage(name: iconImgUrl)
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
    private var iconView: UIImageView = .init()
    private var bgIconView: UIImageView = .init()
    private var micView: SAMicVolView = .init()
    private var volImgView: UIImageView = .init()
    private var volBgView: UIView = .init()
    private var nameBtn: UIButton = .init()
    private var coverView: UIView = .init()
    private var activeButton: UIButton = .init()
    private var svgaPlayer: SVGAPlayer = .init()
    private var parser: SVGAParser = .init()
    private lazy var arrowImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "sa_middle_arrow"))
        return imageView
    }()
    private var lastAngle: Double = 0

    private var lineView: UIView = .init()
    
    var angle: Double = 0 {
        didSet {
            let value = (angle - 90) / 180.0 * Double.pi
            UIView.animate(withDuration: 0.25, delay: 0, options: .curveLinear) {
                self.arrowImageView.transform = self.arrowImageView.transform.rotated(by: value - self.lastAngle)
            }
            lastAngle = value
        }
    }

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
        lineView.backgroundColor = .clear
        lineView.layer.bounds = CGRect(x: 0, y: 0, width: 30~, height: 82~)
        lineView.layer.anchorPoint = CGPoint(x: 0.5, y: 1)
        addSubview(lineView)

        addSubview(arrowImageView)
        arrowImageView.translatesAutoresizingMaskIntoConstraints = false
        
        bgView.layer.cornerRadius = 40~
        bgView.layer.masksToBounds = true
        bgView.backgroundColor = UIColor(red: 104 / 255.0, green: 128 / 255.0, blue: 1, alpha: 1)
        addSubview(bgView)
        
        arrowImageView.centerXAnchor.constraint(equalTo: bgView.centerXAnchor).isActive = true
        arrowImageView.centerYAnchor.constraint(equalTo: bgView.centerYAnchor, constant: 0).isActive = true
    
        let tap = UITapGestureRecognizer(target: self, action: #selector(tapClick))
        bgView.addGestureRecognizer(tap)
        bgView.isUserInteractionEnabled = true

        lineView.addSubview(svgaPlayer)
        svgaPlayer.loops = 0
        svgaPlayer.clearsAfterStop = true

        parser.parse(withNamed: "一个箭头", in: nil) { [weak self] videoItem in
            self?.svgaPlayer.videoItem = videoItem
            self?.svgaPlayer.startAnimation()
        }

        bgIconView.image = UIImage.sceneImage(name: "icons／solid／add")
        bgIconView.layer.cornerRadius = 15~
        bgIconView.layer.masksToBounds = true
        addSubview(bgIconView)

        iconView.image = UIImage(named: "avatar1")
        iconView.layer.cornerRadius = 37~
        iconView.layer.masksToBounds = true
        addSubview(iconView)

        addSubview(micView)

        nameBtn.setTitleColor(.white, for: .normal)
        nameBtn.titleLabel?.font = UIFont.systemFont(ofSize: 11)~
        nameBtn.setTitle("", for: .normal)
        nameBtn.isUserInteractionEnabled = false
        addSubview(nameBtn)

        lineView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.height.equalTo(82~)
            make.width.equalTo(30~)
            make.top.equalTo(self).offset(40~)
        }

        svgaPlayer.snp.makeConstraints { make in
            make.left.right.equalTo(lineView)
            make.height.equalTo(30~)
            make.width.equalTo(30~)
            make.top.equalTo(8)
        }

        bgView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self).offset(40~)
            make.width.height.equalTo(82~)
        }

        bgIconView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.centerY.equalTo(self.bgView)
            make.width.height.equalTo(30~)
        }

        iconView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self).offset(44~)
            make.width.height.equalTo(74~)
        }

        micView.snp.makeConstraints { make in
            make.right.equalTo(self.iconView).offset(5~)
            make.width.height.equalTo(18~)
            make.bottom.equalTo(self.iconView.snp.bottom).offset(-5~)
        }

        nameBtn.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self.iconView.snp.bottom).offset(10~)
            make.height.equalTo(20~)
        }
    }

    @objc private func tapClick(tap: UITapGestureRecognizer) {
        print("3D 头像点击")
    }
}
