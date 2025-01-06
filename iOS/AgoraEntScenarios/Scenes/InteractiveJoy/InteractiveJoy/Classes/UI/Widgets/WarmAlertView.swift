import UIKit
import SnapKit

class WarmAlertView: AgoraAlertView {
    
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
        gradientLayer.frame = CGRectMake(0, 0, UIScreen.main.bounds.width, 59)
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
        label.text = NSLocalizedString("warm_alert_title", comment: "")
        label.font = .systemFont(ofSize: 18, weight: .semibold)
        label.textColor = UIColor(red: 0.02, green: 0.04, blue: 0.15, alpha: 1)
        label.textAlignment = .center
        return label
    }()
    
    private lazy var warningStackView: UIStackView = {
        let stack = UIStackView()
        stack.axis = .vertical
        stack.spacing = 4
        stack.alignment = .leading
        return stack
    }()
    
    private lazy var confirmButton: UIButton = {
        let button = UIButton()
        button.setTitle(NSLocalizedString("warm_alert_ensure_button", comment: ""), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
        button.backgroundColor = UIColor(red: 0.18, green: 0.42, blue: 0.96, alpha: 1)
        button.layer.cornerRadius = 12
        button.addTarget(self, action: #selector(confirmButtonTapped), for: .touchUpInside)
        return button
    }()
    
    required init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupConstraints()
        setupWarningTexts()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(containerView)
        containerView.addSubview(headerView)
        headerView.addSubview(handleView)
        containerView.addSubview(titleLabel)
        containerView.addSubview(warningStackView)
        containerView.addSubview(confirmButton)
    }
    
    private func setupConstraints() {
        containerView.snp.makeConstraints { make in
            make.left.right.bottom.equalToSuperview()
            make.height.equalTo(277)
        }
        
        headerView.snp.makeConstraints { make in
            make.top.left.right.equalToSuperview()
            make.height.equalTo(59)
        }
        
        handleView.snp.makeConstraints { make in
            make.top.equalTo(8)
            make.centerX.equalToSuperview()
            make.width.equalTo(37)
            make.height.equalTo(3)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.top.equalTo(handleView.snp.bottom).offset(8)
            make.left.right.equalToSuperview().inset(20)
        }
        
        warningStackView.snp.makeConstraints { make in
            make.top.equalTo(headerView.snp.bottom).offset(16)
            make.left.right.equalToSuperview().inset(20)
        }
        
        confirmButton.snp.makeConstraints { make in
            make.left.right.equalToSuperview().inset(20)
            make.bottom.equalTo(-54)
            make.height.equalTo(48)
        }
    }
    
    private func setupWarningTexts() {
        let warnings = [
            NSLocalizedString("warm_alert_content1", comment: ""),
            NSLocalizedString("warm_alert_content2", comment: ""),
            NSLocalizedString("warm_alert_content3", comment: "")
        ]
        
        warnings.forEach { text in
            let label = UILabel()
            label.text = text
            label.font = .systemFont(ofSize: 14)
            label.textColor = UIColor(red: 0.19, green: 0.21, blue: 0.33, alpha: 1)
            label.numberOfLines = 0
            warningStackView.addArrangedSubview(label)
        }
    }
    
    @objc private func confirmButtonTapped() {
        hide()
    }
}
