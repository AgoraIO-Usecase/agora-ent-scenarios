//
//  UIView+Animation.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/25.
//

import Foundation

private let kRotationKey = "animtion_rotation_key"
private let kAnimationDefaultDuration: TimeInterval = 0.3
extension CALayer {
    
    /// <#Description#>
    /// - Parameters:
    ///   - from: angle 0-2*pi
    ///   - to: angle 0-2*pi
    ///   - duration: <#duration description#>
    ///   - loop: <#loop description#>
    func startRotationAnimation(from: Float,
                                to: Float,
                                duration: TimeInterval = kAnimationDefaultDuration,
                                loop: Bool = false) {
        stopRotationAnimation()
        let anim = CABasicAnimation(keyPath: "transform.rotation.z")
        anim.duration = duration
        anim.fromValue = from
        anim.toValue = to
        anim.repeatCount = loop ? Float.infinity : 1
        add(anim, forKey: kRotationKey)
    }
    
    func stopRotationAnimation() {
        removeAnimation(forKey: kRotationKey)
    }
}
