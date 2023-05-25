//
//  AUiAlertView.swift
//  AUiToast
//
//  Created by zhaoyongqiang on 2023/4/10.
//

import UIKit

class AUiAlertView: UIView {
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
        button.layer.cornerRadius = 25
        button.backgroundColor = UIColor(red: 120/255.0, green: 0/255.0, blue: 255/255.0, alpha: 1.0)
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
    public func background(color: UIColor?) -> AUiAlertView {
        backgroundColor = color
        return self
    }
    public func isShowCloseButton(isShow: Bool) -> AUiAlertView {
        closeButton.isHidden = !isShow
        return self
    }
    public func title(title: String?) -> AUiAlertView {
        titleLabelContaniner.isHidden = title == nil
        titleLabel.text = title
        return self
    }
    public func titleColor(color: UIColor?) -> AUiAlertView {
        titleLabel.textColor = color
        return self
    }
    public func titleFont(font: UIFont?) -> AUiAlertView {
        titleLabel.font = font
        return self
    }
    public func content(content: String?) -> AUiAlertView {
        contentLabel.isHidden = content == nil
        contentLabel.text = content
        return self
    }
    public func content(textAlignment: NSTextAlignment) -> AUiAlertView {
        contentLabel.textAlignment = textAlignment
        return self
    }
    public func contentAttrs(content: NSAttributedString?) -> AUiAlertView {
        contentLabel.isHidden = content == nil
        contentLabel.attributedText = content
        return self
    }
    public func contentColor(color: UIColor?) -> AUiAlertView {
        contentLabel.textColor = color
        return self
    }
    public func contentFont(font: UIFont?) -> AUiAlertView {
        contentLabel.font = font
        return self
    }
    public func textField(text: String?) -> AUiAlertView {
        textField.isHidden = text == nil
        textField.text = text
        return self
    }
    public func textField(color: UIColor?) -> AUiAlertView {
        textField.textColor = color
        return self
    }
    public func textField(font: UIFont?) -> AUiAlertView {
        textField.font = font
        return self
    }
    public func textField(leftView: UIView?) -> AUiAlertView {
        textField.leftView = leftView
        return self
    }
    public func textField(cornerRadius: CGFloat) -> AUiAlertView {
        textField.layer.cornerRadius = cornerRadius
        textField.layer.masksToBounds = true
        return self
    }
    public func textField(showBottomDivider: Bool) -> AUiAlertView {
        textFieldLineView.isHidden = !showBottomDivider
        return self
    }
    public func textField(bottmeDividerColor: UIColor?) -> AUiAlertView {
        textFieldLineView.backgroundColor = bottmeDividerColor
        return self
    }
    public func textFieldBackground(color: UIColor?) -> AUiAlertView {
        textField.backgroundColor = color
        return self
    }
    public func textFieldPlaceholder(placeholder: String?) -> AUiAlertView {
        textField.isHidden = placeholder == nil
        textField.placeholder = placeholder
        return self
    }
    public func textFieldPlaceholder(color: UIColor?) -> AUiAlertView {
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
    public func textFieldPlaceholder(font: UIFont?) -> AUiAlertView {
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
    public func leftButton(title: String?) -> AUiAlertView {
        leftButton.isHidden = title == nil
        leftButton.setTitle(title, for: .normal)
        return self
    }
    public func leftButton(color: UIColor?) -> AUiAlertView {
        leftButton.setTitleColor(color, for: .normal)
        return self
    }
    public func leftButton(font: UIFont?) -> AUiAlertView {
        leftButton.titleLabel?.font = font
        return self
    }
    public func leftButton(cornerRadius: CGFloat) -> AUiAlertView {
        leftButton.layer.cornerRadius = cornerRadius
        leftButton.layer.masksToBounds = true
        return self
    }
    public func leftButtonBackground(color: UIColor?) -> AUiAlertView {
        leftButton.backgroundColor = color
        return self
    }
    public func leftButtonBorder(color: UIColor?) -> AUiAlertView {
        leftButton.layer.borderColor = color?.cgColor
        return self
    }
    public func leftButtonBorder(width: CGFloat) -> AUiAlertView {
        leftButton.layer.borderWidth = width
        return self
    }
    @discardableResult
    public func leftButtonTapClosure(onTap: @escaping () -> Void) -> AUiAlertView {
        cancelClosure = onTap
        return self
    }
    public func rightButton(title: String?) -> AUiAlertView {
        rightButton.isHidden = title == nil
        rightButton.setTitle(title, for: .normal)
        return self
    }
    public func rightButton(color: UIColor?) -> AUiAlertView {
        rightButton.setTitleColor(color, for: .normal)
        return self
    }
    public func rightButton(font: UIFont?) -> AUiAlertView {
        rightButton.titleLabel?.font = font
        return self
    }
    public func rightButton(cornerRadius: CGFloat) -> AUiAlertView {
        rightButton.layer.cornerRadius = cornerRadius
        rightButton.layer.masksToBounds = true
        return self
    }
    public func rightButtonBackground(color: UIColor?) -> AUiAlertView {
        rightButton.backgroundColor = color
        return self
    }
    public func rightButtonBorder(color: UIColor?) -> AUiAlertView {
        rightButton.layer.borderColor = color?.cgColor
        return self
    }
    public func rightButtonBorder(width: CGFloat) -> AUiAlertView {
        rightButton.layer.borderWidth = width
        return self
    }
    @discardableResult
    public func rightButtonTapClosure(onTap: @escaping (String?) -> Void) -> AUiAlertView {
        sureClosure = onTap
        return self
    }
    @discardableResult
    public func rightButtonTapClosure(onTap: @escaping () -> Void) -> AUiAlertView {
        rightClosure = onTap
        return self
    }
    @discardableResult
    public func show() -> AUiAlertView {
        AlertManager.show(view: self, alertPostion: .center, didCoverDismiss: false)
        return self
    }
    @discardableResult
    public func hidden() -> AUiAlertView {
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
}
