//
//  RoomListCell.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/28.
//

import Foundation
import SDWebImage
import FLAnimatedImage


class RoomUserInfoView: UIView {
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
    
    // 房间名称
    private lazy var roomNameLabel: UILabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    
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
        addSubview(roomNameLabel)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        
        titleLabel.sizeToFit()
        titleLabel.frame = CGRect(origin: CGPoint(x: 15, y: 0), size: titleLabel.size)
        
        let wh = 32.0
        let avatartViewTop = roomNameLabel.attributedText == nil ? aui_height - wh : 30
        avatarView.frame = CGRect(x: 15, y: avatartViewTop, width: wh, height: wh)
        avatarView.layer.cornerRadius = wh / 2
        avatarView.clipsToBounds = true
        
        nameLabel.aui_left = avatarView.aui_right + 10
        nameLabel.aui_size = CGSize(width: aui_width - nameLabel.aui_left - 10, height: avatarView.aui_height)
        nameLabel.aui_centerY = avatarView.aui_centerY
        
        roomNameLabel.sizeToFit()
        roomNameLabel.aui_tl = CGPoint(x: avatarView.aui_left, y: avatarView.aui_bottom + 14)
    }
    
    func setInfo(title: String, avatarUrl: String, avatarName: String, roomName: String? = nil) {
        titleLabel.text = title
        nameLabel.text = avatarName
        avatarView.sd_setImage(with: URL(string: avatarUrl))
        
        if let roomName = roomName {
            let textAttr = NSAttributedString(string: " \(roomName)")
            let attach = NSTextAttachment()
            attach.image = UIImage.sceneImage(name: "live_roomname")
            let imageSize = CGSize(width: 14, height: 14)
            attach.bounds = CGRect(origin: CGPoint(x: 0, y: (roomNameLabel.font.capHeight - imageSize.height).rounded() / 2), size: imageSize)
            let imgAttr = NSAttributedString(attachment: attach)
            let attr = NSMutableAttributedString()
            attr.append(imgAttr)
            attr.append(textAttr)
            roomNameLabel.attributedText = attr
        } else {
            roomNameLabel.attributedText = nil
        }
    }
}

class RoomListCell: UICollectionViewCell {
    var callClosure: ((ShowTo1v1RoomInfo?)->())?
    var tapClosure: ((ShowTo1v1RoomInfo?)->())?
    var roomInfo: ShowTo1v1RoomInfo? {
        didSet {
            bgImageView.image = roomInfo?.bgImage()
            contentImageView.image = bgImageView.image
            
            remoteUserView.setInfo(title: "user_list_cell_remote_user".showTo1v1Localization(),
                                   avatarUrl: roomInfo?.avatar ?? "",
                                   avatarName: roomInfo?.userName ?? "",
                                   roomName: roomInfo?.roomName)
            
            callButton.startAnimation()
        }
    }
    var localUserInfo: ShowTo1v1UserInfo? {
        didSet {
            localUserView.setInfo(title: "user_list_cell_local_user".showTo1v1Localization(),
                                  avatarUrl: localUserInfo?.avatar ?? "",
                                  avatarName: localUserInfo?.userName ?? "")
        }
    }
    
    private lazy var localUserView = RoomUserInfoView()
    private lazy var remoteUserView = RoomUserInfoView()
    
    private lazy var gradientLayer: CAGradientLayer = {
        let layer = CAGradientLayer()
        layer.colors = [
            UIColor(hexString: "#000000")!.withAlphaComponent(0).cgColor,
            UIColor(hexString: "#000000")!.withAlphaComponent(0.5).cgColor,
        ]
        
        return layer
    }()
    
    private lazy var liveTagView: LiveTagView = LiveTagView()
    
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
    
    lazy var canvasView: ThumnbnailCanvasView = {
        let view = ThumnbnailCanvasView()
        view.backgroundColor = UIColor(hexString: "#0038ff")
        let tapGes = UITapGestureRecognizer {[weak self] ges in
            guard let self = self else {return}
            self.tapClosure?(self.roomInfo)
        }
        view.addGestureRecognizer(tapGes)
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
    
    //呼叫按钮
    private lazy var callButton: TouchWaveView = {
        let button =  TouchWaveView()
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
        contentImageView.addSubview(liveTagView)
        contentImageView.addSubview(canvasView)
        contentImageView.addSubview(localUserView)
        contentImageView.addSubview(remoteUserView)
        contentImageView.addSubview(callButton)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        bgImageView.frame = contentView.bounds
        blurView.frame = bgImageView.bounds
        let top = UIDevice.current.aui_SafeDistanceTop + 51
        let bottom = 82.0
        contentImageView.frame = CGRect(x: 15, y: top, width: self.aui_width - 30, height: self.aui_height - bottom - top)
        gradientLayer.frame = CGRect(x: 0, y: contentImageView.aui_height - 254, width: contentImageView.aui_width, height: 254)
        
        
        canvasView.frame = CGRect(x: contentImageView.aui_width - 13 - 110, y: 21, width: 110, height: 164)
        canvasView.setCornerRadius(25)
        callButton.aui_size = CGSize(width: 76, height: 76)
        callButton.aui_right = contentImageView.aui_width - 15
        callButton.aui_bottom = contentImageView.aui_height - 20
        
        liveTagView.sizeToFit()
        liveTagView.aui_tl = CGPoint(x: 11, y: 10)
        
        
        let remoteUserViewHeight = 100.0
        remoteUserView.frame = CGRect(x: 0.0, y: contentImageView.aui_height - remoteUserViewHeight - 15, width: callButton.aui_left, height: remoteUserViewHeight)
        
        let localUserViewHeight = 60.0
        localUserView.frame = CGRect(x: 0.0, y: remoteUserView.aui_top - localUserViewHeight - 15, width: remoteUserView.aui_width, height: localUserViewHeight)
    }
    
    @objc func _callAction() {
        callClosure?(roomInfo)
    }
}
