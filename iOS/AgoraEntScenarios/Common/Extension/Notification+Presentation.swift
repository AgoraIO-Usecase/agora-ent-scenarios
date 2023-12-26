//
//  VoiceRoomAlertViewController.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import Foundation

public extension Notification {
    /// 键盘frame
    var keyboardEndFrame: CGRect? {
        return (userInfo?[UIApplication.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue
    }

    /// 键盘动画时间
    var keyboardAnimationDuration: TimeInterval? {
        return (userInfo?[UIApplication.keyboardAnimationDurationUserInfoKey] as? NSNumber)?.doubleValue
    }
}
