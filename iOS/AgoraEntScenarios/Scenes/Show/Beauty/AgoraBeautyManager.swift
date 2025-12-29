//
//  AgoraBeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/11/10.
//

import Foundation

// Model type constants for beauty algorithm selection
enum AgoraBeautyModelType: Int {
    case adaptive = 0  // Adaptive mode: SDK automatically selects model based on device score
    case large = 1     // Large model: Force all devices to use large model
    case small = 2     // Small model: Force all devices to use small model
}

class AgoraBeautyManager: NSObject {
    var agoraKit: AgoraRtcEngineKit?
    var beautyEffect: AgoraVideoEffectObject?  // VideoEffectObject for style makeup, filter, and sticker
    lazy var render = AgoraBeautyRender()
    private static var _sharedManager: AgoraBeautyManager?
    
    // Track if beautyEffect is initialized
    private var isBeautyEffectInitialized: Bool = false
    
    // UserDefaults key for model type
    private static let kModelTypeKey = "show_model_type"
    
    // MARK: - Bare Face Comparison State
    
    // Saved state for bare face comparison
    // Match Android implementation: only save switches and resource names, not parameter values
    private var savedBeautyState: Bool = false
    private var savedFaceShapeState: Bool = false
    private var savedMakeupName: String? = nil  // "makeup1", "makeup2", or nil
    private var savedMakeupStrength: Float = 0.6  // Saved makeup strength (0.0-1.0)
    private var savedFilterName: String? = nil   // Saved filter template name
    private var savedFilterStrength: Float = 0.4  // Saved filter strength (0.0-1.0)
    private var savedStickerName: String? = nil  // Currently not used in iOS, but kept for consistency
    private var isBeautyDisabled: Bool = false
    
    // Track current makeup and filter names and strengths
    private var currentMakeupName: String? = nil
    private var currentMakeupStrength: Float = 0.6  // Default strength (0.6 = 60%)
    private var currentFilterStrength: Float = 0.4  // Current filter strength (0.0-1.0)
    
    // Track current beauty and adjust parameter values (for bare face comparison restore)
    // These parameters are set via beautyEffect?.setVideoEffectFloatParam, so we need to save/restore them
    private var currentClarity: Float = 0.0  // clarity (contrast_strength)
    private var currentHue: Float = 0.0  // hue
    private var currentTemperature: Float = 0.0  // temperature
    private var currentSaturation: Float = 0.0  // saturation
    private var currentBrightness: Float = 0.0  // brightness
    private var currentNasolabialfolds: Float = 0.0  // nasolabial_fold
    private var currentBrighteneye: Float = 0.0  // brighten_eye
    private var currentDarkcircle: Float = 0.0  // eye_pouch
    private var currentWhitenteeth: Float = 0.0  // whiten_teeth
    
    // Saved values for bare face comparison restore
    private var savedClarity: Float = 0.0
    private var savedHue: Float = 0.0
    private var savedTemperature: Float = 0.0
    private var savedSaturation: Float = 0.0
    private var savedBrightness: Float = 0.0
    private var savedNasolabialfolds: Float = 0.0
    private var savedBrighteneye: Float = 0.0
    private var savedDarkcircle: Float = 0.0
    private var savedWhitenteeth: Float = 0.0
    
    // Beauty template (match example code)
    private var _beautyTemplate: String? = nil
    var beautyTemplate: String? {
        set {
            if _beautyTemplate == newValue {
                return
            }
            _beautyTemplate = newValue
            if _beautyTemplate == nil {
                enableBeauty(false)
            } else {
                enableBeauty(true)
            }
        }
        get {
            return _beautyTemplate
        }
    }
    
    // Filter template (match example code)
    private var _filterTemplate: String? = nil
    var filterTemplate: String? {
        set {
            if _filterTemplate == newValue {
                return
            }
            _filterTemplate = newValue
            if _filterTemplate == nil {
                enableFilter(false)
            } else {
                enableFilter(true)
            }
        }
        get {
            return _filterTemplate
        }
    }
    
    // Makeup template (match example code)
    private var _makeupTemplate: String? = nil
    var makeupTemplate: String? {
        set {
            if _makeupTemplate == newValue {
                return
            }
            _makeupTemplate = newValue
            if _makeupTemplate == nil {
                enableMakeup(false)
            } else {
                enableMakeup(true)
            }
        }
        get {
            return _makeupTemplate
        }
    }
    
    // Track beauty and faceShape switch states (match Android implementation)
    private var currentBeautyState: Bool = false
    private var currentFaceShapeState: Bool = false
    
    // Sticker template (match example code)
    private var _stickerTemplate: String? = nil
    var stickerTemplate: String? {
        set {
            if _stickerTemplate == newValue {
                return
            }
            _stickerTemplate = newValue
            if _stickerTemplate == nil {
                enableSticker(false)
            } else {
                enableSticker(true)
            }
        }
        get {
            return _stickerTemplate
        }
    }
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
    
    // Copy bundle to sandbox because we need file save permissions (match example code)
    let material_copy_dest_path: String = NSHomeDirectory() + "/Documents/AgoraBeautyMaterial.bundle"
    
    private lazy var beautifyOption = AgoraBeautyOptions()
    private lazy var filterOption = AgoraFilterEffectOptions()
    private lazy var faceshapeOption = AgoraFaceShapeBeautyOptions()
    
    // Reusable AgoraFaceShapeAreaOptions (set shapeArea and shapeIntensity before each use)
    private lazy var areaOption = AgoraFaceShapeAreaOptions()
    
    // Store current face shape parameter values (match Android implementation)
    // These values will be used to restore parameters during bare face comparison
    // Use dictionary for cleaner code with for loop restoration
    private var currentFaceShapeValues: [String: CGFloat] = [:]
    
    static func castFromPositive100(_ value: Int32) -> Float {
        return 0.01 * Float(value);
    }
    
    static func castToPositive100(_ value: Float) -> Int32 {
        return Int32(roundf(100.0 * value));
    }
    
    /// Convert UI value (-50..50) to SDK value (-100..100) for shape parameters
    /// Used for parameters like faceLength, chinLength, eyePosition, etc.
    /// - Parameter uiValue: UI value in range -50..50
    /// - Returns: SDK value in range -100..100
    static func convertShapeParameterValue(_ uiValue: CGFloat) -> Int32 {
        // UI: -50..50, SDK: -100..100
        // Conversion: SDK = UI * 2
        return Int32(roundf(Float(uiValue) * 2.0))
    }
    
    /// Convert UI value (-50..50) to SDK value (-1.0..1.0) for adjust parameters
    /// Used for parameters like clarity, hue, temperature, saturation, brightness
    /// - Parameter uiValue: UI value in range -50..50
    /// - Returns: SDK value in range -1.0..1.0
    static func convertAdjustParameterValue(_ uiValue: CGFloat) -> Float {
        // UI: -50..50, SDK: -1.0..1.0
        // Conversion: SDK = UI / 50
        return Float(uiValue) / 50.0
    }
    
    
    /// Initialize AgoraVideoEffectObject
    /// This should be called once when initializing beauty SDK
    func initBeautyEffect() {
        guard let agoraKit = agoraKit, !isBeautyEffectInitialized else {
            return
        }
        
        // Copy bundle first (match example code)
        copyBeautyBundle()
        
        // Get bundle path
        let materialPath = material_copy_dest_path + "/beauty_material_functional"
        
        // Create VideoEffectObject (match example code)
        beautyEffect = agoraKit.createVideoEffectObject(bundlePath: materialPath, sourceType: .primaryCamera)
        
        if beautyEffect != nil {
            isBeautyEffectInitialized = true
            ShowLogger.info("BeautyEffect initialized successfully", context: "AgoraBeautyManager")
        } else {
            ShowLogger.error("Failed to create VideoEffectObject", context: "AgoraBeautyManager")
        }
    }
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        if key == nil {
            // close beauty effect
            beautyTemplate = nil
            return
        }
        beautyTemplate = "模板-空"
        switch key ?? "" {
        // 基础美颜参数 (0-100 UI → 0.0-1.0 SDK)
        case "smoothnessLevel":
            beautifyOption.smoothnessLevel = Float(value) / 100.0  // UI: 0-100 → SDK: 0.0-1.0
            currentBeautyState = true
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "lighteningLevel":
            beautifyOption.lighteningLevel = Float(value) / 100.0  // UI: 0-100 → SDK: 0.0-1.0
            currentBeautyState = true
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "rednessLevel":
            beautifyOption.rednessLevel = Float(value) / 100.0  // UI: 0-100 → SDK: 0.0-1.0
            currentBeautyState = true
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "sharpnessLevel":
            beautifyOption.sharpnessLevel = Float(value) / 100.0  // UI: 0-100 → SDK: 0.0-1.0
            currentBeautyState = true
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "clarity":
            // UI: -50~50 → SDK: -1.0~1.0
            let sdkValue = AgoraBeautyManager.convertAdjustParameterValue(value)
            // Match example code: use VideoEffectObject API
            beautyEffect?.setVideoEffectFloatParam(option: "beauty_effect_option", key: "contrast_strength", floatValue: sdkValue)
            currentClarity = sdkValue  // Save current value for bare face comparison restore
            currentBeautyState = true
        break
        case "nasolabialfolds":
            // UI: 0-100 → SDK: 0.0-1.0
            let sdkValue = Float(value) / 100.0
            // Match example code: use VideoEffectObject API
            beautyEffect?.setVideoEffectFloatParam(option: "face_buffing_option", key: "nasolabial_fold", floatValue: sdkValue)
            currentNasolabialfolds = sdkValue  // Save current value for bare face comparison restore
            currentBeautyState = true
        break
        case "brighteneye":
            // UI: 0-100 → SDK: 0.0-1.0
            let sdkValue = Float(value) / 100.0
            // Match example code: use VideoEffectObject API
            beautyEffect?.setVideoEffectFloatParam(option: "face_buffing_option", key: "brighten_eye", floatValue: sdkValue)
            currentBrighteneye = sdkValue  // Save current value for bare face comparison restore
            currentBeautyState = true
        break
        case "darkcircle":
            // UI: 0-100 → SDK: 0.0-1.0
            let sdkValue = Float(value) / 100.0
            // Match example code: use VideoEffectObject API
            beautyEffect?.setVideoEffectFloatParam(option: "face_buffing_option", key: "eye_pouch", floatValue: sdkValue)
            currentDarkcircle = sdkValue  // Save current value for bare face comparison restore
            currentBeautyState = true
        break
        case "whitenteeth":
            // UI: 0-100 → SDK: 0.0-1.0
            let sdkValue = Float(value) / 100.0
            // Match example code: use VideoEffectObject API
            beautyEffect?.setVideoEffectFloatParam(option: "face_buffing_option", key: "whiten_teeth", floatValue: sdkValue)
            currentWhitenteeth = sdkValue  // Save current value for bare face comparison restore
            currentBeautyState = true
        break
        case "hue":
            // UI: -50~50 → SDK: -1.0~1.0
            let sdkValue = AgoraBeautyManager.convertAdjustParameterValue(value)
            // Match example code: use VideoEffectObject API
            beautyEffect?.setVideoEffectFloatParam(option: "beauty_effect_option", key: "hue", floatValue: sdkValue)
            currentHue = sdkValue  // Save current value for bare face comparison restore
            currentBeautyState = true
        break
        case "temperature":
            // UI: -50~50 → SDK: -1.0~1.0
            let sdkValue = AgoraBeautyManager.convertAdjustParameterValue(value)
            // Match example code: use VideoEffectObject API
            beautyEffect?.setVideoEffectFloatParam(option: "beauty_effect_option", key: "temperature", floatValue: sdkValue)
            currentTemperature = sdkValue  // Save current value for bare face comparison restore
            currentBeautyState = true
        break
        case "saturation":
            // UI: -50~50 → SDK: -1.0~1.0
            let sdkValue = AgoraBeautyManager.convertAdjustParameterValue(value)
            // Match example code: use VideoEffectObject API
            beautyEffect?.setVideoEffectFloatParam(option: "beauty_effect_option", key: "saturation", floatValue: sdkValue)
            currentSaturation = sdkValue  // Save current value for bare face comparison restore
            currentBeautyState = true
        break
        case "brightness":
            // UI: -50~50 → SDK: -1.0~1.0
            let sdkValue = AgoraBeautyManager.convertAdjustParameterValue(value)
            // Match example code: use VideoEffectObject API
            beautyEffect?.setVideoEffectFloatParam(option: "beauty_effect_option", key: "brightness", floatValue: sdkValue)
            currentBrightness = sdkValue  // Save current value for bare face comparison restore
            currentBeautyState = true
        break
        case "headscale":
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeArea = AgoraFaceShapeArea.headScale
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["headscale"] = value  // Save current value for restoration
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "forehead":
            // forehead corresponds to hairlineHeight in Android, which is -50~50 UI → -100~100 SDK
            // But if this is called with a 0-100 value, it should be treated as 0-100 range
            // Check if value is in -50~50 range or 0-100 range
            areaOption.shapeArea = AgoraFaceShapeArea.forehead
            if value >= -50 && value <= 50 {
                // UI value range: -50..50, SDK value range: -100..100
                areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            } else {
                // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
                areaOption.shapeIntensity = Int32(round(value))
            }
            currentFaceShapeValues["forehead"] = value  // Save current value for restoration
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        case "facecontour":
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeArea = AgoraFaceShapeArea.faceContour
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["facecontour"] = value  // Save current value for restoration
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "facewidth":
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeArea = AgoraFaceShapeArea.faceWidth
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["facewidth"] = value  // Save current value for restoration
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "facelength":
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeArea = AgoraFaceShapeArea.faceLength
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["facelength"] = value  // Save current value for restoration
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "cheekbone":
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeArea = AgoraFaceShapeArea.cheekbone
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["cheekbone"] = value  // Save current value for restoration
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "cheek":
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeArea = AgoraFaceShapeArea.cheek
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["cheek"] = value  // Save current value for restoration
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "chinlength":
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeArea = AgoraFaceShapeArea.chin
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["chinlength"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "hairlineheight":
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeArea = AgoraFaceShapeArea.forehead
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["hairlineheight"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "enlargeeye":
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeArea = AgoraFaceShapeArea.eyeScale
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["enlargeeye"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "eyeposition":
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeArea = AgoraFaceShapeArea.eyePosition
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["eyeposition"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "eyedistance":
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeArea = AgoraFaceShapeArea.eyeDistance
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["eyedistance"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "eyepupil":
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeArea = AgoraFaceShapeArea.eyePupils
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["eyepupil"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "eyelid":
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeArea = AgoraFaceShapeArea.lowerEyelid
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["eyelid"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "eyeinnercorner":
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeArea = AgoraFaceShapeArea.eyeInnerCorner
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["eyeinnercorner"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "eyeoutercorner":
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeArea = AgoraFaceShapeArea.eyeOuterCorner
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["eyeoutercorner"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "narrownose":
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeArea = AgoraFaceShapeArea.noseWidth
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["narrownose"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "nosewing":
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeArea = AgoraFaceShapeArea.noseWing
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["nosewing"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "nosebridge":
            areaOption.shapeArea = AgoraFaceShapeArea.noseBridge
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["nosebridge"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "noseroot":
            areaOption.shapeArea = AgoraFaceShapeArea.noseRoot
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["noseroot"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "nosetip":
            areaOption.shapeArea = AgoraFaceShapeArea.noseTip
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["nosetip"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "nosegeneral":
            areaOption.shapeArea = AgoraFaceShapeArea.noseGeneral
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["nosegeneral"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "mouthsize":
            areaOption.shapeArea = AgoraFaceShapeArea.mouthScale
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["mouthsize"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "mouthposition":
            areaOption.shapeArea = AgoraFaceShapeArea.mouthPosition
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["mouthposition"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "mouthsmile":
            areaOption.shapeArea = AgoraFaceShapeArea.mouthSmile
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["mouthsmile"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "mouthlip":
            areaOption.shapeArea = AgoraFaceShapeArea.mouthLip
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["mouthlip"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "eyebrowposition":
            areaOption.shapeArea = AgoraFaceShapeArea.eyebrowPosition
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["eyebrowposition"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "eyebrowthickness":
            areaOption.shapeArea = AgoraFaceShapeArea.eyebrowThickness
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["eyebrowthickness"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "mandible":
            areaOption.shapeArea = AgoraFaceShapeArea.mandible
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["mandible"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "shrinkcheek":
            areaOption.shapeArea = AgoraFaceShapeArea.cheek
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["shrinkcheek"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        // 兼容旧参数名
        case "chin":
            areaOption.shapeArea = AgoraFaceShapeArea.chin
            // chin corresponds to chinLength in Android, which is -50~50 UI → -100~100 SDK
            // But if this is called with a 0-100 value, it should be treated as 0-100 range
            // Check if value is in -50~50 range or 0-100 range
            if value >= -50 && value <= 50 {
                // UI value range: -50..50, SDK value range: -100..100
                areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            } else {
                // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
                areaOption.shapeIntensity = Int32(round(value))
            }
            currentFaceShapeValues["chinlength"] = value  // Save current value for restoration (chin is alias for chinlength)
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "eyescale":
            areaOption.shapeArea = AgoraFaceShapeArea.eyeScale
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["eyescale"] = value  // Save current value for restoration
            currentFaceShapeState = true
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "noselength":
            areaOption.shapeArea = AgoraFaceShapeArea.noseLength
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeValues["noselength"] = value  // Save current value for restoration
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "nosewidth":
            areaOption.shapeArea = AgoraFaceShapeArea.noseWidth
            // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
            areaOption.shapeIntensity = Int32(round(value))
            currentFaceShapeValues["nosewidth"] = value  // Save current value for restoration
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "mouthscale":
            areaOption.shapeArea = AgoraFaceShapeArea.mouthScale
            // mouthscale corresponds to mouthSize in Android, which is -50~50 UI → -100~100 SDK
            // But if this is called with a 0-100 value, it should be treated as 0-100 range
            // Check if value is in -50~50 range or 0-100 range
            if value >= -50 && value <= 50 {
                // UI value range: -50..50, SDK value range: -100..100
                areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            } else {
                // UI value is already 0-100, SDK expects 0-100 Int32 (match Android: value.toInt())
                areaOption.shapeIntensity = Int32(round(value))
            }
            currentFaceShapeValues["mouthscale"] = value  // Save current value for restoration
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "gentlemaneface":
            faceshapeOption.shapeStyle = .male
            faceshapeOption.styleIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
            break
        case "ladyface":
            faceshapeOption.shapeStyle = .female
            faceshapeOption.styleIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
            break
        default: break
        }
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat) {
        // Match example code exactly
        if key == nil {
            // close stylemakeup effect
            makeupTemplate = nil
            return
        }
        else if key == "init" {
            // load stylemakeup default template
            makeupTemplate = ""
            return
        }
        makeupTemplate = key
        // Convert UI value (0-100) to SDK value (0.0-1.0)
        let sdkValue = Float(value) / 100.0
        beautyEffect?.setVideoEffectFloatParam(option: "style_makeup_option", key: "styleIntensity", floatValue: sdkValue)
        currentMakeupName = key
        currentMakeupStrength = sdkValue  // Save current strength
    }
    
    func setFilter(path: String?, key: String?, value: CGFloat) {
        // Match example code exactly
        if key == nil {
            // close filter effect
            filterTemplate = nil
            return
        }
        else if key == "init" {
            // load filter default template
            filterTemplate = ""
            return
        }
        filterTemplate = key
        // Convert UI value (0-100) to SDK value (0.0-1.0)
        let sdkValue = Float(value) / 100.0
        beautyEffect?.setVideoEffectFloatParam(option: "filter_effect_option", key: "strength", floatValue: sdkValue)
        // Save current strength
        currentFilterStrength = sdkValue
    }
    
    func enableFilter(_ enabled: Bool) {
        // Match example code exactly - no checks, beautyEffect should be initialized already
        if (enabled) {
            // load last effect templates
            beautyEffect?.addOrUpdateVideoEffect(nodeId: AgoraVideoEffectNodeId.filter.rawValue, templateName: _filterTemplate ?? "")
        } else {
            // remove all effects
            beautyEffect?.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.filter.rawValue)
        }
    }
    
    func enableMakeup(_ enabled: Bool) {
        // Match example code exactly - no checks, beautyEffect should be initialized already
        if (enabled) {
            // load last effect templates
            beautyEffect?.addOrUpdateVideoEffect(nodeId: AgoraVideoEffectNodeId.styleMakeup.rawValue, templateName: _makeupTemplate ?? "")
        } else {
            // remove all effects
            beautyEffect?.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.styleMakeup.rawValue)
        }
    }
    
    func setSticker(path: String?, key: String?, value: CGFloat) {
        // Match example code exactly
        if key == nil {
            // close sticker effect
            stickerTemplate = nil
        }
        else if key == "init" {
            // load sticker default template
            stickerTemplate = ""
        }
        else {
            stickerTemplate = key
        }
    }
    
    func enableSticker(_ enabled: Bool) {
        // Match example code exactly - no checks, beautyEffect should be initialized already
        if (enabled) {
            beautyEffect?.addOrUpdateVideoEffect(nodeId: AgoraVideoEffectNodeId.sticker.rawValue, templateName: _stickerTemplate ?? "")
        } else {
            beautyEffect?.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.sticker.rawValue)
        }
    }
    
    func enableBeauty(_ enabled: Bool) {
        // Match example code exactly - no checks, beautyEffect should be initialized already
        if (enabled) {
            // load last effect templates
            beautyEffect?.addOrUpdateVideoEffect(nodeId: AgoraVideoEffectNodeId.beauty.rawValue, templateName: _beautyTemplate ?? "")
        } else {
            // remove all effects
            beautyEffect?.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.beauty.rawValue)
        }
    }
    
    func enable(_ enabled: Bool) {
        enableBeauty(enabled)
        enableMakeup(enabled)
        enableFilter(enabled)
        enableSticker(enabled)
        // Match Android: call resume() when enabling to re-apply all saved parameters
        if enabled {
            resume()
        }
    }
    
    /// Resume all beauty parameters by re-applying saved values
    /// Match Android implementation: trigger all setters to re-apply parameters to SDK
    private func resume() {
        // Re-apply all face shape parameters from saved values
        // Match Android: self-assignment triggers setter and SDK call
        for (key, value) in currentFaceShapeValues {
            setBeauty(path: nil, key: key, value: value)
        }
    }
    
    func reset(datas: [BeautyModel], type: ShowBeautyFaceVCType) {
        switch type {
        case .beauty:
            resetBeauty(datas: datas)
        case .style:
            resetStyle(datas: datas)
        case .filter:
            resetFilter(datas: datas)
        default:
            // For other types, just disable effects
        agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
        agoraKit?.setFaceShapeBeautyOptions(false, options: faceshapeOption)
        agoraKit?.setFilterEffectOptions(false, options: filterOption)
        makeupTemplate = nil  // Match example code: disable makeup using template
        }
    }
    
    func resetBeauty(datas: [BeautyModel]) {
        // Match Android: enable beauty and faceShape before resetting
        // Android: beautyConfig.beauty = true; beautyConfig.faceShape = true; beautyConfig.resetBeauty();
        currentBeautyState = true
        currentFaceShapeState = true
        
        // Get default values from createAgoraBeautyData()
        let defaultData = BeautyModel.createAgoraBeautyData()
        
        // Create a dictionary for quick lookup: [key: defaultValue]
        var defaultValuesByKey: [String: CGFloat] = [:]
        defaultData.forEach { model in
            if let key = model.key {
                defaultValuesByKey[key] = model.value
            }
        }
        
        // Reset SDK parameters using default values (match Android: directly set each property)
        // Android sets each property directly, which triggers the setter and calls SDK
        for (key, uiValue) in defaultValuesByKey {
            setBeauty(path: nil, key: key, value: uiValue)
        }
        
        // Ensure beauty and faceShape are enabled after reset (match Android behavior)
        agoraKit?.setBeautyEffectOptions(true, options: beautifyOption)
        agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        
        // Update UI data array to reflect reset values
        ShowBeautyFaceVC.beautyData.enumerated().forEach { index, model in
            // Save original selection state
            let wasSelected = model.isSelected
            
            // Handle special cases
            if model.name == "show_beauty_item_none".show_localized {
                // "None" button should never be selected
                model.isSelected = false
            } else if model.name == "show_beauty_item_beauty_reset".show_localized {
                // Reset button: don't change selection state (match Android: reset doesn't change selection)
                // Keep the original selection state
            } else if let key = model.key, let defaultValue = defaultValuesByKey[key] {
                // Update value from default data
                model.value = defaultValue
            }
            
            // Restore selection state (except for "none" button which is handled above)
            if model.name != "show_beauty_item_none".show_localized {
                model.isSelected = wasSelected
            }
        }
    }
    
    func resetStyle(datas: [BeautyModel]) {
        // Match Android: reset makeup to null (remove makeup)
        // Android: beautyConfig.makeupName = null
        // iOS: Use VideoEffectObject API
        if let beautyEffect = beautyEffect {
            beautyEffect.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.styleMakeup.rawValue)
        }
        currentMakeupName = nil
        
        // Update UI: reset all makeup items to default values (60, match Android: 0.6 * 100)
        for data in datas {
            if let key = data.key, !key.isEmpty {
                data.value = 60
            }
        }
    }
    
    func resetFilter(datas: [BeautyModel]) {
        // Match Android: reset filter to null (remove filter)
        // Android: beautyConfig.filterName = null
        // iOS: Use VideoEffectObject API
        if let beautyEffect = beautyEffect {
            beautyEffect.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.filter.rawValue)
        }
        filterTemplate = nil
        
        // Update UI: reset all filter items to default values (40, match Android: 0.4 * 100)
        for data in datas {
            if let key = data.key, !key.isEmpty {
                data.value = 40
            }
        }
    }
    
    func resetSticker(datas: [BeautyModel]) {
        // Match Android: reset sticker to null (remove sticker)
        // Android: beautyConfig.stickerName = null
        // iOS: Use VideoEffectObject API
        if let beautyEffect = beautyEffect {
            beautyEffect.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.sticker.rawValue)
        }
        _stickerTemplate = nil
        
        // Update UI: reset all sticker items to default selection (first item "无" should be selected)
        datas.enumerated().forEach { index, model in
            model.isSelected = index == 0
        }
    }
            
    /// Check if beauty SDK is initialized
    /// - Returns: true if agoraKit is not nil and beautyEffect is initialized, false otherwise
    func isInitialized() -> Bool {
        return agoraKit != nil && isBeautyEffectInitialized
    }
            
    func destroy() {
        // Disable all beauty effects first
        agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
        agoraKit?.setFaceShapeBeautyOptions(false, options: faceshapeOption)
        agoraKit?.setFilterEffectOptions(false, options: filterOption)
        makeupTemplate = nil  // Match example code: disable makeup using template
        filterTemplate = nil  // Match example code: disable filter using template
        stickerTemplate = nil  // Match example code: disable sticker using template
        
        // Remove all video effects using VideoEffectObject API
        if let beautyEffect = beautyEffect {
            beautyEffect.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.beauty.rawValue)
            beautyEffect.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.styleMakeup.rawValue)
            beautyEffect.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.filter.rawValue)
            beautyEffect.removeVideoEffect(nodeId: AgoraVideoEffectNodeId.sticker.rawValue)
        }
        
        // Destroy VideoEffectObject
        agoraKit?.destroyVideoEffectObject(beautyEffect)
        beautyEffect = nil
        isBeautyEffectInitialized = false
        
        // Disable extension (match example code)
        agoraKit?.enableExtension(withVendor: "agora_video_filters_clear_vision",
                                  extension: "clear_vision",
                                  enabled: false,
                                  sourceType: .primaryCamera)
        
        // Clean up resources
        agoraKit = nil
        AgoraBeautyManager._sharedManager = nil
        ShowLogger.info("Beauty SDK destroyed and resources cleaned up", context: "AgoraBeautyManager")
    }
    
    private func getLutBundlePath(_ name: String) -> String? {
        guard let bundlePath = Bundle.main.path(forResource: "BeautyResource", ofType: "bundle"),
              let bundle = Bundle(path: bundlePath) else {
            return nil
        }
        return bundle.path(forResource: name, ofType: "cube")
    }
    
    // MARK: - Bare Face Comparison
    
    /// Save current beauty state and disable all beauty effects
    /// This is called when user presses the bare face comparison button
    /// Match Android implementation: only save switches and resource names
    func actionBareFace() {
        guard !isBeautyDisabled else {
            // Already disabled, do nothing
            return
        }
        
        // Save current state (switches and resource names, not parameter values)
        // Match Android: directly save beauty and faceShape switch states
        savedBeautyState = currentBeautyState
        savedFaceShapeState = currentFaceShapeState
        
        // Save makeup, filter, and sticker names and strengths
        savedMakeupName = currentMakeupName
        savedMakeupStrength = currentMakeupStrength  // Save current makeup strength
        savedFilterName = _filterTemplate  // Save current filter template name
        savedFilterStrength = currentFilterStrength  // Save current filter strength
        savedStickerName = _stickerTemplate  // Save current sticker template name
        
        // Save beauty and adjust parameter values (set via setVideoEffectFloatParam)
        savedClarity = currentClarity
        savedHue = currentHue
        savedTemperature = currentTemperature
        savedSaturation = currentSaturation
        savedBrightness = currentBrightness
        savedNasolabialfolds = currentNasolabialfolds
        savedBrighteneye = currentBrighteneye
        savedDarkcircle = currentDarkcircle
        savedWhitenteeth = currentWhitenteeth
        
        // Temporarily disable all beauty effects using enable(false) (match example code)
        currentBeautyState = false  // Update state to match Android
        currentFaceShapeState = false  // Update state to match Android
        agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
        agoraKit?.setFaceShapeBeautyOptions(false, options: faceshapeOption)
        agoraKit?.setFilterEffectOptions(false, options: filterOption)
        
        // Disable all effects using enable(false) (match example code)
        enable(false)
        
        isBeautyDisabled = true
        ShowLogger.info("Bare face comparison enabled - all beauty effects disabled", context: "AgoraBeautyManager")
    }
    
    /// Restore previously saved beauty state
    /// This is called when user releases the bare face comparison button
    /// Match Android implementation: restore switches and resource names
    func actionBeauty() {
        guard isBeautyDisabled else {
            // Not disabled, do nothing
            return
        }
        
        // Restore beauty state (parameter values are maintained by SDK/options objects)
        currentBeautyState = savedBeautyState
        if savedBeautyState {
            agoraKit?.setBeautyEffectOptions(true, options: beautifyOption)
        } else {
            agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
        }

        // Restore faceShape state
        // Match Android: simply set the switch, resume() will be called by enable(true) below
        currentFaceShapeState = savedFaceShapeState
        if savedFaceShapeState {
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        } else {
            agoraKit?.setFaceShapeBeautyOptions(false, options: faceshapeOption)
        }
        
        // Restore filter by name (match Android: restore filterName)
        // Match Android: beautyConfig.filterName = savedFilterName
        // iOS: Simply set filterTemplate, the setter will call enableFilter which handles extension and initialization
        filterTemplate = savedFilterName
        // Restore filter strength if filter was restored
        if let savedFilter = savedFilterName, !savedFilter.isEmpty {
            beautyEffect?.setVideoEffectFloatParam(option: "filter_effect_option", key: "strength", floatValue: savedFilterStrength)
            ShowLogger.info("Restored filter: \(savedFilter) with strength: \(savedFilterStrength)", context: "AgoraBeautyManager")
        }
        
        // Restore makeup by name (match Android: restore makeupName)
        // Match Android: beautyConfig.makeupName = savedMakeupName
        // iOS: Simply set makeupTemplate, the setter will call enableMakeup
        makeupTemplate = savedMakeupName
        // Restore makeup strength if makeup was restored
        if let savedMakeup = savedMakeupName, !savedMakeup.isEmpty {
            beautyEffect?.setVideoEffectFloatParam(option: "style_makeup_option", key: "styleIntensity", floatValue: savedMakeupStrength)
            currentMakeupName = savedMakeup
            currentMakeupStrength = savedMakeupStrength
            ShowLogger.info("Restored makeup: \(savedMakeup) with strength: \(savedMakeupStrength)", context: "AgoraBeautyManager")
        } else {
            currentMakeupName = nil
        }
        
        // Restore sticker by name (match Android: restore stickerName)
        // Match Android: beautyConfig.stickerName = savedStickerName
        // iOS: Simply set stickerTemplate, the setter will call enableSticker
        stickerTemplate = savedStickerName
        
        // Enable all effects using enable(true) (match example code)
        // This will enable beauty, makeup, filter, and sticker based on their template states
        enable(true)
        
        // After enabling effects, re-apply beautifyOption values to ensure they are restored
        // This is necessary because enable(false) removes the beauty effect, and the values might be lost
        if savedBeautyState {
            agoraKit?.setBeautyEffectOptions(true, options: beautifyOption)
        }
        
        // Re-apply adjust parameters (clarity, hue, temperature, saturation, brightness)
        // and other parameters (nasolabialfolds, brighteneye, darkcircle, whitenteeth)
        // These are set via beautyEffect?.setVideoEffectFloatParam, so we need to re-apply them
        // after enable(true) to ensure they are restored
        if savedBeautyState {
            // Restore adjust parameters (画质)
            beautyEffect?.setVideoEffectFloatParam(option: "beauty_effect_option", key: "contrast_strength", floatValue: savedClarity)
            beautyEffect?.setVideoEffectFloatParam(option: "beauty_effect_option", key: "hue", floatValue: savedHue)
            beautyEffect?.setVideoEffectFloatParam(option: "beauty_effect_option", key: "temperature", floatValue: savedTemperature)
            beautyEffect?.setVideoEffectFloatParam(option: "beauty_effect_option", key: "saturation", floatValue: savedSaturation)
            beautyEffect?.setVideoEffectFloatParam(option: "beauty_effect_option", key: "brightness", floatValue: savedBrightness)
            
            // Restore face buffing parameters
            beautyEffect?.setVideoEffectFloatParam(option: "face_buffing_option", key: "nasolabial_fold", floatValue: savedNasolabialfolds)
            beautyEffect?.setVideoEffectFloatParam(option: "face_buffing_option", key: "brighten_eye", floatValue: savedBrighteneye)
            beautyEffect?.setVideoEffectFloatParam(option: "face_buffing_option", key: "eye_pouch", floatValue: savedDarkcircle)
            beautyEffect?.setVideoEffectFloatParam(option: "face_buffing_option", key: "whiten_teeth", floatValue: savedWhitenteeth)
            
            // Update current values
            currentClarity = savedClarity
            currentHue = savedHue
            currentTemperature = savedTemperature
            currentSaturation = savedSaturation
            currentBrightness = savedBrightness
            currentNasolabialfolds = savedNasolabialfolds
            currentBrighteneye = savedBrighteneye
            currentDarkcircle = savedDarkcircle
            currentWhitenteeth = savedWhitenteeth
        }
        
        isBeautyDisabled = false
        ShowLogger.info("Beauty effects restored from bare face comparison", context: "AgoraBeautyManager")
    }
    
    // MARK: - Model Type Management
    
    /// Get current model type from UserDefaults
    /// - Returns: Model type (adaptive, large, or small)
    static func getCurrentModelType() -> AgoraBeautyModelType {
        let rawValue = UserDefaults.standard.integer(forKey: kModelTypeKey)
        return AgoraBeautyModelType(rawValue: rawValue) ?? .adaptive
    }
    
    /// Save model type to UserDefaults
    /// - Parameter modelType: Model type to save
    static func saveModelType(_ modelType: AgoraBeautyModelType) {
        UserDefaults.standard.set(modelType.rawValue, forKey: kModelTypeKey)
        UserDefaults.standard.synchronize()
    }
    
    /// Set model type private parameter based on user selection
    /// This should be called before initializing beauty SDK
    /// - Parameter rtcEngine: RTC Engine instance
    // Match Android: model type parameter setting is commented out
    // SDK will automatically select model type based on device score
    static func setModelTypeParameter(rtcEngine: AgoraRtcEngineKit) {
        // Android commit 982bc9484: Commented out model type parameter setting
        // Let SDK automatically select model type based on device score
        ShowLogger.info("Model type parameter setting is disabled, using SDK default behavior", context: "AgoraBeautyManager")
        
        // Original implementation (commented out to match Android):
        // let modelType = getCurrentModelType()
        // let parameter: String
        //
        // switch modelType {
        // case .large:
        //     parameter = "{\"che.video.low_alg_score_4_beauty\":0}"
        // case .small:
        //     parameter = "{\"che.video.low_alg_score_4_beauty\":100}"
        // case .adaptive:
        //     ShowLogger.info("Model type is ADAPTIVE, using default behavior", context: "AgoraBeautyManager")
        //     return
        // }
        //
        // let ret = rtcEngine.setParameters(parameter)
        // ShowLogger.info("Set model type to \(modelType), result: \(ret)", context: "AgoraBeautyManager")
    }
    
    // MARK: - Bundle Management
    
    private static var m_beauty_bundle_copied = false
    
    private func copyBeautyBundle() {
        if AgoraBeautyManager.m_beauty_bundle_copied {
            return
        }
        guard let bundle_path = Bundle.main.path(forResource: "AgoraBeautyMaterial", ofType: "bundle") else {
            ShowLogger.error("AgoraBeautyMaterial.bundle not found", context: "AgoraBeautyManager")
            return
        }
        if FileManager.default.fileExists(atPath: material_copy_dest_path) {
            try? FileManager.default.removeItem(atPath: material_copy_dest_path)
        }
        try? FileManager.default.copyItem(atPath: bundle_path, toPath: material_copy_dest_path)
        AgoraBeautyManager.m_beauty_bundle_copied = true
    }
}
