//
//  VoiceRoomRTCManager.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/5.
//

import AgoraRtcKit
import Foundation

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
 * 耳返模式
 *
 */
public enum INEAR_MODE: Int {
    case auto = 0
    case opneSL
    case oboe
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
}

/**
 * 场景枚举
 * VoiceChat 语聊房2.0
 * KTV KTV场景
 */
@objc public enum ASManagerType: Int {
    case VoiceChat = 1
    case KTV = 2
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

// MARK: - VMMusicPlayerDelegate

@objc public protocol VMMusicPlayerDelegate: NSObjectProtocol {
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
    @objc optional func didMPKChangedTo(state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) -> Void // MPK 状态回调
}

// MARK: - VMManagerDelegate

@objc public protocol VMManagerDelegate: NSObjectProtocol {
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
    @objc optional func reportAlien(with type: ALIEN_TYPE, musicType: VMMUSIC_TYPE) -> Void
    
    /**
     * 加载歌曲
     * @param songCode code
     * @param progress 进度
     * @param status 状态
     */
    @objc optional func downloadBackgroundMusicStatus(songCode: Int, progress: Int, status: AgoraMusicContentCenterPreloadStatus)
}

public let kMPK_RTC_UID: UInt = 1
@objc public class VoiceRoomRTCManager: NSObject {
    // init manager
    private static var _sharedInstance: VoiceRoomRTCManager?

    private var mediaPlayer: AgoraRtcMediaPlayerProtocol?

    private var role: ASRoleType = .audience

    private var type: ASManagerType = .VoiceChat

    private var channelName: String?

    private var streamId: Int = -1

    fileprivate var localRtcUid: UInt = 0

    private var musicType: VMMUSIC_TYPE?
    
    private var mcc: AgoraMusicContentCenter?
    private var musicPlayer: AgoraMusicPlayerProtocol?
    typealias MusicListCallback = ([AgoraMusic])->()
    private var onMusicChartsIdCache: [String: MusicListCallback] = [:]
    private var lastSongCode: Int = 0
    var backgroundMusics: [AgoraMusic] = []
    
    @objc public weak var delegate: VMManagerDelegate?

    @objc public weak var playerDelegate: VMMusicPlayerDelegate?
    
    var stopMixingClosure: (() -> ())?
    var downloadBackgroundMusicStatusClosure: ((_ songCode: Int, _ progress: Int, _ status: AgoraMusicContentCenterPreloadStatus) -> Void)?
    var backgroundMusicPlayingStatusClosure: ((_ state: AgoraMediaPlayerState) -> Void)?

    // 单例
    @objc public class func getSharedInstance() -> VoiceRoomRTCManager {
        guard let instance = _sharedInstance else {
            _sharedInstance = VoiceRoomRTCManager()
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
                count = AgoraConfig.baseAlienMic.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = "\(AgoraConfig.CreateCommonRoom)\(AgoraConfig.baseAlienMic[musicIndex])"
            case .ainsHigh:
                count = AgoraConfig.HighAINSIntroduc.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = "\(AgoraConfig.SetAINSIntroduce)\(AgoraConfig.HighAINSIntroduc[musicIndex])"
            case .ainsMid:
                count = AgoraConfig.MediumAINSIntroduc.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = "\(AgoraConfig.SetAINSIntroduce)\(AgoraConfig.MediumAINSIntroduc[musicIndex])"
            case .ainsOff:
                count = AgoraConfig.NoneAINSIntroduc.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = "\(AgoraConfig.SetAINSIntroduce)\(AgoraConfig.NoneAINSIntroduc[musicIndex])"
            case .social:
                count = AgoraConfig.SoundSelectSocial.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = AgoraConfig.SoundSelectSocial[musicIndex]
            case .ktv:
                count = AgoraConfig.SoundSelectKTV.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = AgoraConfig.SoundSelectKTV[musicIndex]
            case .game:
                count = AgoraConfig.SoundSelectGame.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = AgoraConfig.SoundSelectGame[musicIndex]
            case .anchor:
                count = AgoraConfig.SoundSelectAnchor.count
                musicIndex = baseMusicCount < count ? baseMusicCount : count - 1
                musicPath = AgoraConfig.SoundSelectAnchor[musicIndex]
            case .sound:
                return
            }
            if baseMusicCount >= count {
                rtcKit.stopAudioMixing()
                delegate?.reportAlien?(with: .ended, musicType: musicType)
            } else {
                if musicPath.contains("-B-") {
                    delegate?.reportAlien?(with: .blue, musicType: musicType)
                } else if musicPath.contains("-R-") {
                    delegate?.reportAlien?(with: .red, musicType: musicType)
                } else if musicPath.contains("-B&R-") {
                    delegate?.reportAlien?(with: .blueAndRed, musicType: musicType)
                }
//                let lanuagePath = LanguageManager.shared.currentLocal.identifier.hasPrefix("zh") ? "voice_lau".voice_localized() : "EN"
//                musicPath = musicPath.replacingOccurrences(of: "CN", with: lanuagePath)
                rtcKit.startAudioMixing(musicPath, loopback: false, cycle: 1)
            }
        }
    }

    // init rtc
    let rtcKit: AgoraRtcEngineKit = AgoraRtcEngineKit.sharedEngine(withAppId: KeyCenter.AppId, delegate: nil)

    /**
     * 设置RTC角色
     * @param role RMCRoleType
     */
    @objc public func setClientRole(role: ASRoleType) {
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
    public func joinVoicRoomWith(with channelName: String,token: String?, rtcUid: Int?, type: VMMUSIC_TYPE) -> Int32 {
        self.type = .VoiceChat
        rtcKit.delegate = self
        rtcKit.enableAudioVolumeIndication(200, smooth: 3, reportVad: true)
        self.setParametersWithMD()
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
        rtcKit.setParameters("{\"che.audio.input_sample_rate\":48000}")
        rtcKit.setEnableSpeakerphone(true)
        rtcKit.setDefaultAudioRouteToSpeakerphone(true)
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.publishMicrophoneTrack = role != .audience
        mediaOption.publishCameraTrack = false
        mediaOption.autoSubscribeAudio = true
        mediaOption.autoSubscribeVideo = false
        mediaOption.clientRoleType = role == .audience ? .audience : .broadcaster
        return rtcKit.joinChannel(byToken: token, channelId: channelName, uid: UInt(rtcUid ?? 0), mediaOptions: mediaOption)
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
    
    public func setParameters(with string: String) {
        rtcKit.setParameters(string)
    }
    
    //Dump 全链路音频数据收集
    public func setAPMOn(isOn: Bool){
        rtcKit.setParameters("{\"rtc.debug.enable\": \(isOn)}")
        rtcKit.setParameters("{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}");
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
    
    public func initMusicControlCenter() {
        let contentCenterConfiguration = AgoraMusicContentCenterConfig()
        contentCenterConfiguration.appId = KeyCenter.AppId
        contentCenterConfiguration.mccUid = Int(VLUserCenter.user.id) ?? 0
        contentCenterConfiguration.token = VLUserCenter.user.agoraRTMToken
        contentCenterConfiguration.rtcEngine = rtcKit
        
        mcc = AgoraMusicContentCenter.sharedContentCenter(config: contentCenterConfiguration)
        mcc?.register(self)
        
        musicPlayer = mcc?.createMusicPlayer(delegate: self)

        musicPlayer?.adjustPlayoutVolume(50)
        musicPlayer?.adjustPublishSignalVolume(50)
    }
    
    func fetchMusicList(musicListCallback: @escaping MusicListCallback) {
        if mcc == nil {
            initMusicControlCenter()
        }
        let jsonOption = "{\"pitchType\":1,\"needLyric\":false}"
        let requestId = mcc?.getMusicCollection(musicChartId: 3, page: 0, pageSize: 20, jsonOption: jsonOption)
        onMusicChartsIdCache[requestId ?? ""] = musicListCallback
    }
    
    func playMusic(songCode: Int, startPos: Int = 0) {
        if musicPlayer?.getPlayerState() == .paused  {
            musicPlayer?.resume()
        } else if musicPlayer?.getPlayerState() == .playing {
            musicPlayer?.pause()
        } else {
            mediaPlayer?.pause()
            musicPlayer?.stop()
            let mediaOption = AgoraRtcChannelMediaOptions()
            mediaOption.publishMediaPlayerId = Int(musicPlayer?.getMediaPlayerId() ?? 0)
            mediaOption.publishMediaPlayerAudioTrack = true
            rtcKit.updateChannel(with: mediaOption)
            lastSongCode = songCode
            if let mcc = mcc, mcc.isPreloaded(songCode: songCode) != 0 {
                mcc.preload(songCode: songCode)
            } else {
                musicPlayer?.openMedia(songCode: songCode, startPos: startPos)
                downloadBackgroundMusicStatusClosure?(songCode, 100, .OK)
            }
        }
    }

    /**
     * 停止播放歌曲
     */
    @objc public func stopMusic() {
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.publishMediaPlayerAudioTrack = false
        rtcKit.updateChannel(with: mediaOption)
        if musicPlayer?.getPlayerState() != .stopped {
            musicPlayer?.stop()
        }
    }
    
    /**
     * 恢复播放
     */
    public func resumeMusic() {
        if musicPlayer?.getPlayerState() == .paused {
            musicPlayer?.resume()
        } else {
            musicPlayer?.play()
        }
    }

    /**
     * 暂停播放
     */
    public func pauseMusic() {
        musicPlayer?.pause()
    }

    /**
     * 调整进度
     */
    public func seekMusic(time: NSInteger) {
       musicPlayer?.seek(toPosition: time)
        
    }
    
    /**
     * 调整音量
     */
    public func adjustMusicVolume(volume: Int) {
        musicPlayer?.adjustPlayoutVolume(Int32(volume))
        musicPlayer?.adjustPublishSignalVolume(Int32(volume))
    }

    /**
     * 选择音轨，原唱、伴唱
     */
    public func selectPlayerTrackMode(isOrigin: Bool) {
        musicPlayer?.selectAudioTrack(isOrigin ? 1: 0)
    }

    /**
     *
     *
     */
    public func playMusic(with type: VMMUSIC_TYPE) {
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
            baseMusicCount = AgoraConfig.baseAlienMic.count
        } else if musicType == .ainsHigh {
            baseMusicCount = AgoraConfig.HighAINSIntroduc.count
        } else if musicType == .ainsMid {
            baseMusicCount = AgoraConfig.MediumAINSIntroduc.count
        } else if musicType == .ainsOff {
            baseMusicCount = AgoraConfig.NoneAINSIntroduc.count
        } else if musicType == .social {
            baseMusicCount = AgoraConfig.SoundSelectSocial.count
        } else if musicType == .ktv {
            baseMusicCount = AgoraConfig.SoundSelectKTV.count
        } else if musicType == .game {
            baseMusicCount = AgoraConfig.SoundSelectGame.count
        } else if musicType == .anchor {
            baseMusicCount = AgoraConfig.SoundSelectAnchor.count
        }
    }

    public func playSound(with index: Int, type: VMMUSIC_TYPE) {
        stopPlayMusic()
        musicType = type
        var path = ""
        if type == .ainsHigh {
            path = AgoraConfig.HighSound[index]
        } else if type == .ainsOff {
            path = AgoraConfig.NoneSound[index]
        }
//        let lanuagePath = LanguageManager.shared.currentLocal.identifier.hasPrefix("zh") ? "voice_lau".voice_localized() : "EN"
//        path = path.replacingOccurrences(of: "CN", with: lanuagePath)
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
    public func enableAEC(with grade: AECGrade) -> Int32 {
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
    public func setAINS(with level: AINS_STATE) {
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
            rtcKit.setParameters("{\"che.audio.ains_mode\":-1}")
            rtcKit.setParameters("{\"che.audio.nsng.lowerBound\":80}")
            rtcKit.setParameters("{\"che.audio.nsng.lowerMask\":50}")
            rtcKit.setParameters("{\"che.audio.nsng.statisticalbound\":5}")
            rtcKit.setParameters("{\"che.audio.nsng.finallowermask\":30}")
            rtcKit.setParameters("{\"che.audio.nsng.enhfactorstastical\":200}")
        }
    }

    /**
     * 开启/关闭 耳返
     * @param
     * @return 开启/关闭耳返的结果
     */
    public func setInEarMode(with mode: INEAR_MODE) {
        switch mode {
        case .auto:
            rtcKit.setParameters("{\"opensl che.audio.opensl.mode\": 0}")
            rtcKit.setParameters("{\"oboe che.audio.oboe.enable\": 0}")
        case .opneSL:
            rtcKit.setParameters("{\"opensl che.audio.opensl.mode\": 1}")
        case .oboe:
            rtcKit.setParameters("{\"oboe che.audio.oboe.enable\": 1}")
        }
    }
    
    /**
     * 开启/关闭 本地视频
     * @param enable 是否开启视频
     * @return 开启/关闭视频的结果
     */
    @discardableResult
    public func enableLocalVideo(enable: Bool) -> Int32 {
        return rtcKit.enableLocalVideo(enable)
    }

    /**
     * 取消或恢复发布本地音频流
     * @param enable 是否发布本地音频流
     * @return 取消或恢复发布本地音频流的结果
     */
    @discardableResult
    public func muteLocalAudioStream(mute: Bool) -> Int32 {
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.publishMicrophoneTrack = !mute
        return rtcKit.updateChannel(with: mediaOption)
    }

    /**
     * 取消或恢复发布本地视频流
     * @param enable 是否发布本地视频流
     * @return 取消或恢复发布本地视频流的结果
     */
    @discardableResult
    public func muteLocalVideoStream(mute: Bool) -> Int32 {
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.publishCameraTrack = !mute
        return rtcKit.updateChannel(with: mediaOption)
    }

    /**
     * 开启耳返
     * @param enable 是否开启耳返
     * @return 开启/关闭耳返的结果
     */
    @discardableResult
    public func enableinearmonitoring(enable: Bool) -> Int32 {
        return rtcKit.enable(inEarMonitoring: enable, includeAudioFilters: .noiseSuppression)
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
        musicPlayer?.pause()
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
        selectPlayerTrackMode(isOrigin: true)
        musicPlayer?.stop()
        musicPlayer = nil
        mcc = nil
        if !backgroundMusics.isEmpty {
            backgroundMusics.removeAll()
        }
        AgoraMusicContentCenter.destroy()
        AgoraRtcEngineKit.destroy()
        VoiceRoomRTCManager._sharedInstance = nil // 释放单例
    }
}

// MARK: - AgoraRtcEngineDelegate

extension VoiceRoomRTCManager: AgoraRtcEngineDelegate {
    // remote joined
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        if role == .coHost && type == .KTV && uid == kMPK_RTC_UID {
            _ = rtcKit.muteRemoteAudioStream(kMPK_RTC_UID, mute: true)
        }

        guard let _ = delegate else {
            return
        }

        if uid == kMPK_RTC_UID {
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

        if uid == kMPK_RTC_UID {
            return
        }

        delegate?.didRtcLocalUserJoinedOfUid?(uid: uid)
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        
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
            if value.uid == kMPK_RTC_UID {
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

extension VoiceRoomRTCManager: AgoraRtcMediaPlayerDelegate {
    public func rtcEngine(_ engine: AgoraRtcEngineKit, audioMixingStateChanged state: AgoraAudioMixingStateType, reasonCode: AgoraAudioMixingReasonCode) {
        if state == .stopped {
            if self.stopMixingClosure != nil {
                self.stopMixingClosure!()
            }
            guard let musicType = musicType else { return }
            var count = 0
            switch musicType {
            case .alien:
                count = AgoraConfig.baseAlienMic.count
            case .ainsHigh:
                count = AgoraConfig.HighAINSIntroduc.count
            case .ainsMid:
                count = AgoraConfig.MediumAINSIntroduc.count
            case .ainsOff:
                count = AgoraConfig.NoneAINSIntroduc.count
            case .sound:
                delegate?.reportAlien?(with: .none, musicType: .sound)
                return
            case .social:
                count = AgoraConfig.SoundSelectSocial.count
            case .ktv:
                count = AgoraConfig.SoundSelectKTV.count
            case .game:
                count = AgoraConfig.SoundSelectGame.count
            case .anchor:
                count = AgoraConfig.SoundSelectAnchor.count
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
        if delegate != nil {
            playerDelegate?.didMPKChangedTo?(state: state, error: error)
        }
        if let musicPlayer = musicPlayer, state == .openCompleted {
            musicPlayer.play()
        }
        backgroundMusicPlayingStatusClosure?(state)
    }
}

extension VoiceRoomRTCManager: AgoraMusicContentCenterEventDelegate {
    public func onMusicChartsResult(_ requestId: String, result: [AgoraMusicChartInfo], errorCode: AgoraMusicContentCenterStatusCode) {
        print("songCode == \(result)")
    }
    
    public func onMusicCollectionResult(_ requestId: String, result: AgoraMusicCollection, errorCode: AgoraMusicContentCenterStatusCode) {
        guard let callback = onMusicChartsIdCache[requestId] else { return }
        backgroundMusics = result.musicList
        DispatchQueue.main.async(execute: {
            callback(result.musicList)
        })
    }
    
    public func onLyricResult(_ requestId: String, songCode: Int, lyricUrl: String?, errorCode: AgoraMusicContentCenterStatusCode) {
        print("songCode == \(songCode)")
    }
    
    public func onSongSimpleInfoResult(_ requestId: String, songCode: Int, simpleInfo: String?, errorCode: AgoraMusicContentCenterStatusCode) {
        print("songCode == \(songCode)")
    }
    
    public func onPreLoadEvent(_ requestId: String, songCode: Int, percent: Int, lyricUrl: String?, status: AgoraMusicContentCenterPreloadStatus, errorCode: AgoraMusicContentCenterStatusCode) {
        delegate?.downloadBackgroundMusicStatus?(songCode: songCode, progress: percent, status: status)
        downloadBackgroundMusicStatusClosure?(songCode, percent, status)
        if status == .OK, lastSongCode == songCode {
            musicPlayer?.openMedia(songCode: songCode, startPos: 0)
        }
    }
}
