//
//  ShowAgoraKitManager.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/22.
//

import Foundation
import AgoraRtcKit
import UIKit

class ShowAgoraKitManager: NSObject {
    
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
        print("--ShowAgoraKitManager deinit--AgoraRtcEngineKit.destroy----")
    }
    
    override init() {
        super.init()
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: nil)
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
    
    /// 切换连麦角色
    func switchRole(role: AgoraClientRole, uid: String?, canvasView: UIView) {
        let options = AgoraRtcChannelMediaOptions()
        options.clientRoleType = role
        options.publishMicrophoneTrack = role == .broadcaster
        options.publishCameraTrack = role == .broadcaster
        agoraKit.updateChannel(with: options)
        agoraKit.setClientRole(role)
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = UInt(uid ?? "0") ?? 0
        videoCanvas.renderMode = .hidden
        videoCanvas.view = canvasView
        if uid == VLUserCenter.user.id {
            agoraKit.setupLocalVideo(videoCanvas)
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
                       view: UIView,
                       role: AgoraClientRole) {
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.autoSubscribeAudio = true
        mediaOptions.autoSubscribeVideo = true
        mediaOptions.publishCameraTrack = false
        mediaOptions.publishMicrophoneTrack = role == .broadcaster
        mediaOptions.clientRoleType = role
    
        let connection = AgoraRtcConnection()
        connection.channelId = channelName ?? ""
        connection.localUid = UInt(VLUserCenter.user.id) ?? 0
        
        NetworkManager.shared.generateToken(channelName: channelName ?? "", 
                                            uid: VLUserCenter.user.id,
                                            tokenType: .token007,
                                            type: .rtc) { token in
            self.agoraKit.joinChannelEx(byToken: token,
                                        connection: connection,
                                        delegate: self.delegate,
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
        
        let ret = agoraKit?.joinChannel(byToken: AppContext.shared.appRtcToken(),
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
        return ret
    }
}


extension ShowAgoraKitManager: AgoraVideoFrameDelegate {
    
    func onCapture(_ videoFrame: AgoraOutputVideoFrame) -> Bool {
        videoFrame.pixelBuffer = ByteBeautyManager.shareManager.processFrame(pixelBuffer: videoFrame.pixelBuffer)
        return true
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
