//
//  ShowRttView.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/24.
//

import UIKit

protocol ShowRttViewDelegate: NSObjectProtocol {
    func onClickRtt(start: Bool)
}

class ShowRttView: UIView {
    
    weak var delegate: ShowRttViewDelegate?
    
    private var isRttOpen = false
    
    // 标题
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.text = "RTT实时语音翻译"
        label.textAlignment = .center
        label.font = UIFont.boldSystemFont(ofSize: 20)
        label.textColor = .black // 标题颜色
        return label
    }()
    
    // 描述性文字
    private let descriptionLabel: UILabel = {
        let label = UILabel()
        label.text = "开启后，将自动为您实时翻译，将对方口语语言实时翻译为你所需语言的相应字幕"
        label.textAlignment = .left
        label.font = UIFont.systemFont(ofSize: 15)
        label.numberOfLines = 0 // 允许多行文本
        label.textColor = .darkGray
        return label
    }()
    
    // 源语言标签
    private let sourceLanguageLabel: PureRttLabelView = {
        let label = PureRttLabelView()
        label.setTitle("对方语言", value: "中文") {
        
        }
        return label
    }()
    
    // 翻译字幕标签
    private let translationSubtitleLabel: PureRttLabelView = {
        let label = PureRttLabelView()
        label.setTitle("翻译字幕", value: "英文") {
        
        }
        return label
    }()
    
    // 开启按钮
    private let startButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("开启该功能", for: .normal)
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
        //heightAnchor.constraint(equalToConstant: 360).isActive = true
        
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
        
        // 根据内容动态计算视图的高度
        self.heightAnchor.constraint(equalToConstant: titleLabel.frame.height + descriptionLabel.frame.height + sourceLanguageLabel.frame.height + translationSubtitleLabel.frame.height + startButton.frame.height).isActive = true
    }
    
    func setStartRttStatus(open: Bool) {
        if (open) {
            isRttOpen = true
            startButton.backgroundColor = .gray
            startButton.setTitleColor(.black, for: .normal)
            startButton.setTitle("关闭该功能", for: .normal)
        } else {
            isRttOpen = false
            startButton.backgroundColor = .blue
            startButton.setTitleColor(.white, for: .normal)
            startButton.setTitle("开启该功能", for: .normal)
        }
        startButton.isEnabled = true
    }
    
    @objc private func didClickRttButton() {
        startButton.isEnabled = false
        startButton.backgroundColor = .gray
        startButton.setTitleColor(.black, for: .normal)
        delegate?.onClickRtt(start: !self.isRttOpen)
    }
}
