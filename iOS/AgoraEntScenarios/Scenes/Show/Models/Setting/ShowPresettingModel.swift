//
//  ShowPresettingModel.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import Foundation

enum ShowPresetStandardType {
    case douyin
    case kuaishou
    
    var image: UIImage? {
        switch self {
        case .douyin:
            return UIImage.show_sceneImage(name: "show_preset_douyin")
        case .kuaishou:
            return UIImage.show_sceneImage(name: "show_preset_kuaishou")
        }
    }
}


enum ShowMode {
    case single // 单主播模式
    case pk // pk模式
}

enum ShowPresetType: Int {
    case unknown
    case show_low   // 秀场低端
    case show_medium    // 秀场终端
    case show_high      // 秀场高端
    
    var title: String {
        switch self {
        case .show_low:
            return "show_presetting_device_level_low_title".show_localized
        case .show_medium:
            return "show_presetting_device_level_medium_title".show_localized
        case .show_high:
            return "show_presetting_device_level_high_title".show_localized
        case .unknown:
            return ""
        }
    }
    
    var iosInfo: String {
        switch self {
        case .show_low:
            return "show_presetting_device_level_low_desc".show_localized
        case .show_medium:
            return "show_presetting_device_level_medium_desc".show_localized
        case .show_high:
            return "show_presetting_device_level_high_desc".show_localized
        case .unknown:
            return ""
        }
    }
}

class ShowPresettingModel {
    var title: String
    var desc: String
    var standard: ShowPresetStandardType
    var optionsArray: [ShowPresetType]
    
    init(title: String, desc: String, standard: ShowPresetStandardType, optionsArray: [ShowPresetType]) {
        self.title = title
        self.desc = desc
        self.standard = standard
        self.optionsArray = optionsArray
    }
}
