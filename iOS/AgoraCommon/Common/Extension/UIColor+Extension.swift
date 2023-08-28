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
        
        var hex = string.hasPrefix("#") ? String(string.dropFirst()) : string
        hex = hex.hasPrefix("0x") ? String(hex.dropFirst(2)) : hex
        guard hex.count == 3 || hex.count == 6  else {
            self.init(white: 1.0, alpha: 0.0)
            return
        }
        
        if hex.count == 3 {
            for (indec, char) in hex.enumerated() {
                hex.insert(char, at: hex.index(hex.startIndex, offsetBy: indec * 2))
            }
        }
        
        self.init(
            red: CGFloat((Int(hex, radix: 16)! >> 16) & 0xFF) / 255.0,
            green: CGFloat((Int(hex, radix: 16)! >> 8) & 0xFF) / 255.0,
            blue: CGFloat((Int(hex, radix: 16)!) & 0xFF) / 255.0,
            alpha: alpha
        )
    }
    
    convenience init(hex value: Int32, alpha: CGFloat = 1.0) {
        self.init(red: CGFloat((value >> 16) & 0xff) / 255.0, green: CGFloat((value >> 8) & 0xff) / 255.0, blue: CGFloat(value & 0xff) / 255.0, alpha: alpha)
    }
    
    var randomColor: UIColor {
        UIColor(red: CGFloat(arc4random() % 256) / 255.0,
                green: CGFloat(arc4random() % 256) / 255.0,
                blue: CGFloat(arc4random() % 256) / 255.0,
                alpha: 1)
    }
}
