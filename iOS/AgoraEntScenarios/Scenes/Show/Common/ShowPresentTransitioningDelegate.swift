//
//  ShowPresentTransitioningDelegate.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/12/1.
//

import UIKit

class ShowPresentAnimator: NSObject , UIViewControllerAnimatedTransitioning {
    
    func transitionDuration(using transitionContext: UIViewControllerContextTransitioning?) -> TimeInterval {
        if let isAnimated = transitionContext?.isAnimated {
            return isAnimated ? 0.3 : 0
        }
        return 0
    }
    
    func animateTransition(using transitionContext: UIViewControllerContextTransitioning) {
        guard let toViewController = transitionContext.viewController(forKey: UITransitionContextViewControllerKey.to) else {return}
        let toView = transitionContext.view(forKey: UITransitionContextViewKey.to)
        var toViewInitalFrame = transitionContext.initialFrame(for: toViewController)
        let toViewFinalFrame = transitionContext.finalFrame(for: toViewController)
        let containerView = transitionContext.containerView
        if toView != nil {
            containerView.addSubview(toView!)
        }
        toViewInitalFrame.origin = CGPoint(x: containerView.bounds.minX, y: containerView.bounds.maxY)
        toViewInitalFrame.size = toViewFinalFrame.size
        toView?.frame = toViewInitalFrame
        
        let duration = transitionDuration(using: transitionContext)
        toView?.alpha = 0
        UIView.animate(withDuration: duration, animations: {
            toView?.frame = toViewFinalFrame
            toView?.alpha = 1
            
        }) { finished in
            let wasCanceled = transitionContext.transitionWasCancelled
            transitionContext.completeTransition(!wasCanceled)
        }
    }
}

class ShowDismissAnimator: NSObject , UIViewControllerAnimatedTransitioning {
    
    func transitionDuration(using transitionContext: UIViewControllerContextTransitioning?) -> TimeInterval {
        if let isAnimated = transitionContext?.isAnimated {
            return isAnimated ? 0.3 : 0
        }
        return 0
    }
    
    func animateTransition(using transitionContext: UIViewControllerContextTransitioning) {
        guard let fromViewController = transitionContext.viewController(forKey: UITransitionContextViewControllerKey.from) else {return}
        let fromView = transitionContext.view(forKey: UITransitionContextViewKey.from)
        let toView = transitionContext.view(forKey: UITransitionContextViewKey.to)
        
        var fromViewFinalFrame = transitionContext.finalFrame(for: fromViewController)
        let containerView = transitionContext.containerView
        if toView != nil {
            containerView.addSubview(toView!)
        }
        
        fromViewFinalFrame = fromView!.frame.offsetBy(dx: 0, dy: fromView!.frame.height)
        let duration = transitionDuration(using: transitionContext)
        UIView.animate(withDuration: duration, delay: 0, animations: {
            fromView?.frame = fromViewFinalFrame
        }) { finished in
            let wasCanceled = transitionContext.transitionWasCancelled
            transitionContext.completeTransition(!wasCanceled)
        }
    }
}


class ShowPresentTransitioningDelegate: NSObject, UIViewControllerTransitioningDelegate {
    
    func animationController(forDismissed dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        return ShowDismissAnimator()
    }
    
    func animationController(forPresented presented: UIViewController, presenting: UIViewController, source: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        return ShowPresentAnimator()
    }
}
