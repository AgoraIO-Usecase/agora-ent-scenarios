//
//  AlertManager.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/10.
//

import UIKit
import AVFoundation

public let cl_screenWidht = UIScreen.main.bounds.width
public let cl_screenHeight = UIScreen.main.bounds.height
class AUIAlertManager: NSObject {
    private struct AlertViewCache {
        var view: UIView?
        var index: Int = 0
    }
    enum AlertPosition {
        case center
        case bottom
    }
    
    private static var vc: UIViewController?
    private static var containerView: UIView?
    private static var currentPosition: AlertPosition = .center
    private static var viewCache: [AlertViewCache] = []
    private static var bottomAnchor: NSLayoutConstraint?
    
    public static func show(view: UIView,
                            fromVC: UIViewController? = nil,
                            alertPostion: AlertPosition = .center,
                            didCoverDismiss: Bool = true) {
        let index = viewCache.isEmpty ? 0 : viewCache.count
        viewCache.append(AlertViewCache(view: view, index: index))
        currentPosition = alertPostion
        if vc == nil {
            containerView = UIButton(frame: CGRect(x: 0, y: 0, width: cl_screenWidht, height: cl_screenHeight))
            containerView?.backgroundColor = UIColor(red: 0.0/255, green: 0.0/255, blue: 0.0/255, alpha: 0.0)
        }
        if didCoverDismiss {
            (containerView as? UIButton)?.addTarget(self, action: #selector(tapView), for: .touchUpInside)
        }
        guard let containerView = containerView else { return }
        containerView.addSubview(view)
        view.translatesAutoresizingMaskIntoConstraints = false
        view.alpha = 0
        if alertPostion == .center {
            view.centerXAnchor.constraint(equalTo: containerView.centerXAnchor).isActive = true
            view.centerYAnchor.constraint(equalTo: containerView.centerYAnchor).isActive = true
        }else{
            bottomAnchor = view.bottomAnchor.constraint(equalTo: containerView.bottomAnchor)
            view.leadingAnchor.constraint(equalTo: containerView.leadingAnchor).isActive = true
            view.trailingAnchor.constraint(equalTo: containerView.trailingAnchor).isActive = true
        }
        let parentVC = fromVC ?? UIViewController.cl_topViewController()
        if vc == nil {
            vc = UIViewController()
            vc?.view.layer.contents = nil
            vc?.view.backgroundColor = UIColor.clear
            vc?.view.addSubview(containerView)
            vc?.modalPresentationStyle = .custom
            parentVC?.present(vc!, animated: false) {
                showAlertPostion(alertPostion: alertPostion, view: view)
            }
        } else {
            vc?.view.addSubview(containerView)
            if let _ = vc?.view.superview {
                showAlertPostion(alertPostion: alertPostion, view: view)
            }else{
                parentVC?.present(vc!, animated: false) {
                    showAlertPostion(alertPostion: alertPostion, view: view)
                }
            }
        }
        //注册键盘出现通知
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(keyboardWillShow(notification:)),
                                               name: UIApplication.keyboardWillShowNotification,
                                               object: nil)
        
        //注册键盘隐藏通知
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(keyboardWillHide(notification:)),
                                               name: UIApplication.keyboardWillHideNotification,
                                               object: nil)
    }

    private static func showAlertPostion(alertPostion: AlertPosition, view: UIView) {
        containerView?.layoutIfNeeded()
        if alertPostion == .center {
            showCenterView(view: view)
        }else{
            bottomAnchor?.constant = view.frame.height
            bottomAnchor?.isActive = true
            containerView?.layoutIfNeeded()
            showBottomView(view: view)
        }
    }
    
    private static func showCenterView(view: UIView){
        if !viewCache.isEmpty {
            viewCache.forEach({ $0.view?.alpha = 0 })
        }
        UIView.animate(withDuration: 0.25, animations: {
            containerView?.backgroundColor = UIColor(red: 0.0/255,
                                                     green: 0.0/255,
                                                     blue: 0.0/255,
                                                     alpha: 0.5)
            view.alpha = 1.0
        })
    }
    
    private static func showBottomView(view: UIView){
        if !viewCache.isEmpty {
            viewCache.forEach({ $0.view?.alpha = 0 })
        }
        view.alpha = 1.0
        bottomAnchor?.constant = 0
        bottomAnchor?.isActive = true
        UIView.animate(withDuration: 0.25, animations: {
            containerView?.backgroundColor = UIColor(red: 0.0/255,
                                                    green: 0.0/255,
                                                    blue: 0.0/255,
                                                    alpha: 0.5)
            containerView?.superview?.layoutIfNeeded()
        })
    }

    static func updateViewHeight() {
        UIView.animate(withDuration: 0.25, animations: {
            containerView?.layoutIfNeeded()
        })
    }
    
    static func hiddenView(all: Bool = true, completion: (() -> Void)? = nil){
        if vc == nil {
            completion?()
            return
        }
        if currentPosition == .bottom {
            guard let lastView = viewCache.last?.view else { return }
            bottomAnchor?.constant = lastView.frame.height
            bottomAnchor?.isActive = true
        }
        UIView.animate(withDuration: 0.25, animations: {
            if all || viewCache.isEmpty {
                containerView?.backgroundColor = UIColor(red: 255.0/255,
                                                         green: 255.0/255,
                                                         blue: 255.0/255,
                                                         alpha: 0.0)
                containerView?.layoutIfNeeded()
            }
            if currentPosition == .center {
                viewCache.last?.view?.alpha = 0
            }
        }, completion: { (_) in
            if all || viewCache.isEmpty {
                viewCache.removeAll()
                vc?.dismiss(animated: false, completion: completion)
                vc = nil
            } else {
                viewCache.removeLast()
                viewCache.last?.view?.alpha = 1
            }
        })
    }
    
    @objc
    private static func tapView(){
        DispatchQueue.main.asyncAfter(deadline: DispatchTime(uptimeNanoseconds: UInt64(0.1))) {
            self.hiddenView()
        }
    }
    
    private static var originFrame:CGRect = .zero
    // 键盘显示
    @objc private static func keyboardWillShow(notification: Notification) {
        let keyboardHeight = (notification.userInfo?["UIKeyboardBoundsUserInfoKey"] as? CGRect)?.height
        let y = cl_screenHeight - (keyboardHeight ?? 304) - containerView!.frame.height
        if originFrame.origin.y != y {
            originFrame = containerView!.frame
        }
        UIView.animate(withDuration: 0.25) {
            containerView?.frame.origin.y = y * 0.5
        }
    }
    // 键盘隐藏
    @objc private static func keyboardWillHide(notification: Notification) {
        UIView.animate(withDuration: 0.25) {
            containerView?.frame = originFrame
        } completion: { _ in
            if currentPosition == .bottom {
                hiddenView()
            }
        }
    }
}

extension UIViewController {
    static var keyWindow: UIWindow? {
        if #available(iOS 13.0, *) {
            // Get connected scenes
            return UIApplication.shared.connectedScenes
            // Keep only active scenes, onscreen and visible to the user
                .filter { $0.activationState == .foregroundActive }
            // Keep only the first `UIWindowScene`
                .first(where: { $0 is UIWindowScene })
            // Get its associated windows
                .flatMap({ $0 as? UIWindowScene })?.windows
            // Finally, keep only the key window
                .first(where: \.isKeyWindow)
        }else {
            return UIApplication.shared.keyWindow
        }
    }
    static func cl_topViewController(_ viewController: UIViewController? = nil) -> UIViewController? {
        let viewController = viewController ?? keyWindow?.rootViewController
        
        if let navigationController = viewController as? UINavigationController,
            !navigationController.viewControllers.isEmpty
        {
            return self.cl_topViewController(navigationController.viewControllers.last)
            
        } else if let tabBarController = viewController as? UITabBarController,
            let selectedController = tabBarController.selectedViewController
        {
            return self.cl_topViewController(selectedController)
            
        } else if let presentedController = viewController?.presentedViewController {
            return self.cl_topViewController(presentedController)
            
        }
        return viewController
    }
}
