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
    private var exConnection: AgoraRtcConnection?
    private lazy var videoEncoderConfig: AgoraVideoEncoderConfiguration = {
        return AgoraVideoEncoderConfiguration(size: CGSize(width: 540, height: 960),
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
        config.frameRate = 15
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
        AgoraRtcEngineKit.destroy()
    }
    
    func leaveChannelEx() {
        guard let connection = exConnection else { return }
        agoraKit?.leaveChannelEx(connection)
    }
    
    func joinChannelEx(channelName: String?, ownerId: String?, view: UIView) {
        let roleOptions = AgoraClientRoleOptions()
        roleOptions.audienceLatencyLevel = .ultraLowLatency
        agoraKit?.setClientRole(.audience, options: roleOptions)
        let uid = UInt(ownerId ?? "0") ?? 0
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.autoSubscribeAudio = true
        mediaOptions.autoSubscribeVideo = true
        mediaOptions.publishCameraTrack = false
        mediaOptions.publishMicrophoneTrack = false
        let connection = AgoraRtcConnection()
        connection.channelId = channelName ?? ""
        connection.localUid = UInt(VLUserCenter.user.id) ?? 0
        agoraKit.joinChannelEx(byToken: AppContext.shared.appRtcToken(),
                                         connection: connection,
                                         delegate: delegate,
                                         mediaOptions: mediaOptions,
                                         joinSuccess: nil)
        
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = view
        videoCanvas.renderMode = .hidden
        agoraKit.setupRemoteVideoEx(videoCanvas, connection: connection)
        
        exConnection = connection
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
//            let connection = AgoraRtcConnection()
//            connection.localUid = uid
//            connection.channelId = channelName
//            canvas.uid = UInt(ownerId) ?? 0
//            agoraKit.setupRemoteVideoEx(canvas, connection: connection)
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
