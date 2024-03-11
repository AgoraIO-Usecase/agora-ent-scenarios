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
        initializeMeta()
        return
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
    
    //2. 注册插件
    func initializeMeta() {
        guard let agoraKit = self.engine else {return}
        // 分割开启参数
        agoraKit.setParameters("{\"rtc.video.seg_before_exts\":true}")
        
        // 注册插件
        agoraKit.registerExtension(withVendor: "agora_video_filters_face_capture", extension: "face_capture", sourceType: AgoraMediaSourceType.primaryCamera)
        agoraKit.registerExtension(withVendor: "agora_video_filters_metakit", extension: "metakit", sourceType: AgoraMediaSourceType.primaryCamera)
        
        // 面捕插件使能与鉴权(需要获取license)
        agoraKit.enableExtension(withVendor: "agora_video_filters_face_capture", extension: "face_capture", enabled: true)
        agoraKit.setExtensionPropertyWithVendor("agora_video_filters_face_capture",
                                                         extension: "face_capture",
                                                         key: "authentication_information",
                                                         value: "{\"company_id\":\"agoraDemo\",\"license\":\"HIYWHc7P5x+IEm8H11NlEKFdAWOYc+rFIyhQ/QEDomcQhBoQrlHpMWqL9+HGAOBjBWZtcPHjol4NsRDlUo6UQ85ib/XSH+1MFlpZT/r5nCTADTkOc1PqfMKic4gNJr4sb3PD3v4EaZ89tZoYMw0LM6rdjt1ySH+8yaWKS+hKKHY=\"}",
                                                         sourceType: AgoraMediaSourceType.primaryCamera)
        // metakit插件使能与初始化
        let result = agoraKit.enableExtension(withVendor: "agora_video_filters_metakit", extension: "metakit", enabled: true)
        print("agora_video_filters_metakit - \(result)")

        agoraKit.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit", key:"initialize", value:"{}")
        
        
        // 注册分割插件
        let bg_src = AgoraVirtualBackgroundSource()
        bg_src.backgroundSourceType = .none
        let seg_prop = AgoraSegmentationProperty()
        seg_prop.modelType = .agoraAi
        let resultVB = agoraKit.enableVirtualBackground(true, backData: bg_src, segData: seg_prop)

    }
    
    //3. 等待插件注册完成后加载资源
    func setupMetaKitEngine() {
        
        guard let agoraKit = self.engine else {return}
        //加载资源
       var scenePath = "\(Bundle.main.path(forResource: "scenePath", ofType: "bundle")!)/scenePath"
       let info_dict = ["sceneInfo" : ["scenePath" : scenePath]] as [String : Any]
       let info_data = try? JSONSerialization.data(withJSONObject: info_dict, options: [])
       let info_str = String(data: info_data!, encoding: String.Encoding.utf8)
       agoraKit.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",
                                                    key:"loadScene", value:info_str ?? "")
        
        let metakit: MetaKitEngine = MetaKitEngine.sharedInstance()
        //let frame = CGRect(x: 0, y: 0, width: SCREEN_WIDTH, height: SCREEN_HEIGHT);
        let frame = CGRect(x: 0, y: 0, width: 225, height: 400);
        guard let metaView = metakit.createSceneView(UIScreen.main.bounds) else {
            print("createSceneView fail")
            return
        }
        
        // view为原生ios UIView，如果需要展示在UI上可自行add到父view上。
        // 如果仅希望通过agora的rtc编码传输出去，并不需要本地展示，则在下面步骤在view增加完内容后，用以下代码：
        self.sceneView = metaView
        //self.localVideoView.addSubview(metaView)
        let address = unsafeBitCast(metaView, to: Int64.self)
        let value = 1//enable ? 1 : 0
        agoraKit.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",
                                                     key:"enableSceneVideo",
                                                     value:"{\"view\":\"\(address)\",\"enable\":\(value)}")
        
        // 指定渲染器输出分辨率
        let resoultionW: Int = 720
        let resoultionH: Int = 1280
        // 指定背景特效能力
        let extra_dict = ["sceneIndex": 0, "backgroundEffect": true] as [String : Any]
        let extra_data = try? JSONSerialization.data(withJSONObject: extra_dict, options: [])
        let extra_str = String(data: extra_data!, encoding: String.Encoding.utf8) ?? ""
        let dict = ["view": String(address), "config": ["width": resoultionW, "height": resoultionH, "extraInfo": extra_str] as [String : Any]] as [String : Any]
        let data = try? JSONSerialization.data(withJSONObject: dict, options: [])
        let data_str = String(data: data!, encoding: String.Encoding.utf8)
        // 把view的能力指定给渲染器
        agoraKit.setExtensionPropertyWithVendor("agora_video_filters_metakit", extension: "metakit",
                                                     key:"addSceneView",
                                                     value: data_str!)
        self.metakit = metakit
    }
    
    //设置特效
    func setupConfigEffect3D(type:Int) {
        if self.metakit == nil {
            self.setupMetaKitEngine()
        }
    
        let light_dict = [
            "id": 2001, // 指定特效素材 ID 为 2001，即紫色火焰
            "enable": true // 指定 enable 为 true，即启用人像边缘火焰效果
            // highlight-end
        ] as [String: Any]

        let light_data = try? JSONSerialization.data(withJSONObject: light_dict, options: [])
        let light_info = String(data: light_data!, encoding: String.Encoding.utf8)

        // highlight-start
        // 根据 JSON 配置添加人像边缘火焰效果
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",
                                                extension: "metakit",
                                                key: "setEffectVideo",
                                                value: light_info!)
    }
    
    func loadScene(scenePath:String){
        // unity
        let info_dict = ["sceneInfo" : ["scenePath" : scenePath]] as [String : Any]
        let info_data = try? JSONSerialization.data(withJSONObject: info_dict,options: [])
        let info_str = String(data: info_data!, encoding: String.Encoding.utf8)
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit",key:"loadScene",value:info_str!)
        createSceneView()
    }
    
    func createSceneView(){
        metakit = MetaKitEngine.sharedInstance()
        let frame = CGRect(x: 0, y: 0, width: 225, height: 400);
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
        let dict = ["view": String(address), "config": ["width": resoultionW,"height": resoultionH, "extraInfo": extra_str] as [String : Any]] as [String : Any]
        let data = try? JSONSerialization.data(withJSONObject: dict, options:[])
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
        setupConfigEffect3D(type: 1)
        return
        guard let sceneView = self.sceneView else { return }
        
        let address = unsafeBitCast(sceneView, to: Int64.self)
        //
        let resoultionW: Int = 720
        let resoultionH: Int = 1280
        //
        let extra_dict = ["sceneIndex": 0, "backgroundEffect": true] as [String: Any]
        let extra_data = try? JSONSerialization.data(withJSONObject:extra_dict, options: [])
        let extra_str = String(data: extra_data!, encoding: String.Encoding.utf8) ?? ""
        let dict = ["view": String(address), "config": ["width": resoultionW,"height": resoultionH, "extraInfo": extra_str] as [String : Any]] as [String : Any]
        guard let data = try? JSONSerialization.data(withJSONObject: dict, options:[]) , let data_str = String(data: data, encoding: String.Encoding.utf8) else {
            return
        }
        // view
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit",key:"addSceneView",value: data_str)
        // 1001“3D”
//        let light_dict = ["id": 1001, 
//                          "param": ["intensity": 2.0, "scale": 0.3]as [String : Any],
//                          "enable": true] as [String : Any]
//        guard let light_data = try? JSONSerialization.data(withJSONObject:light_dict, options: []),let light_info = String(data: light_data, encoding: String.Encoding.utf8) else {
//            return
//        }
//        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",extension: "metakit",key:"setEffectVideo",value:light_info)
        
        let light_dict = [
            "id": 2001, // 指定特效素材 ID 为 2001，即紫色火焰
            "enable": true // 指定 enable 为 true，即启用人像边缘火焰效果
            // highlight-end
        ] as [String: Any]

        let light_data = try? JSONSerialization.data(withJSONObject: light_dict, options: [])
        let light_info = String(data: light_data!, encoding: String.Encoding.utf8)

        // highlight-start
        // 根据 JSON 配置添加人像边缘火焰效果
        engine?.setExtensionPropertyWithVendor("agora_video_filters_metakit",
                                                extension: "metakit",
                                                key: "setEffectVideo",
                                                value: light_info!)

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
