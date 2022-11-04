//
//  UIColor+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import Foundation

extension UIColor {
    
    // 主文字颜色
    @objc static var show_main_text: UIColor {
        return UIColor(red: 1, green: 1, blue: 1, alpha: 1)
    }
    
    // 未选中字体颜色
    @objc static var show_beauty_deselect: UIColor {
        return UIColor.HexColor(hex: 0x989DBA, alpha: 1)
    }
    
    // 空页面描述颜色
    @objc static var show_empty_desc: UIColor {
        return UIColor.HexColor(hex: 0x989DBA, alpha: 1)
    }
    
    // slider
    @objc static var show_slider_tint: UIColor {
        return UIColor.HexColor(hex: 0xFFFFFF, alpha: 0.7)
    }
    
    // slider
    @objc static var show_zi03: UIColor {
        return UIColor.HexColor(hex: 0x7A59FB, alpha: 1)
    }
    
    // 蒙版
    @objc static var show_cover: UIColor {
        return UIColor.HexColor(hex: 0x0C0923, alpha: 0.3)
    }
    
    // 背景色
    @objc static var show_beauty_setting_bg: UIColor {
        return UIColor.HexColor(hex: 0x151325, alpha: 0.85)
    }
    
    // 按钮背景
    @objc static var show_btn_bg: UIColor {
        return UIColor.HexColor(hex: 0x345DFF, alpha: 1)
    }
}
