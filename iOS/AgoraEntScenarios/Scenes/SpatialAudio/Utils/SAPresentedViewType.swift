//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import Foundation

/// presentedView的设置
public struct SAPresentedViewComponent {
    /// presentedView的size
    public var contentSize: CGSize

    /// presentedView最终展示位置
    public var destination: SAPresentationDestination = .bottomBaseline

    /// present转场动画，为nil则基于destination使用
    public var presentTransitionType: SATransitionType?

    /// dismiss转场动画，为nil则基于destination使用
    public var dismissTransitionType: SATransitionType?

    /// 是否开启点击背景dismiss
    public var canTapBGDismiss: Bool = true

    /// 是否开启pan手势dismiss
    public var canPanDismiss: Bool = true

    /// pan手势方向，为nil则基于destination使用
    public var panDismissDirection: SAPanDismissDirection?

    /// 键盘出现的平移方式，默认贴近PresentedView
    public var keyboardTranslationType: SAKeyboardTranslationType = .unabgeschirmt(compress: true)

    /// 键盘间隔，默认20
    public var keyboardPadding: CGFloat = 0

    /// 初始化方法
    ///
    /// - Parameters:
    ///   - contentSize: presentedView的size
    ///   - destination: presentedView最终展示位置
    ///   - presentTransitionType: present转场动画
    ///   - dismissTransitionType: dismiss转场动画
    ///   - canTapBGDismiss:  是否开启点击背景dismiss
    ///   - canPanDismiss: 是否开启pan手势dismiss
    ///   - panDismissDirection: pan手势方向
    ///   - keyboardTranslationType: 键盘出现的平移方式，默认贴近PresentedView
    ///   - keyboardPadding: 键盘间隔，默认20
    public init(contentSize: CGSize,
                destination: SAPresentationDestination = .bottomBaseline,
                presentTransitionType: SATransitionType? = nil,
                dismissTransitionType: SATransitionType? = nil,
                canTapBGDismiss: Bool = true,
                canPanDismiss: Bool = true,
                panDismissDirection: SAPanDismissDirection? = nil,
                keyboardTranslationType: SAKeyboardTranslationType = .unabgeschirmt(compress: true),
                keyboardPadding: CGFloat = 0)
    {
        self.contentSize = contentSize
        self.destination = destination
        self.presentTransitionType = presentTransitionType
        self.dismissTransitionType = dismissTransitionType
        self.canTapBGDismiss = canTapBGDismiss
        self.canPanDismiss = canPanDismiss
        self.panDismissDirection = panDismissDirection
        self.keyboardTranslationType = keyboardTranslationType
        self.keyboardPadding = keyboardPadding
    }
}

/// presentedView必须遵守此协议
public protocol SAPresentedViewType {
    /// presentedView的设置
    var presentedViewComponent: SAPresentedViewComponent? { get set }
}

extension SAPresentedViewType {
    var presentTransitionType: SATransitionType {
        if self is UINavigationController {
            let vc = (self as? UINavigationController)?.viewControllers.last as? SAAlertViewController
            if vc?.presentedViewComponent?.presentTransitionType != nil {
                return vc!.presentedViewComponent!.presentTransitionType ?? .translation(origin: (vc?.presentedViewComponent?.destination.defaultOrigin ?? .none) ?? .bottomOutOfLine)
            }
            return .translation(origin: (vc?.presentedViewComponent?.destination.defaultOrigin ?? .bottomOutOfLine))
        }
        return presentedViewComponent!.presentTransitionType ?? .translation(origin: presentedViewComponent!.destination.defaultOrigin)
    }

    var dismissTransitionType: SATransitionType {
        if self is UINavigationController {
            let vc = (self as? UINavigationController)?.viewControllers.last as? SAAlertViewController
            if vc?.presentedViewComponent!.dismissTransitionType != nil {
                return vc!.presentedViewComponent!.dismissTransitionType ?? presentTransitionType
            }
            return presentTransitionType
        }
        return presentedViewComponent!.dismissTransitionType ?? presentTransitionType
    }
}
