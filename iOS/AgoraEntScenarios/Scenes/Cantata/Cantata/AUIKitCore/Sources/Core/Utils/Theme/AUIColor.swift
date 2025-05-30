//
//  AUIColor.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/25.
//

import SwiftTheme

public func AUIColor(_ keyPath: String)-> ThemeColorPicker {
    ThemeColorPicker(v: {
        if let value = ThemeManager.value(for: keyPath) as? String, !value.contains("#") {
            return ThemeManager.color(for: value)
        }
        
        return ThemeManager.color(for: keyPath)
    })
}

public func AUICGColor(_ keyPath: String)-> ThemeCGColorPicker {
    ThemeCGColorPicker(v: {
        if let value = ThemeManager.value(for: keyPath) as? String, !value.contains("#") {
            return ThemeManager.color(for: value)?.cgColor
        }
        
        return ThemeManager.color(for: keyPath)?.cgColor
    })
}

public func AUIGradientColor(_ keyPath: String) -> ThemeAnyPicker {
    ThemeAnyPicker(keyPath: keyPath, map: { val in
        guard let array = val as? [String] else {
            print("SwiftTheme WARNING: Not found string key path: \(keyPath)")
            return []
        }
        var colors: [CGColor] = []
        array.forEach { hex in
            let color = UIColor(rgba: hex)
            colors.append(color.cgColor)
        }
        print("SwiftTheme array: \(array) \(keyPath)")
        return colors
    })
}

