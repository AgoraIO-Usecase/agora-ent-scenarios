//
//  Pure1v1NaviBar.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/20.
//

import Foundation

class Pure1v1NaviBar: UIView {
    public lazy var backButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "navi_back"), for: .normal)
        return button
    }()
    
    public lazy var refreshButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "refresh_black"), for: .normal)
        return button
    }()
    
    public lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 16)
        label.textColor = .black
        label.text = "user_list_title".pure1v1Localization()
        label.textAlignment = .center
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubview()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubview() {
//        layer.addSublayer(gradientLayer)
        addSubview(titleLabel)
        addSubview(backButton)
        addSubview(refreshButton)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
//        gradientLayer.frame = bounds
        backButton.frame = CGRect(x: 6, y: 0, width: self.aui_height, height: self.aui_height)
        refreshButton.frame = CGRect(x: self.aui_width - 6 - self.aui_height, y: 0, width: self.aui_height, height: self.aui_height)
        titleLabel.frame = bounds
    }
}
