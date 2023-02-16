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
        
        updateSettingForkey(.debugPVC)
        updateSettingForkey(.focusFace)
        updateSettingForkey(.encode)
        updateSettingForkey(.codeCType)
        updateSettingForkey(.mirror)
        updateSettingForkey(.renderMode)
        updateSettingForkey(.colorEnhance)
        updateSettingForkey(.lowlightEnhance)
        updateSettingForkey(.videoDenoiser)
    }
    
    func debugDefaultAudienceSetting() {
        updateSettingForkey(.debugSR)
        updateSettingForkey(.debugSrType)
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
