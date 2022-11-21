//
//  ShowSettingManager.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/17.
//

import Foundation
import AgoraRtcKit

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
    
}

extension ShowSettingManager {
    
    /// 更新设置
    /// - Parameter key: 要更新的key
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
