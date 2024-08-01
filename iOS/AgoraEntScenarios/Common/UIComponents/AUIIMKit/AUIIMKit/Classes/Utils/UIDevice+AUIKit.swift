//
//  UIDevice+AUIKit.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/26.
//

import UIKit

public func getWindow()-> UIWindow? {
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

extension UIDevice {
    public var aui_SafeDistanceTop: CGFloat {
        let window = getWindow()
        
        return window?.safeAreaInsets.top ?? 0
    }
    
    public var aui_SafeDistanceBottom: CGFloat {
        let window = getWindow()
        
        return window?.safeAreaInsets.bottom ?? 0
    }
}
