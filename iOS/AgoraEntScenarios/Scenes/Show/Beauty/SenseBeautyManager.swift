//
//  SenseBeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/1/12.
//

import UIKit

class SenseBeautyManager: NSObject {
    
    public let render = SenseBeautyRender()
    
    private var processor: VideoProcessingManager {
        return render.videoProcessing
    }
    
    private static var _sharedManager: SenseBeautyManager?
    static var shareManager: SenseBeautyManager {
        get {
            if let sharedManager = _sharedManager { return sharedManager }
            let sharedManager = SenseBeautyManager()
            _sharedManager = sharedManager
            _sharedManager?.checkLicense()
            return sharedManager
        }
        set {
            _sharedManager = nil
        }
    }
    private(set) var isSuccessLicense: Bool = false {
        didSet {
            guard isSuccessLicense, !datas.isEmpty else { return }
            datas.forEach({
                setBeauty(path: $0.path, key: $0.key, value: $0.value)
            })
            datas.removeAll()
        }
    }
    private var timer: Timer?
    private var datas: [BeautyModel] = [BeautyModel]()
    private var stickerId: Int32 = 0
    private var styleId: Int32 = 0
    var isEnableBeauty: Bool = true
    
    // MARK: - Bare Face Comparison State
    
    // Saved state for bare face comparison
    // Match Android implementation: save all 19 beauty parameter values + makeup + sticker
    private var savedSmooth: Float = 0.0
    private var savedWhiten: Float = 0.0
    private var savedThinFace: Float = 0.0
    private var savedEnlargeEye: Float = 0.0
    private var savedSharpen: Float = 0.0
    private var savedClear: Float = 0.0
    private var savedRedden: Float = 0.0
    private var savedShrinkCheekbone: Float = 0.0
    private var savedShrinkJawbone: Float = 0.0
    private var savedWhiteTeeth: Float = 0.0
    private var savedHairlineHeight: Float = 0.0
    private var savedNarrowNose: Float = 0.0
    private var savedMouthSize: Float = 0.0
    private var savedChinLength: Float = 0.0
    private var savedBrightEye: Float = 0.0
    private var savedDarkCircles: Float = 0.0
    private var savedNasolabialFolds: Float = 0.0
    private var savedSaturation: Float = 0.0
    private var savedContrast: Float = 0.0
    
    // Saved makeup and sticker (using path strings)
    private var savedMakeupPath: String? = nil
    private var savedMakeupStrength: Float = 0.0
    private var savedStickerPath: String? = nil
    private var savedStickerId: Int32 = 0
    private var savedStyleId: Int32 = 0
    private var isBeautyDisabled: Bool = false
    
    // Track current parameter values (key -> value mapping)
    // Key mapping: "103"=smooth, "101"=whiten, "201"=thinFace, "202"=enlargeEye, etc.
    private var currentBeautyParams: [String: Float] = [:]
    private var currentMakeupPath: String? = nil
    private var currentMakeupStrength: Float = 0.0
    private var currentStickerPath: String? = nil
    
    private func checkLicense() {
        var licensePath = STDynmicResourceConfig.shareInstance().licFilePath ?? ""
        if FileManager.default.fileExists(atPath: licensePath) == false {
            licensePath = Bundle.main.path(forResource: "SENSEME", ofType: "lic") ?? ""
        }
        isSuccessLicense = EffectsProcess.authorize(withLicensePath: licensePath)
        timer = Timer(timeInterval: 1, block: { [weak self] _ in
            guard let self = self else { return }
            self.isSuccessLicense = self.processor.effectsProcess.isAuthrized()
            if self.isSuccessLicense {
                self.timer?.invalidate()
                self.timer = nil
            }
        }, repeats: true)
        RunLoop.main.add(timer!, forMode: .common)
    }
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        if processor.effectsProcess.isAuthrized() == false {
            let model = BeautyModel()
            model.path = path
            model.value = value
            model.key = key
            datas.append(model)
            return
        }
        guard let keyStr = key, let key = UInt32(keyStr) else { return }
        processor.setEffectType(key, value: Float(value))
        
        // Track current parameter values for bare face comparison
        currentBeautyParams[keyStr] = Float(value)
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat) {
        guard let path = path, !path.isEmpty, let key = key else { return }
        processor.addStylePath(path, groupId: key == "Makeup_ALL" ? 0 : 1, strength: value) { [weak self] stickerId in
            guard let self = self else { return }
            self.styleId = stickerId
        }
        
        // Track current makeup for bare face comparison
        if key == "Makeup_ALL" {
            currentMakeupPath = path
            currentMakeupStrength = Float(value)
        }
    }
    
    func setFilter(path: String?, value: CGFloat) { }
    
    func setSticker(path: String?) {
        if let path = path {
            processor.removeStickerId(stickerId)
            processor.setStickerWithPath(path) { [weak self] stickerId in
                self?.stickerId = stickerId
            }
            // Track current sticker for bare face comparison
            currentStickerPath = path
        } else {
            resetSticker(datas: ShowBeautyFaceVC.stickerData)
            currentStickerPath = nil
        }
    }
    
    func reset(datas: [BeautyModel]) {
        datas.forEach({
//            $0.isSelected = $0.key == "103"
            guard $0.path != nil, let key = UInt32($0.key ?? "0") else { return }
            processor.setEffectType(key, value: 0)
        })
    }
    
    func resetStyle(datas: [BeautyModel]) {
        processor.removeStickerId(styleId)
        styleId = 0
    }
    
    func resetFilter(datas: [BeautyModel]) {
        datas.forEach({ item in
            item.isSelected = item.path == nil
            setFilter(path: item.path, value: 0)
        })
    }
    
    func resetSticker(datas: [BeautyModel]) {
        processor.removeStickerId(stickerId)
        stickerId = 0
    }
    
    func processFrame(pixelBuffer: CVPixelBuffer?) -> CVPixelBuffer? {
        if isSuccessLicense == false {
//            Log.errorText(text: "商汤美颜License检验失败,请检查license文件")
        }
        if !isEnableBeauty { return pixelBuffer }
        guard let pixel = pixelBuffer else { return nil }
        let result = processor.videoProcessHandler(pixel)
        return result.takeUnretainedValue()
    }
    
    func destroy() {
        render.destroy()
        SenseBeautyManager._sharedManager = nil
        datas.removeAll()
    }
    
    // MARK: - Bare Face Comparison
    
    /// Save current beauty state and disable all beauty effects
    /// This is called when user presses the bare face comparison button
    /// Match Android implementation: save all 19 beauty parameter values + makeup + sticker
    func actionBareFace() {
        guard !isBeautyDisabled else {
            // Already disabled, do nothing
            return
        }
        
        // Save all current beauty parameter values
        // Key mapping based on BeautyModel+Sense.swift:
        // "103"=smooth, "101"=whiten, "201"=thinFace, "202"=enlargeEye, "603"=sharpen, "604"=clear,
        // "318"=shrinkCheekbone, "320"=shrinkJawbone, "317"=whiteTeeth, "304"=hairlineHeight,
        // "306"=narrowNose, "309"=mouthSize, "303"=chinLength, "314"=brightEye, "315"=darkCircles,
        // "316"=nasolabialFolds, "602"=saturation, "601"=contrast
        // Save all 19 beauty parameters (iOS may not have redden, so we'll save it as 0 if not present)
        savedSmooth = currentBeautyParams["103"] ?? 0.0
        savedWhiten = currentBeautyParams["101"] ?? 0.0
        savedThinFace = currentBeautyParams["201"] ?? 0.0
        savedEnlargeEye = currentBeautyParams["202"] ?? 0.0
        savedSharpen = currentBeautyParams["603"] ?? 0.0
        savedClear = currentBeautyParams["604"] ?? 0.0
        savedRedden = currentBeautyParams["102"] ?? 0.0  // redden (may not exist in iOS, default to 0)
        savedShrinkCheekbone = currentBeautyParams["318"] ?? 0.0
        savedShrinkJawbone = currentBeautyParams["320"] ?? 0.0
        savedWhiteTeeth = currentBeautyParams["317"] ?? 0.0
        savedHairlineHeight = currentBeautyParams["304"] ?? 0.0
        savedNarrowNose = currentBeautyParams["306"] ?? 0.0
        savedMouthSize = currentBeautyParams["309"] ?? 0.0
        savedChinLength = currentBeautyParams["303"] ?? 0.0
        savedBrightEye = currentBeautyParams["314"] ?? 0.0
        savedDarkCircles = currentBeautyParams["315"] ?? 0.0
        savedNasolabialFolds = currentBeautyParams["316"] ?? 0.0
        savedSaturation = currentBeautyParams["602"] ?? 0.0
        savedContrast = currentBeautyParams["601"] ?? 0.0
        
        // Save makeup and sticker
        savedMakeupPath = currentMakeupPath
        savedMakeupStrength = currentMakeupStrength
        savedStickerPath = currentStickerPath
        savedStickerId = stickerId
        savedStyleId = styleId
        
        // Temporarily disable all beauty effects to show original face
        // Set all parameters to 0
        processor.setEffectType(103, value: 0)  // smooth
        processor.setEffectType(101, value: 0)  // whiten
        processor.setEffectType(201, value: 0)  // thinFace
        processor.setEffectType(202, value: 0)  // enlargeEye
        processor.setEffectType(603, value: 0)  // sharpen
        processor.setEffectType(604, value: 0)  // clear
        // Note: redden (102) may not exist in iOS, but we'll set it to 0 for consistency
        if currentBeautyParams["102"] != nil {
            processor.setEffectType(102, value: 0)  // redden
        }
        processor.setEffectType(318, value: 0)  // shrinkCheekbone
        processor.setEffectType(320, value: 0)  // shrinkJawbone
        processor.setEffectType(317, value: 0)  // whiteTeeth
        processor.setEffectType(304, value: 0)  // hairlineHeight
        processor.setEffectType(306, value: 0)  // narrowNose
        processor.setEffectType(309, value: 0)  // mouthSize
        processor.setEffectType(303, value: 0)  // chinLength
        processor.setEffectType(314, value: 0)  // brightEye
        processor.setEffectType(315, value: 0)  // darkCircles
        processor.setEffectType(316, value: 0)  // nasolabialFolds
        processor.setEffectType(602, value: 0)  // saturation
        processor.setEffectType(601, value: 0)  // contrast
        
        // Disable makeup
        processor.removeStickerId(styleId)
        styleId = 0
        
        // Disable sticker
        processor.removeStickerId(stickerId)
        stickerId = 0
        
        isBeautyDisabled = true
    }
    
    /// Restore previously saved beauty state
    /// This is called when user releases the bare face comparison button
    /// Match Android implementation: restore all 19 beauty parameter values + makeup + sticker
    func actionBeauty() {
        guard isBeautyDisabled else {
            // Not disabled, do nothing
            return
        }
        
        // Restore all saved beauty parameter values
        processor.setEffectType(103, value: savedSmooth)  // smooth
        processor.setEffectType(101, value: savedWhiten)  // whiten
        processor.setEffectType(201, value: savedThinFace)  // thinFace
        processor.setEffectType(202, value: savedEnlargeEye)  // enlargeEye
        processor.setEffectType(603, value: savedSharpen)  // sharpen
        processor.setEffectType(604, value: savedClear)  // clear
        // Restore redden if it was saved (may not exist in iOS)
        if savedRedden > 0 || currentBeautyParams["102"] != nil {
            processor.setEffectType(102, value: savedRedden)  // redden
            currentBeautyParams["102"] = savedRedden
        }
        processor.setEffectType(318, value: savedShrinkCheekbone)  // shrinkCheekbone
        processor.setEffectType(320, value: savedShrinkJawbone)  // shrinkJawbone
        processor.setEffectType(317, value: savedWhiteTeeth)  // whiteTeeth
        processor.setEffectType(304, value: savedHairlineHeight)  // hairlineHeight
        processor.setEffectType(306, value: savedNarrowNose)  // narrowNose
        processor.setEffectType(309, value: savedMouthSize)  // mouthSize
        processor.setEffectType(303, value: savedChinLength)  // chinLength
        processor.setEffectType(314, value: savedBrightEye)  // brightEye
        processor.setEffectType(315, value: savedDarkCircles)  // darkCircles
        processor.setEffectType(316, value: savedNasolabialFolds)  // nasolabialFolds
        processor.setEffectType(602, value: savedSaturation)  // saturation
        processor.setEffectType(601, value: savedContrast)  // contrast
        
        // Update current parameter values
        currentBeautyParams["103"] = savedSmooth
        currentBeautyParams["101"] = savedWhiten
        currentBeautyParams["201"] = savedThinFace
        currentBeautyParams["202"] = savedEnlargeEye
        currentBeautyParams["603"] = savedSharpen
        currentBeautyParams["604"] = savedClear
        currentBeautyParams["102"] = savedRedden
        currentBeautyParams["318"] = savedShrinkCheekbone
        currentBeautyParams["320"] = savedShrinkJawbone
        currentBeautyParams["317"] = savedWhiteTeeth
        currentBeautyParams["304"] = savedHairlineHeight
        currentBeautyParams["306"] = savedNarrowNose
        currentBeautyParams["309"] = savedMouthSize
        currentBeautyParams["303"] = savedChinLength
        currentBeautyParams["314"] = savedBrightEye
        currentBeautyParams["315"] = savedDarkCircles
        currentBeautyParams["316"] = savedNasolabialFolds
        currentBeautyParams["602"] = savedSaturation
        currentBeautyParams["601"] = savedContrast
        
        // Restore makeup if it was saved
        if let savedMakeup = savedMakeupPath {
            currentMakeupPath = savedMakeup
            currentMakeupStrength = savedMakeupStrength
            processor.addStylePath(savedMakeup, groupId: 0, strength: CGFloat(savedMakeupStrength)) { [weak self] stickerId in
                self?.styleId = stickerId
            }
        } else {
            currentMakeupPath = nil
            currentMakeupStrength = 0.0
        }
        
        // Restore sticker if it was saved
        if let savedSticker = savedStickerPath {
            currentStickerPath = savedSticker
            processor.setStickerWithPath(savedSticker) { [weak self] stickerId in
                self?.stickerId = stickerId
            }
        } else {
            currentStickerPath = nil
        }
        
        isBeautyDisabled = false
    }
}
