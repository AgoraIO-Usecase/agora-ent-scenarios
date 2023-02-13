//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import Foundation

/// 遵守PresentationViewType协议的UIViewController
public typealias SAPresentationViewController = UIViewController & SAPresentedViewType

public extension SABaseViewController {
    /// 自定义present方法
    func sa_presentViewController(_ viewController: SAPresentationViewController, animated: Bool = true) {
        viewController.modalPresentationStyle = .custom
        viewController.transitioningDelegate = self
        present(viewController, animated: animated, completion: nil)
    }
}

// MARK: -  UIViewControllerTransitioningDelegate
//#if DEBUG
//#else
extension SABaseViewController: UIViewControllerTransitioningDelegate {
    public func presentationController(forPresented presented: UIViewController, presenting: UIViewController?, source: UIViewController) -> UIPresentationController? {
        return SAPresentationController(presentedViewController: presented, presenting: presenting)
    }

    public func animationController(forPresented presented: UIViewController, presenting: UIViewController, source: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        guard let presentedVC = presented as? SAPresentedViewType else { return nil }
        return presentedVC.presentTransitionType.animation
    }

    public func animationController(forDismissed dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        guard let dismissedVC = dismissed as? SAPresentedViewType else { return nil }
        return dismissedVC.dismissTransitionType.animation
    }
}
//#endif
