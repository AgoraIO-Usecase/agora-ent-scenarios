//
//  VoiceRealInEarView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/5/22.
//

import UIKit

class VoiceRealInEarView: UIView {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "实时耳返延时".show_localized
        label.textColor = UIColor(hex: "#3C4267", alpha: 1.0)
        label.font = UIFont.systemFont(ofSize: 13)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = "请对着耳麦说话，测试实时耳返延时".show_localized
        label.textColor = UIColor(hex: "#979CBB", alpha: 1.0)
        label.font = UIFont.systemFont(ofSize: 11)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var progressLabel: UILabel = {
        let label = UILabel()
        label.text = "0ms"
        label.textColor = UIColor(hex: "#6D7291", alpha: 1.0)
        label.font = UIFont.systemFont(ofSize: 14)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var progressView: UIProgressView = {
        let progress = UIProgressView()
        progress.progressTintColor = UIColor(hex: "#FAAD15", alpha: 1.0)
        progress.progress = 0.5
        progress.translatesAutoresizingMaskIntoConstraints = false
        return progress
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(titleLabel)
        addSubview(descLabel)
        addSubview(progressLabel)
        addSubview(progressView)
        
        titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 20).isActive = true
        titleLabel.topAnchor.constraint(equalTo: topAnchor, constant: 12).isActive = true
        
        descLabel.leadingAnchor.constraint(equalTo: titleLabel.leadingAnchor).isActive = true
        descLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 5).isActive = true
        
        progressLabel.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        progressLabel.topAnchor.constraint(equalTo: descLabel.bottomAnchor, constant: 20).isActive = true
        
        progressView.leadingAnchor.constraint(equalTo: titleLabel.leadingAnchor).isActive = true
        progressView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -20).isActive = true
        progressView.topAnchor.constraint(equalTo: progressLabel.bottomAnchor, constant: 8).isActive = true
        progressView.heightAnchor.constraint(equalToConstant: 4).isActive = true
    }
}
