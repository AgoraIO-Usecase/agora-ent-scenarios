//
//  Pure1v1Dialog.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/20.
//

import UIKit

class Pure1v1Dialog: UIView {
    private lazy var iconView = UIImageView(image: UIImage.sceneImage(name: "dialog_icon"))
    private lazy var gradientLayer: CAGradientLayer = {
        let layer = CAGradientLayer()
        layer.colors = [
            UIColor(hexString: "#F6F2FF")!.cgColor,
            UIColor(hexString: "#FFFFFF")!.cgColor,
        ]

        return layer
    }()
    private lazy var contentView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 20
        view.clipsToBounds = true
        return view
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubView()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate func _loadSubView() {
        backgroundColor = .clear
        addSubview(contentView)
        contentView.layer.addSublayer(gradientLayer)
        contentView.addSubview(iconView)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        contentView.frame = bounds
        gradientLayer.frame = CGRect(x: 0, y: 0, width: contentView.aui_width, height: 58)
        iconView.aui_size = CGSize(width: 106, height: 100)
    }
}

//房间无人
class Pure1v1NoDataDialog: Pure1v1Dialog {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = UIFont.systemFont(ofSize: 20)
        label.text = "user_list_waitting".pure1v1Localization()
        return label
    }()
    private lazy var contentLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.numberOfLines = 0
        label.font = UIFont.systemFont(ofSize: 14)
        label.text = "user_list_nodata_tips".pure1v1Localization()
        return label
    }()
    override func _loadSubView() {
        super._loadSubView()
        addSubview(titleLabel)
        addSubview(contentLabel)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.sizeToFit()
        titleLabel.aui_centerX = self.aui_width / 2
        titleLabel.aui_top = 25
        
        contentLabel.aui_left = 30
        contentLabel.aui_width = self.aui_width - 60
        contentLabel.sizeToFit()
        contentLabel.aui_top = titleLabel.aui_bottom + 40
    }
}

private let kDialogTag = 1112234567

//主叫弹窗
class Pure1v1CallerDialog: Pure1v1Dialog {
    var cancelClosure: (()->())?
    fileprivate var userInfo: Pure1v1UserInfo? {
        didSet {
            avatarView.sd_setImage(with: URL(string: userInfo?.avatar ?? ""))
            userNameLabel.text = userInfo?.userName ?? ""
        }
    }
    private lazy var avatarView: UIImageView = {
        let view = UIImageView()
        view.clipsToBounds = true
        return view
    }()
    
    private lazy var userNameLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = UIFont.systemFont(ofSize: 22)
        return label
    }()
    
    private lazy var stateLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = UIFont.systemFont(ofSize: 14)
        label.text = "call_state_waitting".pure1v1Localization()
        return label
    }()
    
    private lazy var cancelButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_reject"), for: .normal)
        button.addTarget(self, action: #selector(_cancelAction), for: .touchUpInside)
        return button
    }()
    
    override func _loadSubView() {
        super._loadSubView()
        addSubview(avatarView)
        addSubview(userNameLabel)
        addSubview(stateLabel)
        addSubview(cancelButton)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarView.aui_size = CGSize(width: 72, height: 72)
        avatarView.centerY = 0
        avatarView.centerX = aui_width / 2
        avatarView.layer.cornerRadius = avatarView.aui_width / 2
        avatarView.layer.borderWidth = 5
        avatarView.layer.borderColor = UIColor.white.cgColor
        
        userNameLabel.sizeToFit()
        stateLabel.sizeToFit()
        userNameLabel.centerX = avatarView.centerX
        stateLabel.centerX = avatarView.centerX
        userNameLabel.aui_top = avatarView.aui_bottom + 18
        
        stateLabel.aui_top = userNameLabel.aui_bottom + 18
        
        cancelButton.aui_size = CGSize(width: 70, height: 70)
        cancelButton.centerX = aui_width / 2
        cancelButton.aui_top = stateLabel.aui_bottom + 62
    }
    
    static func show(user: Pure1v1UserInfo) -> Pure1v1CallerDialog? {
        Pure1v1CallerDialog.hidden()
        guard let window = getWindow() else {return nil}
        let dialog = Pure1v1CallerDialog(frame: CGRect(x: 0, y: window.aui_height - 357, width: window.aui_width, height: 357))
        dialog.userInfo = user
        dialog.tag = kDialogTag
        window.addSubview(dialog)
        
        return dialog
    }
    
    static func hidden() {
        getWindow()?.viewWithTag(kDialogTag)?.removeFromSuperview()
    }
    
    @objc private func _cancelAction() {
        cancelClosure?()
    }
}

//被叫弹窗
class Pure1v1CalleeDialog: Pure1v1Dialog {
    var rejectClosure: (()->())?
    var acceptClosure: (()->())?
    fileprivate var userInfo: Pure1v1UserInfo? {
        didSet {
            avatarView.sd_setImage(with: URL(string: userInfo?.avatar ?? ""))
            userNameLabel.text = userInfo?.userName ?? ""
        }
    }
    private lazy var avatarView: UIImageView = {
        let view = UIImageView()
        view.clipsToBounds = true
        return view
    }()
    
    private lazy var userNameLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = UIFont.systemFont(ofSize: 18)
        return label
    }()
    
    private lazy var stateLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = UIFont.systemFont(ofSize: 14)
        label.text = "call_state_waitting".pure1v1Localization()
        return label
    }()
    
    private lazy var rejectButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_reject"), for: .normal)
        button.addTarget(self, action: #selector(_rejectAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var acceptButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_accept"), for: .normal)
        button.addTarget(self, action: #selector(_acceptAction), for: .touchUpInside)
        return button
    }()
    
    override func _loadSubView() {
        super._loadSubView()
        addSubview(avatarView)
        addSubview(userNameLabel)
        addSubview(stateLabel)
        addSubview(rejectButton)
        addSubview(acceptButton)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarView.aui_size = CGSize(width: 88, height: 88)
        avatarView.aui_top = 34
        avatarView.centerX = aui_width / 2
        avatarView.layer.cornerRadius = avatarView.aui_width / 2
        avatarView.layer.borderWidth = 6
        avatarView.layer.borderColor = UIColor(hexString: "#3252F5")!.withAlphaComponent(0.4).cgColor
        
        userNameLabel.sizeToFit()
        stateLabel.sizeToFit()
        userNameLabel.centerX = avatarView.centerX
        stateLabel.centerX = avatarView.centerX
        userNameLabel.aui_top = avatarView.aui_bottom + 27
        
        stateLabel.aui_top = userNameLabel.aui_bottom + 18
        
        let padding = (aui_width - 140 ) / 3
        rejectButton.aui_size = CGSize(width: 70, height: 70)
        rejectButton.aui_left = padding
        rejectButton.aui_top = stateLabel.aui_bottom + 62
        
        acceptButton.aui_size = rejectButton.aui_size
        acceptButton.aui_left = rejectButton.aui_right + padding
        acceptButton.aui_top = rejectButton.aui_top
    }
    
    static func show(user: Pure1v1UserInfo) -> Pure1v1CalleeDialog? {
        Pure1v1CalleeDialog.hidden()
        guard let window = getWindow() else {return nil}
        let dialog = Pure1v1CalleeDialog(frame: CGRect(x: 0, y: window.aui_height - 403, width: window.aui_width, height: 403))
        dialog.userInfo = user
        dialog.tag = kDialogTag
        window.addSubview(dialog)
        
        return dialog
    }
    
    static func hidden() {
        getWindow()?.viewWithTag(kDialogTag)?.removeFromSuperview()
    }
    
    @objc private func _rejectAction() {
        rejectClosure?()
    }
    
    @objc private func _acceptAction() {
        acceptClosure?()
    }
}
