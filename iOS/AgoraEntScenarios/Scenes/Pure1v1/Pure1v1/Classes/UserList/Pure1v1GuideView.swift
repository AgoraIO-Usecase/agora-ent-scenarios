//
//  Pure1v1GuideView.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/24.
//

import Foundation

private let kGuideAnimationKey = "wave_animation"
class Pure1v1GuideView: UIView {
    private lazy var gradientLayer: CAGradientLayer = {
        let layer = CAGradientLayer()
        layer.colors = [
            UIColor.white.cgColor,
            UIColor.white.withAlphaComponent(0.1).cgColor,
            UIColor.clear.cgColor
        ]

        return layer
    }()
    private lazy var imageView: UIImageView = {
       let view = UIImageView()
        view.image = UIImage.sceneImage(name: "guide_finger")
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
        backgroundColor = .clear
        layer.addSublayer(gradientLayer)
        
        addSubview(imageView)
        _layoutSubviews()
    }
    
    private func _layoutSubviews() {
        
        let size = CGSize(width: 14, height: 136)
        gradientLayer.cornerRadius = 7
        gradientLayer.frame = CGRect(origin: CGPoint(x: self.aui_width / 2 - size.width, y: (self.aui_height - size.height) / 2), size: size)
        imageView.aui_top = gradientLayer.frame.origin.y
        imageView.aui_right = gradientLayer.frame.origin.x + gradientLayer.frame.size.width
        imageView.aui_size = CGSize(width: 84, height: 55)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        _layoutSubviews()
    }
    
    private func _startAnimation() {
        _removeAnimation()
        
        let anim = CABasicAnimation(keyPath: "position")
        anim.duration = 0.8
        anim.repeatCount = Float.infinity
        anim.fromValue = CGPoint(x: imageView.aui_centerX, y: gradientLayer.frame.origin.y)
        anim.toValue = CGPoint(x: imageView.aui_centerX, y: gradientLayer.frame.origin.y + gradientLayer.frame.height)
        imageView.layer.add(anim, forKey: kGuideAnimationKey)
    }
    
    private func _removeAnimation() {
        imageView.layer.removeAllAnimations()
    }
    
    override func willMove(toSuperview newSuperview: UIView?) {
        if newSuperview == nil {
            _removeAnimation()
        } else {
            _startAnimation()
        }
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesEnded(touches, with: event)
        
        removeFromSuperview()
    }
}
