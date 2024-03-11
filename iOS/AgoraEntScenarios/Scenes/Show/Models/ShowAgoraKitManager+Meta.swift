//
//  ShowAgoraKitManager+Meta.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2024/3/6.
//

import Foundation

private let company_id = "agoraDemo"
private let license = "HIYWHc7P5x+IEm8H11NlEKFdAWOYc+rFIyhQ/QEDomcQhBoQrlHpMWqL9+HGAOBjBWZtcPHjol4NsRDlUo6UQ85ib/XSH+1MFlpZT/r5nCTADTkOc1PqfMKic4gNJr4sb3PD3v4EaZ89tZoYMw0LM6rdjt1ySH+8yaWKS+hKKHY="

extension ShowAgoraKitManager {
    func registerMetaPlugin(){
        // MetaKit
        let agoraKit = engine
        agoraKit?.registerExtension(withVendor: "agora_video_filters_face_capture", extension: "face_capture", sourceType: AgoraMediaSourceType.primaryCamera)
        agoraKit?.registerExtension(withVendor: "agora_video_filters_metakit",extension: "metakit", sourceType: AgoraMediaSourceType.primaryCamera)
        
        //
        agoraKit?.setParameters("{\"rtc.video.seg_before_exts\":true}")
        let bg_src = AgoraVirtualBackgroundSource()
        bg_src.backgroundSourceType = .none
        let seg_prop = AgoraSegmentationProperty()
        seg_prop.modelType = .agoraAi
        agoraKit?.enableVirtualBackground(true, backData: bg_src, segData: seg_prop)
        
        // (license)
        agoraKit?.enableExtension(withVendor:
        "agora_video_filters_face_capture", extension: "face_capture", enabled:
        true)
        agoraKit?.setExtensionPropertyWithVendor("agora_video_filters_face_capture",extension:"face_capture",key:"authentication_information",value: "{\"company_id\":\"\(company_id)\", \"license\":\"\(license)\"}",sourceType:AgoraMediaSourceType.primaryCamera)
        // metakit
        agoraKit?.enableExtension(withVendor: "agora_video_filters_metakit",extension: "metakit", enabled: true)
        agoraKit?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit", key:"initialize", value:"{}")
    }
    
    func loadScene(scenePath:String){
        // unity
        let info_dict = ["sceneInfo" : ["scenePath" : scenePath]] as [String : Any]
        let info_data = try? JSONSerialization.data(withJSONObject: info_dict,
        options: [])
        let info_str = String(data: info_data!, encoding: String.Encoding.utf8)
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit",key:"loadScene",value:info_str!)
    }
    
    func createSceneView(){
        metakit = MetaKitEngine.sharedInstance()
        var frame = CGRect(x: 0, y: 0, width: 225, height: 400);
        let sceneView = metakit?.createSceneView(frame)
        let address = unsafeBitCast(sceneView!, to: Int64.self)
        let value = 1
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit", key:"enableSceneVideo",value:"{\"view\":\"\(address)\",\"enable\":\(value)}")
        self.sceneView = sceneView
    }
    
    func removeSceneView(view: UIView){
        let address = unsafeBitCast(view, to: Int64.self)
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit",key:"removeSceneView",value:"{\"view\":\"\(address)\"}")
    }
    
    func leaveScene(){
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit",key:"unloadScene", value:"{}")
    }
    
    func destoryMate(){
        // view
        sceneView?.removeFromSuperview()
        metakit?.removeSceneView(sceneView)
        //
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit",key:"destroy", value:"{}")
    }
 
}

extension ShowAgoraKitManager: AgoraMediaFilterEventDelegate {
    func onEvent(_ provider: String?, extension extensionStr: String?, key: String?, value: String?) {
        if (provider == "agora_video_filters_metakit" && extensionStr == "metakit") {
            showLogger.info(" agora_video_filters_metakit ----onEvent ------ status:\(key ?? "")")
        }
    }
    
    func onExtensionError(_ provider: String?, extension extensionStr: String?, error: Int32, message: String?) {
        if provider == "agora_video_filters_metakit" && extensionStr == "metakit" {
            showLogger.error("[MetaKit] onExtensionError, Code: \(error), Message: \(message ?? "")")
         }
    }
}

extension ShowAgoraKitManager {
    func showAnimoji(quality: Int = 1){
        guard let path = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true).first else {
            return
        }
        loadScene(scenePath: "\(path)/assets/Animoji")
        createSceneView()
        guard let sceneView = self.sceneView else { return }
        
        let address = unsafeBitCast(sceneView, to: Int64.self)
        //
        let resoultionW: Int = 720
        let resoultionH: Int = 1280
        // animoji
        let animoji = "dog"
        let extra_dict = ["sceneIndex": 0, "avatar": animoji, "avatarMode": 1]
        as [String : Any]
        let extra_data = try? JSONSerialization.data(withJSONObject:extra_dict, options: [])
        let extra_str = String(data: extra_data!, encoding: String.Encoding.utf8) ?? ""
        let dict = ["view": String(address), "config": ["width": resoultionW,
        "height": resoultionH, "extraInfo": extra_str] as [String : Any]] as
        [String : Any]
        let data = try? JSONSerialization.data(withJSONObject: dict, options:
        [])
        let data_str = String(data: data!, encoding: String.Encoding.utf8)
        // view
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit",key:"addSceneView",value: data_str!)
        // animojiquality 0- 1- 2-
        let value = try? JSONSerialization.data(withJSONObject: ["general": quality], options:[])
        let str = String(data: value!, encoding: String.Encoding.utf8) ?? ""
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit",key: "setRenderQuality",value: str)
        // animoji
        let name = "dog"
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit", key:"switchAvatarMode",value:"{\"mode\":\(1),\"viewAddress\":\"\(address)\",\"avatar\":\"\(name)\"}")
    }
    
    func showBackgroudEffective(){
        guard let path = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true).first else {
            return
        }
        loadScene(scenePath: "\(path)/assets/Animoji")
        createSceneView()
        guard let sceneView = self.sceneView else { return }
        
        let address = unsafeBitCast(sceneView, to: Int64.self)
        //
        let resoultionW: Int = 720
        let resoultionH: Int = 1280
        //
        let extra_dict = ["sceneIndex": 0, "backgroundEffect": true] as [String
        : Any]
        let extra_data = try? JSONSerialization.data(withJSONObject:
        extra_dict, options: [])
        let extra_str = String(data: extra_data!, encoding: String.Encoding.utf8) ?? ""
        let dict = ["view": String(address), "config": ["width": resoultionW,
        "height": resoultionH, "extraInfo": extra_str] as [String : Any]] as [String : Any]
        let data = try? JSONSerialization.data(withJSONObject: dict, options:[])
        let data_str = String(data: data!, encoding: String.Encoding.utf8)
        // view
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit", key:"addSceneView",value: data_str!)
        // 2d/360
//        var bg_dict: [String: Any]
//        if (mode == "pic2d") {
//         bg_dict = ["mode" : mode, "param" : ["path": bgPath]] as [String : Any]
//        } else if(paramset == "tex360") {
//         bg_dict = ["mode": mode, "param" : ["path": panoPath, "rotation": "0"]] as [String : Any]
//        }
//        let bg_data = try? JSONSerialization.data(withJSONObject: bg_dict,
//        options: [.withoutEscapingSlashes])
//        let bg_info = String(data: bg_data!, encoding: String.Encoding.utf8)
//        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",
//        extension: "metakit",
//         key:"setBGVideo",
//         value:bg_info!)
        // 3603d
        let gyro_dict = ["state": "on"] as [String : String]
        let gyro_data = try? JSONSerialization.data(withJSONObject: gyro_dict,options: [])
        let gyro_info = String(data: gyro_data!, encoding: String.Encoding.utf8)
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",key:"setCameraGyro",value:gyro_info!)
    }
}

extension ShowAgoraKitManager {
    
    enum RhythmMode: Int {
        case heart = 1 // 心跳律动
        case portrait = 2 // 效果描述：焦距缓慢变大后再变小，在最小的时候，图像会有轻微的抖动，同时叠加一个人像虚影。
        case front_back = 3 // 效果描述：焦距平缓但不均匀地变大再变小。
        case up_down = 4 // 效果描述：镜头首先向上移动，然后再向下移动。
        case left_right = 5 // 效果描述：镜头首先向左移动，然后再向右移动。
        case faceLock_L = 6 // 效果描述：脸部被锁定在视频中间。
        case faceLock_P = 7 // 效果描述：脸部被锁定在固定点位（视频画面中上部 2/5 处）
    }
    
    
    /// 开关律动
    func enableRhythm(_ enable: Bool){
        engine?.enableExtension(withVendor: "agora_video_filters_portrait_rhythm", extension: "portrait_rhythm", enabled: enable)
    }
    
    /// 切换律动模式
    func swithRhythm(mode: RhythmMode) {
        engine?.setExtensionPropertyWithVendor("agora_video_filters_portrait_rhythm", extension: "portrait_rhythm", key: "mode", value: "\(mode.rawValue)")
        enableRhythm(true)
    }
}
