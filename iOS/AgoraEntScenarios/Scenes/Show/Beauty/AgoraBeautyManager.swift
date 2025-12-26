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
    lazy var render = AgoraBeautyRender()
    private static var _sharedManager: AgoraBeautyManager?
    private var styleParam: [String : Any] = ["enable_mu": false]
    
    // Track if extension is enabled to avoid duplicate calls
    private var isExtensionEnabled: Bool = false
    
    // UserDefaults key for model type
    private static let kModelTypeKey = "show_model_type"
    
    // MARK: - Bare Face Comparison State
    
    // Saved state for bare face comparison
    // Match Android implementation: only save switches and resource names, not parameter values
    private var savedBeautyState: Bool = false
    private var savedFaceShapeState: Bool = false
    private var savedMakeupName: String? = nil  // "makeup1", "makeup2", or nil
    private var savedFilterName: String? = nil   // "yuansheng", "lengbai", "nenbai", or nil
    private var savedStickerName: String? = nil  // Currently not used in iOS, but kept for consistency
    private var isBeautyDisabled: Bool = false
    
    // Track current makeup and filter names
    private var currentMakeupName: String? = nil
    private var currentFilterName: String? = nil
    
    // Track beauty and faceShape switch states (match Android implementation)
    private var currentBeautyState: Bool = false
    private var currentFaceShapeState: Bool = false
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
    
    private lazy var beautifyOption = AgoraBeautyOptions()
    private lazy var filterOption = AgoraFilterEffectOptions()
    private lazy var faceshapeOption = AgoraFaceShapeBeautyOptions()
    
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
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        if key == nil {
            // Disable all beauty effects
            currentBeautyState = false
            currentFaceShapeState = false
            currentMakeupName = nil
            currentFilterName = nil
            agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
            agoraKit?.setFaceShapeBeautyOptions(false, options: faceshapeOption)
            agoraKit?.setFilterEffectOptions(false, options: filterOption)
            self.updateMakeupOptions(dict: ["enable_mu": false] as [String : Any])
            isExtensionEnabled = false
        }
        else if key == "init" {
            // Only enable extension if not already enabled to avoid duplicate calls
            // This fixes the issue where setting beauty parameters on first install
            // would override the beauty effect by calling enableExtension multiple times
            if !isExtensionEnabled {
                let ret = agoraKit?.enableExtension(withVendor: "agora_video_filters_clear_vision",
                                                     extension: "clear_vision",
                                                     enabled: true,
                                                     sourceType: .primaryCamera)
                if ret == 0 {
                    isExtensionEnabled = true
                    ShowLogger.info("Extension enabled successfully", context: "AgoraBeautyManager")
                } else {
                    ShowLogger.error("Failed to enable extension: \(ret ?? -1)", context: "AgoraBeautyManager")
                }
            }
            currentBeautyState = true  // Init enables beauty
            currentFaceShapeState = true  // Init enables face shape
            agoraKit?.setBeautyEffectOptions(true, options: beautifyOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
            self.updateMakeupOptions(dict: styleParam)
        }
        switch key ?? "" {
        case "smoothnessLevel":
            beautifyOption.smoothnessLevel = Float(value)
            currentBeautyState = true  // Setting beauty parameter enables beauty
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "lighteningLevel":
            beautifyOption.lighteningLevel = Float(value)
            currentBeautyState = true  // Setting beauty parameter enables beauty
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "rednessLevel":
            beautifyOption.rednessLevel = Float(value)
            currentBeautyState = true  // Setting beauty parameter enables beauty
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "sharpnessLevel":
            beautifyOption.sharpnessLevel = Float(value)
            currentBeautyState = true  // Setting beauty parameter enables beauty
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "headscale":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.headScale
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "forehead":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.forehead
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        case "facecontour":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.faceContour
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "facewidth":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.faceWidth
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "facelength":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.faceLength
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "cheekbone":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.cheekbone
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "cheek":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.cheek
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "chin":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.chin
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "eyescale":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.eyeScale
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "noselength":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.noseLength
            // UI value range: -50..50, SDK value range: -100..100
            areaOption.shapeIntensity = AgoraBeautyManager.convertShapeParameterValue(value)
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "nosewidth":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.noseWidth
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            currentFaceShapeState = true  // Setting face shape parameter enables face shape
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "mouthscale":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.mouthScale
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
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
        if key == "init" {
            // Only enable extension if not already enabled to avoid duplicate calls
            if !isExtensionEnabled {
                let ret = agoraKit?.enableExtension(withVendor: "agora_video_filters_clear_vision",
                                                    extension: "clear_vision",
                                                    enabled: true,
                                                    sourceType: .primaryCamera)
                if ret == 0 {
                    isExtensionEnabled = true
                    ShowLogger.info("Extension enabled in setStyle", context: "AgoraBeautyManager")
                }
            }
        }
        switch key ?? "" {
        case "makeup1":
            let dic = [
                "enable_mu": true,
                "browStyle": 1,
                "browColor": 1,
                "browStrength": value,
                "lashStyle": 1,
                "lashColor": 1,
                "lashStrength": value,
                "shadowStyle": 1,
                "shadowStrength": value,
                "pupilStyle": 1,
                "pupilStrength": value,
                "blushStyle": 1,
                "blushColor": 1,
                "blushStrength": value,
                "lipStyle": 1,
                "lipColor": 1,
                "lipStrength": value] as [String : Any]
            self.updateMakeupOptions(dict: dic)
            styleParam = dic
            currentMakeupName = "makeup1"
        break
        case "makeup2":
            let dic = [
                "enable_mu": true,
                "browStyle": 2,
                "browColor": 1,
                "browStrength": value,
                "lashStyle": 2,
                "lashColor": 1,
                "lashStrength": value,
                "shadowStyle": 2,
                "shadowStrength": value,
                "pupilStyle": 2,
                "pupilStrength": value,
                "blushStyle": 2,
                "blushColor": 1,
                "blushStrength": value,
                "lipStyle": 2,
                "lipColor": 1,
                "lipStrength": value] as [String : Any]
            self.updateMakeupOptions(dict: dic)
            styleParam = dic
            currentMakeupName = "makeup2"
        break
        default:
            let dic = [
                "enable_mu": false
                ] as [String : Any]
            self.updateMakeupOptions(dict: dic)
            styleParam = dic
            currentMakeupName = nil
        break
        }
    }
    
    func setFilter(path: String?, key: String?, value: CGFloat) {
        if key == "init" {
            // Only enable extension if not already enabled to avoid duplicate calls
            if !isExtensionEnabled {
                let ret = agoraKit?.enableExtension(withVendor: "agora_video_filters_clear_vision",
                                                    extension: "clear_vision",
                                                    enabled: true,
                                                    sourceType: .primaryCamera)
                if ret == 0 {
                    isExtensionEnabled = true
                    ShowLogger.info("Extension enabled in setFilter", context: "AgoraBeautyManager")
                }
            }
        }
        switch key ?? "" {
        case "yuansheng":
            filterOption.strength = Float(value)
            filterOption.path = getLutBundlePath("yuansheng32")
            agoraKit?.setFilterEffectOptions(true, options: filterOption)
            currentFilterName = "yuansheng"
        break
        case "lengbai":
            filterOption.strength = Float(value)
            filterOption.path = getLutBundlePath("lengbai32")
            agoraKit?.setFilterEffectOptions(true, options: filterOption)
            currentFilterName = "lengbai"
        break
        case "nenbai":
            filterOption.strength = Float(value)
            filterOption.path = getLutBundlePath("nenbai32")
            agoraKit?.setFilterEffectOptions(true, options: filterOption)
            currentFilterName = "nenbai"
        break
        default:
            agoraKit?.setFilterEffectOptions(false, options: filterOption)
            currentFilterName = nil
        break
        }
    }
    
    private func updateMakeupOptions(dict: Dictionary<String, Any>) {
        let value = try? JSONSerialization.data(withJSONObject: dict, options: [])
        let str = String(data: value!, encoding: String.Encoding.utf8) ?? ""
        agoraKit?.setExtensionPropertyWithVendor("agora_video_filters_clear_vision", extension: "clear_vision", key: "makeup_options", value: str, sourceType: .primaryCamera)
    }
    
    func reset(datas: [BeautyModel]) {
        agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
        agoraKit?.setFaceShapeBeautyOptions(false, options: faceshapeOption)
        agoraKit?.setFilterEffectOptions(false, options: filterOption)
        self.updateMakeupOptions(dict: ["enable_mu": false] as [String : Any])
    }
    
    func resetStyle(datas: [BeautyModel]) {
        for data in datas {
            setStyle(path: data.path, key: data.key, value: 0)
        }
    }
    
    func resetFilter(datas: [BeautyModel]) {
        for data in datas {
            setFilter(path: data.path, key: data.key, value: 0)
        }
    }
            
    /// Check if beauty SDK is initialized
    /// - Returns: true if agoraKit is not nil and extension is enabled, false otherwise
    func isInitialized() -> Bool {
        return agoraKit != nil && isExtensionEnabled
    }
    
    func destroy() {
        // Disable all beauty effects first
        agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
        agoraKit?.setFaceShapeBeautyOptions(false, options: faceshapeOption)
        agoraKit?.setFilterEffectOptions(false, options: filterOption)
        self.updateMakeupOptions(dict: ["enable_mu": false] as [String : Any])
        
        // Disable extension only if it was enabled
        if isExtensionEnabled {
            agoraKit?.enableExtension(withVendor: "agora_video_filters_clear_vision",
                                      extension: "clear_vision",
                                      enabled: false,
                                      sourceType: .primaryCamera)
        }
        
        // Clean up resources
        agoraKit = nil
        isExtensionEnabled = false
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
        
        // Save makeup and filter names
        savedMakeupName = currentMakeupName
        savedFilterName = currentFilterName
        savedStickerName = nil  // iOS doesn't use sticker for Agora beauty
        
        // Temporarily disable all beauty effects
        currentBeautyState = false  // Update state to match Android
        currentFaceShapeState = false  // Update state to match Android
        agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
        agoraKit?.setFaceShapeBeautyOptions(false, options: faceshapeOption)
        agoraKit?.setFilterEffectOptions(false, options: filterOption)
        self.updateMakeupOptions(dict: ["enable_mu": false] as [String : Any])
        
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
        
        // Restore face shape state
        currentFaceShapeState = savedFaceShapeState
        if savedFaceShapeState {
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        } else {
            agoraKit?.setFaceShapeBeautyOptions(false, options: faceshapeOption)
        }
        
        // Restore filter by name
        if let savedFilter = savedFilterName {
            currentFilterName = savedFilter
            switch savedFilter {
            case "yuansheng":
                filterOption.path = getLutBundlePath("yuansheng32")
            case "lengbai":
                filterOption.path = getLutBundlePath("lengbai32")
            case "nenbai":
                filterOption.path = getLutBundlePath("nenbai32")
            default:
                break
            }
            agoraKit?.setFilterEffectOptions(true, options: filterOption)
        } else {
            currentFilterName = nil
            agoraKit?.setFilterEffectOptions(false, options: filterOption)
        }
        
        // Restore makeup by name
        if let savedMakeup = savedMakeupName {
            currentMakeupName = savedMakeup
            // Restore makeup with default strength (0.5)
            if savedMakeup == "makeup1" {
                let dic = [
                    "enable_mu": true,
                    "browStyle": 1, "browColor": 1, "browStrength": 0.5,
                    "lashStyle": 1, "lashColor": 1, "lashStrength": 0.5,
                    "shadowStyle": 1, "shadowStrength": 0.5,
                    "pupilStyle": 1, "pupilStrength": 0.5,
                    "blushStyle": 1, "blushColor": 1, "blushStrength": 0.5,
                    "lipStyle": 1, "lipColor": 1, "lipStrength": 0.5] as [String : Any]
                styleParam = dic
                self.updateMakeupOptions(dict: dic)
            } else if savedMakeup == "makeup2" {
                let dic = [
                    "enable_mu": true,
                    "browStyle": 2, "browColor": 1, "browStrength": 0.5,
                    "lashStyle": 2, "lashColor": 1, "lashStrength": 0.5,
                    "shadowStyle": 2, "shadowStrength": 0.5,
                    "pupilStyle": 2, "pupilStrength": 0.5,
                    "blushStyle": 2, "blushColor": 1, "blushStrength": 0.5,
                    "lipStyle": 2, "lipColor": 1, "lipStrength": 0.5] as [String : Any]
                styleParam = dic
                self.updateMakeupOptions(dict: dic)
            }
        } else {
            currentMakeupName = nil
            let dic = ["enable_mu": false] as [String : Any]
            styleParam = dic
            self.updateMakeupOptions(dict: dic)
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
    static func setModelTypeParameter(rtcEngine: AgoraRtcEngineKit) {
        let modelType = getCurrentModelType()
        let parameter: String
        
        switch modelType {
        case .large:
            // All devices use large model (score = 0)
            parameter = "{\"che.video.low_alg_score_4_beauty\":0}"
        case .small:
            // All devices use small model (score = 100)
            parameter = "{\"che.video.low_alg_score_4_beauty\":100}"
        case .adaptive:
            // Adaptive mode: do not set parameter, SDK will automatically select based on device score
            ShowLogger.info("Model type is ADAPTIVE, using default behavior", context: "AgoraBeautyManager")
            return
        }
        
        let ret = rtcEngine.setParameters(parameter)
        ShowLogger.info("Set model type to \(modelType), result: \(ret)", context: "AgoraBeautyManager")
    }
}
