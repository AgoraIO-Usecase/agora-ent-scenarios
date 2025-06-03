//
//  NotificationExtension.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation

public extension Notification {
    
    var a: AUIKitSwiftLib<Self> {
        AUIKitSwiftLib.init(self)
    }
    
}

public extension AUIKitSwiftLib where Base == Notification {
    
    
    /// Description keyboardEndFrame
    var keyboardEndFrame: CGRect? {
        return (base.userInfo?[UIApplication.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue
    }

    
    /// Description keyboard animation duration
    var keyboardAnimationDuration: TimeInterval? {
        return (base.userInfo?[UIApplication.keyboardAnimationDurationUserInfoKey] as? NSNumber)?.doubleValue
    }
}
