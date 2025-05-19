//
//  UIColor+Rtt.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/26.
//

import Foundation
import YYCategories

extension UIColor {
    
    // 主文字颜色
    @objc static var show_main_text: UIColor {
        return UIColor(red: 1, green: 1, blue: 1, alpha: 1)
    }
    
    // 未选中字体颜色
    @objc static var show_beauty_deselect: UIColor {
        return UIColor(hex: "989DBA", alpha: 1)
    }
    
    // 空页面描述颜色
    @objc static var show_empty_desc: UIColor {
        return UIColor(hex: "989DBA", alpha: 1)
    }
    
    // slider
    @objc static var show_slider_tint: UIColor {
        return UIColor(hex: "FFFFFF", alpha: 0.7)
    }
    
    @objc static var show_zi01: UIColor {
        return UIColor(hex: "CE27FB", alpha: 1)
    }
    
    @objc static var show_zi02: UIColor {
        return UIColor(hex: "684BF2", alpha: 1)
    }
    
    // slider
    @objc static var show_zi03: UIColor {
        return UIColor(hex: "7A59FB", alpha: 1)
    }
    
    // 蒙版
    @objc static var show_cover: UIColor {
        return UIColor(hex: "0C0923", alpha: 0.3)
    }
    
    // 直播场景背景色
    @objc static var show_room_info_cover: UIColor {
        return UIColor(hex: "000000", alpha: 0.25)
    }
    
    // 音乐设置背景色
    @objc static var show_music_item_bg: UIColor {
        return UIColor(hex: "000000", alpha: 0.35)
    }
    
    // 背景色
    @objc static var show_dark_cover_bg: UIColor {
        return UIColor(hex: "151325", alpha: 0.85)
    }
    
    // 按钮背景
    @objc static var show_btn_bg: UIColor {
        return UIColor(hex: "345DFF", alpha: 1)
    }

    // 不推荐按钮背景
    @objc static var show_btn_bg_not_recommended: UIColor {
        return UIColor(hex: "EFF4FF", alpha: 1)
    }
    
    // 红点颜色
    @objc static var show_red_dot: UIColor {
        return UIColor(hex: "FF317B", alpha: 1)
    }
    
    // 红点颜色
    @objc static var show_chat_user_name: UIColor {
        return UIColor(hex: "A6C4FF", alpha: 1)
    }
    
    // 输入框背景
    @objc static var show_chat_input_bg: UIColor {
        return UIColor(hex: "F1F3F8", alpha: 1)
    }
    
    // 输入框文字
    @objc static var show_chat_input_text: UIColor {
        return UIColor(hex: "3C4267", alpha: 1)
    }
    
    // segment未选中的背景
    @objc static var show_segment_bg: UIColor {
        return UIColor(hex: "F2F2F5", alpha: 1)
    }
    
    // segment未选中的边框
    @objc static var show_segment_border: UIColor {
        return UIColor(hex: "ECECF0", alpha: 1)
    }
    
    // segment未选中的标题
    @objc static var show_segment_title_nor: UIColor {
        return UIColor(hex: "979CBB", alpha: 1)
    }
    
    @objc static var show_footer_separator: UIColor {
        return UIColor(hex: "F5F4F6", alpha: 1)
    }
    
    @objc static var show_Ellipse2: UIColor {
        return UIColor(hex: "E6E5F1", alpha: 1)
    }
    
    @objc static var show_Ellipse5: UIColor {
        return UIColor(hex: "6D7291", alpha: 1)
    }
    
    @objc static var show_Ellipse6: UIColor {
        return UIColor(hex: "303553", alpha: 1)
    }
    
    @objc static var show_Ellipse7: UIColor {
        return UIColor(hex: "040A25", alpha: 1)
    }
    
   
    @objc static var show_blue03: UIColor {
        return UIColor(hex: "1BA1FC", alpha: 1)
    }
    
    @objc static var show_preset_bg: UIColor {
        return UIColor(hex: "F0F9FF", alpha: 1)
    }
    
    @objc static var show_end_bg: UIColor {
        return UIColor(hex: "141650", alpha: 1)
    }
}

public extension UIColor {
    /// 便利构造Hex颜色
    ///
    /// - Parameters:
    ///   - string: hex值
    ///   - alpha: alpha值，默认1.0
    convenience init(hex string: String, alpha: CGFloat = 1.0) {
        var hex = string.hasPrefix("#") ? String(string.dropFirst()) : string
        guard hex.count == 3 || hex.count == 6 else {
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
            blue: CGFloat(Int(hex, radix: 16)! & 0xFF) / 255.0,
            alpha: alpha
        )
    }

    var randomColor: UIColor {
        UIColor(red: CGFloat(arc4random() % 256) / 255.0,
                green: CGFloat(arc4random() % 256) / 255.0,
                blue: CGFloat(arc4random() % 256) / 255.0,
                alpha: 1)
    }
}
