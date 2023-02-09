//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import Foundation

public class SACrossZoomAnimation: SAPresentationAnimation {
    private var scale: CGFloat

    public init(scale: CGFloat, options: SAAnimationOptions = .normal(duration: 0.3), origin: SAPresentationOrigin? = nil) {
        self.scale = scale
        super.init(options: options, origin: origin)
    }

    override public func beforeAnimation(animationContext: SAAnimationContext) {
        animationContext.animatingView?.frame = animationContext.finalFrame
        let translate = calculateTranslate(animationContext: animationContext)
        animationContext.animatingView?.transform = animationContext.isPresenting ? CGAffineTransform(translationX: translate.x, y: translate.y).scaledBy(x: scale, y: scale) : .identity
    }

    override public func performAnimation(animationContext: SAAnimationContext) {
        let translate = calculateTranslate(animationContext: animationContext)
        animationContext.animatingView?.transform = animationContext.isPresenting ? .identity : CGAffineTransform(translationX: translate.x, y: translate.y).scaledBy(x: scale, y: scale)
    }

    private func calculateTranslate(animationContext: SAAnimationContext) -> CGPoint {
        let finalFrame = animationContext.finalFrame
        let initialFrame = transformInitialFrame(containerFrame: animationContext.containerView.frame, finalFrame: finalFrame)
        let translate = CGPoint(x: initialFrame.minX - finalFrame.minX, y: initialFrame.minY - finalFrame.minY)
        return translate
    }
}
