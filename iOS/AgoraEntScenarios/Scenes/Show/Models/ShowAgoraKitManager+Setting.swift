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

private let fpsItems: [AgoraVideoFrameRate] = [
    .fps1,
    .fps7,
    .fps10,
    .fps15,
    .fps24,
    .fps30,
    .fps60
]

extension ShowAgoraKitManager {
    
    // 超分倍数
    enum SRType: Int {
        case none = -1
        case x1 = 6
        case x1_33 = 7
        case x1_5 = 8
        case x2 = 3
        case x_sharpen = 11
        case x_superQuality = 20
    }
    
    private var dimensionsItems: [CGSize] {
        ShowAgoraVideoDimensions.allCases.map({$0.sizeValue})
    }
    
    private var captureDimensionsItems: [CGSize] {
        ShowAgoraCaptureVideoDimensions.allCases.map({$0.sizeValue})
    }
    
    // 默认设置
    func defaultSetting() {
        // 默认音量设置
        ShowSettingKey.recordingSignalVolume.writeValue(80)
        ShowSettingKey.musincVolume.writeValue(30)
        let hasOpened = UserDefaults.standard.bool(forKey: hasOpenedKey)
        // 第一次进入房间的时候设置
        if hasOpened == false {
            updatePresetForType(.show_medium, mode: .single)
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
        updateSettingForkey(.captureVideoSize)
        updateSettingForkey(.FPS)
        updateSettingForkey(.videoBitRate)
    }
    
    /// 设置超分 不保存数据
    /// - Parameters:
    ///   - isOn: 开关
    ///   - srType: 默认1.5倍
    func setSuperResolutionOn(_ isOn: Bool, srType:SRType = .none) {
        // 避免重复设置
        if isOn == self.srIsOn && srType == self.srType {
            return
        }
        self.srIsOn = isOn
        self.srType = srType
        if srType == .none {
            engine?.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(false), \"mode\": 2}}")
        }else{
            engine?.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(false), \"mode\": 2}}")
            engine?.setParameters("{\"rtc.video.sr_type\":\(srType.rawValue)}")
            engine?.setParameters("{\"rtc.video.sr_max_wh\":\(921600)}")
            // enabled要放在srType之后 否则修改超分倍数可能不会立即生效
            engine?.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(isOn), \"mode\": 2}}")
        }
        showLogger.info("----- setSuperResolutionOn\(ShowSettingKey.SR.boolValue)  srType: \(srType)")
    }
    
    func setDefaultSuperResolutionForAudienceType(presetType: ShowPresetType) {
        var srType = SRType.none
        switch presetType {
        case .quality_medium, .quality_high:
            srType = .x1_5
        default:
            break
        }
        setSuperResolutionOn(ShowSettingKey.SR.boolValue, srType: srType)
    }
    
    /// 设置超分倍数
    /// - Parameters:
    ///   - presetType: 预设类型
    ///   - videoWidth: 编码分辨率的宽
    func setSuperResolutionForAudienceType(presetType: ShowPresetType, videoWidth:Int, mode: ShowMode) {
        var srType: SRType = .x1_33
        switch presetType {
        case .unknown,.show_low, .show_medium, .show_high:
        break
        case .quality_low:
            srType = .none
        case .quality_medium:
            switch mode {
            case .single:
                if videoWidth <= 540 {
                    srType = .x1_33
                } else if videoWidth == 720 {
                    srType = .x_superQuality
                } else {
                    srType = .none
                }
            case .pk:
                srType = .x1_33
            }
        case .quality_high:
            switch mode {
            case .single:
                if videoWidth <= 360 {
                    srType = .x2
                }else if videoWidth <= 540 {
                    srType = .x1_33
                }else if videoWidth == 720 {
                    srType = .x_superQuality
                }else {
                    srType = .none
                }
            case .pk:
                srType = .x1_33
            }
        case .base_low, .base_medium, .base_high:
            srType = .none
        }
        setSuperResolutionOn(ShowSettingKey.SR.boolValue, srType: srType)
    }
    
    /// 选择采集分辨率
    /// - Parameter index: 索引
    func selectCaptureVideoDimensions(index: Int) {
//        setCaptureVideoDimensions(captureDimensionsItems[index])
//        ShowSettingKey.captureVideoSize.writeValue(index)
    }
    
    // 预设模式
    private func _presetValuesWith(encodeSize: ShowAgoraVideoDimensions, fps: AgoraVideoFrameRate, bitRate: Float, h265On: Bool, captureSize: ShowAgoraCaptureVideoDimensions, cache: Bool = true) {
        if cache {
            ShowSettingKey.videoEncodeSize.writeValue(dimensionsItems.firstIndex(of: encodeSize.sizeValue))
            ShowSettingKey.FPS.writeValue(fpsItems.firstIndex(of: fps))
            ShowSettingKey.videoBitRate.writeValue(bitRate)
            ShowSettingKey.H265.writeValue(h265On)
            ShowSettingKey.captureVideoSize.writeValue(captureDimensionsItems.firstIndex(of: captureSize.sizeValue))
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
            updateSettingForkey(.captureVideoSize)
            updateCameraCaptureConfiguration()
        }else {
            videoEncoderConfig.dimensions = encodeSize.sizeValue
            videoEncoderConfig.frameRate = fps
            videoEncoderConfig.bitrate = Int(bitRate)
            captureConfig.dimensions = captureSize.sizeValue
            captureConfig.frameRate = Int32(fps.rawValue)
            engine?.setVideoEncoderConfiguration(videoEncoderConfig)
            setH265On(h265On)
        }
       
    }
    
    /// 设置观众端超分
    private func _setQualityEnable(_ isOn: Bool){
        ShowSettingKey.SR.writeValue(isOn)
    }
    
    private func _resetPresetValues() {
        updateSettingForkey(.videoEncodeSize)
        updateSettingForkey(.videoBitRate)
        updateSettingForkey(.FPS)
        updateSettingForkey(.captureVideoSize)
    }

    func updatePresetForType(_ type: ShowPresetType, mode: ShowMode, cache: Bool = true) {
        switch type {
        case .unknown:
            break
        case .show_low:
            switch mode {
            case .single:
                _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 1461, h265On: false, captureSize: ._1080P, cache: cache)
            case .pk:
                _presetValuesWith(encodeSize: ._360x640, fps: .fps15, bitRate: 700, h265On: false, captureSize: ._720P, cache: cache)
            }
            broadcastorMachineType = .low
        case .show_medium:
            switch mode {
            case .single:
                _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 1800, h265On: true, captureSize: ._720P, cache: cache)
            case .pk:
                _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 800, h265On: true, captureSize: ._720P, cache: cache)
            }
            broadcastorMachineType = .medium
        case .show_high:
            
            switch mode {
            case .single:
                _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 2099, h265On: true, captureSize: ._720P, cache: cache)
            case .pk:
                _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 800, h265On: true, captureSize: ._720P, cache: cache)
            }
            broadcastorMachineType = .high
        case .quality_low:
            _setQualityEnable(false)
        case .quality_medium:
            _setQualityEnable(true)
        case .quality_high:
            _setQualityEnable(true)
        case .base_low:
            _setQualityEnable(false)
        case .base_medium:
            _setQualityEnable(false)
        case .base_high:
            _setQualityEnable(false)
        }
    }
    
    /// 更新配置信息 该设置不会保存到本地
    /// - Parameters:
    ///   - mode: 秀场交互类型
    func updateVideoProfileForMode(_ mode: ShowMode) {
        switch broadcastorMachineType {
        case .unknown:
            break
        case .low:
            switch mode {
            case .single:
                _resetPresetValues()
            case .pk:
                updatePresetForType(.show_low, mode: .pk, cache: false)
            }
            
        case .medium:
            switch mode {
            case .single:
                _resetPresetValues()
            case .pk:
                updatePresetForType(.show_medium, mode: .pk, cache: false)
            }
            
        case .high:
            switch mode {
            case .single:
                _resetPresetValues()
            case .pk:
                updatePresetForType(.show_high, mode: .pk, cache: false)
            }
        }
    }
    
    /// 更新设置
    /// - Parameter key: 要更新的key
    func updateSettingForkey(_ key: ShowSettingKey, currentChannelId:String? = nil) {
        let isOn = key.boolValue
        let indexValue = key.intValue
        let sliderValue = key.floatValue
        
        switch key {
        case .lowlightEnhance:
            engine?.setLowlightEnhanceOptions(isOn, options: AgoraLowlightEnhanceOptions())
        case .colorEnhance:
            engine?.setColorEnhanceOptions(isOn, options: AgoraColorEnhanceOptions())
        case .videoDenoiser:
            engine?.setVideoDenoiserOptions(isOn, options: AgoraVideoDenoiserOptions())
        case .beauty:
            engine?.setBeautyEffectOptions(isOn, options: AgoraBeautyOptions())
        case .PVC:
            engine?.setParameters("{\"rtc.video.enable_pvc\":\(isOn)}")
        case .SR:
            engine?.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(isOn), \"mode\": 2}}")
//            setSuperResolutionOn(isOn)
        case .BFrame:
            
           break
        case .videoEncodeSize:
            let index = indexValue % dimensionsItems.count
            videoEncoderConfig.dimensions = dimensionsItems[index]
            if let currentChannelId = currentChannelId{
                updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)
            }else{
                engine?.setVideoEncoderConfiguration(videoEncoderConfig)
            }
        case .videoBitRate:
            videoEncoderConfig.bitrate = Int(sliderValue)
            if let currentChannelId = currentChannelId {
                updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)
            }else{
                engine?.setVideoEncoderConfiguration(videoEncoderConfig)
            }
        case .FPS:
            let index = indexValue % fpsItems.count
            videoEncoderConfig.frameRate = fpsItems[index]
            if let currentChannelId = currentChannelId {
                updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)
            }else{
                engine?.setVideoEncoderConfiguration(videoEncoderConfig)
            }
            // 采集帧率
            captureConfig.frameRate = Int32(fpsItems[index].rawValue)
            
        case .H265:
            setH265On(isOn)
        case .earmonitoring:
            engine?.enable(inEarMonitoring: isOn)
        case .recordingSignalVolume:
            engine?.adjustRecordingSignalVolume(Int(sliderValue))
        case .musincVolume:
            engine?.adjustAudioMixingVolume(Int(sliderValue))
        case .audioBitRate:
            break
        case .captureVideoSize:
            let index = indexValue % captureDimensionsItems.count
            captureConfig.dimensions = captureDimensionsItems[index]
        }
    }

}

private let kBroadcastorMachineType = "kBroadcastorMachineType"

extension ShowAgoraKitManager {
    enum MachineType: Int {
        case unknown = 0
        case high
        case medium
        case low
    }
    
    // 选择的主播端机型
    var broadcastorMachineType: MachineType {
        set {
            UserDefaults.standard.set(newValue.rawValue, forKey: kBroadcastorMachineType)
        }
        get {
            if let value = UserDefaults.standard.value(forKey: kBroadcastorMachineType) as? Int {
                return MachineType(rawValue: value) ?? .unknown
            }
            return .unknown
        }
    }
}
