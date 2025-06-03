//
//  RealNameAlertViewController.swift
//  AgoraEntScenarios
//
//  Created by qinhui on 2024/12/31.
//

import UIKit
import SnapKit
import WebKit
import SVProgressHUD
import AgoraCommon

// 添加协议类型枚举
enum PrivacyType {
    case summary    // 隐私政策摘要
    case guide      // 隐私保护指引
    case agreement  // 用户协议
}

class RealNameAlertViewController: AgoraAlertViewController {
    // 添加协议点击回调
    var privacyCallback: ((PrivacyType) -> Void)?
    
    // 记录键盘高度
    private var keyboardHeight: CGFloat = 0
    
    // 添加回调闭包
    var confirmCallback: ((String, String) -> Void)?
    
    private lazy var containerView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 20
        view.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        return view
    }()
    
    private lazy var headerView: UIView = {
        let view = UIView()
        let gradientLayer = CAGradientLayer()
        gradientLayer.colors = [
            UIColor(red: 243/255.0, green: 239/255.0, blue: 255/255.0, alpha: 1).cgColor,
            UIColor(red: 1, green: 1, blue: 1, alpha: 0).cgColor
        ]
        gradientLayer.startPoint = CGPoint(x: 0.5, y: 0)
        gradientLayer.endPoint = CGPoint(x: 0.5, y: 1)
        gradientLayer.cornerRadius = 20
        view.layer.addSublayer(gradientLayer)
        return view
    }()
    
    private lazy var handleView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(red: 0.83, green: 0.81, blue: 0.90, alpha: 1)
        view.layer.cornerRadius = 2
        return view
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("realname_alert_title", comment: "")
        label.font = .systemFont(ofSize: 18, weight: .semibold)
        label.textColor = UIColor(hex: 0x040925)
        label.textAlignment = .center
        return label
    }()
    
    private lazy var subtitleLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("realname_alert_des", comment: "")
        label.font = .systemFont(ofSize: 12)
        label.textColor = UIColor(hex: 0x979BBA)
        label.textAlignment = .left
        label.numberOfLines = 0
        return label
    }()
    
    private lazy var nameTextField: UITextField = {
        let textField = UITextField()
        textField.placeholder = NSLocalizedString("realname_name_textfield", comment: "")
        textField.backgroundColor = UIColor(hex: 0xF4F7FF)
        textField.layer.cornerRadius = 8
        textField.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 12, height: 0))
        textField.leftViewMode = .always
        textField.font = .systemFont(ofSize: 14, weight: .medium)
        textField.delegate = self
        textField.clearButtonMode(.whileEditing)
        return textField
    }()
    
    private lazy var idTextField: UITextField = {
        let textField = UITextField()
        textField.placeholder = NSLocalizedString("realname_card_textfield", comment: "")
        textField.backgroundColor = UIColor(hex: 0xF4F7FF)
        textField.layer.cornerRadius = 8
        textField.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 12, height: 0))
        textField.leftViewMode = .always
        textField.font = .systemFont(ofSize: 14, weight: .medium)
        textField.delegate = self
        textField.clearButtonMode(.whileEditing)
        return textField
    }()
    
    private lazy var checkboxButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(named: "icon_privacy_un_select"), for: .normal)
        button.setImage(UIImage(named: "icon_privacy_select_button"), for: .selected)
        button.addTarget(self, action: #selector(checkboxTapped), for: .touchUpInside)
        return button
    }()
    
    private lazy var privacyLabel: UILabel = {
        let label = UILabel()
        
        // 创建段落样式
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineSpacing = 4
        paragraphStyle.lineBreakMode = .byWordWrapping
        
        let text = NSMutableAttributedString(string: NSLocalizedString("realname_hight_light_text1", comment: ""), attributes: [
            .foregroundColor: UIColor(hex: 0x979BBA),
            .font: UIFont.systemFont(ofSize: 12),
            .paragraphStyle: paragraphStyle
        ])
        
        // 添加隐私保护指引
        text.append(NSAttributedString(string: NSLocalizedString("realname_hight_light_text2", comment: ""), attributes: [
            .foregroundColor: UIColor(hex: 0x2E6CF6),
            .font: UIFont.systemFont(ofSize: 12),
            NSAttributedString.Key(rawValue: "CustomLink"): "privacy_guide"
        ]))
        
        text.append(NSAttributedString(string: NSLocalizedString("realname_hight_light_text3", comment: ""), attributes: [
            .foregroundColor: UIColor(hex: 0x979BBA),
            .font: UIFont.systemFont(ofSize: 12)
        ]))
        
        // 添加用户协议
        text.append(NSAttributedString(string: NSLocalizedString("realname_hight_light_text4", comment: ""), attributes: [
            .foregroundColor: UIColor(hex: 0x2E6CF6),
            .font: UIFont.systemFont(ofSize: 12),
            NSAttributedString.Key(rawValue: "CustomLink"): "user_agreement"
        ]))
        
        label.attributedText = text
        label.numberOfLines = 0
        label.isUserInteractionEnabled = true
        
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handlePrivacyTap(_:)))
        label.addGestureRecognizer(tapGesture)
        
        return label
    }()
    
    private lazy var cancelButton: UIButton = {
        let button = UIButton()
        button.setTitle(NSLocalizedString("realname_alert_cancel_title", comment: ""), for: .normal)
        button.setTitleColor(UIColor(hex: 0x303553), for: .normal)
        button.backgroundColor = UIColor(hex: 0x08062F).withAlphaComponent(0.05)
        button.layer.cornerRadius = 12
        button.titleLabel?.font = .systemFont(ofSize: 15, weight: .medium)
        button.addTarget(self, action: #selector(cancelButtonTapped), for: .touchUpInside)
        return button
    }()
    
    private lazy var confirmButton: UIButton = {
        let button = UIButton()
        button.setTitle(NSLocalizedString("realname_alert_enter_title", comment: ""), for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.backgroundColor = UIColor(hex: 0x2E6CF6)
        button.layer.cornerRadius = 12
        button.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
        button.alpha = 0.4
        button.isEnabled = false
        button.addTarget(self, action: #selector(confirmButtonTapped), for: .touchUpInside)
        return button
    }()
    
    private var originalContainerBottom: Constraint?
        
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupConstraints()
        setupKeyboardObservers()
        setupTapGesture()
        isBackgroundDismissEnabled = false
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    private func setupUI() {
        view.addSubview(containerView)
        containerView.addSubview(headerView)
        headerView.addSubview(handleView)
        containerView.addSubview(titleLabel)
        containerView.addSubview(subtitleLabel)
        containerView.addSubview(nameTextField)
        containerView.addSubview(idTextField)
        containerView.addSubview(checkboxButton)
        containerView.addSubview(privacyLabel)
        containerView.addSubview(cancelButton)
        containerView.addSubview(confirmButton)
    }
    
    private func setupConstraints() {
        containerView.snp.makeConstraints { make in
            make.left.right.bottom.equalToSuperview()
            make.height.equalTo(372)
        }
        
        headerView.snp.makeConstraints { make in
            make.top.left.right.equalToSuperview()
            make.height.equalTo(70)
        }
        
        handleView.snp.makeConstraints { make in
            make.top.equalTo(8)
            make.centerX.equalToSuperview()
            make.width.equalTo(37)
            make.height.equalTo(3)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.top.equalTo(handleView.snp.bottom).offset(4)
            make.centerX.equalToSuperview()
        }
        
        subtitleLabel.snp.makeConstraints { make in
            make.top.equalTo(titleLabel.snp.bottom).offset(4)
            make.left.right.equalToSuperview().inset(20)
        }
        
        nameTextField.snp.makeConstraints { make in
            make.top.equalTo(subtitleLabel.snp.bottom).offset(20)
            make.left.right.equalToSuperview().inset(20)
            make.height.equalTo(48)
        }
        
        idTextField.snp.makeConstraints { make in
            make.top.equalTo(nameTextField.snp.bottom).offset(12)
            make.left.right.equalToSuperview().inset(20)
            make.height.equalTo(48)
        }
        
        checkboxButton.snp.makeConstraints { make in
            make.top.equalTo(idTextField.snp.bottom).offset(20)
            make.left.equalTo(20)
            make.width.height.equalTo(16)
        }
        
        privacyLabel.snp.makeConstraints { make in
            make.top.equalTo(checkboxButton)
            make.left.equalTo(checkboxButton.snp.right).offset(6)
            make.right.equalTo(-20)
        }
        
        cancelButton.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.bottom.equalTo(-20)
            make.height.equalTo(48)
            make.width.equalTo((UIScreen.main.bounds.width - 56) / 2)
        }
        
        confirmButton.snp.makeConstraints { make in
            make.right.equalTo(-20)
            make.bottom.equalTo(cancelButton)
            make.height.equalTo(48)
            make.width.equalTo(cancelButton)
        }
    }
    
    private func setupKeyboardObservers() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardWillShow),
            name: UIResponder.keyboardWillShowNotification,
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardWillHide),
            name: UIResponder.keyboardWillHideNotification,
            object: nil
        )
    }
    
    private func setupTapGesture() {
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTapGesture))
        view.addGestureRecognizer(tapGesture)
        
        let containerTapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTapGesture))
        containerView.addGestureRecognizer(containerTapGesture)
    }
    
    @objc private func keyboardWillShow(_ notification: Notification) {
        guard let keyboardFrame = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect else {
            return
        }
        
        keyboardHeight = keyboardFrame.height
        refreshUI()
    }
    
    private func refreshUI() {
        let activeTextField = nameTextField.isFirstResponder ? nameTextField : idTextField
        
        // 如果没有活跃的输入框或键盘没显示，重置位置
        guard activeTextField.isFirstResponder, keyboardHeight > 0 else {
            UIView.animate(withDuration: 0.25) {
                self.containerView.transform = .identity
            }
            return
        }
        
        // 重要：先重置 transform，以获取原始位置
        containerView.transform = .identity
        
        let textFieldFrame = activeTextField.convert(activeTextField.bounds, to: view)
        let textFieldBottom = textFieldFrame.maxY
        
        let keyboardTop = UIScreen.main.bounds.height - keyboardHeight
        let minimumSpacing: CGFloat = 20
        
        // 计算需要移动的距离，使输入框始终保持在键盘上方固定距离
        let offset = textFieldBottom - keyboardTop + minimumSpacing
        
        self.containerView.transform = CGAffineTransform(translationX: 0, y: -offset)
    }
    
    @objc private func keyboardWillHide(_ notification: Notification) {
        guard let duration = notification.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? Double else {
            return
        }
        
        keyboardHeight = 0
        UIView.animate(withDuration: duration) {
            self.containerView.transform = .identity
        }
    }
    
    @objc private func handleTapGesture(_ gesture: UITapGestureRecognizer) {
        let location = gesture.location(in: view)
        
        let activeTextField = nameTextField.isFirstResponder ? nameTextField : (idTextField.isFirstResponder ? idTextField : nil)
        
        if let textField = activeTextField,
           textField.frame.contains(location) {
            return
        }
        
        view.endEditing(true)
    }
    
    @objc private func checkboxTapped() {
        checkboxButton.isSelected.toggle()
        updateConfirmButtonState()
    }
    
    @objc private func cancelButtonTapped() {
        dismiss(animated: false)
    }
    
    @objc private func confirmButtonTapped() {
        // 检查是否勾选协议
        guard checkboxButton.isSelected else {
            SVProgressHUD.showError(withStatus: NSLocalizedString("realname_toast_des", comment: ""))
            return
        }
        
        // 获取输入内容
        guard let name = nameTextField.text, !name.isEmpty,
              let idNumber = idTextField.text, !idNumber.isEmpty else {
            return
        }
        
        // 处理认证逻辑
        startRealNameAuthentication(name: name, idCard: idNumber)
    }
    
    private func updateConfirmButtonState() {
        let hasName = !(nameTextField.text?.isEmpty ?? true)
        let hasIdNumber = !(idTextField.text?.isEmpty ?? true)
        let isEnabled = hasName && hasIdNumber
        
        // 更新按钮状态
        confirmButton.alpha = isEnabled ? 1.0 : 0.4
        confirmButton.isEnabled = isEnabled
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        // 更新渐变层的frame
        if let gradientLayer = headerView.layer.sublayers?.first as? CAGradientLayer {
            gradientLayer.frame = headerView.bounds
        }
    }
    
    // 处理隐私协议点击
    @objc private func handlePrivacyTap(_ gesture: UITapGestureRecognizer) {
        let label = gesture.view as! UILabel
        let text = label.attributedText!
        let point = gesture.location(in: label)
        
        let index = label.indexOfAttributedTextCharacterAtPoint(point: point)
        
        if let link = text.attribute(NSAttributedString.Key(rawValue: "CustomLink"), at: index, effectiveRange: nil) as? String {
            let vc = PrivacyWebViewController()
            
            switch link {
            case "privacy_guide":
                vc.url = "https://fullapp.oss-cn-beijing.aliyuncs.com/scenarios/privacy.html"
                showPrivacyVC(vc)
            case "user_agreement":
                vc.url = "https://fullapp.oss-cn-beijing.aliyuncs.com/scenarios/service.html"
                showPrivacyVC(vc)
            default:
                break
            }
        }
    }
    
    private func showPrivacyVC(_ viewController: UIViewController) {
        let nav = UINavigationController(rootViewController: viewController)
        nav.modalPresentationStyle = .fullScreen
        present(nav, animated: true)
    }
    
    private func startRealNameAuthentication(name: String, idCard: String) {
        SVProgressHUD.show()
        let model = RealNameAuthenticationModel()
        model.realName = name
        model.idCard = idCard
        model.request { [weak self] error, res in
            SVProgressHUD.dismiss()
            if let err = error {
                SVProgressHUD.showError(withStatus: err.localizedDescription)
                return
            }
         
            self?.dismiss(animated: true)
        }
    }
}

extension UIColor {
    convenience init(hex: Int) {
        self.init(
            red: CGFloat((hex >> 16) & 0xFF) / 255.0,
            green: CGFloat((hex >> 8) & 0xFF) / 255.0,
            blue: CGFloat(hex & 0xFF) / 255.0,
            alpha: 1.0
        )
    }
}

// MARK: - UITextFieldDelegate
extension RealNameAlertViewController: UITextFieldDelegate {
    func textFieldDidChangeSelection(_ textField: UITextField) {
        updateConfirmButtonState()
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if textField == nameTextField {
            // 延迟一帧执行，确保键盘和焦点状态都已更新
            DispatchQueue.main.async {
                self.idTextField.becomeFirstResponder()
//                self.refreshUI()
            }
        } else {
            textField.resignFirstResponder()
        }
        return true
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let text = textField.text else { return true }
        let newLength = text.count + string.count - range.length
        
        if textField == nameTextField {
            // 限制姓名输入2-15个字符
            if newLength > 15 {
                return false
            }
            return true
        } else if textField == idTextField {
            // 限制身份证号输入18位数字和字母
            if newLength > 18 {
                return false
            }
            
            // 只允许输入数字和字母
            let allowedCharacters = CharacterSet(charactersIn: "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
            let characterSet = CharacterSet(charactersIn: string)
            if !allowedCharacters.isSuperset(of: characterSet) {
                return false
            }
            return true
        }
        return true
    }
}

// 添加一个扩展来帮助计算点击位置
extension UILabel {
    func indexOfAttributedTextCharacterAtPoint(point: CGPoint) -> Int {
        guard let attributedText = attributedText else { return -1 }
        
        let textStorage = NSTextStorage(attributedString: attributedText)
        let layoutManager = NSLayoutManager()
        let textContainer = NSTextContainer(size: bounds.size)
        
        textContainer.lineFragmentPadding = 0
        textContainer.maximumNumberOfLines = numberOfLines
        textContainer.lineBreakMode = lineBreakMode
        
        layoutManager.addTextContainer(textContainer)
        textStorage.addLayoutManager(layoutManager)
        
        let index = layoutManager.characterIndex(for: point, in: textContainer, fractionOfDistanceBetweenInsertionPoints: nil)
        return index
    }
}

class PrivacyWebViewController: UIViewController {
    var url: String = "" {
        didSet {
            if let url = URL(string: url) {
                let request = URLRequest(url: url)
                webView.load(request)
            }
        }
    }
    
    private lazy var webView: WKWebView = {
        let config = WKWebViewConfiguration()
        let webView = WKWebView(frame: .zero, configuration: config)
        webView.navigationDelegate = self
        return webView
    }()
    
    private lazy var closeButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage(named: "back"), for: .normal)
        button.addTarget(self, action: #selector(closeButtonTapped), for: .touchUpInside)
        return button
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupConstraints()
    }
    
    private func setupUI() {
        view.backgroundColor = .white
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: closeButton)
        view.addSubview(webView)
    }
    
    private func setupConstraints() {
        webView.snp.makeConstraints { make in
            make.top.equalTo(view.safeAreaLayoutGuide)
            make.left.right.bottom.equalToSuperview()
        }
    }
    
    @objc private func closeButtonTapped() {
        dismiss(animated: true)
    }
}

// MARK: - WKNavigationDelegate
extension PrivacyWebViewController: WKNavigationDelegate {
    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
        // 可以在这里添加加载指示器
    }
    
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        // 可以在这里隐藏加载指示器
    }
    
    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        // 可以在这里处理加载失败的情况
    }
}

@objcMembers
public class RealNameAuthenticationModel: VLCommonNetworkModel {
    public var realName: String?
    public var idCard: String?
    
    public override init() {
        super.init()
        self.method = .post
        self.interfaceName = "/api-login/users/realNameAuth"
    }
}
