//
//  ShowSettingManager.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import Foundation
import AgoraRtcKit

enum ShowAgoraVideoDimensions: String {
    
    case _320x240 = "320x240"
    case _480x360 = "480x360"
    case _360x640 = "360x640"
    case _960x540 = "960x540"
    case _960x720 = "960x720"
    case _1280x720 = "1280x720"
     
    func sizeValue() -> CGSize{
        let arr: [String] = rawValue.split(separator: "x").compactMap{"\($0)"}
        guard let first = arr.first, let width = Float(first), let last = arr.last, let height = Float(last) else {
            return CGSize(width: 320, height: 240)
        }
        return CGSize(width: CGFloat(width), height: CGFloat(height))
    }
}

extension AgoraVideoFrameRate {
    func stringValue() -> String {
        return "\(rawValue) fps"
    }
}

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
            return .aSwitch
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
            return [ShowAgoraVideoDimensions._320x240.rawValue,
                    ShowAgoraVideoDimensions._480x360.rawValue,
                    ShowAgoraVideoDimensions._360x640.rawValue,
                    ShowAgoraVideoDimensions._960x540.rawValue,
                    ShowAgoraVideoDimensions._960x720.rawValue,
                    ShowAgoraVideoDimensions._1280x720.rawValue
            ]
        case .FPS:
            return [AgoraVideoFrameRate.fps1.stringValue(),
                    AgoraVideoFrameRate.fps7.stringValue(),
                    AgoraVideoFrameRate.fps10.stringValue(),
                    AgoraVideoFrameRate.fps15.stringValue(),
                    AgoraVideoFrameRate.fps24.stringValue(),
                    AgoraVideoFrameRate.fps30.stringValue(),
                    AgoraVideoFrameRate.fps60.stringValue()
            ]
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

