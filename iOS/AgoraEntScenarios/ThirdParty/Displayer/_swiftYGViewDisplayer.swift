//
//  _swiftYGViewDisplayer.swift
//  YGKit
//
//  Created by edz on 2021/6/23.
//

// import SwiftEntryKit
import ObjectiveC.runtime
import UIKit
@objc public enum _swiftYGViewDisplayOptionsScroll: Int {
    case disabled
    case swap
}

/// 用户交互
@objc public enum _swiftYGViewDisplayOptionsUserInteraction: Int {
    /// 消失
    case dismiss
    /// 向下层view传递事件
    case forward
    /// 拦截
    case absorb
    fileprivate var ekValue: EKAttributes.UserInteraction {
        switch self {
        case .dismiss: return .dismiss
        case .forward: return .forward
        case .absorb: return .absorbTouches
        }
    }
}

/// 布局
@objc public enum _swiftYGViewDisplayOptionsPositionConstraints: Int {
    /// 宽度拉满
    case fullWidth
    /// 充满屏幕
    case fullScreen
    /// 悬浮
    case float
    fileprivate var ekValue: EKAttributes.PositionConstraints {
        switch self {
        case .fullWidth: return .fullWidth
        case .fullScreen: return .fullScreen
        case .float: return .float
        }
    }
}

/// safeArea
@objc public enum _swiftYGViewDisplayOptionsSafeArea: Int {
    /// 空
    case empty
    /// 空且使用background填充
    case emptyFill
    /// 内容覆盖
    case overridden
    fileprivate var ekValue: EKAttributes.PositionConstraints.SafeArea {
        switch self {
        case .empty: return .empty(fillSafeArea: false)
        case .emptyFill: return .empty(fillSafeArea: true)
        case .overridden: return .overridden
        }
    }
}

/// 状态栏
@objc public enum _swiftYGViewDisplayOptionsStatusBar: Int {
    /// 隐藏
    case hidden
    /// 高亮
    case light
    /// 置灰
    case dark
    /// ignore
    case ignored
    fileprivate var ekValue: EKAttributes.StatusBar {
        switch self {
        case .hidden: return .hidden
        case .light: return .light
        case .dark: return .dark
        case .ignored: return .ignored
        }
    }
}

/// 优先级
@objc public enum _swiftYGViewDisplayPriority: Int {
    case max
    case high
    case normal
    case low
    case min
    fileprivate var ekValue: EKAttributes.Precedence.Priority {
        switch self {
        case .max: return .max
        case .high: return .high
        case .normal: return .normal
        case .low: return .low
        case .min: return .min
        }
    }
}

@objc public enum _swiftYGViewDisplayOptionsWindowLevel: Int {
    case alert
    case statusBar
    case normal
    fileprivate var ekValue: EKAttributes.WindowLevel {
        switch self {
        case .alert: return .alerts
        case .statusBar: return .statusBar
        case .normal: return .normal
        }
    }
}

public class _swiftYGViewDisplayOptions: NSObject {
    private static var identityIndex: Int = 0
    @objc public lazy var identity: String = {
        defer {
            _swiftYGViewDisplayOptions.identityIndex = _swiftYGViewDisplayOptions.identityIndex + 1
        }
        return "yg.view.display.\(_swiftYGViewDisplayOptions.identityIndex)"
    }()

    /// 多久后自动消失，默认为MAXFLOAT
    @objc public var duration: Float = .infinity
    /// 遮罩区颜色，默认为UIColor(white: 100.0/255.0, alpha: 0.2)
    @objc public var screenBackgroundColor: UIColor = .init(white: 100.0 / 255.0, alpha: 0.2)
    /// 内容区背景色，默认为white
    @objc public var backgroundColor: UIColor = .white
    /// 圆角，默认20，浮窗为四角，底部推出为上部分
    @objc public var cornerRidus: CGFloat = 15
    /// 布局fullWidth
    @objc public var positionConstraints: _swiftYGViewDisplayOptionsPositionConstraints = .fullWidth
    /// 点击内容区 默认为absorb
    @objc public var interaction: _swiftYGViewDisplayOptionsUserInteraction = .absorb
    /// 点击屏幕 默认为absorb
    @objc public var screenInteraction: _swiftYGViewDisplayOptionsUserInteraction = .absorb
    /// safearea
    @objc public var safeArea: _swiftYGViewDisplayOptionsSafeArea = .emptyFill
    /// statusBar
    @objc public var statusBar: _swiftYGViewDisplayOptionsStatusBar = .ignored
    /// windowlevel
    @objc public var windowLevel: _swiftYGViewDisplayOptionsWindowLevel = .normal
    /// 最大宽度
    @objc public var maxSize: CGSize = UIScreen.main.bounds.size
    /// 优先级
    @objc public var priority: _swiftYGViewDisplayPriority = .normal
}

public class _swiftYGViewDisplayer: NSObject {
    @objc public static func useWindowScene(_ value: Bool) {
        EKWindow.useWindowScene = value
    }

    private enum Style {
        case centerFloat
        case bottomFloat
        case `default`
        var ekAttributes: EKAttributes {
            switch self {
            case .centerFloat: return EKAttributes.centerFloat
            case .bottomFloat: return EKAttributes.bottomFloat
            case .default: return .centerFloat
            }
        }
    }

    /// 底部弹出到中间
    @objc public static func popupCenter(_ view: UIView, setupBlock: ((_swiftYGViewDisplayOptions) -> Void)?) {
        let options = _swiftYGViewDisplayOptions()
        options.positionConstraints = .float
        options.safeArea = .overridden
        options.priority = .max
        setupBlock?(options)
        var attributes = self.attributes(.centerFloat, options)
        attributes.shadow = .active(
            with: .init(
                color: .black,
                opacity: 0.3,
                radius: 8
            )
        )
        attributes.entranceAnimation = .init(
            translate: .init(
                duration: 0.7,
                spring: .init(damping: 0.7, initialVelocity: 0)
            ),
            scale: .init(
                from: 0.7,
                to: 1,
                duration: 0.4,
                spring: .init(damping: 1, initialVelocity: 0)
            )
        )
        attributes.exitAnimation = .init(
            translate: .init(duration: 0.2)
        )
        attributes.popBehavior = .animated(
            animation: .init(
                translate: .init(duration: 0.35)
            )
        )
        ekDisplay(view, attributes)
    }

    /// 底部弹出固定在底部，默认宽度充满屏幕
    @objc public static func popupBottom(_ view: UIView, setupBlock: ((_swiftYGViewDisplayOptions) -> Void)?) {
        let options = _swiftYGViewDisplayOptions()
        options.priority = .max
        options.positionConstraints = .fullWidth
        setupBlock?(options)
        var attributes = self.attributes(.bottomFloat, options)
        attributes.shadow = .active(
            with: .init(
                color: .black,
                opacity: 0.1,
                radius: 8
            )
        )

        attributes.exitAnimation = .init(
            translate: .init(duration: 0.2)
        )
        attributes.popBehavior = .animated(
            animation: .init(
                translate: .init(duration: 0.2)
            )
        )
        attributes.entranceAnimation = .init(
            translate: .init(
                duration: 0.5,
                spring: .init(damping: 1, initialVelocity: 0)
            )
        )
        ekDisplay(view, attributes)
    }

    /// 淡出在中间
    @objc public static func fadeCenter(_ view: UIView, setupBlock: ((_swiftYGViewDisplayOptions) -> Void)?) {
        let options = _swiftYGViewDisplayOptions()
        options.positionConstraints = .float
        options.safeArea = .overridden
        options.priority = .max
        setupBlock?(options)
        var attributes = self.attributes(.centerFloat, options)
        attributes.entranceAnimation = .init(
            scale: .init(
                from: 0.9,
                to: 1,
                duration: 0.3,
                spring: .init(damping: 1, initialVelocity: 0)
            ),
            fade: .init(
                from: 0,
                to: 1,
                duration: 0.3
            )
        )
        attributes.exitAnimation = .init(
            fade: .init(
                from: 1,
                to: 0,
                duration: 0.2
            )
        )
        attributes.shadow = .active(
            with: .init(
                color: .black,
                opacity: 0.1,
                radius: 5
            )
        )
        ekDisplay(view, attributes)
    }

    @objc public static func display(_ view: UIView, setupBlock: ((_swiftYGViewDisplayOptions) -> Void)?) {
        let options = _swiftYGViewDisplayOptions()
        options.duration = .infinity
        options.screenBackgroundColor = .clear
        options.backgroundColor = .clear
        options.positionConstraints = .fullScreen
        options.screenInteraction = .absorb
        options.interaction = .absorb
        options.safeArea = .overridden
        options.priority = .max
        options.cornerRidus = 0
        setupBlock?(options)
        print("window-----\(options.windowLevel.rawValue)")
        var attributes = self.attributes(.default, options)
        attributes.entranceAnimation = .none
        attributes.exitAnimation = .none
        attributes.shadow = .none
        ekDisplay(view, attributes)
    }

    private enum AssociatedKeys { static var displayIdentity = "displayIdentity" }
    private static func ekDisplay(_ view: UIView, _ attributes: EKAttributes) {
        objc_setAssociatedObject(view, &AssociatedKeys.displayIdentity, attributes.name, .OBJC_ASSOCIATION_COPY_NONATOMIC)
        SwiftEntryKit.display(entry: view, using: attributes)
    }

    /// dismiss某一个view
    @objc public static func dismiss(_ view: UIView, completionHandler: (() -> Void)?) {
        guard let name = objc_getAssociatedObject(view, &AssociatedKeys.displayIdentity) as? String else {
            return
        }
        SwiftEntryKit.dismiss(.specific(entryName: name), with: completionHandler)
    }

    /// dismiss当前
    @objc public static func dismissDisplayed(_ completionHandler: (() -> Void)?) {
        SwiftEntryKit.dismiss(.displayed, with: completionHandler)
    }

    @objc public static func dismiss(identity: String, completionHandler: (() -> Void)?) {
        SwiftEntryKit.dismiss(.specific(entryName: identity), with: completionHandler)
    }

    /// 移出所有，当前显示的和队列中的
    @objc public static func dismissAll(_ completionHandler: (() -> Void)?) {
        SwiftEntryKit.dismiss(.all, with: completionHandler)
    }

    private static func attributes(_ style: Style, _ options: _swiftYGViewDisplayOptions) -> EKAttributes {
        var attributes = style.ekAttributes
        attributes.name = options.identity
        attributes.displayMode = .light
        attributes.displayDuration = EKAttributes.DisplayDuration(options.duration)
        attributes.screenBackground = .color(color: .init(options.screenBackgroundColor))
        attributes.entryBackground = .color(color: .init(options.backgroundColor))
        attributes.positionConstraints = options.positionConstraints.ekValue
        attributes.positionConstraints.safeArea = options.safeArea.ekValue
        attributes.positionConstraints.maxSize = .init(width: .constant(value: options.maxSize.width), height: .constant(value: options.maxSize.height))
        attributes.precedence = .enqueue(priority: options.priority.ekValue)
        attributes.roundCorners = .all(radius: options.cornerRidus)
        attributes.screenInteraction = options.screenInteraction.ekValue
        attributes.entryInteraction = options.interaction.ekValue
        attributes.statusBar = options.statusBar.ekValue
        attributes.windowLevel = options.windowLevel.ekValue
        attributes.scroll = .disabled
        return attributes
    }
}
