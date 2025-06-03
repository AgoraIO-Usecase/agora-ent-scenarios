//
//  AUIAlertView.swift
//  AUIToast
//
//  Created by zhaoyongqiang on 2023/4/10.
//

import UIKit

open class AUIAlertView: UIView {
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
        label.text = aui_localized("title")
        label.textColor = UIColor(red: 27/255.0, green: 16/255.0, blue: 103/255.0, alpha: 1.0)
        label.font = UIFont.systemFont(ofSize: 17)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var contentLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.numberOfLines = 0
        label.textColor = UIColor(red: 27/255.0, green: 16/255.0, blue: 103/255.0, alpha: 0.5)
        label.font = UIFont.systemFont(ofSize: 14)
        label.translatesAutoresizingMaskIntoConstraints = false
        label.isHidden = true
        return label
    }()
    private lazy var textField: UITextField = {
       let textField = UITextField()
        textField.placeholder = aui_localized("contentPlaceholder")
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
        button.setTitle(aui_localized("cancel"), for: .normal)
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
        button.setTitle(aui_localized("confirm"), for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 17)
        button.layer.cornerRadius = 25
//        button.backgroundColor = UIColor(red: 120/255.0, green: 0/255.0, blue: 255/255.0, alpha: 1.0)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.isHidden = true
        button.addTarget(self, action: #selector(clickSureButton), for: .touchUpInside)
        return button
    }()
    private lazy var closeButton: UIButton = {
        let button = UIButton()
        if #available(iOS 13.0, *) {
            button.setImage(UIImage(systemName: "xmark")?.withTintColor(.darkGray, renderingMode: .alwaysOriginal),
                            for: .normal)
        }
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(clickCloseButton), for: .touchUpInside)
        button.isHidden = true
        return button
    }()
    
    private var placeHolderColor: UIColor?
    private var placeHolderFont: UIFont?
    
    //MARK: Public
    public func background(color: UIColor?) -> AUIAlertView {
        backgroundColor = color
        return self
    }
    public func isShowCloseButton(isShow: Bool) -> AUIAlertView {
        closeButton.isHidden = !isShow
        return self
    }
    public func title(title: String?) -> AUIAlertView {
        titleLabelContaniner.isHidden = title == nil
        titleLabel.text = title
        return self
    }
    public func titleColor(color: UIColor?) -> AUIAlertView {
        titleLabel.textColor = color
        return self
    }
    public func titleFont(font: UIFont?) -> AUIAlertView {
        titleLabel.font = font
        return self
    }
    public func content(content: String?) -> AUIAlertView {
        contentLabel.isHidden = content == nil
        contentLabel.text = content
        return self
    }
    public func contentTextAligment(textAlignment: NSTextAlignment) -> AUIAlertView {
        contentLabel.textAlignment = textAlignment
        return self
    }
    
    public func contentAttrs(content: NSAttributedString?) -> AUIAlertView {
        contentLabel.isHidden = content == nil
        contentLabel.attributedText = content
        return self
    }
    public func contentColor(color: UIColor?) -> AUIAlertView {
        contentLabel.textColor = color
        return self
    }
    public func contentFont(font: UIFont?) -> AUIAlertView {
        contentLabel.font = font
        return self
    }
    public func textField(text: String?) -> AUIAlertView {
        textField.isHidden = text == nil
        textField.text = text
        return self
    }
    public func textField(color: UIColor?) -> AUIAlertView {
        textField.textColor = color
        return self
    }
    public func textField(font: UIFont?) -> AUIAlertView {
        textField.font = font
        return self
    }
    public func textField(leftView: UIView?) -> AUIAlertView {
        textField.leftView = leftView
        return self
    }
    public func textField(cornerRadius: CGFloat) -> AUIAlertView {
        textField.layer.cornerRadius = cornerRadius
        textField.layer.masksToBounds = true
        return self
    }
    public func textField(showBottomDivider: Bool) -> AUIAlertView {
        textFieldLineView.isHidden = !showBottomDivider
        return self
    }
    public func textField(bottomDividerColor: UIColor?) -> AUIAlertView {
        textFieldLineView.backgroundColor = bottomDividerColor
        return self
    }
    public func textFieldBackground(color: UIColor?) -> AUIAlertView {
        textField.backgroundColor = color
        return self
    }
    public func textFieldPlaceholder(placeholder: String?) -> AUIAlertView {
        textField.isHidden = placeholder == nil
        textField.placeholder = placeholder
        return self
    }
    public func textFieldPlaceholder(color: UIColor?) -> AUIAlertView {
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
    public func textFieldPlaceholder(font: UIFont?) -> AUIAlertView {
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
    public func leftButton(title: String?) -> AUIAlertView {
        leftButton.isHidden = title == nil
        leftButton.setTitle(title, for: .normal)
        return self
    }
    public func leftButton(color: UIColor?) -> AUIAlertView {
        leftButton.setTitleColor(color, for: .normal)
        return self
    }
    public func leftButton(font: UIFont?) -> AUIAlertView {
        leftButton.titleLabel?.font = font
        return self
    }
    public func leftButton(cornerRadius: CGFloat) -> AUIAlertView {
        leftButton.layer.cornerRadius = cornerRadius
        leftButton.layer.masksToBounds = true
        return self
    }
    public func leftButtonBackground(color: UIColor?) -> AUIAlertView {
        leftButton.backgroundColor = color
        return self
    }
    public func leftButtonBorder(color: UIColor?) -> AUIAlertView {
        leftButton.layer.borderColor = color?.cgColor
        return self
    }
    public func leftButtonBorder(width: CGFloat) -> AUIAlertView {
        leftButton.layer.borderWidth = width
        return self
    }
    @discardableResult
    public func leftButtonTapClosure(onTap: @escaping () -> Void) -> AUIAlertView {
        cancelClosure = onTap
        return self
    }
    public func rightButton(title: String?) -> AUIAlertView {
        rightButton.isHidden = title == nil
        rightButton.setTitle(title, for: .normal)
        return self
    }
    public func rightButton(color: UIColor?) -> AUIAlertView {
        rightButton.setTitleColor(color, for: .normal)
        return self
    }
    public func rightButton(font: UIFont?) -> AUIAlertView {
        rightButton.titleLabel?.font = font
        return self
    }
    public func rightButton(cornerRadius: CGFloat) -> AUIAlertView {
        rightButton.layer.cornerRadius = cornerRadius
        rightButton.layer.masksToBounds = true
        return self
    }
    public func rightButtonBackground(color: UIColor?) -> AUIAlertView {
        rightButton.backgroundColor = color
        return self
    }
    public func rightButtonBorder(color: UIColor?) -> AUIAlertView {
        rightButton.layer.borderColor = color?.cgColor
        return self
    }
    public func rightButtonBorder(width: CGFloat) -> AUIAlertView {
        rightButton.layer.borderWidth = width
        return self
    }
    @discardableResult
    public func rightButtonTapClosure(onTap: @escaping (String?) -> Void) -> AUIAlertView {
        sureClosure = onTap
        return self
    }
    @discardableResult
    public func rightButtonTapClosure(onTap: @escaping () -> Void) -> AUIAlertView {
        rightClosure = onTap
        return self
    }
    @discardableResult
    public func show(fromVC: UIViewController? = nil) -> AUIAlertView {
        AUIAlertManager.show(view: self, fromVC: fromVC, alertPostion: .center, didCoverDismiss: false)
        return self
    }
    @discardableResult
    public func hidden() -> AUIAlertView {
        AUIAlertManager.hiddenView()
        return self
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required public init?(coder: NSCoder) {
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
        AUIAlertManager.hiddenView()
        textField.endEditing(true)
        cancelClosure?()
    }
    
    @objc
    private func clickSureButton(){
        AUIAlertManager.hiddenView()
        textField.endEditing(true)
        sureClosure == nil ? rightClosure?() : sureClosure?(textField.text)
    }
    @objc
    private func clickCloseButton() {
        AUIAlertManager.hiddenView()
        textField.endEditing(true)
    }
}


import SwiftTheme
extension AUIAlertView {
    public class func theme_defaultAlert() -> AUIAlertView {
        let alert = AUIAlertView()
            .theme_background(color: "Alert.backgroundColor")
            .theme_leftButtonBackground(color: "Alert.leftBackgroundColor")
            .theme_leftButton(color: "Alert.leftTextColor")
            .theme_leftButtonBorder(color: "Alert.leftBorderColor").theme_rightButton(color: "Alert.rightTextColor")
            .theme_rightButtonBackground(color: "Alert.rightBackgroundColor")
            .theme_titleColor(color: "Alert.titleColor")
            .theme_titleFont(font: "CommonFont.big")
            .theme_contentColor(color: "Alert.contentTextColor")
            .theme_contentFont(font: "CommonFont.middle").theme_textFieldBackground(color: "Alert.inputBackground").theme_textFieldTextColor(color: "Alert.inputTextColor")
        return alert
    }
    
    public func theme_background(color: ThemeColorPicker?) -> AUIAlertView {
        theme_backgroundColor = color
        return self
    }
    
    //title
    public func theme_titleColor(color: ThemeColorPicker?) -> AUIAlertView {
        titleLabel.theme_textColor = color
        return self
    }
    public func theme_titleFont(font: ThemeFontPicker?) -> AUIAlertView {
        titleLabel.theme_font = font
        return self
    }
    
    public func theme_textFieldBackground(color: ThemeColorPicker?) -> AUIAlertView {
        textField.theme_backgroundColor = color
        return self
    }
    
    public func theme_textFieldTextColor(color: ThemeColorPicker?) -> AUIAlertView {
        textField.theme_textColor = color
        return self
    }
    
    //content
    public func theme_contentColor(color: ThemeColorPicker?) -> AUIAlertView {
        contentLabel.theme_textColor = color
        return self
    }
    public func theme_contentFont(font: ThemeFontPicker?) -> AUIAlertView {
        contentLabel.theme_font = font
        return self
    }
    
    //right button
    public func theme_rightButton(color: ThemeColorPicker?) -> AUIAlertView {
        rightButton.theme_setTitleColor(color, forState: .normal)
        return self
    }
    
    public func theme_rightButton(font: ThemeFontPicker?) -> AUIAlertView {
        rightButton.titleLabel?.theme_font = font
        return self
    }
    public func theme_rightButton(cornerRadius: ThemeCGFloatPicker) -> AUIAlertView {
        rightButton.layer.theme_cornerRadius = cornerRadius
        rightButton.layer.masksToBounds = true
        return self
    }
    
    public func theme_rightButtonBackground(color: ThemeColorPicker?) -> AUIAlertView {
        rightButton.theme_backgroundColor = color
        return self
    }
    
    public func theme_backgroundImage(color: ThemeImagePicker?,state: UIControl.State) -> AUIAlertView {
        self.rightButton.theme_setBackgroundImage(color, forState: state)
        return self
    }
    
    public func theme_titleColor(color: ThemeColorPicker?,state: UIControl.State) -> AUIAlertView {
        self.rightButton.theme_setTitleColor(color, forState: state)
        return self
    }
    
    public func rightButtonBorder(color: ThemeCGColorPicker?) -> AUIAlertView {
        rightButton.layer.theme_borderColor = color
        return self
    }
    public func theme_rightButtonBorder(width: ThemeCGFloatPicker) -> AUIAlertView {
        rightButton.layer.theme_borderWidth = width
        return self
    }
    
    //left button
    public func theme_leftButton(color: ThemeColorPicker?) -> AUIAlertView {
        leftButton.theme_setTitleColor(color, forState: .normal)
        return self
    }
    public func theme_leftButton(font: ThemeFontPicker?) -> AUIAlertView {
        leftButton.titleLabel?.theme_font = font
        return self
    }
    
    public func theme_leftButton(cornerRadius: ThemeCGFloatPicker) -> AUIAlertView {
        leftButton.layer.theme_cornerRadius = cornerRadius
        leftButton.layer.masksToBounds = true
        return self
    }
    
    public func theme_leftButtonBackground(color: ThemeColorPicker?) -> AUIAlertView {
        leftButton.theme_backgroundColor = color
        return self
    }
    
    public func theme_leftButtonBorder(color: ThemeCGColorPicker?) -> AUIAlertView {
        leftButton.layer.theme_borderColor = color
        return self
    }
    public func theme_leftButtonBorder(width: ThemeCGFloatPicker) -> AUIAlertView {
        leftButton.layer.theme_borderWidth = width
        return self
    }
}
