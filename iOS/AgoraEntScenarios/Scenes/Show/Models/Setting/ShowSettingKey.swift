//
//  ShowSettingManager.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import Foundation
import AgoraRtcKit

enum ShowAgoraSRType: String, CaseIterable {
    case x1 = "x1"
    case x1_33 = "x1.33"
    case x1_5 = "x1.5"
    case x2 = "x2"
    
    var typeValue: ShowAgoraKitManager.SRType {
        switch self {
        case .x1:
            return .x1
        case .x1_33:
            return .x1_33
        case .x1_5:
            return .x1_5
        case .x2:
            return .x2
        }
    }
}

enum ShowAgoraRenderMode: String, CaseIterable {
    case hidden = "hidden"
    case fit = "fit"
    
    var modeValue: AgoraVideoRenderMode {
        switch self {
        case .hidden:
            return .hidden
        case .fit:
            return .fit
        }
    }
}

enum ShowAgoraEncode: String, CaseIterable {
    case hard = "硬编"
    case soft = "软编"
    
    var encodeValue: Bool {
        switch self {
        case .hard:
            return true
        case .soft:
            return false
        }
    }
}

enum ShowAgoraCodeCType: String, CaseIterable {
    case h265 = "h265"
    case h264 = "h264"
    
    var typeValue: Int {
        switch self {
        case .h265:
            return 3
        case .h264:
            return 2
        }
    }
}

enum ShowAgoraVideoDimensions: String, CaseIterable {
    
    case _240x360 = "240x360"
    case _360x640 = "360x640"
    case _480x856 = "480x856"
    case _540x960 = "540x960"
    case _720x1280 = "720x1280"
//    case _1080x1920 = "1080x1920"
     
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
    case captureFrameRate       // 采集码率
    case focusFace              // 人脸对焦
    case encode                 // 硬编/软编
    case codeCType                // 编码器
    case mirror                 // 镜像
    case renderMode             // 模式
    case debugSrType            // 超分倍数
    case debugSR                // debug超分开关
    case debugPVC               // pvc
    
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
        case .captureFrameRate:
            return ""
        case .focusFace:
            return "人脸对焦"
        case .encode:
            return "硬编/软编"
        case .codeCType:
            return "编码器"
        case .mirror:
            return "镜像"
        case .renderMode:
            return "fit/hidden"
        case .debugSrType:
            return "超分倍数"
        case .debugSR:
            return "超分开关"
        case .debugPVC:
            return "PVC"
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
        case .captureFrameRate:
            return .label
        case .focusFace:
            return .aSwitch
        case .encode:
            return .label
        case .codeCType:
            return .label
        case .mirror:
            return .aSwitch
        case .renderMode:
            return .label
        case .debugSrType:
            return .label
        case .debugSR:
            return .aSwitch
        case .debugPVC:
            return .aSwitch
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
        case .videoEncodeSize:
            return "show_advance_setting_videoEncodeSize_tips".show_localized
        case .FPS:
            return "show_advance_setting_fps_tips".show_localized
        default:
            return ""
        }
    }
    
    // slider的取值区间
    var sliderValueScope: (Float, Float) {
        switch self {
        case .videoBitRate:
            return (200, 4000)
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
        case .FPS, .captureFrameRate:
            return [AgoraVideoFrameRate.fps1.stringValue(),
                    AgoraVideoFrameRate.fps7.stringValue(),
                    AgoraVideoFrameRate.fps10.stringValue(),
                    AgoraVideoFrameRate.fps15.stringValue(),
                    AgoraVideoFrameRate.fps24.stringValue(),
            ]
        case .audioBitRate:
            return ["2","3","5"]
        case .captureVideoSize:
            return ShowAgoraCaptureVideoDimensions.allCases.map({ "\($0.rawValue)P" })
        case .encode:
            return ShowAgoraEncode.allCases.map({$0.rawValue})
        case .codeCType:
            return ShowAgoraCodeCType.allCases.map({$0.rawValue})
        case .renderMode:
            return ShowAgoraRenderMode.allCases.map({$0.rawValue})
        case .debugSrType:
            return ShowAgoraSRType.allCases.map({$0.rawValue})
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

