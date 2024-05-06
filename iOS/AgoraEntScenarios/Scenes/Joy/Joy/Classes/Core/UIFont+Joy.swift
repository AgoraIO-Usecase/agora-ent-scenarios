//
//  UIFont+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import Foundation

extension UIFont {
    
    @objc private static func joy_regularFontSize(_ size: CGFloat) ->UIFont? {
        UIFont(name: "PingFangSC-Regular", size: size)
    }
    
    @objc private static func joy_MediumFontSize(_ size: CGFloat) ->UIFont? {
        UIFont(name: "PingFangSC-Medium", size: size)
    }
    
    @objc private static func joy_SemiboldFontSize(_ size: CGFloat) ->UIFont?{
        UIFont(name: "PingFangSC-Semibold", size: size)
    }
    
    @objc static var joy_chat_user_name: UIFont? {
        UIFont(name: "PingFangSC-Semibold", size: 13)
    }
    
    @objc static var joy_chat_msg: UIFont? {
        UIFont(name: "PingFangSC-Regular", size: 13)
    }
    
    // 中等
    @objc static var joy_M_10: UIFont? {
        joy_MediumFontSize(10)
    }
    
    @objc static var joy_M_12: UIFont? {
        joy_MediumFontSize(12)
    }
    
    @objc static var joy_M_14: UIFont? {
        joy_MediumFontSize(14)
    }
    
    @objc static var joy_M_15: UIFont? {
        joy_MediumFontSize(15)
    }
    
    @objc static var joy_M_17: UIFont? {
        joy_MediumFontSize(17)
    }
    
    // 常规
    @objc static var joy_R_9: UIFont? {
        joy_regularFontSize(9)
    }
    
    @objc static var joy_R_10: UIFont? {
        joy_regularFontSize(10)
    }
    
    @objc static var joy_R_11: UIFont? {
        joy_regularFontSize(11)
    }
    
    @objc static var joy_R_12: UIFont? {
        joy_regularFontSize(12)
    }
    
    @objc static var joy_R_13: UIFont? {
        joy_regularFontSize(13)
    }
    
    @objc static var joy_R_14: UIFont? {
        joy_regularFontSize(14)
    }
    
    @objc static var joy_R_16: UIFont? {
        joy_regularFontSize(16)
    }
    
    @objc static var joy_S_16: UIFont? {
        joy_SemiboldFontSize(16)
    }
    
    @objc static var joy_S_18: UIFont? {
        joy_SemiboldFontSize(18)
    }
    
    // 导航栏标题
    @objc static var joy_navi_title: UIFont? {
        joy_SemiboldFontSize(16)
    }
    // 按钮字体
    @objc static var joy_btn_title: UIFont? {
        joy_SemiboldFontSize(16)
    }
    
    
}
