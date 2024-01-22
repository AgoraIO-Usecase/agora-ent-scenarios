//
//  VLVerifyCodeViewController.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/13.
//

import UIKit
import AgoraCommon
class VLVerifyCodeViewController: VLBaseViewController {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("app_please_input_v_code", comment: "")
        label.textColor = UIColor(hex: "#1D2129", alpha: 1.0)
        label.font = UIFont.boldSystemFont(ofSize: 30)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#86909C", alpha: 1.0)
        label.font = .systemFont(ofSize: 14)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    
    private lazy var codeView: VerifyCodeView = {
        view.layoutIfNeeded()
        let maxY = descLabel.frame.maxY
        let codeView = VerifyCodeView(frame: CGRect(x: 25.fit, y: maxY + 40.fit, width: Screen.width - 50.fit, height: 60.fit), codeNumbers: 4, space: 20, padding: 0)
        codeView.textFiled.becomeFirstResponder()
        codeView.codeViews.forEach({ $0.layer.borderWidth = 0; $0.layer.borderColor = UIColor(hex: "#2E6CF6", alpha: 1.0).cgColor; $0.backgroundColor = .white })
        codeView.inputFinish = { [weak self] code in
            self?.verifyCodeHandler(code: code)
        }
        return codeView
    }()
    private lazy var sendCodeButton: UIButton = {
        let button = UIButton()
        button.setTitle("\(NSLocalizedString("app_login_verify_code_retry", comment: "")) 60s", for: .normal)
        button.setTitle(NSLocalizedString("app_login_verify_code_retry", comment: ""), for: .selected)
        button.setTitleColor(UIColor(hex: "#86909C", alpha: 1.0), for: .normal)
        button.setTitleColor(UIColor(hex: "#2E6CF6", alpha: 1.0), for: .selected)
        button.titleLabel?.font = .systemFont(ofSize: 16)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(onClickSendVerifyCode), for: .touchUpInside)
        return button
    }()
    private lazy var tipsLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("app_login_verify_code_error", comment: "")
        label.textColor = UIColor(hex: "#FA396A", alpha: 1.0)
        label.font = .systemFont(ofSize: 16)
        label.translatesAutoresizingMaskIntoConstraints = false
        label.isHidden = true
        return label
    }()
    
    private var phoneNumber: String?
    private var timer: Timer?
    private var count: Int = 60
    
    init(phoneNumber: String?) {
        super.init(nibName: nil, bundle: nil)
        descLabel.text = "\(NSLocalizedString("app_login_verify_code_send", comment: "")) +86 \(phoneNumber ?? "")"
        self.phoneNumber = phoneNumber
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setBackBtn()
        setupUI()
        sendVerifyCodeHandler()
        setupTimer()
    }
    
    deinit {
        timer?.invalidate()
        timer = nil
    }
    
    private func setupTimer() {
        timer = Timer(timeInterval: 1, repeats: true, block: { [weak self] t in
            guard let self = self else { return }
            self.count -= 1
            self.sendCodeButton.setTitle("\(NSLocalizedString("app_login_verify_code_retry", comment: "")) \(self.count)s", for: .normal)
            self.sendCodeButton.isSelected = self.count <= 0
            if self.count <= 0 {
                t.invalidate()
                self.count = 60
            }
        })
        RunLoop.main.add(timer!, forMode: .common)
    }
    
    private func sendVerifyCodeHandler() {
        let model = VLVerifyCodeNetworkModel()
        model.phone = phoneNumber
        model.request { [weak self] err, data in
            if let response: VLResponseData = data as? VLResponseData {
                if  response.code != 0 {
                    ToastView.show(text: response.message ?? "", postion: .center)
                    self?.count = 1
                }
            }else{
                ToastView.show(text: err?.localizedDescription ?? "", postion: .center)
                self?.count = 1
            }
        }
    }
    
    private func verifyCodeHandler(code: String) {
        let model = VLLoginNetworkModel()
        model.phone = phoneNumber
        model.code = code
        model.request { err, data in
            if let response: VLResponseData = data as? VLResponseData {
                if response.code == 0, let responseData = response.data {
                    guard let loginModel = VLLoginModel.yy_model(withJSON: responseData) else { return }
                    VLUserCenter.shared().storeUserInfo(loginModel)
                    UIApplication.shared.delegate?.window??.configRootViewController()
                } else {
                    DispatchQueue.main.async {
                        self.tipsLabel.isHidden = false
                    }
                }
            } else {
                DispatchQueue.main.async {
                    self.tipsLabel.isHidden = false
                }
            }
        }
    }
    
    private func setupUI() {
        view.addSubview(titleLabel)
        view.addSubview(descLabel)
        view.addSubview(sendCodeButton)
        view.addSubview(tipsLabel)
        
        titleLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 25.fit).isActive = true
        titleLabel.topAnchor.constraint(equalTo: view.topAnchor, constant: Screen.kNavHeight + 48.fit).isActive = true
        
        descLabel.leadingAnchor.constraint(equalTo: titleLabel.leadingAnchor).isActive = true
        descLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 8.fit).isActive = true
        
        view.addSubview(codeView)
        
        sendCodeButton.leadingAnchor.constraint(equalTo: descLabel.leadingAnchor).isActive = true
        sendCodeButton.topAnchor.constraint(equalTo: codeView.bottomAnchor, constant: 20.fit).isActive = true
        
        tipsLabel.centerYAnchor.constraint(equalTo: sendCodeButton.centerYAnchor).isActive = true
        tipsLabel.trailingAnchor.constraint(equalTo: codeView.trailingAnchor).isActive = true
    }
    
    @objc
    private func onClickSendVerifyCode() {
        guard sendCodeButton.isSelected else { return }
        tipsLabel.isHidden = true
        sendVerifyCodeHandler()
        setupTimer()
    }
}
