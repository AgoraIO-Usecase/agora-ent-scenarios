//
//  FUBeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/1/12.
//

import UIKit

class FUBeautyManager: NSObject {
    
    public lazy var render = FUBeautyRender()
    
    private static var _sharedManager: FUBeautyManager?
    static var shareManager: FUBeautyManager {
        get {
            if let sharedManager = _sharedManager { return sharedManager }
            let sharedManager = FUBeautyManager()
            _sharedManager = sharedManager
            return sharedManager
        }
        set {
            _sharedManager = nil
        }
    }
    
    var isSuccessLicense: Bool {
        render.fuManager.isSuccessLicense
    }
    
    // MARK: - Bare Face Comparison State
    
    // Saved state for bare face comparison
    // Match Android implementation: save makeup and sticker (beauty state is saved in BeautyManager)
    private var savedMakeupPath: String? = nil
    private var savedMakeupKey: String? = nil
    private var savedMakeupStrength: Float = 0.0
    private var savedStickerPath: String? = nil
    private var isBeautyDisabled: Bool = false
    
    // Track current makeup and sticker paths
    private var currentMakeupPath: String? = nil
    private var currentMakeupKey: String? = nil
    private var currentMakeupStrength: Float = 0.0
    private var currentStickerPath: String? = nil
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        if path == "init" {
            render.setBeautyPreset()
        } else {
            // Convert UI value to SDK value based on parameter type
            let sdkValue = convertUIValueToSDKValue(key: key, uiValue: value)
            render.setBeautyWithPath(path ?? "", key: key ?? "", value: sdkValue)
        }
    }
    
    /// Convert UI value to SDK value (0-1 range)
    /// This method only handles UI→SDK conversion, FUBeautyRender handles SDK→FaceUnity conversion
    private func convertUIValueToSDKValue(key: String?, uiValue: CGFloat) -> Float {
        guard let key = key else { return Float(uiValue) }
        
        switch key {
        // Parameters with -50~50 UI range: convert to 0-1 SDK range
        // Match Android: (value + 50) / 100
        case "chin", "forehead", "eyePosition", "eyeDistance", "eyecorner", "noseLength", "mouth", "eyebrowPosition", "eyebrowThickness":
            let sdkValue = Float(uiValue + 50) / 100.0
            // Ensure value is in valid range [0.0, 1.0] to match Android behavior
            return max(0.0, min(1.0, sdkValue))
            
        // All other parameters have 0-100 UI range: convert to 0-1 SDK range
        default:
            // Convert 0-100 UI range to 0-1 SDK range
            let sdkValue = Float(uiValue) / 100.0
            // Ensure value is in valid range [0.0, 1.0]
            return max(0.0, min(1.0, sdkValue))
        }
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat) {
        render.setStyleWithPath(path ?? "", key: key ?? "", value: Float(value))
        
        // Track current makeup for bare face comparison
        // For FaceUnity, key is the main identifier (path can be empty for style makeup)
        if let key = key, !key.isEmpty {
            currentMakeupPath = path  // path can be empty for style makeup
            currentMakeupKey = key
            currentMakeupStrength = Float(value)
        } else {
            currentMakeupPath = nil
            currentMakeupKey = nil
            currentMakeupStrength = 0.0
        }
    }
    
    func setAnimoji(path: String?) {
        render.setAnimojiWithPath(path ?? "")
    }
    
    func setFilter(path: String?, value: CGFloat) { }
    
    func setSticker(path: String?) {
        render.setStickerWithPath(path ?? "")
        
        // Track current sticker for bare face comparison
        currentStickerPath = path
    }
    
    func reset(datas: [BeautyModel], type: ShowBeautyFaceVCType) {
        switch type {
        case .beauty:
            resetBeauty(datas: datas)
        default:
            render.reset()
        }
    }
    
    func resetBeauty(datas: [BeautyModel]) {
        // Get default values from createFUBeautyData()
        let defaultData = BeautyModel.createFUBeautyData()
        
        // Create a dictionary for quick lookup: [key: defaultValue]
        var defaultValuesByKey: [String: CGFloat] = [:]
        defaultData.forEach { model in
            if let key = model.key {
                defaultValuesByKey[key] = model.value
            }
        }
        
        // Reset SDK parameters using default values
        let path = "face_beautification"
        for (key, uiValue) in defaultValuesByKey {
            let sdkValue = convertUIValueToSDKValue(key: key, uiValue: uiValue)
            render.setBeautyWithPath(path, key: key, value: sdkValue)
        }
        
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
        render.resetStyle()
        ShowBeautyFaceVC.styleData.enumerated().forEach({
            $0.element.isSelected = $0.offset == 0
        })
        // Clear tracked makeup state when resetting (match Android: beautyConfig.makeUp = null)
        currentMakeupPath = nil
        currentMakeupKey = nil
        currentMakeupStrength = 0.0
    }
    
    func resetFilter(datas: [BeautyModel]) {
        
    }
    
    func resetSticker(datas: [BeautyModel]) {
        render.resetSticker()
        ShowBeautyFaceVC.stickerData.enumerated().forEach({
            $0.element.isSelected = $0.offset == 0
        })
        // Clear tracked sticker state when resetting (match Android: beautyConfig.sticker = null)
        currentStickerPath = nil
    }
    
    func destroy() {
        render.destroy()
        FUBeautyManager._sharedManager = nil
    }
    
    // MARK: - Bare Face Comparison
    
    /// Save current beauty state and disable all beauty effects
    /// This is called when user presses the bare face comparison button
    /// Match Android implementation: save beauty switch, makeup, and sticker
    func actionBareFace() {
        guard !isBeautyDisabled else {
            // Already disabled, do nothing
            return
        }
        
        // Save makeup and sticker (beauty state is saved in BeautyManager via beautyAPI.isEnable)
        savedMakeupPath = currentMakeupPath
        savedMakeupKey = currentMakeupKey
        savedMakeupStrength = currentMakeupStrength
        savedStickerPath = currentStickerPath
        
        // Match Android: explicitly disable makeup and sticker
        // Android sets beautyConfig.makeUp = null and beautyConfig.sticker = null
        render.resetStyle()
        render.resetSticker()
        
        // Clear current tracking state
        currentMakeupPath = nil
        currentMakeupKey = nil
        currentMakeupStrength = 0.0
        currentStickerPath = nil
        
        // Temporarily disable beauty via beautyAPI.enable(false) in BeautyManager
        isBeautyDisabled = true
    }
    
    /// Restore previously saved beauty state
    /// This is called when user releases the bare face comparison button
    /// Match Android implementation: restore beauty switch, makeup, and sticker
    func actionBeauty() {
        guard isBeautyDisabled else {
            // Not disabled, do nothing
            return
        }
        
        // Note: Beauty is restored via beautyAPI.enable() in BeautyManager (using saved beautyAPI.isEnable state)
        
        // Restore makeup if it was saved
        // Match Android: directly set makeup, which will recreate it
        if let savedMakeup = savedMakeupPath, let savedKey = savedMakeupKey {
            currentMakeupPath = savedMakeup
            currentMakeupKey = savedKey
            currentMakeupStrength = savedMakeupStrength
            render.setStyleWithPath(savedMakeup, key: savedKey, value: savedMakeupStrength)
        }
        // Note: If savedMakeup is nil, currentMakeupPath is already nil (cleared in actionBareFace)
        
        // Restore sticker if it was saved
        if let savedSticker = savedStickerPath {
            currentStickerPath = savedSticker
            render.setStickerWithPath(savedSticker)
        }
        // Note: If savedSticker is nil, currentStickerPath is already nil (cleared in actionBareFace)
        
        isBeautyDisabled = false
    }
}
