//
//  UIFont+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import Foundation

extension UIFont {
    
    @objc private static func show_regularFontSize(_ size: CGFloat) ->UIFont? {
        UIFont(name: "PingFangSC-Regular", size: size)
    }
    
    @objc private static func show_MediumFontSize(_ size: CGFloat) ->UIFont? {
        UIFont(name: "PingFangSC-Medium", size: size)
    }
    
    @objc private static func show_SemiboldFontSize(_ size: CGFloat) ->UIFont?{
        UIFont(name: "PingFangSC-Semibold", size: size)
    }
    
    @objc static var show_chat_user_name: UIFont? {
        UIFont(name: "PingFangSC-Semibold", size: 13)
    }
    
    @objc static var show_chat_msg: UIFont? {
        UIFont(name: "PingFangSC-Regular", size: 13)
    }
    
    // 中等
    @objc static var show_M_12: UIFont? {
        show_MediumFontSize(12)
    }
    
    @objc static var show_M_14: UIFont? {
        show_MediumFontSize(14)
    }
    
    @objc static var show_M_15: UIFont? {
        show_MediumFontSize(15)
    }
    
    // 常规
    @objc static var show_R_9: UIFont? {
        show_regularFontSize(9)
    }
    
    @objc static var show_R_10: UIFont? {
        show_regularFontSize(10)
    }
    
    @objc static var show_R_11: UIFont? {
        show_regularFontSize(11)
    }
    
    @objc static var show_R_12: UIFont? {
        show_regularFontSize(12)
    }
    
    @objc static var show_R_13: UIFont? {
        show_regularFontSize(13)
    }
    
    @objc static var show_R_14: UIFont? {
        show_regularFontSize(14)
    }
    
    @objc static var show_R_16: UIFont? {
        show_regularFontSize(16)
    }
    
    @objc static var show_S_18: UIFont? {
        show_SemiboldFontSize(18)
    }
    
    // 导航栏标题
    @objc static var show_navi_title: UIFont? {
        show_SemiboldFontSize(16)
    }
    // 按钮字体
    @objc static var show_btn_title: UIFont? {
        show_SemiboldFontSize(16)
    }
    
    
}
