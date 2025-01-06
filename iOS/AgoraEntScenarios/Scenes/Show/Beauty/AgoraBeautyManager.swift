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
    private lazy var filterOption = AgoraFilterEffectOptions()
    private lazy var faceshapeOption = AgoraFaceShapeBeautyOptions()
    
    static func castFromPositive100(_ value: Int32) -> Float {
        return 0.01 * Float(value);
    }
    
    static func castToPositive100(_ value: Float) -> Int32 {
        return Int32(roundf(100.0 * value));
    }
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        if key == nil {
            agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
            agoraKit?.setFaceShapeBeautyOptions(false, options: faceshapeOption)
            agoraKit?.setFilterEffectOptions(false, options: filterOption)
            self.updateMakeupOptions(dict: ["enable_mu": false] as [String : Any])
        }
        else if key == "init" {
            agoraKit?.enableExtension(withVendor: "agora_video_filters_clear_vision",
                                      extension: "clear_vision",
                                      enabled: true,
                                      sourceType: .primaryCamera)
            agoraKit?.setBeautyEffectOptions(true, options: beautifyOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        }
        switch key ?? "" {
        case "smoothnessLevel":
            beautifyOption.smoothnessLevel = Float(value)
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "lighteningLevel":
            beautifyOption.lighteningLevel = Float(value)
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "rednessLevel":
            beautifyOption.rednessLevel = Float(value)
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "sharpnessLevel":
            beautifyOption.sharpnessLevel = Float(value)
            agoraKit?.setBeautyEffectOptions(key != nil, options: beautifyOption)
        break
        case "headscale":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.headScale
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "forehead":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.forehead
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        case "facecontour":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.faceContour
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "facewidth":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.faceWidth
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "facelength":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.faceLength
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "cheekbone":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.cheekbone
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "cheek":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.cheek
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "chin":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.chin
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "eyescale":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.eyeScale
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "noselength":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.noseLength
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "nosewidth":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.noseWidth
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "mouthscale":
            let areaOption = AgoraFaceShapeAreaOptions()
            areaOption.shapeArea = AgoraFaceShapeArea.mouthScale
            areaOption.shapeIntensity = AgoraBeautyManager.castToPositive100(Float(value))
            agoraKit?.setFaceShapeAreaOptions(areaOption)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
        break
        case "gentlemaneface":
            faceshapeOption.shapeStyle = .male
            faceshapeOption.styleIntensity = Int32(value)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
            break
        case "ladyface":
            faceshapeOption.shapeStyle = .female
            faceshapeOption.styleIntensity = Int32(value)
            agoraKit?.setFaceShapeBeautyOptions(true, options: faceshapeOption)
            break
        default: break
        }
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat) {
        if key == "init" {
            agoraKit?.enableExtension(withVendor: "agora_video_filters_clear_vision",
                                      extension: "clear_vision",
                                      enabled: true,
                                      sourceType: .primaryCamera)
        }
        switch key ?? "" {
        case "makeup1":
            self.updateMakeupOptions(dict: [
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
                "lipStrength": value] as [String : Any])
        break
        case "makeup2":
            self.updateMakeupOptions(dict: [
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
                "lipStrength": value] as [String : Any])
        break
        default:
            self.updateMakeupOptions(dict: [
                "enable_mu": false
                ] as [String : Any])
        break
        }
    }
    
    func setFilter(path: String?, key: String?, value: CGFloat) {
        if key == "init" {
            agoraKit?.enableExtension(withVendor: "agora_video_filters_clear_vision",
                                      extension: "clear_vision",
                                      enabled: true,
                                      sourceType: .primaryCamera)
        }
        switch key ?? "" {
        case "yuansheng":
            filterOption.strength = Float(value)
            filterOption.path = getLutBundlePath("yuansheng32")
            agoraKit?.setFilterEffectOptions(true, options: filterOption)
        break
        case "lengbai":
            filterOption.strength = Float(value)
            filterOption.path = getLutBundlePath("lengbai32")
            agoraKit?.setFilterEffectOptions(true, options: filterOption)
        break
        case "nenbai":
            filterOption.strength = Float(value)
            filterOption.path = getLutBundlePath("lengbai32")
            agoraKit?.setFilterEffectOptions(true, options: filterOption)
        break
        default:
            agoraKit?.setFilterEffectOptions(false, options: filterOption)
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
            
    func destroy() {
        agoraKit?.enableExtension(withVendor: "agora_video_filters_clear_vision",
                                  extension: "clear_vision",
                                  enabled: false,
                                  sourceType: .primaryCamera)
        agoraKit?.setBeautyEffectOptions(false, options: beautifyOption)
        agoraKit?.setFaceShapeBeautyOptions(false, options: faceshapeOption)
        agoraKit?.setFilterEffectOptions(false, options: filterOption)
        self.updateMakeupOptions(dict: [
            "enable_mu": false
            ] as [String : Any])
        AgoraBeautyManager._sharedManager = nil
    }
    
    private func getLutBundlePath(_ name: String) -> String? {
        guard let bundlePath = Bundle.main.path(forResource: "BeautyResource", ofType: "bundle"),
              let bundle = Bundle(path: bundlePath) else {
            return nil
        }
        return bundle.path(forResource: name, ofType: "cube")
    }
}
