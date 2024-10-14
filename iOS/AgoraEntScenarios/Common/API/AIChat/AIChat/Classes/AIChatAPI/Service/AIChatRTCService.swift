import Foundation
import AgoraRtcKit
import AgoraCommon

protocol AIChatRTCServiceProtocol {
    func joinChannel(channelName: String)
    
    func leaveChannel(channelName: String)
    
    func addDelegate(channelName: String, delegate: AgoraRtcEngineDelegate)

    func removeDelegate(channelName: String, delegate: AgoraRtcEngineDelegate)
                
    func sendDataStream(to userId: Int, cmd: String) -> String
    
    func destory()
}

class AIChatRTCService: NSObject {
    var rtcKit: AgoraRtcEngineKit?
    var token: String = ""
    private var dataStreamId: Int = 0
    
    init(appId: String, convertService: AIChatAudioTextConvertorService?) {
        super.init()
        
        let config = AgoraRtcEngineConfig()
        config.appId = appId
        config.areaCode = .global
        config.channelProfile = .liveBroadcasting
        config.eventDelegate = convertService
        let logConfig = AgoraLogConfig()
        logConfig.filePath = AgoraEntLog.sdkLogPath()
        config.logConfig = logConfig
        
        let rtcKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: nil)
        rtcKit.setDefaultAudioRouteToSpeakerphone(true)
        rtcKit.muteLocalAudioStream(true)
        rtcKit.muteLocalAudioStream(true)
        rtcKit.setParameters ("{\"che.audio.sf.nsEnable\":1}")
        rtcKit.setParameters ("{\"che.audio.sf.ainsToLoadFlag\" :1}")
        rtcKit.setParameters ("{\"che.audio.sf.nsngAlgRoute\":12}")
        rtcKit.setParameters ("{\"che.audio.sf.nsngPredefAgg\" :10}")
        rtcKit.setAudioScenario(.gameStreaming)
        rtcKit.setAudioProfile(.default)
        self.rtcKit = rtcKit
    }
}

extension AIChatRTCService: AIChatRTCServiceProtocol {
    func addDelegate(channelName: String, delegate: AgoraRtcEngineDelegate) {
        let uid = Int(VLUserCenter.user.id) ?? 0
        aichatPrint("addDelegate[\(channelName)] uid:\(uid)", context: "AIChatRTCService")
        let connection = AgoraRtcConnection(channelId: channelName, localUid: uid)
        rtcKit?.addDelegateEx(delegate, connection: connection)
    }
    
    func removeDelegate(channelName: String, delegate: AgoraRtcEngineDelegate) {
        let uid = Int(VLUserCenter.user.id) ?? 0
        aichatPrint("removeDelegate[\(channelName)] uid:\(uid)", context: "AIChatRTCService")
        let connection = AgoraRtcConnection(channelId: channelName, localUid: uid)
        rtcKit?.removeDelegateEx(delegate, connection: connection)
    }
    
    func joinChannel(channelName: String) {
        guard let rtcKit = rtcKit else { return }
        let option = AgoraRtcChannelMediaOptions()
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = false
        option.autoSubscribeVideo = false
        option.autoSubscribeAudio = false
        option.clientRoleType = .audience
        
        let uid = Int(VLUserCenter.user.id) ?? 0
        let connection = AgoraRtcConnection(channelId: channelName, localUid: uid)
        let ret = rtcKit.joinChannelEx(byToken: token,
                                       connection: connection,
                                       delegate: nil,
                                       mediaOptions: option) { channel, uid, elapsed in
            aichatPrint("join channel[\(channel)] success  uid: \(uid) elapsed: \(elapsed)", context: "AIChatRTCService")
        }
        aichatPrint("join channel[\(channelName)] start uid: \(uid) ret: \(ret)", context: "AIChatRTCService")
    }
    
    func updateRole(channelName: String, role: AgoraClientRole) {
        let uid = Int(VLUserCenter.user.id) ?? 0
        aichatPrint("updateRole[\(channelName)] role:\(role.rawValue)", context: "AIChatRTCService")
        let connection = AgoraRtcConnection(channelId: channelName, localUid: uid)
        
        let option = AgoraRtcChannelMediaOptions()
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = role == .audience ? false : true
        option.autoSubscribeVideo = false
        option.autoSubscribeAudio = role == .audience ? false : true
        option.clientRoleType = role
        if role == .broadcaster {
            rtcKit?.enableLocalAudio(true)
            rtcKit?.setEnableSpeakerphone(true)
            rtcKit?.setDefaultAudioRouteToSpeakerphone(true)
            rtcKit?.enableAudioVolumeIndicationEx(50, smooth: 10, reportVad: true, connection: connection)
        }
        rtcKit?.updateChannelEx(with: option, connection: connection)
    }
    
    func muteLocalAudioStream(channelName: String, isMute: Bool) {
        let uid = Int(VLUserCenter.user.id) ?? 0
        aichatPrint("muteAudio[\(channelName)] isMute:\(isMute)", context: "AIChatRTCService")
        let connection = AgoraRtcConnection(channelId: channelName, localUid: uid)
        rtcKit?.muteLocalAudioStreamEx(isMute, connection: connection)
    }
    
    func leaveChannel(channelName: String) {
        aichatPrint("leaveChannel[\(channelName)]", context: "AIChatRTCService")
        let uid = Int(VLUserCenter.user.id) ?? 0
        let connection = AgoraRtcConnection(channelId: channelName, localUid: uid)
        rtcKit?.leaveChannelEx(connection)
    }
    
    func sendDataStream(to userId: Int, cmd: String) -> String {
        let messageId = UUID().uuidString
        let map: [String : Any] = [
            "messageId": messageId,
            "to": userId,
            "type": 0,
            "cmdType": cmd,
            "payload": [:]
        ]
        
        let message = map.z.jsonString
        
        let config = AgoraDataStreamConfig()
        var result: Int32 = 0
        if dataStreamId == 0 {
             result = rtcKit?.createDataStream(&dataStreamId, config: config) ?? 0
        }
        
        aichatPrint("createDataStream call ret: \(result), message: \(message)")
        let sendResult = rtcKit?.sendStreamMessage(dataStreamId, data: Data(message.utf8))
        
        return messageId
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
