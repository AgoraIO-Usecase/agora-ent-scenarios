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
    public var user: SAUser? {
        didSet {
            iconImgUrl = user?.portrait ?? ""
            nameStr = user?.name ?? "\(tag - 200)"
            volume = user?.volume ?? 0
            micView.setState(user?.mic_status == .mute ? .off : .on)
        }
    }

    public var iconImgUrl: String = "" {
        didSet {
            if iconImgUrl.hasPrefix("http") {
                avatarImageView.sd_setImage(with: URL(string: iconImgUrl))
            } else {
                avatarImageView.image = UIImage.sceneImage(name: iconImgUrl)
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
    private var avatarImageView: UIImageView = .init()
    private var bgIconView: UIImageView = .init()
    private var micView: SAMicVolView = .init()
    private var nameBtn: UIButton = .init()
    private var activeButton: UIButton = .init()
    private lazy var animateContainerView: UIView = {
        let view = UIView()
        return view
    }()
    private var animateImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "3d_arrow_animation/voice_go_00000"))
        imageView.animationImages = (0...45).map({ UIImage.sceneImage(name: String(format: "3d_arrow_animation/voice_go_%05d", $0)) ?? UIImage() })
        imageView.animationDuration = 2.0
        imageView.animationRepeatCount = .max
        imageView.startAnimating()
        imageView.transform = CGAffineTransform(rotationAngle: .pi)
        return imageView
    }()
    private lazy var icon3dImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "SA_3D_icon"))
        return imageView
    }()
    
    private var lastAngle: Double = 0

    public var tapClickBlock:(() -> Void)?
    
    var angle: Double = 180 {
        didSet {
            let value = (angle - 90) / 180.0 * Double.pi
            UIView.animate(withDuration: 0.25, delay: 0, options: .curveLinear) {
                self.animateContainerView.transform = self.animateContainerView.transform.rotated(by: value - self.lastAngle)
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
        addSubview(animateContainerView)
        animateContainerView.addSubview(animateImageView)
        
        bgView.layer.cornerRadius = 40~
        bgView.layer.masksToBounds = true
        bgView.backgroundColor = UIColor(white: 1, alpha: 0.3)
        addSubview(bgView)
    
        let tap = UITapGestureRecognizer(target: self, action: #selector(tapClick))
        bgView.addGestureRecognizer(tap)
        bgView.isUserInteractionEnabled = true

        bgIconView.image = UIImage.sceneImage(name: "icons／solid／add")
        bgIconView.layer.cornerRadius = 15~
        bgIconView.layer.masksToBounds = true
        addSubview(bgIconView)

        avatarImageView.image = UIImage(named: "")
        avatarImageView.layer.cornerRadius = 37~
        avatarImageView.layer.masksToBounds = true
        addSubview(avatarImageView)
        
        addSubview(icon3dImageView)

        addSubview(micView)

        nameBtn.setTitleColor(.white, for: .normal)
        nameBtn.titleLabel?.font = UIFont.systemFont(ofSize: 11)~
        nameBtn.setTitle("", for: .normal)
        nameBtn.isUserInteractionEnabled = false
        addSubview(nameBtn)
        
        bgView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self).offset(40~)
            make.width.height.equalTo(82~)
        }
        
        animateContainerView.snp.makeConstraints { make in
            make.edges.equalTo(bgView)
        }
        
        animateImageView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(animateContainerView.snp.bottom)
            make.width.height.equalTo(26)
        }
        
        bgIconView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.centerY.equalTo(self.bgView)
            make.width.height.equalTo(30~)
        }

        avatarImageView.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self).offset(44~)
            make.width.height.equalTo(74~)
        }
        
        icon3dImageView.snp.makeConstraints { make in
            make.top.equalTo(avatarImageView.snp.top)
            make.centerX.equalTo(avatarImageView.snp.centerX)
        }

        micView.snp.makeConstraints { make in
            make.right.equalTo(self.avatarImageView).offset(5~)
            make.width.height.equalTo(18~)
            make.bottom.equalTo(self.avatarImageView.snp.bottom).offset(-5~)
        }

        nameBtn.snp.makeConstraints { make in
            make.centerX.equalTo(self)
            make.top.equalTo(self.avatarImageView.snp.bottom).offset(10~)
            make.height.equalTo(20~)
        }
    }

    @objc private func tapClick(tap: UITapGestureRecognizer) {
        print("3D 头像点击")
        guard let tapClickBlock = tapClickBlock else {return}
        tapClickBlock()
    }
}
