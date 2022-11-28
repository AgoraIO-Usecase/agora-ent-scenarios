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
    
    // 预设类型
    private var presetType: ShowPresetType?
    
    private let videoEncoderConfig = AgoraVideoEncoderConfiguration()
    private let dimensionsItems: [CGSize] = ShowAgoraVideoDimensions.allCases.map({$0.sizeValue})
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
    
    // 默认设置
    func defaultSetting() {
        
        updatePresetForType(presetType ?? .show_low, mode: .signle)
        updateSettingForkey(.lowlightEnhance)
        updateSettingForkey(.colorEnhance)
        updateSettingForkey(.videoEncodeSize)
        updateSettingForkey(.beauty)
        updateSettingForkey(.PVC)
        updateSettingForkey(.SR)
        updateSettingForkey(.earmonitoring)
        updateSettingForkey(.recordingSignalVolume)
        updateSettingForkey(.musincVolume)
        updateSettingForkey(.audioBitRate)
        /*
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
        videoEncoderConfig.bitrate = Int(ShowSettingKey.videoBitRate.floatValue())
        agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)

        // fps
        let fpsIndex = ShowSettingKey.FPS.intValue()
        videoEncoderConfig.frameRate = fpsItems[fpsIndex]
        agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)

        // H265:
        let h265On = ShowSettingKey.H265.boolValue()
        agoraKit.setParameters("{\"engine.video.enable_hw_encoder\":\(h265On)}")
        agoraKit.setParameters("{\"engine.video.codec_type\":\"\(h265On ? 3 : 2)\"}")
         */
    }
    
    // 预设模式
    private func _presetValuesWith(dimensions: ShowAgoraVideoDimensions, fps: AgoraVideoFrameRate, bitRate: Float, h265On: Bool, captrueSize: ShowAgoraVideoDimensions) {
        ShowSettingKey.videoEncodeSize.writeValue(dimensionsItems.firstIndex(of: dimensions.sizeValue))
        ShowSettingKey.FPS.writeValue(fpsItems.firstIndex(of: fps))
        ShowSettingKey.videoBitRate.writeValue(bitRate)
        ShowSettingKey.H265.writeValue(h265On)
        
        updateSettingForkey(.videoEncodeSize)
        updateSettingForkey(.videoBitRate)
        updateSettingForkey(.FPS)
        updateSettingForkey(.H265)
//        let config = AgoraCameraCapturerConfiguration()
//        config.dimensions = dimensions.sizeValue
//        agoraKit.setCameraCapturerConfiguration(config)
    }
    
    func updatePresetForType(_ type: ShowPresetType, mode: ShowMode) {
        switch type {
        case .show_low:
            switch mode {
            case .signle:
                _presetValuesWith(dimensions: ._960x540, fps: .fps15, bitRate: 1500, h265On: true, captrueSize: ._1280x720)
            case .pk:
                _presetValuesWith(dimensions: ._960x540, fps: .fps15, bitRate: 700, h265On: false, captrueSize: ._1280x720)
            }
            break
        case .show_medium:
            switch mode {
            case .signle:
                _presetValuesWith(dimensions: ._1280x720, fps: .fps15, bitRate: 1800, h265On: true, captrueSize: ._1280x720)
            case .pk:
                _presetValuesWith(dimensions: ._960x540, fps: .fps15, bitRate: 800, h265On: true, captrueSize: ._1280x720)
            }
            
            break
        case .show_high:
            
            break
        }
    }
}

extension ShowSettingManager {
    
    /// 更新设置
    /// - Parameter key: 要更新的key
    func updateSettingForkey(_ key: ShowSettingKey) {
        let isOn = key.boolValue
        let index = key.intValue
        let sliderValue = key.floatValue
        
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
        case .videoEncodeSize:
            videoEncoderConfig.dimensions = dimensionsItems[index]
            agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        case .videoBitRate:
            videoEncoderConfig.bitrate = Int(sliderValue)
            agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        case .FPS:
            videoEncoderConfig.frameRate = fpsItems[index]
            agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        case .H265:
            agoraKit.setParameters("{\"engine.video.enable_hw_encoder\":\(isOn)}")
            agoraKit.setParameters("{\"engine.video.codec_type\":\"\(isOn ? 3 : 2)\"}")
        case .earmonitoring:
            agoraKit.enable(inEarMonitoring: isOn)
        case .recordingSignalVolume:
            agoraKit.adjustRecordingSignalVolume(Int(sliderValue))
        case .musincVolume:
            agoraKit.adjustAudioMixingVolume(Int(sliderValue))
        case .audioBitRate:
            break
        }
    }
}
