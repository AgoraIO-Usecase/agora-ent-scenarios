//
//  ShowAgoraKitManager.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/22.
//

import Foundation
import AgoraRtcKit

class ShowAgoraKitManager: NSObject {
    
    private lazy var videoEncoderConfig: AgoraVideoEncoderConfiguration = {
        return AgoraVideoEncoderConfiguration(size: CGSize(width: 480, height: 840),
                                       frameRate: .fps30,
                                       bitrate: AgoraVideoBitrateStandard,
                                       orientationMode: .fixedPortrait,
                                       mirrorMode: .auto)
    }()
    
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
        return config
    }()
    
    private (set) var agoraKit: AgoraRtcEngineKit!
    
    weak var delegate: AgoraRtcEngineDelegate? {
        didSet {
            agoraKit.delegate = delegate
        }
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
        
        let canvas = AgoraRtcVideoCanvas()
        canvas.renderMode = .hidden
        canvas.mirrorMode = .disabled
        canvas.view = canvasView
        agoraKit.setupLocalVideo(canvas)
        agoraKit.enableVideo()
        agoraKit.startPreview()
    }
    
    /// 切换摄像头
    func switchCamera() {
        agoraKit.switchCamera()
    }
    
    /// 设置采集分辨率
    /// - Parameter size: 分辨率
    func setCaptureVideoDimensions(_ size: CGSize){
        agoraKit.disableVideo()
        agoraKit.enableVideo()
        captureConfig.dimensions = CGSize(width: size.width, height: size.height)
        agoraKit?.setCameraCapturerConfiguration(captureConfig)
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
        AgoraRtcEngineKit.destroy()
    }
    
    func joinChannel(channelName: String, uid: UInt, ownerId: String, canvasView: UIView) -> Int32? {
        
        let role: AgoraClientRole = ownerId == VLUserCenter.user.id ? .broadcaster : .audience
        let roleOptions = AgoraClientRoleOptions()
        roleOptions.audienceLatencyLevel = role == .audience ? .ultraLowLatency : .lowLatency
        agoraKit?.setClientRole(role, options: roleOptions)
        agoraKit?.enableVideo()
        
        let canvas = AgoraRtcVideoCanvas()
        canvas.view = canvasView
        canvas.renderMode = .hidden
        if role == .broadcaster {
            canvas.uid = uid
            canvas.mirrorMode = .disabled
            agoraKit?.setVideoFrameDelegate(self)
            agoraKit.setDefaultAudioRouteToSpeakerphone(true)
            agoraKit.enableAudio()
            agoraKit.setupLocalVideo(canvas)
            agoraKit.startPreview()
        } else {
            canvas.uid = UInt(ownerId) ?? 0
            agoraKit.setupRemoteVideo(canvas)
        }
        
        let ret = agoraKit?.joinChannel(byToken: AppContext.shared.appRtcToken(), channelId: channelName, info: nil, uid: uid)
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
