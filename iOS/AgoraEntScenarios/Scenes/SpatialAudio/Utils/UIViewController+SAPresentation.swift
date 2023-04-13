//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import Foundation

/// 遵守PresentationViewType协议的UIViewController
public typealias SAPresentationViewController = UIViewController & SAPresentedViewType
public typealias SAPresentationNavigationController = UINavigationController & SAPresentedViewType

public extension SABaseViewController {
    /// 自定义present方法
    func sa_presentViewController(_ viewController: SAPresentationViewController, animated: Bool = true) {
        dismiss(animated: false)
        viewController.modalPresentationStyle = .custom
        viewController.transitioningDelegate = self
        present(viewController, animated: animated, completion: nil)
    }
    func sa_navigationViewController(_ viewController: SAPresentationNavigationController, animated: Bool = true) {
        viewController.modalPresentationStyle = .custom
        viewController.transitioningDelegate = self
        present(viewController, animated: animated, completion: nil)
    }
}

extension SABaseViewController: UIViewControllerTransitioningDelegate {
    public func presentationController(forPresented presented: UIViewController, presenting: UIViewController?, source: UIViewController) -> UIPresentationController? {
        if presented is SAAlertNavigationController {
            guard let vc = (presented as? SAAlertNavigationController)?.viewControllers.last as? SAAlertViewController else { return nil }
            (presented as? SAAlertNavigationController)?.presentedViewComponent = vc.presentedViewComponent
        }
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
