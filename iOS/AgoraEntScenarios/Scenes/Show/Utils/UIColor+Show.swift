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
    
    // 空页面描述颜色
    @objc static var show_empty_desc: UIColor {
        return UIColor.HexColor(hex: 0x989DBA, alpha: 1)
    }
    
    // 蒙版
    @objc static var show_cover: UIColor {
        return UIColor.HexColor(hex: 0x0C0923, alpha: 0.3)
    }
    
    // 按钮背景
    @objc static var show_btn_bg: UIColor {
        return UIColor.HexColor(hex: 0x345DFF, alpha: 1)
    }
}
