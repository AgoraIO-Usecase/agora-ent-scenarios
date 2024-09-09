import Foundation
import AgoraRtcKit
import AgoraCommon

protocol AIChatRTCDelegate: AnyObject, AgoraRtcEngineDelegate {}

protocol AIChatRTCEvent {
    func run(appId: String, channelName: String)
    
    func addDelegate(_ delegate: AIChatRTCDelegate)

    func removeDelegate(_ delegate: AIChatRTCDelegate)
    
    func removeAllDelegates()
        
    func muteLocalAudioStream(mute: Bool) -> Int32
    
    func destory()
}

class AIChatRTCService: NSObject {
    var rtcKit: AgoraRtcEngineKit!
    static let shared: AIChatRTCService = AIChatRTCService()
    let audioConvertorService = AIChatAudioTextConvertorService.shared

    private let delegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    private func setupRtc(appId: String) {
        let config = AgoraRtcEngineConfig()
        config.appId = appId
        config.areaCode = .global
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .chatRoom
        config.eventDelegate = audioConvertorService
        
        rtcKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: self)
        rtcKit.disableVideo()
        rtcKit.setDefaultAudioRouteToSpeakerphone(true)
        rtcKit.setClientRole(.broadcaster)
        
        audioConvertorService.run(appId: AppContext.shared.hyAppId, apiKey: AppContext.shared.hyAPIKey, apiSecret: AppContext.shared.hyAPISecret, convertType: .normal, agoraRtcKit: rtcKit)
    }
}

extension AIChatRTCService: AIChatRTCEvent {
    func run(appId: String, channelName: String) {
        setupRtc(appId: appId)
        joinChannel(channelName: channelName)
    }
    
    func addDelegate(_ delegate: any AIChatRTCDelegate) {
        delegates.add(delegate)
    }
    
    func removeDelegate(_ delegate: any AIChatRTCDelegate) {
        delegates.remove(delegate)
    }
    
    func removeAllDelegates() {
        delegates.removeAllObjects()
    }
    
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
        option.autoSubscribeAudio = false
        option.autoSubscribeVideo = false

        self.rtcKit.joinChannel(byToken: nil, channelId: "agora_extension", uid: 0, mediaOptions: option)
        self.rtcKit.setEnableSpeakerphone(true)
    }
    
    func muteLocalAudioStream(mute: Bool) -> Int32 {
        return self.rtcKit.muteLocalAudioStream(mute)
    }
}

extension AIChatRTCService: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        for delegate in delegates.allObjects {
            (delegate as? AIChatRTCDelegate)?.rtcEngine?(engine, didJoinChannel: channel, withUid: uid, elapsed: elapsed)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        for delegate in delegates.allObjects {
            (delegate as? AIChatRTCDelegate)?.rtcEngine?(engine, didJoinedOfUid: uid, elapsed: elapsed)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        for delegate in delegates.allObjects {
            (delegate as? AIChatRTCDelegate)?.rtcEngine?(engine, didOfflineOfUid: uid, reason: reason)
        }
    }
}
