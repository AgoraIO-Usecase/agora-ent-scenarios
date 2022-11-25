//
//  ShowPresettingModel.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import Foundation

enum ShowMode {
    case signle // 单主播模式
    case pk // pk模式
}

enum ShowPresetType {
    case show_low       // 秀场低端
    case show_medium    // 秀场终端
    case show_high      // 秀场高端
    
    var title: String {
        switch self {
        case .show_low:
            return "低端机"
        case .show_medium:
            return "中端机"
        case .show_high:
            return "高端机"
        }
    }
    
    var iosInfo: String {
        switch self {
        case .show_low:
            return "iPhone6 及以下"
        case .show_medium:
            return "iPhone6~iPhoneX"
        case .show_high:
            return "iPhoneX 及以上"
        }
    }
}

class ShowPresettingModel {
    var title: String
    var desc: String
    var icon: String
    var optionsArray: [ShowPresetType]
    
    init(title: String, desc: String, icon: String, optionsArray: [ShowPresetType]) {
        self.title = title
        self.desc = desc
        self.icon = icon
        self.optionsArray = optionsArray
    }
}
