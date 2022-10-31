//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import Foundation

// MARK: - UIPresentationController子类，重写present相关属性和方法

public final class PresentationController: UIPresentationController {
    /// present配置
    private let component: PresentedViewComponent

    /// 背景蒙层
    private lazy var backgroundView: UIView = {
        let containerbounds = containerView?.bounds ?? UIScreen.main.bounds
        let backgroundView = UIView(frame: containerbounds)
        backgroundView.backgroundColor = UIColor.black.withAlphaComponent(0.3)
        backgroundView.alpha = 0.0
        if component.canTapBGDismiss {
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(backgroundViewDidTapped))
            backgroundView.addGestureRecognizer(tapGesture)
        }
        return backgroundView
    }()

    /// pan的起始点
    private var panStart: CGPoint = .zero

    /// pan手势方向
    private var panDirection: PanDismissDirection {
        return component.panDismissDirection ?? component.destination.panDirection
    }

    /// 当前输入view
    private var textInputView: UIView?

    /// 键盘frame
    private var keyboardFrame: CGRect?

    /// 键盘动画时间
    private var keyboardAnimationDuration: TimeInterval?

    // MARK: -  override

    override public init(presentedViewController: UIViewController, presenting presentingViewController: UIViewController?) {
        component = (presentedViewController as? PresentedViewType)?.presentedViewComponent ?? PresentedViewComponent(contentSize: CGSize(width: 240, height: 200))
        super.init(presentedViewController: presentedViewController, presenting: presentingViewController)
    }

    override public var frameOfPresentedViewInContainerView: CGRect {
        let containerbounds = containerView?.bounds ?? UIScreen.main.bounds
        let containerWidth = containerbounds.width
        let containerHeight = containerbounds.height
        let contentSize = component.contentSize
        switch component.destination {
        case .center:
            return CGRect(x: (containerWidth - contentSize.width) / 2, y: (containerHeight - contentSize.height) / 2, width: contentSize.width, height: contentSize.height)
        case .bottomBaseline:
            return CGRect(x: (containerWidth - contentSize.width) / 2, y: containerHeight - contentSize.height, width: contentSize.width, height: contentSize.height)
        case .leftBaseline:
            return CGRect(x: 0, y: (containerHeight - contentSize.height) / 2, width: contentSize.width, height: contentSize.height)
        case .rightBaseline:
            return CGRect(x: containerWidth - contentSize.width, y: (containerHeight - contentSize.height) / 2, width: contentSize.width, height: contentSize.height)
        case .topBaseline:
            return CGRect(x: (containerWidth - contentSize.width) / 2, y: 0, width: contentSize.width, height: contentSize.height)
        case let .custom(center):
            return CGRect(x: center.x - contentSize.width / 2, y: center.y - contentSize.height / 2, width: contentSize.width, height: contentSize.height)
        }
    }

    /// 将要弹出时添加背景按钮
    override public func presentationTransitionWillBegin() {
        /// 注册键盘通知
        if component.destination != .topBaseline, component.destination != .center {
            registerObservers()
        }
        /// 背景动画
        guard let containerView = containerView else { return }
        containerView.addSubview(backgroundView)
        guard let coordinator = presentedViewController.transitionCoordinator else {
            backgroundView.alpha = 1.0
            return
        }
        /// 动画
        coordinator.animate(alongsideTransition: { context in
            self.backgroundView.alpha = 1.0
        }, completion: nil)
    }

    /// 已经弹出视图
    override public func presentationTransitionDidEnd(_ completed: Bool) {
        if component.canPanDismiss {
            let panGuesture = UIPanGestureRecognizer(target: self, action: #selector(handlePanGesture(gesture:)))
            presentedViewController.view.addGestureRecognizer(panGuesture)
        }
    }

    override public func dismissalTransitionWillBegin() {
        /// 移除键盘通知
        removeObservers()
        /// 背景动画
        guard let coordinator = presentedViewController.transitionCoordinator else {
            backgroundView.alpha = 0.0
            return
        }
        coordinator.animate(alongsideTransition: { context in
            self.backgroundView.alpha = 0.0
        }, completion: nil)
    }

    override public func dismissalTransitionDidEnd(_ completed: Bool) {
        if completed {
            backgroundView.removeFromSuperview()
        }
    }
}

// MARK: -  Handle Gestures

extension PresentationController {
    @objc private func backgroundViewDidTapped() {
        presentedViewController.dismiss(animated: true, completion: nil)
    }

    @objc private func handlePanGesture(gesture: UIPanGestureRecognizer) {
        guard component.canPanDismiss else { return }
        let offset = gesture.translation(in: presentedView)
        let contentSize = presentedViewController.view.frame.size
        switch gesture.state {
        case .began:
            panStart = offset
        case .changed:
            var alpha: CGFloat = 1.0
            var offsetX: CGFloat = 0
            var offsetY: CGFloat = 0
            switch panDirection {
            case .up:
                offsetY = -min(contentSize.height, max(panStart.y - offset.y, 0))
                alpha = 1 - abs(offsetY / contentSize.height)
            case .down:
                offsetY = max(0, min(offset.y - panStart.y, contentSize.height))
                alpha = 1 - offsetY / contentSize.height
            case .left:
                offsetX = -min(contentSize.width, max(panStart.x - offset.x, 0))
                alpha = 1 - abs(offsetX / contentSize.width)
            case .right:
                offsetX = max(0, min(offset.x - panStart.x, contentSize.width))
                alpha = 1 - offsetX / contentSize.width
            }
            presentedViewController.view.transform = CGAffineTransform(translationX: offsetX, y: offsetY)
            backgroundView.alpha = alpha
        case .ended, .cancelled, .failed:
            var canDismiss = false
            switch panDirection {
            case .up:
                canDismiss = panStart.y - offset.y > min(contentSize.height / 2, 100)
            case .down:
                canDismiss = offset.y - panStart.y > min(contentSize.height / 2, 100)
            case .left:
                canDismiss = panStart.x - offset.x > min(contentSize.width / 2, 100)
            case .right:
                canDismiss = offset.x - panStart.x > min(contentSize.width / 2, 100)
            }
            if canDismiss {
                backgroundViewDidTapped()
            } else {
                UIView.animate(withDuration: 0.2, delay: 0, options: .curveLinear, animations: {
                    self.presentedViewController.view.transform = CGAffineTransform.identity
                    self.backgroundView.alpha = 1
                })
            }
        default:
            break
        }
    }
}

// MARK: -  Keyboard

extension PresentationController {
    private func registerObservers() {
        /// 注册键盘通知
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(notification:)), name: UIApplication.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillChangeFrame(notification:)), name: UIApplication.keyboardWillChangeFrameNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(notification:)), name: UIApplication.keyboardWillHideNotification, object: nil)
        /// 注册输入框通知
        NotificationCenter.default.addObserver(self, selector: #selector(textInputViewDidBeginEditing(notification:)), name: UITextField.textDidBeginEditingNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(textInputViewDidBeginEditing(notification:)), name: UITextView.textDidBeginEditingNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(textInputViewDidEndEditing(notification:)), name: UITextField.textDidEndEditingNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(textInputViewDidEndEditing(notification:)), name: UITextView.textDidEndEditingNotification, object: nil)
    }

    private func removeObservers() {
        /// 移除键盘通知
        NotificationCenter.default.removeObserver(self, name: UIApplication.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UIApplication.keyboardWillChangeFrameNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UIApplication.keyboardWillHideNotification, object: nil)
        /// 移除输入框通知
        NotificationCenter.default.removeObserver(self, name: UITextField.textDidBeginEditingNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UITextView.textDidBeginEditingNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UITextField.textDidEndEditingNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: UITextView.textDidEndEditingNotification, object: nil)
    }

    @objc private func keyboardWillShow(notification: Notification) {
        keyboardFrame = notification.keyboardEndFrame
        keyboardAnimationDuration = notification.keyboardAnimationDuration
        handleKeyboardAdjustAnimation()
    }

    @objc private func keyboardWillHide(notification: Notification) {
        keyboardFrame = notification.keyboardEndFrame
        keyboardAnimationDuration = notification.keyboardAnimationDuration
        handleKeyboardAdjustAnimation()
    }

    @objc private func keyboardWillChangeFrame(notification: Notification) {
        keyboardFrame = notification.keyboardEndFrame
        keyboardAnimationDuration = notification.keyboardAnimationDuration
        handleKeyboardAdjustAnimation()
    }

    @objc private func textInputViewDidBeginEditing(notification: NSNotification) {
        textInputView = notification.object as? UIView
        handleKeyboardAdjustAnimation()
    }

    @objc private func textInputViewDidEndEditing(notification: NSNotification) {
        let inputView = notification.object as? UIView
        if textInputView == inputView {
            textInputView = nil
        }
        handleKeyboardAdjustAnimation()
    }

    private func translateFrame(keyboardFrame: CGRect, presentedViewFrame: CGRect, inputViewFrame: CGRect) -> CGRect {
        var newFrame = presentedViewFrame
        let keyboardTop = UIScreen.main.bounds.height - keyboardFrame.size.height
        switch component.keyboardTranslationType {
        case let .unabgeschirmt(compress):
            let presentedBottom = presentedViewFrame.maxY + component.keyboardPadding
            let offset = presentedBottom - keyboardTop
            if compress || offset > 0 {
                newFrame.origin.y -= offset
            }
        case .compressInputView:
            let inputViewBottom = inputViewFrame.maxY + component.keyboardPadding
            let offset = inputViewBottom - keyboardTop
            newFrame.origin.y -= offset
        }
        return newFrame
    }

    private func handleKeyboardAdjustAnimation() {
        guard let keyboardFrame = keyboardFrame,
              let keyboardAnimationDuration = keyboardAnimationDuration else { return }
        if let textInputView = textInputView {
            let presentedViewFrame = frameOfPresentedViewInContainerView
            let inputViewFrame = textInputView.convert(textInputView.bounds, to: nil)
            let translatedFrame = translateFrame(keyboardFrame: keyboardFrame, presentedViewFrame: presentedViewFrame, inputViewFrame: inputViewFrame)
            if translatedFrame != presentedViewFrame {
                UIView.setAnimationBeginsFromCurrentState(true)
                UIView.animate(withDuration: keyboardAnimationDuration, animations: {
                    self.presentedView?.frame = translatedFrame
                })
            }
        } else {
            let presentedViewFrame = frameOfPresentedViewInContainerView
            if presentedView?.frame != presentedViewFrame {
                UIView.setAnimationBeginsFromCurrentState(true)
                UIView.animate(withDuration: keyboardAnimationDuration, animations: {
                    self.presentedView?.frame = presentedViewFrame
                })
            }
        }
    }
}
