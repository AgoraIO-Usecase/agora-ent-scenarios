//
//  ScenarioApi.swift
//  1 VS 1 Demo
//
//  Created by CP on 2024/2/23.
//

import Foundation
import AgoraRtcKit

/**
 * 场景类型
 * @param KTV K歌房
 * @param ChatRoom 语聊房
 * @param Show 秀场
 */
public enum SceneType: Int {
    case Chat = 0
    case Show = 1
}

/**
 * 音频设置类型
 * @param Chat_Caller 1v1 呼叫中的主叫（通常是榜一大哥）
 * @param Chat_Callee 1v1 呼叫中的被叫（通常是主播小姐姐）
 * @param Show_Host 秀场主播
 * @param Show_InteractiveAudience 秀场连麦观众
 */
public enum AudioScenarioType: Int {
    case Chat_Caller = 0
    case Chat_Callee = 1
    case Show_Host = 2
    case Show_InteractiveAudience = 3
}

/**
 * 虚拟声卡类型
 * @param Magnetic 磁性
 * @param Pleasant 悦耳
 * @param Close 关
 */
public enum SoundCardType {
    case magnetic
    case pleasant
    case close
    
    var gainValue: Float {
        switch self {
        case .magnetic, .pleasant:
            return 200.0
        case .close:
            return -100.0
        }
    }
    
    var presetValue: Int {
        switch self {
        case .magnetic, .pleasant:
            return 4
        case .close:
            return -1
        }
    }
    
    var gender: Int {
        switch self {
        case .magnetic, .pleasant:
            return 0
        case .close:
            return -1
        }
    }
    
    var effect: Int {
        switch self {
        case .magnetic:
            return 0
        case .pleasant:
            return 1
        case .close:
            return -1
        }
    }
}

struct AudioScenarioSetting {
    let sf: Bool
    let aiaec: AIAECType
    let anis: ANISType
    let agcType: AGCType
    let codec: Int?
    let fecType: FECType
    let netEQ: Int
    let a2dp: Bool?
}

struct RecommendAudioScenarioSetting {
    static let BoySpeaker = AudioScenarioSetting(
        sf: true,
        aiaec: .Normal,
        anis: .Strong,
        agcType: .BigGain,
        codec: 18000,
        fecType: .Single,
        netEQ: 250,
        a2dp: nil
    )
    
    static let BoyWired = AudioScenarioSetting(
        sf: true,
        aiaec: .Off,
        anis: .Strong,
        agcType: .Normal,
        codec: 18000,
        fecType: .Single,
        netEQ: 250,
        a2dp: nil
    )
    
    static let BoyBluetooth = AudioScenarioSetting(
        sf: true,
        aiaec: .Off,
        anis: .Strong,
        agcType: .Normal,
        codec: 18000,
        fecType: .Single,
        netEQ: 250,
        a2dp: true
    )
    
    static let GirlSpeaker = AudioScenarioSetting(
        sf: true,
        aiaec: .Normal,
        anis: .Strong,
        agcType: .BigGain,
        codec: 24000,
        fecType: .Single,
        netEQ: 250,
        a2dp: nil
    )
    
    static let GirlWired = AudioScenarioSetting(
        sf: true,
        aiaec: .LowAggressive,
        anis: .Off,
        agcType: .Normal,
        codec: nil,
        fecType: .Auto,
        netEQ: 250,
        a2dp: nil
    )
    
    static let GirlBluetooth = AudioScenarioSetting(
        sf: true,
        aiaec: .LowAggressive,
        anis: .Off,
        agcType: .Normal,
        codec: nil,
        fecType: .Auto,
        netEQ: 250,
        a2dp: false
    )
    
    static let HostSpeaker = AudioScenarioSetting(
        sf: true,
        aiaec: .Normal,
        anis: .Strong,
        agcType: .BigGain,
        codec: 128000,
        fecType: .Auto,
        netEQ: 250,
        a2dp: nil
    )
    
    static let HostWired = AudioScenarioSetting(
        sf: false,
        aiaec: .Off,
        anis: .Off,
        agcType: .Off,
        codec: 128000,
        fecType: .Auto,
        netEQ: 250,
        a2dp: nil
    )
    
    static let HostBluetooth = AudioScenarioSetting(
        sf: true,
        aiaec: .Off,
        anis: .Off,
        agcType: .Normal,
        codec: 128000,
        fecType: .Auto,
        netEQ: 250,
        a2dp: false
    )
    
    static let AudienceSpeaker = AudioScenarioSetting(
        sf: true,
        aiaec: .Normal,
        anis: .Strong,
        agcType: .BigGain,
        codec: 18000,
        fecType: .Single,
        netEQ: 250,
        a2dp: nil
    )
    
    static let AudienceWired = AudioScenarioSetting(
        sf: true,
        aiaec: .Off,
        anis: .Strong,
        agcType: .Normal,
        codec: 18000,
        fecType: .Single,
        netEQ: 250,
        a2dp: nil
    )
    
    static let AudienceBluetooth = AudioScenarioSetting(
        sf: true,
        aiaec: .Off,
        anis: .Strong,
        agcType: .Normal,
        codec: 18000,
        fecType: .Single,
        netEQ: 250,
        a2dp: true
    )
}

enum ANISType: Int {
    case Off = 0
    case Strong = 1
}

enum AIAECType {
    case Off
    case Normal
    case LowAggressive
    
    var flag: Int {
        switch self {
        case .Off:
            return -1
        case .Normal:
            return 1
        case .LowAggressive:
            return -1
        }
    }
    
    var route: Int {
        switch self {
        case .Off:
            return -1
        case .Normal:
            return 11
        case .LowAggressive:
            return -1
        }
    }
    
    var aggressive: Int {
        switch self {
        case .Off:
            return -1
        case .Normal:
            return -1
        case .LowAggressive:
            return 0
        }
    }
}

enum AGCType {
    case Off
    case Normal
    case BigGain
    
    var enable: Bool {
        switch self {
        case .Off:
            return false
        case .Normal, .BigGain:
            return true
        }
    }
    
    var targetlevelBov: Int {
        switch self {
        case .Off:
            return -1
        case .Normal:
            return 6
        case .BigGain:
            return 3
        }
    }
    
    var compressionGain: Int {
        switch self {
        case .Off:
            return -1
        case .Normal:
            return 12
        case .BigGain:
            return 18
        }
    }
}

enum FECType: Int {
    case Auto = 0
    case Single = 1
}

public class AudioScenarioApi: NSObject {
    static let tag = "AUDIO_API_LOG"
    static let version = "8_iOS_0.1.0"
    
    private var rtcEngine: AgoraRtcEngineKit
    private var audioScenarioType: AudioScenarioType?
    private var audioRoute: AgoraAudioOutputRouting = .default
    func initialize() {
        // 数据上报
        rtcEngine.setParameters("{\"rtc.direct_send_custom_event\": true}")
        // 写日志
        rtcEngine.setParameters("{\"rtc.log_external_input\": true}")
        rtcEngine.addDelegate(self)
    }
    
    public init(rtcEngine: AgoraRtcEngineKit) {
        self.rtcEngine = rtcEngine
        super.init()
        initialize()
    }
    
    public func setAudioScenario(sceneType: SceneType, audioScenarioType: AudioScenarioType) {
        reportCallScenarioApi(event: "setAudioScenario", params: ["sceneType": sceneType, "audioScenarioType": audioScenarioType])
        self.audioScenarioType = audioScenarioType
        switch sceneType {
        case .Chat:
            switch audioScenarioType {
            case .Chat_Caller:
                rtcEngine.setAudioProfile(.default)
                rtcEngine.setAudioScenario(.meeting)
                //大哥默认关闭虚拟声卡
                enableVirtualSoundCard(soundCardType: .close)
            case .Chat_Callee:
                rtcEngine.setAudioProfile(.musicHighQualityStereo)
                rtcEngine.setAudioScenario(.meeting)
                enableVirtualSoundCard(soundCardType: .magnetic)
            default:
                scenarioApiLogError(msg: "not supported")
            }
        case .Show:
            switch audioScenarioType {
            case .Show_Host:
                rtcEngine.setAudioProfile(.musicHighQualityStereo)
                rtcEngine.setAudioScenario(.gameStreaming)
                rtcEngine.setParameters("{\"che.audio.custom_payload_type\": 78}")
                //主播默认关闭虚拟声卡
                enableVirtualSoundCard(soundCardType: .close)
            case .Show_InteractiveAudience:
                rtcEngine.setAudioProfile(.default)
                rtcEngine.setAudioScenario(.meeting)
                //连麦观众默认关闭虚拟声卡
                enableVirtualSoundCard(soundCardType: .close)
            default:
                scenarioApiLogError(msg: "not supported")
            }
        }
        setAudioSettings()
    }
    
}

extension AudioScenarioApi: AgoraRtcEngineDelegate {

    public func rtcEngine(_ engine: AgoraRtcEngineKit, didAudioRouteChanged routing: AgoraAudioOutputRouting) {
        scenarioApiLog(msg: "onAudioRouteChanged:\(routing)")
        self.audioRoute = routing
        setAudioSettings()
    }
    
    private func setAudioSettings() {
        guard let type = audioScenarioType else {return}
        switch self.audioRoute {
        case  .headset, .headsetNoMic, .usb:
            // 耳机
            switch type {
            case .Chat_Caller:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.BoyWired)
            case .Chat_Callee:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.GirlWired)
            case .Show_Host:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.HostWired)
            case .Show_InteractiveAudience:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.AudienceWired)
            }
        case .speakerphone:
            // 扬声器
            switch type {
            case .Chat_Caller:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.BoySpeaker)
            case .Chat_Callee:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.GirlSpeaker)
            case .Show_Host:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.HostSpeaker)
            case .Show_InteractiveAudience:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.AudienceSpeaker)
            }
        case .bluetoothDeviceA2dp, .bluetoothDeviceHfp:
            // 蓝牙耳机
            switch type {
            case .Chat_Caller:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.BoyBluetooth)
            case .Chat_Callee:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.GirlBluetooth)
            case .Show_Host:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.HostBluetooth)
            case .Show_InteractiveAudience:
                setAudioSettingsWithConfig(RecommendAudioScenarioSetting.AudienceBluetooth)
            }
        default:
            break
        }
    }
    
    private func setAudioSettingsWithConfig(_ setting: AudioScenarioSetting) {
        setAudioSettings(sf: setting.sf, aiaec: setting.aiaec, ains: setting.anis, agcType: setting.agcType, codec: setting.codec, fecType: setting.fecType, netEQ: setting.netEQ, a2dp: setting.a2dp)
    }
    
    private func setAudioSettings(sf: Bool, aiaec: AIAECType?, ains: ANISType?, agcType: AGCType?, codec: Int?, fecType: FECType?, netEQ: Int?, a2dp: Bool?) {
        rtcEngine.setParameters("{\"che.audio.sf.enabled\": \(sf)}")
        rtcEngine.setParameters("{\"che.audio.input_sample_rate\":48000}")
        
        ains.map {
            if $0 == .Strong {
                rtcEngine.setParameters("{\"che.audio.sf.ainsToLoadFlag\": 1}")
                rtcEngine.setParameters("{\"che.audio.sf.nsngAlgRoute\": 12}")
                rtcEngine.setParameters("{\"che.audio.sf.nsngPredefAgg\": 11}")
            } else if $0 == .Off && !sf {
                rtcEngine.setParameters("{\"che.audio.sf.nsEnable\": 0}")
                rtcEngine.setParameters("{\"che.audio.ans.enable\": false}")
            }
        }
        
        aiaec.map {
            switch $0 {
            case .Normal:
                rtcEngine.setParameters("{\"che.audio.sf.ainlpToLoadFlag\": \($0.flag)}")
                rtcEngine.setParameters("{\"che.audio.sf.nlpAlgRoute\": \($0.route)}")
            case .LowAggressive:
                rtcEngine.setParameters("{\"che.audio.sf.nlpAggressiveness\": \($0.aggressive)}")
            case .Off:
                if !sf {
                    rtcEngine.setParameters("{\"che.audio.sf.nlpEnable\": 0}")
                    rtcEngine.setParameters("{\"che.audio.aec.enable\": false}")
                }
            }
        }
        
        agcType.map {
            rtcEngine.setParameters("{\"che.audio.agc.enable\": \($0.enable)}")
            if $0.enable {
                rtcEngine.setParameters("{\"che.audio.agc.targetlevelBov\": \($0.targetlevelBov)}")
                rtcEngine.setParameters("{\"che.audio.agc.compressionGain\": \($0.compressionGain)}")
            }
        }
        
        let _ = codec.map {
            rtcEngine.setParameters("{\"che.audio.custom_bitrate\": \($0)}")
        }
        
        fecType.map {
            switch $0 {
            case .Single:
                rtcEngine.setParameters("{\"che.audio.rsfec\":[8, 4]}")
            case .Auto:
                break
            }
        }
        
        let _ = netEQ.map {
            rtcEngine.setParameters("{\"rtc.min_playout_delay_speaker\": \($0)}")
        }
        
        let _ = a2dp.map {
            rtcEngine.setParameters("{\"che.audio.force_bluetooth_a2dp\": \($0)}")
        }
        
    }
    
    private func enableVirtualSoundCard(soundCardType: SoundCardType) {
        rtcEngine.setParameters("{\"che.audio.virtual_soundcard\":{\"preset\":\(soundCardType.presetValue),\"gain\":\(soundCardType.gainValue),\"gender\":\(soundCardType.gender),\"effect\":\(soundCardType.effect)}}")
    }
    
    private func reportCallScenarioApi(event: String, params: [String: Any]) {
        scenarioApiLog(msg: "event: \(event), params: \(params)")
        rtcEngine.sendCustomReportMessage("scenarioAPI", category: AudioScenarioApi.version, event: event, label: "\(params)", value: 0)
    }
    
    private func scenarioApiLog(msg: String) {
       rtcEngine.writeLog(.info, content: msg)
    }
    
    private func scenarioApiLogError(msg: String) {
        rtcEngine.writeLog(.error, content: msg)
    }
}

