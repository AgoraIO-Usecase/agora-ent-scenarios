//
//  Pure1v1UserCell.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/21.
//

import Foundation
import SDWebImage
import FLAnimatedImage

class PureUserInfoView: UIView {
    lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    
    // 用户名称
    lazy var nameLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 20)
        return label
    }()
    
    // 头像
    lazy var avatarView: UIImageView = UIImageView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubViews() {
        addSubview(titleLabel)
        addSubview(avatarView)
        addSubview(nameLabel)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let wh = 32.0
        titleLabel.sizeToFit()
        titleLabel.frame = CGRect(origin: CGPoint(x: 15, y: 0), size: titleLabel.size)
        
        avatarView.frame = CGRect(x: 15, y: aui_height - wh, width: wh, height: wh)
        avatarView.layer.cornerRadius = wh / 2
        avatarView.clipsToBounds = true
        
        nameLabel.aui_left = avatarView.aui_right + 10
        nameLabel.aui_size = CGSize(width: aui_width - nameLabel.aui_left - 10, height: avatarView.aui_height)
        nameLabel.aui_centerY = avatarView.aui_centerY
    }
    
    func setInfo(title: String, avatarUrl: String, avatarName: String) {
        titleLabel.text = title
        nameLabel.text = avatarName
        avatarView.sd_setImage(with: URL(string: avatarUrl))
    }
}

class Pure1v1UserCell: UICollectionViewCell {
    var callClosure: ((Pure1v1UserInfo?)->())?
    var userInfo: Pure1v1UserInfo? {
        didSet {
            bgImageView.image = userInfo?.bgImage()
            contentImageView.image = bgImageView.image
            remoteUserView.setInfo(title: "user_list_cell_remote_user".pure1v1Localization(),
                                   avatarUrl: userInfo?.avatar ?? "",
                                   avatarName: userInfo?.userName ?? "")
            callButton.startAnimation()
        }
    }
    var localUserInfo: Pure1v1UserInfo? {
        didSet {
            localUserView.setInfo(title: "user_list_cell_local_user".pure1v1Localization(),
                                  avatarUrl: localUserInfo?.avatar ?? "",
                                  avatarName: localUserInfo?.userName ?? "")
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
    
    private lazy var liveGradientLayer: CAGradientLayer = {
        let layer = CAGradientLayer()
        layer.cornerRadius = 10
        layer.colors = [
        UIColor(red: 1, green: 0.429, blue: 0.719, alpha: 1).cgColor,
        UIColor(red: 0.921, green: 0.658, blue: 0.265, alpha: 1).cgColor
        ]
        layer.locations = [0, 1]
        layer.startPoint = CGPoint(x: 0.25, y: 0.5)
        layer.endPoint = CGPoint(x: 0.75, y: 0.5)

        return layer
    }()
    
    private lazy var liveAnimationView: FLAnimatedImageView = {
        let view = FLAnimatedImageView(frame: CGRect(x: 0, y: 0, width: 36, height: 30))
        let path = UIImage.sceneImagePath(name: "Image/user_list_cell_live.gif", bundleName: "Pure1v1")!
        view.sd_setImage(with: URL.init(fileURLWithPath: path), placeholderImage: nil)
        
        return view
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
    //呼叫按钮
    private lazy var callButton: Pure1v1TouchWaveView = {
        let button =  Pure1v1TouchWaveView()
        button.addTarget(self, action: #selector(_callAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var remoteUserView = PureUserInfoView()
    private lazy var localUserView = PureUserInfoView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubview()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubview() {
        clipsToBounds = true
        contentView.addSubview(bgImageView)
        bgImageView.addSubview(blurView)
        contentView.addSubview(contentImageView)
        contentImageView.layer.addSublayer(gradientLayer)
        contentImageView.layer.addSublayer(liveGradientLayer)
        contentImageView.addSubview(liveAnimationView)
        contentImageView.addSubview(localUserView)
        contentImageView.addSubview(remoteUserView)
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
        
        liveAnimationView.aui_tl = CGPoint(x: 11, y: 10)
        liveGradientLayer.frame = liveAnimationView.frame
        
        callButton.aui_size = CGSize(width: 76, height: 76)
        callButton.aui_right = contentImageView.aui_width - 15
        callButton.aui_bottom = contentImageView.aui_height - 20
        
        let userViewHeight = 60.0
        remoteUserView.frame = CGRect(x: 0.0, y: contentImageView.aui_height - userViewHeight - 15, width: callButton.aui_left, height: userViewHeight)
        
        localUserView.frame = CGRect(x: 0.0, y: remoteUserView.aui_top - userViewHeight - 15, width: remoteUserView.aui_width, height: userViewHeight)
    }
    
    @objc func _callAction() {
        callClosure?(userInfo)
    }
}
