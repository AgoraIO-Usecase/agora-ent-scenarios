//
//  ShowAgoraKitManager+Setting.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/12/5.
//

import Foundation
import AgoraRtcKit

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
    
    func setupAudienceProfile() {
        setSuperResolutionOn(true)
        setPVCon(false)
        _presetValuesWith(encodeSize: ._360x640, fps: .fps15, bitRate: 0, h265On: true)
    }
    
    func setupBroadcasterProfile() {
        setSuperResolutionOn(false)
        setPVCon(false)
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
            option.level = .strength
        case .medium:
            option.mode = .manual
            option.level = .highQuality
        case .low:
            option.mode = .manual
            option.level = .fast
        }
        engine?.setVideoDenoiserOptions(isOn, options: option)
    }
    
    /** 设置小流参数
     */
    private func setSimulcastStream(isOn: Bool, dimensions: CGSize = CGSizeMake(360, 640), fps: Int32 = 5, bitrate: Int = 680, svc: Bool = false) {
        if isOn {
            let config = AgoraSimulcastStreamConfig()
            config.dimensions = dimensions
            config.framerate = fps
            config.kBitrate = Int32(bitrate)
            rtcParam.dualStream = config
            rtcParam.svc = svc
        } else {
            rtcParam.dualStream = nil
            rtcParam.svc = false
        }
    }
    /** 应用小流设置
     * 在joinChannel成功之后主播应用之前对小流的配置
     */
    func applySimulcastStream(connection: AgoraRtcConnection) {
        guard let simulcastConfig = rtcParam.dualStream else {
            engine?.setDualStreamModeEx(.disableSimulcastStream,
                                        streamConfig: AgoraSimulcastStreamConfig(),
                                        connection: connection)
            return
        }
        // 小流SVC 开关
        if (rtcParam.svc) {
            engine?.setParameters("{\"che.video.minor_stream_num_temporal_layers\": 2}")
            engine?.setParameters("{\"rtc.video.high_low_video_ratio_enabled\": true}")
            engine?.setParameters("{\"che.video.enable_264_fix_svc_nego\": false}")
            engine?.setParameters("{\"rtc.video.low_stream_enable_hw_encoder\": false}")
        } else {
            engine?.setParameters("{\"rtc.video.high_low_video_ratio_enabled\": false}")
        }
        engine?.setDualStreamModeEx(.enableSimulcastStream, streamConfig: simulcastConfig, connection: connection)
    }
    
    // 预设模式
    private func _presetValuesWith(encodeSize: ShowAgoraVideoDimensions, fps: AgoraVideoFrameRate, bitRate: Float, h265On: Bool) {
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
        let net = netCondition
        let performance = performanceMode
        
        rtcParam.suggested = true
        if (machine == .high && net == .good && performance == .smooth && showMode == .single) {
            // 高端机，好网，清晰，单播
            _presetValuesWith(encodeSize: ._1080x1920, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .high && net == .good && performance == .fluent && showMode == .single) {
            // 高端机，好网，流畅，单播
            _presetValuesWith(encodeSize: ._1080x1920, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(540, 960), fps: 15, bitrate: 1100, svc: false)
        } else if (machine == .high && net == .bad && performance == .smooth && showMode == .single) {
            // 高端机，弱网，清晰，单播
            _presetValuesWith(encodeSize: ._1080x1920, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .high && net == .bad && performance == .fluent && showMode == .single) {
            // 高端机，弱网，流畅，单播
            _presetValuesWith(encodeSize: ._1080x1920, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, bitrate: 748, svc: true)
        } else if (machine == .medium && net == .good && performance == .smooth && showMode == .single) {
            // 中端机，好网，清晰，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .medium && net == .good && performance == .fluent && showMode == .single) {
            // 中端机，好网，流畅，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, bitrate: 680, svc: false)
        } else if (machine == .medium && net == .bad && performance == .smooth && showMode == .single) {
            // 中端机，弱网，清晰，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .medium && net == .bad && performance == .fluent && showMode == .single) {
            // 中端机，弱网，流畅，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, bitrate: 748, svc: true)
        } else if (machine == .low && net == .good && performance == .smooth && showMode == .single) {
            // 低端机，好网，清晰，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .low && net == .good && performance == .fluent && showMode == .single) {
            // 低端机，好网，流畅，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, bitrate: 680, svc: false)
        } else if (machine == .low && net == .bad && performance == .smooth && showMode == .single) {
            // 低端机，弱网，清晰，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .low && net == .bad && performance == .fluent && showMode == .single) {
            // 低端机，弱网，流畅，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, bitrate: 748, svc: true)
        }
        // pk
        else if (machine == .high && net == .good && performance == .smooth && showMode == .pk) {
            // 高端机，好网，清晰，pk
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .high && net == .good && performance == .fluent && showMode == .pk) {
            // 高端机，好网，流畅，pk
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, bitrate: 680, svc: false)
        } else if (machine == .high && net == .bad && performance == .smooth && showMode == .pk) {
            // 高端机，弱网，清晰，pk
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .high && net == .bad && performance == .fluent && showMode == .pk) {
            // 高端机，弱网，流畅，pk
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, bitrate: 680, svc: false)
        } else if (machine == .medium && net == .good && performance == .smooth && showMode == .pk) {
            // 中端机，好网，清晰，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .medium && net == .good && performance == .fluent && showMode == .pk) {
            // 中端机，好网，流畅，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, bitrate: 680, svc: false)
        } else if (machine == .medium && net == .bad && performance == .smooth && showMode == .pk) {
            // 中端机，弱网，清晰，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .medium && net == .bad && performance == .fluent && showMode == .pk) {
            // 中端机，弱网，流畅，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, bitrate: 680, svc: false)
        } else if (machine == .low && net == .good && performance == .smooth && showMode == .pk) {
            // 低端机，好网，清晰，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .low && net == .good && performance == .fluent && showMode == .pk) {
            // 低端机，好网，流畅，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, bitrate: 680, svc: false)
        } else if (machine == .low && net == .bad && performance == .smooth && showMode == .pk) {
            // 低端机，弱网，清晰，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .low && net == .bad && performance == .fluent && showMode == .pk) {
            // 低端机，弱网，流畅，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, bitrate: 680, svc: false)
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
        case .SR:
            break
        case .BFrame:
           break
        case .videoEncodeSize:
            let indexValue = key.intValue
            let dimensions = ShowAgoraVideoDimensions.values()
            let index = indexValue % dimensions.count
            let size = dimensions[index]
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
            encoderConfig.bitrate = Int(sliderValue)
            if let currentChannelId = currentChannelId {
                updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)
            }else{
                engine?.setVideoEncoderConfiguration(encoderConfig)
            }
        case .FPS:
            let indexValue = key.intValue
            let index = indexValue % fpsItems.count
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
        }
    }

}
// MARK: - Presetting options
extension ShowAgoraKitManager {
    // 预设值：网络状况
    enum NetCondition: Int {
        // 好
        case good
        // 差
        case bad
    }
    
    // 预设值：性能策略
    enum PerformanceMode: Int {
        // 清晰策略
        case smooth
        // 流畅策略
        case fluent
    }
    
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
