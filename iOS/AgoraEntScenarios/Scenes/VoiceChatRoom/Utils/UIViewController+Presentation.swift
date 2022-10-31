//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import Foundation

/// 遵守PresentationViewType协议的UIViewController
public typealias PresentationViewController = UIViewController & PresentedViewType

public extension UIViewController {
    /// 自定义present方法
    func presentViewController(_ viewController: PresentationViewController, animated: Bool = true) {
        viewController.modalPresentationStyle = .custom
        viewController.transitioningDelegate = self
        present(viewController, animated: animated, completion: nil)
    }
}

// MARK: -  UIViewControllerTransitioningDelegate

extension UIViewController: UIViewControllerTransitioningDelegate {
    public func presentationController(forPresented presented: UIViewController, presenting: UIViewController?, source: UIViewController) -> UIPresentationController? {
        return PresentationController(presentedViewController: presented, presenting: presenting)
    }

    public func animationController(forPresented presented: UIViewController, presenting: UIViewController, source: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        guard let presentedVC = presented as? PresentedViewType else { return nil }
        return presentedVC.presentTransitionType.animation
    }

    public func animationController(forDismissed dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        guard let dismissedVC = dismissed as? PresentedViewType else { return nil }
        return dismissedVC.dismissTransitionType.animation
    }
}
