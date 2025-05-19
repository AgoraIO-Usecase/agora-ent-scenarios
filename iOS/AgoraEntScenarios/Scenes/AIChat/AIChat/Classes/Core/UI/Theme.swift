//
//  Theme.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import Foundation

/// Contain light and dark themes of the ease chat UIKit.If you want to switch custom theme,you'll modify primary&secondary&error&neutral&neutralSpecial hues properties in ``Appearance``.Then call `Theme.switchTheme(style: .custom)` method
@objc public enum ThemeStyle: UInt {
    case light
    case dark
    case custom
}

/// When the system theme changes, you can use this static method to switch between the light and dark themes of the ease chat UIKit.
@objc public protocol ThemeSwitchProtocol: NSObjectProtocol {
    
    /// When some view Implement the protocol method,you can use `Theme.switchTheme(style: .dark)` to switch theme.
    /// - Parameter style: ``ThemeStyle``
    func switchTheme(style: ThemeStyle)
}


/// The theme switching class is used for users to switch themes or register some user's own views that comply with the ThemeSwitchProtocol protocol.
/// How to use?
/// A view conform ThemeSwitchProtocol.Then Implement protocol methods.When you want switch theme that light and dark themes provided by default chatroom UIKit  `theme` .
/// Call `Theme.switchTheme(style: .dark)` method
/// A view conform ThemeSwitchProtocol.Then Implement protocol methods.When you want switch themes provided by customer .
@objcMembers open class Theme: NSObject {
    
    public static var style: ThemeStyle = .light
    
    private static var registerViews :NSHashTable<ThemeSwitchProtocol> = NSHashTable<ThemeSwitchProtocol>.weakObjects()
    
    /// Register some user's own views that Implement with the ThemeSwitchProtocol protocol.
    /// - Parameter view: The view conform ThemeSwitchProtocol.
    /// How to use?
    /// `Theme.registerSwitchThemeViews(view: Some view implement ThemeSwitchProtocol)`
    public static func registerSwitchThemeViews(view: ThemeSwitchProtocol) {
        if self.registerViews.contains(view) {
            return
        }
        self.registerViews.add(view)
    }
    
    /// Clean register views.
    public static func unregisterSwitchThemeViews() {
        self.registerViews.removeAllObjects()
    }
    
    /// The method
    /// - Parameter style: ThemeStyle
    /// How to use?
    /// `Theme.switchTheme(style: .dark)`
    @MainActor public static func switchTheme(style: ThemeStyle) {
        self.style = style
        if style == .custom {
            UIColor.ColorTheme.switchHues(hues: [203/360.0,155/360.0,350/360.0,203/360.0,220/360.0])
        }
        for view in self.registerViews.allObjects {
            if view.conforms(to: ThemeSwitchProtocol.self) {
                view.switchTheme(style: style)
            }
        }
    }
    
        
}

extension Unmanaged where Instance : ThemeSwitchProtocol&UIView {
    
}

