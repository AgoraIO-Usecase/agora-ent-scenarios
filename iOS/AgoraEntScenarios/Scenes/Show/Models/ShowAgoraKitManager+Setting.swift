//
//  ShowAgoraKitManager+Setting.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/12/5.
//

import Foundation
import AgoraRtcKit

// 标记是否已经打开过
private let hasOpenedKey = "hasOpenKey"

extension ShowAgoraKitManager {
    
    // 超分倍数
    enum SRType: Int {
        case x1 = 6
        case x1_33 = 7
        case x1_5 = 8
        case x2 = 3
        case x_sharpen = 11
    }
    
    private var dimensionsItems: [CGSize] {
        ShowAgoraVideoDimensions.allCases.map({$0.sizeValue})
    }
    
    private var fpsItems: [AgoraVideoFrameRate] {
        [
           .fps1,
           .fps7,
           .fps10,
           .fps15,
           .fps24,
           .fps30,
           .fps60
       ]
    }
    
    // 默认设置
    func defaultSetting() {
        // 默认音量设置
        ShowSettingKey.recordingSignalVolume.writeValue(80)
        ShowSettingKey.musincVolume.writeValue(30)
//        agoraKit.enableExtension(withVendor: <#T##String#>, extension: <#T##String#>, enabled: <#T##Bool#>)
//        agoraKit.enableExtension("agora_video_filters_super_resolution", "super_resolution")
        ShowSettingKey.SR.writeValue(false) // 默认关闭sr
        let hasOpened = UserDefaults.standard.bool(forKey: hasOpenedKey)
        // 第一次进入房间的时候设置
        if hasOpened == false {
            updatePresetForType(presetType ?? .show_low, mode: .signle)
            UserDefaults.standard.set(true, forKey: hasOpenedKey)
        }
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
    }
    
    // 预设模式
    private func _presetValuesWith(dimensions: ShowAgoraVideoDimensions, fps: AgoraVideoFrameRate, bitRate: Float, h265On: Bool, videoSize: ShowAgoraVideoDimensions) {
        ShowSettingKey.videoEncodeSize.writeValue(dimensionsItems.firstIndex(of: dimensions.sizeValue))
        ShowSettingKey.FPS.writeValue(fpsItems.firstIndex(of: fps))
        ShowSettingKey.videoBitRate.writeValue(bitRate)
        ShowSettingKey.H265.writeValue(h265On)
        ShowSettingKey.lowlightEnhance.writeValue(false)
        ShowSettingKey.colorEnhance.writeValue(false)
        ShowSettingKey.videoDenoiser.writeValue(false)
        ShowSettingKey.PVC.writeValue(false)
        
        updateSettingForkey(.videoEncodeSize)
        updateSettingForkey(.videoBitRate)
        updateSettingForkey(.FPS)
        updateSettingForkey(.H265)
        updateSettingForkey(.lowlightEnhance)
        updateSettingForkey(.colorEnhance)
        updateSettingForkey(.videoDenoiser)
        updateSettingForkey(.PVC)
        
        // 设置采集分辨率
//        setCaptureVideoDimensions(videoSize.sizeValue)
    }
    
    /// 设置观众端画质增强
    private func _setQualityEnable(_ isOn: Bool, srType: SRType? = nil, uid: UInt?){
        if let uid = uid {
            agoraKit.enableRemoteSuperResolution(uid, enable: isOn)
        }
        ShowSettingKey.SR.writeValue(isOn)
        /*
        agoraKit.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(isOn), \"mode\": 2}}")
        if srType != nil {
            agoraKit.setParameters("{\"rtc.video.sr_type\":\(srType!.rawValue)}")
            agoraKit.setParameters("{\"rtc.video.sr_max_wh\":\(921600)}")
        }
         */
    }
    
    func updatePresetForType(_ type: ShowPresetType, mode: ShowMode,uid: UInt? = nil) {
        switch type {
        case .show_low:
            switch mode {
            case .signle:
                _presetValuesWith(dimensions: ._960x540, fps: .fps15, bitRate: 1500, h265On: false, videoSize: ._1280x720)
            case .pk:
                _presetValuesWith(dimensions: ._480x360, fps: .fps15, bitRate: 700, h265On: false, videoSize: ._1280x720)
            }
            break
        case .show_medium:
            switch mode {
            case .signle:
                _presetValuesWith(dimensions: ._1280x720, fps: .fps24, bitRate: 1800, h265On: true, videoSize: ._1280x720)
            case .pk:
                _presetValuesWith(dimensions: ._960x540, fps: .fps15, bitRate: 800, h265On: true, videoSize: ._1280x720)
            }
            
            break
        case .show_high:
            
            switch mode {
            case .signle:
                _presetValuesWith(dimensions: ._1280x720, fps: .fps24, bitRate: 1800, h265On: true, videoSize: ._1280x720)
            case .pk:
                _presetValuesWith(dimensions: ._960x540, fps: .fps15, bitRate: 800, h265On: true, videoSize: ._1280x720)
            }
            
            break
            
        case .quality_low:
            _setQualityEnable(false,uid: uid)
            break
        case .quality_medium:
            _setQualityEnable(true, srType: SRType.x1_5, uid: uid)
        case .quality_high:
            _setQualityEnable(true, srType: SRType.x2, uid: uid)
        case .base_low:
            _setQualityEnable(false,uid: uid)
        case .base_medium:
            _setQualityEnable(false,uid: uid)
        case .base_high:
            _setQualityEnable(false,uid: uid)
        }
    }
    
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
