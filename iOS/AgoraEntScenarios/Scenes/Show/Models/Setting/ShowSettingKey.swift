//
//  ShowSettingManager.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import Foundation
import AgoraRtcKit

enum ShowAgoraVideoDimensions: String, CaseIterable {
    
    case _240x360 = "240x360"
    case _360x640 = "360x640"
    case _480x854 = "480x854"
    case _540x960 = "540x960"
    case _720x1280 = "720x1280"
    case _1080x1920 = "1080x1920"
     
    var sizeValue: CGSize {
        let arr: [String] = rawValue.split(separator: "x").compactMap{"\($0)"}
        guard let first = arr.first, let width = Float(first), let last = arr.last, let height = Float(last) else {
            return CGSize(width: 360, height: 640)
        }
        return CGSize(width: CGFloat(width), height: CGFloat(height))
    }
}

enum ShowAgoraCaptureVideoDimensions: Int, CaseIterable {
    
    case _1080P = 1080
    case _720P = 720
    case _540P = 540
    case _480P = 480
    case _360P = 360
    case _270P = 270
     
    var sizeValue: CGSize {
        if rawValue == 480 {
            return CGSize(width: 480, height: 854)
        }
        return CGSize(width: CGFloat(rawValue), height: CGFloat(rawValue) * 1280.0 / 720.0)
    }
    
    var valueTitle: String {
        return "\(rawValue)P"
    }
    
    var levelTitle: String {
        switch self {
        case ._1080P:
            return "极清"
        case ._720P:
            return "超清"
        case ._540P:
            return "高清"
        case ._480P:
            return "标清"
        case ._360P:
            return "流畅"
        case ._270P:
            return "低清"
        }
    }
}

extension AgoraVideoFrameRate {
    func stringValue() -> String {
        return "\(rawValue) fps"
    }
}

enum ShowSettingKey: String, CaseIterable {
    
    enum KeyType {
        case aSwitch
        case segment
        case slider
        case label
    }
    
    case lowlightEnhance        // 暗光增强
    case colorEnhance           // 色彩增强
    case videoDenoiser          // 降噪
    case beauty                 // 美颜
    case PVC                    // pvc
    case SR                     // 超分
    case BFrame                 // b帧
    case videoEncodeSize       // 视频编码分辨率
    case FPS                    // 帧率
    case H265                   // h265
    case videoBitRate           // 视频码率
    case earmonitoring          // 耳返
    case recordingSignalVolume  // 人声音量
    case musincVolume           // 音乐音量
    case audioBitRate           // 音频码率
    case captureVideoSize       // 采集分辨率
    
    var title: String {
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
        case .videoEncodeSize:
            return "show_advance_setting_videoCaptureSize_title".show_localized
        case .FPS:
            return "show_advance_setting_FPS_title".show_localized
        case .videoBitRate:
            return "show_advance_setting_bitRate_title".show_localized
        case .H265:
            return "show_advance_setting_H265_title".show_localized
        case .earmonitoring:
            return "show_advance_setting_earmonitoring_title".show_localized
        case .recordingSignalVolume:
            return "show_advance_setting_recordingVolume_title".show_localized
        case .musincVolume:
            return "show_advance_setting_musicVolume_title".show_localized
        case .audioBitRate:
            return "show_advance_setting_audio_bitRate_title".show_localized
        case .captureVideoSize:
            return ""
        }
    }
    
    // 类型
    var type: KeyType {
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
        case .videoEncodeSize:
            return .label
        case .FPS:
            return .label
        case .H265:
            return .aSwitch
        case .videoBitRate:
            return .slider
        case .earmonitoring:
            return .aSwitch
        case .recordingSignalVolume:
            return .slider
        case .musincVolume:
            return .slider
        case .audioBitRate:
            return .label
        case .captureVideoSize:
            return .label
        }
    }
    
    // 弹窗提示文案
    var tips: String {
        switch self {
        case .lowlightEnhance:
            return "show_advance_setting_lowlightEnhance_tips".show_localized
        case .colorEnhance:
            return "show_advance_setting_colorEnhance_tips".show_localized
        case .videoDenoiser:
            return "show_advance_setting_videoDenoiser_tips".show_localized
        case .PVC:
            return "show_advance_setting_PVC_tips".show_localized
        case .SR:
            return "show_advance_setting_SR_tips".show_localized
        case .H265:
            return "show_advance_setting_H265_tips".show_localized
        default:
            return ""
        }
    }
    
    // slider的取值区间
    var sliderValueScope: (Float, Float) {
        switch self {
        case .videoBitRate:
            return (200, 2000)
        case .recordingSignalVolume:
            return (0, 100)
        case .musincVolume:
            return (0, 100)
        default:
            return (0,0)
        }
    }
    
    // 选项
    var items: [String] {
        switch self {
        case .videoEncodeSize:
            return ShowAgoraVideoDimensions.allCases.map({ $0.rawValue })
        case .FPS:
            return [AgoraVideoFrameRate.fps1.stringValue(),
                    AgoraVideoFrameRate.fps7.stringValue(),
                    AgoraVideoFrameRate.fps10.stringValue(),
                    AgoraVideoFrameRate.fps15.stringValue(),
                    AgoraVideoFrameRate.fps24.stringValue(),
                    AgoraVideoFrameRate.fps30.stringValue(),
                    AgoraVideoFrameRate.fps60.stringValue()
            ]
        case .audioBitRate:
            return ["2","3","5"]
        case .captureVideoSize:
            return ShowAgoraCaptureVideoDimensions.allCases.map({ "\($0.rawValue)P" })
        default:
            return []
        }
    }
    
    var boolValue: Bool {
        return UserDefaults.standard.bool(forKey: self.rawValue)
    }
    
    var floatValue: Float {
        return UserDefaults.standard.float(forKey: self.rawValue)
    }
    
    var intValue: Int {
        return UserDefaults.standard.integer(forKey: self.rawValue)
    }
    
    func writeValue(_ value: Any?){
        UserDefaults.standard.set(value, forKey: self.rawValue)
        UserDefaults.standard.synchronize()
    }
}

