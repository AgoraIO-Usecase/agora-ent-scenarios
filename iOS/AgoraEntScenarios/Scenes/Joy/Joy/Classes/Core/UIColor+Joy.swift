//
//  UIColor+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import Foundation
import YYCategories

extension UIColor {
    
    // 主文字颜色
    @objc static var joy_main_text: UIColor {
        return .white
    }
    
    // 未选中字体颜色
    @objc static var joy_beauty_deselect: UIColor {
        return UIColor(hexString: "989DBA")!
    }
    
    // 空页面描述颜色
    @objc static var joy_empty_desc: UIColor {
        return UIColor(hexString: "989DBA")!
    }
    
    // slider
    @objc static var joy_slider_tint: UIColor {
        return UIColor(hexString: "FFFFFF")!.withAlphaComponent(0.7)
    }
    
    @objc static var joy_zi01: UIColor {
        return UIColor(hexString: "CE27FB")!
    }
    
    @objc static var joy_zi02: UIColor {
        return UIColor(hexString: "684BF2")!
    }
    
    // slider
    @objc static var joy_zi03: UIColor {
        return UIColor(hexString: "7A59FB")!
    }
    
    // 蒙版
    @objc static var joy_cover: UIColor {
        return UIColor(hexString: "0C0923")!.withAlphaComponent(0.3)
    }
    
    // 直播场景背景色
    @objc static var joy_room_info_cover: UIColor {
        return UIColor(hexString: "08062f")!.withAlphaComponent(0.3)
    }
    
    // 背景色
    @objc static var joy_dark_cover_bg: UIColor {
        return UIColor(hexString: "151325")!.withAlphaComponent(0.85)
    }
    
    // 按钮背景
    @objc static var joy_btn_bg: UIColor {
        return UIColor(hexString: "2F70FF")!
    }

    // 不推荐按钮背景
    @objc static var joy_btn_bg_not_recommended: UIColor {
        return UIColor(hexString: "EFF4FF")!
    }
    
    // 红点颜色
    @objc static var joy_red_dot: UIColor {
        return UIColor(hexString: "FF317B")!
    }
    
    // 红点颜色
    @objc static var joy_chat_user_name: UIColor {
        return UIColor(hexString: "A6C4FF")!
    }
    
    // 输入框背景
    @objc static var joy_chat_input_bg: UIColor {
        return UIColor(hexString: "F1F3F8")!
    }
    
    // 输入框文字
    @objc static var joy_chat_input_text: UIColor {
        return UIColor(hexString: "3C4267")!
    }
    
    // segment未选中的背景
    @objc static var joy_segment_bg: UIColor {
        return UIColor(hexString: "F2F2F5")!
    }
    
    // segment未选中的边框
    @objc static var joy_segment_border: UIColor {
        return UIColor(hexString: "ECECF0")!
    }
    
    // segment未选中的标题
    @objc static var joy_segment_title_nor: UIColor {
        return UIColor(hexString: "979CBB")!
    }
    
    @objc static var joy_footer_separator: UIColor {
        return UIColor(hexString: "F5F4F6")!
    }
    
    @objc static var joy_Ellipse2: UIColor {
        return UIColor(hexString: "E6E5F1")!
    }
    
    @objc static var joy_Ellipse5: UIColor {
        return UIColor(hexString: "6D7291")!
    }
    
    @objc static var joy_Ellipse6: UIColor {
        return UIColor(hexString: "303553")!
    }
    
    @objc static var joy_Ellipse7: UIColor {
        return UIColor(hexString: "040A25")!
    }
    
   
    @objc static var joy_blue03: UIColor {
        return UIColor(hexString: "1BA1FC")!
    }
    
    @objc static var joy_preset_bg: UIColor {
        return UIColor(hexString: "F0F9FF")!
    }
    
    @objc static var joy_end_bg: UIColor {
        return UIColor(hexString: "141650")!
    }
}
