//
//  VLLoginViewController.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/12.
//

import UIKit

@objc
class VLLoginController: VLBaseViewController {
    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "login_icon"))
        imageView.translatesAutoresizingMaskIntoConstraints = false
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("personal_information", comment: "")
        label.textColor = UIColor(hex: "#000000", alpha: 1.0)
        label.font = UIFont.systemFont(ofSize: 20, weight: .medium)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var textView: AttributedTextView = {
        let userProtocol = "《\(NSLocalizedString("app_user_agreement", comment: ""))》"
        let privacyPolicy = "《\(NSLocalizedString("app_privacy_agreement", comment: ""))》"
        let tips = NSLocalizedString("app_user_info_tips", comment: "")
        let string = NSLocalizedString("app_user_info_desc", comment: "")
        
        let attrs = NSMutableAttributedString(string: tips, attributes: [.font: UIFont.systemFont(ofSize: 14, weight: .medium)])
        
        let userIndexs = string.indices(of: userProtocol)
        let range1 = NSRange(location: userIndexs.first ?? 0, length: userProtocol.count)
        let privacyIndexs = string.indices(of: privacyPolicy)
        let range2 = NSRange(location: privacyIndexs.first ?? 0, length: privacyPolicy.count)
        let range3 = NSRange(location: userIndexs[1], length: userProtocol.count)
        let range4 = NSRange(location: privacyIndexs[1], length: privacyPolicy.count)
        let textView = AttributedTextView(frame: .zero,
                                          text: string,
                                          font: .systemFont(ofSize: 14),
                                          attributedStringS: [userProtocol, privacyPolicy, userProtocol, privacyPolicy],
                                          ranges: [range1, range2, range3, range4],
                                          textColor: UIColor(hex: "#000000", alpha: 1.0)!,
                                          attributeTextColor: UIColor(hex: "#2E6CF6", alpha: 1.0)!,
                                          attrs: attrs)

        textView.delegate = self
        textView.translatesAutoresizingMaskIntoConstraints = false
        textView.setContentHuggingPriority(.defaultLow, for: .vertical)
        textView.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
        return textView
    }()
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("app_user_info_choice_agree_tips", comment: "")
        label.textColor = UIColor(hex: "#979CBB", alpha: 1.0)
        label.font = .systemFont(ofSize: 12)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var notAgreeButton: UIButton = {
        let button = UIButton()
        button.setTitle(NSLocalizedString("disagree", comment: ""), for: .normal)
        button.setTitleColor(UIColor(hex: "#303553", alpha: 1.0), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 15)
        button.backgroundColor = UIColor(hex: "#08062F", alpha: 0.05)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.cornerRadius(12)
        button.addTarget(self, action: #selector(onClickNotAgressButton), for: .touchUpInside)
        return button
    }()
    private lazy var agreeButton: UIButton = {
        let button = UIButton()
        button.setTitle(NSLocalizedString("agree", comment: ""), for: .normal)
        button.setTitleColor(UIColor(hex: "#FFFFFF", alpha: 1.0), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 15)
        button.backgroundColor = UIColor(hex: "#2E6CF6", alpha: 1.0)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.cornerRadius(12)
        button.addTarget(self, action: #selector(onClickAgressButton), for: .touchUpInside)
        return button
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        setupUI()
    }
    
    private func setupUI() {
        view.addSubview(iconImageView)
        view.addSubview(titleLabel)
        view.addSubview(textView)
        view.addSubview(descLabel)
        view.addSubview(notAgreeButton)
        view.addSubview(agreeButton)
        
        iconImageView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        iconImageView.topAnchor.constraint(equalTo: view.topAnchor, constant: 40.fit).isActive = true
        
        titleLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        titleLabel.topAnchor.constraint(equalTo: iconImageView.bottomAnchor, constant: 8.fit).isActive = true
        
        textView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20.fit).isActive = true
        textView.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 32.fit).isActive = true
        textView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20.fit).isActive = true
        
        descLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        descLabel.topAnchor.constraint(equalTo: textView.bottomAnchor, constant: 61.fit).isActive = true
        
        notAgreeButton.leadingAnchor.constraint(equalTo: textView.leadingAnchor).isActive = true
        notAgreeButton.topAnchor.constraint(equalTo: descLabel.bottomAnchor, constant: 12.fit).isActive = true
        notAgreeButton.widthAnchor.constraint(equalToConstant: (Screen.width - 40.fit - 16.fit) * 0.5).isActive = true
        notAgreeButton.heightAnchor.constraint(equalToConstant: 48.fit).isActive = true
        notAgreeButton.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -12.fit).isActive = true
        
        agreeButton.trailingAnchor.constraint(equalTo: textView.trailingAnchor).isActive = true
        agreeButton.topAnchor.constraint(equalTo: notAgreeButton.topAnchor).isActive = true
        agreeButton.widthAnchor.constraint(equalTo: notAgreeButton.widthAnchor).isActive = true
        agreeButton.heightAnchor.constraint(equalTo: notAgreeButton.heightAnchor).isActive = true
    }
    
    @objc
    private func onClickNotAgressButton() {
        let textString = NSLocalizedString("privacy_protection_tip2", comment: "")
        let range1 = NSRange(location: 3, length: 4)
        let range2 = NSRange(location: 10, length: 4)
        let arrayTitles = [NSLocalizedString("app_user_agreement", comment: ""), NSLocalizedString("app_privacy_agreement", comment: "")]
        VLAlert.shared().showAttributeAlert(withFrame: CGRect(origin: .zero, size: CGSize(width: Screen.width, height: 120)),
                                            title: NSLocalizedString("personal_information", comment: ""),
                                            text: textString,
                                            attributedStringS: arrayTitles,
                                            ranges: [range1, range2],
                                            textColor: UIColor(hex: "#6C7192")!,
                                            attributeTextColor: UIColor(hex: "#009FFF")!,
                                            buttonTitles: [NSLocalizedString("disagree_and_exit", comment: ""),
                                                           NSLocalizedString("agree_and_goon", comment: "")], completion: { flag, text in
            VLAlert.shared().dismiss()
            if flag == false {
                exit(0)
            } else {
                self.onClickAgressButton()
            }
        }, linkCompletion: { tag in
            VLAlert.shared().dismiss()
            self.toWebVC(url: tag == "0" ? VLURLConfig.kURLPathH5UserAgreement : VLURLConfig.kURLPathH5Privacy)
        })
    }
    @objc
    private func onClickAgressButton() {
        let guideVC = VLLoginGuideViewController()
        navigationController?.pushViewController(guideVC, animated: true)
    }
    
    private func toWebVC(url: String) {
        let webVC = VLCommonWebViewController();
        webVC.urlString = url
        navigationController?.pushViewController(webVC, animated: true)
    }
}

extension VLLoginController: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange) -> Bool {
        if URL.absoluteString == "0" || URL.absoluteString == "2" {
            toWebVC(url: VLURLConfig.kURLPathH5UserAgreement)
        } else {
            toWebVC(url: VLURLConfig.kURLPathH5Privacy)
        }
        return true
    }
}

extension String {
    func indices(of string: String) -> [Int] {
        var indices: [Int] = []
        var searchStartIndex = self.startIndex
        while searchStartIndex < self.endIndex,
              let range = self.range(of: string, range: searchStartIndex..<self.endIndex),
              !range.isEmpty {
            let index = distance(from: self.startIndex, to: range.lowerBound)
            indices.append(index)
            searchStartIndex = range.upperBound
        }
        return indices
    }
}
