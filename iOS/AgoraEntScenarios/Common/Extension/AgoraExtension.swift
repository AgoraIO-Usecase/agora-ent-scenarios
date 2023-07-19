//
//  AgoraCode.swift
//  OpenLive
//
//  Created by CavanSu on 2019/9/16.
//  Copyright © 2019 Agora. All rights reserved.
//

import AgoraRtcKit
import UIKit

extension UIApplication {
    static var kWindow: UIWindow? {
        // Get connected scenes
        if #available(iOS 13.0, *) {
            return UIApplication.shared.connectedScenes
            // Keep only active scenes, onscreen and visible to the user
                .filter { $0.activationState == .foregroundActive }
            // Keep only the first `UIWindowScene`
                .first(where: { $0 is UIWindowScene })
            // Get its associated windows
                .flatMap({ $0 as? UIWindowScene })?.windows
            // Finally, keep only the key window
                .first(where: \.isKeyWindow)
        } else {
            return UIApplication.shared.keyWindow
        }
    }

    /// The top most view controller
    static var topMostViewController: UIViewController? {
        kWindow?.rootViewController?.visibleViewController
    }
}

extension UIViewController {
    /// The visible view controller from a given view controller
    var visibleViewController: UIViewController? {
        if let navigationController = self as? UINavigationController {
            return navigationController.topViewController?.visibleViewController
        } else if let tabBarController = self as? UITabBarController {
            return tabBarController.selectedViewController?.visibleViewController
        } else if let presentedViewController = presentedViewController {
            return presentedViewController.visibleViewController
        } else {
            return self
        }
    }
}

extension OutputStream {
    /// Write `String` to `OutputStream`
    ///
    /// - parameter string:                The `String` to write.
    /// - parameter encoding:              The `String.Encoding` to use when writing the string. This will default to `.utf8`.
    /// - parameter allowLossyConversion:  Whether to permit lossy conversion when writing the string. Defaults to `false`.
    ///
    /// - returns:                         Return total number of bytes written upon success. Return `-1` upon failure.

    func write(_ string: String, encoding: String.Encoding = .utf8, allowLossyConversion: Bool = false) -> Int {
        if let data = string.data(using: encoding, allowLossyConversion: allowLossyConversion) {
            let ret = data.withUnsafeBytes {
                write($0, maxLength: data.count)
            }
            if ret < 0 {
                print("write fail: \(streamError.debugDescription)")
            }
        }

        return -1
    }
}

extension Date {
    func getFormattedDate(format: String) -> String {
        let dateformat = DateFormatter()
        dateformat.dateFormat = format
        return dateformat.string(from: self)
    }
}

extension UIView {
    @discardableResult
    func setGradient(_ colors: [UIColor], _ points: [CGPoint]) -> Self {
        let gradientColors: [CGColor] = colors.map { $0.cgColor }
        let startPoint = points[0]
        let endPoint = points[1]
        let gradientLayer = CAGradientLayer().colors(gradientColors).startPoint(startPoint).endPoint(endPoint).frame(bounds).backgroundColor(UIColor.clear.cgColor)
        layer.insertSublayer(gradientLayer, at: 0)
        return self
    }
}
