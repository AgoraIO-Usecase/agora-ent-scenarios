//
//  VMRTCKit.swift
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

@objc public protocol ASMusicPlayerDelegate: NSObjectProtocol {
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

@objc public protocol ASManagerDelegate: NSObjectProtocol {
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
}

public let kMPK_RTC_UID: UInt = 1
@objc public class ASRTCKit: NSObject {
    // init manager
    private static var _sharedInstance: ASRTCKit?

    private var mediaPlayer: AgoraRtcMediaPlayerProtocol?

    private var role: ASRoleType = .audience

    private var type: ASManagerType = .VoiceChat

    private var channelName: String?

    private var streamId: Int = -1

    fileprivate var localRtcUid: UInt = 0

    private var musicType: VMMUSIC_TYPE?

    @objc public weak var delegate: ASManagerDelegate?

    @objc public weak var playerDelegate: ASMusicPlayerDelegate?

    // 单例
    @objc public class func getSharedInstance() -> ASRTCKit {
        guard let instance = _sharedInstance else {
            _sharedInstance = ASRTCKit()
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
            switch musicType {
            case .alien:
                count = AgoraConfig.baseAlienMic.count
            case .ainsHigh:
                count = AgoraConfig.HighAINSIntroduc.count
            case .ainsMid:
                count = AgoraConfig.MediumAINSIntroduc.count
            case .ainsOff:
                count = AgoraConfig.NoneAINSIntroduc.count
            case .social:
                count = AgoraConfig.SoundSelectSocial.count
            case .ktv:
                count = AgoraConfig.SoundSelectKTV.count
            case .game:
                count = AgoraConfig.SoundSelectGame.count
            case .anchor:
                count = AgoraConfig.SoundSelectAnchor.count
            case .sound:
                return
            }
            if baseMusicCount >= count {
                rtcKit.stopAudioMixing()
                delegate?.reportAlien?(with: .ended, musicType: musicType)
            } else {
                if musicType == .alien {
                    if AgoraConfig.baseAlienMic[baseMusicCount].contains("-B-") {
                        delegate?.reportAlien?(with: .blue, musicType: musicType)
                    } else if AgoraConfig.baseAlienMic[baseMusicCount].contains("-R-") {
                        delegate?.reportAlien?(with: .red, musicType: musicType)
                    } else if AgoraConfig.baseAlienMic[baseMusicCount].contains("-B&R-") {
                        delegate?.reportAlien?(with: .blueAndRed, musicType: musicType)
                    }
                    var musicStr = "\(AgoraConfig.CreateCommonRoom)\(AgoraConfig.baseAlienMic[baseMusicCount])"
                    musicStr = musicStr.replacingOccurrences(of: "CN", with: LanguageManager.localValue(key: "Lau"))
                    rtcKit.startAudioMixing(musicStr, loopback: false, cycle: 1)
                } else if musicType == .ainsHigh {
                    if AgoraConfig.HighAINSIntroduc[baseMusicCount].contains("-B-") {
                        delegate?.reportAlien?(with: .blue, musicType: musicType)
                    } else if AgoraConfig.HighAINSIntroduc[baseMusicCount].contains("-R-") {
                        delegate?.reportAlien?(with: .red, musicType: musicType)
                    } else if AgoraConfig.HighAINSIntroduc[baseMusicCount].contains("-B&R-") {
                        delegate?.reportAlien?(with: .blueAndRed, musicType: musicType)
                    }
                    print("-----\(AgoraConfig.SetAINSIntroduce)\(AgoraConfig.HighAINSIntroduc[baseMusicCount])")
                    var musicStr = "\(AgoraConfig.SetAINSIntroduce)\(AgoraConfig.HighAINSIntroduc[baseMusicCount])"
                    musicStr = musicStr.replacingOccurrences(of: "CN", with: LanguageManager.localValue(key: "Lau"))
                    rtcKit.startAudioMixing(musicStr, loopback: false, cycle: 1)
                } else if musicType == .ainsMid {
                    if AgoraConfig.MediumAINSIntroduc[baseMusicCount].contains("-B-") {
                        delegate?.reportAlien?(with: .blue, musicType: musicType)
                    } else if AgoraConfig.MediumAINSIntroduc[baseMusicCount].contains("-R-") {
                        delegate?.reportAlien?(with: .red, musicType: musicType)
                    } else if AgoraConfig.MediumAINSIntroduc[baseMusicCount].contains("-B&R-") {
                        delegate?.reportAlien?(with: .blueAndRed, musicType: musicType)
                    }
                    var musicStr = "\(AgoraConfig.SetAINSIntroduce)\(AgoraConfig.MediumAINSIntroduc[baseMusicCount])"
                    musicStr = musicStr.replacingOccurrences(of: "CN", with: LanguageManager.localValue(key: "Lau"))
                    rtcKit.startAudioMixing(musicStr, loopback: false, cycle: 1)
                } else if musicType == .ainsOff {
                    if AgoraConfig.NoneAINSIntroduc[baseMusicCount].contains("-B-") {
                        delegate?.reportAlien?(with: .blue, musicType: musicType)
                    } else if AgoraConfig.NoneAINSIntroduc[baseMusicCount].contains("-R-") {
                        delegate?.reportAlien?(with: .red, musicType: musicType)
                    } else if AgoraConfig.NoneAINSIntroduc[baseMusicCount].contains("-B&R-") {
                        delegate?.reportAlien?(with: .blueAndRed, musicType: musicType)
                    }
                    var musicStr = "\(AgoraConfig.SetAINSIntroduce)\(AgoraConfig.NoneAINSIntroduc[baseMusicCount])"
                    musicStr = musicStr.replacingOccurrences(of: "CN", with: LanguageManager.localValue(key: "Lau"))
                    rtcKit.startAudioMixing(musicStr, loopback: false, cycle: 1)
                } else if musicType == .social {
                    if AgoraConfig.SoundSelectSocial[baseMusicCount].contains("-B-") {
                        delegate?.reportAlien?(with: .blue, musicType: musicType)
                    } else if AgoraConfig.SoundSelectSocial[baseMusicCount].contains("-R-") {
                        delegate?.reportAlien?(with: .red, musicType: musicType)
                    } else if AgoraConfig.SoundSelectSocial[baseMusicCount].contains("-B&R-") {
                        delegate?.reportAlien?(with: .blueAndRed, musicType: musicType)
                    }
                    var musicStr = "\(AgoraConfig.SoundSelectSocial[baseMusicCount])"
                    musicStr = musicStr.replacingOccurrences(of: "CN", with: LanguageManager.localValue(key: "Lau"))
                    rtcKit.startAudioMixing(musicStr, loopback: false, cycle: 1)
                } else if musicType == .ktv {
                    if AgoraConfig.SoundSelectKTV[baseMusicCount].contains("-B-") {
                        delegate?.reportAlien?(with: .blue, musicType: musicType)
                    } else if AgoraConfig.SoundSelectKTV[baseMusicCount].contains("-R-") {
                        delegate?.reportAlien?(with: .red, musicType: musicType)
                    } else if AgoraConfig.SoundSelectKTV[baseMusicCount].contains("-B&R-") {
                        delegate?.reportAlien?(with: .blueAndRed, musicType: musicType)
                    }
                    var musicStr = "\(AgoraConfig.SoundSelectKTV[baseMusicCount])"
                    musicStr = musicStr.replacingOccurrences(of: "CN", with: LanguageManager.localValue(key: "Lau"))
                    rtcKit.startAudioMixing(musicStr, loopback: false, cycle: 1)
                } else if musicType == .game {
                    if AgoraConfig.SoundSelectGame[baseMusicCount].contains("-B-") {
                        delegate?.reportAlien?(with: .blue, musicType: musicType)
                    } else if AgoraConfig.SoundSelectGame[baseMusicCount].contains("-R-") {
                        delegate?.reportAlien?(with: .red, musicType: musicType)
                    } else if AgoraConfig.SoundSelectGame[baseMusicCount].contains("-B&R-") {
                        delegate?.reportAlien?(with: .blueAndRed, musicType: musicType)
                    }
                    var musicStr = "\(AgoraConfig.SoundSelectGame[baseMusicCount])"
                    musicStr = musicStr.replacingOccurrences(of: "CN", with: LanguageManager.localValue(key: "Lau"))
                    rtcKit.startAudioMixing(musicStr, loopback: false, cycle: 1)
                } else if musicType == .anchor {
                    if AgoraConfig.SoundSelectAnchor[baseMusicCount].contains("-B-") {
                        delegate?.reportAlien?(with: .blue, musicType: musicType)
                    } else if AgoraConfig.SoundSelectAnchor[baseMusicCount].contains("-R-") {
                        delegate?.reportAlien?(with: .red, musicType: musicType)
                    } else if AgoraConfig.SoundSelectAnchor[baseMusicCount].contains("-B&R-") {
                        delegate?.reportAlien?(with: .blueAndRed, musicType: musicType)
                    }
                    var musicStr = "\(AgoraConfig.SoundSelectAnchor[baseMusicCount])"
                    musicStr = musicStr.replacingOccurrences(of: "CN", with: LanguageManager.localValue(key: "Lau"))
                    rtcKit.startAudioMixing(musicStr, loopback: false, cycle: 1)
                }
            }
        }
    }

    // init rtc
    private let rtcKit: AgoraRtcEngineKit = AgoraRtcEngineKit.sharedEngine(withAppId: AgoraConfig.rtcId, delegate: nil)

    /**
     * 设置RTC角色
     * @param role RMCRoleType
     */
    @objc public func setClientRole(role: ASRoleType) {
        rtcKit.setClientRole(role == .audience ? .audience : .broadcaster)
        self.role = role
    }

    /**
     * 加入实时KTV频道
     * @param channelName 频道名称
     * @param rtcUid RTCUid 如果传0，大网会自动分配
     * @param rtmUid 可选，如果不使用RTM，使用自己的IM，这个值不用传
     */
    @objc public func joinKTVChannelWith(with channelName: String, rtcUid: Int) {
        self.channelName = channelName
        type = .KTV

        loadKit(with: channelName, rtcUid: rtcUid)

        // Support dynamic setting in the channel and real-time chorus scene
        rtcKit.setParameters("{\"rtc.audio_resend\":false}")
        rtcKit.setParameters("{\"rtc.audio_fec\":[3,2]}")
        rtcKit.setParameters("{\"rtc.audio.aec_length\":50}")
        rtcKit.setAudioProfile(.musicHighQualityStereo, scenario: .chorus)
        rtcKit.enableAudioVolumeIndication(200, smooth: 3, reportVad: false)

        let config = AgoraVideoEncoderConfiguration(width: 120, height: 160, frameRate: .fps7, bitrate: AgoraVideoBitrateStandard, orientationMode: .adaptative, mirrorMode: .auto)
        rtcKit.setVideoEncoderConfiguration(config)

        if role != .audience {
            mediaPlayer = rtcKit.createMediaPlayer(with: self)

            if streamId == -1 {
                let config = AgoraDataStreamConfig()
                config.ordered = false
                config.syncWithAudio = false
                rtcKit.createDataStream(&streamId, config: config)
                if streamId == -1 {
                    return
                }
            }
        }

        if role == .owner {
            let option = AgoraRtcChannelMediaOptions()
            option.publishCameraTrack = true
            option.publishMicrophoneTrack = true
            option.publishCustomAudioTrack = false
            option.autoSubscribeAudio = false
            option.autoSubscribeVideo = false
            option.clientRoleType = .broadcaster
            rtcKit.setAudioProfile(.musicHighQuality, scenario: .chorus)
            rtcKit.joinChannel(byToken: nil, channelId: channelName, uid: UInt(rtcUid), mediaOptions: option)

            let connection = AgoraRtcConnection()
            connection.channelId = channelName
            connection.localUid = kMPK_RTC_UID

            let option2 = AgoraRtcChannelMediaOptions()
            option2.publishCameraTrack = false // 取消发送视频流
            option2.publishMicrophoneTrack = false // 取消SDK采集音频
            option2.autoSubscribeAudio = false // 取消订阅其他人的音频流
            option2.publishCustomAudioTrack = false // 开启音频自采集，如果使用SDK采集，传入false。

            option2.enableAudioRecordingOrPlayout = false
            option2.publishMediaPlayerAudioTrack = true
            option2.publishMediaPlayerId = Int(mediaPlayer!.getMediaPlayerId())
            option2.clientRoleType = .broadcaster // 设置角色为主播

            rtcKit.joinChannelEx(byToken: nil, connection: connection, delegate: nil, mediaOptions: option2) { [weak self] channel_name, user_uid, elapsed in
                self?.rtcKit.muteRemoteAudioStream(kMPK_RTC_UID, mute: true)
            }

        } else if role == .coHost {
            let option = AgoraRtcChannelMediaOptions()
            option.publishCameraTrack = true
            option.publishMicrophoneTrack = true
            option.publishCustomAudioTrack = false
            option.autoSubscribeAudio = true
            option.autoSubscribeVideo = true
            option.clientRoleType = .broadcaster
            rtcKit.setAudioProfile(.musicHighQuality, scenario: .chorus)
            rtcKit.joinChannel(byToken: nil, channelId: channelName, uid: UInt(rtcUid), mediaOptions: option)

        } else {
            let option = AgoraRtcChannelMediaOptions()
            option.publishCameraTrack = false // 关闭视频采集
            option.publishMicrophoneTrack = false // 关闭音频采集
            option.autoSubscribeAudio = true
            rtcKit.setAudioProfile(.musicHighQuality, scenario: .chorus) // 设置profile
            option.clientRoleType = .audience // 设置观众角色
            rtcKit.joinChannel(byToken: nil, channelId: channelName, uid: 0, mediaOptions: option)
        }
    }

    /**
     * 加入语聊房
     * @param channelName 频道名称
     * @param rtcUid RTCUid 如果传0，大网会自动分配
     * @param rtmUid 可选，如果不使用RTM，使用自己的IM，这个值不用传
     * @param type 有四种 social，ktv，game， anchor
     */
    public func joinVoicRoomWith(with channelName: String, rtcUid: Int?, type: VMMUSIC_TYPE) -> Int32 {
        self.type = .VoiceChat
        rtcKit.enableAudioVolumeIndication(200, smooth: 3, reportVad: true)
        if type == .ktv || type == .social {
            rtcKit.setChannelProfile(.liveBroadcasting)
            rtcKit.setAudioProfile(.musicHighQuality)
            rtcKit.setAudioScenario(.gameStreaming)
        } else if type == .game {
            rtcKit.setChannelProfile(.communication)
        } else if type == .anchor {
            rtcKit.setAudioProfile(.musicHighQualityStereo)
            rtcKit.setAudioScenario(.gameStreaming)
            rtcKit.setParameters("{\"che.audio.custom_payload_type\":73}")
            rtcKit.setParameters("{\"che.audio.custom_bitrate\":128000}")
            //  rtcKit.setRecordingDeviceVolume(128)
            rtcKit.setParameters("{\"che.audio.input_channels\":2}")
        }
        setAINS(with: .mid)
        loadKit(with: channelName, rtcUid: rtcUid)
        let code: Int32 = rtcKit.joinChannel(byToken: nil, channelId: channelName, info: nil, uid: UInt(rtcUid ?? 0))
        return code
    }

    /**
     * 加载RTC
     * @param channelName 频道名称
     * @param rtcUid RTCUid 如果传0，大网会自动分配
     */
    private func loadKit(with channelName: String, rtcUid: Int?) {
        rtcKit.delegate = self
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
        path = path.replacingOccurrences(of: "CN", with: LanguageManager.localValue(key: "Lau"))
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
            rtcKit.setParameters("{\"che.audio.ains_mode\":0}")
            rtcKit.setParameters("{\"che.audio.nsng.lowerBound\":80}")
            rtcKit.setParameters("{\"che.audio.nsng.lowerMask\":50}")
            rtcKit.setParameters("{\"che.audio.nsng.statisticalbound\":5}")
            rtcKit.setParameters("{\"che.audio.nsng.finallowermask\":30}")
            rtcKit.setParameters("{\"che.audio.nsng.enhfactorstastical\":200}")
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
        AgoraRtcEngineKit.destroy()
        ASRTCKit._sharedInstance = nil // 释放单例
    }
}

// MARK: - AgoraRtcEngineDelegate

extension ASRTCKit: AgoraRtcEngineDelegate {
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

extension ASRTCKit: AgoraRtcMediaPlayerDelegate {
    public func rtcEngine(_ engine: AgoraRtcEngineKit, audioMixingStateChanged state: AgoraAudioMixingStateType, reasonCode: AgoraAudioMixingReasonCode) {
        if state == .stopped {
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
    public func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        guard let _ = delegate else {
            return
        }

        playerDelegate?.didMPKChangedTo?(state: state, error: error)
    }
}
