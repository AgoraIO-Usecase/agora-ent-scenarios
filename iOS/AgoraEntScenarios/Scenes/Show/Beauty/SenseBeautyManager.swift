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
    private var isSuccessLicense: Bool = false {
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
    
    private func checkLicense() {
        let licensePath = Bundle.main.path(forResource: "SENSEME", ofType: "lic")
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
        guard let key = UInt32(key ?? "0") else { return }
        processor.setEffectType(key, value: Float(value))
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat) {
        guard let path = path, !path.isEmpty, let key = key else { return }
        processor.addStylePath(path, groupId: key == "Makeup_ALL" ? 0 : 1, strength: value) { [weak self] stickerId in
            guard let self = self else { return }
            self.styleId = stickerId
        }
    }
    
    func setFilter(path: String?, value: CGFloat) { }
    
    func setSticker(path: String?) {
        if let path = path {
            processor.removeStickerId(stickerId)
            processor.setStickerWithPath(path) { [weak self] stickerId in
                self?.stickerId = stickerId
            }
        } else {
            resetSticker(datas: ShowBeautyFaceVC.stickerData)
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
        SenseBeautyManager._sharedManager = nil
        datas.removeAll()
        reset(datas: ShowBeautyFaceVC.beautyData)
        reset(datas: ShowBeautyFaceVC.adjustData)
        resetStyle(datas: ShowBeautyFaceVC.styleData)
        resetSticker(datas: ShowBeautyFaceVC.stickerData)
        resetFilter(datas: ShowBeautyFaceVC.filterData)
        ShowBeautyFaceVC.backgroundData.forEach({
            $0.isSelected = $0.path == nil
        })
    }
}
