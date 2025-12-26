//
//  ByteBeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/18.
//

import UIKit

class ByteBeautyManager {
    public lazy var render = BytesBeautyRender()
    
    private var processor: BEEffectManager {
        return render.effectManager
    }
    
    private var beautyNodes = ["/beauty_IOS_lite", "/reshape_lite", "/beauty_4Items"] {
        didSet {
            processor.updateComposerNodes(beautyNodes)
        }
    }
    private var stylePath: String?
    
    // MARK: - Bare Face Comparison State
    
    // Saved state for bare face comparison
    // Match Android FaceUnity implementation: save beauty switch, makeup (style), and sticker
    private var savedBeautyState: Bool = false
    private var savedStylePath: String? = nil  // makeup (style) path
    private var savedStickerPath: String? = nil  // sticker path
    private var isBeautyDisabled: Bool = false
    
    // Track current sticker path (we need to track it when setSticker is called)
    private var currentStickerPath: String? = nil
    
    private static var _sharedManager: ByteBeautyManager?
    static var shareManager: ByteBeautyManager {
        get {
            if let sharedManager = _sharedManager { return sharedManager }
            let sharedManager = ByteBeautyManager()
            _sharedManager = sharedManager
            return sharedManager
        }
        set {
            _sharedManager = nil
        }
    }
    
    var isEnableBeauty: Bool = true
    
    var isSuccessLicense: Bool {
        processor.isSuccessLicense
    }
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        guard let path = path, let key = key else { return }
        if !path.isEmpty, !beautyNodes.contains(path) {
            beautyNodes.append(path)
        }
        processor.updateComposerNodeIntensity(path,
                                              key: key,
                                              intensity: Float(value))
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat) {
        guard let path = path, !path.isEmpty, let key = key else { return }
        if let stylePath = stylePath, let index = beautyNodes.firstIndex(of: stylePath) {
            beautyNodes.remove(at: index)
        }
        if !beautyNodes.contains(path) {
            beautyNodes.append(path)
        }
        processor.updateComposerNodeIntensity(path,
                                              key: key,
                                              intensity: Float(value))
        stylePath = path
    }
    
    func setFilter(path: String?, value: CGFloat) {
        guard let path = path else { return }
        processor.setFilterPath(path)
        processor.setFilterIntensity(Float(value))
    }
    
    func setSticker(path: String?) {
        processor.setStickerPath(path ?? "")
        
        // Track current sticker path for bare face comparison
        currentStickerPath = path
    }
    
    func reset(datas: [BeautyModel]) {
        datas.forEach({
            $0.isSelected = $0.key == "smooth"
            guard $0.path != nil else { return }
            processor.updateComposerNodeIntensity($0.path,
                                                  key: $0.key,
                                                  intensity: 0)
        })
    }
    
    func resetStyle(datas: [BeautyModel]) {
        datas.forEach({
            $0.isSelected = $0.path == nil
            guard $0.path != nil else { return }
            processor.updateComposerNodeIntensity($0.path,
                                                  key: $0.key,
                                                  intensity: 0)
            if let index = beautyNodes.firstIndex(of: $0.path ?? "") {
                beautyNodes.remove(at: index)
            }
        })
        stylePath = nil
    }
    
    func resetFilter(datas: [BeautyModel]) {
        datas.forEach({ item in
            item.isSelected = item.path == nil
            setFilter(path: item.path, value: 0)
        })
    }
    
    func resetSticker(datas: [BeautyModel]) {
        datas.forEach({ item in
            item.isSelected = item.path == nil
            setSticker(path: "")
        })
    }
    
    func destroy() {
        render.destroy()
        ByteBeautyManager._sharedManager = nil
    }
    
    // MARK: - Bare Face Comparison
    
    /// Save current beauty state and disable all beauty effects
    /// This is called when user presses the bare face comparison button
    /// Match Android FaceUnity implementation: save beauty switch, makeup (style), and sticker
    func actionBareFace() {
        guard !isBeautyDisabled else {
            // Already disabled, do nothing
            return
        }
        
        // Save current state (beauty switch, style/makeup, and sticker)
        savedBeautyState = isEnableBeauty
        savedStylePath = stylePath
        savedStickerPath = currentStickerPath
        
        // Temporarily disable beauty by setting isEnableBeauty to false
        isEnableBeauty = false
        
        // Disable style (makeup) - remove from nodes
        if let stylePath = stylePath, let index = beautyNodes.firstIndex(of: stylePath) {
            beautyNodes.remove(at: index)
        }
        stylePath = nil
        
        // Disable sticker
        processor.setStickerPath("")
        currentStickerPath = nil
        
        isBeautyDisabled = true
    }
    
    /// Restore previously saved beauty state
    /// This is called when user releases the bare face comparison button
    /// Match Android FaceUnity implementation: restore beauty switch, makeup (style), and sticker
    func actionBeauty() {
        guard isBeautyDisabled else {
            // Not disabled, do nothing
            return
        }
        
        // Restore beauty switch
        isEnableBeauty = savedBeautyState
        
        // Restore style (makeup) if it was saved
        if let savedStyle = savedStylePath {
            stylePath = savedStyle
            if !beautyNodes.contains(savedStyle) {
                beautyNodes.append(savedStyle)
            }
        }
        
        // Restore sticker if it was saved
        if let savedSticker = savedStickerPath {
            currentStickerPath = savedSticker
            processor.setStickerPath(savedSticker)
        } else {
            currentStickerPath = nil
        }
        
        isBeautyDisabled = false
    }
}
