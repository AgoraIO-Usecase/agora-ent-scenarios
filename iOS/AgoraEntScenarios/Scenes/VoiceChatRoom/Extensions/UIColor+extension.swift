//
//  UIColor+extension.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/10/13.
//

import Foundation
import UIKit

extension UIColor {
    static func HexColor(hex: integer_t, alpha: CGFloat) -> UIColor {
        return UIColor(red: CGFloat((hex >> 16) & 0xff) / 255.0, green: CGFloat((hex >> 8) & 0xff) / 255.0, blue: CGFloat(hex & 0xff) / 255.0, alpha: alpha)
    }
}
