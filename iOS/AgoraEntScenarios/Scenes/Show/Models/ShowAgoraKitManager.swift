//
//  ShowAgoraKitManager.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/22.
//

import Foundation
import AgoraRtcKit
import UIKit

//TODO: fix retain cycle
class ShowAgoraExProxy: NSObject, AgoraRtcEngineDelegate {
    weak var delegate: AgoraRtcEngineDelegate?
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, reportRtcStats stats: AgoraChannelStats) {
        delegate?.rtcEngine?(engine, reportRtcStats: stats)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
        delegate?.rtcEngine?(engine, localAudioStats: stats)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, localVideoStats stats: AgoraRtcLocalVideoStats, sourceType: AgoraVideoSourceType) {
        delegate?.rtcEngine?(engine, localVideoStats: stats, sourceType: sourceType)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
        delegate?.rtcEngine?(engine, remoteVideoStats: stats)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteAudioStats stats: AgoraRtcRemoteAudioStats) {
        delegate?.rtcEngine?(engine, remoteAudioStats: stats)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, uplinkNetworkInfoUpdate networkInfo: AgoraUplinkNetworkInfo) {
        delegate?.rtcEngine?(engine, uplinkNetworkInfoUpdate: networkInfo)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, downlinkNetworkInfoUpdate networkInfo: AgoraDownlinkNetworkInfo) {
        delegate?.rtcEngine?(engine, downlinkNetworkInfoUpdate: networkInfo)
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
    
    private var exConnection: AgoraRtcConnection?
    
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
    
    private (set) var agoraKit: AgoraRtcEngineKit!
    
    weak var delegate: AgoraRtcEngineDelegate? {
        didSet {
            agoraKit.delegate = delegate
        }
    }
    
    deinit {
        AgoraRtcEngineKit.destroy()
        print("deinit-- ShowAgoraKitManager")
    }
    
    override init() {
        super.init()
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: nil)
        setupContentInspectConfig()
        print("init-- ShowAgoraKitManager")
    }
    
    //MARK: private
    private func setupContentInspectConfig() {
        let config = AgoraContentInspectConfig()
        let dic: [String: String] = [
            "id": VLUserCenter.user.id,
            "sceneName": "show"
        ]
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: dic, options: .prettyPrinted) else {
            print("setupContentInspectConfig fail")
            return
        }
        let jsonStr = String(data: jsonData, encoding: .utf8)
        config.extraInfo = jsonStr
        let module = AgoraContentInspectModule()
        module.interval = 30
        module.type = .moderation
        config.modules = [module]
        let ret = agoraKit.enableContentInspect(true, config: config)
        print("setupContentInspectConfig: \(ret)")
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
                                     "traceId": UUID().uuid16string(),
                                     "src": "iOS",
                                     "payload": JSONObject.toJsonString(dict: userInfo) ?? ""]
        NetworkManager.shared.postRequest(urlString: "https://toolbox.bj2.agoralab.co/v1/moderation/audio",
                                          params: parasm) { response in
            print("response === \(response)")
        } failure: { errr in
            print(errr)
        }
    }
    
    /// 初始化并预览
    /// - Parameter canvasView: 画布
    func startPreview(canvasView: UIView) {
        agoraKit?.setClientRole(.broadcaster)
        agoraKit?.setVideoEncoderConfiguration(videoEncoderConfig)
        agoraKit?.setVideoFrameDelegate(self)
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
    
    /// 开启虚化背景
    func enableVirtualBackground(isOn: Bool) {
        let source = AgoraVirtualBackgroundSource()
        source.backgroundSourceType = .blur
        source.blurDegree = .high
        agoraKit.enableVirtualBackground(isOn, backData: source, segData: nil)
    }
    
    /// 设置虚拟背景
    func seVirtualtBackgoundImage(imagePath: String?, isOn: Bool) {
        guard let bundlePath = Bundle.main.path(forResource: "showResource", ofType: "bundle"),
              let bundle = Bundle(path: bundlePath) else { return }
        let imgPath = bundle.path(forResource: imagePath, ofType: "jpg")
        let source = AgoraVirtualBackgroundSource()
        source.backgroundSourceType = .img
        source.source = imgPath
        agoraKit.enableVirtualBackground(isOn, backData: source, segData: nil)
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
        print("采集分辨率切换为 width = \(size.width), height = \(size.height)")
        agoraKit?.setCameraCapturerConfiguration(captureConfig)
        agoraKit.enableVideo()
    }
    
    /// 设置编码分辨率
    /// - Parameter size: 分辨率
    func setVideoDimensions(_ size: CGSize){
        videoEncoderConfig.dimensions = CGSize(width: size.width, height: size.height)
        agoraKit?.setVideoEncoderConfiguration(videoEncoderConfig)
    }
    
    func leaveChannel(){
        agoraKit?.leaveChannel({stats in
            print("leave channel: \(stats)")
        })
        agoraKit.stopPreview()
        agoraKit?.disableAudio()
        agoraKit?.disableVideo()
//        AgoraRtcEngineKit.destroy()
    }
    
    func leaveChannelEx() {
        guard let connection = exConnection else { return }
        agoraKit?.leaveChannelEx(connection)
    }
    
    func joinChannelEx(channelName: String?,
                       ownerId: String?,
                       options:AgoraRtcChannelMediaOptions,
                       view: UIView,
                       role: AgoraClientRole) {
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.autoSubscribeAudio = true
        mediaOptions.autoSubscribeVideo = true
        mediaOptions.publishCameraTrack = false
        mediaOptions.publishMicrophoneTrack = options.publishMicrophoneTrack//role == .broadcaster
        mediaOptions.clientRoleType = role
    
        let connection = AgoraRtcConnection()
        connection.channelId = channelName ?? ""
        connection.localUid = UInt(VLUserCenter.user.id) ?? 0
        
        NetworkManager.shared.generateToken(channelName: channelName ?? "", 
                                            uid: VLUserCenter.user.id,
                                            tokenType: .token007,
                                            type: .rtc) {[weak self] token in
            guard let self = self else { return }
            //TODO: retain cycle in joinChannelEx
            let proxy = ShowAgoraExProxy()
            proxy.delegate = self.delegate
            self.agoraKit.joinChannelEx(byToken: token,
                                        connection: connection,
                                        delegate: proxy,
                                        mediaOptions: mediaOptions,
                                        joinSuccess: nil)
            
            let uid = UInt(ownerId ?? "0") ?? 0
            let videoCanvas = AgoraRtcVideoCanvas()
            videoCanvas.uid = uid
            videoCanvas.view = view
            videoCanvas.renderMode = .hidden
            self.agoraKit.setupRemoteVideoEx(videoCanvas, connection: connection)
        }
        exConnection = connection
    }
    
    func joinChannel(channelName: String, uid: UInt, ownerId: String, canvasView: UIView) -> Int32? {
        let role: AgoraClientRole = ownerId == VLUserCenter.user.id ? .broadcaster : .audience
        let roleOptions = AgoraClientRoleOptions()
        roleOptions.audienceLatencyLevel = role == .audience ? .ultraLowLatency : .lowLatency
        agoraKit?.setClientRole(role, options: roleOptions)
        agoraKit?.enableVideo()
        
        let ret = agoraKit?.joinChannel(byToken: AppContext.shared.appRTCToken(),
                                        channelId: channelName,
                                        info: nil,
                                        uid: uid)
        
        canvas.view = canvasView
        if role == .broadcaster {
            canvas.uid = uid
            agoraKit?.setVideoFrameDelegate(self)
            agoraKit.setDefaultAudioRouteToSpeakerphone(true)
            agoraKit.enableAudio()
            agoraKit.setupLocalVideo(canvas)
            agoraKit.startPreview()
        } else {
            canvas.uid = UInt(ownerId) ?? 0
            agoraKit.setupRemoteVideo(canvas)
        }
        moderationAudio(channelName: channelName, role: role)
        return ret
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
