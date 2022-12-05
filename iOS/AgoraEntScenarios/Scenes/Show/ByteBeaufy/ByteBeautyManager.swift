//
//  ByteBeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2022/11/18.
//

import UIKit

class ByteBeautyManager {
    private lazy var processor: BEFrameProcessor = {
        let context = EAGLContext(api: .openGLES2)
        let processor = BEFrameProcessor(context: context, resourceDelegate: nil)
        processor?.setEffectOn(true)
        processor?.updateComposerNodes(beautyNodes)
        return processor!
    }()
    private var beautyNodes = ["/beauty_IOS_lite", "/reshape_lite", "/beauty_4Items"] {
        didSet {
            processor.updateComposerNodes(beautyNodes)
        }
    }
    
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
                                              intensity: value)
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat) {
        guard let path = path, let key = key else { return }
        if !path.isEmpty, !beautyNodes.contains(path) {
            beautyNodes.append(path)
        }
        processor.updateComposerNodeIntensity(path,
                                              key: key,
                                              intensity: value)
    }
    
    func setFilter(path: String?, value: CGFloat) {
        guard let path = path else { return }
        processor.setFilterPath(path)
        processor.setFilterIntensity(Float(value))
    }
    
    func setSticker(path: String?) {
        processor.setStickerPath(path ?? "")
    }
    
    func setCameraPosition(isFront: Bool) {
        processor.setCameraPosition(isFront)
    }
    
    func reset(datas: [ByteBeautyModel], key: String? = nil) {
        datas.forEach({
            $0.isSelected = $0.path == nil
            guard $0.path != nil else { return }
            processor.updateComposerNodeIntensity($0.path,
                                                  key: key ?? $0.key,
                                                  intensity: 0)
        })
    }
    
    func resetFilter(datas: [ByteBeautyModel]) {
        datas.forEach({ item in
            item.isSelected = item.path == nil
            setFilter(path: item.path, value: 0)
        })
    }
    
    func resetSticker(datas: [ByteBeautyModel]) {
        datas.forEach({ item in
            item.isSelected = item.path == nil
            setSticker(path: "")
        })
    }
    
    func processFrame(pixelBuffer: CVPixelBuffer?) -> CVPixelBuffer? {
        if !isEnableBeauty { return pixelBuffer }
        guard let pixel = pixelBuffer else { return nil }
        let result = processor.process(pixel,
                                       timeStamp: Date().timeIntervalSince1970)
        return result?.pixelBuffer
    }
    
    func destroy() {
        ByteBeautyManager._sharedManager = nil
        reset(datas: ShowBeautyFaceVC.beautyData)
        reset(datas: ShowBeautyFaceVC.styleData)
        resetSticker(datas: ShowBeautyFaceVC.stickerData)
        resetFilter(datas: ShowBeautyFaceVC.filterData)
    }
}
