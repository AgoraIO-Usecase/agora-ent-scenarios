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
}
