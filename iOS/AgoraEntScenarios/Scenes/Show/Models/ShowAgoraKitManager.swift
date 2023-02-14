//
//  ShowAgoraKitManager.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/22.
//

import Foundation
import AgoraRtcKit
import UIKit
import YYCategories

enum ShowRTCLoadingType: Int {
    case idle = 0
    case preload
    case loading
}

//TODO: fix retain cycle
class ShowAgoraProxy: NSObject {
    weak var delegate: NSObjectProtocol?
    
    override func responds(to aSelector: Selector!) -> Bool {
        return delegate?.responds(to: aSelector) ?? false
    }
    
    override func method(for aSelector: Selector!) -> IMP! {
        guard let obj = self.delegate as? NSObject else {
            return super.method(for: aSelector)
        }
        
        return obj.method(for: aSelector)
    }
    
    override func forwardingTarget(for aSelector: Selector!) -> Any? {
        if delegate?.responds(to: aSelector) ?? false {
            return delegate
        }
        
        return super.forwardingTarget(for: aSelector)
    }
}

class ShowAgoraExProxy: ShowAgoraProxy, AgoraRtcEngineDelegate {
    init(delegate: AgoraRtcEngineDelegate?) {
        super.init()
        self.delegate = delegate
    }
}

class ShowAgoraKitManager: NSObject {
    /*
    lazy var musicDataArray: [ShowMusicConfigData] = {
        return [musicBg, beautyVoice, mixVoice]
    }()
    
    // 背景音乐
    lazy var musicBg: ShowMusicConfigData = {
        return musicBgConfigData()
    }()
    
    // 美声
    lazy var beautyVoice: ShowMusicConfigData = {
        return beautyVoiceConfigData()
    }()
    
    // 混响
    lazy var mixVoice: ShowMusicConfigData = {
        return mixVoiceConfigData()
    }()
    */
    
    // 是否开启绿幕功能
    static var isOpenGreen: Bool = false
    static var isBlur: Bool = false
    
    // 预设类型
    var presetType: ShowPresetType?
    
    var srIsOn = false
    var srType: SRType = .x1_33
    
    let videoEncoderConfig = AgoraVideoEncoderConfiguration()
    
    //[ex channelId: connection]
    private var exConnectionMap: [String: AgoraRtcConnection] = [:]
    //[ex channelId: [room id: status]]
    private var exConnectionDeps: [String: [String: ShowRTCLoadingType]] = [:]
    
    private var delegateMap: [String: ShowAgoraExProxy] = [:]
    
    private lazy var rtcEngineConfig: AgoraRtcEngineConfig = {
       let config = AgoraRtcEngineConfig()
        config.appId = KeyCenter.AppId
        config.channelProfile = .liveBroadcasting
        config.areaCode = .global
        return config
    }()
    
//    let captureConfig = AgoraCameraCapturerConfiguration()
    lazy var captureConfig: AgoraCameraCapturerConfiguration = {
        let config = AgoraCameraCapturerConfiguration()
        config.cameraDirection = .front
        config.dimensions = CGSize(width: 1280, height: 720)
        config.frameRate = 15
        return config
    }()
    
    private lazy var canvas: AgoraRtcVideoCanvas = {
        let canvas = AgoraRtcVideoCanvas()
        canvas.renderMode = .hidden
        canvas.mirrorMode = .disabled
        return canvas
    }()
    
    fileprivate(set) lazy var agoraKit: AgoraRtcEngineKit = {
        let kit = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: nil)
        showLogger.info("load AgoraRtcEngineKit, sdk version: \(AgoraRtcEngineKit.getSdkVersion())", context: kShowLogBaseContext)
        return kit
    }()
    
    deinit {
        AgoraRtcEngineKit.destroy()
        showLogger.info("deinit-- ShowAgoraKitManager")
    }
    
    override init() {
        super.init()
        setupContentInspectConfig()
        showLogger.info("init-- ShowAgoraKitManager")
    }
    
    //MARK: private
    private func setupContentInspectConfig() {
        let config = AgoraContentInspectConfig()
        let dic: [String: String] = [
            "id": VLUserCenter.user.id,
            "sceneName": "show"
        ]
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: dic, options: .prettyPrinted) else {
            showLogger.error("setupContentInspectConfig fail")
            return
        }
        let jsonStr = String(data: jsonData, encoding: .utf8)
        config.extraInfo = jsonStr
        let module = AgoraContentInspectModule()
        module.interval = 30
        module.type = .moderation
        config.modules = [module]
        let ret = agoraKit.enableContentInspect(true, config: config)
        showLogger.info("setupContentInspectConfig: \(ret)")
    }
    
    /// 语音审核
    private func moderationAudio(channelName: String, role: AgoraClientRole) {
        guard role == .broadcaster else { return }
        let userInfo = ["id": VLUserCenter.user.id,
                        "sceneName": "show",
                        "userName": VLUserCenter.user.name]
        let parasm: [String: Any] = ["appId": KeyCenter.AppId,
                                     "channelName": channelName,
                                     "channelType": rtcEngineConfig.channelProfile.rawValue,
                                     "traceId": NSString.withUUID().md5(),
                                     "src": "iOS",
                                     "payload": JSONObject.toJsonString(dict: userInfo) ?? ""]
        NetworkManager.shared.postRequest(urlString: "https://toolbox.bj2.agoralab.co/v1/moderation/audio",
                                          params: parasm) { response in
            showLogger.info("response === \(response)")
        } failure: { errr in
            showLogger.error(errr)
        }
    }
    
    private func _joinChannelEx(currentChannelId: String,
                                targetChannelId: String,
                                ownerId: UInt,
                                token: String,
                                options:AgoraRtcChannelMediaOptions,
                                role: AgoraClientRole) {
//        initAudienceConfig()
        if exConnectionMap[targetChannelId] == nil {
            let subscribeStatus = role == .audience ? false : true
            let mediaOptions = AgoraRtcChannelMediaOptions()
            mediaOptions.autoSubscribeAudio = subscribeStatus
            mediaOptions.autoSubscribeVideo = subscribeStatus
//            mediaOptions.publishCameraTrack = false
//            mediaOptions.publishMicrophoneTrack = options.publishMicrophoneTrack//role == .broadcaster
            mediaOptions.clientRoleType = role
            // 极速直播
            if role == .audience {
                mediaOptions.audienceLatencyLevel = .lowLatency
            }else{
                agoraKit.setCameraCapturerConfiguration(captureConfig)
                updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)
            }
        
            let connection = AgoraRtcConnection()
            connection.channelId = targetChannelId
            connection.localUid = UInt(VLUserCenter.user.id) ?? 0
            
            //TODO: retain cycle in joinChannelEx
            let proxy = ShowAgoraExProxy(delegate: delegateMap[currentChannelId])
            let date = Date()
            let ret =
            agoraKit.joinChannelEx(byToken: token,
                                   connection: connection,
                                   delegate: proxy,
                                   mediaOptions: mediaOptions) { channelName, uid, elapsed in
                let cost = Int(-date.timeIntervalSinceNow * 1000)
                showLogger.info("join room[\(channelName)] ex success \(uid) cost \(cost) ms", context: kShowLogBaseContext)
            }
            agoraKit.updateChannelEx(with: mediaOptions, connection: connection)
            exConnectionMap[targetChannelId] = connection
            
            if ret == 0 {
                showLogger.info("join room ex: channelId: \(targetChannelId) ownerId: \(ownerId)",
                                context: "AgoraKitManager")
            }else{
                showLogger.error("join room ex fail: channelId: \(targetChannelId) ownerId: \(ownerId) token = \(token), \(ret)",
                                 context: kShowLogBaseContext)
            }
        }
    }
    
    func updateVideoEncoderConfigurationForConnenction(currentChannelId: String) {
        let connection = AgoraRtcConnection()
        connection.channelId = currentChannelId
        connection.localUid = UInt(VLUserCenter.user.id) ?? 0
        let encoderRet = agoraKit.setVideoEncoderConfigurationEx(videoEncoderConfig, connection: connection)
        showLogger.info("setVideoEncoderConfigurationEx  dimensions = \(videoEncoderConfig.dimensions), bitrate = \(videoEncoderConfig.bitrate), fps = \(videoEncoderConfig.frameRate),  encoderRet = \(encoderRet)", context: kShowLogBaseContext)
    }
    
    //MARK: public method
    func setRtcDelegate(delegate: AgoraRtcEngineDelegate?, roomId: String) {
        guard let delegate = delegate else {
            delegateMap[roomId] = nil
            return
        }
        let proxy = ShowAgoraExProxy(delegate:delegate)
        
        delegateMap[roomId] = proxy
    }
    
    func renewToken(origToken: String) {
        AppContext.shared.rtcTokenMap?.forEach({ (key: String, value: String) in
            if origToken == value {
                self.renewToken(channelId: key)
            }
        })
    }
    
    func renewToken(channelId: String) {
        showLogger.info("renewToken with channelId: \(channelId)",
                        context: kShowLogBaseContext)
        NetworkManager.shared.generateToken(channelName: channelId,
                                            uid: UserInfo.userId,
                                            tokenType: .token007,
                                            type: .rtc) { token in
            guard let token = token else {
                showLogger.error("renewToken fail: token is empty")
                return
            }
            let option = AgoraRtcChannelMediaOptions()
            option.token = token
            AppContext.shared.rtcTokenMap?[channelId] = token
            self.updateChannelEx(channelId: channelId, options: option)
        }
    }
    
    //MARK: public sdk method
    /// 初始化并预览
    /// - Parameter canvasView: 画布
    func startPreview(canvasView: UIView) {
        agoraKit.setClientRole(.broadcaster)
        let encodeRet = agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        showLogger.info("----setVideoEncoderConfiguration width = \(videoEncoderConfig.dimensions.width), height = \(videoEncoderConfig.dimensions.height), ret = \(encodeRet)")
        agoraKit.setVideoFrameDelegate(self)
        let ret = agoraKit.setCameraCapturerConfiguration(captureConfig)
        showLogger.info("----setCaptureVideoDimensions width = \(captureConfig.dimensions.width), height = \(captureConfig.dimensions.height), ret = \(ret)")
        canvas.view = canvasView
        agoraKit.setupLocalVideo(canvas)
        agoraKit.enableVideo()
        agoraKit.startPreview()
    }
    
    /// 切换摄像头
    func switchCamera() {
        agoraKit.switchCamera()
    }
    
    /// 开启虚化背景
    func enableVirtualBackground(isOn: Bool, greenCapacity: Float = 0) {
        let source = AgoraVirtualBackgroundSource()
        source.backgroundSourceType = .blur
        source.blurDegree = .high
        var seg: AgoraSegmentationProperty?
        if ShowAgoraKitManager.isOpenGreen {
            seg = AgoraSegmentationProperty()
            seg?.modelType = .agoraGreen
            seg?.greenCapacity = greenCapacity
        }
        let ret = agoraKit.enableVirtualBackground(isOn, backData: source, segData: seg)
        showLogger.info("isOn = \(isOn), enableVirtualBackground ret = \(ret)")
    }
    
    /// 设置虚拟背景
    func seVirtualtBackgoundImage(imagePath: String?, isOn: Bool, greenCapacity: Float = 0) {
        guard let bundlePath = Bundle.main.path(forResource: "showResource", ofType: "bundle"),
              let bundle = Bundle(path: bundlePath) else { return }
        let imgPath = bundle.path(forResource: imagePath, ofType: "jpg")
        let source = AgoraVirtualBackgroundSource()
        source.backgroundSourceType = .img
        source.source = imgPath
        var seg: AgoraSegmentationProperty?
        if ShowAgoraKitManager.isOpenGreen {
            seg = AgoraSegmentationProperty()
            seg?.modelType = .agoraGreen
            seg?.greenCapacity = greenCapacity
        }
        agoraKit.enableVirtualBackground(isOn, backData: source, segData: seg)
    }
    
    func updateChannelEx(channelId: String, options: AgoraRtcChannelMediaOptions) {
        guard let connection = exConnectionMap[channelId] else {
            showLogger.error("updateChannelEx fail: connection is empty")
            return
        }
        
        agoraKit.updateChannelEx(with: options, connection: connection)
    }
    
    /// 切换连麦角色
    func switchRole(role: AgoraClientRole,
                    channelId: String,
                    options:AgoraRtcChannelMediaOptions,
                    uid: String?,
                    canvasView: UIView?) {
        guard let uid = UInt(uid ?? ""), let canvasView = canvasView else {
            showLogger.error("switchRole fatel")
            return
        }
        options.clientRoleType = role
        updateChannelEx(channelId:channelId, options: options)
        if "\(uid)" == VLUserCenter.user.id {
            setupLocalVideo(uid: uid, canvasView: canvasView)
        } else {
            setupRemoteVideo(channelId: channelId, uid: uid, canvasView: canvasView)
        }
    }
    
    /// 设置分辨率
    /// 设置采集分辨率
    /// - Parameter size: 分辨率
    func setCaptureVideoDimensions(_ size: CGSize){
        agoraKit.disableVideo()
        captureConfig.dimensions = CGSize(width: size.width, height: size.height)
        let ret = agoraKit.setCameraCapturerConfiguration(captureConfig)
        agoraKit.enableVideo()
        showLogger.info("setCaptureVideoDimensions width = \(size.width), height = \(size.height), ret = \(ret)")
    }
    
    /// 设置编码分辨率
    /// - Parameter size: 分辨率
    func setVideoDimensions(_ size: CGSize){
        videoEncoderConfig.dimensions = CGSize(width: size.width, height: size.height)
        agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
    }
    
    /// 设置265
    /// - Parameter isOn: 开关
    func setH265On(_ isOn: Bool) {
        agoraKit.setParameters("{\"engine.video.enable_hw_encoder\":\(isOn)}")
        agoraKit.setParameters("{\"engine.video.codec_type\":\"\(isOn ? 3 : 2)\"}")
    }
    
    func cleanCapture() {
        ByteBeautyManager.shareManager.destroy()
        agoraKit.stopPreview()
    }
    
    func leaveChannelEx(roomId: String, channelId: String) {
        guard let connection = exConnectionMap[channelId] else { return }
        let depMap: [String: ShowRTCLoadingType]? = exConnectionDeps[channelId]
//        depMap?[roomId] = nil
        guard depMap?.count ?? 0 == 0 else {
            showLogger.info("leaveChannelEx break, depcount: \(depMap?.count ?? 0), roomId: \(roomId), channelId: \(channelId)", context: kShowLogBaseContext)
            return
        }
        showLogger.info("leaveChannelEx roomId: \(roomId), channelId: \(channelId)", context: kShowLogBaseContext)
        agoraKit.leaveChannelEx(connection)
        exConnectionMap[channelId] = nil
    }
    
    func joinChannelEx(currentChannelId: String,
                       targetChannelId: String,
                       ownerId: UInt,
                       options:AgoraRtcChannelMediaOptions,
                       role: AgoraClientRole,
                       completion: (()->())?) {
        if let rtcToken = AppContext.shared.rtcTokenMap?[targetChannelId] {
            _joinChannelEx(currentChannelId: currentChannelId,
                           targetChannelId: targetChannelId,
                           ownerId: ownerId,
                           token: rtcToken,
                           options: options,
                           role: role)
            completion?()
            return
        }
        
        NetworkManager.shared.generateToken(channelName: targetChannelId,
                                            uid: VLUserCenter.user.id,
                                            tokenType: .token007,
                                            type: .rtc) {[weak self] token in
            defer {
                completion?()
            }
            
            guard let token = token else {
                showLogger.error("joinChannelEx fail: token is empty")
                return
            }
            AppContext.shared.rtcTokenMap?[targetChannelId] = token
            self?._joinChannelEx(currentChannelId: currentChannelId,
                                 targetChannelId: targetChannelId,
                                 ownerId: ownerId,
                                 token: token,
                                 options: options,
                                 role: role)
        }
    }
    
    func setupLocalVideo(uid: UInt, canvasView: UIView) {
        canvas.view = canvasView
        canvas.uid = uid
        agoraKit.setVideoFrameDelegate(self)
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        agoraKit.enableAudio()
        agoraKit.enableVideo()
        agoraKit.setupLocalVideo(canvas)
        agoraKit.startPreview()
        showLogger.info("setupLocalVideo target uid:\(uid), user uid\(UserInfo.userId)", context: kShowLogBaseContext)
    }
    
    func setupRemoteVideo(channelId: String, uid: UInt, canvasView: UIView) {
        guard let connection = exConnectionMap[channelId] else {
            showLogger.error("_joinChannelEx fail: connection is empty")
            return
        }
        
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = canvasView
        videoCanvas.renderMode = .hidden
        let ret = agoraKit.setupRemoteVideoEx(videoCanvas, connection: connection)
        
        showLogger.info("setupRemoteVideoEx ret = \(ret), uid:\(uid) localuid: \(UserInfo.userId) channelId: \(channelId)", context: kShowLogBaseContext)
    }
    
    func updateLoadingType(roomId: String, channelId: String, loadingType: ShowRTCLoadingType) {
        guard let _ = exConnectionMap[channelId] else {
            showLogger.error("updateLoadingType fail, mediaOptions not found")
            return
        }
        
        //TODO: new func?
        var map: [String: ShowRTCLoadingType]? = exConnectionDeps[channelId]
        if map == nil {
            map = [:]
        }
        if loadingType == .idle {
            map?[roomId] = nil
        } else {
            map?[roomId] = loadingType
        }
        
        guard let map = map else {
            showLogger.error("updateLoadingType fatal, map init fail")
            return
        }
        exConnectionDeps[channelId] = map
        
        var realLoadingType = loadingType
        //calc real type
        map.forEach { (key: String, value: ShowRTCLoadingType) in
            if realLoadingType.rawValue < value.rawValue {
                realLoadingType = value
            }
        }
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        if realLoadingType == .loading {
            mediaOptions.autoSubscribeAudio = true
            mediaOptions.autoSubscribeVideo = true
        } else {
            mediaOptions.autoSubscribeAudio = false
            mediaOptions.autoSubscribeVideo = false
        }

        showLogger.info("room[\(roomId)] updateLoadingType \(channelId) want:\(loadingType.rawValue) real: \(realLoadingType.rawValue)", context: kShowLogBaseContext)
        updateChannelEx(channelId:channelId, options: mediaOptions)
    }
}


extension ShowAgoraKitManager: AgoraVideoFrameDelegate {
    
    func onCapture(_ videoFrame: AgoraOutputVideoFrame) -> Bool {
        videoFrame.pixelBuffer = BeautyManager.shareManager.processFrame(pixelBuffer: videoFrame.pixelBuffer)
        return true
    }
    
    func onRenderVideoFrame(_ videoFrame: AgoraOutputVideoFrame, uid: UInt, channelId: String) -> Bool {
        true
    }
    
    func getVideoFormatPreference() -> AgoraVideoFormat {
        .cvPixelBGRA
    }
    
    func getVideoFrameProcessMode() -> AgoraVideoFrameProcessMode {
        .readWrite
    }
    
    func getMirrorApplied() -> Bool {
        true
    }
    
    func getRotationApplied() -> Bool {
        false
    }
    
}


//MARK: private param
extension ShowAgoraKitManager {
//    func initClinetRole() {
//        let clientRoleOptions = AgoraClientRoleOptions()
//        clientRoleOptions.audienceLatencyLevel = AgoraAudienceLatencyLevelType.lowLatency
//        agoraKit.setClientRole(AgoraClientRole.audience, options: clientRoleOptions)
//    }
//
//    func initDefaultEncoderConfig(rtcengine : AgoraRtcEngineKit?) {
//        agoraKit.setVideoEncoderConfiguration(AgoraVideoEncoderConfiguration(size: CGSize(width: 640, height: 360),
//                                                                              frameRate: .fps30,
//                                                                              bitrate: AgoraVideoBitrateStandard,
//                                                                              orientationMode: .fixedPortrait,
//                                                                              mirrorMode: .auto))
//    }
//
//    func initEncoderConfig() -> Void {
//        //todo
//    }
//
    func initBroadcasterConfig() {
        agoraKit.setParameters("{\"rtc.enable_crypto_access\":false}")
        agoraKit.setParameters("{\"rtc.use_global_location_priority_domain\":true}")
        agoraKit.setParameters("{\"che.video.has_intra_request\":false}")
        agoraKit.setParameters("{\"che.hardware_encoding\":1}")
        agoraKit.setParameters("{\"engine.video.enable_hw_encoder\":true}")
        agoraKit.setParameters("{\"che.video.keyFrameInterval\":2}")
        agoraKit.setParameters("{\"che.video.hw265_enc_enable\":1}")
        agoraKit.setParameters("{\"che.video.enable_first_frame_sw_decode\":true}")
        agoraKit.setParameters("{\"rtc.asyncCreateMediaEngine\":true}")
    }
    
    func initAudienceConfig() {
        agoraKit.setParameters("{\"rtc.enable_crypto_access\":false}")
        agoraKit.setParameters("{\"rtc.use_global_location_priority_domain\":true}")
        agoraKit.setParameters("{\"che.hardware_decoding\":0}")
        agoraKit.setParameters("{\"rtc.enable_nasa2\": false}")
        agoraKit.setParameters("{\"rtc.asyncCreateMediaEngine\":true}")
        agoraKit.setParameters("{\"che.video.enable_first_frame_sw_decode\":true}")
    }
    
    func initH265Config() {
        agoraKit.setParameters("{\"che.video.videoCodecIndex\":2}") // 265
    }
    
    func initH264Config() {
        agoraKit.setParameters("{\"che.video.videoCodecIndex\":1}") //264
        agoraKit.setParameters("{\"che.video.minQP\":10}")
        agoraKit.setParameters("{\"che.video.maxQP\":35}")
    }
    
//    func vqcEnable(enable: Bool) {
//        if (enable) {
//            agoraKit.setParameters("{\"rtc.video.degradation_preference\":3}") // on vqc balanced
//        }else {
//            agoraKit.setParameters("{\"rtc.video.degradation_preference\":100}") // off vqc
//        }
//    }
//    
//    func vprEnable(enable: Bool) {
//        if (enable) {
//            agoraKit.setParameters("{\"che.video.vpr.enable\":true}") // on
//        }else {
//            agoraKit.setParameters("{\"che.video.vpr.enable\":false}") // off
//        }
//    }
}
