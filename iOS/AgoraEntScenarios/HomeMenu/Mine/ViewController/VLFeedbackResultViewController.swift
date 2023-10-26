//
//  VLFeedbackResultViewController.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/19.
//

import UIKit

class VLFeedbackResultViewController: VLBaseViewController {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("feedback_result_tips", comment: "")
        label.textColor = UIColor(hex: "#000000", alpha: 1.0)
        label.font = .systemFont(ofSize: 17, weight: .medium)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("feedback_result_desc", comment: "")
        label.textColor = UIColor(hex: "#000000", alpha: 1.0)
        label.font = .systemFont(ofSize: 15)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var phoneButton: UIButton = {
        let button = UIButton()
        button.setTitle("400-632-6626", for: .normal)
        button.setTitleColor(UIColor(hex: "#2E6CF6", alpha: 1.0), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 15, weight: .medium)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(onClickPhoneButton(sender:)), for: .touchUpInside)
        return button
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        let vcs = navigationController?.viewControllers.filter({ !($0 is VLFeedbackViewController) })
        navigationController?.viewControllers = vcs ?? []
    }
    
    private func setupUI() {
        hiddenBackgroundImage()
        setNaviTitleName(NSLocalizedString("app_submit_feedback", comment: ""))
        setBackBtn()
        
        view.addSubview(titleLabel)
        titleLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        titleLabel.topAnchor.constraint(equalTo: view.topAnchor, constant: Screen.kNavHeight + 160).isActive = true
        
        view.addSubview(descLabel)
        descLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        descLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 16).isActive = true
        
        view.addSubview(phoneButton)
        phoneButton.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        phoneButton.topAnchor.constraint(equalTo: descLabel.bottomAnchor, constant: 16).isActive = true
    }
    
    @objc
    private func onClickPhoneButton(sender: UIButton) {
        if let url = URL(string: "tel://\(sender.titleLabel?.text ?? "")") {
            if UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url, options: [:], completionHandler: nil)
            }
        }
    }
}
