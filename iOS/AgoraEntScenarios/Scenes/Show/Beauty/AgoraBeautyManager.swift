//
//  AgoraBeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/11/10.
//  Refactored to match Android implementation
//

import Foundation
import AgoraRtcKit

// MARK: - Model Type Enum

/// Model type constants for beauty algorithm selection
enum AgoraBeautyModelType: Int {
    case adaptive = 0  // Adaptive mode: SDK automatically selects model based on device score
    case large = 1     // Large model: Force all devices to use large model
    case small = 2     // Small model: Force all devices to use small model
}

// MARK: - Mirror Mode Enum

/// Mirror mode for camera (match Android implementation)
enum MirrorMode {
    case mirrorLocalRemote  // Both local and remote mirrored
    case mirrorLocalOnly    // Only local mirrored
    case mirrorRemoteOnly   // Only remote mirrored
    case mirrorNone         // Neither local nor remote mirrored
}

// MARK: - Camera Config

/// Camera configuration for mirror handling (match Android implementation)
struct CameraConfig {
    let frontMirror: MirrorMode
    let backMirror: MirrorMode
    
    init(frontMirror: MirrorMode = .mirrorLocalRemote, backMirror: MirrorMode = .mirrorNone) {
        self.frontMirror = frontMirror
        self.backMirror = backMirror
    }
}

// MARK: - AgoraBeautyManager

class AgoraBeautyManager: NSObject {
    
    // MARK: - Singleton
    
    private static var _sharedManager: AgoraBeautyManager?
    static var shareManager: AgoraBeautyManager {
        get {
            if let sharedManager = _sharedManager { return sharedManager }
            let sharedManager = AgoraBeautyManager()
            _sharedManager = sharedManager
            return sharedManager
        }
        set {
            _sharedManager = nil
        }
    }
    
    // MARK: - Properties
    
    /// RTC Engine instance (can be set externally for compatibility with BeautyManager)
    var agoraKit: AgoraRtcEngineKit?
    private var beautyEffect: AgoraVideoEffectObject?
    
    // Enable flags (match Android implementation)
    private var beautyEnable = false
    private var filterEnable = false
    private var makeupEnable = false
    private var stickerEnable = false
    
    // Material path management
    private static let assetsName = "AgoraBeautyMaterial"
    private let materialCopyDestPath: String = NSHomeDirectory() + "/Documents/AgoraBeautyMaterial.bundle"
    private var materialPath = ""
    private static var materialCopied = false
    
    // UserDefaults key for model type
    private static let kModelTypeKey = "show_model_type"
    
    // Beauty configuration object (match Android implementation)
    public let beautyConfig = BeautyConfig()
    
    // Camera config for mirror handling (match Android)
    private var cameraConfig = CameraConfig(
        frontMirror: .mirrorLocalRemote,
        backMirror: .mirrorNone
    )
    private var captureMirror = false
    private var renderMirror = false
    private var isFrontCamera = true  // Track current camera position
    private var localVideoRenderMode: AgoraVideoRenderMode = .hidden
    private let mainExecutor = DispatchQueue.main
    
    // Bare face comparison state
    private var savedBeautyState = false
    private var savedFaceShapeState = false
    private var savedMakeupName: String?
    private var savedFilterName: String?
    private var savedStickerName: String?
    
    // Legacy render support
    lazy var render = AgoraBeautyRender()
    
    // MARK: - Initialization
    
    /// Initialize beauty SDK (match Android implementation)
    /// - Parameters:
    ///   - rtcEngine: RTC Engine instance
    ///   - useLocalBeautyResource: Whether to use local resources (reserved for future use)
    /// - Returns: true if initialization succeeds, false otherwise
    @discardableResult
    func initBeautySDK(rtcEngine: AgoraRtcEngineKit, useLocalBeautyResource: Bool = true) -> Bool {
        // Copy material bundle if not already copied
        if !AgoraBeautyManager.materialCopied {
            copyBeautyBundle()
            AgoraBeautyManager.materialCopied = true
        }
        
        materialPath = materialCopyDestPath + "/beauty_material_functional"
        
        self.agoraKit = rtcEngine
        
        // Set model type private parameter before createVideoEffectObject (match Android)
        AgoraBeautyManager.setModelTypeParameter(rtcEngine: rtcEngine)
        
        // Enable extension
        let ret = rtcEngine.enableExtension(
            withVendor: "agora_video_filters_clear_vision",
            extension: "clear_vision",
            enabled: true,
            sourceType: .primaryCamera
        )
        
        if ret != 0 {
            ShowLogger.error("enableExtension failed: errorCode: \(ret)", context: "AgoraBeautyManager")
            self.agoraKit = nil
            return false
        }
        
        // Create VideoEffectObject
        beautyEffect = rtcEngine.createVideoEffectObject(bundlePath: materialPath, sourceType: .primaryCamera)
        
        if beautyEffect == nil {
            ShowLogger.error("Failed to create VideoEffectObject", context: "AgoraBeautyManager")
            self.agoraKit = nil
            return false
        }
        
        // Link beautyConfig to this manager
        beautyConfig.linkToManager(self)
        
        return true
    }
    
    /// Initialize beauty effect (legacy method for compatibility)
    /// This method is called by BeautyManager.configBeautyAPI() in current iOS code
    /// - Parameter rtcEngine: RTC Engine instance
    func initBeautyEffect(rtcEngine: AgoraRtcEngineKit) {
        // Call the new initBeautySDK method
        initBeautySDK(rtcEngine: rtcEngine, useLocalBeautyResource: true)
    }
    
    /// Initialize beauty effect (legacy overload without parameter, for backward compatibility)
    /// Deprecated: Use initBeautyEffect(rtcEngine:) instead
    @available(*, deprecated, message: "Use initBeautyEffect(rtcEngine:) instead")
    func initBeautyEffect() {
        guard let rtcEngine = agoraKit else {
            ShowLogger.error("agoraKit is nil, please set agoraKit property before calling initBeautyEffect()", context: "AgoraBeautyManager")
            return
        }
        initBeautyEffect(rtcEngine: rtcEngine)
    }
    
    /// Print current beauty status for debugging
    func printBeautyStatus() {
        // Status logging removed
    }
    
    /// Uninitialize beauty SDK (match Android implementation)
    func unInitBeautySDK() {
        do {
            enable(false)
            
            // Destroy beautyEffect before disabling extension
            if let effect = beautyEffect {
                agoraKit?.destroyVideoEffectObject(effect)
            }
            
            agoraKit?.enableExtension(
                withVendor: "agora_video_filters_clear_vision",
                extension: "clear_vision",
                enabled: false,
                sourceType: .primaryCamera
            )
        }
        
        agoraKit = nil
        beautyEffect = nil
        beautyEnable = false
        filterEnable = false
        makeupEnable = false
        stickerEnable = false
        beautyConfig.reset()
    }
    
    // MARK: - Enable Control
    
    /// Enable or disable all beauty effects (match Android implementation)
    func enable(_ enable: Bool) {
        if enable {
            enableBeauty(true)
            enableFilter(true)
            enableMakeup(true)
                enableSticker(true)
            beautyConfig.resume()
        } else {
            enableBeauty(false)
            enableFilter(false)
            enableMakeup(false)
            enableSticker(false)
        }
    }
    
    private func enableBeauty(_ enable: Bool) {
        guard let effect = beautyEffect else {
            ShowLogger.error("enableBeauty(\(enable)) failed: beautyEffect is nil", context: "AgoraBeautyManager")
            return
        }
        if enable == beautyEnable {
            return
        }
        
        if enable {
            effect.addOrUpdateVideoEffect(
                nodeId: AgoraVideoEffectNodeId.beauty.rawValue,
                templateName: beautyConfig.beautyName
            )
        } else {
            effect.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.beauty.rawValue)
        }
        
        self.beautyEnable = enable
    }
    
    private func enableFilter(_ enable: Bool) {
        guard let effect = beautyEffect else { return }
        if enable == filterEnable { return }
        
        if enable {
            if let filterName = beautyConfig.filterName {
                effect.addOrUpdateVideoEffect(
                    nodeId: AgoraVideoEffectNodeId.filter.rawValue,
                    templateName: filterName
                )
            }
        } else {
            effect.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.filter.rawValue)
        }
        
        self.filterEnable = enable
    }
    
    private func enableMakeup(_ enable: Bool) {
        guard let effect = beautyEffect else { return }
        if enable == makeupEnable { return }
        
        if enable {
            if let makeupName = beautyConfig.makeupName {
                effect.addOrUpdateVideoEffect(
                    nodeId: AgoraVideoEffectNodeId.styleMakeup.rawValue,
                    templateName: makeupName
                )
            }
        } else {
            effect.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.styleMakeup.rawValue)
        }
        
        self.makeupEnable = enable
    }
    
    private func enableSticker(_ enable: Bool) {
        guard let effect = beautyEffect else { return }
        if enable == stickerEnable { return }
        
        if enable {
            if let stickerName = beautyConfig.stickerName {
                effect.addOrUpdateVideoEffect(
                    nodeId: AgoraVideoEffectNodeId.sticker.rawValue,
                    templateName: stickerName
                )
            }
        } else {
            effect.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.sticker.rawValue)
        }
        
        self.stickerEnable = enable
    }
    
    // MARK: - External Compatibility Layer (Keep existing API signatures)
    
    /// Set beauty parameters (external API - for compatibility)
    /// - Parameters:
    ///   - path: Resource path (reserved, not used)
    ///   - key: Parameter key
    ///   - value: Parameter value (UI range, will be converted internally)
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        if key == nil {
            // Close beauty effect
            beautyConfig.beauty = false
            beautyConfig.faceShape = false
            return
        }
        
        switch key ?? "" {
        // Basic beauty parameters (0-100 UI → 0.0-1.0 SDK)
        case "smoothnessLevel":
            beautyConfig.smoothness = Float(value) / 100.0
        case "lighteningLevel":
            beautyConfig.whitenNatural = Float(value) / 100.0
        case "rednessLevel":
            beautyConfig.redness = Float(value) / 100.0
        case "sharpnessLevel":
            beautyConfig.sharpen = Float(value) / 100.0
            
        // Adjust parameters (-50~50 UI → -1.0~1.0 SDK)
        case "clarity":
            beautyConfig.clarity = Float(value) / 50.0
        case "hue":
            beautyConfig.hue = Float(value) / 50.0
        case "temperature":
            beautyConfig.temperature = Float(value) / 50.0
        case "saturation":
            beautyConfig.saturation = Float(value) / 50.0
        case "brightness":
            beautyConfig.brightness = Float(value) / 50.0
            
        // Face buffing parameters (0-100 UI → 0.0-1.0 SDK)
        case "nasolabialfolds":
            beautyConfig.nasolabialFolds = Float(value) / 100.0
        case "brighteneye":
            beautyConfig.brightenEye = Float(value) / 100.0
        case "darkcircle":
            beautyConfig.darkCircle = Float(value) / 100.0
        case "whitenteeth":
            beautyConfig.whitenTeeth = Float(value) / 100.0
            
        // Face shape parameters (0-100 or -100~100)
        case "headscale":
            beautyConfig.headScale = Int32(round(value))
        case "facecontour":
            beautyConfig.faceContour = Int32(round(value))
        case "facewidth":
            beautyConfig.faceWidth = Int32(round(value))
        case "facelength":
            beautyConfig.faceLength = Int32(round(value * 2.0))  // -50~50 → -100~100
        case "cheekbone":
            beautyConfig.cheekbone = Int32(round(value))
        case "cheek", "shrinkcheek":
            beautyConfig.shrinkCheek = Int32(round(value))
        case "mandible":
            beautyConfig.mandible = Int32(round(value))
        case "chin", "chinlength":
            beautyConfig.chinLength = Int32(round(value * 2.0))  // -50~50 → -100~100
        case "forehead", "hairlineheight":
            beautyConfig.hairlineHeight = Int32(round(value * 2.0))  // -50~50 → -100~100
            
        // Eye parameters
        case "enlargeeye", "eyescale":
            beautyConfig.enlargeEye = Int32(round(value))
        case "eyeposition":
            beautyConfig.eyePosition = Int32(round(value * 2.0))  // -50~50 → -100~100
        case "eyedistance":
            beautyConfig.eyeDistance = Int32(round(value * 2.0))  // -50~50 → -100~100
        case "eyepupil":
            beautyConfig.eyePupil = Int32(round(value))
        case "eyelid":
            beautyConfig.eyeLid = Int32(round(value))
        case "eyeinnercorner":
            beautyConfig.eyeInnercorner = Int32(round(value * 2.0))  // -50~50 → -100~100
        case "eyeoutercorner":
            beautyConfig.eyeOutercorner = Int32(round(value * 2.0))  // -50~50 → -100~100
            
        // Nose parameters
        case "narrownose", "nosewidth":
            beautyConfig.narrowNose = Int32(round(value))
        case "noselength":
            beautyConfig.noseLength = Int32(round(value * 2.0))  // -50~50 → -100~100
        case "nosewing":
            beautyConfig.noseWing = Int32(round(value))
        case "nosebridge":
            beautyConfig.noseBridge = Int32(round(value))
        case "noseroot":
            beautyConfig.noseRoot = Int32(round(value))
        case "nosetip":
            beautyConfig.noseTip = Int32(round(value))
        case "nosegeneral":
            beautyConfig.noseGeneral = Int32(round(value * 2.0))  // -50~50 → -100~100
            
        // Mouth parameters
        case "mouthsize", "mouthscale":
            beautyConfig.mouthSize = Int32(round(value * 2.0))  // -50~50 → -100~100
        case "mouthposition":
            beautyConfig.mouthPosition = Int32(round(value))
        case "mouthsmile":
            beautyConfig.mouthSmile = Int32(round(value))
        case "mouthlip":
            beautyConfig.mouthLip = Int32(round(value))
            
        // Eyebrow parameters
        case "eyebrowposition":
            beautyConfig.eyebrowPosition = Int32(round(value * 2.0))  // -50~50 → -100~100
        case "eyebrowthickness":
            beautyConfig.eyebrowThickness = Int32(round(value * 2.0))  // -50~50 → -100~100
            
        default:
        break
        }
    }
    
    /// Set style makeup (external API - for compatibility)
    func setStyle(path: String?, key: String?, value: CGFloat) {
        if key == nil {
            beautyConfig.makeupName = nil
            return
        } else if key == "init" {
            beautyConfig.makeupName = ""
            return
        }
        beautyConfig.makeupName = key
        beautyConfig.makeupStrength = Float(value) / 100.0
    }
    
    /// Set filter (external API - for compatibility)
    func setFilter(path: String?, key: String?, value: CGFloat) {
        if key == nil {
            beautyConfig.filterName = nil
            return
        } else if key == "init" {
            beautyConfig.filterName = ""
            return
        }
        beautyConfig.filterName = key
        beautyConfig.filterStrength = Float(value) / 100.0
    }
    
    /// Set sticker (external API - for compatibility)
    func setSticker(path: String?, key: String?, value: CGFloat) {
        if key == nil {
            beautyConfig.stickerName = nil
        } else if key == "init" {
            beautyConfig.stickerName = ""
        } else {
            beautyConfig.stickerName = key
        }
    }
    
    // MARK: - Reset Methods
    
    /// Reset beauty effects (external API - for compatibility)
    func reset(datas: [BeautyModel], type: ShowBeautyFaceVCType) {
        switch type {
        case .beauty:
            resetBeauty(datas: datas)
        case .style:
            resetStyle(datas: datas)
        case .filter:
            resetFilter(datas: datas)
        default:
            break
        }
    }
    
    func resetBeauty(datas: [BeautyModel]) {
        // Match Android: enable beauty and faceShape before resetting
        // IMPORTANT: Ensure beautyEnable is true before resetting, otherwise setters will be skipped
        if !beautyEnable {
            enableBeauty(true)
        }
        beautyConfig.beauty = true
        beautyConfig.faceShape = true
        beautyConfig.resetBeauty()
        
        // Update UI data array to reflect reset values
        // Get default values from createAgoraBeautyData()
        let defaultData = BeautyModel.createAgoraBeautyData()
        var defaultValuesByKey: [String: CGFloat] = [:]
        defaultData.forEach { model in
            if let key = model.key {
                defaultValuesByKey[key] = model.value
            }
        }
        
        ShowBeautyFaceVC.beautyData.enumerated().forEach { index, model in
            let wasSelected = model.isSelected
            
            if model.name == "show_beauty_item_none".show_localized {
                model.isSelected = false
            } else if model.name != "show_beauty_item_beauty_reset".show_localized {
                // Update value from default if key exists
                if let key = model.key, let defaultValue = defaultValuesByKey[key] {
                model.value = defaultValue
                }
            }
            
            if model.name != "show_beauty_item_none".show_localized {
                model.isSelected = wasSelected
            }
        }
    }
    
    func resetStyle(datas: [BeautyModel]) {
        beautyConfig.makeupName = nil
    }
    
    func resetFilter(datas: [BeautyModel]) {
        beautyConfig.filterName = nil
    }
    
    func resetSticker(datas: [BeautyModel]) {
        beautyConfig.stickerName = nil
    }
    
    // MARK: - Bare Face Comparison
    
    /// Save current state and disable all beauty effects (match Android implementation)
    func actionBareFace() {
        savedBeautyState = beautyConfig.beauty
        savedFaceShapeState = beautyConfig.faceShape
        savedMakeupName = beautyConfig.makeupName
        savedFilterName = beautyConfig.filterName
        savedStickerName = beautyConfig.stickerName
        
        // Temporarily disable beauty to show original face
        beautyConfig.beauty = false
        beautyConfig.faceShape = false
        beautyConfig.makeupName = nil
        beautyConfig.filterName = nil
        beautyConfig.stickerName = nil
    }
    
    /// Restore saved beauty state (match Android implementation)
    func actionBeauty() {
        beautyConfig.beauty = savedBeautyState
        beautyConfig.faceShape = savedFaceShapeState
        beautyConfig.makeupName = savedMakeupName
        beautyConfig.filterName = savedFilterName
        beautyConfig.stickerName = savedStickerName
    }
    
    // MARK: - Video Setup
    
    /// Setup local video (match Android implementation)
    func setupLocalVideo(view: UIView, renderMode: AgoraVideoRenderMode) -> Int32 {
        localVideoRenderMode = renderMode
        
        let canvas = AgoraRtcVideoCanvas()
        canvas.view = view
        canvas.renderMode = renderMode
        canvas.mirrorMode = .auto
        
        agoraKit?.setupLocalVideo(canvas)
        return 0
    }
    
    /// Process video frame and update mirror if needed (match Android implementation)
    /// Note: In iOS, we use isFrontCamera property to track camera position
    /// This should be updated when switching cameras
    func onCaptureVideoFrame(_ videoFrame: AgoraOutputVideoFrame) -> Bool {
        let isFront = isFrontCamera
        
        // Calculate capture mirror and render mirror based on camera config
        let cMirror: Bool
        let rMirror: Bool
        
        if isFront {
            switch cameraConfig.frontMirror {
            case .mirrorLocalRemote:
                cMirror = true
                rMirror = false
            case .mirrorLocalOnly:
                cMirror = false
                rMirror = true
            case .mirrorRemoteOnly:
                cMirror = true
                rMirror = true
            case .mirrorNone:
                cMirror = false
                rMirror = false
            }
        } else {
            switch cameraConfig.backMirror {
            case .mirrorLocalRemote:
                cMirror = true
                rMirror = false
            case .mirrorLocalOnly:
                cMirror = false
                rMirror = true
            case .mirrorRemoteOnly:
                cMirror = true
                rMirror = true
            case .mirrorNone:
                cMirror = false
                rMirror = false
            }
        }
        
        // Update render mirror if changed
        if renderMirror != rMirror {
            renderMirror = rMirror
            mainExecutor.async { [weak self] in
                self?.agoraKit?.setLocalRenderMode(
                    .hidden,
                    mirror: rMirror ? .enabled : .disabled
                )
            }
        }
        
        captureMirror = cMirror
        return true
    }
    
    /// Get mirror applied status (match Android implementation)
    func getMirrorApplied() -> Bool {
        return captureMirror && !beautyEnable
    }
    
    /// Update camera position (should be called when switching camera)
    func updateCameraPosition(isFront: Bool) {
        isFrontCamera = isFront
    }
    
    // MARK: - Model Type Management
    
    /// Get current model type from UserDefaults
    static func getCurrentModelType() -> AgoraBeautyModelType {
        let rawValue = UserDefaults.standard.integer(forKey: kModelTypeKey)
        return AgoraBeautyModelType(rawValue: rawValue) ?? .adaptive
    }
    
    /// Save model type to UserDefaults
    static func saveModelType(_ modelType: AgoraBeautyModelType) {
        UserDefaults.standard.set(modelType.rawValue, forKey: kModelTypeKey)
        UserDefaults.standard.synchronize()
    }
    
    /// Set model type private parameter (match Android - commented out)
    /// This function controls which beauty algorithm model the SDK uses:
    /// - ADAPTIVE: SDK automatically selects based on device performance (default)
    /// - LARGE: Force large model (high quality, may impact performance)
    /// - SMALL: Force small model (performance optimized, slightly lower quality)
    /// Currently disabled to use SDK's adaptive behavior (Android commit 982bc9484)
    private static func setModelTypeParameter(rtcEngine: AgoraRtcEngineKit) {
        // Match Android commit 982bc9484: Commented out model type parameter setting
        // Let SDK automatically select model type based on device score
        
        // Original implementation (commented out to match Android):
        // let modelType = AgoraBeautyManager.getCurrentModelType()
        // switch modelType {
        // case .large:
        //     // Force all devices to use large model (score = 0)
        //     let ret = rtcEngine.setParameters("{\"che.video.low_alg_score_4_beauty\":0}")
        // case .small:
        //     // Force all devices to use small model (score = 100)
        //     let ret = rtcEngine.setParameters("{\"che.video.low_alg_score_4_beauty\":100}")
        // case .adaptive:
        //     // Adaptive mode: do not set parameter, SDK will automatically select
        // }
    }
    
    // MARK: - Helper Methods
            
    /// Check if beauty SDK is initialized
    func isInitialized() -> Bool {
        return agoraKit != nil && beautyEffect != nil
    }
            
    /// Destroy and clean up resources
    func destroy() {
        unInitBeautySDK()
        AgoraBeautyManager._sharedManager = nil
    }
    
    private func copyBeautyBundle() {
        guard let bundlePath = Bundle.main.path(forResource: AgoraBeautyManager.assetsName, ofType: "bundle") else {
            ShowLogger.error("\(AgoraBeautyManager.assetsName).bundle not found", context: "AgoraBeautyManager")
            return
        }
        
        if FileManager.default.fileExists(atPath: materialCopyDestPath) {
            try? FileManager.default.removeItem(atPath: materialCopyDestPath)
        }
        
        try? FileManager.default.copyItem(atPath: bundlePath, toPath: materialCopyDestPath)
    }
}

// MARK: - BeautyConfig Class

extension AgoraBeautyManager {
    
    /// Beauty configuration class (match Android implementation)
    class BeautyConfig {
        
        // Weak reference to manager to access beautyEffect and flags
        private weak var manager: AgoraBeautyManager?
        
        fileprivate func linkToManager(_ manager: AgoraBeautyManager) {
            self.manager = manager
        }
        
        // MARK: - Beauty Switches
        
        var beauty: Bool = false {
            didSet {
                guard let manager = manager else {
                    ShowLogger.error("beauty setter: manager is nil", context: "BeautyConfig")
                    return
                }
                guard manager.beautyEnable else {
                    return
                }
                manager.beautyEffect?.setVideoEffectBoolParam(
                    option: "beauty_effect_option",
                    key: "enable",
                    boolValue: beauty
                )
            }
        }
        
        var faceShape: Bool = false {
            didSet {
                guard let manager = manager else {
                    ShowLogger.error("faceShape setter: manager is nil", context: "BeautyConfig")
                    return
                }
                guard manager.beautyEnable else {
                    return
                }
                manager.beautyEffect?.setVideoEffectBoolParam(
                    option: "face_shape_beauty_option",
                    key: "enable",
                    boolValue: faceShape
                )
            }
        }
        
        // MARK: - Beauty Template
        
        var beautyName: String = "" {
            didSet {
                if oldValue == beautyName { return }
                manager?.beautyEffect?.addOrUpdateVideoEffect(
                    nodeId: AgoraVideoEffectNodeId.beauty.rawValue,
                    templateName: beautyName
                )
            }
        }
        
        // MARK: - Basic Beauty Parameters (0.0-1.0)
        
        var smoothness: Float = 0.7 {
            didSet {
                guard let manager = manager else {
                    ShowLogger.error("smoothness setter: manager is nil", context: "BeautyConfig")
            return
                }
                guard manager.beautyEnable else {
                    return
                }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "beauty_effect_option",
                    key: "smoothness",
                    floatValue: smoothness
                )
            }
        }
        
        var whitenNatural: Float = 0.7 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectStringParam(
                    option: "beauty_effect_option",
                    key: "whiten_lut_path",
                    stringValue: ""
                )
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "beauty_effect_option",
                    key: "lightness",
                    floatValue: whitenNatural
                )
            }
        }
        
        var redness: Float = 0.3 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "beauty_effect_option",
                    key: "redness",
                    floatValue: redness
                )
            }
        }
        
        var sharpen: Float = 0.6 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "beauty_effect_option",
                    key: "sharpness",
                    floatValue: sharpen
                )
            }
        }
        
        var clarity: Float = 0.0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "beauty_effect_option",
                    key: "contrast_strength",
                    floatValue: clarity
                )
            }
        }
        
        // MARK: - Face Shape Parameters (0-100 or -100~100)
        
        var faceContour: Int32 = 10 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .faceContour
                areaOption.shapeIntensity = faceContour
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var headScale: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .headScale
                areaOption.shapeIntensity = headScale
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var faceWidth: Int32 = 10 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .faceWidth
                areaOption.shapeIntensity = faceWidth
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var faceLength: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .faceLength
                areaOption.shapeIntensity = faceLength
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var cheekbone: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .cheekbone
                areaOption.shapeIntensity = cheekbone
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var shrinkCheek: Int32 = 10 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .cheek
                areaOption.shapeIntensity = shrinkCheek
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var mandible: Int32 = 50 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .mandible
                areaOption.shapeIntensity = mandible
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var chinLength: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .chin
                areaOption.shapeIntensity = chinLength
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var hairlineHeight: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .forehead
                areaOption.shapeIntensity = hairlineHeight
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var nasolabialFolds: Float = 0.8 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "face_buffing_option",
                    key: "nasolabial_fold",
                    floatValue: nasolabialFolds
                )
            }
        }
        
        // MARK: - Eye Parameters
        
        var enlargeEye: Int32 = 40 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .eyeScale
                areaOption.shapeIntensity = enlargeEye
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var brightenEye: Float = 0.8 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "face_buffing_option",
                    key: "brighten_eye",
                    floatValue: brightenEye
                )
            }
        }
        
        var darkCircle: Float = 0.8 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "face_buffing_option",
                    key: "eye_pouch",
                    floatValue: darkCircle
                )
            }
        }
        
        var eyePosition: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .eyePosition
                areaOption.shapeIntensity = eyePosition
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var eyeDistance: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .eyeDistance
                areaOption.shapeIntensity = eyeDistance
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var eyePupil: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .eyePupils
                areaOption.shapeIntensity = eyePupil
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var eyeLid: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .lowerEyelid
                areaOption.shapeIntensity = eyeLid
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var eyeInnercorner: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .eyeInnerCorner
                areaOption.shapeIntensity = eyeInnercorner
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var eyeOutercorner: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .eyeOuterCorner
                areaOption.shapeIntensity = eyeOutercorner
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        // MARK: - Nose Parameters
        
        var narrowNose: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .noseWidth
                areaOption.shapeIntensity = narrowNose
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var noseLength: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .noseLength
                areaOption.shapeIntensity = noseLength
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var noseWing: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .noseWing
                areaOption.shapeIntensity = noseWing
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var noseBridge: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .noseBridge
                areaOption.shapeIntensity = noseBridge
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var noseRoot: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .noseRoot
                areaOption.shapeIntensity = noseRoot
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var noseTip: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .noseTip
                areaOption.shapeIntensity = noseTip
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var noseGeneral: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .noseGeneral
                areaOption.shapeIntensity = noseGeneral
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        // MARK: - Mouth Parameters
        
        var mouthSize: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .mouthScale
                areaOption.shapeIntensity = mouthSize
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var mouthPosition: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .mouthPosition
                areaOption.shapeIntensity = mouthPosition
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var mouthSmile: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .mouthSmile
                areaOption.shapeIntensity = mouthSmile
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var mouthLip: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .mouthLip
                areaOption.shapeIntensity = mouthLip
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var whitenTeeth: Float = 0.0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "face_buffing_option",
                    key: "whiten_teeth",
                    floatValue: whitenTeeth
                )
            }
        }
        
        // MARK: - Eyebrow Parameters
        
        var eyebrowPosition: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .eyebrowPosition
                areaOption.shapeIntensity = eyebrowPosition
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        var eyebrowThickness: Int32 = 0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                faceShape = true
                let areaOption = AgoraFaceShapeAreaOptions()
                areaOption.shapeArea = .eyebrowThickness
                areaOption.shapeIntensity = eyebrowThickness
                manager.agoraKit?.setFaceShapeAreaOptions(areaOption)
            }
        }
        
        // MARK: - Image Quality Parameters (-1.0~1.0)
        
        var hue: Float = 0.0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "beauty_effect_option",
                    key: "hue",
                    floatValue: hue
                )
            }
        }
        
        var temperature: Float = 0.0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "beauty_effect_option",
                    key: "temperature",
                    floatValue: temperature
                )
            }
        }
        
        var saturation: Float = 0.0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "beauty_effect_option",
                    key: "saturation",
                    floatValue: saturation
                )
            }
        }
        
        var brightness: Float = 0.0 {
            didSet {
                guard let manager = manager, manager.beautyEnable else { return }
                beauty = true
                manager.beautyEffect?.setVideoEffectFloatParam(
                    option: "beauty_effect_option",
                    key: "brightness",
                    floatValue: brightness
                )
            }
        }
        
        // MARK: - Makeup
        
        var makeupName: String? = nil {
            didSet {
                if oldValue == makeupName { return }
                guard let manager = manager else { return }
                
                if makeupName == nil {
                    manager.beautyEffect?.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.styleMakeup.rawValue)
                } else if manager.makeupEnable, let name = makeupName {
                    manager.beautyEffect?.addOrUpdateVideoEffect(
                        nodeId: AgoraVideoEffectNodeId.styleMakeup.rawValue,
                        templateName: name
                    )
                }
            }
        }
        
        var makeupStrength: Float = 0.6 {
            didSet {
                manager?.beautyEffect?.setVideoEffectFloatParam(
                    option: "style_makeup_option",
                    key: "styleIntensity",
                    floatValue: makeupStrength
                )
            }
        }
        
        // MARK: - Filter
        
        var filterName: String? = nil {
            didSet {
                if oldValue == filterName { return }
                guard let manager = manager else { return }
                
                if filterName == nil {
                    manager.beautyEffect?.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.filter.rawValue)
                } else if manager.filterEnable, let name = filterName {
                    manager.beautyEffect?.addOrUpdateVideoEffect(
                        nodeId: AgoraVideoEffectNodeId.filter.rawValue,
                        templateName: name
                    )
                }
            }
        }
        
        var filterStrength: Float = 0.4 {
            didSet {
                manager?.beautyEffect?.setVideoEffectFloatParam(
                    option: "filter_effect_option",
                    key: "strength",
                    floatValue: filterStrength
                )
            }
        }
        
        // MARK: - Sticker
        
        var stickerName: String? = nil {
            didSet {
                if oldValue == stickerName { return }
                guard let manager = manager else { return }
                
                if stickerName == nil {
                    manager.beautyEffect?.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.sticker.rawValue)
                    // Restore mirror mode (match Android)
                    // Note: RtcEngineInstance access would need to be adjusted for your project
                    // RtcEngineInstance.videoEncoderConfiguration.mirrorMode = .enabled
                    // manager.agoraKit?.setVideoEncoderConfiguration(RtcEngineInstance.videoEncoderConfiguration)
                } else if manager.stickerEnable, let name = stickerName {
                    manager.beautyEffect?.addOrUpdateVideoEffect(
                        nodeId: AgoraVideoEffectNodeId.sticker.rawValue,
                        templateName: name
                    )
                    // Adjust mirror mode based on camera (match Android)
                    // if RtcEngineInstance.isFrontCamera {
                    //     RtcEngineInstance.videoEncoderConfiguration.mirrorMode = .enabled
                    // } else {
                    //     RtcEngineInstance.videoEncoderConfiguration.mirrorMode = .auto
                    // }
                    // manager.agoraKit?.setVideoEncoderConfiguration(RtcEngineInstance.videoEncoderConfiguration)
                }
            }
        }
        
        // MARK: - Reset Methods
        
        internal func resetBeauty() {
            // Beauty parameters
            smoothness = 0.7
            whitenNatural = 0.7
            redness = 0.3
            sharpen = 0.6
            clarity = 0.0
            
            // Face shape parameters
            faceContour = 10
            headScale = 0
            faceWidth = 10
            faceLength = 0
            cheekbone = 0
            shrinkCheek = 10
            mandible = 50
            chinLength = 0
            hairlineHeight = 0
            nasolabialFolds = 0.8
            
            // Eye parameters
            enlargeEye = 40
            brightenEye = 0.8
            darkCircle = 0.8
            eyePosition = 0
            eyeDistance = 0
            eyePupil = 0
            eyeLid = 0
            eyeInnercorner = 0
            eyeOutercorner = 0
            
            // Nose parameters
            narrowNose = 0
            noseLength = 0
            noseWing = 0
            noseBridge = 0
            noseRoot = 0
            noseTip = 0
            noseGeneral = 0
            
            // Mouth parameters
            mouthSize = 0
            mouthPosition = 0
            mouthSmile = 0
            mouthLip = 0
            whitenTeeth = 0.0
            
            // Eyebrow parameters
            eyebrowPosition = 0
            eyebrowThickness = 0
        }
        
        internal func reset() {
            resetBeauty()
            
            // Image quality parameters
            hue = 0.0
            temperature = 0.0
            saturation = 0.0
            brightness = 0.0
            
            filterName = nil
            filterStrength = 0.4
            makeupName = nil
            makeupStrength = 0.6
            stickerName = nil
        }
        
        internal func resume() {
            // Trigger all setters to re-apply parameters
            // Use temporary variables to avoid "Assigning a property to itself" warning
            let _beauty = beauty
            let _beautyName = beautyName
            beauty = _beauty
            beautyName = _beautyName
            
            // Beauty parameters
            let _smoothness = smoothness
            let _whitenNatural = whitenNatural
            let _redness = redness
            let _sharpen = sharpen
            let _clarity = clarity
            smoothness = _smoothness
            whitenNatural = _whitenNatural
            redness = _redness
            sharpen = _sharpen
            clarity = _clarity
            
            // Face shape parameters
            let _faceContour = faceContour
            let _headScale = headScale
            let _faceWidth = faceWidth
            let _faceLength = faceLength
            let _cheekbone = cheekbone
            let _shrinkCheek = shrinkCheek
            let _mandible = mandible
            let _chinLength = chinLength
            let _hairlineHeight = hairlineHeight
            let _nasolabialFolds = nasolabialFolds
            faceContour = _faceContour
            headScale = _headScale
            faceWidth = _faceWidth
            faceLength = _faceLength
            cheekbone = _cheekbone
            shrinkCheek = _shrinkCheek
            mandible = _mandible
            chinLength = _chinLength
            hairlineHeight = _hairlineHeight
            nasolabialFolds = _nasolabialFolds
            
            // Eye parameters
            let _enlargeEye = enlargeEye
            let _brightenEye = brightenEye
            let _darkCircle = darkCircle
            let _eyePosition = eyePosition
            let _eyeDistance = eyeDistance
            let _eyePupil = eyePupil
            let _eyeLid = eyeLid
            let _eyeInnercorner = eyeInnercorner
            let _eyeOutercorner = eyeOutercorner
            enlargeEye = _enlargeEye
            brightenEye = _brightenEye
            darkCircle = _darkCircle
            eyePosition = _eyePosition
            eyeDistance = _eyeDistance
            eyePupil = _eyePupil
            eyeLid = _eyeLid
            eyeInnercorner = _eyeInnercorner
            eyeOutercorner = _eyeOutercorner
            
            // Nose parameters
            let _narrowNose = narrowNose
            let _noseLength = noseLength
            let _noseWing = noseWing
            let _noseBridge = noseBridge
            let _noseRoot = noseRoot
            let _noseTip = noseTip
            let _noseGeneral = noseGeneral
            narrowNose = _narrowNose
            noseLength = _noseLength
            noseWing = _noseWing
            noseBridge = _noseBridge
            noseRoot = _noseRoot
            noseTip = _noseTip
            noseGeneral = _noseGeneral
            
            // Mouth parameters
            let _mouthSize = mouthSize
            let _mouthPosition = mouthPosition
            let _mouthSmile = mouthSmile
            let _mouthLip = mouthLip
            let _whitenTeeth = whitenTeeth
            mouthSize = _mouthSize
            mouthPosition = _mouthPosition
            mouthSmile = _mouthSmile
            mouthLip = _mouthLip
            whitenTeeth = _whitenTeeth
            
            // Eyebrow parameters
            let _eyebrowPosition = eyebrowPosition
            let _eyebrowThickness = eyebrowThickness
            eyebrowPosition = _eyebrowPosition
            eyebrowThickness = _eyebrowThickness
            
            // Image quality parameters
            let _hue = hue
            let _temperature = temperature
            let _saturation = saturation
            let _brightness = brightness
            hue = _hue
            temperature = _temperature
            saturation = _saturation
            brightness = _brightness
            
            // Filter parameters
            let _filterName = filterName
            let _filterStrength = filterStrength
            filterName = _filterName
            filterStrength = _filterStrength
            
            // Makeup parameters
            let _makeupName = makeupName
            let _makeupStrength = makeupStrength
            makeupName = _makeupName
            makeupStrength = _makeupStrength
            
            let _stickerName = stickerName
            stickerName = _stickerName
        }
    }
}
