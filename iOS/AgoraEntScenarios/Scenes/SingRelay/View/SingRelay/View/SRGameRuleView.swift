//
//  SRGameRuleView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/24.
//

import Foundation

import UIKit

class SRGameRuleView: UIView {

    private let purpleView: UIView = {
        let view = UIView()
        view.backgroundColor = .purple
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    private let ruleLabel: UILabel = {
        let label = UILabel()
        label.text = "游戏规则"
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 15)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    
    private let hideButton: UIButton = {
        let button = UIButton()
        button.setTitle("收起", for: .normal)
        button.setTitleColor(.blue, for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        button.layer.cornerRadius = 15
        button.layer.masksToBounds = true
        button.layer.borderColor = UIColor.white.cgColor
        button.layer.borderWidth = 1
        button.addTarget(self, action: #selector(hideButtonTapped), for: .touchUpInside)
        button.translatesAutoresizingMaskIntoConstraints = false
        return button
    }()
    
    private let contentLabel: UILabel = {
        let label = UILabel()
        label.text = "系统随机播放一首分好的歌曲，首段由房主主唱，抢到演唱权的玩家演唱下一段，游戏末显示评分排名。"
        label.textAlignment = .left
        label.numberOfLines = 0
        label.font = UIFont.systemFont(ofSize: 12)
        label.textColor = .white
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        addSubview(purpleView)
        purpleView.topAnchor.constraint(equalTo: topAnchor, constant: 10).isActive = true
        purpleView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 10).isActive = true
        purpleView.widthAnchor.constraint(equalToConstant: 100).isActive = true
        purpleView.heightAnchor.constraint(equalToConstant: 30).isActive = true
        purpleView.layer.cornerRadius = 5
        purpleView.layer.masksToBounds = true
        
        purpleView.addSubview(ruleLabel)
        ruleLabel.centerXAnchor.constraint(equalTo: purpleView.centerXAnchor).isActive = true
        ruleLabel.centerYAnchor.constraint(equalTo: purpleView.centerYAnchor).isActive = true
        
        addSubview(hideButton)
        hideButton.topAnchor.constraint(equalTo: topAnchor, constant: 10).isActive = true
        hideButton.heightAnchor.constraint(equalToConstant: 30).isActive = true
        hideButton.widthAnchor.constraint(equalToConstant: 80).isActive = true
        hideButton.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -10).isActive = true
        
        addSubview(contentLabel)
        contentLabel.topAnchor.constraint(equalTo: hideButton.bottomAnchor, constant: 10).isActive = true
        contentLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 10).isActive = true
        contentLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -10).isActive = true
        contentLabel.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -10).isActive = true
        
        let blurEffect = UIBlurEffect(style: .light)
        let visualEffectView = UIVisualEffectView(effect: blurEffect)
        visualEffectView.frame = bounds
        visualEffectView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        insertSubview(visualEffectView, at: 0)
        
        self.layer.cornerRadius = 10
        self.layer.masksToBounds = true
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc private func hideButtonTapped() {
        UIView.animate(withDuration: 0.25) {
            self.isHidden = true
        }
    }
}
