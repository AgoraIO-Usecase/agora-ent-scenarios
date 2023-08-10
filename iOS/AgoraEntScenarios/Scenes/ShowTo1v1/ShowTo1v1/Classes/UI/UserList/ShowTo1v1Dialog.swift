//
//  ShowTo1v1Dialog.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/20.
//

import UIKit

private let kDialogAnimationDuration = 0.3
class ShowTo1v1Dialog: UIView {
    fileprivate lazy var iconView = UIImageView(image: UIImage.sceneImage(name: "dialog_icon"))
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
class ShowTo1v1NoDataDialog: ShowTo1v1Dialog {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = UIFont.systemFont(ofSize: 20)
        label.text = "user_list_waitting".showTo1v1Localization()
        return label
    }()
    private lazy var contentLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.numberOfLines = 0
        let text = NSMutableAttributedString(string: "user_list_nodata_tips".showTo1v1Localization())
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineSpacing = 5
        text.addAttribute(.paragraphStyle, value: paragraphStyle, range: NSRange(location: 0, length: text.length))
        label.font = UIFont.systemFont(ofSize: 14)
        label.attributedText = text
        return label
    }()
    override func _loadSubView() {
        super._loadSubView()
        contentView.addSubview(titleLabel)
        contentView.addSubview(contentLabel)
    }
    
    override func contentSize() -> CGSize {
        return CGSize(width: self.width, height: 214 + UIDevice.current.aui_SafeDistanceBottom)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.sizeToFit()
        titleLabel.aui_centerX = contentView.aui_width / 2
        titleLabel.aui_top = 25
        
        contentLabel.aui_left = 30
        contentLabel.aui_width = contentView.aui_width - 60
        contentLabel.sizeToFit()
        contentLabel.aui_top = titleLabel.aui_bottom + 16
    }
}


private let kDialogTag = 1112234567
//创建房间弹窗
class CreateRoomDialog: ShowTo1v1Dialog, TextLoadingBinderDelegate {
    var stateTitle: String? = "user_list_creating".showTo1v1Localization() 
    var renderStateTitle: String? {
        set {
            createButton.setTitle(newValue ?? "", for: .disabled)
        }
        get {
            return createButton.title(for: .disabled)
        }
    }
    
    var isLoading: Bool = false {
        didSet {
            createButton.isEnabled = !isLoading
            createButton.isLoading = isLoading
            loadingBinder = isLoading ? TextLoadingBinder(delegate: self) : nil
        }
    }
    var userInfo: ShowTo1v1UserInfo?
    var createClosure: ((String)->())?
    
    private var loadingBinder: TextLoadingBinder?
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "create_room_title".showTo1v1Localization()
        label.textColor = UIColor(hexString: "#040a25")
        label.font = UIFont.boldSystemFont(ofSize: 16)
        return label
    }()
    private lazy var textField: UITextField = {
        let tf = UITextField()
        tf.font = UIFont.systemFont(ofSize: 14)
        tf.backgroundColor = UIColor(hexString: "#f8f8f8")
        tf.placeholder = "create_room_tips".showTo1v1Localization()
        let paddingView = UIView(frame: CGRect(x: 0, y: 0, width: 20, height: 10))
        tf.leftView = paddingView
        tf.leftViewMode = .always
        return tf
    }()
    private lazy var randomButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setTitle("create_room_random".showTo1v1Localization(), for: .normal)
        button.setTitleColor(UIColor(hexString: "#363b41"), for: .normal)
        button.setImage(UIImage.sceneImage(name: "icon_random"), for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        button.adjustHorizonAlign(spacing: 3)
        button.sizeToFit()
        button.aui_size = CGSize(width: button.aui_width + 6, height: button.aui_height)
        return button
    }()
    
    private lazy var tipsLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .center
        label.textColor = UIColor(hexString: "#363b41")
        label.font = UIFont.systemFont(ofSize: 10)
        label.backgroundColor = .white.withAlphaComponent(0.2)
        label.clipsToBounds = true
        
        let textAttr = NSAttributedString(string: "call_usage_tips".showTo1v1Localization())
        let attach = NSTextAttachment()
        attach.image = UIImage.sceneImage(name: "icon_notice")!
        let imageSize = CGSize(width: 14, height: 14)
        attach.bounds = CGRect(origin: CGPoint(x: 0, y: (label.font.capHeight - imageSize.height).rounded() / 2), size: imageSize)
        let imgAttr = NSAttributedString(attachment: attach)
        let attr = NSMutableAttributedString()
        attr.append(imgAttr)
        attr.append(textAttr)
        label.attributedText = attr
        return label
    }()
    
    private lazy var createButton: LoadingButton = {
        let button = LoadingButton(type: .custom)
        button.frame = CGRect(x: 0,
                              y: 0,
                              width: 175,
                              height: 42)
        button.setBackgroundImage(UIImage.init(color: UIColor(hexString: "#345dff")!), for: .normal)
        button.setBackgroundImage(UIImage.init(color: UIColor(red: 0.204, green: 0.365, blue: 1, alpha: 1)), for: .normal)
        button.setCornerRadius(21)
        button.setTitle("user_list_create".showTo1v1Localization(), for: .normal)
        button.setTitle("user_list_creating".showTo1v1Localization(), for: .disabled)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        button.setTitleColor(.white, for: .normal)
        button.addTarget(self, action: #selector(_createAction), for: .touchUpInside)
        return button
    }()
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func _loadSubView() {
        super._loadSubView()
        dialogView.addSubview(titleLabel)
        dialogView.addSubview(randomButton)
        dialogView.addSubview(textField)
        dialogView.addSubview(tipsLabel)
        dialogView.addSubview(createButton)
        
        iconView.isHidden = true
        
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardShow(note:)), name: UIResponder.keyboardWillShowNotification, object: nil)

        NotificationCenter.default.addObserver(self, selector: #selector(keyboardHidden(note:)), name: UIResponder.keyboardWillHideNotification, object: nil)
    }
    
    @objc private func keyboardShow(note: Notification) {
        let keyboardHeight = (note.userInfo?["UIKeyboardBoundsUserInfoKey"] as? CGRect)?.height ?? 0
        UIView.animate(withDuration: kDialogAnimationDuration) {
            self.dialogView.aui_bottom = self.aui_height + (self.dialogView.aui_height - self.createButton.aui_bottom) - keyboardHeight
        }
    }

    @objc private func keyboardHidden(note: Notification) {
        UIView.animate(withDuration: kDialogAnimationDuration) {
            self.dialogView.aui_bottom = self.aui_height
        }
    }
    
    override func contentSize() -> CGSize {
        return CGSize(width: self.width, height: 232 + UIDevice.current.aui_SafeDistanceBottom)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.sizeToFit()
        titleLabel.aui_tl = CGPoint(x: 38, y: 34)
        randomButton.aui_br = CGPoint(x: dialogView.aui_width - 26, y: titleLabel.aui_bottom)
        textField.frame = CGRect(x: 30, y: titleLabel.aui_bottom + 9, width: dialogView.aui_width - 60, height: 44)
        textField.setCornerRadius(22)
        tipsLabel.sizeToFit()
        tipsLabel.centerX = textField.centerX
        tipsLabel.aui_top = textField.aui_bottom + 29
        createButton.centerX = textField.centerX
        createButton.aui_bottom = dialogView.aui_height - UIDevice.current.aui_SafeDistanceBottom - 19
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesEnded(touches, with: event)
        guard let point = touches.first?.location(in: self), !dialogView.frame.contains(point) else {return}
        hiddenAnimation()
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
    
    static func show(user: ShowTo1v1UserInfo, createClosure: @escaping (String)->()) -> CreateRoomDialog? {
        CreateRoomDialog.hidden()
        guard let window = getWindow() else {return nil}
        let dialog = CreateRoomDialog(frame: window.bounds)
        dialog.userInfo = user
        dialog.createClosure = createClosure
        dialog.tag = kDialogTag
        window.addSubview(dialog)
        dialog.showAnimation()
        return dialog
    }
    
    static func hidden() {
        getWindow()?.viewWithTag(kDialogTag)?.removeFromSuperview()
    }
    
    @objc private func _createAction() {
        createClosure?(textField.text ?? "")
    }
}

//主叫弹窗
class CallerDialog: ShowTo1v1Dialog, TextLoadingBinderDelegate {
    var cancelClosure: (()->())?
    var stateTitle: String? = "call_state_connecting".showTo1v1Localization() {
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
    private var loadingBinder: TextLoadingBinder?
    fileprivate var userInfo: ShowTo1v1UserInfo? {
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
        
        loadingBinder = TextLoadingBinder(delegate: self)
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
    
    static func show(user: ShowTo1v1UserInfo) -> CallerDialog? {
        CallerDialog.hidden()
        guard let window = getWindow() else {return nil}
        let dialog = CallerDialog(frame: window.bounds)
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
