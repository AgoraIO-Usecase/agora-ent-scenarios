//
//  ShowSettingManager.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import Foundation

enum ShowSettingKey: String {
    
    enum KeyType {
        case aSwitch
        case segment
        case slider
        case label
    }
    
    case lowlightEnhance    // 暗光增强
    case colorEnhance       // 色彩增强
    case videoDenoiser      // 降噪
    case beauty             // 美颜
    case PVC                // pvc
    case SR                 // 超分
    case BFrame             // b帧
    case videoCaptureSize   // 视频采集分辨率
    case FPS                // 帧率
    case H265               // h265
    case bitRate            // 码率
    
    func title() -> String {
        switch self {
        case .lowlightEnhance:
            return "show_advance_setting_lowlight_title".show_localized
        case .colorEnhance:
            return "show_advance_setting_colorEnhance_title".show_localized
        case .videoDenoiser:
            return "show_advance_setting_videoDenoiser_title".show_localized
        case .beauty:
            return "show_advance_setting_beauty_title".show_localized
        case .PVC:
            return "show_advance_setting_PVC_title".show_localized
        case .SR:
            return "show_advance_setting_SR_title".show_localized
        case .BFrame:
            return "show_advance_setting_BFrame_title".show_localized
        case .videoCaptureSize:
            return "show_advance_setting_videoCaptureSize_title".show_localized
        case .FPS:
            return "show_advance_setting_FPS_title".show_localized
        case .bitRate:
            return "show_advance_setting_bitRate_title".show_localized
        case .H265:
            return "show_advance_setting_H265_title".show_localized
        }
    }
    
    func type() -> KeyType {
        switch self {
        case .lowlightEnhance:
            return .aSwitch
        case .colorEnhance:
            return .aSwitch
        case .videoDenoiser:
            return .aSwitch
        case .beauty:
            return .aSwitch
        case .PVC:
            return .aSwitch
        case .SR:
            return .slider
        case .BFrame:
            return .aSwitch
        case .videoCaptureSize:
            return .label
        case .FPS:
            return .label
        case .H265:
            return .aSwitch
        case .bitRate:
            return .slider
        }
    }
    
    func items() -> [String] {
        switch self {
        case .videoCaptureSize:
            return ["320x240","480x360","360x640","960x540","960x720","1280x720"]
        case .FPS:
            return ["1 fps","7 fps","10 fps","15 fps","24 fps","30 fps","60 fps"]
        default:
            return []
        }
    }
    
    func boolValue() -> Bool {
        return UserDefaults.standard.bool(forKey: self.rawValue)
    }
    
    func floatValue() -> Float {
        return UserDefaults.standard.float(forKey: self.rawValue)
    }
    
    func intValue() -> Int {
        return UserDefaults.standard.integer(forKey: self.rawValue)
    }
    
    func writeValue(_ value: Any?){
        UserDefaults.standard.set(value, forKey: self.rawValue)
        UserDefaults.standard.synchronize()
    }
}

