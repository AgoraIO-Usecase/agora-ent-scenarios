import Foundation
import AgoraRtcKit
import AgoraCommon

protocol AIChatRTCDelegate: AnyObject {
    func didJoinedChannel()
}

protocol AIChatRTCEvent {
    func joinChannel(channelName: String)
    func muteLocalAudioStream(mute: Bool) -> Int32
    func destory()
}

class AIChatRTCService: NSObject {
    private var appId: String
    private var rtcKit: AgoraRtcEngineKit!
    weak var delegate: AIChatRTCDelegate?
    
    init(appId: String) {
        self.appId = appId
        super.init()
        setupRtc()
    }
    
    private func setupRtc() {
        let config = AgoraRtcEngineConfig()
        config.appId = self.appId
        config.areaCode = .global
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .chatRoom
        
        rtcKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: self)
        rtcKit.setClientRole(.broadcaster)
        rtcKit.disableVideo()
        rtcKit.enableAudio()

        rtcKit.setAudioProfile(.default)
        rtcKit.setDefaultAudioRouteToSpeakerphone(true)
        
        rtcKit.setRecordingAudioFrameParametersWithSampleRate(44100, channel: 1, mode: .readWrite, samplesPerCall: 4410)
        rtcKit.setMixedAudioFrameParametersWithSampleRate(44100, channel: 1, samplesPerCall: 4410)
        rtcKit.setPlaybackAudioFrameParametersWithSampleRate(44100, channel: 1, mode: .readWrite, samplesPerCall: 4410)
        
        rtcKit.enableAudioVolumeIndication(200, smooth: 3, reportVad: true)
    }
}

extension AIChatRTCService: AIChatRTCEvent {
    func destory() {
        self.rtcKit.enable(inEarMonitoring: false)
        self.rtcKit.disableAudio()
        self.rtcKit.disableVideo()
        self.rtcKit.leaveChannel()
    }
    
    func joinChannel(channelName: String) {
        let option = AgoraRtcChannelMediaOptions()
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = true
        option.clientRoleType = .broadcaster
        NetworkManager.shared.generateToken(channelName: channelName, uid: "", tokenTypes: [.rtc]) { [weak self] token in
            guard let self = self else { return }
            let result = self.rtcKit.joinChannel(byToken: token, channelId: channelName, uid: 0, mediaOptions: option)
            if result != 0 {
                //error
            }
        }
    }
    
    func muteLocalAudioStream(mute: Bool) -> Int32 {
        return self.rtcKit.muteLocalAudioStream(mute)
    }
}

extension AIChatRTCService: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        
    }
}
