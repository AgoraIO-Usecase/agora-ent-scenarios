//
//  ShowAgoraKitManager+Setting.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/12/5.
//

import Foundation
import AgoraRtcKit

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
    var simulcast = false
    var pvc = false
    var svc = false
}

// MARK: - Extension
extension ShowAgoraKitManager {
    
    private var dimensionsItems: [CGSize] {
        ShowAgoraVideoDimensions.allCases.map({$0.sizeValue})
    }
    
    // 默认设置
    func defaultSetting() {
        // 默认音量设置
        ShowSettingKey.recordingSignalVolume.writeValue(80)
        ShowSettingKey.musincVolume.writeValue(30)
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
        updateSettingForkey(.FPS)
        updateSettingForkey(.videoBitRate)
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
            engine?.setParameters("{\"rtc.video.sr_max_wh\":\(921600)}")
            // enabled要放在srType之后 否则修改超分倍数可能不会立即生效
            engine?.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(isOn), \"mode\": 2}}")
        }
    }
    
    /// 设置超分 不保存数据
    /// - Parameters:
    ///   - isOn: 开关
    func setSuperResolutionOn(_ isOn: Bool) {
        // 避免重复设置
        if isOn == self.rtcParam.sr {
            return
        }
        self.rtcParam.sr = isOn
        engine?.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(false), \"mode\": 2}}")
        engine?.setParameters("{\"rtc.video.sr_max_wh\":\(921598)}")
        // enabled要放在srType之后 否则修改超分倍数可能不会立即生效
        engine?.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(isOn), \"mode\": 2}}")
    }
    /** 设置小流参数
     */
    private func setSimulcastStream(isOn: Bool, dimensions: CGSize = CGSizeMake(360, 640), fps: Int32 = 5, svc: Bool = false) {
        rtcParam.simulcast = isOn
        rtcParam.svc = svc
        guard isOn else {// 关小流
            engine?.enableDualStreamMode(false)
            return
        }
        // 开启并设置小流
        let simulcastStreamConfig = AgoraSimulcastStreamConfig()
        simulcastStreamConfig.dimensions = dimensions
        simulcastStreamConfig.framerate = fps
        engine?.enableDualStreamMode(true, streamConfig: simulcastStreamConfig)
        // 小流SVC 开关
        if (svc) {
            engine?.setParameters("\"che.video.minor_stream_num_temporal_layers\": 2")
            engine?.setParameters("\"rtc.video.high_low_video_ratio_enabled\": true")
        } else {
            engine?.setParameters("\"rtc.video.high_low_video_ratio_enabled\": false")
        }
    }
    
    /// 选择采集分辨率
    /// - Parameter index: 索引
    func selectCaptureVideoDimensions(index: Int) {
//        setCaptureVideoDimensions(captureDimensionsItems[index])
//        ShowSettingKey.captureVideoSize.writeValue(index)
    }
    
    // 预设模式
    private func _presetValuesWith(encodeSize: ShowAgoraVideoDimensions, fps: AgoraVideoFrameRate, bitRate: Float, h265On: Bool) {
        ShowSettingKey.videoEncodeSize.writeValue(dimensionsItems.firstIndex(of: encodeSize.sizeValue))
        ShowSettingKey.FPS.writeValue(fpsItems.firstIndex(of: fps))
        ShowSettingKey.videoBitRate.writeValue(bitRate)
        ShowSettingKey.H265.writeValue(h265On)
        ShowSettingKey.lowlightEnhance.writeValue(false)
        ShowSettingKey.colorEnhance.writeValue(false)
        ShowSettingKey.videoDenoiser.writeValue(false)
        ShowSettingKey.PVC.writeValue(true)
        ShowSettingKey.SR.writeValue(true)
        
        updateSettingForkey(.videoEncodeSize)
        updateSettingForkey(.videoBitRate)
        updateSettingForkey(.FPS)
        updateSettingForkey(.H265)
        updateSettingForkey(.lowlightEnhance)
        updateSettingForkey(.colorEnhance)
        updateSettingForkey(.videoDenoiser)
        updateSettingForkey(.PVC)
        updateSettingForkey(.SR)
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
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(540, 960), fps: 15, svc: false)
        } else if (machine == .high && net == .good && performance == .fluent && showMode == .single) {
            // 高端机，好网，流畅，单播
            _presetValuesWith(encodeSize: ._1080x1920, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .high && net == .bad && performance == .smooth && showMode == .single) {
            // 高端机，弱网，清晰，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, svc: true)
        } else if (machine == .high && net == .bad && performance == .fluent && showMode == .single) {
            // 高端机，弱网，流畅，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .medium && net == .good && performance == .smooth && showMode == .single) {
            // 中端机，好网，清晰，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, svc: false)
        } else if (machine == .medium && net == .good && performance == .fluent && showMode == .single) {
            // 中端机，好网，流畅，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .medium && net == .bad && performance == .smooth && showMode == .single) {
            // 中端机，弱网，清晰，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, svc: true)
        } else if (machine == .medium && net == .bad && performance == .fluent && showMode == .single) {
            // 中端机，弱网，流畅，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .low && net == .good && performance == .smooth && showMode == .single) {
            // 低端机，好网，清晰，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, svc: false)
        } else if (machine == .low && net == .good && performance == .fluent && showMode == .single) {
            // 低端机，好网，流畅，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .low && net == .bad && performance == .smooth && showMode == .single) {
            // 低端机，弱网，清晰，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, svc: true)
        } else if (machine == .low && net == .bad && performance == .fluent && showMode == .single) {
            // 低端机，弱网，流畅，单播
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        }
        // pk
        else if (machine == .high && net == .good && performance == .smooth && showMode == .pk) {
            // 高端机，好网，清晰，pk
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, svc: false)
        } else if (machine == .high && net == .good && performance == .fluent && showMode == .pk) {
            // 高端机，好网，流畅，pk
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .high && net == .bad && performance == .smooth && showMode == .pk) {
            // 高端机，弱网，清晰，pk
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, svc: true)
        } else if (machine == .high && net == .bad && performance == .fluent && showMode == .pk) {
            // 高端机，弱网，流畅，pk
            _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .medium && net == .good && performance == .smooth && showMode == .pk) {
            // 中端机，好网，清晰，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, svc: false)
        } else if (machine == .medium && net == .good && performance == .fluent && showMode == .pk) {
            // 中端机，好网，流畅，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .medium && net == .bad && performance == .smooth && showMode == .pk) {
            // 中端机，弱网，清晰，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, svc: true)
        } else if (machine == .medium && net == .bad && performance == .fluent && showMode == .pk) {
            // 中端机，弱网，流畅，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .low && net == .good && performance == .smooth && showMode == .pk) {
            // 低端机，好网，清晰，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, svc: false)
        } else if (machine == .low && net == .good && performance == .fluent && showMode == .pk) {
            // 低端机，好网，流畅，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
        } else if (machine == .low && net == .bad && performance == .smooth && showMode == .pk) {
            // 低端机，弱网，清晰，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: true, dimensions: CGSizeMake(360, 640), fps: 15, svc: true)
        } else if (machine == .low && net == .bad && performance == .fluent && showMode == .pk) {
            // 低端机，弱网，流畅，pk
            _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 0, h265On: true)
            setSimulcastStream(isOn: false)
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
            rtcParam.pvc = isOn
            engine?.setParameters("{\"rtc.video.enable_pvc\":\(isOn)}")
        case .SR:
            rtcParam.sr = isOn
            setSuperResolutionOn(isOn)
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
        case high
        case medium
        
        func description() -> String {
            switch self {
            case .high:     return "show_setting_device_level_high".show_localized
            case .medium:   return "show_setting_device_level_mediu".show_localized
            case .low:      return "show_setting_device_level_low".show_localized
            }
        }
    }
}
