//
//  UIColor+AUIKit.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/13.
//

import UIKit

extension UIColor {
    public static var aui_primary: UIColor {
        return UIColor(hex: "#7800ff")
    }
    
    public static var aui_primary35: UIColor {
        return UIColor(hex: "#7800ff", alpha: 0.35)
    }
    
    public static var aui_black: UIColor {
        return UIColor(hex: "#1b1067")
    }
    
    public static var aui_black90: UIColor {
        return UIColor(hex: "#1b1067", alpha: 0.9)
    }
    
    public static var aui_navy: UIColor {
        return UIColor(hex: "#4130c7")
    }
    
    public static var aui_danger: UIColor {
        return UIColor(hex: "#fa5151")
    }
    
    public static var aui_grey: UIColor {
        return UIColor(hex: "#9599ef")
    }
    
    public static var aui_blue: UIColor {
        return UIColor(hex: "#545cff")
    }
    
    public static var aui_lightGrey: UIColor {
        return UIColor(hex: "#c3c5fe")
    }
    
    public static var aui_lightGrey35: UIColor {
        return UIColor(hex: "#c3c5fe", alpha: 0.35)
    }
    
    public static var aui_normalTextColor: UIColor {
        return .white
    }
    
    public static var aui_normalTextColor50: UIColor {
        return .white.withAlphaComponent(0.5)
    }
    
    public static var aui_normalTextColor35: UIColor {
        return .white.withAlphaComponent(0.35)
    }
}
