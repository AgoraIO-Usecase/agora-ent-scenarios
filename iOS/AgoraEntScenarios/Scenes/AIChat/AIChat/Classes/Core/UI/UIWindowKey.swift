//
//  UIWindowKey.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/30.
//

import UIKit
import Foundation

struct AIChatWrapper<Base> {
    var base: Base
    init(_ base: Base) {
        self.base = base
    }
}

extension UIApplication {
    var chat: AIChatWrapper<UIApplication> {
        return AIChatWrapper.init(self)
    }
}


extension AIChatWrapper where Base == UIApplication {
    
    /// KeyWindow property
    /// How to use?
    /// `UIApplication.shared.chat.keyWindow`
    var keyWindow: UIWindow? {
        (base.connectedScenes
         // Keep only active scenes, onscreen and visible to the user
            .filter { $0.activationState == .foregroundActive }
         // Keep only the first `UIWindowScene`
            .first(where: { $0 is UIWindowScene })
         // Get its associated windows
            .flatMap({ $0 as? UIWindowScene })?.windows
         // Finally, keep only the key window
            .first(where: \.isKeyWindow))
    }
}



