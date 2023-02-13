//
//  UIColor+Extension.swift
//  BreakoutRoom
//
//  Created by zhaoyongqiang on 2021/10/29.
//

import UIKit

extension UIColor {
    static var blueColor: UIColor {
        .init(hex: "#2397FE") ?? .white
    }
}

public extension UIColor {
    /// 便利构造Hex颜色
    ///
    /// - Parameters:
    ///   - string: hex值
    ///   - alpha: alpha值，默认1.0
    convenience init?(hex string: String, alpha: CGFloat = 1.0) {
        let r, g, b, a: CGFloat
        
        if string.hasPrefix("#") {
            let start = string.index(string.startIndex, offsetBy: 1)
            let hexColor = String(string[start...])
            
            if hexColor.count == 8 {
                let scanner = Scanner(string: hexColor)
                var hexNumber: UInt64 = 0
                
                // #ffe700ff  分别代表 red, green, blue 以及 alpha
                if scanner.scanHexInt64(&hexNumber) {
                    r = CGFloat((hexNumber & 0xff000000) >> 24) / 255
                    g = CGFloat((hexNumber & 0x00ff0000) >> 16) / 255
                    b = CGFloat((hexNumber & 0x0000ff00) >> 8) / 255
                    a = CGFloat(hexNumber & 0x000000ff) / 255
                    
                    self.init(red: r, green: g, blue: b, alpha: a)
                    return
                }
            }
        }
        
        return nil
    }
    
    var randomColor: UIColor {
        UIColor(red: CGFloat(arc4random() % 256) / 255.0,
                green: CGFloat(arc4random() % 256) / 255.0,
                blue: CGFloat(arc4random() % 256) / 255.0,
                alpha: 1)
    }
}
