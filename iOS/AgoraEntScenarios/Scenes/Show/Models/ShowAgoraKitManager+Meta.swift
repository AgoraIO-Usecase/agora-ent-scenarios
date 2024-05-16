//
//  ShowAgoraKitManager+Meta.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2024/3/6.
//

import Foundation
import AGResourceManager

extension ShowAgoraKitManager {
    
    func registerMetaPlugin(){
        // 背景分割
        let ret = engine?.setParameters("{\"rtc.video.seg_before_exts\":true}")
        showLogger.info("registerMetaPlugin setParameters ret:\(ret ?? -9999)")
        enableAIVirtualBackground()
        // metakit
        engine?.registerExtension(withVendor: "agora_video_filters_metakit",extension: "metakit", sourceType: AgoraMediaSourceType.primaryCamera)
        engine?.enableExtension(withVendor: "agora_video_filters_metakit",extension: "metakit", enabled: true)
    }
    
    func initializeMeta(){
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit", key:"initialize", value:"{}")
    }
    
    //3. 等待插件注册完成后加载资源
    private func setupMetaKitEngine() {
        let metakit: MetaKitEngine = MetaKitEngine.sharedInstance()
        guard let metaView = metakit.createSceneView(UIScreen.main.bounds) else { return }
        
        self.sceneView = metaView
        let address = unsafeBitCast(metaView, to: Int64.self)
        let value = 1//enable ? 1 : 0
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",
                                                     key:"enableSceneVideo",
                                                     value:"{\"view\":\"\(address)\",\"enable\":\(value)}")
        
        // 指定渲染器输出分辨率
        let resoultionW: Int = 720
        let resoultionH: Int = 1280
        // 指定背景特效能力
        let extra_dict = ["sceneIndex": 0, "backgroundEffect": true] as [String : Any]
        guard let extra_data = try? JSONSerialization.data(withJSONObject: extra_dict, options: []) else {return}
        let extra_str = String(data: extra_data, encoding: String.Encoding.utf8) ?? ""
        let dict = ["view": String(address), 
                    "config": ["width": resoultionW, "height": resoultionH, "extraInfo": extra_str] as [String : Any]
        ] as [String : Any]
        
        guard let data = try? JSONSerialization.data(withJSONObject: dict, options: []) , let data_str = String(data: data, encoding: String.Encoding.utf8) else {
            return
        }
        // 把view的能力指定给渲染器
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",
                                                     key:"addSceneView",
                                                     value: data_str)
        self.metakit = metakit
    }
    
    private func loadScene(scenePath:String){
        // unity
        let info_dict = ["sceneInfo" : ["scenePath" : scenePath]] as [String : Any]
        let info_data = try? JSONSerialization.data(withJSONObject: info_dict,options: [])
        let info_str = String(data: info_data!, encoding: String.Encoding.utf8)
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit",key:"loadScene",value:info_str!)
        setupMetaKitEngine()
    }
    
    func unloadScene(){
        if let metaView = self.sceneView {
            let address = unsafeBitCast(metaView, to: Int64.self)
            engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",
                                                     key:"removeSceneView",
                                                     value:"{\"view\":\"\(address)\"}")
            metakit?.removeSceneView(metaView)
        }
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",
                                                 key:"unloadScene", value:"{}")
    }
    
    func destroyScene(){
        self.sceneView?.removeFromSuperview()
        self.sceneView = nil;
        self.metakit?.removeSceneView(self.sceneView)
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",key:"destroy", value:"{}")
        self.metakit = nil
    }
}

extension ShowAgoraKitManager: AgoraMediaFilterEventDelegate {
    func onEvent(_ provider: String?, extension extensionStr: String?, key: String?, value: String?) {
        if (provider == "agora_video_filters_metakit" && extensionStr == "metakit") {
            guard let status = key else {
                return
            }
            DispatchQueue.main.async {
                if status == "unityLoadFinish" {
                    self.loadScene()
                } else if status == "addSceneViewResp"{
//                    self.setupBGVideo()
                } else if status == "unloadSceneResp"{
                    self.destroyScene()
                }
            }
            showLogger.info(" agora_video_filters_metakit ----onEvent ------ status:\(key ?? ""), value = \(value ?? "")")
        }
    }
    
    func onExtensionError(_ provider: String?, extension extensionStr: String?, error: Int32, message: String?) {
        if provider == "agora_video_filters_metakit" && extensionStr == "metakit" {
            showLogger.error("[MetaKit] onExtensionError, Code: \(error), Message: \(message ?? "")")
         }
    }
}

extension ShowAgoraKitManager {
    
    enum Effect3DType: Int {
        case ad_light = 3002 // 广告灯
        case face_border_light = 2001
        case ai_3d_light = 1001 // ai3D打光
        case ai_3d_light_virtual_bg = 4001 // 3D打光+虚拟背景
        case polar_light = 1003 // 极光
    }
    
    //设置特效
    func setOnEffect3D(type:Effect3DType) {
        if self.metakit == nil {
            self.setupMetaKitEngine()
        }
    
        let light_dict = [
            "id": type.rawValue, // 指定特效素材 ID 为 2001，即紫色火焰
            "enable": true // 指定 enable 为 true，即启用人像边缘火焰效果
        ] as [String: Any]

        let light_data = try? JSONSerialization.data(withJSONObject: light_dict, options: [])
        let light_info = String(data: light_data!, encoding: String.Encoding.utf8)
        
        // 根据 JSON 配置添加人像边缘火焰效果
//        enableAIVirtualBackground()
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit",key: "setEffectVideo",value: light_info!)
    }
    
    func setupBGVideo() {
        // 背景特效3D需要提前准备好相应的图资源
        let bg_dict = ["mode" : "off"]
        let bg_data = try? JSONSerialization.data(withJSONObject: bg_dict, options: [.withoutEscapingSlashes])
        let bg_info = String(data: bg_data!, encoding: String.Encoding.utf8)
        
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",
                                                     key:"setBGVideo",
                                                     value:bg_info!)
        
        
        // 对于360全景、3d最好打开陀螺仪，才能体验其效果
        let gyro_dict = ["state": "on"] as [String : String]
        let gyro_data = try? JSONSerialization.data(withJSONObject: gyro_dict, options: [])
        let gyro_info = String(data: gyro_data!, encoding: String.Encoding.utf8)
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",key:"setCameraGyro",value:gyro_info!)
    }
    
    
    // 开关特效
    func setOffEffect3D(type:Effect3DType) {
            // 关闭
        let close_dict = ["id": type.rawValue, "enable": false] as [String : Any]
        let close_data = try? JSONSerialization.data(withJSONObject: close_dict, options: [])
        let close_info = String(data: close_data!, encoding: String.Encoding.utf8)
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",key:"setEffectVideo", value:close_info!)
    }
}

extension ShowAgoraKitManager {
    
    private func enableAIVirtualBackground(){
        let bg_src = AgoraVirtualBackgroundSource()
        bg_src.backgroundSourceType = .none
        let seg_prop = AgoraSegmentationProperty()
        seg_prop.modelType = .agoraAi
        engine?.enableVirtualBackground(true, backData: bg_src, segData: seg_prop)
    }
    
    // 开关特效
    func setupBackground360(enabled:Bool) {
        var bg_dict : [String : Any] = ["mode": "off"]
        var gyro_dict:  [String : String] = ["state": "off"]
        if enabled {
            // 开启
            guard let imagePath = effectBgImgPath else { return }
//            enableAIVirtualBackground()
            bg_dict = ["mode": "tex360", "param" : ["path": "\(imagePath)/pano.jpg", "rotation": "0"]] as [String : Any]
            // 开启 对于360全景、3d最好打开陀螺仪，才能体验其效果
            gyro_dict = ["state": "on"]
        }
        
        let bg_data = try? JSONSerialization.data(withJSONObject: bg_dict, options: [.withoutEscapingSlashes])
        let bg_info = String(data: bg_data!, encoding: String.Encoding.utf8)
        
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",
                                                     key:"setBGVideo",
                                                     value:bg_info!)
        
        
        let gyro_data = try? JSONSerialization.data(withJSONObject: gyro_dict, options: [])
        let gyro_info = String(data: gyro_data!, encoding: String.Encoding.utf8)
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",
                                                     key:"setCameraGyro",
                                                     value:gyro_info!)
        enableVirtualBg360 = enabled
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

extension ShowAgoraKitManager {
    
    // 基础资源文件
    private var baseResourceFile: AGResource? {
        AGResourceManager.shared.getResource(uri: "DefaultPackage")
    }
    
    // 背景图文件
    private var effectBgImageFile: AGResource? {
        AGResourceManager.shared.getResource(uri: "AREffect/bgImage")
    }
    
    private var effectBgImgPath:String? {
        guard let effectBgImageFile = effectBgImageFile else { return nil }
        return AGResourceManager.shared.getFolderPath(resource: effectBgImageFile)
    }
    
    // 基础资源是否已经下载
    var baseResourceIsLoaded: Bool {
        guard let file = baseResourceFile else {
            return false
        }
        return AGResourceManager.shared.getStatus(resource: file) == .downloaded
    }
    
    // 虚拟背景图是否已下载
    var effectImageIsLoaded: Bool {
        guard let file = effectBgImageFile else {
            return false
        }
        return AGResourceManager.shared.getStatus(resource: file) == .downloaded
    }
    
    func downloadBaseEffectResources(completion:((_ error: String?, _ effectImageIsLoaded: Bool)->())?){
        if baseResourceIsLoaded {
            loadScene()
            completion?(nil, effectImageIsLoaded)
            return
        }
        guard let file = baseResourceFile else {
            completion?("resource not found ", effectImageIsLoaded)
            ShowAgoraKitManager.downloadManifestList {
                self.downloadBaseEffectResources(completion: completion)
            }
            return
        }
        AGResourceManager.shared.downloadResource(resource: file) { progress in
            showLogger.info(" progress = \(progress)")
        } completionHandler: {[weak self] path, error in
            DispatchQueue.main.async { [weak self] in
                if let _ = path {
                    self?.loadScene()
                    completion?(nil, self?.effectImageIsLoaded ?? false)
                }else{
                    completion?(error?.localizedDescription ?? "", self?.effectImageIsLoaded ?? false)
                }
            }
        }
    }
    
    func downloadEffectBgImage(completion:((_ error: String?, _ baseResourceIsLoaded: Bool)->())?){
        if effectImageIsLoaded {
            completion?(nil, baseResourceIsLoaded)
            return
        }
        guard let file = effectBgImageFile else {
            completion?("resource not found ", baseResourceIsLoaded)
            ShowAgoraKitManager.downloadManifestList {
                self.downloadEffectBgImage(completion: completion)
            }
            return
        }
        AGResourceManager.shared.downloadResource(resource: file) { progress in
//            showLogger.info(" progress = \(progress)")
        } completionHandler: {[weak self] path, error in
            DispatchQueue.main.async { [weak self] in
                if let _ = path {
                    completion?(nil, self?.baseResourceIsLoaded ?? false)
                }else{
                    completion?(error?.localizedDescription ?? "", self?.baseResourceIsLoaded ?? false)
                }
            }
        }
    }
    
    func loadScene(){
        if self.metakit != nil {
            return
        }
        if baseResourceIsLoaded {
            guard let file = baseResourceFile else {return}
            let path = AGResourceManager.shared.getFolderPath(resource: file)
            loadScene(scenePath: path)
        }
    }
    
    static func downloadManifestList(completion:(()->())? = nil) {
        AGResourceManagerContext.shared.displayLogClosure = { msg in
            showLogger.info(msg, context: "AGResourceManager")
        }
        let url = "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/resource/manifest/manifestList"
        AGResourceManager.shared.downloadManifestList(url: url) { _ in
        } completionHandler: { fileList, err in
            completion?()
        }
    }
    
}
