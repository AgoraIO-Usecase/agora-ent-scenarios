//
//  ShowAgoraKitManager+Setting.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/12/5.
//

import Foundation
import AgoraRtcKit
import AgoraCommon
private let kEncodeWidth = "kEncodeWidth"
private let kEncodeHeight = "kEncodeHeight"
private let kEncodeFPS = "kEncodeFPS"
private let kEncodeBitrate = "kEncodeBitrate"

enum ShowMode {
    case single // 单主播模式
    case pk // pk模式
}

private let fpsItems: [AgoraVideoFrameRate] = [
    .fps1,
    .fps7,
    .fps10,
    .fps15,
    .fps24,
    .fps30,
    .fps60
]

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

class ShowRTCParams {
    // 自动设置数据
    var suggested = true
    
    var sr = false
    var srType: SRType = .x1_33
    var dualStream: AgoraSimulcastStreamConfig?
    var pvc = false
    var svc = false
    var musicVolume: Int = 30
    var recordingSignalVolume: Int = 80
}

// MARK: - Extension
extension ShowAgoraKitManager {
    
    func updateAudienceProfile() {
        _presetValuesWith(encodeSize: ._360x640, fps: .fps15, bitRate: 0, h265On: true)
    }
    
    func setupAudienceProfile() {
        setSuperResolutionOn(true)
        setPVCon(true)
        _presetValuesWith(encodeSize: ._360x640, fps: .fps15, bitRate: 0, h265On: true)
    }
    
    func setupBroadcasterProfile() {
        setSuperResolutionOn(false)
        setPVCon(true)
        updateVideoProfileForMode(.single)
    }
    
    /// 设置超分 不保存数据
    /// - Parameters:
    ///   - isOn: 开关
    ///   - srType: 默认1.5倍
    func setDebugSuperResolutionOn(_ isOn: Bool, srType:SRType = .none) {
        // 避免重复设置
        if isOn == self.rtcParam.sr && srType == self.rtcParam.srType {
            return
        }
        self.rtcParam.sr = isOn
        self.rtcParam.srType = srType
        if srType == .none {
            engine?.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(false), \"mode\": 2}}")
        }else{
            engine?.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(false), \"mode\": 2}}")
            engine?.setParameters("{\"rtc.video.sr_type\":\(srType.rawValue)}")
            engine?.setParameters("{\"rtc.video.sr_max_wh\":\(921598)}")
            // enabled要放在srType之后 否则修改超分倍数可能不会立即生效
            engine?.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(isOn), \"mode\": 2}}")
        }
    }
    
    /// 设置超分 不保存数据
    /// - Parameters:
    ///   - isOn: 开关
    func setSuperResolutionOn(_ isOn: Bool) {
        self.rtcParam.sr = isOn
        engine?.setParameters("{\"rtc.video.sr_max_wh\":\(921598)}")
        engine?.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(isOn), \"mode\": 2}}")
    }
    
    /// 设置PVC
    /// - Parameters:
    ///   - isOn: 开关
    func setPVCon(_ isOn: Bool) {
        self.rtcParam.pvc = isOn
        engine?.setParameters("{\"rtc.video.enable_pvc\":\(isOn)}")
    }
    
    /// 设置降噪
    /// - Parameters:
    ///   - isOn: 开关
    func setDenoiserOn(_ isOn: Bool) {
        let option = AgoraVideoDenoiserOptions()
        switch deviceLevel {
        case .high:
            option.mode = .manual
            option.level = .highQuality
        case .medium:
            option.mode = .manual
            option.level = .highQuality
        case .low:
            option.mode = .manual
            option.level = .fast
        }
        engine?.setVideoDenoiserOptions(isOn, options: option)
    }
    
    // 预设模式
    private func _presetValuesWith(encodeSize: ShowAgoraVideoDimensions, fps: AgoraVideoFrameRate, bitRate: Float, h265On: Bool) {
        if AppContext.shared.isDeveloperMode {
            return
        }
        ShowSettingKey.videoEncodeSize.writeValue(ShowAgoraVideoDimensions.values().firstIndex(of: encodeSize.sizeValue))
        ShowSettingKey.FPS.writeValue(fpsItems.firstIndex(of: fps))
        ShowSettingKey.videoBitRate.writeValue(bitRate)
        ShowSettingKey.H265.writeValue(h265On)
        ShowSettingKey.lowlightEnhance.writeValue(false)
        ShowSettingKey.colorEnhance.writeValue(false)
        ShowSettingKey.videoDenoiser.writeValue(false)
        
        updateSettingForkey(.videoEncodeSize)
        updateSettingForkey(.videoBitRate)
        updateSettingForkey(.FPS)
        updateSettingForkey(.H265)
        updateSettingForkey(.lowlightEnhance)
        updateSettingForkey(.colorEnhance)
        updateSettingForkey(.videoDenoiser)
        updateSettingForkey(.recordingSignalVolume)
    }

    /// 更新配置信息 该设置不会保存到本地
    /// - Parameters:
    ///   - mode: 秀场交互类型
    func updateVideoProfileForMode(_ showMode: ShowMode) {
        let machine = deviceLevel
        
        rtcParam.suggested = true
        if (showMode == .single) {
            if (machine == .high) {
                _presetValuesWith(encodeSize: ._1080x1920, fps: .fps24, bitRate: 0, h265On: true)
            } else if (machine == .medium) {
                _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 0, h265On: true)
            } else if (machine == .low) {
                _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            }
        } else {
            if (machine == .high) {
                _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            } else {
                _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            }
        }
    }
    
    /// 更新设置
    /// - Parameter key: 要更新的key
    func updateSettingForkey(_ key: ShowSettingKey, currentChannelId:String? = nil) {
        switch key {
        case .lowlightEnhance:
            let isOn = key.boolValue
            engine?.setLowlightEnhanceOptions(isOn, options: AgoraLowlightEnhanceOptions())
        case .colorEnhance:
            let isOn = key.boolValue
            engine?.setColorEnhanceOptions(isOn, options: AgoraColorEnhanceOptions())
        case .videoDenoiser:
            let isOn = key.boolValue
            setDenoiserOn(isOn)
        case .beauty:
            let isOn = key.boolValue
            engine?.setBeautyEffectOptions(isOn, options: AgoraBeautyOptions())
        case .PVC:
            break
        case .BFrame:
           break
        case .videoEncodeSize:
            let indexValue = key.intValue
            let dimensions = ShowAgoraVideoDimensions.values()
            let index = indexValue % dimensions.count
            let size = dimensions[index]
            let encoderConfig = getEncoderConfig()
            let captureConfig = getCaptureConfig()
            encoderConfig.dimensions = size
            captureConfig.dimensions = size
            
            if let currentChannelId = currentChannelId{
                engine?.setCameraCapturerConfiguration(captureConfig)
                updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)
            } else {
                engine?.setCameraCapturerConfiguration(captureConfig)
                engine?.setVideoEncoderConfiguration(encoderConfig)
            }
        case .videoBitRate:
            let sliderValue = key.floatValue
            let encoderConfig = getEncoderConfig()
            encoderConfig.bitrate = Int(sliderValue)
            if let currentChannelId = currentChannelId {
                updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)
            }else{
                engine?.setVideoEncoderConfiguration(encoderConfig)
            }
        case .FPS:
            let indexValue = key.intValue
            let index = indexValue % fpsItems.count
            let encoderConfig = getEncoderConfig()
            let captureConfig = getCaptureConfig()
            encoderConfig.frameRate = fpsItems[index]
            // 采集帧率
            captureConfig.frameRate = Int32(fpsItems[index].rawValue)
            engine?.setCameraCapturerConfiguration(captureConfig)
            if let currentChannelId = currentChannelId {
                updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)
            }else{
                engine?.setVideoEncoderConfiguration(encoderConfig)
            }
        case .H265:
            let isOn = key.boolValue
            let encoderConfig = getEncoderConfig()
            encoderConfig.codecType = isOn ? .H265 : .H264
            if let channelId = currentChannelId {
                updateVideoEncoderConfigurationForConnenction(currentChannelId: channelId)
            }
        case .earmonitoring:
            let isOn = key.boolValue
            engine?.enable(inEarMonitoring: isOn)
        case .recordingSignalVolume:
            let value = rtcParam.recordingSignalVolume
            engine?.adjustRecordingSignalVolume(value)
        case .musicVolume:
            let value = rtcParam.musicVolume
            engine?.adjustAudioMixingVolume(value)
        case .audioBitRate:
            break
        default: break
        }
    }

    func getEncoderConfig() -> AgoraVideoEncoderConfiguration {
        let encoderConfig = AgoraVideoEncoderConfiguration()
        if AppContext.shared.isDeveloperMode {
            if let encodeWidth: CGFloat = UserDefaults.standard.value(forKey: kEncodeWidth) as? CGFloat ,let encodeHeight: CGFloat = UserDefaults.standard.value(forKey: kEncodeHeight) as? CGFloat {
                encoderConfig.dimensions = CGSize(width: encodeWidth, height: encodeHeight)
            }
            if let fps: Int = UserDefaults.standard.value(forKey: kEncodeFPS) as? Int {
                encoderConfig.frameRate =  AgoraVideoFrameRate(rawValue: fps) ?? .fps15
            }
            if let bitrate: Int = UserDefaults.standard.value(forKey: kEncodeBitrate) as? Int {
                encoderConfig.bitrate = bitrate
            }
            return encoderConfig
        }
        let indexValue = ShowSettingKey.videoEncodeSize.intValue
        let dimensions = ShowAgoraVideoDimensions.values()
        let index = indexValue % dimensions.count
        let size = dimensions[index]
        encoderConfig.dimensions = size
        
        let sliderValue = ShowSettingKey.videoBitRate.floatValue
        encoderConfig.bitrate = Int(sliderValue)
        
        let fpsIndex = ShowSettingKey.FPS.intValue
        let idx = fpsIndex % fpsItems.count
        encoderConfig.frameRate = fpsItems[idx]
        
        let isOn = ShowSettingKey.H265.boolValue
        encoderConfig.codecType = isOn ? .H265 : .H264
        return encoderConfig
    }
    
    func getCaptureConfig() -> AgoraCameraCapturerConfiguration {
        let config = AgoraCameraCapturerConfiguration()
        config.followEncodeDimensionRatio = true
//        config.cameraDirection = .front
        
        if AppContext.shared.isDeveloperMode {
            if let encodeWidth: CGFloat = UserDefaults.standard.value(forKey: kEncodeWidth) as? CGFloat ,let encodeHeight: CGFloat = UserDefaults.standard.value(forKey: kEncodeHeight) as? CGFloat {
                config.dimensions = CGSize(width: encodeWidth, height: encodeHeight)
            }
            if let fps: Int = UserDefaults.standard.value(forKey: kEncodeFPS) as? Int {
                config.frameRate = Int32(fps)
            }
            return config
        }
       
        let indexValue = ShowSettingKey.videoEncodeSize.intValue
        let dimensions = ShowAgoraVideoDimensions.values()
        let index = indexValue % dimensions.count
        let size = dimensions[index]
        config.dimensions = size
        
        let fpsIndex = ShowSettingKey.FPS.intValue
        let idx = fpsIndex % fpsItems.count
        config.frameRate = Int32(fpsItems[idx].rawValue)
        
        return config
    }
}
    
// MARK: - Presetting options
extension ShowAgoraKitManager {
    
    // 预设值：设备状况
    enum DeviceLevel: Int {
        case low = 0
        case medium
        case high
        
        func description() -> String {
            switch self {
            case .high:     return "show_setting_device_level_high".show_localized
            case .medium:   return "show_setting_device_level_mediu".show_localized
            case .low:      return "show_setting_device_level_low".show_localized
            }
        }
    }
}
