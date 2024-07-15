//
//  VLRegisterViewController.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/13.
//

import UIKit
import LSTPopView

class VLRegisterViewController: VLBaseViewController {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("app_register_account", comment: "")
        label.textColor = UIColor(hex: "#1D2129", alpha: 1.0)
        label.font = UIFont.boldSystemFont(ofSize: 30)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("app_register_account_tips", comment: "")
        label.textColor = UIColor(hex: "#86909C", alpha: 1.0)
        label.font = .systemFont(ofSize: 14)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var inputContainerView: UIView = {
        let view = UIView()
        view.translatesAutoresizingMaskIntoConstraints = false
        view.cornerRadius(12.fit)
        view.layer.borderColor = UIColor(hex: "#2E6CF6", alpha: 1.0)?.cgColor
        view.layer.borderWidth = 2.0
        return view
    }()
    private lazy var areaButton: UIButton = {
        let button = UIButton()
        button.setTitle("+86", for: .normal)
        button.setTitleColor(UIColor(hex: "#1D2129", alpha: 1.0), for: .normal)
        button.backgroundColor = UIColor(hex: "#EDF2F9", alpha: 1.0)
        button.titleLabel?.font = .systemFont(ofSize: 14)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.titleLabel?.textAlignment = .center
        return button
    }()
    private lazy var textField: UITextField = {
        let textField = UITextField()
        textField.translatesAutoresizingMaskIntoConstraints = false
        textField.placeholder = NSLocalizedString("app_please_input_account", comment: "")
        textField.textColor = .black
        textField.backgroundColor = UIColor(hex: "#FFFFFF", alpha: 1.0)
        textField.leftViewMode = .always
        textField.font = .systemFont(ofSize: 14)
        textField.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 10, height: 10))
        textField.delegate = self
        textField.keyboardType = .numberPad
        textField.becomeFirstResponder()
        textField.addTarget(self, action: #selector(onTextFieldValueChange(sender:)), for: .editingChanged)
        return textField
    }()
    private lazy var verifyCodeButton: UIButton = {
        let button = UIButton()
        button.setTitle(NSLocalizedString("app_login_get_verify_code", comment: ""), for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 16)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.backgroundColor = UIColor(hex: "#2E6CF6", alpha: 1.0)
        button.setContentHuggingPriority(.defaultHigh, for: .vertical)
        button.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        button.addTarget(self, action: #selector(onClickVerifyCodeButton), for: .touchUpInside)
        button.cornerRadius(12.fit)
        return button
    }()
    private var popView: LSTPopView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setBackBtn()
        setupUI()
    }
    
    private func setupUI() {
        view.addSubview(titleLabel)
        view.addSubview(descLabel)
        view.addSubview(inputContainerView)
        inputContainerView.addSubview(areaButton)
        inputContainerView.addSubview(textField)
        view.addSubview(verifyCodeButton)
        
        titleLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 25.fit).isActive = true
        titleLabel.topAnchor.constraint(equalTo: view.topAnchor, constant: Screen.kNavHeight + 48.fit).isActive = true
        
        descLabel.leadingAnchor.constraint(equalTo: titleLabel.leadingAnchor).isActive = true
        descLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 8.fit).isActive = true
        
        inputContainerView.leadingAnchor.constraint(equalTo: descLabel.leadingAnchor).isActive = true
        inputContainerView.topAnchor.constraint(equalTo: descLabel.bottomAnchor, constant: 40.fit).isActive = true
        inputContainerView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -25.fit).isActive = true
        inputContainerView.heightAnchor.constraint(equalToConstant: 48.fit).isActive = true
        
        areaButton.leadingAnchor.constraint(equalTo: inputContainerView.leadingAnchor).isActive = true
        areaButton.topAnchor.constraint(equalTo: inputContainerView.topAnchor).isActive = true
        areaButton.bottomAnchor.constraint(equalTo: inputContainerView.bottomAnchor).isActive = true
        areaButton.widthAnchor.constraint(equalToConstant: 48.fit).isActive = true
        textField.leadingAnchor.constraint(equalTo: areaButton.trailingAnchor).isActive = true
        textField.topAnchor.constraint(equalTo: inputContainerView.topAnchor).isActive = true
        textField.bottomAnchor.constraint(equalTo: inputContainerView.bottomAnchor).isActive = true
        textField.trailingAnchor.constraint(equalTo: inputContainerView.trailingAnchor).isActive = true
        
        verifyCodeButton.leadingAnchor.constraint(equalTo: inputContainerView.leadingAnchor).isActive = true
        verifyCodeButton.trailingAnchor.constraint(equalTo: inputContainerView.trailingAnchor).isActive = true
        verifyCodeButton.topAnchor.constraint(equalTo: inputContainerView.bottomAnchor, constant: 28.fit).isActive = true
        verifyCodeButton.heightAnchor.constraint(equalToConstant: 48.fit).isActive = true
    }

    private func setupCodeView() {
        let imageVerifyView = VLPopImageVerifyView(frame: CGRect(x: 40, y: 0, width: Screen.width - 80, height: 198 + (Screen.width - 120) * 0.65), with: self)
        let popView = LSTPopView.initWithCustomView(imageVerifyView, parentView: view, popStyle: .fade, dismissStyle: .fade)
        popView?.hemStyle = .center
        popView?.popDuration = 0.5
        popView?.dismissDuration = 0.5
        popView?.cornerRadius = 16
        popView?.isClickFeedback = false
        popView?.pop()
        self.popView = popView
    }
    
    @objc
    private func onClickVerifyCodeButton() {
        textField.endEditing(true)
    }
    @objc
    private func onTextFieldValueChange(sender: UITextField) {
        if verifyCodeButton.alpha != 1.0 {
            verifyCodeButton.alpha = sender.text?.count == 11 ? 1.0 : 0.5
        }
    }
}

extension VLRegisterViewController: UITextFieldDelegate {
    func textFieldDidBeginEditing(_ textField: UITextField) {
        inputContainerView.layer.borderWidth = 2.0
    }
    func textFieldDidEndEditing(_ textField: UITextField) {
        if textField.text?.isEmpty ?? false {
            ToastView.show(text: NSLocalizedString("app_please_input_account", comment: ""), postion: .bottom, duration: 2.5, view: view)
            inputContainerView.layer.borderWidth = 0.0
            verifyCodeButton.alpha = 0.5
            return
        }
        if textField.text?.count != 11 {
            ToastView.show(text: NSLocalizedString("invalid_phone_number", comment: ""), postion: .bottom, duration: 2.5, view: view)
            inputContainerView.layer.borderWidth = 0.0
            verifyCodeButton.alpha = 0.5
            return
        }
        verifyCodeButton.alpha = 1.0
        setupCodeView()
    }
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let currentText = textField.text ?? ""
        let updatedText = currentText.replacingCharacters(in: Range(range, in: currentText)!, with: string)
        return updatedText.count <= 11
    }
}
extension VLRegisterViewController: VLPopImageVerifyViewDelegate {
    func closeBtnAction() {
        popView?.dismiss()
    }
    func slideSuccessAction() {
        popView?.dismiss()
        let verifyCodeVC = VLVerifyCodeViewController(phoneNumber: textField.text)
        self.navigationController?.pushViewController(verifyCodeVC, animated: true)
    }
}
