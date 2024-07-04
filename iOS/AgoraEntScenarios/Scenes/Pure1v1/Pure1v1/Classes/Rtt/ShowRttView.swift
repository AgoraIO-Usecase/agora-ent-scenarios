//
//  ShowRttView.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/24.
//

import UIKit

protocol ShowRttViewDelegate: NSObjectProtocol {
    func onClickRtt(start: Bool)
    func onClickSourceLanguage()
    func onClickTargetLanguage()
}

class ShowRttView: UIView {
    
    weak var delegate: ShowRttViewDelegate?
    
    private var isRttOpen = false
    
    // 标题
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.text = "rtt_title".pure1v1Localization()
        label.textAlignment = .center
        label.font = UIFont.boldSystemFont(ofSize: 20)
        label.textColor = .black // 标题颜色
        return label
    }()
    
    // 描述性文字
    private let descriptionLabel: UILabel = {
        let label = UILabel()
        label.text = "rtt_note".pure1v1Localization()
        label.textAlignment = .left
        label.font = UIFont.systemFont(ofSize: 15)
        label.numberOfLines = 0 // 允许多行文本
        label.textColor = .darkGray
        return label
    }()
    
    // 源语言标签
    private let sourceLanguageLabel: PureRttLabelView = {
        let label = PureRttLabelView()
        return label
    }()
    
    // 翻译字幕标签
    private let translationSubtitleLabel: PureRttLabelView = {
        let label = PureRttLabelView()
        return label
    }()
    
    // 开启按钮
    private let startButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("rtt_enable".pure1v1Localization(), for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.backgroundColor = .blue
        button.layer.cornerRadius = 12
        button.addTarget(self, action: #selector(didClickRttButton), for: .touchUpInside)
        return button
    }()
    
    // 初始化方法
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupView()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupView()
    }
    
    // 设置视图
    private func setupView() {
        backgroundColor = .white
        layer.cornerRadius = 12
        layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        layer.masksToBounds = true
        
        sourceLanguageLabel.setTitle("rtt_source_language".pure1v1Localization(), value: RttManager.shared.currentSourceLanguageDisplayName()) {
            self.delegate?.onClickSourceLanguage()
        }
        
        translationSubtitleLabel.setTitle("rtt_target_language".pure1v1Localization(), value: RttManager.shared.currentTargetLanguageDisplayName()) {
            self.delegate?.onClickTargetLanguage()
        }
        
        addSubview(titleLabel)
        addSubview(descriptionLabel)
        addSubview(sourceLanguageLabel)
        addSubview(translationSubtitleLabel)
        addSubview(startButton)
        
        setupConstraints()
    }
    
    // 布局约束
    private func setupConstraints() {
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        descriptionLabel.translatesAutoresizingMaskIntoConstraints = false
        sourceLanguageLabel.translatesAutoresizingMaskIntoConstraints = false
        translationSubtitleLabel.translatesAutoresizingMaskIntoConstraints = false
        startButton.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            // 标题约束
            titleLabel.topAnchor.constraint(equalTo: topAnchor, constant: 30),
            titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 20),
            titleLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -20),
            
            // 描述性文字约束
            descriptionLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 20),
            descriptionLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 20),
            descriptionLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -20),
            
            // 源语言标签约束
            sourceLanguageLabel.topAnchor.constraint(equalTo: descriptionLabel.bottomAnchor, constant: 30),
            sourceLanguageLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 0),
            sourceLanguageLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: 0), // 这里可能需要调整，具体取决于您想要的布局
            sourceLanguageLabel.heightAnchor.constraint(equalToConstant: 40), // 假设高度为40点

            // 翻译字幕标签约束
            translationSubtitleLabel.topAnchor.constraint(equalTo: sourceLanguageLabel.bottomAnchor, constant: 30),
            translationSubtitleLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 0),
            translationSubtitleLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: 0), // 这里可能需要调整
            translationSubtitleLabel.heightAnchor.constraint(equalToConstant: 40), // 假设高度为40点
            
            // 开启按钮约束
            startButton.topAnchor.constraint(equalTo: translationSubtitleLabel.bottomAnchor, constant: 30),
            startButton.centerXAnchor.constraint(equalTo: centerXAnchor),
            startButton.widthAnchor.constraint(equalToConstant: 335),
            startButton.heightAnchor.constraint(equalToConstant: 48)
        ])
    }
    
    func setStartRttStatus(open: Bool) {
        if (open) {
            isRttOpen = true
            startButton.backgroundColor = .gray
            startButton.setTitleColor(.black, for: .normal)
            startButton.setTitle("rtt_disable".pure1v1Localization(), for: .normal)
        } else {
            isRttOpen = false
            startButton.backgroundColor = .blue
            startButton.setTitleColor(.white, for: .normal)
            startButton.setTitle("rtt_enable".pure1v1Localization(), for: .normal)
        }
        startButton.isEnabled = true
    }
    
    func reloadChosenLanguage() {
        sourceLanguageLabel.updataValue(value: RttManager.shared.currentSourceLanguageDisplayName())
        translationSubtitleLabel.updataValue(value: RttManager.shared.currentTargetLanguageDisplayName())
    }
    
    @objc private func didClickRttButton() {
        startButton.backgroundColor = .gray
        startButton.setTitleColor(.black, for: .normal)
        startButton.isEnabled = false
        delegate?.onClickRtt(start: !self.isRttOpen)
    }
    
    @objc private func didClickSourceLanguage() {
        delegate?.onClickSourceLanguage()
    }
}
