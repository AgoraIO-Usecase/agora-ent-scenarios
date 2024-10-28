//
//  AIChatAlertView.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/9.
//

import UIKit

class AIChatAlertView: UIView {
    private var cancelClosure:(()->())?
    private var sureClosure:((String?)->())?
    private var rightClosure:(()->())?
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.spacing = 16
        stackView.axis = .vertical
        stackView.alignment = .fill
        stackView.distribution = .fill
        stackView.translatesAutoresizingMaskIntoConstraints = false
        return stackView
    }()
    private lazy var titleLabelContaniner: UIView = {
        let view = UIView()
        view.isHidden = true
        return view
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "标题"
        label.textColor = UIColor(red: 27/255.0, green: 16/255.0, blue: 103/255.0, alpha: 1.0)
        label.font = UIFont.systemFont(ofSize: 17)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var contentLabel: UILabel = {
        let label = UILabel()
        label.text = "2023年2月14日，中国足球协会主席、党委副书记陈戌源涉嫌严重违纪违法，目前正接受中央纪委国家监委驻国家体育总局纪检监察组和湖北省监委审查调查。"
        label.numberOfLines = 0
        label.textColor = UIColor(red: 27/255.0, green: 16/255.0, blue: 103/255.0, alpha: 0.5)
        label.font = UIFont.systemFont(ofSize: 14)
        label.translatesAutoresizingMaskIntoConstraints = false
        label.isHidden = true
        return label
    }()
    private lazy var textField: UITextField = {
       let textField = UITextField()
        textField.placeholder = "请输入内容"
        textField.translatesAutoresizingMaskIntoConstraints = false
        textField.isHidden = true
        textField.leftView = UIView(frame: CGRect(origin: .zero, size: CGSize(width: 10, height: 10)))
        textField.leftViewMode = .always
        return textField
    }()
    private lazy var textFieldLineView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(red: 195/255.0, green: 197/255.0, blue: 254/255.0, alpha: 1)
        view.translatesAutoresizingMaskIntoConstraints = false
        view.isHidden = true
        return view
    }()
    private lazy var buttonContaninerView: UIView = {
        let view = UIView()
        return view
    }()
    private lazy var buttonStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.spacing = 7
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fillEqually
        stackView.translatesAutoresizingMaskIntoConstraints = false
        return stackView
    }()
    private lazy var leftButton: UIButton = {
        let button = UIButton()
        button.setTitle("取消", for: .normal)
        button.setTitleColor(UIColor(red: 120/255.0, green: 0/255.0, blue: 255/255.0, alpha: 1.0), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 17)
        button.layer.cornerRadius = 25
        button.layer.borderColor = UIColor(red: 120/255.0, green: 0/255.0, blue: 255/255.0, alpha: 1.0).cgColor
        button.layer.borderWidth = 1
        button.translatesAutoresizingMaskIntoConstraints = false
        button.isHidden = true
        button.addTarget(self, action: #selector(clickCancelButton), for: .touchUpInside)
        return button
    }()
    private lazy var rightButton: UIButton = {
        let button = UIButton()
        button.setTitle("确定", for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 17)
//        button.layer.cornerRadius = 25
//        button.backgroundColor = UIColor(red: 120/255.0, green: 0/255.0, blue: 255/255.0, alpha: 1.0)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.isHidden = true
        button.addTarget(self, action: #selector(clickSureButton), for: .touchUpInside)
        return button
    }()
    private lazy var closeButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(systemName: "xmark")?.withTintColor(.darkGray, renderingMode: .alwaysOriginal),
                        for: .normal)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(clickCloseButton), for: .touchUpInside)
        button.isHidden = true
        return button
    }()
    
    private var placeHolderColor: UIColor?
    private var placeHolderFont: UIFont?
    
    //MARK: Public
    public func background(color: UIColor?) -> AIChatAlertView {
        backgroundColor = color
        return self
    }
    public func isShowCloseButton(isShow: Bool) -> AIChatAlertView {
        closeButton.isHidden = !isShow
        return self
    }
    public func title(title: String?) -> AIChatAlertView {
        titleLabelContaniner.isHidden = title == nil
        titleLabel.text = title
        return self
    }
    public func titleColor(color: UIColor?) -> AIChatAlertView {
        titleLabel.textColor = color
        return self
    }
    public func titleFont(font: UIFont?) -> AIChatAlertView {
        titleLabel.font = font
        return self
    }
    public func content(content: String?) -> AIChatAlertView {
        contentLabel.isHidden = content == nil
        contentLabel.text = content
        return self
    }
    public func content(textAlignment: NSTextAlignment) -> AIChatAlertView {
        contentLabel.textAlignment = textAlignment
        return self
    }
    public func contentAttrs(content: NSAttributedString?) -> AIChatAlertView {
        contentLabel.isHidden = content == nil
        contentLabel.attributedText = content
        return self
    }
    public func contentColor(color: UIColor?) -> AIChatAlertView {
        contentLabel.textColor = color
        return self
    }
    public func contentFont(font: UIFont?) -> AIChatAlertView {
        contentLabel.font = font
        return self
    }
    public func textField(text: String?) -> AIChatAlertView {
        textField.isHidden = text == nil
        textField.text = text
        return self
    }
    public func textField(color: UIColor?) -> AIChatAlertView {
        textField.textColor = color
        return self
    }
    public func textField(font: UIFont?) -> AIChatAlertView {
        textField.font = font
        return self
    }
    public func textField(leftView: UIView?) -> AIChatAlertView {
        textField.leftView = leftView
        return self
    }
    public func textField(cornerRadius: CGFloat) -> AIChatAlertView {
        textField.layer.cornerRadius = cornerRadius
        textField.layer.masksToBounds = true
        return self
    }
    public func textField(showBottomDivider: Bool) -> AIChatAlertView {
        textFieldLineView.isHidden = !showBottomDivider
        return self
    }
    public func textField(bottmeDividerColor: UIColor?) -> AIChatAlertView {
        textFieldLineView.backgroundColor = bottmeDividerColor
        return self
    }
    public func textFieldBackground(color: UIColor?) -> AIChatAlertView {
        textField.backgroundColor = color
        return self
    }
    public func textFieldPlaceholder(placeholder: String?) -> AIChatAlertView {
        textField.isHidden = placeholder == nil
        textField.placeholder = placeholder
        return self
    }
    public func textFieldPlaceholder(color: UIColor?) -> AIChatAlertView {
        guard let color = color else { return self }
        placeHolderColor = color
        var attr = NSAttributedString(string: textField.placeholder ?? "",
                                      attributes: [.foregroundColor: color])
        if let font = placeHolderFont {
            attr = NSAttributedString(string: textField.placeholder ?? "",
                                      attributes: [.foregroundColor: color,
                                                   .font: font])
        }
        textField.attributedPlaceholder = attr
        return self
    }
    public func textFieldPlaceholder(font: UIFont?) -> AIChatAlertView {
        guard let font = font else { return self }
        placeHolderFont = font
        var attr = NSAttributedString(string: textField.placeholder ?? "",
                                      attributes: [.font: font])
        if let color = placeHolderColor  {
            attr = NSAttributedString(string: textField.placeholder ?? "",
                                      attributes: [.font: font,
                                                   .foregroundColor: color])
        }
        textField.attributedPlaceholder = attr
        return self
    }
    public func leftButton(title: String?) -> AIChatAlertView {
        leftButton.isHidden = title == nil
        leftButton.setTitle(title, for: .normal)
        return self
    }
    public func leftButton(color: UIColor?) -> AIChatAlertView {
        leftButton.setTitleColor(color, for: .normal)
        return self
    }
    public func leftButton(font: UIFont?) -> AIChatAlertView {
        leftButton.titleLabel?.font = font
        return self
    }
    public func leftButton(cornerRadius: CGFloat) -> AIChatAlertView {
        leftButton.layer.cornerRadius = cornerRadius
        leftButton.layer.masksToBounds = true
        return self
    }
    public func leftButtonBackground(color: UIColor?) -> AIChatAlertView {
        leftButton.backgroundColor = color
        return self
    }
    public func leftButtonBorder(color: UIColor?) -> AIChatAlertView {
        leftButton.layer.borderColor = color?.cgColor
        return self
    }
    public func leftButtonBorder(width: CGFloat) -> AIChatAlertView {
        leftButton.layer.borderWidth = width
        return self
    }
    @discardableResult
    public func leftButtonTapClosure(onTap: @escaping () -> Void) -> AIChatAlertView {
        cancelClosure = onTap
        return self
    }
    
    public func rightButtonGradient(colors: [UIColor]?) -> AIChatAlertView {
        guard let colors = colors else { return self }
        let gradientLayer = CAGradientLayer()
        gradientLayer.colors = colors.map { $0.cgColor }
        gradientLayer.startPoint = CGPoint(x: 0.25, y: 0.5)
        gradientLayer.endPoint = CGPoint(x: 0.75, y: 0.5)
        gradientLayer.locations = [0, 0.5, 1]
        gradientLayer.transform = CATransform3DMakeAffineTransform(CGAffineTransform(a: 1, b: 0, c: 0, d: 6.7, tx: 0, ty: -2.85))
        gradientLayer.bounds = self.rightButton.bounds.insetBy(dx: -0.5*self.rightButton.bounds.size.width, dy: -0.5*self.rightButton.bounds.size.height)
        gradientLayer.position = self.rightButton.center
        rightButton.layer.addSublayer(gradientLayer)
        return self
    }
    
    public func rightButton(title: String?) -> AIChatAlertView {
        rightButton.isHidden = title == nil
        rightButton.setTitle(title, for: .normal)
        return self
    }
    public func rightButton(color: UIColor?) -> AIChatAlertView {
        rightButton.setTitleColor(color, for: .normal)
        return self
    }
    public func rightButton(font: UIFont?) -> AIChatAlertView {
        rightButton.titleLabel?.font = font
        return self
    }
    public func rightButton(cornerRadius: CGFloat) -> AIChatAlertView {
        rightButton.layer.cornerRadius = cornerRadius
        rightButton.layer.masksToBounds = true
        return self
    }
    public func rightButtonBackground(color: UIColor?) -> AIChatAlertView {
        rightButton.backgroundColor = color
        return self
    }
    public func rightButtonBackgroundImage(image: UIImage?) -> AIChatAlertView {
        rightButton.setBackgroundImage(image, for: .normal)
        return self
    }
    public func rightButtonBorder(color: UIColor?) -> AIChatAlertView {
        rightButton.layer.borderColor = color?.cgColor
        return self
    }
    public func rightButtonBorder(width: CGFloat) -> AIChatAlertView {
        rightButton.layer.borderWidth = width
        return self
    }
    @discardableResult
    public func rightButtonTapClosure(onTap: @escaping (String?) -> Void) -> AIChatAlertView {
        sureClosure = onTap
        return self
    }
    @discardableResult
    public func rightButtonTapClosure(onTap: @escaping () -> Void) -> AIChatAlertView {
        rightClosure = onTap
        return self
    }
    @discardableResult
    public func show() -> AIChatAlertView {
        AlertManager.show(view: self, alertPostion: .center, didCoverDismiss: false)
        return self
    }
    @discardableResult
    public func hidden() -> AIChatAlertView {
        AlertManager.hiddenView()
        return self
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(stackView)
        titleLabelContaniner.addSubview(titleLabel)
        addSubview(closeButton)
        stackView.addArrangedSubview(titleLabelContaniner)
        stackView.addArrangedSubview(contentLabel)
        stackView.addArrangedSubview(textField)
        textField.addSubview(textFieldLineView)
        stackView.addArrangedSubview(buttonContaninerView)
        buttonContaninerView.addSubview(buttonStackView)
        buttonStackView.addArrangedSubview(leftButton)
        buttonStackView.addArrangedSubview(rightButton)
        
        backgroundColor = .white
        layer.cornerRadius = 16
        layer.shadowColor = UIColor(red: 0.106, green: 0.063, blue: 0.404, alpha: 0.2).cgColor
        layer.shadowRadius = 40
        layer.shadowOpacity = 1
        layer.shadowOffset = CGSize(width: 0, height: 0)
        translatesAutoresizingMaskIntoConstraints = false
        widthAnchor.constraint(equalToConstant: UIScreen.main.bounds.width - 32).isActive = true
        
        stackView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 16).isActive = true
        stackView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -16).isActive = true
        stackView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        stackView.topAnchor.constraint(equalTo: topAnchor, constant: 16).isActive = true
        
        titleLabel.topAnchor.constraint(equalTo: titleLabelContaniner.topAnchor).isActive = true
        titleLabel.centerXAnchor.constraint(equalTo: titleLabelContaniner.centerXAnchor).isActive = true
        titleLabel.bottomAnchor.constraint(equalTo: titleLabelContaniner.bottomAnchor, constant: -16).isActive = true
        
        closeButton.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -10).isActive = true
        closeButton.topAnchor.constraint(equalTo: topAnchor, constant: 10).isActive = true
        closeButton.widthAnchor.constraint(equalToConstant: 20).isActive = true
        closeButton.heightAnchor.constraint(equalToConstant: 20).isActive = true
        
        textField.heightAnchor.constraint(equalToConstant: 50).isActive = true
        
        textFieldLineView.leadingAnchor.constraint(equalTo: textField.leadingAnchor).isActive = true
        textFieldLineView.bottomAnchor.constraint(equalTo: textField.bottomAnchor).isActive = true
        textFieldLineView.trailingAnchor.constraint(equalTo: textField.trailingAnchor).isActive = true
        textFieldLineView.heightAnchor.constraint(equalToConstant: 1).isActive = true
        
        buttonStackView.topAnchor.constraint(equalTo: buttonContaninerView.topAnchor, constant: 16).isActive = true
        buttonStackView.heightAnchor.constraint(equalToConstant: 50).isActive = true
        buttonStackView.leadingAnchor.constraint(equalTo: buttonContaninerView.leadingAnchor).isActive = true
        buttonStackView.trailingAnchor.constraint(equalTo: buttonContaninerView.trailingAnchor).isActive = true
        buttonStackView.bottomAnchor.constraint(equalTo: buttonContaninerView.bottomAnchor, constant: -16).isActive = true
    }
    
    @objc
    private func clickCancelButton(){
        AlertManager.hiddenView()
        textField.endEditing(true)
        cancelClosure?()
    }
    
    @objc
    private func clickSureButton(){
        AlertManager.hiddenView()
        textField.endEditing(true)
        sureClosure == nil ? rightClosure?() : sureClosure?(textField.text)
    }
    @objc
    private func clickCloseButton() {
        AlertManager.hiddenView()
        textField.endEditing(true)
    }
    
    static func showCustomAlert(title: String? = nil,
                                message: String? = nil,
                                confirmTitle: String? = NSLocalizedString("confirm", comment: ""),
                                cancelTitle: String? = NSLocalizedString("cancel", comment: ""),
                                confirm: (()->Void)? = nil,
                                cancel: (()->Void)? = nil) {
        
        AIChatAlertView()
            .title(title: title)
            .content(content: message)
            .content(textAlignment: .center)
            .contentColor(color: UIColor(hexString: "#6C7192"))
            .leftButton(title: cancelTitle)
            .leftButtonBorder(color: .clear)
            .leftButton(color: UIColor(hexString: "#3C4267"))
            .leftButtonBackground(color: UIColor(hexString: "#EFF4FF"))
            .leftButton(cornerRadius: 24)
            .rightButton(title: confirmTitle)
            .rightButton(color: UIColor(hexString: "#FFFFFF"))
            .rightButtonBackground(color: UIColor(hexString: "#219BFF"))
            .rightButton(cornerRadius: 24)
            .leftButtonTapClosure {
                cancel?()
            }.rightButtonTapClosure {
                confirm?()
            }.show()
    }
}

public let cl_screenWidht = UIScreen.main.bounds.width
public let cl_screenHeight = UIScreen.main.bounds.height
class AlertManager: NSObject {
    private struct AlertViewCache {
        var view: UIView?
        var index: Int = 0
    }

    enum AlertPosition {
        case top
        case center
        case bottom
    }

    private static var vc: UIViewController?
    private static var containerView: UIView?
    private static var currentPosition: AlertPosition = .center
    private static var viewCache: [AlertViewCache] = []
    private static var bottomAnchor: NSLayoutConstraint?

    public static func show(view: UIView,
                            alertPostion: AlertPosition = .center,
                            didCoverDismiss: Bool = true)
    {
        let index = viewCache.isEmpty ? 0 : viewCache.count
        viewCache.append(AlertViewCache(view: view, index: index))
        currentPosition = alertPostion
        if vc == nil {
            containerView = UIButton(frame: CGRect(x: 0, y: 0, width: cl_screenWidht, height: cl_screenHeight))
            containerView?.backgroundColor = UIColor(red: 0.0 / 255, green: 0.0 / 255, blue: 0.0 / 255, alpha: 0.0)
        }
        if didCoverDismiss {
            (containerView as? UIButton)?.addTarget(self, action: #selector(tapView), for: .touchUpInside)
        }
        guard let containerView = containerView else { return }
        containerView.addSubview(view)
        view.translatesAutoresizingMaskIntoConstraints = false
        view.alpha = 0
        switch alertPostion {
        case .top:
            view.centerXAnchor.constraint(equalTo: containerView.centerXAnchor).isActive = true
            view.topAnchor.constraint(equalTo: containerView.topAnchor, constant: StatusBarHeight + 50).isActive = true
            
        case .center:
            view.centerXAnchor.constraint(equalTo: containerView.centerXAnchor).isActive = true
            view.centerYAnchor.constraint(equalTo: containerView.centerYAnchor).isActive = true
            
        case .bottom:
            bottomAnchor = view.bottomAnchor.constraint(equalTo: containerView.bottomAnchor)
            view.leadingAnchor.constraint(equalTo: containerView.leadingAnchor).isActive = true
            view.trailingAnchor.constraint(equalTo: containerView.trailingAnchor).isActive = true
        }
        if vc == nil {
            vc = UIViewController()
            vc?.view.layer.contents = nil
            vc?.view.backgroundColor = UIColor.clear
            vc?.view.addSubview(containerView)
            vc?.modalPresentationStyle = .custom
            if let topVC = UIViewController.cl_topViewController() {
                topVC.present(vc!, animated: false) {
                    showAlertPostion(alertPostion: alertPostion, view: view)
                }
            } else {
                vc = nil
            }
        } else {
            showAlertPostion(alertPostion: alertPostion, view: view)
        }
        // 注册键盘出现通知
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(notification:)), name: UIApplication.keyboardWillShowNotification, object: nil)

        // 注册键盘隐藏通知
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(notification:)), name: UIApplication.keyboardWillHideNotification, object: nil)
    }

    private static func showAlertPostion(alertPostion: AlertPosition, view: UIView) {
        containerView?.layoutIfNeeded()
        switch alertPostion {
        case .top, .center:
            showCenterView(view: view)
            
        case .bottom:
            bottomAnchor?.constant = view.frame.height
            bottomAnchor?.isActive = true
            containerView?.layoutIfNeeded()
            showBottomView(view: view)
        }
    }

    private static func showCenterView(view: UIView) {
        if !viewCache.isEmpty {
            viewCache.forEach({ $0.view?.alpha = 0 })
        }
        UIView.animate(withDuration: 0.25, animations: {
            containerView?.backgroundColor = UIColor(red: 0.0 / 255,
                                                     green: 0.0 / 255,
                                                     blue: 0.0 / 255,
                                                     alpha: 0.5)
            view.alpha = 1.0
        })
    }

    private static func showBottomView(view: UIView) {
        if !viewCache.isEmpty {
            viewCache.forEach({ $0.view?.alpha = 0 })
        }
        view.alpha = 1.0
        bottomAnchor?.constant = 0
        bottomAnchor?.isActive = true
        UIView.animate(withDuration: 0.25, animations: {
            containerView?.backgroundColor = UIColor(red: 0.0 / 255,
                                                     green: 0.0 / 255,
                                                     blue: 0.0 / 255,
                                                     alpha: 0.5)
            containerView?.superview?.layoutIfNeeded()
        })
    }

    static func updateViewHeight() {
        UIView.animate(withDuration: 0.25, animations: {
            containerView?.layoutIfNeeded()
        })
    }

    static func hiddenView(all: Bool = true, completion: (() -> Void)? = nil) {
        if vc == nil {
            completion?()
            return
        }
        if currentPosition == .bottom {
            guard let lastView = viewCache.last?.view else { return }
            bottomAnchor?.constant = lastView.frame.height
            bottomAnchor?.isActive = true
        }
        UIView.animate(withDuration: 0.25, animations: {
            if all || viewCache.isEmpty {
                containerView?.backgroundColor = UIColor(red: 255.0 / 255,
                                                         green: 255.0 / 255,
                                                         blue: 255.0 / 255,
                                                         alpha: 0.0)
                containerView?.layoutIfNeeded()
            }
            if currentPosition != .bottom {
                viewCache.last?.view?.alpha = 0
            }
        }, completion: { _ in
            if all || viewCache.isEmpty {
                viewCache.removeAll()
                vc?.dismiss(animated: false, completion: completion)
                vc = nil
            } else {
                viewCache.removeLast()
                viewCache.last?.view?.alpha = 1
            }
        })
    }

    @objc
    private static func tapView() {
        DispatchQueue.main.asyncAfter(deadline: DispatchTime(uptimeNanoseconds: UInt64(0.1))) {
            self.hiddenView()
        }
    }

    private static var originFrame: CGRect = .zero
    // 键盘显示
    @objc private static func keyboardWillShow(notification: Notification) {
        let keyboardHeight = (notification.userInfo?["UIKeyboardBoundsUserInfoKey"] as? CGRect)?.height
        let y = cl_screenHeight - (keyboardHeight ?? 304) - containerView!.frame.height
        if originFrame.origin.y != y {
            originFrame = containerView!.frame
        }
        UIView.animate(withDuration: 0.25) {
            containerView?.frame.origin.y = y
        }
    }

    // 键盘隐藏
    @objc private static func keyboardWillHide(notification: Notification) {
        UIView.animate(withDuration: 0.25) {
            containerView?.frame = originFrame
        } completion: { _ in
            if currentPosition == .bottom {
                hiddenView()
            }
        }
    }
}

extension UIViewController {
    static var keyWindow: UIWindow? {
        // Get connected scenes
        if #available(iOS 13.0, *) {
            return UIApplication.shared.connectedScenes
            // Keep only active scenes, onscreen and visible to the user
                .filter { $0.activationState == .foregroundActive }
            // Keep only the first `UIWindowScene`
                .first(where: { $0 is UIWindowScene })
            // Get its associated windows
                .flatMap({ $0 as? UIWindowScene })?.windows
            // Finally, keep only the key window
                .first(where: \.isKeyWindow)
        } else {
            return UIApplication.shared.keyWindow
        }
    }

    static func cl_topViewController(_ viewController: UIViewController? = nil) -> UIViewController? {
        let viewController = viewController ?? keyWindow?.rootViewController

        if let navigationController = viewController as? UINavigationController,
           !navigationController.viewControllers.isEmpty
        {
            return cl_topViewController(navigationController.viewControllers.last)

        } else if let tabBarController = viewController as? UITabBarController,
                  let selectedController = tabBarController.selectedViewController
        {
            return cl_topViewController(selectedController)

        } else if let presentedController = viewController?.presentedViewController {
            return cl_topViewController(presentedController)
        }
        return viewController
    }
}

