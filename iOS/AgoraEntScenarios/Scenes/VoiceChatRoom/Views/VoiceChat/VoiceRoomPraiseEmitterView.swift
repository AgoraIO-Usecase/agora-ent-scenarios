//
//  VoiceRoomEmitterView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/7.
//

import UIKit

public class VoiceRoomPraiseEmitterView: UIView, CAAnimationDelegate {
    var images = ["finger_heart", "thunder", "thumbs_up", "No_of_the_beast", "lips", "heart"]

    var liveLayers = [CALayer]()

    var deleteLayers = [CALayer]()

    var count = UInt(0)

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func setupEmitter() {
        count += 1
        if count == UInt.max {
            count = 0
        }
        var shipLayer: CALayer?
        if deleteLayers.count > 0 {
            shipLayer = deleteLayers.first
            deleteLayers.removeAll { $0 == shipLayer }
        } else {
            shipLayer = CALayer()
            shipLayer?.contents = UIImage(images[Int(count) % images.count])?.cgImage
            shipLayer?.contentsScale = UIScreen.main.scale
            shipLayer?.frame = CGRect(x: bounds.width / 2.0, y: bounds.height, width: 40, height: 40)
            shipLayer?.transform = CATransform3DRotate(CATransform3DIdentity, CGFloat(Double.pi / 2.0), 0, 0, 1)
        }
        shipLayer?.opacity = 1.0
        layer.addSublayer(shipLayer!)
        liveLayers.append(shipLayer!)
        animationKeyFrameWithLayer(layer: shipLayer!)
    }

    func animationKeyFrameWithLayer(layer: CALayer) {
        let width = bounds.width
        let height = bounds.height
        let path = UIBezierPath()
        path.move(to: CGPoint(x: width / 2.0, y: height))
        path.addCurve(to: CGPoint(x: CGFloat(arc4random()).truncatingRemainder(dividingBy: width), y: 0), controlPoint1: CGPoint(x: (CGFloat(arc4random()).truncatingRemainder(dividingBy: width)) / 2.0, y: (CGFloat(arc4random()).truncatingRemainder(dividingBy: height)) / 2.0), controlPoint2: CGPoint(x: (CGFloat(arc4random()).truncatingRemainder(dividingBy: width)) / 2.0 + width / 2.0, y: (CGFloat(arc4random()).truncatingRemainder(dividingBy: height)) / 2.0 + height / 2.0))

        let animation = CAKeyframeAnimation(keyPath: "position")
        animation.duration = 1.2 + (CGFloat(arc4random()).truncatingRemainder(dividingBy: 9) / 10.0)
        animation.rotationMode = .rotateAuto
        animation.timingFunction = CAMediaTimingFunction(name: CAMediaTimingFunctionName.easeIn)
        animation.path = path.cgPath
        animation.fillMode = .forwards

        let scaleAnimation = CABasicAnimation(keyPath: "transform.scale")
        scaleAnimation.fromValue = NSNumber(value: 1.1)
        scaleAnimation.toValue = NSNumber(value: 0.7)
        scaleAnimation.duration = 1 + ((CGFloat(arc4random()).truncatingRemainder(dividingBy: 10)) / 10.0)

        let alphaAnimation = CABasicAnimation(keyPath: "opacity")
        alphaAnimation.fromValue = NSNumber(value: 1.0)
        alphaAnimation.toValue = NSNumber(value: 0)
        alphaAnimation.duration = animation.duration
        alphaAnimation.fillMode = .forwards

        let animationGroup = CAAnimationGroup()
        animationGroup.repeatCount = 1
        animationGroup.isRemovedOnCompletion = false
        animationGroup.duration = animation.duration
        animationGroup.fillMode = .forwards
        animationGroup.animations = [animation, scaleAnimation, alphaAnimation]
        animation.delegate = self
        layer.add(animationGroup, forKey: "likeAnimation\(count)")
    }

    public func animationDidStop(_ anim: CAAnimation, finished flag: Bool) {
        let layer = liveLayers.first
        layer?.removeAllAnimations()
        deleteLayers.append(layer!)
        layer?.removeFromSuperlayer()
        liveLayers.removeAll { $0 == layer! }
    }
}
