//
//  BeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/1/16.
//

import UIKit
import AgoraRtcKit

class BeautyManager: NSObject {
    private static var _sharedManager: BeautyManager?
    static var shareManager: BeautyManager {
        get {
            if let sharedManager = _sharedManager { return sharedManager }
            let sharedManager = BeautyManager()
            _sharedManager = sharedManager
            return sharedManager
        }
        set {
            _sharedManager = nil
        }
    }
    private var agoraKit: AgoraRtcEngineKit?
    public let beautyAPI = BeautyAPI()
    
    // MARK: - Bare Face Comparison State
    
    // State for bare face comparison button (素颜对比按钮状态)
    private var isBeautyDisabled: Bool = false
    
    // Save the original beautyAPI enable state before disabling for comparison
    private var savedBeautyAPIState: Bool = true
    
    override init() {
        super.init()
    }
    
    func setup(engine: AgoraRtcEngineKit) {
        agoraKit = engine
        
    }
    
    func initBeautyRender() {
        switch BeautyModel.beautyType {
        case .byte:
            beautyAPI.beautyRender = ByteBeautyManager.shareManager.render
        case .sense:
            beautyAPI.beautyRender = SenseBeautyManager.shareManager.render
        case .fu:
            beautyAPI.beautyRender = FUBeautyManager.shareManager.render
        case .agora: break
        }
    }
    
    var isEnableBeauty: Bool = true {
        didSet {
            switch BeautyModel.beautyType {
            case .agora:
                if isEnableBeauty == false {
                    AgoraBeautyManager.shareManager.setBeauty(path: nil, key: nil, value: 0)
                }else{
                    AgoraBeautyManager.shareManager.setBeauty(path: nil, key: "init", value: 0)
                }
            default:
                beautyAPI.enable(isEnableBeauty)
            }
        }
    }
    
    func checkLicense() -> Bool {
        switch BeautyModel.beautyType {
        case .byte: return ByteBeautyManager.shareManager.isSuccessLicense
        case .sense: return SenseBeautyManager.shareManager.isSuccessLicense
        case .fu: return FUBeautyManager.shareManager.isSuccessLicense
        case .agora: return true
        }
    }
    
    func configBeautyAPI() {
        let config = BeautyConfig()
        config.rtcEngine = agoraKit
        config.captureMode = .agora
        switch BeautyModel.beautyType {
        case .byte:
            config.beautyRender = ByteBeautyManager.shareManager.render
        case .sense:
            config.beautyRender = SenseBeautyManager.shareManager.render
        case .fu:
            config.beautyRender = FUBeautyManager.shareManager.render
        case .agora:
            config.beautyRender = AgoraBeautyManager.shareManager.render
            AgoraBeautyManager.shareManager.agoraKit = agoraKit
        }
        config.statsEnable = false
        config.statsDuration = 1
        config.eventCallback = { stats in
            print("min == \(stats.minCostMs)")
            print("max == \(stats.maxCostMs)")
            print("averageCostMs == \(stats.averageCostMs)")
        }
        let result = beautyAPI.initialize(config)
        if result != 0 {
            print("initialize error == \(result)")
        }
        beautyAPI.enable(true)
    }
    
    func updateBeautyRedner() {
        guard let agoraKit = agoraKit else { return }
        configBeautyAPI()
        if BeautyModel.beautyType == .agora {
            AgoraBeautyManager.shareManager.setBeauty(path: nil, key: "init", value: 0)
        } else {
            beautyAPI.setBeautyPreset(.default)
        }
    }
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.setBeauty(path: path, key: key, value: value)
            
        case .sense:
            SenseBeautyManager.shareManager.setBeauty(path: path, key: key, value: value)
            
        case .fu:
            FUBeautyManager.shareManager.setBeauty(path: path, key: key, value: value)
            
        case .agora:
            AgoraBeautyManager.shareManager.setBeauty(path: path, key: key, value: value)
        }
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.setStyle(path: path, key: key, value: value)
            
        case .sense:
            SenseBeautyManager.shareManager.setStyle(path: path, key: key, value: value)
            
        case .fu:
            FUBeautyManager.shareManager.setStyle(path: path, key: key, value: value)
            
        case .agora: 
            AgoraBeautyManager.shareManager.setStyle(path: path, key: key, value: value)
        }
    }
    
    func setAnimoji(path: String?) {
        FUBeautyManager.shareManager.setAnimoji(path: path)
    }
    
    func setFilter(path: String?, key: String?, value: CGFloat) {
        guard let path = path else { return }
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.setFilter(path: path, value: value)
            
        case .sense:
            SenseBeautyManager.shareManager.setFilter(path: path, value: value)
            
        case .fu:
            FUBeautyManager.shareManager.setFilter(path: path, value: value)
            
        case .agora:
            AgoraBeautyManager.shareManager.setFilter(path: path, key: key, value: value)
        }
        
    }
    
    func setSticker(path: String?) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.setSticker(path: path)
            
        case .sense:
            SenseBeautyManager.shareManager.setSticker(path: path)
            
        case .fu:
            FUBeautyManager.shareManager.setSticker(path: path)
            
        case .agora: break
        }
    }
    
    func reset(datas: [BeautyModel], type: ShowBeautyFaceVCType) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.reset(datas: datas)
            
        case .sense:
            SenseBeautyManager.shareManager.reset(datas: datas)
            
        case .fu:
            FUBeautyManager.shareManager.reset(datas: datas, type: type)
            
        case .agora:
            AgoraBeautyManager.shareManager.reset(datas: datas)
        }
    }
    
    func resetStyle(datas: [BeautyModel]) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.resetStyle(datas: datas)
            
        case .sense:
            SenseBeautyManager.shareManager.resetStyle(datas: datas)
            
        case .fu:
            FUBeautyManager.shareManager.resetStyle(datas: datas)
            
        case .agora:
            AgoraBeautyManager.shareManager.resetStyle(datas: datas)
        }
    }
    
    func resetFilter(datas: [BeautyModel]) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.resetFilter(datas: datas)
            
        case .sense:
            SenseBeautyManager.shareManager.resetFilter(datas: datas)
            
        case .fu:
            FUBeautyManager.shareManager.resetFilter(datas: datas)
            
        case .agora:
            AgoraBeautyManager.shareManager.resetFilter(datas: datas)
        }
    }
    
    func resetSticker(datas: [BeautyModel]) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.resetSticker(datas: datas)
            
        case .sense:
            SenseBeautyManager.shareManager.resetSticker(datas: datas)
            
        case .fu:
            FUBeautyManager.shareManager.resetSticker(datas: datas)
            
        case .agora: break
        }
    }
    
    func destroy(isAll: Bool = true) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.destroy()
            
        case .sense:
            SenseBeautyManager.shareManager.destroy()
            
        case .fu:
            FUBeautyManager.shareManager.destroy()
            
        case .agora:
            AgoraBeautyManager.shareManager.destroy()
        }
        beautyAPI.destroy()
        guard isAll else { return }
        BeautyManager._sharedManager = nil
        ShowAgoraKitManager.shared.enableVirtualBackground(isOn: false,
                                                           greenCapacity: 0)
        ShowAgoraKitManager.shared.seVirtualtBackgoundImage(imagePath: nil, isOn: false)
        BeautyModel.beautyType = .sense
    }
    
    // MARK: - Bare Face Comparison
    
    /// Save current beauty state and disable all beauty effects
    /// This is called when user presses the bare face comparison button
    func actionBareFace() {
        // Prevent duplicate calls
        guard !isBeautyDisabled else {
            return
        }
        
        switch BeautyModel.beautyType {
        case .byte:
            // For Byte beauty, save state and disable beauty
            // Save current beautyAPI state before disabling
            savedBeautyAPIState = beautyAPI.isEnable
            ByteBeautyManager.shareManager.actionBareFace()
            
        case .sense:
            // For Sense beauty, save state and disable beauty
            // Save current beautyAPI state before disabling
            savedBeautyAPIState = beautyAPI.isEnable
            SenseBeautyManager.shareManager.actionBareFace()
            
        case .fu:
            // For FU beauty, save state and disable beauty via beautyAPI
            // Save current beautyAPI state before disabling
            savedBeautyAPIState = beautyAPI.isEnable
            FUBeautyManager.shareManager.actionBareFace()
            beautyAPI.enable(false)
            
        case .agora:
            // For Agora beauty, use the specific actionBareFace method
            // Save current beauty state (Agora manages its own state)
            savedBeautyAPIState = isEnableBeauty
            AgoraBeautyManager.shareManager.actionBareFace()
        }
        
        isBeautyDisabled = true
    }
    
    /// Restore previously saved beauty state
    /// This is called when user releases the bare face comparison button
    func actionBeauty() {
        // Only restore if beauty was disabled
        guard isBeautyDisabled else {
            return
        }
        
        switch BeautyModel.beautyType {
        case .byte:
            // For Byte beauty, restore saved state
            ByteBeautyManager.shareManager.actionBeauty()
            // Restore the original beautyAPI state (may be false if user selected "no effect")
            beautyAPI.enable(savedBeautyAPIState)
            
        case .sense:
            // For Sense beauty, restore saved state
            SenseBeautyManager.shareManager.actionBeauty()
            // Restore the original beautyAPI state (may be false if user selected "no effect")
            beautyAPI.enable(savedBeautyAPIState)
            
        case .fu:
            // For FU beauty, restore saved state via beautyAPI
            FUBeautyManager.shareManager.actionBeauty()
            // Restore the original beautyAPI state (may be false if user selected "no effect")
            beautyAPI.enable(savedBeautyAPIState)
            
        case .agora:
            // For Agora beauty, use the specific actionBeauty method
            AgoraBeautyManager.shareManager.actionBeauty()
            // Restore the original beauty state (may be false if user selected "no effect")
            isEnableBeauty = savedBeautyAPIState
        }
        
        isBeautyDisabled = false
    }
}
