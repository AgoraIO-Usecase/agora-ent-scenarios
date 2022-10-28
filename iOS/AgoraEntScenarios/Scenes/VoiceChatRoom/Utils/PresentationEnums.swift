//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import Foundation

/// pan手势滑动方向
///
/// - bottom: 向下
/// - top: 向上
/// - left: 向左
/// - right: 向右
public enum PanDismissDirection {
    case down
    case up
    case left
    case right
}

/// present的起始位置
///
/// - center: 屏幕中心
/// - bottomOutOfLine: 屏幕底部以下
/// - leftOutOfLine: 屏幕左边以外
/// - rightOutOfLine: 屏幕右边以外
/// - topOutOfLine: 屏幕上部以上
/// - custom: 自定义中心点
public enum PresentationOrigin: Equatable {
    case center
    case bottomOutOfLine
    case leftOutOfLine
    case rightOutOfLine
    case topOutOfLine
    case custom(center: CGPoint)

    // MARK: -  Equatable

    public static func == (lhs: PresentationOrigin, rhs: PresentationOrigin) -> Bool {
        switch (lhs, rhs) {
        case (.center, .center):
            return true
        case (.bottomOutOfLine, .bottomOutOfLine):
            return true
        case (.leftOutOfLine, .leftOutOfLine):
            return true
        case (.rightOutOfLine, .rightOutOfLine):
            return true
        case (.topOutOfLine, .topOutOfLine):
            return true
        case let (.custom(lhsCenter), .custom(rhsCenter)):
            return lhsCenter == rhsCenter
        default:
            return false
        }
    }
}

/// present的最终的位置
///
/// - center: 屏幕中心
/// - bottomBaseline: 基于屏幕底部
/// - leftBaseline: 基于屏幕左边
/// - rightBaseline: 基于屏幕右边
/// - topBaseline: 基于屏幕上部
/// - custom: 自定义中心点
public enum PresentationDestination: Equatable {
    case center
    case bottomBaseline
    case leftBaseline
    case rightBaseline
    case topBaseline
    case custom(center: CGPoint)

    /// pan手势方向
    var panDirection: PanDismissDirection {
        switch self {
        case .center, .bottomBaseline, .custom:
            return .down
        case .leftBaseline:
            return .left
        case .rightBaseline:
            return .right
        case .topBaseline:
            return .up
        }
    }

    /// 默认的起始位置
    var defaultOrigin: PresentationOrigin {
        switch self {
        case .center:
            return .center
        case .leftBaseline:
            return .leftOutOfLine
        case .rightBaseline:
            return .rightOutOfLine
        case .topBaseline:
            return .topOutOfLine
        default:
            return .bottomOutOfLine
        }
    }

    // MARK: -  Equatable

    public static func == (lhs: PresentationDestination, rhs: PresentationDestination) -> Bool {
        switch (lhs, rhs) {
        case (.center, .center):
            return true
        case (.bottomBaseline, .bottomBaseline):
            return true
        case (.leftBaseline, .leftBaseline):
            return true
        case (.rightBaseline, .rightBaseline):
            return true
        case (.topBaseline, .topBaseline):
            return true
        case let (.custom(lhsCenter), .custom(rhsCenter)):
            return lhsCenter == rhsCenter
        default:
            return false
        }
    }
}

/// 转场动画类型
///
/// - translation: 平移
/// - crossDissolve: 淡入淡出
/// - crossZoom: 缩放
/// - flipHorizontal: 水平翻转
/// - custom: 自定义动画
public enum TransitionType: Equatable {
    case translation(origin: PresentationOrigin)
    case crossDissolve
    case crossZoom
    case flipHorizontal
    case custom(animation: PresentationAnimation)

    var animation: PresentationAnimation {
        switch self {
        case let .translation(origin):
            return PresentationAnimation(origin: origin)
        case .crossDissolve:
            return CrossDissolveAnimation()
        case .crossZoom:
            return CrossZoomAnimation(scale: 0.1)
        case .flipHorizontal:
            return FlipHorizontalAnimation()
        case let .custom(animation):
            return animation
        }
    }

    // MARK: -  Equatable

    public static func == (lhs: TransitionType, rhs: TransitionType) -> Bool {
        switch (lhs, rhs) {
        case let (.translation(lhsOrigin), .translation(rhsOrigin)):
            return lhsOrigin == rhsOrigin
        case (.crossDissolve, .crossDissolve):
            return true
        case (.flipHorizontal, .flipHorizontal):
            return true
        case (.crossZoom, .crossZoom):
            return true
        case let (.custom(lhsAnimation), .custom(rhsAnimation)):
            return lhsAnimation == rhsAnimation
        default:
            return false
        }
    }
}

/// 动画选项设置
///
/// - normal: 正常类型
/// - spring: 弹簧类型
public enum AnimationOptions {
    case normal(duration: TimeInterval)
    case spring(duration: TimeInterval, delay: TimeInterval, damping: CGFloat, velocity: CGFloat)

    var duration: TimeInterval {
        switch self {
        case let .normal(duration):
            return duration
        case let .spring(duration, _, _, _):
            return duration
        }
    }
}

/// 键盘出现的平移方式
///
/// - unabgeschirmt: 不遮挡PresentedView，compress: 键盘是否贴近PresentedView
/// - compressInputView: 贴近输入框
public enum KeyboardTranslationType {
    case unabgeschirmt(compress: Bool)
    case compressInputView
}
