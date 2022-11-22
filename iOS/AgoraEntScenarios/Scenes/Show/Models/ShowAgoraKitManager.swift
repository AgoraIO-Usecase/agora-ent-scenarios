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
        /// 开启扬声器
        agoraKit?.setDefaultAudioRouteToSpeakerphone(true)
        let canvas = AgoraRtcVideoCanvas()
//        canvas.uid = UInt(VLUserCenter.user.id) ?? 0
        canvas.renderMode = .hidden
        canvas.view = canvasView
        agoraKit?.setupLocalVideo(canvas)
        agoraKit?.enableAudio()
        agoraKit?.enableVideo()
        agoraKit?.startPreview()
    }
    
    /// 切换摄像头
    func switchCamera() {
        agoraKit.switchCamera()
    }
    
    /// 设置分辨率
    /// - Parameter size: 分辨率
    func setVideoDimensions(_ size: CGSize){
        videoEncoderConfig.dimensions = CGSize(width: size.width, height: size.height)
        agoraKit?.setVideoEncoderConfiguration(videoEncoderConfig)
    }
    
    func leaveChannel(){
        agoraKit?.leaveChannel({stats in
            print("leave channel: \(stats)")
        })
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
        agoraKit?.enableAudio()
        agoraKit?.setDefaultAudioRouteToSpeakerphone(true)
        
        let ret = agoraKit?.joinChannel(byToken: nil, channelId: channelName, info: nil, uid: uid)
        let canvas = AgoraRtcVideoCanvas()
        canvas.view = canvasView
        canvas.renderMode = .hidden
        if role == .broadcaster {
            canvas.uid = uid
            agoraKit?.setupLocalVideo(canvas)
            agoraKit?.startPreview()
        } else {
            canvas.uid = UInt(ownerId) ?? 0
            agoraKit?.setupRemoteVideo(canvas)
        }
        return ret
    }
    
}

