//
//  ShowTo1v1NaviBar.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/28.
//

import Foundation

enum NaviBarStyle: Int {
    case dark = 0
    case light
    
    func backImage() -> UIImage? {
        switch self {
        case .dark:
            return UIImage.sceneImage(name: "navi_back_black")
        default:
            return UIImage.sceneImage(name: "navi_back_white")
        }
    }
    
    func refreshImage() -> UIImage? {
        switch self {
        case .dark:
            return UIImage.sceneImage(name: "navi_refresh_black")
        default:
            return UIImage.sceneImage(name: "navi_refresh_white")
        }
    }
    
    func titleColor() -> UIColor {
        switch self {
        case .dark:
            return .black
        default:
            return .white
        }
    }
}

class ShowTo1v1NaviBar: UIView {
    public var style: NaviBarStyle = .dark {
        didSet {
            backButton.setImage(style.backImage(), for: .normal)
            refreshButton.setImage(style.refreshImage(), for: .normal)
            titleLabel.textColor = style.titleColor()
        }
    }
    public lazy var backButton: UIButton = {
        let button = UIButton()
        return button
    }()
    
    public lazy var refreshButton: UIButton = {
        let button = UIButton()
        return button
    }()
    
    public lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.boldSystemFont(ofSize: 16)
        label.text = "user_list_title".showTo1v1Localization()
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
        style = .dark
        addSubview(titleLabel)
        addSubview(backButton)
        addSubview(refreshButton)
    }
    
    func startRefreshAnimation() {
        refreshButton.isEnabled = false
        refreshButton.layer.startRotationAnimation(from: 0, to: Float.pi * 2, duration: 0.8, loop: true)
    }
    
    func stopRefreshAnimation() {
        refreshButton.isEnabled = true
        refreshButton.layer.removeAllAnimations()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
//        gradientLayer.frame = bounds
        backButton.frame = CGRect(x: 6, y: 0, width: self.aui_height, height: self.aui_height)
        refreshButton.frame = CGRect(x: self.aui_width - 6 - self.aui_height, y: 0, width: self.aui_height, height: self.aui_height)
        titleLabel.frame = bounds
    }
}
