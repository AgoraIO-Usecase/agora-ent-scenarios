import Foundation
import AgoraRtcKit
import AgoraCommon

protocol AIChatRTCServiceProtocol {
    func joinChannel(channelName: String)
    
    func leaveChannel(channelName: String)
    
    func addDelegate(channelName: String, delegate: AgoraRtcEngineDelegate)

    func removeDelegate(channelName: String, delegate: AgoraRtcEngineDelegate)
                
    func destory()
}

class AIChatRTCService: NSObject {
    var rtcKit: AgoraRtcEngineKit?
    var token: String = ""
    
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
        
        self.rtcKit = rtcKit
    }
}

extension AIChatRTCService: AIChatRTCServiceProtocol {
    func addDelegate(channelName: String, delegate: AgoraRtcEngineDelegate) {
        let uid = Int(VLUserCenter.user.id) ?? 0
        aichatPrint("addDelegate[\(channelName)] uid:\(uid)", content: "AIChatRTCService")
        let connection = AgoraRtcConnection(channelId: channelName, localUid: uid)
        rtcKit?.addDelegateEx(delegate, connection: connection)
    }
    
    func removeDelegate(channelName: String, delegate: AgoraRtcEngineDelegate) {
        let uid = Int(VLUserCenter.user.id) ?? 0
        aichatPrint("removeDelegate[\(channelName)] uid:\(uid)", content: "AIChatRTCService")
        let connection = AgoraRtcConnection(channelId: channelName, localUid: uid)
        rtcKit?.removeDelegateEx(delegate, connection: connection)
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
        AgoraRtcEngineKit.destroy()
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
