//
//  Pure1v1Dialog.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/20.
//

import UIKit

private let kDialogAnimationDuration = 0.3
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
    fileprivate lazy var dialogView: UIView = {
        let view = UIView()
        view.backgroundColor = .clear
        return view
    }()
    fileprivate lazy var contentView: UIView = {
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
    
    func contentSize() ->CGSize {
        return .zero
    }
    
    fileprivate func _loadSubView() {
        backgroundColor = .clear
        addSubview(dialogView)
        dialogView.addSubview(contentView)
        contentView.layer.addSublayer(gradientLayer)
        contentView.addSubview(iconView)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        let contentSize = contentSize()
        dialogView.frame = CGRect(x: 0, y: self.aui_height - contentSize.height, width: contentSize.width, height: contentSize.height)
        contentView.frame = dialogView.bounds
        gradientLayer.frame = CGRect(x: 0, y: 0, width: contentView.aui_width, height: 58)
        iconView.aui_size = CGSize(width: 106, height: 100)
    }
    
    func showAnimation() {
        
    }
    
    func hiddenAnimation() {
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
        contentView.addSubview(titleLabel)
        contentView.addSubview(contentLabel)
    }
    
    override func contentSize() -> CGSize {
        return CGSize(width: self.width, height: 328)
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
class Pure1v1CallerDialog: Pure1v1Dialog, Pure1v1TextLoadingBinderDelegate {
    var cancelClosure: (()->())?
    var stateTitle: String? = "call_state_waitting".pure1v1Localization() {
        didSet {
            setNeedsLayout()
        }
    }
    var renderStateTitle: String? {
        set {
            stateLabel.text = newValue
            stateLabel.sizeToFit()
        }
        get {
            return stateLabel.text
        }
    }
    private var loadingBinder: Pure1v1TextLoadingBinder?
    fileprivate var userInfo: Pure1v1UserInfo? {
        didSet {
            avatarView.sd_setImage(with: URL(string: userInfo?.avatar ?? ""))
            userNameLabel.text = userInfo?.userName ?? ""
            bgImageView.image = userInfo?.bgImage()
        }
    }
    
    private lazy var bgImageView: UIImageView = {
        let view = UIImageView()
        view.contentMode = .scaleAspectFill
        return view
    }()
    private lazy var bgMaskView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hexString: "#070707")?.withAlphaComponent(0.2)
        return view
    }()
    
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
        return label
    }()
    
    private lazy var cancelButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_reject"), for: .normal)
        button.addTarget(self, action: #selector(_cancelAction), for: .touchUpInside)
        return button
    }()
    
    override func _loadSubView() {
        addSubview(bgImageView)
        bgImageView.addSubview(bgMaskView)
        super._loadSubView()
        dialogView.addSubview(avatarView)
        dialogView.addSubview(userNameLabel)
        dialogView.addSubview(stateLabel)
        dialogView.addSubview(cancelButton)
        
        loadingBinder = Pure1v1TextLoadingBinder(delegate: self)
    }
    
    override func contentSize() -> CGSize {
        return CGSize(width: self.aui_width, height: 357)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        bgImageView.frame = bounds
        bgMaskView.frame = bgImageView.bounds
        
        avatarView.aui_size = CGSize(width: 72, height: 72)
        avatarView.centerY = contentView.aui_top
        avatarView.centerX = aui_width / 2
        avatarView.layer.cornerRadius = avatarView.aui_width / 2
        avatarView.layer.borderWidth = 5
        avatarView.layer.borderColor = UIColor.white.cgColor
        
        userNameLabel.sizeToFit()
        stateLabel.sizeToFit()
        userNameLabel.centerX = avatarView.centerX
        userNameLabel.aui_top = avatarView.aui_bottom + 18
        stateLabel.aui_centerX = aui_width / 2
        stateLabel.aui_top = userNameLabel.aui_bottom + 18
        
        cancelButton.aui_size = CGSize(width: 70, height: 70)
        cancelButton.centerX = aui_width / 2
        cancelButton.aui_top = stateLabel.aui_bottom + 62
    }
    
    override func showAnimation() {
        setNeedsLayout()
        layoutIfNeeded()
        bgImageView.alpha = 0
        dialogView.aui_top = self.aui_height
        UIView.animate(withDuration: kDialogAnimationDuration) {
            self.bgImageView.alpha = 1
            self.dialogView.aui_bottom = self.aui_height
        }
    }
    
    override func hiddenAnimation() {
        UIView.animate(withDuration: kDialogAnimationDuration) {
            self.bgImageView.alpha = 0
            self.dialogView.aui_top = self.aui_height
        } completion: { success in
            self.removeFromSuperview()
        }
    }
    
    static func show(user: Pure1v1UserInfo) -> Pure1v1CallerDialog? {
        Pure1v1CallerDialog.hidden()
        guard let window = getWindow() else {return nil}
        let dialog = Pure1v1CallerDialog(frame: window.bounds)
        dialog.userInfo = user
        dialog.tag = kDialogTag
        window.addSubview(dialog)
        dialog.showAnimation()
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
class Pure1v1CalleeDialog: Pure1v1Dialog, Pure1v1TextLoadingBinderDelegate {
    var rejectClosure: (()->())?
    var acceptClosure: (()->())?
    var stateTitle: String? = "call_state_waitting".pure1v1Localization() {
        didSet {
            setNeedsLayout()
        }
    }
    var renderStateTitle: String? {
        set {
            stateLabel.text = newValue
            stateLabel.sizeToFit()
        }
        get {
            return stateLabel.text
        }
    }
    private var loadingBinder: Pure1v1TextLoadingBinder?
    fileprivate var userInfo: Pure1v1UserInfo? {
        didSet {
            avatarView.sd_setImage(with: URL(string: userInfo?.avatar ?? ""))
            userNameLabel.text = userInfo?.userName ?? ""
        }
    }
    private lazy var avatarBg2View: UIView = {
        let view = UIView(frame: CGRect(origin: .zero, size: CGSize(width: 91, height: 91)))
        view.clipsToBounds = true
        view.layer.cornerRadius = view.aui_width / 2
        view.backgroundColor = .white
        view.layer.borderColor = UIColor(hexString: "#3252F5")!.withAlphaComponent(0.4).cgColor
        view.layer.borderWidth = 6
        return view
    }()
    private lazy var avatarBg1View: UIView = {
        let view = UIView(frame: CGRect(origin: .zero, size: CGSize(width: 103, height: 103)))
        view.clipsToBounds = true
        view.layer.cornerRadius = view.aui_width / 2
        view.backgroundColor = .white
        view.layer.borderColor = UIColor(hexString: "#3252F5")!.withAlphaComponent(0.1).cgColor
        view.layer.borderWidth = 1
        return view
    }()
    private lazy var avatarView: UIImageView = {
        let view = UIImageView(frame: CGRect(origin: .zero, size: CGSize(width: 75, height: 75)))
        view.clipsToBounds = true
        view.layer.cornerRadius = view.aui_width / 2
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
        return label
    }()
    
    private lazy var rejectButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_reject"), for: .normal)
        button.setTitle("call_title_reject".pure1v1Localization(), for: .normal)
        button.setTitleColor(.black, for: .normal)
        button.addTarget(self, action: #selector(_rejectAction), for: .touchUpInside)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        return button
    }()
    
    private lazy var acceptButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_accept"), for: .normal)
        button.setTitle("call_title_accept".pure1v1Localization(), for: .normal)
        button.setTitleColor(.black, for: .normal)
        button.addTarget(self, action: #selector(_acceptAction), for: .touchUpInside)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        return button
    }()
    
    private lazy var tipsLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .center
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 10)
        label.backgroundColor = .white.withAlphaComponent(0.2)
        label.clipsToBounds = true
        
        let textAttr = NSAttributedString(string: "call_usage_tips".pure1v1Localization())
        let attach = NSTextAttachment()
        attach.image = UIImage.sceneImage(name: "icon_notice")
        let imageSize = CGSize(width: 14, height: 14)
        attach.bounds = CGRect(origin: CGPoint(x: 0, y: (label.font.capHeight - imageSize.height).rounded() / 2), size: imageSize)
        let imgAttr = NSAttributedString(attachment: attach)
        let attr = NSMutableAttributedString()
        attr.append(imgAttr)
        attr.append(textAttr)
        label.attributedText = attr
        return label
    }()
    
    override func _loadSubView() {
        super._loadSubView()
        backgroundColor = UIColor(hexString: "#070707")?.withAlphaComponent(0.2)
        addSubview(tipsLabel)
        contentView.addSubview(avatarBg1View)
        avatarBg1View.addSubview(avatarBg2View)
        contentView.addSubview(avatarView)
        contentView.addSubview(userNameLabel)
        contentView.addSubview(stateLabel)
        contentView.addSubview(rejectButton)
        contentView.addSubview(acceptButton)
        
        loadingBinder = Pure1v1TextLoadingBinder(delegate: self)
    }
    
    override func contentSize() -> CGSize {
        return CGSize(width: aui_width, height: 403)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        tipsLabel.sizeToFit()
        tipsLabel.aui_size = CGSize(width: tipsLabel.aui_width + 20, height: tipsLabel.aui_height + 20)
        tipsLabel.layer.cornerRadius = tipsLabel.aui_height / 2
        tipsLabel.aui_centerX = aui_width / 2
        tipsLabel.aui_bottom = dialogView.aui_top - 24
        avatarBg1View.aui_top = 34
        avatarBg1View.centerX = aui_width / 2
        avatarBg2View.center = CGPoint(x: avatarBg1View.aui_width / 2, y: avatarBg1View.aui_height / 2)
        avatarView.aui_center = avatarBg1View.aui_center
        
        userNameLabel.sizeToFit()
        stateLabel.sizeToFit()
        userNameLabel.centerX = avatarBg1View.centerX
        userNameLabel.aui_top = avatarBg1View.aui_bottom + 27
        stateLabel.aui_top = userNameLabel.aui_bottom + 18
        stateLabel.aui_centerX = aui_width / 2
        
        let padding = (aui_width - 140 ) / 3
        rejectButton.aui_size = CGSize(width: 70, height: 98)
        rejectButton.adjustVerticallyAlign(spacing: 10)
        rejectButton.aui_left = padding
        rejectButton.aui_top = stateLabel.aui_bottom + 62
        
        acceptButton.aui_size = rejectButton.aui_size
        acceptButton.adjustVerticallyAlign(spacing: 10)
        acceptButton.aui_left = rejectButton.aui_right + padding
        acceptButton.aui_top = rejectButton.aui_top
    }
    
    override func showAnimation() {
        setNeedsLayout()
        layoutIfNeeded()
        dialogView.aui_top = self.aui_height
        UIView.animate(withDuration: kDialogAnimationDuration) {
            self.dialogView.aui_bottom = self.aui_height
        }
    }
    
    override func hiddenAnimation() {
        UIView.animate(withDuration: kDialogAnimationDuration) {
            self.dialogView.aui_top = self.aui_height
        } completion: { success in
            self.removeFromSuperview()
        }
    }
    
    //头像呼吸动画
    private func _startAnimation() {
        _removeAnimation()
        
        let keyAnim1 = CAKeyframeAnimation(keyPath: "transform.scale")
        keyAnim1.duration = 2.5
        keyAnim1.values = [1, 1.1, 1]
        keyAnim1.beginTime = CACurrentMediaTime()
        keyAnim1.repeatCount = Float.infinity
        
        let keyAnim2 = CAKeyframeAnimation(keyPath: "opacity")
        keyAnim2.duration = 2.5
        keyAnim2.values = [0, 1, 0]
        keyAnim2.beginTime = CACurrentMediaTime()
        keyAnim2.repeatCount = Float.infinity
        
        avatarView.layer.add(keyAnim1, forKey: "callee_animation_scale")
        avatarBg1View.layer.add(keyAnim2, forKey: "callee_animation_alpha")
    }
    
    private func _removeAnimation() {
        avatarView.layer.removeAllAnimations()
        avatarBg1View.layer.removeAllAnimations()
    }
    
    override func willMove(toSuperview newSuperview: UIView?) {
        if newSuperview == nil {
            _removeAnimation()
        } else {
            _startAnimation()
        }
    }
    
    static func show(user: Pure1v1UserInfo) -> Pure1v1CalleeDialog? {
        Pure1v1CalleeDialog.hidden()
        guard let window = getWindow() else {return nil}
        let dialog = Pure1v1CalleeDialog(frame: window.bounds)
        dialog.userInfo = user
        dialog.tag = kDialogTag
        window.addSubview(dialog)
        dialog.showAnimation()
        return dialog
    }
    
    static func hidden() {
        getWindow()?.viewWithTag(kDialogTag)?.removeFromSuperview()
    }
    
    @objc private func _rejectAction() {
        rejectClosure?()
    }
    
    @objc private func _acceptAction() {
        acceptButton.isEnabled = false
        acceptClosure?()
    }
}
