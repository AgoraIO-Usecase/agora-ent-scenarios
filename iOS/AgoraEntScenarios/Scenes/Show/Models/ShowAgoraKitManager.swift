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
import VideoLoaderAPI
import AgoraCommon
import AudioScenarioApi

class ShowAgoraKitManager: NSObject {
    private static var _sharedManager: ShowAgoraKitManager?
    static var shared: ShowAgoraKitManager {
        get {
            if let sharedManager = _sharedManager { return sharedManager }
            let sharedManager = ShowAgoraKitManager()
            _sharedManager = sharedManager
            return sharedManager
        }
        set {
            _sharedManager = nil
        }
    }
        
    // 是否开启绿幕功能
    static var isOpenGreen: Bool = false
    static var isBlur: Bool = false
    
    public let rtcParam = ShowRTCParams()
    public var deviceLevel: DeviceLevel = .medium
    public var deviceScore: Int = 100
    public var netCondition: NetCondition = .good
    public var performanceMode: PerformanceMode = .smooth
    
    private var broadcasterConnection: AgoraRtcConnection?
    
    private var channelIdUidCanvasMap: [String: UInt] = [:]
    
    private var audioApi: AudioScenarioApi?
    
//    var exposureRangeX: Int?
//    var exposureRangeY: Int?
//    var matrixCoefficientsExt: Int?
//    var videoFullrangeExt: Int?
    
//    let encoderConfig = AgoraVideoEncoderConfiguration()
//
//    public lazy var captureConfig: AgoraCameraCapturerConfiguration = {
//        let config = AgoraCameraCapturerConfiguration()
//        config.followEncodeDimensionRatio = true
//        config.cameraDirection = .front
//        config.frameRate = 15
//        return config
//    }()
    
    public var engine: AgoraRtcEngineKit? {
        didSet {
            if oldValue != engine {
                audioApi = nil
            }
            if let engine = engine {
                audioApi = AudioScenarioApi(rtcEngine: engine)
            }
        }
    }
    
    private var player: AgoraRtcMediaPlayerProtocol?
    func mediaPlayer() -> AgoraRtcMediaPlayerProtocol? {
        if let p = player {
            return p
        } else {
            player = engine?.createMediaPlayer(with: self)
            player?.setLoopCount(-1)
            return player
        }
    }
    
    func prepareEngine() {
        let engine = AgoraRtcEngineKit.sharedEngine(with: engineConfig(), delegate: nil)
        self.engine = engine
        
        let loader = VideoLoaderApiImpl.shared
        loader.addListener(listener: self)
        let config = VideoLoaderConfig()
        config.rtcEngine = engine
        loader.setup(config: config)
        
        ShowLogger.info("load AgoraRtcEngineKit, sdk version: \(AgoraRtcEngineKit.getSdkVersion())", context: kShowLogBaseContext)
    }
    
    func destoryEngine() {
        AgoraRtcEngineKit.destroy()
        ShowAgoraKitManager._sharedManager = nil
        ShowLogger.info("deinit-- ShowAgoraKitManager")
    }
    // 退出已加入的频道和子频道
    func leaveAllRoom() {
        VideoLoaderApiImpl.shared.cleanCache()
        if let p = player {
            engine?.destroyMediaPlayer(p)
            player = nil
        }
    }
    
    //MARK: private
    private func engineConfig() -> AgoraRtcEngineConfig {
        let config = AgoraRtcEngineConfig()
         config.appId = KeyCenter.AppId
         config.channelProfile = .liveBroadcasting
         config.areaCode = .global
         return config
    }
    
    private func setupContentInspectConfig(_ enable: Bool, connection: AgoraRtcConnection) {
        let config = AgoraContentInspectConfig()
        let dic: [String: String] = [
            "id": VLUserCenter.user.id,
            "sceneName": "show",
            "userNo": VLUserCenter.user.userNo
        ]
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: dic, options: .prettyPrinted) else {
            ShowLogger.error("setupContentInspectConfig fail")
            return
        }
        let jsonStr = String(data: jsonData, encoding: .utf8)
        config.extraInfo = jsonStr
        let module = AgoraContentInspectModule()
        module.interval = 60
        module.type = .imageModeration
        config.modules = [module]
        let ret = engine?.enableContentInspectEx(enable, config: config, connection: connection)
        ShowLogger.info("setupContentInspectConfig: \(ret ?? -1)")
    }
    
    /// 语音审核
    private func moderationAudio(channelName: String, role: AgoraClientRole) {
        guard role == .broadcaster else { return }
        let userInfo = ["id": VLUserCenter.user.id,
                        "sceneName": "show",
                        "userNo": VLUserCenter.user.userNo,
                        "userName": VLUserCenter.user.name]
        let parasm: [String: Any] = ["appId": KeyCenter.AppId,
                                     "channelName": channelName,
                                     "channelType": engineConfig().channelProfile.rawValue,
                                     "traceId": NSString.withUUID().md5(),
                                     "src": "iOS",
                                     "payload": JSONObject.toJsonString(dict: userInfo) ?? ""]
        let baseURL = AppContext.shared.baseServerUrl
        NetworkManager.shared.postRequest(urlString: "\(baseURL)/toolbox/v1/moderation/audio",
                                          params: parasm) { response in
            ShowLogger.info("response === \(response)")
        } failure: { errr in
            ShowLogger.error(errr)
        }
    }
    
    private func _joinChannelEx(currentChannelId: String,
                                targetChannelId: String,
                                ownerId: UInt,
                                token: String,
                                options:AgoraRtcChannelMediaOptions,
                                role: AgoraClientRole) {
        let localUid = UInt(VLUserCenter.user.id)!
        if role == .audience {
            let anchorInfo = getAnchorInfo(channelId: targetChannelId, uid: ownerId)
            let newState: AnchorState = broadcasterConnection == nil ? .prejoined : .joinedWithVideo
            VideoLoaderApiImpl.shared.switchAnchorState(newState: newState, localUid: localUid, anchorInfo: anchorInfo, tagId: currentChannelId)
            return
        }
        
        guard let engine = engine else {
            assert(true, "rtc engine not initlized")
            return
        }

        if let _ = broadcasterConnection {
            return
        }
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.publishCameraTrack = true
        mediaOptions.publishMicrophoneTrack = true
        mediaOptions.autoSubscribeAudio = true
        mediaOptions.autoSubscribeVideo = true
        mediaOptions.clientRoleType = .broadcaster
        
        audioApi?.setAudioScenario(sceneType: .Show, audioScenarioType: .Show_Host)
        
        updateVideoEncoderConfigurationForConnenction(currentChannelId: currentChannelId)

        let connection = AgoraRtcConnection()
        connection.channelId = targetChannelId
        connection.localUid = localUid

//        let proxy = VideoLoaderApiImpl.shared.getRTCListener(anchorId: currentChannelId)
        let date = Date()
        ShowLogger.info("try to join room[\(connection.channelId)] ex uid: \(connection.localUid)", context: kShowLogBaseContext)
        let ret =
        engine.joinChannelEx(byToken: token,
                               connection: connection,
                               delegate: nil,
                               mediaOptions: mediaOptions) {[weak self] channelName, uid, elapsed in
            let cost = Int(-date.timeIntervalSinceNow * 1000)
            ShowLogger.info("join room[\(channelName)] ex success uid: \(uid) cost \(cost) ms", context: kShowLogBaseContext)
            self?.setupContentInspectConfig(true, connection: connection)
//            self?.moderationAudio(channelName: targetChannelId, role: role)
            self?.applySimulcastStream(connection: connection)
        }
//        engine.addDelegateEx(<#T##delegate: AgoraRtcEngineDelegate##AgoraRtcEngineDelegate#>, connection: connection)
        
        ShowLogger.info("_joinChannelEx[\(connection.channelId)] role: \(mediaOptions.clientRoleType.rawValue) \(mediaOptions.publishMicrophoneTrack) \(mediaOptions.publishCameraTrack)")
        _updateChannelEx(options: mediaOptions, connection: connection)
        broadcasterConnection = connection

        if ret == 0 {
            ShowLogger.info("join room ex: channelId: \(targetChannelId) ownerId: \(ownerId)",
                            context: "AgoraKitManager")
        }else{
            ShowLogger.error("join room ex fail: channelId: \(targetChannelId) ownerId: \(ownerId) token = \(token), \(ret)",
                             context: kShowLogBaseContext)
        }
    }
    
    private func _updateChannelEx(options: AgoraRtcChannelMediaOptions,
                                  connection: AgoraRtcConnection) {
        ShowLogger.info("_updateChannelEx[\(connection.channelId)][\(connection.localUid)] role: \(options.clientRoleType.rawValue) publishMicrophoneTrack:\(options.publishMicrophoneTrack) publishCameraTrack:\(options.publishCameraTrack)")
        engine?.updateChannelEx(with: options, connection: connection)
    }
    
    func updateVideoEncoderConfigurationForConnenction(currentChannelId: String) {
        guard let engine = engine else {
            assert(true, "rtc engine not initlized")
            return
        }
        let connection = AgoraRtcConnection()
        connection.channelId = currentChannelId
        connection.localUid = UInt(VLUserCenter.user.id) ?? 0
        let encoderConfig = getEncoderConfig()
        let encoderRet = engine.setVideoEncoderConfigurationEx(encoderConfig, connection: connection)
        ShowLogger.info("setVideoEncoderConfigurationEx  dimensions = \(encoderConfig.dimensions), bitrate = \(encoderConfig.bitrate), fps = \(encoderConfig.frameRate),  encoderRet = \(encoderRet)", context: kShowLogBaseContext)
    }
    
    //MARK: public method
    func addRtcDelegate(delegate: AgoraRtcEngineDelegate, roomId: String) {
        ShowLogger.info("addRtcDelegate[\(roomId)]")
        let localUid = Int(VLUserCenter.user.id)!
        let connection = AgoraRtcConnection(channelId: roomId, localUid: localUid)
        engine?.addDelegateEx(delegate, connection: connection)
//        VideoLoaderApiImpl.shared.addRTCListener(anchorId: roomId, listener: delegate)
    }
    
    func removeRtcDelegate(delegate: AgoraRtcEngineDelegate, roomId: String) {
        ShowLogger.info("removeRtcDelegate[\(roomId)]")
        let localUid = Int(VLUserCenter.user.id)!
        let connection = AgoraRtcConnection(channelId: roomId, localUid: localUid)
        engine?.removeDelegateEx(delegate, connection: connection)
//        VideoLoaderApiImpl.shared.removeRTCListener(anchorId: roomId, listener: delegate)
    }
    
    func renewToken(channelId: String) {
        ShowLogger.info("renewToken with channelId: \(channelId)",
                  context: kShowLogBaseContext)
        preGenerateToken {[weak self] token in
            guard let token = token else {
                ShowLogger.error("renewToken fail: token is empty")
                return
            }
            let option = AgoraRtcChannelMediaOptions()
            option.token = token
            AppContext.shared.rtcToken = token
            self?.updateChannelEx(channelId: channelId, options: option)
        }
    }
    
    //MARK: public sdk method
    /// 初始化并预览
    /// - Parameter canvasView: 画布
    func startPreview(canvasView: UIView) {
        guard let engine = engine else {
            assert(true, "rtc engine not initlized")
            return
        }
        engine.setClientRole(.broadcaster)
        let encoderConfig = getEncoderConfig()
        let captureConfig = getCaptureConfig()
        engine.setVideoEncoderConfiguration(encoderConfig)
        engine.setCameraCapturerConfiguration(captureConfig)
        BeautyManager.shareManager.beautyAPI.setupLocalVideo(canvasView, renderMode: .hidden)
        engine.enableVideo()
        engine.startPreview()
    }
    
    /// 切换摄像头
    func switchCamera(_ channelId: String? = nil) {
        BeautyManager.shareManager.beautyAPI.switchCamera()
    }
    
    /// 开启虚化背景
    func enableVirtualBackground(isOn: Bool, greenCapacity: Float = 0) {
        guard let engine = engine else {
            assert(true, "rtc engine not initlized")
            return
        }
        let source = AgoraVirtualBackgroundSource()
        source.backgroundSourceType = .blur
        source.blurDegree = .high
        var seg: AgoraSegmentationProperty?
        if ShowAgoraKitManager.isOpenGreen {
            seg = AgoraSegmentationProperty()
            seg?.modelType = .agoraGreen
            seg?.greenCapacity = greenCapacity
        }
        let ret = engine.enableVirtualBackground(isOn, backData: source, segData: seg)
        ShowLogger.info("isOn = \(isOn), enableVirtualBackground ret = \(ret)")
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
        guard let engine = engine else {
            assert(true, "rtc engine not initlized")
            return
        }
        engine.enableVirtualBackground(isOn, backData: source, segData: seg)
    }
    
    func updateChannelEx(channelId: String, options: AgoraRtcChannelMediaOptions) {
        guard let connection = (broadcasterConnection?.channelId == channelId ? broadcasterConnection : nil) ?? VideoLoaderApiImpl.shared.getConnectionMap()[channelId] else {
            ShowLogger.error("updateChannelEx fail: connection is empty")
            return
        }
        
        ShowLogger.info("updateChannelEx[\(channelId)] role: \(options.clientRoleType.rawValue) publishMicrophoneTrack:\(options.publishMicrophoneTrack) publishCameraTrack:\(options.publishCameraTrack)")
        _updateChannelEx(options: options, connection: connection)
    }
    
    
    /// 切换连麦角色
    /// - Parameters:
    ///   - role: 角色，连麦双方为broadcaster，观众为audience，连麦主播结束连麦也为audience，无需修改则为nil
    ///   - channelId: 频道号
    ///   - options: <#options description#>
    ///   - uid: 连麦窗口的uid
    ///   - canvasView: 画布，nil表示需要移除
    func switchRole(role: AgoraClientRole? = nil,
                    channelId: String,
                    options:AgoraRtcChannelMediaOptions,
                    uid: String?) {
        guard let uid = UInt(uid ?? "") else {
            ShowLogger.error("switchRole fatel")
            return
        }
        
        ShowLogger.info("switchRole[\(channelId)], role: \(role?.rawValue ?? -1) localUid:\(UserInfo.userId) uid: \(uid)", context: kShowLogBaseContext)
        if role == .broadcaster {
            audioApi?.setAudioScenario(sceneType: .Show, audioScenarioType: .Show_InteractiveAudience)
        } 
        
        if let role = role {
            let roleOptions = AgoraRtcChannelMediaOptions()
            roleOptions.clientRoleType = role
            roleOptions.audienceLatencyLevel = role == .audience ? .lowLatency : .ultraLowLatency
            roleOptions.publishCameraTrack = options.publishCameraTrack
            roleOptions.publishMicrophoneTrack = options.publishMicrophoneTrack
            updateChannelEx(channelId:channelId, options: roleOptions)
        }
    }
    
    func updateLiveView(role: AgoraClientRole? = nil,
                        channelId: String,
                        uid: String?,
                        canvasView: UIView?) {
        guard let uid = UInt(uid ?? "") else {
            ShowLogger.error("updateLiveView fatel")
            return
        }
        
        if role == .audience {
            //观众先清理本地画面，然后设置远端画面
            setupLocalVideo(canvasView: nil)
            cleanCapture()
        }
        
        
        if "\(uid)" == VLUserCenter.user.id {
            //自己是连麦主播，渲染本地画面
            setupLocalVideo(canvasView: canvasView)
        } else {
            //自己不是连麦主播，渲染连麦主播远端画面
            setupRemoteVideo(channelId: channelId, uid: uid, canvasView: canvasView)
        }
    }
    
    
    /// 预加载连麦
    /// - Parameters:
    ///   - isOn: <#isOn description#>
    ///   - channelId: <#channelId description#>
    func prePublishOnseatVideo(isOn: Bool, channelId: String) {
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.publishCameraTrack = isOn
        mediaOptions.publishMicrophoneTrack = false
        switchRole(role: isOn ? .broadcaster : .audience,
                   channelId: channelId,
                   options: mediaOptions,
                   uid: VLUserCenter.user.id)
    }
    
    func preSubscribePKVideo(isOn: Bool, channelId: String) {
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.publishCameraTrack = isOn
        mediaOptions.publishMicrophoneTrack = false
        mediaOptions.autoSubscribeAudio = false
        mediaOptions.autoSubscribeVideo = isOn
        mediaOptions.clientRoleType = isOn ? .broadcaster : .audience
        let uid = Int(VLUserCenter.user.id) ?? 0
        let connection = AgoraRtcConnection(channelId: channelId, localUid: uid)
        _updateChannelEx(options: mediaOptions, connection: connection)
    }
    
    /// 设置编码分辨率
    /// - Parameter size: 分辨率
    func setVideoDimensions(_ size: CGSize){
        guard let engine = engine else {
            assert(true, "rtc engine not initlized")
            return
        }
        let encoderConfig = getEncoderConfig()
        encoderConfig.dimensions = CGSize(width: size.width, height: size.height)
        engine.setVideoEncoderConfiguration(encoderConfig)
    }
    
    func cleanCapture() {
        guard let engine = engine else {
            assert(true, "rtc engine not initlized")
            return
        }
//        ByteBeautyManager.shareManager.destroy()
//        setupContentInspectConfig(false)
        engine.stopPreview()
        engine.setVideoFrameDelegate(nil)
        engine.enableVirtualBackground(false, backData: nil, segData: nil)
        engine.setAudioEffectPreset(.off)
        engine.setVoiceConversionPreset(.off)
        ShowAgoraKitManager.isOpenGreen = false
    }
    
    
    /// 离开频道
    /// - Parameters:
    ///   - roomId: 业务房间id（pk填写所在的业务房间，其他填写和channelId一致）
    ///   - channelId: 需要离开的频道id
    func leaveChannelEx(roomId: String, channelId: String) {
        if let connection = broadcasterConnection, connection.channelId == channelId {
            engine?.leaveChannelEx(connection)
            broadcasterConnection = nil
            return
        }
        let anchorInfo = getAnchorInfo(channelId: channelId)
        VideoLoaderApiImpl.shared.switchAnchorState(newState: .idle, localUid: anchorInfo.uid, anchorInfo: anchorInfo, tagId: roomId)
    }
    
    func joinChannelEx(currentChannelId: String,
                       targetChannelId: String,
                       ownerId: UInt,
                       options:AgoraRtcChannelMediaOptions,
                       role: AgoraClientRole,
                       completion: (()->())?) {
        _joinChannelEx(currentChannelId: currentChannelId,
                       targetChannelId: targetChannelId,
                       ownerId: ownerId,
                       token: AppContext.shared.rtcToken ?? "",
                       options: options,
                       role: role)
        completion?()
    }
    
    func setupLocalVideo(mirrorMode: AgoraVideoMirrorMode = .disabled,
                         canvasView: UIView?) {
        guard let engine = engine else {
            assert(true, "rtc engine not initlized")
            return
        }
        let canvas = AgoraRtcVideoCanvas()
        canvas.view = canvasView
        canvas.mirrorMode = mirrorMode
        engine.setupLocalVideo(canvas)
        engine.startPreview()
        engine.setDefaultAudioRouteToSpeakerphone(true)
        engine.enableLocalAudio(true)
        engine.enableLocalVideo(true)
        ShowLogger.info("setupLocalVideo, user uid:\(UserInfo.userId) view: \(canvasView?.description ?? "")", context: kShowLogBaseContext)
    }
    
    //连麦用，pk走的VideoloaderAPI
    private func setupRemoteVideo(channelId: String, uid: UInt, canvasView: UIView?) {
        let connection = AgoraRtcConnection(channelId: channelId, localUid: Int(VLUserCenter.user.id) ?? 0)
        //先清理老的view
        if let uid = channelIdUidCanvasMap[channelId] {
            let videoCanvas = AgoraRtcVideoCanvas()
            videoCanvas.uid = uid
            videoCanvas.view = nil
            let _ = engine?.setupRemoteVideoEx(videoCanvas, connection: connection)
            channelIdUidCanvasMap.removeValue(forKey: channelId)
        }
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = canvasView
        let ret = engine?.setupRemoteVideoEx(videoCanvas, connection: connection)
        if let _ = canvasView {
            channelIdUidCanvasMap[channelId] = uid
        }
        ShowLogger.info("setupRemoteVideoEx[\(channelId)] ret = \(ret ?? -1), uid:\(uid) view: \(canvasView?.description ?? "")", context: kShowLogBaseContext)
    }
    
    func updateLoadingType(roomId: String, channelId: String, playState: AnchorState) {
        if broadcasterConnection?.channelId == channelId {return}
        let anchorInfo = getAnchorInfo(channelId: channelId)
        VideoLoaderApiImpl.shared.switchAnchorState(newState: playState, localUid: anchorInfo.uid, anchorInfo: anchorInfo, tagId: roomId)
    }
    
    func cleanChannel(without roomIds: [String]) {
        let videoLoader = VideoLoaderApiImpl.shared
        for (key, _) in videoLoader.getConnectionMap() {
            if roomIds.contains(key) {continue}
            let anchorInfo = AnchorInfo()
            anchorInfo.channelName = key
            anchorInfo.uid = UInt(VLUserCenter.user.id)!
            videoLoader.switchAnchorState(newState: .idle, localUid: anchorInfo.uid, anchorInfo: anchorInfo, tagId: key)
        }
    }
}

//MARK: private param
extension ShowAgoraKitManager {
    
    func initBroadcasterConfig() {
        engine?.setParameters("{\"rtc.enable_crypto_access\":false}")
        engine?.setParameters("{\"rtc.use_global_location_priority_domain\":true}")
        engine?.setParameters("{\"che.video.has_intra_request\":false}")
        engine?.setParameters("{\"che.hardware_encoding\":1}")
        engine?.setParameters("{\"engine.video.enable_hw_encoder\":true}")
        engine?.setParameters("{\"che.video.keyFrameInterval\":2}")
        engine?.setParameters("{\"che.video.hw265_enc_enable\":1}")
        engine?.setParameters("{\"che.video.enable_first_frame_sw_decode\":true}")
        engine?.setParameters("{\"rtc.asyncCreateMediaEngine\":true}")
    }
    
    func initAudienceConfig() {
        engine?.setParameters("{\"rtc.enable_crypto_access\":false}")
        engine?.setParameters("{\"rtc.use_global_location_priority_domain\":true}")
        engine?.setParameters("{\"che.hardware_decoding\":0}")
        engine?.setParameters("{\"rtc.enable_nasa2\": false}")
        engine?.setParameters("{\"rtc.asyncCreateMediaEngine\":true}")
        engine?.setParameters("{\"che.video.enable_first_frame_sw_decode\":true}")
    }
    
    func initH265Config() {
        engine?.setParameters("{\"che.video.videoCodecIndex\":2}") // 265
    }
    
    func initH264Config() {
        engine?.setParameters("{\"che.video.videoCodecIndex\":1}") //264
        engine?.setParameters("{\"che.video.minQP\":10}")
        engine?.setParameters("{\"che.video.maxQP\":35}")
    }
    
}

extension ShowAgoraKitManager {
    func getAnchorInfo(channelId: String, uid: UInt? = nil)->AnchorInfo {
        let anchorInfo = AnchorInfo()
        anchorInfo.channelName = channelId
        anchorInfo.uid = uid ?? (UInt(VLUserCenter.user.id) ?? 0)
        anchorInfo.token = AppContext.shared.rtcToken ?? ""
        
        return anchorInfo
    }
    
    func setOffMediaOptionsVideo(roomid: String) {
        guard let connection = VideoLoaderApiImpl.shared.getConnectionMap()[roomid] else {
            ShowLogger.info("setOffMediaOptionsVideo  connection 不存在 \(roomid)")
            return
        }
        ShowLogger.info("setOffMediaOptionsVideo with roomid = \(roomid)")
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.autoSubscribeVideo = false
        
        _updateChannelEx(options: mediaOptions, connection: connection)
    }
    
    func setOffMediaOptionsAudio() {
        VideoLoaderApiImpl.shared.getConnectionMap().forEach { _, connention in
            let mediaOptions = AgoraRtcChannelMediaOptions()
            mediaOptions.autoSubscribeAudio = false
            
            _updateChannelEx(options: mediaOptions, connection: connention)
        }
    }
    
}
// MARK: - IVideoLoaderApiListener
extension ShowAgoraKitManager: IVideoLoaderApiListener {
    public func debugInfo(_ message: String) {
        ShowLogger.info(message, context: "VideoLoaderApi")
    }
    public func debugWarning(_ message: String) {
        ShowLogger.warn(message, context: "VideoLoaderApi")
    }
    public func debugError(_ message: String) {
        ShowLogger.error(message, context: "VideoLoaderApi")
    }
}
// MARK: - AgoraRtcMediaPlayerDelegate
extension ShowAgoraKitManager: AgoraRtcMediaPlayerDelegate {
    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, reason: AgoraMediaPlayerReason) {
        if state == .openCompleted {
            playerKit.play()
        }
    }
}


extension ShowAgoraKitManager {
    func preGenerateToken(completion: ((String?)->())?) {
        AppContext.shared.rtcToken = nil
        NetworkManager.shared.generateToken(channelName: "",
                                            uid: UserInfo.userId,
                                            tokenType: .token007,
                                            type: .rtc) { token in
            guard let token = token else {
                ShowLogger.error("renewToken fail: token is empty")
                completion?(nil)
                return
            }
            AppContext.shared.rtcToken = token
            completion?(token)
        }
    }
}
