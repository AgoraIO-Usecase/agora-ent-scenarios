//
//  TouchWaveView.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/21.
//

import Foundation

class TouchWaveView: UIControl {
    var minAnimationScaleValue: CGFloat = 1
    var maxAnimationScaleValue: CGFloat = 1.26
    private lazy var bgGradientLayer: CAGradientLayer = {
        let layer = CAGradientLayer()
        layer.type = .radial
        layer.colors = [
            UIColor(hexString: "#FFCF85")!.withAlphaComponent(0).cgColor,
            UIColor(hexString: "#FF7DAF")!.withAlphaComponent(0.54).cgColor,
        ]

        layer.locations = [0, 1]
        layer.startPoint = CGPoint(x: 0.5, y: 0.5)
        layer.endPoint = CGPoint(x: 1, y: 1)
        return layer
    }()
    
    fileprivate lazy var contentGradientLayer: CAGradientLayer = {
        let layer = CAGradientLayer()
        layer.colors = [
            UIColor(hexString: "#FD5ED5")!.cgColor,
            UIColor(hexString: "#FEE663")!.cgColor,
        ]

        layer.locations = [0, 1]
        layer.startPoint = CGPoint(x: 0.0, y: 0.0)
        layer.endPoint = CGPoint(x: 1, y: 1)
        return layer
    }()
    
    fileprivate lazy var contentImageView: UIImageView = {
        let view = UIImageView()
        view.image = UIImage.sceneImage(name: "live_linklive")
        return view
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubView()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    fileprivate func _loadSubView() {
        layer.addSublayer(bgGradientLayer)
        layer.addSublayer(contentGradientLayer)
        addSubview(contentImageView)
    }
    
    func startAnimation() {
        removeAnimation()
        
        let keyAnim1 = CAKeyframeAnimation(keyPath: "transform.scale")
        keyAnim1.duration = 1
        keyAnim1.values = [minAnimationScaleValue, maxAnimationScaleValue, minAnimationScaleValue]
        keyAnim1.beginTime = CACurrentMediaTime()
        keyAnim1.repeatCount = Float.infinity
        
        let keyAnim2 = CAKeyframeAnimation(keyPath: "opacity")
        keyAnim2.duration = 1
        keyAnim2.values = [0.5, 1, 0.5]
        keyAnim2.beginTime = CACurrentMediaTime()
        keyAnim2.repeatCount = Float.infinity
        
        bgGradientLayer.add(keyAnim1, forKey: "wave_animation_scale")
        bgGradientLayer.add(keyAnim2, forKey: "wave_animation_opacity")
    }
    
    func removeAnimation() {
        bgGradientLayer.removeAllAnimations()
    }
    
    override func didAddSubview(_ subview: UIView) {
        super.didAddSubview(subview)
        startAnimation()
    }
    
    override func willRemoveSubview(_ subview: UIView) {
        super.willRemoveSubview(subview)
        removeAnimation()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        bgGradientLayer.frame = bounds
        bgGradientLayer.cornerRadius = bgGradientLayer.frame.size.height / 2
        
        contentGradientLayer.frame = bounds.insetBy(dx: 8, dy: 8)
        contentGradientLayer.cornerRadius = contentGradientLayer.frame.size.height / 2
        
        contentImageView.frame = bounds.insetBy(dx: 14, dy: 14)
    }
}

class TouchWaveTextButton: TouchWaveView {
    var content: String = "" {
        didSet {
            textLabel.text = content
            textLabel.sizeToFit()
            sizeToFit()
        }
    }
    private lazy var textLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 14)
        label.textColor = .white
        return label
    }()
    
    override func _loadSubView() {
        super._loadSubView()
        addSubview(textLabel)
        maxAnimationScaleValue = 1.1
    }
    
    override func sizeThatFits(_ size: CGSize) -> CGSize {
        var size = CGSize(width: 0, height: 38)
        
        let width = aui_height - 12
        size.width = width + textLabel.aui_width + 14 + 6 + 10
        return size
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        contentGradientLayer.frame = bounds.insetBy(dx: 4, dy: 4)
        contentGradientLayer.cornerRadius = contentGradientLayer.frame.size.height / 2
        
        let width = aui_height - 12
        contentImageView.frame = CGRect(x: 10, y: 6, width: width, height: width)
        textLabel.aui_centerY = contentImageView.aui_centerY
        textLabel.aui_left = contentImageView.aui_right + 6
        
        startAnimation()
    }
}
