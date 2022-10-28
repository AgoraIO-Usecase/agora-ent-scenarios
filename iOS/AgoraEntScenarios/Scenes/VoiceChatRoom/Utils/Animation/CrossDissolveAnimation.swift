//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import Foundation

public class CrossDissolveAnimation: PresentationAnimation {
    override public func beforeAnimation(animationContext: AnimationContext) {
        super.beforeAnimation(animationContext: animationContext)
        animationContext.animatingView?.alpha = animationContext.isPresenting ? 0.0 : 1.0
    }

    override public func performAnimation(animationContext: AnimationContext) {
        super.performAnimation(animationContext: animationContext)
        animationContext.animatingView?.alpha = animationContext.isPresenting ? 1.0 : 0.0
    }

    override public func afterAnimation(animationContext: AnimationContext) {
        super.afterAnimation(animationContext: animationContext)
        animationContext.animatingView?.alpha = 1.0
    }
}
