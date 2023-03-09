//
//  ShowAgoraKitManager+DebugSetting.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/2/11.
//
import Foundation
import AgoraRtcKit


enum ShowDebug1TFSettingKey: String {
    
    case captureFrameRate = "采集帧率"
    case encodeFrameRate = "编码帧率"
    case bitRate = "码率"
    
    var unit: String {
        switch self {
        case .captureFrameRate:
            return "fps"
        case .encodeFrameRate:
            return "fps"
        case .bitRate:
            return "kbps"
        }
    }
}

enum ShowDebug2TFSettingKey: String {
    case captureVideoSize = "采集分辨率"
    case encodeVideoSize = "编码分辨率"
    case exposureRange = "曝光区域"
    case colorSpace = "颜色空间"
    
    var separator: String {
        switch self {
        case .captureVideoSize:
            return "x"
        case .encodeVideoSize:
            return "x"
        case .exposureRange:
            return "x"
        case .colorSpace:
            return "/"
        }
    }
}

extension ShowAgoraKitManager {
    
    private var debugDimensionsItems: [CGSize] {
        ShowAgoraVideoDimensions.allCases.map({$0.sizeValue})
    }
    
    private var debugCaptureDimensionsItems: [CGSize] {
        ShowAgoraCaptureVideoDimensions.allCases.map({$0.sizeValue})
    }
    
    private var debugEncodeItems: [Bool] {
        ShowAgoraEncode.allCases.map({$0.encodeValue})
    }
    
    private var debugCodeCTypeItems: [Int] {
        ShowAgoraCodeCType.allCases.map({$0.typeValue})
    }
    
    private var debugRenderModeItems: [AgoraVideoRenderMode] {
        ShowAgoraRenderMode.allCases.map({$0.modeValue})
    }
    
    private var debugSrTypeItems: [SRType] {
        ShowAgoraSRType.allCases.map({$0.typeValue})
    }
    
    func debugDefaultBroadcastorSetting() {
        captureConfig.dimensions = CGSize(width: 720, height: 1280)
        captureConfig.frameRate = 15
        updateCameraCaptureConfiguration()
        
        videoEncoderConfig.dimensions = CGSize(width: 720, height: 1280)
        videoEncoderConfig.frameRate = .fps15
        videoEncoderConfig.bitrate = 1800
        agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        
        setExposureRange()
        setColorSpace()
        
        ShowDebugSettingKey.debugPVC.writeValue(false)
        ShowDebugSettingKey.focusFace.writeValue(false)
        ShowDebugSettingKey.encode.writeValue(0)
        ShowDebugSettingKey.codeCType.writeValue(0)
        ShowDebugSettingKey.mirror.writeValue(false)
        ShowDebugSettingKey.renderMode.writeValue(0)
        ShowDebugSettingKey.colorEnhance.writeValue(false)
        ShowDebugSettingKey.lowlightEnhance.writeValue(false)
        ShowDebugSettingKey.videoDenoiser.writeValue(false)
        
        updateSettingForDebugkey(.debugPVC)
        updateSettingForDebugkey(.focusFace)
        updateSettingForDebugkey(.encode)
        updateSettingForDebugkey(.codeCType)
        updateSettingForDebugkey(.mirror)
        updateSettingForDebugkey(.renderMode)
        updateSettingForDebugkey(.colorEnhance)
        updateSettingForDebugkey(.lowlightEnhance)
        updateSettingForDebugkey(.videoDenoiser)
    }
    
    func debugDefaultAudienceSetting() {
        ShowDebugSettingKey.debugSR.writeValue(false)
        ShowDebugSettingKey.debugSrType.writeValue(0)
        
        updateSettingForDebugkey(.debugSR)
        updateSettingForDebugkey(.debugSrType)
    }
    
    func debug1TFModelForKey(_ key: ShowDebug1TFSettingKey) -> ShowDebug1TFModel {
        var originalValue = ""
        switch key {
        case .captureFrameRate:
            originalValue = "\(captureConfig.frameRate)"
        case .encodeFrameRate:
            originalValue = "\(videoEncoderConfig.frameRate.rawValue)"
        case .bitRate:
            originalValue = "\(videoEncoderConfig.bitrate)"
        }
        return ShowDebug1TFModel(title: key.rawValue, tfText: originalValue, unitText: key.unit)
    }
    
    func debug2TFModelForKey(_ key: ShowDebug2TFSettingKey) -> ShowDebug2TFModel{
        var text1 = "", text2 = ""
        switch key {
        case .captureVideoSize:
            text1 = "\(Int(captureConfig.dimensions.width))"
            text2 = "\(Int(captureConfig.dimensions.height))"
        case .encodeVideoSize:
            text1 = "\(Int(videoEncoderConfig.dimensions.width))"
            text2 = "\(Int(videoEncoderConfig.dimensions.height))"
        case .exposureRange:
            if let exposureRangeX = exposureRangeX {
                text1 = "\(exposureRangeX)"
            }
            if let exposureRangeY = exposureRangeY {
                text2 = "\(exposureRangeY)"
            }
        case .colorSpace:
            if let videoFullrangeExt = videoFullrangeExt {
                text1 = "\(videoFullrangeExt)"
            }
            if let matrixCoefficientsExt = matrixCoefficientsExt {
                text2 = "\(matrixCoefficientsExt)"
            }
        }
        return ShowDebug2TFModel(title: key.rawValue, tf1Text: text1, tf2Text: text2, separatorText: key.separator)
    }
    
    func updateDebugProfileFor1TFMode(_ model: ShowDebug1TFModel) {
        guard let text = model.tfText else { return }
        guard let title = model.title, let key =  ShowDebug1TFSettingKey(rawValue: title) else { return }
        switch key {
        case .captureFrameRate:
            guard let value = Int32(text) else {
                showLogger.info("***Debug*** 采集帧率参数为空 ")
                return
            }
            captureConfig.frameRate = value
            updateCameraCaptureConfiguration()
            showLogger.info("***Debug*** setCameraCapturerConfiguration.captureFrameRate = \(captureConfig.frameRate) ")
        case .encodeFrameRate:
            guard let value = Int(text), let fps = AgoraVideoFrameRate(rawValue: value) else {
                showLogger.info("***Debug*** 编码帧率参数为空 ")
                return
            }
            videoEncoderConfig.frameRate = fps
            agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
            showLogger.info("***Debug*** setVideoEncoderConfiguration.encodeFrameRate = \(videoEncoderConfig.frameRate) ")
        case .bitRate:
            guard let value = Int(text) else {
                showLogger.info("***Debug*** 码率参数为空")
                return
            }
            videoEncoderConfig.bitrate = value
            agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
            showLogger.info("***Debug*** setVideoEncoderConfiguration.bitrate = \(videoEncoderConfig.bitrate) ")
        }
    }
    
    func updateDebugProfileFor2TFModel(_ model: ShowDebug2TFModel) {
        guard let title = model.title, let key =  ShowDebug2TFSettingKey(rawValue: title) else { return }
        guard let text1 = model.tf1Text, let text2 = model.tf2Text else { return }
        guard let value1 = Int(text1), let value2 = Int(text2) else {return}
        guard value1 > 0, value2 > 0 else { return }
        switch key {
        case .captureVideoSize:
            captureConfig.dimensions = CGSize(width: value1, height: value2)
            updateCameraCaptureConfiguration()
            showLogger.info("***Debug*** setCameraCapturerConfiguration.captureVideoSize = \(captureConfig.dimensions) ")
        case .encodeVideoSize:
            videoEncoderConfig.dimensions = CGSize(width: value1, height: value2)
            agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
            showLogger.info("***Debug*** setVideoEncoderConfiguration.encodeVideoSize = \(videoEncoderConfig.dimensions) ")
        case .exposureRange:
            exposureRangeX = value1
            exposureRangeY = value2
            setExposureRange()
        case .colorSpace:
            videoFullrangeExt = value1
            matrixCoefficientsExt = value2
            setColorSpace()
        }
    }
    
    /// 更新设置
    /// - Parameter key: 要更新的key
    func updateSettingForDebugkey(_ key: ShowDebugSettingKey, currentChannelId:String? = nil) {
        let isOn = key.boolValue
        let indexValue = key.intValue
        
        switch key {
        case .lowlightEnhance:
            agoraKit.setLowlightEnhanceOptions(isOn, options: AgoraLowlightEnhanceOptions())
        case .colorEnhance:
            agoraKit.setColorEnhanceOptions(isOn, options: AgoraColorEnhanceOptions())
        case .videoDenoiser:
            agoraKit.setVideoDenoiserOptions(isOn, options: AgoraVideoDenoiserOptions())
        case .PVC:
            agoraKit.setParameters("{\"rtc.video.enable_pvc\":\(isOn)}")
        case .focusFace:
            agoraKit.setCameraAutoFocusFaceModeEnabled(isOn)
            showLogger.info("***Debug*** setCameraAutoFocusFaceModeEnabled  \(isOn)")
        case .encode:
            let index = indexValue % debugEncodeItems.count
            agoraKit.setParameters("{\"engine.video.enable_hw_encoder\":\"\(debugEncodeItems[index])\"}")
            showLogger.info("***Debug*** engine.video.enable_hw_encoder  \(debugEncodeItems[index])")
        case .codeCType:
            let index = indexValue % debugCodeCTypeItems.count
            agoraKit.setParameters("{\"engine.video.codec_type\":\"\(debugCodeCTypeItems[index])\"}")
            showLogger.info("***Debug*** engine.video.codec_type  \(debugCodeCTypeItems[index])")

        case .mirror, .renderMode:
            let index = ShowDebugSettingKey.renderMode.intValue % debugRenderModeItems.count
            let mirrorIsOn = ShowDebugSettingKey.mirror.boolValue
            agoraKit.setLocalRenderMode(debugRenderModeItems[index], mirror: mirrorIsOn ? .enabled : .disabled)
            showLogger.info("***Debug*** setLocalRenderMode  mirror = \(mirrorIsOn ? AgoraVideoMirrorMode.enabled : AgoraVideoMirrorMode.disabled), rendermode = \(debugRenderModeItems[index])")
        case .debugSR, .debugSrType:
            let srIsOn = ShowDebugSettingKey.debugSR.boolValue
            let index = ShowDebugSettingKey.debugSrType.intValue % debugSrTypeItems.count
            setSuperResolutionOn(srIsOn, srType: debugSrTypeItems[index])
            showLogger.info("***Debug*** setSuperResolutionOn  srIsOn = \(srIsOn), srType = \(debugSrTypeItems[index])")
        case .debugPVC:
            agoraKit.setParameters("{\"rtc.video.enable_pvc\":\(isOn)}")
            showLogger.info("***Debug*** rtc.video.enable_pvc \(isOn)")
        }
    }
}

extension ShowAgoraKitManager {
    
    private func setExposureRange() {
        if let x = exposureRangeX, let y = exposureRangeY {
            agoraKit.setCameraExposurePosition(CGPoint(x: x, y: y))
            showLogger.info("***Debug*** setCameraExposurePosition = \(CGPoint(x: x, y: y)) ")
        }
    }
    
    private func setColorSpace(){
        if let v1 = videoFullrangeExt, let v2 = matrixCoefficientsExt {
            agoraKit.setParameters("{\"che.video.videoFullrangeExt\":\(v1)}")
            agoraKit.setParameters("{\"che.video.matrixCoefficientsExt\":\(v2)}")
            showLogger.info("***Debug*** {\"che.video.videoFullrangeExt\":\(v1)} {\"che.video.matrixCoefficientsExt\":\(v2)} ")
        }
    }
}

private let userDefaultKeyTag = "debug"

enum ShowDebugSettingKey: String, CaseIterable {
    
    enum KeyType {
        case aSwitch
        case segment
        case slider
        case label
    }
    
    case lowlightEnhance        // 暗光增强
    case colorEnhance           // 色彩增强
    case videoDenoiser          // 降噪
    case PVC                    // pvc
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
        case .PVC:
            return "show_advance_setting_PVC_title".show_localized
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
        case .PVC:
            return .aSwitch
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
        default:
            return ""
        }
    }
    
    // 选项
    var items: [String] {
        switch self {
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
        return UserDefaults.standard.bool(forKey: self.rawValue + userDefaultKeyTag)
    }
    
    var floatValue: Float {
        return UserDefaults.standard.float(forKey: self.rawValue + userDefaultKeyTag)
    }
    
    var intValue: Int {
        return UserDefaults.standard.integer(forKey: self.rawValue + userDefaultKeyTag)
    }
    
    func writeValue(_ value: Any?){
        UserDefaults.standard.set(value, forKey: self.rawValue + userDefaultKeyTag)
        UserDefaults.standard.synchronize()
    }
}
