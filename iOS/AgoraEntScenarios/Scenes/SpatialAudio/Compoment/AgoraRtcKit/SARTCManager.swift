//
//  SARTCManager.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/5.
//

import AgoraRtcKit
import Foundation

public struct SARtcType {
    /**
     * 用户角色枚举
     * unknown 未知角色，异常
     * owner 主播，老师
     * coHost 合唱 ，学生
     * audience  观众 ，旁听
     */
    @objc public enum ASRoleType: Int, Codable {
        case unknown = -1
        case owner = 0
        case coHost = 1
        case audience = 2
    }

    /**
     * AI降噪等级
     *
     */
    public enum AINS_STATE {
        case high
        case mid
        case off
    }

    /**
     * 机器人类型
     *
     */
    @objc public enum ALIEN_TYPE: Int, Codable {
        case blue = 1
        case red = 2
        case blueAndRed = 3
        case none = 4
        case ended = 5
    }

    /**
     *
     */
    @objc public enum VMMUSIC_TYPE: Int, Codable {
        case alien = 1
        case ainsHigh = 2
        case ainsMid = 3
        case ainsOff = 4
        case sound = 5
        case game = 6
        case social = 7
        case ktv = 8
        case anchor = 9
        case Spatical = 10
    }

    /**
     * 场景枚举
     * VoiceChat 语聊房2.0
     * KTV KTV场景
     */
    @objc public enum ASManagerType: Int {
        case VoiceChat = 1
        case KTV = 2
        case SpatialAudio = 3
    }

    /**
     * 回声消除等级
     * NoEcho 对应UI上的零回声
     * Standard 对应UI上的标准
     * Fluent 对应UI上的流畅
     */
    @objc public enum AECGrade: Int {
        // FIXME: 枚举值首字母小写
        case NoEcho = 1
        case Standard = 3
        case Fluent = 5
    }

    @objc public enum VMScene: Int {
        case game = 0
        case social = 1
        case ktv = 2
        case anchor = 3
    }
}

// MARK: - VMMusicPlayerDelegate

@objc public protocol SAMusicPlayerDelegate: NSObjectProtocol {
    /**
     * datastream消息消息回调
     * @param uid 用户的Uid
     * @param data 用户收到的消息
     */
    @objc optional func didReceiveStreamMsgOfUid(uid: UInt, data: Data) -> Void

    /**
     * MPK的seek进度
     * @param position MPK当前修改的进度
     */
    @objc optional func didMPKChangedToPosition(position: Int) -> Void

    /**
     * MPK 当前状态回调
     * @param state MPK当前的状态
     * @param error MPK当前的错误码
     */
    @objc optional func didMPKChangedTo(_ playerKit: AgoraRtcMediaPlayerProtocol, state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) -> Void // MPK 状态回调
}

// MARK: - VMManagerDelegate

@objc public protocol SAManagerDelegate: NSObjectProtocol {
    /**
     * RTC远端用户下线
     * @param uid 远端下线用户Uid
     */
    @objc optional func didRtcUserOfflineOfUid(uid: UInt) -> Void

    /**
     * RTC当前用户加入频道
     * @param uid 当前用户Uid
     */
    @objc optional func didRtcLocalUserJoinedOfUid(uid: UInt) -> Void

    /**
     * RTC远端用户加入频道
     * @param uid 远端上线用户Uid
     */
    @objc optional func didRtcRemoteUserJoinedOfUid(uid: UInt) -> Void

    /**
     * 实时音量返回
     * @param speakers 返回的用户声音信息
     * @param totalVolume 返回当前的总音量
     */
    @objc optional func reportAudioVolumeIndicationOfSpeakers(speakers: [AgoraRtcAudioVolumeInfo]) -> Void

    /**
     * 用户视频第一帧
     * @param size 渲染的视频尺寸（宽度和高度）
     * @param elapsed 视频显示出来第一帧的时间
     */
    // @objc optional func didUserFirstVideoFrameWith(uid: UInt) -> Void

    /**
     * report alien type
     */
    @objc optional func reportAlien(with type: SARtcType.ALIEN_TYPE, musicType: SARtcType.VMMUSIC_TYPE) -> Void
    
    @objc optional func didOccurError(with code: AgoraErrorCode) -> Void
}

public let kMPK_RTC_UID_SA: UInt = 1
@objc public class SARTCManager: NSObject {
    // init manager
    private static var _sharedInstance: SARTCManager?

    private var mediaPlayer: AgoraRtcMediaPlayerProtocol?

    private var role: SARtcType.ASRoleType = .audience

    private var type: SARtcType.ASManagerType = .VoiceChat

    private var channelName: String?

    private var streamId: Int = -1

    fileprivate var localRtcUid: UInt = 0

    private var musicType: SARtcType.VMMUSIC_TYPE?

    @objc public weak var delegate: SAManagerDelegate?

    @objc public weak var playerDelegate: SAMusicPlayerDelegate?
    
    public var localSpatial: AgoraLocalSpatialAudioKit?
    
    public var redMediaPlayer: AgoraRtcMediaPlayerProtocol?
    public var blueMediaPlayer: AgoraRtcMediaPlayerProtocol?

    // 单例
    @objc public class func getSharedInstance() -> SARTCManager {
        guard let instance = _sharedInstance else {
            _sharedInstance = SARTCManager()
            return _sharedInstance!
        }
        return instance
    }

    private var baseMusicCount: Int = 0 {
        didSet {
            guard let musicType = musicType else {
                return
            }
            var count = 0
            var musicPath: String = ""
            var musicIndex: Int = 0
            switch musicType {
            case .alien:
                count = SAConfig.baseAlienMic.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = "\(SAConfig.CreateCommonRoom)\(SAConfig.baseAlienMic[musicIndex])"
            case .ainsHigh:
                count = SAConfig.HighAINSIntroduc.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = "\(SAConfig.SetAINSIntroduce)\(SAConfig.HighAINSIntroduc[musicIndex])"
            case .ainsMid:
                count = SAConfig.MediumAINSIntroduc.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = "\(SAConfig.SetAINSIntroduce)\(SAConfig.MediumAINSIntroduc[musicIndex])"
            case .ainsOff:
                count = SAConfig.NoneAINSIntroduc.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = "\(SAConfig.SetAINSIntroduce)\(SAConfig.NoneAINSIntroduc[musicIndex])"
            case .social:
                count = SAConfig.SoundSelectSocial.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = SAConfig.SoundSelectSocial[musicIndex]
            case .ktv:
                count = SAConfig.SoundSelectKTV.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = SAConfig.SoundSelectKTV[musicIndex]
            case .game:
                count = SAConfig.SoundSelectGame.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = SAConfig.SoundSelectGame[musicIndex]
            case .anchor:
                count = SAConfig.SoundSelectAnchor.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = SAConfig.SoundSelectAnchor[musicIndex]
            case .Spatical:
                count = SAConfig.spatialAlienMic.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = "\(SAConfig.CreateSpatialRoom)\(SAConfig.spatialAlienMic[musicIndex])"
            case .sound:
                return
            }
            if baseMusicCount >= count {
               // rtcKit.stopAudioMixing()
                redMediaPlayer?.stop()
                blueMediaPlayer?.stop()
                delegate?.reportAlien?(with: .ended, musicType: musicType)
            } else {
                musicPath = musicPath.replacingOccurrences(of: "EN", with: "Lau".localized())
                print("musicPath:\(musicPath)")
                if musicPath.contains("-B-") {
                    delegate?.reportAlien?(with: .blue, musicType: musicType)
                    blueMediaPlayer?.open(musicPath, startPos: 0)
                } else if musicPath.contains("-R-") {
                    delegate?.reportAlien?(with: .red, musicType: musicType)
                    redMediaPlayer?.open(musicPath, startPos: 0)
                } else if musicPath.contains("-B&R-") {
                    delegate?.reportAlien?(with: .blueAndRed, musicType: musicType)
                    blueMediaPlayer?.open(musicPath, startPos: 0)
                    redMediaPlayer?.open(musicPath, startPos: 0)
                }
                
               // rtcKit.startAudioMixing(musicPath, loopback: false, cycle: 1)
            }
        }
    }

    // init rtc
    private let rtcKit: AgoraRtcEngineKit = AgoraRtcEngineKit.sharedEngine(withAppId: KeyCenter.AppId, delegate: nil)

    /**
     * 设置RTC角色
     * @param role RMCRoleType
     */
    @objc public func setClientRole(role: SARtcType.ASRoleType) {
        rtcKit.setClientRole(role == .audience ? .audience : .broadcaster)
        self.role = role
    }


    /**
     * 加入语聊房
     * @param channelName 频道名称
     * @param rtcUid RTCUid 如果传0，大网会自动分配
     * @param rtmUid 可选，如果不使用RTM，使用自己的IM，这个值不用传
     * @param type 有四种 social，ktv，game， anchor
     */
    public func joinVoicRoomWith(with channelName: String,token: String?, rtcUid: Int?, type: SARtcType.VMMUSIC_TYPE) -> Int32 {
        self.type = .VoiceChat
        rtcKit.delegate = self
        rtcKit.enableAudioVolumeIndication(200, smooth: 3, reportVad: true)
        setParametersWithMD()
        if type == .ktv || type == .social {
            rtcKit.setChannelProfile(.liveBroadcasting)

            rtcKit.setAudioProfile(.musicHighQuality)
            rtcKit.setAudioScenario(.gameStreaming)
        } else if type == .game {
            rtcKit.setChannelProfile(.communication)
        } else if type == .anchor {
            rtcKit.setChannelProfile(.liveBroadcasting)
            rtcKit.setAudioProfile(.musicHighQualityStereo)
            rtcKit.setAudioScenario(.gameStreaming)
            rtcKit.setParameters("{\"che.audio.custom_payload_type\":73}")
            rtcKit.setParameters("{\"che.audio.custom_bitrate\":128000}")
            //  rtcKit.setRecordingDeviceVolume(128)
            rtcKit.setParameters("{\"che.audio.input_channels\":2}")
        }
        setAINS(with: .mid)
        rtcKit.setParameters("{\"che.audio.start_debug_recording\":\"all\"}")
        let code: Int32 = rtcKit.joinChannel(byToken: token, channelId: channelName, info: nil, uid: UInt(rtcUid ?? 0))
        return code
    }
    
    func initSpatialAudio(recvRange: Float) {
        localSpatial = AgoraLocalSpatialAudioKit()
        let localSpatialConfig = AgoraLocalSpatialAudioConfig()
        localSpatialConfig.rtcEngine = rtcKit
        localSpatial = AgoraLocalSpatialAudioKit.sharedLocalSpatialAudio(with: localSpatialConfig)
        localSpatial?.muteLocalAudioStream(false)
        localSpatial?.muteAllRemoteAudioStreams(false)
        localSpatial?.setAudioRecvRange(recvRange)
        localSpatial?.setMaxAudioRecvCount(6)
        localSpatial?.setDistanceUnit(1)
        rtcKit.setParameters("{\"che.audio.force_bluetooth_a2dp\":true}")
        
        let config = AgoraDataStreamConfig()
        config.ordered = false
        config.syncWithAudio = false
        rtcKit.createDataStream(&streamId, config: config)
    }
    
    public func setupSpatialAudio() {
        redMediaPlayer = initMediaPlayer()
        redMediaPlayer?.adjustPlayoutVolume(Int32(0.45 * 400))
        setPlayerAttenuation(attenuation: 0.2, playerId: redMediaPlayer?.getMediaPlayerId() ?? 0)
        blueMediaPlayer = initMediaPlayer()
        blueMediaPlayer?.adjustPlayoutVolume(Int32(0.45 * 400))
        setPlayerAttenuation(attenuation: 0.2, playerId: blueMediaPlayer?.getMediaPlayerId() ?? 0)
    }
    
    func initMediaPlayer() -> AgoraRtcMediaPlayerProtocol? {
        let mediaPlayer = rtcKit.createMediaPlayer(with: self)
        mediaPlayer?.setLoopCount(10000)
        return mediaPlayer
    }
    
    func setPlayerAttenuation(attenuation: Double, playerId: Int32) {
        guard playerId > 0 else { return }
        localSpatial?.setPlayerAttenuation(attenuation,
                                           playerId: UInt(playerId),
                                           forceSet: false)
    }
    
    func updatePlayerVolume(value: Double) {
        redMediaPlayer?.adjustPlayoutVolume(Int32(value))
        blueMediaPlayer?.adjustPlayoutVolume(Int32(value))
    }
    
    func setMediaPlayerPositionInfo(playerId: Int,
                                    position: [NSNumber],
                                    forward: [NSNumber]?) {
        let positionInfo = AgoraRemoteVoicePositionInfo()
        positionInfo.position = position
        positionInfo.forward = forward
        localSpatial?.updatePlayerPositionInfo(playerId,
                                               positionInfo: positionInfo)
    }
    
    func updateSpetialPostion(position: [NSNumber],
                              axisForward: [NSNumber],
                              axisRight: [NSNumber],
                              axisUp: [NSNumber]) {
        localSpatial?.updateSelfPosition(position,
                                         axisForward: axisForward,
                                         axisRight: axisRight,
                                         axisUp: axisUp)
    }
    
    func updateRemoteSpetialPostion(uid: String?,
                                    position: [NSNumber],
                                    forward: [NSNumber]?) {
        let positionInfo = AgoraRemoteVoicePositionInfo()
        positionInfo.position = position
        positionInfo.forward = forward
        let uid = UInt(uid ?? "0") ?? 0
        localSpatial?.updateRemotePosition(uid, positionInfo: positionInfo)
    }
    
    /**
     * 加载RTC
     * @param channelName 频道名称
     * @param rtcUid RTCUid 如果传0，大网会自动分配
     */
    private func loadKit(with channelName: String, rtcUid: Int?) {
        rtcKit.delegate = self
    }

    private func setParametersWithMD (){
        rtcKit.setParameters("{\"che.audio.md.enable\":false}")

    }
    /**
     * 加载RTC
     */
    private func loadRTC(with channalName: String, uid: Int) {
        rtcKit.enableVideo()
        rtcKit.startPreview()
    }

    /**
     * 开启/关闭 本地音频
     * @param enable 是否开启音频
     * @return 开启/关闭音频的结果
     */
    @discardableResult
    public func enableLocalAudio(enable: Bool) -> Int32 {
        return rtcKit.enableLocalAudio(enable)
    }

    /**
     *
     *
     */
    public func playMusic(with type: SARtcType.VMMUSIC_TYPE) {
        let code = rtcKit.stopAudioMixing()
        if code == 0 {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.03) {
                self.musicType = type
                self.baseMusicCount = 0
            }
        }
    }

    public func stopPlayMusic() {
        guard let musicType = musicType else {
            return
        }
        delegate?.reportAlien?(with: .none, musicType: musicType)
        if musicType == .alien {
            baseMusicCount = SAConfig.baseAlienMic.count
        } else if musicType == .ainsHigh {
            baseMusicCount = SAConfig.HighAINSIntroduc.count
        } else if musicType == .ainsMid {
            baseMusicCount = SAConfig.MediumAINSIntroduc.count
        } else if musicType == .ainsOff {
            baseMusicCount = SAConfig.NoneAINSIntroduc.count
        } else if musicType == .social {
            baseMusicCount = SAConfig.SoundSelectSocial.count
        } else if musicType == .ktv {
            baseMusicCount = SAConfig.SoundSelectKTV.count
        } else if musicType == .game {
            baseMusicCount = SAConfig.SoundSelectGame.count
        } else if musicType == .anchor {
            baseMusicCount = SAConfig.SoundSelectAnchor.count
        } else if musicType == .Spatical {
            baseMusicCount = SAConfig.spatialAlienMic.count
        }
    }

    public func playSound(with index: Int, type: SARtcType.VMMUSIC_TYPE) {
        stopPlayMusic()
        musicType = type
        var path = ""
        if type == .ainsHigh {
            path = SAConfig.HighSound[index]
        } else if type == .ainsOff {
            path = SAConfig.NoneSound[index]
        }
        path = path.replacingOccurrences(of: "CN", with: sceneLocalized("Lau"))
        rtcKit.startAudioMixing(path, loopback: false, cycle: 1)
    }

    public func stopPlaySound() {
        rtcKit.stopAudioMixing()
    }

    /**
     * 开启/关闭 回声消除
     * @param enable 是否开启回声消除
     * @return 开启/关闭回声消除的结果
     */
    @discardableResult
    public func enableAEC(with grade: SARtcType.AECGrade) -> Int32 {
        return rtcKit.setParameters("{\"rtc.audio.music_mode\": \(grade.rawValue)}")
    }
    
    //AIAEC-AI回声消除
    public func setAIAECOn(isOn:Bool){
        //agora_ai_echo_cancellation
//        rtcKit.enableExtension(withVendor: "agora_ai_echo_cancellation", extension: "", enabled: true)
        
        if (isOn){
            rtcKit.setParameters("{\"che.audio.aiaec.working_mode\":1}");

        } else {
            rtcKit.setParameters("{\"che.audio.aiaec.working_mode\":0}");

        }
    }

    //AGC-新增人声自动增益开关
    public func setAGCOn(isOn:Bool){
        if (isOn) {
            rtcKit.setParameters("{\"che.audio.agc.enable\":true}")
        } else {
            rtcKit.setParameters("{\"che.audio.agc.enable\":false}")
        }
        rtcKit.setParameters("{\"che.audio.agc.targetlevelBov\":3}")
        rtcKit.setParameters("{\"che.audio.agc.compressionGain\":18}")
    }
    
    /**
     * 开启/关闭 AI降噪
     * @param
     * @return 开启/关闭回声消除的结果
     */
    public func setAINS(with level: SARtcType.AINS_STATE) {
        switch level {
        case .high:
            rtcKit.setParameters("{\"che.audio.ains_mode\":2}")
            rtcKit.setParameters("{\"che.audio.nsng.lowerBound\":10}")
            rtcKit.setParameters("{\"che.audio.nsng.lowerMask\":10}")
            rtcKit.setParameters("{\"che.audio.nsng.statisticalbound\":0}")
            rtcKit.setParameters("{\"che.audio.nsng.finallowermask\":8}")
            rtcKit.setParameters("{\"che.audio.nsng.enhfactorstastical\":200}")
        case .mid:
            rtcKit.setParameters("{\"che.audio.ains_mode\":2}")
            rtcKit.setParameters("{\"che.audio.nsng.lowerBound\":80}")
            rtcKit.setParameters("{\"che.audio.nsng.lowerMask\":50}")
            rtcKit.setParameters("{\"che.audio.nsng.statisticalbound\":5}")
            rtcKit.setParameters("{\"che.audio.nsng.finallowermask\":30}")
            rtcKit.setParameters("{\"che.audio.nsng.enhfactorstastical\":200}")
        case .off:
            rtcKit.setParameters("{\"che.audio.ains_mode\":0}")
            rtcKit.setParameters("{\"che.audio.nsng.lowerBound\":80}")
            rtcKit.setParameters("{\"che.audio.nsng.lowerMask\":50}")
            rtcKit.setParameters("{\"che.audio.nsng.statisticalbound\":5}")
            rtcKit.setParameters("{\"che.audio.nsng.finallowermask\":30}")
            rtcKit.setParameters("{\"che.audio.nsng.enhfactorstastical\":200}")
        }
    }

    /**
     * 取消或恢复发布本地音频流
     * @param enable 是否发布本地音频流
     * @return 取消或恢复发布本地音频流的结果
     */
    @discardableResult
    public func muteLocalAudioStream(mute: Bool) -> Int32 {
        return rtcKit.muteLocalAudioStream(mute)
    }

    /**
     * 取消或恢复发布本地视频流
     * @param enable 是否发布本地视频流
     * @return 取消或恢复发布本地视频流的结果
     */
    @discardableResult
    public func muteLocalVideoStream(mute: Bool) -> Int32 {
        return rtcKit.muteLocalVideoStream(mute)
    }

    /**
     * 开启耳返
     * @param enable 是否开启耳返
     * @return 开启/关闭耳返的结果
     */
    @discardableResult
    public func enableinearmonitoring(enable: Bool) -> Int32 {
        return rtcKit.enable(inEarMonitoring: enable, includeAudioFilters: .builtInAudioFilters)
    }

    /**
     * 设置耳返音量
     * @param volume 耳返音量值
     * @return 设置耳返音量的结果
     */
    @discardableResult
    public func setInEarMonitoringVolume(with volume: Int) -> Int32 {
        return rtcKit.setInEarMonitoringVolume(volume)
    }

    /**
     * 设置用户本地采集音量
     * @param volume 音量值
     * @return 设置用户本地采集音量的结果
     */
    @discardableResult
    public func adjustRecordingSignalVolume(with volume: Int) -> Int32 {
        return rtcKit.adjustRecordingSignalVolume(volume)
    }

    /**
     * 设置本地播放的指定远端用户的音量
     * @param volume 音量值
     * @param uid 需要设置的用户的uid
     * @return 设置本地播放的指定远端用户的音量的结果
     */
    @discardableResult
    public func adjustUserPlaybackSignalVolume(with uid: UInt, volume: Int32) -> Int32 {
        return rtcKit.adjustUserPlaybackSignalVolume(uid, volume: volume)
    }

    /**
     * 设置美声
     * @param params 美声的参数配置
     * @return 设置美声的结果
     */
    @discardableResult
    public func setVoiceBeautifierParameters(with preset: AgoraVoiceBeautifierPreset) -> Int32 {
        return rtcKit.setVoiceBeautifierPreset(preset)
    }

    /**
     * 设置预设美声效果
     * @param preset 美声的参数配置
     * @param param1 歌声的性别特征：
        1: 男声
        2: 女声
     *  @param param2 歌声的混响效果：
        1: 歌声在小房间的混响效果。
        2: 歌声在大房间的混响效果。
        3: 歌声在大厅的混响效果。

     * @return 设置预设美声效果的结果
     */
    @discardableResult
    public func setVoiceBeautifierParameters(with preset: AgoraVoiceBeautifierPreset, param1: Int32, param2: Int32) -> Int32 {
        return rtcKit.setVoiceBeautifierParameters(.presetSingingBeautifier, param1: param1, param2: param2)
    }

    /**
     * 设置变声
     * @param params 变声的参数配置
     * @return 设置变声的结果
     */
    //   @discardableResult
//    public func setLocalVoiceChanger(with voiceChanger: AgoraAudioVoiceChanger) -> Int32 {
//        return rtcKit.setLocalVoiceChanger(voiceChanger)
//    }

    /**
     * 设置本地视频视图
     * @param local 本地canvas的参数配置
     * @return 设置本地视频视图的结果
     */
    @discardableResult
    public func setupLocalVideo(local: AgoraRtcVideoCanvas?) -> Int32 {
        return rtcKit.setupLocalVideo(local ?? AgoraRtcVideoCanvas())
    }

    /**
     * 设置远端视频视图
     * @param remote 远端canvas的参数配置
     * @return 设置远端视频视图的结果
     */
    @discardableResult
    public func setupRemoteVideo(remote: AgoraRtcVideoCanvas?) -> Int32 {
        return rtcKit.setupRemoteVideo(remote ?? AgoraRtcVideoCanvas())
    }

    /**
     * 发送dataStream消息
     * @param data 发送的data
     * @return 发送dataStream消息的结果
     */
    @discardableResult
    @objc public func sendStreamMessage(with data: Data) -> Int32 {
        return rtcKit.sendStreamMessage(streamId, data: data)
    }

    /**
     * 打开音乐
     * @param url 音乐的本地或者线上地址
     * @param startPos 音乐从哪里开始播放 毫秒
     * @return 打开音乐的结果
     */
    @discardableResult
    @objc public func open(with url: String, startPos: Int) -> Int32 {
        mediaPlayer?.setLoopCount(-1)
        return mediaPlayer?.open(url, startPos: startPos) ?? -1
    }

    /**
     * 播放音乐
     * @return 播放音乐的结果
     */
    @discardableResult
    @objc public func play() -> Int32 {
        return mediaPlayer?.play() ?? -1
    }

    /**
     * 暂停播放
     * @return 暂停播放的结果
     */
    @discardableResult
    @objc public func pause() -> Int32 {
        return mediaPlayer?.pause() ?? -1
    }

    /**
     * 停止播放
     * @return 停止播放的结果
     */
    @discardableResult
    @objc public func stop() -> Int32 {
        return mediaPlayer?.stop() ?? -1
    }

    /**
     * 设置音乐声道
     * @return 设置音乐声道的结果
     */
    @discardableResult
    @objc public func setAudioDualMonoMode(with mode: AgoraAudioDualMonoMode) -> Int32 {
        return Int32((mediaPlayer?.setAudioDualMonoMode(mode))!)
    }

    /**
     * 老师，学生设置伴奏音量
     * @param volume 伴奏音量值
     * @return 设置伴奏音量的结果
     */
    @discardableResult
    public func adjustPlayoutVolume(with volume: Int32) -> Int32 {
        return mediaPlayer?.adjustPlayoutVolume(volume) ?? -1
    }

    @discardableResult
    public func adjustAudioMixingVolume(with volume: Int) -> Int32 {
        return rtcKit.adjustAudioMixingVolume(volume)
    }

    /**
     * 获取MPK的播放状态
     * @return MPK的播放状态的结果
     */
    @discardableResult
    public func getPlayerState() -> AgoraMediaPlayerState {
        return mediaPlayer?.getPlayerState() ?? .failed
    }

    /**
     * 获取播放进度
     * @return 获取播放进度的结果
     */
    @discardableResult
    public func getPosition() -> Int {
        return mediaPlayer?.getPosition() ?? 0
    }

    /**
     * 获取歌曲总时长
     * @return 获取歌曲总时长的结果
     */
    @discardableResult
    public func getDuration() -> Int {
        return mediaPlayer?.getDuration() ?? 0
    }

    /**
     * 设置歌曲播放进度
     * @return 设置歌曲播放进度的结果
     */
    @discardableResult
    public func seek(to position: Int) -> Int32 {
        return (mediaPlayer?.seek(toPosition: position))!
    }

    /**
     * 离开频道，释放资源
     */
    @objc public func leaveChannel() {
        rtcKit.stopPreview()
        rtcKit.leaveChannel(nil)
        rtcKit.delegate = nil
        AgoraLocalSpatialAudioKit.destroy()
        AgoraRtcEngineKit.destroy()
        SARTCManager._sharedInstance = nil // 释放单例
    }
}

// MARK: - AgoraRtcEngineDelegate

extension SARTCManager: AgoraRtcEngineDelegate {
    // remote joined
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        if role == .coHost && type == .KTV && uid == kMPK_RTC_UID_SA {
            _ = rtcKit.muteRemoteAudioStream(kMPK_RTC_UID_SA, mute: true)
        }

        guard let _ = delegate else {
            return
        }

        if uid == kMPK_RTC_UID_SA {
            return
        }

        delegate?.didRtcRemoteUserJoinedOfUid?(uid: uid)
    }

    // remote offline
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        guard let _ = delegate else {
            return
        }

        delegate?.didRtcUserOfflineOfUid?(uid: uid)
    }

    // local joined
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        localRtcUid = uid

        guard let _ = delegate else {
            return
        }

        if uid == kMPK_RTC_UID_SA {
            return
        }

        delegate?.didRtcLocalUserJoinedOfUid?(uid: uid)
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        guard let _ = delegate else {
            return
        }
        delegate?.didOccurError?(with: errorCode)
    }

    // dataStream received
    public func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        playerDelegate?.didReceiveStreamMsgOfUid?(uid: uid, data: data)
    }

    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
        guard let _ = delegate else {
            return
        }
        // 如果Uid = 0，表示是本地用户的声音
        var real_speakers: [AgoraRtcAudioVolumeInfo] = speakers
        for (index, value) in speakers.enumerated() {
            if value.uid == kMPK_RTC_UID_SA {
                real_speakers.remove(at: index)
            }

            if value.uid == 0 {
                real_speakers[index].uid = localRtcUid
            }
        }
        delegate?.reportAudioVolumeIndicationOfSpeakers?(speakers: real_speakers)
    }
}

// MARK: - AgoraRtcMediaPlayerDelegate

extension SARTCManager: AgoraRtcMediaPlayerDelegate {
    public func rtcEngine(_ engine: AgoraRtcEngineKit, audioMixingStateChanged state: AgoraAudioMixingStateType, reasonCode: AgoraAudioMixingReasonCode) {
        if state == .stopped {
            guard let musicType = musicType else { return }
            var count = 0
            switch musicType {
            case .alien:
                count = SAConfig.baseAlienMic.count
            case .ainsHigh:
                count = SAConfig.HighAINSIntroduc.count
            case .ainsMid:
                count = SAConfig.MediumAINSIntroduc.count
            case .ainsOff:
                count = SAConfig.NoneAINSIntroduc.count
            case .sound:
                delegate?.reportAlien?(with: .none, musicType: .sound)
                return
            case .social:
                count = SAConfig.SoundSelectSocial.count
            case .ktv:
                count = SAConfig.SoundSelectKTV.count
            case .game:
                count = SAConfig.SoundSelectGame.count
            case .anchor:
                count = SAConfig.SoundSelectAnchor.count
            case .Spatical:
                count = SAConfig.spatialAlienMic.count
            }
            if baseMusicCount < count {
                baseMusicCount += 1
            }
        }
    }

    // mpk didChangedToPosition
    public func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedToPosition position: Int) {
        guard let _ = delegate else {
            return
        }

        playerDelegate?.didMPKChangedToPosition?(position: position)
    }

    // mpk didChangedTo
    public func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        if state == .openCompleted {
            playerKit.play()
        } else if (state == .playBackAllLoopsCompleted || state == .playBackCompleted)  {
            playerKit.stop()
            let count = SAConfig.spatialAlienMic.count
            if baseMusicCount < count {
                baseMusicCount += 1
            }
        }

    }
}
