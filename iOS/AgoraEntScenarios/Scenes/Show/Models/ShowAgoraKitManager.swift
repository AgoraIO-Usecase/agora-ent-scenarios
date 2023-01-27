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
    case preload = 0
    case loading
}

//TODO: fix retain cycle
class ShowAgoraExProxy: NSObject, AgoraRtcEngineDelegate {
    weak var delegate: AgoraRtcEngineDelegate?
    
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
    
    // 预设类型
    var presetType: ShowPresetType?
    
    let videoEncoderConfig = AgoraVideoEncoderConfiguration()
    
    private var exConnectionMap: [String: AgoraRtcConnection] = [:]
    
    private lazy var rtcEngineConfig: AgoraRtcEngineConfig = {
       let config = AgoraRtcEngineConfig()
        config.appId = KeyCenter.AppId
        config.channelProfile = .liveBroadcasting
        config.areaCode = .global
        return config
    }()
    
    private lazy var captureConfig: AgoraCameraCapturerConfiguration = {
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
        let roleOptions = AgoraClientRoleOptions()
        roleOptions.audienceLatencyLevel = .ultraLowLatency
        kit.setClientRole(.audience, options: roleOptions)
        kit.enableAudio()
        kit.enableVideo()
        return kit
    }()
    
    weak var delegate: AgoraRtcEngineDelegate? {
        didSet {
            agoraKit.delegate = delegate
        }
    }
    
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
    
    /// 初始化并预览
    /// - Parameter canvasView: 画布
    func startPreview(canvasView: UIView) {
        agoraKit.setClientRole(.broadcaster)
        agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
        agoraKit.setVideoFrameDelegate(self)
        agoraKit.setCameraCapturerConfiguration(captureConfig)
        
        canvas.view = canvasView
        agoraKit.setupLocalVideo(canvas)
        agoraKit.enableVideo()
        agoraKit.startPreview()
    }
    
    /// 切换摄像头
    func switchCamera() {
        agoraKit.switchCamera()
    }
    
    /// 切换连麦角色
    func switchRole(role: AgoraClientRole,
                    options:AgoraRtcChannelMediaOptions,
                    uid: String?,
                    canvasView: UIView?) {
//        let options = AgoraRtcChannelMediaOptions()
        options.clientRoleType = role
//        options.publishMicrophoneTrack = role == .broadcaster
//        options.publishCameraTrack = role == .broadcaster
        agoraKit.updateChannel(with: options)
        agoraKit.setClientRole(role)
        guard canvasView != nil else { return }
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = UInt(uid ?? "0") ?? 0
        videoCanvas.renderMode = .hidden
        videoCanvas.view = canvasView
        if uid == VLUserCenter.user.id {
            agoraKit.setupLocalVideo(videoCanvas)
            agoraKit.startPreview()
        } else {
            agoraKit.setupRemoteVideo(videoCanvas)
        }
    }
    
    /// 设置分辨率
    /// 设置采集分辨率
    /// - Parameter size: 分辨率
    func setCaptureVideoDimensions(_ size: CGSize){
        agoraKit.disableVideo()
        captureConfig.dimensions = CGSize(width: size.width, height: size.height)
        showLogger.info("setCaptureVideoDimensions width = \(size.width), height = \(size.height)")
        agoraKit.setCameraCapturerConfiguration(captureConfig)
        agoraKit.enableVideo()
    }
    
    /// 设置编码分辨率
    /// - Parameter size: 分辨率
    func setVideoDimensions(_ size: CGSize){
        videoEncoderConfig.dimensions = CGSize(width: size.width, height: size.height)
        agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
    }
    
    func leaveChannel(){
        agoraKit.leaveChannel({stats in
            showLogger.info("leave channel: \(stats)")
        })
        agoraKit.stopPreview()
        agoraKit.disableAudio()
        agoraKit.disableVideo()
//        AgoraRtcEngineKit.destroy()
    }
    
    func leaveChannelEx(roomId: String) {
        guard let connection = exConnectionMap[roomId] else { return }
        agoraKit.leaveChannelEx(connection)
        exConnectionMap[roomId] = nil
    }
    
    func joinChannelEx(channelName: String,
                       ownerId: UInt,
                       options:AgoraRtcChannelMediaOptions,
                       role: AgoraClientRole) {
        if let rtcToken = AppContext.shared.rtcTokenMap?[channelName] {
            _joinChannelEx(channelName: channelName,
                           ownerId: ownerId,
                           token: rtcToken,
                           options: options,
                           role: role)
            return
        }
        
        
        let group = DispatchGroup()
        group.enter()
        NetworkManager.shared.generateToken(channelName: channelName,
                                            uid: VLUserCenter.user.id,
                                            tokenType: .token007,
                                            type: .rtc) {[weak self] token in
            defer {
                group.leave()
            }
            
            guard let token = token else {
                assert(false, "joinChannelEx fail: token is empty")
                return
            }
            self?._joinChannelEx(channelName: channelName,
                                 ownerId: ownerId,
                                 token: token,
                                 options: options,
                                 role: role)
        }
    }
    
    private func _joinChannelEx(channelName: String,
                                ownerId: UInt,
                                token: String,
                                options:AgoraRtcChannelMediaOptions,
                                role: AgoraClientRole) {
        initAudienceConfig()
        if exConnectionMap[channelName] == nil {
            let mediaOptions = AgoraRtcChannelMediaOptions()
            mediaOptions.autoSubscribeAudio = false
            mediaOptions.autoSubscribeVideo = false
            mediaOptions.publishCameraTrack = false
            mediaOptions.publishMicrophoneTrack = options.publishMicrophoneTrack//role == .broadcaster
            mediaOptions.clientRoleType = role
        
            let connection = AgoraRtcConnection()
            connection.channelId = channelName
            connection.localUid = UInt(VLUserCenter.user.id) ?? 0
            
            //TODO: retain cycle in joinChannelEx
            let proxy = ShowAgoraExProxy()
            proxy.delegate = self.delegate
            let date = Date()
            let ret =
            agoraKit.joinChannelEx(byToken: token,
                                   connection: connection,
                                   delegate: proxy,
                                   mediaOptions: mediaOptions) { channelName, uid, elapsed in
                let cost = Int(-date.timeIntervalSinceNow * 1000)
                showLogger.info("join room[\(channelName)] ex success \(uid) cost \(cost) ms", context: "AgoraKit")
            }
            exConnectionMap[channelName] = connection
            
            if ret == 0 {
                showLogger.info("join room ex: channelName: \(channelName) ownerId: \(ownerId)",
                                context: "AgoraKitManager")
            }else{
                showLogger.error("join room ex fail: channelName: \(channelName) ownerId: \(ownerId), \(ret)",
                                 context: "AgoraKit")
            }
        }
        
//        guard let connection = exConnectionMap[channelName] else {
//            assert(false, "_joinChannelEx fail: connection is empty")
//            return
//        }
//
//        let uid = UInt(ownerId ?? "0") ?? 0
//        let videoCanvas = AgoraRtcVideoCanvas()
//        videoCanvas.uid = uid
//        videoCanvas.view = view
//        videoCanvas.renderMode = .hidden
//        agoraKit.setupRemoteVideoEx(videoCanvas, connection: connection)
    }
    
    func joinChannel(channelName: String, uid: UInt, ownerId: String) {
        let role: AgoraClientRole = ownerId == VLUserCenter.user.id ? .broadcaster : .audience
        let roleOptions = AgoraClientRoleOptions()
        let isAudience = role == .audience ? true : false
        roleOptions.audienceLatencyLevel = isAudience ? .ultraLowLatency : .lowLatency
        agoraKit.setClientRole(role, options: roleOptions)
        if isAudience {
            initAudienceConfig()
        } else {
            initBroadcasterConfig()
        }
        agoraKit.enableVideo()
        
        let rtcToken: String? = AppContext.shared.rtcTokenMap?[channelName]
        let ret = agoraKit.joinChannel(byToken: rtcToken!,
                                        channelId: channelName,
                                        info: nil,
                                        uid: uid)
        moderationAudio(channelName: channelName, role: role)
        
//        canvas.view = canvasView
//        if role == .broadcaster {
//            canvas.uid = uid
//            agoraKit.setVideoFrameDelegate(self)
//            agoraKit.setDefaultAudioRouteToSpeakerphone(true)
//            agoraKit.enableAudio()
//            agoraKit.setupLocalVideo(canvas)
//            agoraKit.startPreview()
//        } else {
//            canvas.uid = UInt(ownerId) ?? 0
//            agoraKit.setupRemoteVideo(canvas)
//        }
        
        if ret == 0 {
            showLogger.info("join room success: \(channelName) \(uid) \(ownerId)")
        }else{
            showLogger.error("join room fail: \(channelName) \(uid) \(ownerId), \(ret)")
        }
    }
    
    func setupLocalVideo(uid: UInt, canvasView: UIView) {
        canvas.view = canvasView
        canvas.uid = uid
        agoraKit.setVideoFrameDelegate(self)
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        agoraKit.enableAudio()
        agoraKit.setupLocalVideo(canvas)
        agoraKit.startPreview()
    }
    
    func setupRemoteVideo(channelName: String, uid: UInt, canvasView: UIView) {
        guard let connection = exConnectionMap[channelName] else {
            assert(false, "_joinChannelEx fail: connection is empty")
            return
        }
        
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = canvasView
        videoCanvas.renderMode = .hidden
        agoraKit.setupRemoteVideoEx(videoCanvas, connection: connection)
    }
    
    
    func updateLoadingType(channelName: String, loadingType: ShowRTCLoadingType) {
        guard let connection = exConnectionMap[channelName] else {
//            assert(false, "updateLoadingType fail, mediaOptions not found")
            return
        }
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        if loadingType == .preload {
            mediaOptions.autoSubscribeAudio = false
            mediaOptions.autoSubscribeVideo = false
        } else {
            mediaOptions.autoSubscribeAudio = true
            mediaOptions.autoSubscribeVideo = true
        }

        showLogger.info("updateLoadingType \(channelName) \(loadingType.rawValue)")
        agoraKit.updateChannelEx(with: mediaOptions, connection: connection)
    }
}


extension ShowAgoraKitManager: AgoraVideoFrameDelegate {
    
    func onCapture(_ videoFrame: AgoraOutputVideoFrame) -> Bool {
        videoFrame.pixelBuffer = ByteBeautyManager.shareManager.processFrame(pixelBuffer: videoFrame.pixelBuffer)
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
