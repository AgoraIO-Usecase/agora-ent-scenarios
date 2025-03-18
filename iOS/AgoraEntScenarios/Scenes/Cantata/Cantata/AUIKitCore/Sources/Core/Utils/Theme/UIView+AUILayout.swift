//
//  UIView+AUILayout.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/26.
//

import Foundation
import SwiftTheme

@objc public class AUIEdges: NSObject {
    var top: CGFloat = 0.0
    var left: CGFloat = 0.0
    var bottom: CGFloat = 0.0
    var right: CGFloat = 0.0
    
    public override init() {
        
    }
    
}

public func aui_getThemePicker(
    _ object : NSObject,
    _ selector : String
) -> ThemePicker? {
    return ThemePicker.getThemePicker(object, selector)
}

public func aui_setThemePicker(
    _ object : NSObject,
    _ selector : String,
    _ picker : ThemePicker?
) {
    return ThemePicker.setThemePicker(object, selector, picker)
}

@objc public extension UIView {
    
    var theme_top: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_top:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_top:", newValue) }
    }
    
    var theme_bottom: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_bottom:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_bottom:", newValue) }
    }
    
    var theme_left: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_left:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_left:", newValue) }
    }
    
    var theme_right: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_right:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_right:", newValue) }
    }
    
    var theme_width: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_width:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_width:", newValue) }
    }

    var theme_height: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_height:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_height:", newValue) }
    }
    
    var theme_centerX: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_centerX:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_centerX:", newValue) }
    }

    var theme_centerY: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_centerY:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_centerY:", newValue) }
    }
    
    var theme_edges: AUIEdges {
        get {
            let edges = AUIEdges()
            edges.top = 0
            edges.left = 0
            edges.bottom = 0
            edges.right = 0
            return edges
        }
        set {
            theme_top = ThemeCGFloatPicker(keyPath: "\(newValue.top)")
            theme_left = ThemeCGFloatPicker(keyPath: "\(newValue.left)")
            theme_bottom = ThemeCGFloatPicker(keyPath: "\(newValue.bottom)")
            theme_right = ThemeCGFloatPicker(keyPath: "\(newValue.right)")
        }
    }
    
    
}

@objc public extension CALayer {
    var theme_cornerRadius: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setCornerRadius:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setCornerRadius:", newValue) }
    }
}
