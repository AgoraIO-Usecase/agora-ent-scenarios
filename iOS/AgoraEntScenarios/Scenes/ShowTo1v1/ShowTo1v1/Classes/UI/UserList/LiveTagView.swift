//
//  LiveTagView.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/8/9.
//

import UIKit
import FLAnimatedImage

class LiveTagView: UIView {
    
    private lazy var liveGradientLayer: CAGradientLayer = {
        let layer = CAGradientLayer()
        layer.cornerRadius = 10
        layer.colors = [
        UIColor(red: 1, green: 0.429, blue: 0.719, alpha: 1).cgColor,
        UIColor(red: 0.921, green: 0.658, blue: 0.265, alpha: 1).cgColor
        ]
        layer.locations = [0, 1]
        layer.startPoint = CGPoint(x: 0.25, y: 0.5)
        layer.endPoint = CGPoint(x: 0.75, y: 0.5)

        return layer
    }()
    
    private lazy var liveAnimationView: FLAnimatedImageView = {
        let view = FLAnimatedImageView(frame: CGRect(x: 0, y: 0, width: 20, height: 20))
        let path = UIImage.sceneImagePath(name: "Image/user_list_cell_live.gif", bundleName: "Pure1v1")!
        view.sd_setImage(with: URL.init(fileURLWithPath: path), placeholderImage: nil)
        
        return view
    }()
    
    private lazy var liveTagNameLabel: UILabel = {
        let label = UILabel()
        label.text = "user_list_live_tag".showTo1v1Localization()
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 14)
        label.sizeToFit()
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubView()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubView() {
        layer.addSublayer(liveGradientLayer)
        addSubview(liveAnimationView)
        addSubview(liveTagNameLabel)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        liveGradientLayer.frame = bounds
        liveAnimationView.aui_left = 8
        liveAnimationView.aui_centerY = aui_height / 2
        liveTagNameLabel.aui_left = liveAnimationView.aui_right + 8
        liveTagNameLabel.aui_centerY = liveAnimationView.aui_centerY
    }
    
    override func sizeThatFits(_ size: CGSize) -> CGSize {
        return CGSize(width: liveTagNameLabel.aui_width + liveAnimationView.aui_width + 8 * 3, height: 30)
    }
}
