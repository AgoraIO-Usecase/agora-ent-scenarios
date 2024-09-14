import Foundation
import AgoraRtcKit
import AgoraCommon

protocol AIChatRTCDelegate: AnyObject, AgoraRtcEngineDelegate {}

protocol AIChatRTCServiceProtocol {
    func joinChannel(channelName: String)
    
    func leaveChannel(channelName: String)
    
    func addDelegate(_ delegate: AIChatRTCDelegate)

    func removeDelegate(_ delegate: AIChatRTCDelegate)
    
    func removeAllDelegates()
            
    func destory()
}

class AIChatRTCService: NSObject {
    var rtcKit: AgoraRtcEngineKit?
    var token: String = ""
    
    private let delegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    init(appId: String, convertService: AIChatAudioTextConvertorService?) {
        super.init()
        
        let config = AgoraRtcEngineConfig()
        config.appId = appId
        config.areaCode = .global
        config.channelProfile = .liveBroadcasting
        config.eventDelegate = convertService
        
        let rtcKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: nil)
        rtcKit.setDefaultAudioRouteToSpeakerphone(true)
        rtcKit.muteLocalAudioStream(true)
        rtcKit.muteLocalAudioStream(true)
        rtcKit.addDelegate(self)
        
        self.rtcKit = rtcKit
    }
}

extension AIChatRTCService: AIChatRTCServiceProtocol {
    func addDelegate(_ delegate: any AIChatRTCDelegate) {
        delegates.add(delegate)
    }
    
    func removeDelegate(_ delegate: any AIChatRTCDelegate) {
        delegates.remove(delegate)
    }
    
    func removeAllDelegates() {
        delegates.removeAllObjects()
    }
    
    func joinChannel(channelName: String) {
        guard let rtcKit = rtcKit else { return }
        
        let option = AgoraRtcChannelMediaOptions()
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = true
        option.autoSubscribeVideo = false
        option.autoSubscribeAudio = true
        option.clientRoleType = .broadcaster
        
        
        let uid = Int(VLUserCenter.user.id) ?? 0
        let connection = AgoraRtcConnection(channelId: channelName, localUid: uid)
        let ret = rtcKit.joinChannelEx(byToken: token,
                                       connection: connection,
                                       delegate: nil,
                                       mediaOptions: option) { channel, uid, elapsed in
            aichatPrint("join channel success \(channel) \(uid) \(elapsed)", content: "AIChatRTCService")
        }
        aichatPrint("join channel start channelName:\(channelName) uid:\(uid) ret: \(ret)", content: "AIChatRTCService")
        rtcKit.enableLocalVideo(true)
        rtcKit.setEnableSpeakerphone(true)
    }
    
    func leaveChannel(channelName: String) {
        aichatPrint("leaveChannel[\(channelName)]", content: "AIChatRTCService")
        let uid = Int(VLUserCenter.user.id) ?? 0
        let connection = AgoraRtcConnection(channelId: channelName, localUid: uid)
        rtcKit?.leaveChannelEx(connection)
    }
    
    func destory() {
        guard let rtcKit = rtcKit else {
            return
        }
        rtcKit.leaveChannel()
        AgoraRtcEngineKit.destroy()
        removeAllDelegates()
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

//MARK: - Music Player
extension AIChatRTCService {
    func createMediaPlayer(delegate: AgoraRtcMediaPlayerDelegate?) ->AgoraRtcMediaPlayerProtocol? {
        let mediaPlayer = rtcKit?.createMediaPlayer(with: delegate)
        return mediaPlayer
    }
    
    func destroyMediaPlayer(mediaPlayer: AgoraRtcMediaPlayerProtocol) {
        rtcKit?.destroyMediaPlayer(mediaPlayer)
    }
}
