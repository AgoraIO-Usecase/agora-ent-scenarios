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
        case none = -1
        case x1 = 6
        case x1_33 = 7
        case x1_5 = 8
        case x2 = 3
        case x_sharpen = 11
    }
    
    private var dimensionsItems: [CGSize] {
        ShowAgoraVideoDimensions.allCases.map({$0.sizeValue})
    }
    
    private var captureDimensionsItems: [CGSize] {
        ShowAgoraCaptureVideoDimensions.allCases.map({$0.sizeValue})
    }
    
    private var encodeItems: [Bool] {
        ShowAgoraEncode.allCases.map({$0.encodeValue})
    }
    
    private var codeCTypeItems: [Int] {
        ShowAgoraCodeCType.allCases.map({$0.typeValue})
    }
    
    private var renderModeItems: [AgoraVideoRenderMode] {
        ShowAgoraRenderMode.allCases.map({$0.modeValue})
    }
    
    private var debugSrTypeItems: [SRType] {
        ShowAgoraSRType.allCases.map({$0.typeValue})
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
//        agoraKit.enableExtension("agora_video_filters_super_resolution", "super_resolution")
//        ShowSettingKey.SR.writeValue(false) // 默认关闭sr
        let hasOpened = UserDefaults.standard.bool(forKey: hasOpenedKey)
        // 第一次进入房间的时候设置
        if hasOpened == false {
            updatePresetForType(presetType ?? .show_medium, mode: .single)
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
            agoraKit.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(false), \"mode\": 2}}")
        }else{
            agoraKit.setParameters("{\"rtc.video.sr_type\":\(srType.rawValue)}")
            agoraKit.setParameters("{\"rtc.video.sr_max_wh\":\(921600)}")
            // enabled要放在srType之后 否则修改超分倍数可能不会立即生效
            agoraKit.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(isOn), \"mode\": 2}}")
        }
        showLogger.info("----- setSuperResolutionOn\(ShowSettingKey.SR.boolValue)  srType: \(srType)")
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
                    srType = .x1_5
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
                    srType = .x1_5
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
        setCaptureVideoDimensions(captureDimensionsItems[index])
        ShowSettingKey.captureVideoSize.writeValue(index)
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
        }else {
            videoEncoderConfig.dimensions = encodeSize.sizeValue
            videoEncoderConfig.frameRate = fps
            videoEncoderConfig.bitrate = Int(bitRate)
            captureConfig.dimensions = captureSize.sizeValue
            captureConfig.frameRate = Int32(fps.rawValue)
//            agoraKit.setCameraCapturerConfiguration(captureConfig)
            updateCameraCaptureConfiguration()
            agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
            setH265On(h265On)
        }
       
    }
    
    /// 设置观众端画质增强
    private func _setQualityEnable(_ isOn: Bool, uid: UInt?){
        /*
        if let uid = uid {
            agoraKit.enableRemoteSuperResolution(uid, enable: isOn)
        }
        ShowSettingKey.SR.writeValue(isOn)
         */
//        setSuperResolutionOn(isOn, srType: srType)
        ShowSettingKey.SR.writeValue(isOn)
    }
    
    func updatePresetForType(_ type: ShowPresetType, mode: ShowMode,uid: UInt? = nil) {
        switch type {
        case .unknown:
            break
        case .show_low:
            switch mode {
            case .single:
                _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 1461, h265On: false, captureSize: ._1080P)
            case .pk:
                _presetValuesWith(encodeSize: ._360x640, fps: .fps15, bitRate: 700, h265On: false, captureSize: ._720P)
            }
            broadcastorMachineType = .low
        case .show_medium:
            switch mode {
            case .single:
                _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 1800, h265On: true, captureSize: ._720P)
            case .pk:
                _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 800, h265On: true, captureSize: ._720P)
            }
            broadcastorMachineType = .medium
        case .show_high:
            
            switch mode {
            case .single:
                _presetValuesWith(encodeSize: ._720x1280, fps: .fps24, bitRate: 2099, h265On: true, captureSize: ._720P)
            case .pk:
                _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 800, h265On: true, captureSize: ._720P)
            }
            broadcastorMachineType = .high
        case .quality_low:
            _setQualityEnable(false,uid: uid)
        case .quality_medium:
            _setQualityEnable(true, uid: uid)
        case .quality_high:
            _setQualityEnable(true, uid: uid)
        case .base_low:
            _setQualityEnable(false,uid: uid)
        case .base_medium:
            _setQualityEnable(false,uid: uid)
        case .base_high:
            _setQualityEnable(false,uid: uid)
        }
    }
    
    /// 更新配置信息
    /// - Parameters:
    ///   - mode: 秀场交互类型
    func updateVideoProfileForMode(_ mode: ShowMode) {
        switch broadcastorMachineType {
        case .unknown:
            break
        case .low:
            switch mode {
            case .single:
                _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 1461, h265On: false, captureSize: ._1080P, cache: false)
            case .pk:
                _presetValuesWith(encodeSize: ._360x640, fps: .fps15, bitRate: 700, h265On: false, captureSize: ._720P, cache: false)
            }
            
        case .medium:
            switch mode {
            case .single:
                _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 1800, h265On: true, captureSize: ._720P, cache: false)
            case .pk:
                _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 800, h265On: true, captureSize: ._720P, cache: false)
            }
            
        case .high:
            switch mode {
            case .single:
                _presetValuesWith(encodeSize: ._720x1280, fps: .fps15, bitRate: 2099, h265On: true, captureSize: ._720P, cache: false)
            case .pk:
                _presetValuesWith(encodeSize: ._540x960, fps: .fps15, bitRate: 800, h265On: true, captureSize: ._720P, cache: false)
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
//            agoraKit.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":\(isOn), \"mode\": 2}}")
            setSuperResolutionOn(isOn)
        case .BFrame:
            
           break
        case .videoEncodeSize:
            let index = indexValue % dimensionsItems.count
            videoEncoderConfig.dimensions = dimensionsItems[index]
            if let currentChannelId = currentChannelId{
                updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)
            }else{
                agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
            }
        case .videoBitRate:
            videoEncoderConfig.bitrate = Int(sliderValue)
            if let currentChannelId = currentChannelId {
                updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)
            }else{
                agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
            }
        case .FPS:
            let index = indexValue % fpsItems.count
            videoEncoderConfig.frameRate = fpsItems[index]
            if let currentChannelId = currentChannelId {
                updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)
            }else{
                agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
            }
            // 采集帧率
            captureConfig.frameRate = Int32(fpsItems[index].rawValue)
//            agoraKit.setCameraCapturerConfiguration(captureConfig)
            updateCameraCaptureConfiguration()
            
        case .H265:
            setH265On(isOn)
        case .earmonitoring:
            agoraKit.enable(inEarMonitoring: isOn)
        case .recordingSignalVolume:
            agoraKit.adjustRecordingSignalVolume(Int(sliderValue))
        case .musincVolume:
            agoraKit.adjustAudioMixingVolume(Int(sliderValue))
        case .audioBitRate:
            break
        case .captureVideoSize:
            let index = indexValue % captureDimensionsItems.count
            setCaptureVideoDimensions(captureDimensionsItems[index])
        case .captureFrameRate:
            let index = indexValue % fpsItems.count
            captureConfig.frameRate = Int32(fpsItems[index].rawValue)
            updateCameraCaptureConfiguration()
        case .focusFace:
            agoraKit.setCameraAutoFocusFaceModeEnabled(isOn)
            showLogger.info("***Debug*** setCameraAutoFocusFaceModeEnabled  \(isOn)")
        case .encode:
            let index = indexValue % encodeItems.count
            agoraKit.setParameters("{\"engine.video.enable_hw_encoder\":\"\(encodeItems[index])\"}")
            showLogger.info("***Debug*** engine.video.enable_hw_encoder  \(encodeItems[index])")
        case .codeCType:
            let index = indexValue % codeCTypeItems.count
            agoraKit.setParameters("{\"engine.video.codec_type\":\"\(codeCTypeItems[index])\"}")
            showLogger.info("***Debug*** engine.video.codec_type  \(codeCTypeItems[index])")

        case .mirror, .renderMode:
            let index = ShowSettingKey.renderMode.intValue % renderModeItems.count
            let mirrorIsOn = ShowSettingKey.mirror.boolValue
            agoraKit.setLocalRenderMode(renderModeItems[index], mirror: mirrorIsOn ? .enabled : .disabled)
            showLogger.info("***Debug*** setLocalRenderMode  mirror = \(mirrorIsOn ? AgoraVideoMirrorMode.enabled : AgoraVideoMirrorMode.disabled), rendermode = \(renderModeItems[index])")
        case .debugSR, .debugSrType:
            let srIsOn = ShowSettingKey.debugSR.boolValue
            let index = ShowSettingKey.debugSrType.intValue % debugSrTypeItems.count
            setSuperResolutionOn(srIsOn, srType: debugSrTypeItems[index])
            showLogger.info("***Debug*** setSuperResolutionOn  srIsOn = \(srIsOn), srType = \(debugSrTypeItems[index])")
        case .debugPVC:
            agoraKit.setParameters("{\"rtc.video.enable_pvc\":\(isOn)}")
            showLogger.info("***Debug*** rtc.video.enable_pvc \(isOn)")
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
