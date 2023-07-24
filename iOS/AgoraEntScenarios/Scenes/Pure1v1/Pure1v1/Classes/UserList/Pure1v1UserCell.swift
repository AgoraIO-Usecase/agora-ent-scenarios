//
//  Pure1v1UserCell.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/21.
//

import Foundation
import SDWebImage

class Pure1v1UserCell: UICollectionViewCell {
    var callClosure: ((Pure1v1UserInfo?)->())?
    var userInfo: Pure1v1UserInfo? {
        didSet {
            let uid = UInt(userInfo?.userId ?? "") ?? 0
            bgImageView.image = UIImage.sceneImage(name: "user_bg\(uid % 3 + 1)")
            contentImageView.image = bgImageView.image
            nameLabel.text = userInfo?.userName
            avatarView.sd_setImage(with: URL(string: userInfo?.avatar ?? ""))

            callButton.startAnimation()
        }
    }
    
    private lazy var gradientLayer: CAGradientLayer = {
        let layer = CAGradientLayer()
        layer.colors = [
            UIColor(hexString: "#000000")!.withAlphaComponent(0).cgColor,
            UIColor(hexString: "#000000")!.withAlphaComponent(0.5).cgColor,
        ]
        
        return layer
    }()
    
    // 背景图
    private lazy var bgImageView: UIImageView = {
        let view = UIImageView()
        view.contentMode = .scaleAspectFill
        return view
    }()
    private lazy var blurView: UIVisualEffectView = {
        let blurEffect = UIBlurEffect(style: .extraLight);
        let visualEffectView = UIVisualEffectView(effect: blurEffect)
        return visualEffectView
    }()
    
    // 内容容器
    private lazy var contentImageView: UIImageView = {
        let view = UIImageView()
        view.contentMode = .scaleAspectFill
        view.layer.cornerRadius = 20
        view.clipsToBounds = true
        view.isUserInteractionEnabled = true
        return view
    }()
    // 用户名称
    private lazy var nameLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 20)
        return label
    }()
    
    // 头像
    private lazy var avatarView: UIImageView = UIImageView()
    //呼叫按钮
    private lazy var callButton: Pure1v1TouchWaveView = {
        let button =  Pure1v1TouchWaveView()
        button.addTarget(self, action: #selector(_callAction), for: .touchUpInside)
        return button
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubview()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubview() {
        contentView.addSubview(bgImageView)
        bgImageView.addSubview(blurView)
        contentView.addSubview(contentImageView)
        contentImageView.layer.addSublayer(gradientLayer)
        contentImageView.addSubview(nameLabel)
        contentImageView.addSubview(avatarView)
        contentImageView.addSubview(callButton)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        bgImageView.frame = contentView.bounds
        blurView.frame = bgImageView.bounds
        let top = UIDevice.current.aui_SafeDistanceTop + 51
        let bottom = 47.0
        contentImageView.frame = CGRect(x: 15, y: top, width: self.aui_width - 30, height: self.aui_height - bottom - top)
        gradientLayer.frame = CGRect(x: 0, y: contentImageView.aui_height - 254, width: contentImageView.aui_width, height: 254)
        let wh = 32.0
        avatarView.frame = CGRect(x: 15, y: contentImageView.aui_height - 40 - wh, width: wh, height: wh)
        nameLabel.sizeToFit()
        nameLabel.aui_left = avatarView.aui_right + 10
        nameLabel.aui_centerY = avatarView.aui_centerY
        
        callButton.aui_size = CGSize(width: 76, height: 76)
        callButton.aui_right = contentImageView.aui_width - 15
        callButton.aui_centerY = avatarView.aui_centerY
    }
    
    @objc func _callAction() {
        callClosure?(userInfo)
    }
}
