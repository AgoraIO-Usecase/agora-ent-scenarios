//
//  ShowTo1v1RoomCell.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/28.
//

import Foundation
import SDWebImage

class ShowTo1v1RoomCell: UICollectionViewCell {
    var callClosure: ((ShowTo1v1RoomInfo?)->())?
    var roomInfo: ShowTo1v1RoomInfo? {
        didSet {
            bgImageView.image = roomInfo?.bgImage()
            contentImageView.image = bgImageView.image
            userNameLabel.text = roomInfo?.userName
            avatarView.sd_setImage(with: URL(string: roomInfo?.avatar ?? ""))
            roomNameLabel.text = roomInfo?.roomName
            
            let textAttr = NSAttributedString(string: " \(roomInfo?.roomName ?? "")")
            let attach = NSTextAttachment()
            attach.image = UIImage.sceneImage(name: "live_roomname")
            let imageSize = CGSize(width: 14, height: 14)
            attach.bounds = CGRect(origin: CGPoint(x: 0, y: (roomNameLabel.font.capHeight - imageSize.height).rounded() / 2), size: imageSize)
            let imgAttr = NSAttributedString(attachment: attach)
            let attr = NSMutableAttributedString()
            attr.append(imgAttr)
            attr.append(textAttr)
            roomNameLabel.attributedText = attr
            
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
    
    lazy var canvasView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hexString: "#0038ff")
        return view
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
    private lazy var userNameLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 20)
        return label
    }()
    
    // 房间名称
    private lazy var roomNameLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    
    // 头像
    private lazy var avatarView: UIImageView = UIImageView()
    //呼叫按钮
    private lazy var callButton: ShowTo1v1TouchWaveView = {
        let button =  ShowTo1v1TouchWaveView()
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
        clipsToBounds = true
        contentView.addSubview(bgImageView)
        bgImageView.addSubview(blurView)
        contentView.addSubview(contentImageView)
        contentImageView.layer.addSublayer(gradientLayer)
        contentImageView.addSubview(canvasView)
        contentImageView.addSubview(userNameLabel)
        contentImageView.addSubview(avatarView)
        contentImageView.addSubview(roomNameLabel)
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
        avatarView.frame = CGRect(x: 15, y: contentImageView.aui_height - 76 - wh, width: wh, height: wh)
        userNameLabel.sizeToFit()
        userNameLabel.aui_left = avatarView.aui_right + 10
        userNameLabel.aui_centerY = avatarView.aui_centerY
        
        roomNameLabel.sizeToFit()
        roomNameLabel.aui_tl = CGPoint(x: avatarView.aui_left, y: avatarView.aui_bottom + 14)
        
        canvasView.frame = CGRect(x: contentImageView.aui_width - 13 - 110, y: 21, width: 110, height: 164)
        canvasView.setCornerRadius(25)
        callButton.aui_size = CGSize(width: 76, height: 76)
        callButton.aui_right = contentImageView.aui_width - 15
        callButton.aui_centerY = avatarView.aui_centerY
    }
    
    @objc func _callAction() {
        callClosure?(roomInfo)
    }
}
