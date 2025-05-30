//
//  AUITextField.swift
//  AUITextField
//
//  Created by zhaoyongqiang on 2023/4/4.
//

import UIKit
import SwiftTheme

public enum AUIDividerSide {
    case all
    case bottom
}

public class AUITextField: UIView {
    private lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.spacing = 0
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fill
        return stackView
    }()
    private lazy var leftIconContainerView: UIView = {
        let view = UIView()
        view.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        view.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        view.isHidden = true
        return view
    }()
    private lazy var leftIconImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    private lazy var textFieldContainerView: UIView = {
        let view = UIView()
        return view
    }()
    private lazy var textField = UITextField()
    private lazy var rightIconContainerView: UIButton = {
        let view = UIButton()
        view.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        view.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        view.isHidden = true
        view.addTarget(self, action: #selector(clickRightButton(sender:)), for: .touchUpInside)
        return view
    }()
    private lazy var rightIconImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    private lazy var topLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = .systemFont(ofSize: 12)
        label.isHidden = true
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var bottomLabel: UILabel = {
        let label = UILabel()
        label.textColor = .red
        label.font = .systemFont(ofSize: 12)
        label.translatesAutoresizingMaskIntoConstraints = false
        label.isHidden = true
        return label
    }()
    private lazy var dividerView: UIView = {
        let view = UIView()
        view.translatesAutoresizingMaskIntoConstraints = false
        view.isHidden = true
        return view
    }()
    
    private var textFieldLeftCons: NSLayoutConstraint?
    private var stackViewLeftCons: NSLayoutConstraint?
    
    // MARK: Public
    public var textEditingChangedClosure: ((String?) -> Void)?
    public var textEditingEndedClosure: ((String?) -> Void)?
    public var clickRightButtonClosure: ((Bool) -> Void)?
    
    @objc public var leftIconImage: UIImage? {
        didSet {
            leftIconImageView.image = leftIconImage
            leftIconContainerView.isHidden = leftIconImage == nil
            textFieldLeftCons?.constant = position == .all ? 16 : 0
            textFieldLeftCons?.isActive = true
            stackViewLeftCons?.constant = ((position == nil || position == .bottom) && leftIconImage != nil) ? -15 : 0
            stackViewLeftCons?.constant = 0
            stackViewLeftCons?.isActive = true
        }
    }
    public override var backgroundColor: UIColor? {
        didSet {
            textFieldLeftCons?.constant = position == .all ? 16 : 0
            textFieldLeftCons?.isActive = true
            stackViewLeftCons?.constant = ((position == nil || position == .bottom) && leftIconImage != nil) ? 0 : 16
            stackViewLeftCons?.isActive = true
        }
    }
    @objc public var rightIconImage: UIImage? {
        didSet {
            rightIconImageView.image = rightIconImage
            rightIconContainerView.isHidden = rightIconImage == nil
        }
    }
    @objc public var rightSelectedIconImage: UIImage?
    
    public var placeHolder: String? {
        didSet {
            textField.placeholder = placeHolder
        }
    }
    
    @objc public var placeHolderColor: UIColor? {
        didSet {
            guard let color = placeHolderColor else { return }
            var attr = NSAttributedString(string: placeHolder ?? "",
                                          attributes: [.foregroundColor: color])
            if let font = placeHolderFont {
                attr = NSAttributedString(string: placeHolder ?? "",
                                          attributes: [.foregroundColor: color,
                                                       .font: font])
            }
            textField.attributedPlaceholder = attr
        }
    }
    @objc public var placeHolderFont: UIFont? {
        didSet {
            guard let font = placeHolderFont else { return }
            var attr = NSAttributedString(string: placeHolder ?? "",
                                          attributes: [.font: font])
            if let color = placeHolderColor  {
                attr = NSAttributedString(string: placeHolder ?? "",
                                          attributes: [.font: font,
                                                       .foregroundColor: color])
            }
            textField.attributedPlaceholder = attr
        }
    }
    
    public var text: String? {
        didSet {
            textField.text = text
        }
    }
    
    @objc public var textColor: UIColor? {
        didSet {
            textField.textColor = textColor
        }
    }
    @objc public var textFont: UIFont? {
        didSet {
            textField.font = textFont
        }
    }
    /// 键盘类型
    public var keyBoardType: UIKeyboardType? {
        didSet {
            textField.keyboardType = keyBoardType ?? .URL
        }
    }
    /// 密码输入框
    public var isSecureTextEntry: Bool = false {
        didSet {
            textField.isSecureTextEntry = isSecureTextEntry
        }
    }
    /// 清除按钮（输入框内右侧小叉）
    public var clearButtonMode: UITextField.ViewMode = .never {
        didSet {
            textField.clearButtonMode = clearButtonMode
        }
    }
    public var textAlignment: NSTextAlignment = .left {
        didSet {
            textField.textAlignment = textAlignment
        }
    }
    public var returnKeyType: UIReturnKeyType = .done {
        didSet {
            textField.returnKeyType = returnKeyType
        }
    }
    public var cornerRadius: CGFloat = 0 {
        didSet {
            layer.cornerRadius = cornerRadius
        }
    }
    public func becomeFirstResponder() {
        textField.becomeFirstResponder()
    }
    public func resignFirstResponder() {
        textField.resignFirstResponder()
    }
    public var topText: String? {
        didSet {
            topLabel.isHidden = topText == nil
            topLabel.text = topText
        }
    }
    public var topTextFont: UIFont? {
        didSet {
            topLabel.font = topTextFont
        }
    }
    public var topTextColor: UIColor? {
        didSet {
            topLabel.textColor = topTextColor
        }
    }
    public var bottomText: String? {
        didSet {
            bottomLabel.isHidden = bottomText == nil
            bottomLabel.text = bottomText
        }
    }
    public var bottomTextFont: UIFont? {
        didSet {
            bottomLabel.font = bottomTextFont
        }
    }
    public var bottomTextColor: UIColor? {
        didSet {
            bottomLabel.textColor = bottomTextColor
        }
    }
    public var dividerColor: UIColor? {
        didSet {
            if position == .all {
                layer.borderColor = dividerColor?.cgColor
            } else {
                dividerView.backgroundColor = dividerColor
            }
        }
    }
    public func divider(color: UIColor, position: AUIDividerSide = .all) {
        self.position = position
        if position == .all {
            layer.borderWidth = 1
            layer.borderColor = color.cgColor
            
        } else {
            dividerView.backgroundColor = color
        }
        dividerView.isHidden = position == .all
        textFieldLeftCons?.constant = (position == .bottom || leftIconImage != nil) ? 0 : 16
        textFieldLeftCons?.isActive = true
        stackViewLeftCons?.constant = (position == .bottom && leftIconImage != nil) ? -15 : 0
        stackViewLeftCons?.isActive = true
    }
    
    private var position: AUIDividerSide?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(stackView)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackViewLeftCons = stackView.leadingAnchor.constraint(equalTo: leadingAnchor)
        stackViewLeftCons?.isActive = true
        stackView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        stackView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        stackView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        
        stackView.addArrangedSubview(leftIconContainerView)
        stackView.addArrangedSubview(textFieldContainerView)
        stackView.addArrangedSubview(rightIconContainerView)
        
        leftIconContainerView.widthAnchor.constraint(equalToConstant: 44).isActive = true
        leftIconContainerView.addSubview(leftIconImageView)
        leftIconImageView.translatesAutoresizingMaskIntoConstraints = false
        leftIconImageView.leadingAnchor.constraint(equalTo: leftIconContainerView.leadingAnchor,
                                               constant: 15).isActive = true
        leftIconImageView.trailingAnchor.constraint(equalTo: leftIconContainerView.trailingAnchor,
                                                constant: -8).isActive = true
        leftIconImageView.centerYAnchor.constraint(equalTo: leftIconContainerView.centerYAnchor).isActive = true
        
        textFieldContainerView.addSubview(textField)
        textField.translatesAutoresizingMaskIntoConstraints = false
        textFieldLeftCons = textField.leadingAnchor.constraint(equalTo: textFieldContainerView.leadingAnchor, constant: 0)
        textFieldLeftCons?.isActive = true
        textField.trailingAnchor.constraint(equalTo: textFieldContainerView.trailingAnchor).isActive = true
        textField.topAnchor.constraint(equalTo: textFieldContainerView.topAnchor).isActive = true
        textField.bottomAnchor.constraint(equalTo: textFieldContainerView.bottomAnchor).isActive = true
        
        rightIconContainerView.widthAnchor.constraint(equalToConstant: 36).isActive = true
        rightIconContainerView.addSubview(rightIconImageView)
        rightIconImageView.translatesAutoresizingMaskIntoConstraints = false
        rightIconImageView.leadingAnchor.constraint(equalTo: rightIconContainerView.leadingAnchor).isActive = true
        rightIconImageView.centerYAnchor.constraint(equalTo: rightIconContainerView.centerYAnchor).isActive = true
        rightIconImageView.trailingAnchor.constraint(equalTo: rightIconContainerView.trailingAnchor,
                                                     constant: -15).isActive = true
        
        addSubview(topLabel)
        topLabel.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        topLabel.bottomAnchor.constraint(equalTo: textField.topAnchor, constant: -10).isActive = true
        
        addSubview(bottomLabel)
        bottomLabel.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        bottomLabel.topAnchor.constraint(equalTo: textField.bottomAnchor, constant: 10).isActive = true
        
        addSubview(dividerView)
        dividerView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        dividerView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        dividerView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        dividerView.heightAnchor.constraint(equalToConstant: 1).isActive = true
        
        defaultConfig()
    }
    
    private func defaultConfig() {
        placeHolderFont = .systemFont(ofSize: 14)
        placeHolderColor = .darkGray
        textField.addTarget(self, action: #selector(textEditingChanged(sender:)),
                            for: .editingChanged)
        
        textField.addTarget(self, action: #selector(textEditingDidEnd(sender:)),
                            for: .editingDidEndOnExit)
    }
    
    @objc
    private func textEditingChanged(sender: UITextField) {
        textEditingChangedClosure?(sender.text)
    }
    
    @objc
    private func textEditingDidEnd(sender: UITextField) {
        textEditingEndedClosure?(sender.text)
    }
    
    @objc func clickRightButton(sender: UIButton) {
        sender.isSelected = !sender.isSelected
        rightIconImageView.image = sender.isSelected ? rightSelectedIconImage : rightIconImage
        clickRightButtonClosure?(sender.isSelected)
    }
}


extension AUITextField {
    var theme_placeHolderColor: ThemeColorPicker? {
        get { return aui_getThemePicker(self, "setPlaceHolderColor:") as? ThemeColorPicker }
        set { aui_setThemePicker(self, "setPlaceHolderColor:", newValue) }
    }
    
    var theme_placeHolderFont: ThemeFontPicker? {
        get { return aui_getThemePicker(self, "setPlaceHolderFont:") as? ThemeFontPicker }
        set { aui_setThemePicker(self, "setPlaceHolderFont:", newValue) }
    }
    
    var theme_textColor: ThemeColorPicker? {
        get { return aui_getThemePicker(self, "setTextColor:") as? ThemeColorPicker }
        set { aui_setThemePicker(self, "setTextColor:", newValue) }
    }
    
    var theme_textFont: ThemeFontPicker? {
        get { return aui_getThemePicker(self, "setTextFont:") as? ThemeFontPicker }
        set { aui_setThemePicker(self, "setTextFont:", newValue) }
    }
    
    var theme_leftIconImage: ThemeImagePicker? {
        get { return aui_getThemePicker(self, "setLeftIconImage:") as? ThemeImagePicker }
        set { aui_setThemePicker(self, "setLeftIconImage:", newValue) }
    }
    
    var theme_rightIconImage: ThemeImagePicker? {
        get { return aui_getThemePicker(self, "setRightIconImage:") as? ThemeImagePicker }
        set { aui_setThemePicker(self, "setRightIconImage:", newValue) }
    }
    
    var theme_rightSelectedIconImage: ThemeImagePicker? {
        get { return aui_getThemePicker(self, "setRightSelectedIconImage:") as? ThemeImagePicker }
        set { aui_setThemePicker(self, "setRightSelectedIconImage:", newValue) }
    }
}
