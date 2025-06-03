//
//  AUIPraiseEmitterView.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit
import AudioToolbox
/*!
 *  \~Chinese
 *  点赞动画效果视图
 *
 *  \~English
 *  Like animation effect view.
 *
 */
public class AUIPraiseEffectView: UIView, CAAnimationDelegate {
    private var images = [UIImage.aui_Image(named: "finger_heart"), UIImage.aui_Image(named: "thunder"), UIImage.aui_Image(named: "thumbs_up"), UIImage.aui_Image(named: "No_of_the_beast"), UIImage.aui_Image(named: "lips"), UIImage.aui_Image(named: "heart")]

    private var liveLayers = [CALayer]()

    private var deleteLayers = [CALayer]()

    public var count = UInt(0)
    
    public var touchFeedback: Bool = true

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    /// Description 初始化方法
    /// - Parameters:
    ///   - frame: 坐标
    ///   - images: 图片数组
    ///   - touchFeedback: 是否开启触感反馈
    /// Description initialization method
    /// - Parameters:
    ///   - frame: coordinates
    ///   - images: array of images
    ///   - touchFeedback: Whether to enable touch feedback
    public convenience init(frame: CGRect,images: [UIImage], touchFeedback: Bool = true) {
        self.init(frame: frame)
        if !images.isEmpty {
            self.images = images
        }
        self.touchFeedback = touchFeedback
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func shakeDevice() {
        AudioServicesPlaySystemSound(1520)
    }
    
    /// Description 播放点赞动画
    /// Description Play like animation
    public func setupEmitter() {
        if self.touchFeedback {
            self.shakeDevice()
        }
        self.count += 1
        if self.count == UInt.max {
            self.count = 0
        }
        var shipLayer: CALayer?
        if self.deleteLayers.count > 0 {
            shipLayer = self.deleteLayers.first
            self.deleteLayers.removeAll { $0 == shipLayer }
        } else {
            shipLayer = CALayer()
            let image = self.images[safe: Int(self.count) % self.images.count] ?? UIImage()
            shipLayer?.contents = image?.cgImage
            shipLayer?.contentsScale = UIScreen.main.scale
            shipLayer?.frame = CGRect(x: bounds.width / 2.0, y: bounds.height+55, width: 40, height: 40)
            shipLayer?.transform = CATransform3DRotate(CATransform3DIdentity, CGFloat(Double.pi / 2.0), 0, 0, 1)
        }
        shipLayer?.opacity = 1.0
        self.layer.addSublayer(shipLayer!)
        self.liveLayers.append(shipLayer!)
        self.animationKeyFrameWithLayer(layer: shipLayer!)
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
        let layer = self.liveLayers.first
        layer?.removeAllAnimations()
        self.deleteLayers.append(layer!)
        layer?.removeFromSuperlayer()
        self.liveLayers.removeAll { $0 == layer! }
    }
}

