//
//  Pure1v1TouchWaveView.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/21.
//

import Foundation

private let kWaveAnimationKey = "wave_animation"
class Pure1v1TouchWaveView: UIControl {
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
    
    private lazy var contentGradientLayer: CAGradientLayer = {
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
    
    private lazy var contentImageView: UIImageView = {
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

    private func _loadSubView() {
        layer.addSublayer(bgGradientLayer)
        layer.addSublayer(contentGradientLayer)
        addSubview(contentImageView)
        startAnimation()
    }
    
    func startAnimation() {
        removeAnimation()
        
        let keyAnim = CAKeyframeAnimation(keyPath: "transform.scale")
        keyAnim.duration = 1
        keyAnim.values = [1, 1.3, 1]
        keyAnim.repeatCount = Float.infinity
        bgGradientLayer.add(keyAnim, forKey: kWaveAnimationKey)
    }
    
    func removeAnimation() {
        bgGradientLayer.removeAllAnimations()
    }
    
    override func willMove(toSuperview newSuperview: UIView?) {
        if newSuperview == nil {
            removeAnimation()
        } else {
            startAnimation()
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        bgGradientLayer.frame = bounds
        bgGradientLayer.cornerRadius = bgGradientLayer.frame.size.width / 2
        
        contentGradientLayer.frame = bounds.insetBy(dx: 8, dy: 8)
        contentGradientLayer.cornerRadius = contentGradientLayer.frame.size.width / 2
        
        contentImageView.frame = bounds.insetBy(dx: 14, dy: 14)
    }
}
