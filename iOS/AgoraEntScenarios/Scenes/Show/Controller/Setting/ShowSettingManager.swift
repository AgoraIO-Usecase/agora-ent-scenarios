//
//  ShowSettingManager.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import Foundation
import AgoraRtcKit

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
            return "暗光增强"
        case .colorEnhance:
            return "色彩增强"
        case .videoDenoiser:
            return "降噪"
        case .beauty:
            return "美颜"
        case .PVC:
            return "PVC"
        case .SR:
            return "超分"
        case .BFrame:
            return "B帧"
        case .videoCaptureSize:
            return "视频采集分辨率"
        case .FPS:
            return "帧率"
        case .bitRate:
            return "码率"
        case .H265:
            return "H265"
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
            return ["320x240","320x2401","320x2402","320x2403","320x2405","320x2406","320x2407"]
        case .FPS:
            return ["1","7","10","15","24","30","60"]
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

class ShowSettingManager {
    
    private var agoraKit: AgoraRtcEngineKit!
    
    private let videoEncoderConfig = AgoraVideoEncoderConfiguration()
    private let dimensionsItems: [CGSize] = [
        CGSize(width: 320, height: 240),
        CGSize(width: 480, height: 360),
        CGSize(width: 360, height: 640),
        CGSize(width: 960, height: 540),
        CGSize(width: 960, height: 720),
        CGSize(width: 1280, height: 720),
    ]
    private let fpsItems: [AgoraVideoFrameRate] = [
        .fps1,
        .fps7,
        .fps10,
        .fps15,
        .fps24,
        .fps30,
        .fps60
    ]
    
    init(agoraKit: AgoraRtcEngineKit) {
        self.agoraKit = agoraKit
        defaultSetting()
    }
    
    private func defaultSetting() {
        
        agoraKit.setLowlightEnhanceOptions(ShowSettingKey.lowlightEnhance.boolValue(), options: AgoraLowlightEnhanceOptions())
        agoraKit.setColorEnhanceOptions(ShowSettingKey.colorEnhance.boolValue(), options: AgoraColorEnhanceOptions())
        agoraKit.setVideoDenoiserOptions(ShowSettingKey.videoDenoiser.boolValue(), options: AgoraVideoDenoiserOptions())
        agoraKit.setBeautyEffectOptions(ShowSettingKey.beauty.boolValue(), options: AgoraBeautyOptions())
        agoraKit.setParameters("{\"rtc.video.enable_pvc\":\(ShowSettingKey.PVC.boolValue())}")
        agoraKit.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(ShowSettingKey.SR.boolValue()), \"mode\": 2}}")
        
        // videoCaptureSize
       
        let index = ShowSettingKey.videoCaptureSize.intValue()
        videoEncoderConfig.dimensions = dimensionsItems[index]
        agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        
        //  bitRate
        videoEncoderConfig.bitrate = Int(ShowSettingKey.bitRate.floatValue())
        agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        
        // fps
        let fpsIndex = ShowSettingKey.FPS.intValue()
        videoEncoderConfig.frameRate = fpsItems[fpsIndex]
        agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        
        // H265:
        let h265On = ShowSettingKey.H265.boolValue()
        agoraKit.setParameters("{\"engine.video.enable_hw_encoder\":\(h265On)}")
        agoraKit.setParameters("{\"engine.video.codec_type\":\"\(h265On ? 3 : 2)\"}")
    }
    
    
    func updateSettingForkey(_ key: ShowSettingKey) {
        let isOn = key.boolValue()
        let index = key.intValue()
        let sliderValue = key.floatValue()
        
        switch key {
        case .lowlightEnhance:
            agoraKit.setLowlightEnhanceOptions(isOn, options: AgoraLowlightEnhanceOptions())
        case .colorEnhance:
            agoraKit.setColorEnhanceOptions(isOn, options: AgoraColorEnhanceOptions())
        case .videoDenoiser:
            agoraKit.setVideoDenoiserOptions(isOn, options: AgoraVideoDenoiserOptions())
        case .beauty:
            agoraKit.setBeautyEffectOptions(isOn, options: AgoraBeautyOptions())
        case .PVC:
            agoraKit.setParameters("{\"rtc.video.enable_pvc\":\(isOn)}")
        case .SR:
            agoraKit.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(isOn), \"mode\": 2}}")
        case .BFrame:
            
           break
        case .videoCaptureSize:
            videoEncoderConfig.dimensions = dimensionsItems[index]
            agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        case .bitRate:
            videoEncoderConfig.bitrate = Int(sliderValue)
            agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        case .FPS:
            videoEncoderConfig.frameRate = fpsItems[index]
            agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        case .H265:
            agoraKit.setParameters("{\"engine.video.enable_hw_encoder\":\(isOn)}")
            agoraKit.setParameters("{\"engine.video.codec_type\":\"\(isOn ? 3 : 2)\"}")
        }
    }
}

