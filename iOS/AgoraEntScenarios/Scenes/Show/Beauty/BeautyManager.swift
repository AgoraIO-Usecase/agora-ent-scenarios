//
//  BeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/1/16.
//

import UIKit

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
    
    public let beautyAPI = BeautyAPI()
    
    override init() {
        super.init()
    
        switch BeautyModel.beautyType {
        case .byte:
            beautyAPI.beautyRender = ByteBeautyManager.shareManager.render
        case .sense:
            beautyAPI.beautyRender = SenseBeautyManager.shareManager.render
        case .fu:
            beautyAPI.beautyRender = FUBeautyManager.shareManager.render
        }
    }
    
    var isEnableBeauty: Bool = true {
        didSet {
            beautyAPI.enable(isEnableBeauty)
        }
    }
    
    func configBeautyAPIWithRtcEngine(engine: AgoraRtcEngineKit) {
        let config = BeautyConfig()
        config.rtcEngine = engine
        config.captureMode = .agora
        switch BeautyModel.beautyType {
        case .byte:
            config.beautyRender = ByteBeautyManager.shareManager.render
        case .sense:
            config.beautyRender = SenseBeautyManager.shareManager.render
        case .fu:
            config.beautyRender = FUBeautyManager.shareManager.render
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
        beautyAPI.initialize(config)
        beautyAPI.enable(true)
    }
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.setBeauty(path: path, key: key, value: value)
            
        case .sense:
            SenseBeautyManager.shareManager.setBeauty(path: path, key: key, value: value)
            
        case .fu:
            FUBeautyManager.shareManager.setBeauty(path: path, key: key, value: value)
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
        }
    }
    
    func setAnimoji(path: String?) {
        FUBeautyManager.shareManager.setAnimoji(path: path)
    }
    
    func setFilter(path: String?, value: CGFloat) {
        guard let path = path else { return }
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.setFilter(path: path, value: value)
            
        case .sense:
            SenseBeautyManager.shareManager.setFilter(path: path, value: value)
            
        case .fu:
            FUBeautyManager.shareManager.setFilter(path: path, value: value)
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
        }
    }
    
    func reset(datas: [BeautyModel]) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.reset(datas: datas)
            
        case .sense:
            SenseBeautyManager.shareManager.reset(datas: datas)
            
        case .fu:
            FUBeautyManager.shareManager.reset(datas: datas)
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
        }
    }
    
    func destroy() {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.destroy()
            
        case .sense:
            SenseBeautyManager.shareManager.destroy()
            
        case .fu:
            FUBeautyManager.shareManager.destroy()
        }
        beautyAPI.destroy()
        BeautyManager._sharedManager = nil
        ShowAgoraKitManager.shared.enableVirtualBackground(isOn: false,
                                                           greenCapacity: 0)
        ShowAgoraKitManager.shared.seVirtualtBackgoundImage(imagePath: nil, isOn: false)
    }
}
