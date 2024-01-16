//
//  AgoraBeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/11/10.
//

import Foundation

class AgoraBeautyManager: NSObject {
    var agoraKit: AgoraRtcEngineKit?
    lazy var render = AgoraBeautyRender()
    private static var _sharedManager: AgoraBeautyManager?
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
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        if key == "init" {
            agoraKit?.enableExtension(withVendor: "agora_video_filters_clear_vision",
                                      extension: "clear_vision",
                                      enabled: true,
                                      sourceType: .primaryCamera)
        }
        switch key ?? "" {
        case "smoothnessLevel": beautifyOption.smoothnessLevel = Float(value)
        case "lighteningLevel": beautifyOption.lighteningLevel = Float(value)
        case "rednessLevel": beautifyOption.rednessLevel = Float(value)
        case "sharpnessLevel": beautifyOption.sharpnessLevel = Float(value)
        default: break
        }
        agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
    }
    
    func reset(datas: [BeautyModel]) {
        agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
    }
            
    func destroy() {
        agoraKit?.enableExtension(withVendor: "agora_video_filters_clear_vision",
                                  extension: "clear_vision",
                                  enabled: false,
                                  sourceType: .primaryCamera)
        agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
        AgoraBeautyManager._sharedManager = nil
    }
}
