package io.agora.scene.show.audio

import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import org.json.JSONObject

/**
 * 场景类型
 * @param Chat 1v1 连麦场景
 * @param Show 视频直播场景
 */
enum class SceneType(val value: Int) {
    Chat(0),
    Show(1)
}

/**
 * 音频设置类型
 * @param Chat_Caller 1v1 呼叫中的主叫（通常是榜一大哥）
 * @param Chat_Caller 1v1 呼叫中的被叫（通常是主播小姐姐）
 * @param ShowHost 秀场主播
 * @param InteractiveAudience 秀场连麦观众
 */
enum class AudioScenarioType(val value: Int) {
    Chat_Caller(0),
    Chat_Callee(1),
    Show_Host(2),
    Show_InteractiveAudience(3)
}

/**
 * 虚拟声卡类型
 * @param Magnetic 磁性
 * @param Pleasant 悦耳
 * @param Close 关
 */
enum class SoundCardType constructor(
    val gainValue: Int,
    val presetValue: Int,
    val gender: Int,
    val effect: Int,
) {
    Magnetic(gainValue = 200, presetValue = 4, gender = 0, effect = 0),
    Pleasant(gainValue = 200, presetValue = 4, gender = 0, effect = 1),
    Close(gainValue = -100, presetValue = -1, gender = -1, effect = -1)
}

/**
 * 插入耳机的类型
 * @param EQ0 针对小米系列有线耳机
 * @param EQ1 针对Sony系列有线耳机
 * @param EQ2 针对JBL系列有线耳机
 * @param EQ3 针对华为系列有线耳机
 * @param EQ4 针对iphone系列有线耳机(默认)
 */
enum class EarPhoneType constructor(val presetValue: Int) {
    EQ0(0),
    EQ1(1),
    EQ2(2),
    EQ3(3),
    EQ4(4),
}

enum class ANISType(val value: Int) {
    Off(0),
    Strong(1)
}

enum class AIAECType(val flag: Int, val route: Int, val aggressive: Int) {
    Off(-1, -1, -1),
    Normal(1, 11, -1),
    LowAggressive(-1, -1, 0)
}

enum class AGCType(val enable: Boolean, val targetlevelBov: Int, val compressionGain: Int) {
    Off(false, -1, -1),
    Normal(true, 6, 12),
    BigGain(true, 3, 18)
}

enum class FECType(val value: Int) {
    Auto(0),
    Single(1)
}

data class AudioScenarioSetting constructor(
    val sf: Boolean,
    val aiaec: AIAECType,
    val anis: ANISType,
    val agcType: AGCType,
    val codec: Int?,
    val fecType: FECType,
    val netEQ: Int,
    val a2dp: Boolean?
)

object RecommendAudioScenarioSetting {
    val BoySpeaker = AudioScenarioSetting(
        sf = true,
        aiaec = AIAECType.Normal,
        anis = ANISType.Strong,
        agcType = AGCType.BigGain,
        codec = 18000,
        fecType = FECType.Single,
        netEQ = 250,
        a2dp = null
    )

    val BoyWired = AudioScenarioSetting(
        sf = true,
        aiaec = AIAECType.Off,
        anis = ANISType.Strong,
        agcType = AGCType.Normal,
        codec = 18000,
        fecType = FECType.Single,
        netEQ = 250,
        a2dp = null
    )

    val BoyBluetooth = AudioScenarioSetting(
        sf = true,
        aiaec = AIAECType.Off,
        anis = ANISType.Strong,
        agcType = AGCType.Normal,
        codec = 18000,
        fecType = FECType.Single,
        netEQ = 250,
        a2dp = true
    )

    val GirlSpeaker = AudioScenarioSetting(
        sf = true,
        aiaec = AIAECType.Normal,
        anis = ANISType.Strong,
        agcType = AGCType.BigGain,
        codec = 24000,
        fecType = FECType.Single,
        netEQ = 250,
        a2dp = null
    )

    val GirlWired = AudioScenarioSetting(
        sf = true,
        aiaec = AIAECType.LowAggressive,
        anis = ANISType.Off,
        agcType = AGCType.Normal,
        codec = null,
        fecType = FECType.Auto,
        netEQ = 250,
        a2dp = null
    )

    val GirlBluetooth = AudioScenarioSetting(
        sf = true,
        aiaec = AIAECType.LowAggressive,
        anis = ANISType.Off,
        agcType = AGCType.Normal,
        codec = null,
        fecType = FECType.Auto,
        netEQ = 250,
        a2dp = false
    )

    val HostSpeaker = AudioScenarioSetting(
        sf = true,
        aiaec = AIAECType.Normal,
        anis = ANISType.Strong,
        agcType = AGCType.BigGain,
        codec = 128000,
        fecType = FECType.Auto,
        netEQ = 250,
        a2dp = null
    )

    val HostWired = AudioScenarioSetting(
        sf = false,
        aiaec = AIAECType.Off,
        anis = ANISType.Off,
        agcType = AGCType.Off,
        codec = 128000,
        fecType = FECType.Auto,
        netEQ = 250,
        a2dp = null
    )

    val HostBluetooth = AudioScenarioSetting(
        sf = true,
        aiaec = AIAECType.Off,
        anis = ANISType.Off,
        agcType = AGCType.Normal,
        codec = 128000,
        fecType = FECType.Auto,
        netEQ = 250,
        a2dp = false
    )

    val AudienceSpeaker = AudioScenarioSetting(
        sf = true,
        aiaec = AIAECType.Normal,
        anis = ANISType.Strong,
        agcType = AGCType.BigGain,
        codec = 18000,
        fecType = FECType.Single,
        netEQ = 250,
        a2dp = null
    )

    val AudienceWired = AudioScenarioSetting(
        sf = true,
        aiaec = AIAECType.Off,
        anis = ANISType.Strong,
        agcType = AGCType.Normal,
        codec = 18000,
        fecType = FECType.Single,
        netEQ = 250,
        a2dp = null
    )

    val AudienceBluetooth = AudioScenarioSetting(
        sf = true,
        aiaec = AIAECType.Off,
        anis = ANISType.Strong,
        agcType = AGCType.Normal,
        codec = 18000,
        fecType = FECType.Single,
        netEQ = 250,
        a2dp = true
    )
}

class AudioScenarioApi(rtcEngine: RtcEngine): IRtcEngineEventHandler() {

    companion object {
        const val tag = "AUDIO_API_LOG"
        const val version = "8_android_0.1.0"
    }

    private var rtcEngine: RtcEngine
    private var audioScenarioType: AudioScenarioType? = null
    private var audioRoute: Int? = null

    init {
        this.rtcEngine = rtcEngine
    }

    fun initialize() {
        // 数据上报
        rtcEngine.setParameters("{\"rtc.direct_send_custom_event\": true}")
        // 写日志
        rtcEngine.setParameters("{\"rtc.log_external_input\": true}")
        // 注册事件监听
        rtcEngine.addHandler(this)
    }

    fun setAudioScenario(sceneType: SceneType, audioScenarioType: AudioScenarioType) {
        reportCallScenarioApi("setAudioScenario", JSONObject().put("sceneType", sceneType).put("audioScenarioType", audioScenarioType))
        this.audioScenarioType = audioScenarioType
        when (sceneType) {
            SceneType.Chat -> {
                when (audioScenarioType) {
                    AudioScenarioType.Chat_Caller -> {
                        rtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT)
                        rtcEngine.setAudioScenario(Constants.AUDIO_SCENARIO_MEETING)
                        // 大哥默认关闭虚拟声卡
                        enableVirtualSoundCard(SoundCardType.Close)
                    }
                    AudioScenarioType.Chat_Callee -> {
                        rtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO)
                        rtcEngine.setAudioScenario(Constants.AUDIO_SCENARIO_MEETING)
                        // 女主播默认开启虚拟声卡
                        enableVirtualSoundCard(SoundCardType.Magnetic)
                    }
                    else -> {
                        scenarioApiLogError("not supported")
                    }
                }
            }
            SceneType.Show -> {
                when (audioScenarioType) {
                    AudioScenarioType.Show_Host -> {
                        rtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO)
                        rtcEngine.setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
                        rtcEngine.setParameters("{\"che.audio.custom_payload_type\": 78}")
                        // 主播默认关闭虚拟声卡
                        enableVirtualSoundCard(SoundCardType.Close)
                    }
                    AudioScenarioType.Show_InteractiveAudience -> {
                        rtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT)
                        rtcEngine.setAudioScenario(Constants.AUDIO_SCENARIO_MEETING)
                        // 连麦观众默认关闭虚拟声卡
                        enableVirtualSoundCard(SoundCardType.Close)
                    }
                    else -> {
                        scenarioApiLogError("not supported")
                    }
                }
            }
        }
        setAudioSettings()
    }

    // ------------- IRtcEngineEventHandler -------------
    override fun onAudioRouteChanged(routing: Int) {
        super.onAudioRouteChanged(routing)
        scenarioApiLog("onAudioRouteChanged: $routing")
        this.audioRoute = routing
        setAudioSettings()
    }

    // -------------- inner private ---------------
    private fun setAudioSettings() {
        audioRoute ?: return
        audioScenarioType ?: return
        when (audioRoute) {
            Constants.AUDIO_ROUTE_HEADSET, Constants.AUDIO_ROUTE_HEADSETNOMIC, Constants.AUDIO_ROUTE_USBDEVICE -> {
                // 耳机
                when (audioScenarioType) {
                    AudioScenarioType.Chat_Caller -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.BoyWired)
                    }
                    AudioScenarioType.Chat_Callee -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.GirlWired)
                    }
                    AudioScenarioType.Show_Host -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.HostWired)
                    }
                    AudioScenarioType.Show_InteractiveAudience -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.AudienceWired)
                    }
                    else -> {}
                }
            }
            Constants.AUDIO_ROUTE_SPEAKERPHONE -> {
                // 扬声器
                when (audioScenarioType) {
                    AudioScenarioType.Chat_Caller -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.BoySpeaker)
                    }
                    AudioScenarioType.Chat_Callee -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.GirlSpeaker)
                    }
                    AudioScenarioType.Show_Host -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.HostSpeaker)
                    }
                    AudioScenarioType.Show_InteractiveAudience -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.AudienceSpeaker)
                    }
                    else -> {}
                }
            }
            Constants.AUDIO_ROUTE_HEADSETBLUETOOTH -> {
                // 蓝牙耳机
                when (audioScenarioType) {
                    AudioScenarioType.Chat_Caller -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.BoyBluetooth)
                    }
                    AudioScenarioType.Chat_Callee -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.GirlBluetooth)
                    }
                    AudioScenarioType.Show_Host -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.HostBluetooth)
                    }
                    AudioScenarioType.Show_InteractiveAudience -> {
                        setAudioSettingsWithConfig(RecommendAudioScenarioSetting.AudienceBluetooth)
                    }
                    else -> {}
                }
            }
            else -> {}
        }
    }

    private fun setAudioSettingsWithConfig(
        setting: AudioScenarioSetting
    ) {
        setAudioSettings(
            sf = setting.sf,
            aiaec = setting.aiaec,
            ains = setting.anis,
            agcType = setting.agcType,
            codec = setting.codec,
            fecType = setting.fecType,
            netEQ = setting.netEQ,
            a2dp = setting.a2dp
        )
    }

    private fun setAudioSettings(
        sf: Boolean,
        aiaec: AIAECType?,
        ains: ANISType?,
        agcType: AGCType?,
        codec: Int?,
        fecType: FECType?,
        netEQ: Int?,
        a2dp: Boolean?
    ) {
        // 开启并行架构
        rtcEngine.setParameters("{\"che.audio.sf.enabled\": $sf}")
        rtcEngine.setParameters("{\"che.audio.input_sample_rate\":48000}")

        ains?.let {
            if (it == ANISType.Strong) {
                rtcEngine.setParameters("{\"che.audio.sf.ainsToLoadFlag\": 1}")
                rtcEngine.setParameters("{\"che.audio.sf.nsngAlgRoute\": 12}")
                rtcEngine.setParameters("{\"che.audio.sf.nsngPredefAgg\": 11}")
            } else if (it == ANISType.Off && !sf) {
                rtcEngine.setParameters("{\"che.audio.sf.nsEnable\": 0}")
                rtcEngine.setParameters("{\"che.audio.ans.enable\": false}")
            } else { }
        }

        aiaec?.let {
            when (it) {
                AIAECType.Normal -> {
                    rtcEngine.setParameters("{\"che.audio.sf.ainlpToLoadFlag\": ${it.flag}}")
                    rtcEngine.setParameters("{\"che.audio.sf.nlpAlgRoute\": ${it.route}}")
                }
                AIAECType.LowAggressive -> {
                    rtcEngine.setParameters("{\"che.audio.sf.nlpAggressiveness\": ${it.aggressive}}")
                }
                AIAECType.Off -> {
                    if (!sf) {
                        rtcEngine.setParameters("{\"che.audio.sf.nlpEnable\": 0}")
                        rtcEngine.setParameters("{\"che.audio.aec.enable\": false}")
                    } else { }
                }
            }
        }

        agcType?.let {
            rtcEngine.setParameters("{\"che.audio.agc.enable\": ${it.enable}}")
            if (it.enable) {
                rtcEngine.setParameters("{\"che.audio.agc.targetlevelBov\": ${it.targetlevelBov}}")
                rtcEngine.setParameters("{\"che.audio.agc.compressionGain\": ${it.compressionGain}}")
            }
        }

        codec?.let {
            rtcEngine.setParameters("{\"che.audio.custom_bitrate\": $it}")
        }

        fecType?.let {
            when (it) {
                FECType.Single -> {
                    rtcEngine.setParameters("{\"che.audio.rsfec\":[8, 4]}")
                }
                FECType.Auto -> {}
            }
        }

        netEQ?.let {
            rtcEngine.setParameters("{\"rtc.min_playout_delay_speaker\": $it}")
        }

        a2dp?.let {
            rtcEngine.setParameters("{\"che.audio.force_bluetooth_a2dp\": $it}")
        }
    }

    private fun enableVirtualSoundCard(soundCardType: SoundCardType, earPhoneType: EarPhoneType? = null) {
        reportCallScenarioApi("enableVirtualSoundCard", JSONObject().put("soundCardType", soundCardType).put("earPhoneType", earPhoneType))

        // 有预设的耳机类型
        earPhoneType?.let {
            if (soundCardType != SoundCardType.Close) {
                rtcEngine.setParameters("{\"che.audio.virtual_soundcard\":{\"preset\":${earPhoneType.presetValue},\"gain\":${soundCardType.gainValue},\"gender\":${soundCardType.gender},\"effect\":${soundCardType.effect}}}")
                return
            } else {
                enableVirtualSoundCard(SoundCardType.Close, null)
            }
        }

        // 默认耳机类型
        rtcEngine.setParameters("{\"che.audio.virtual_soundcard\":{\"preset\":${soundCardType.presetValue},\"gain\":${soundCardType.gainValue},\"gender\":${soundCardType.gender},\"effect\":${soundCardType.effect}}}")
    }

    // ------------------- 日志和数据上报 --------------------
    // 数据上报
    private fun reportCallScenarioApi(event: String, params: JSONObject) {
        scenarioApiLog("event: $event, params:$params")
        rtcEngine.sendCustomReportMessage(
            "agora:scenarioAPI",
            version,
            event,
            params.toString(),
            0)
    }

    private fun scenarioApiLog(msg: String) {
        rtcEngine.writeLog(Constants.LOG_LEVEL_INFO, "[$tag] $msg")
    }

    private fun scenarioApiLogError(msg: String) {
        rtcEngine.writeLog(Constants.LOG_LEVEL_ERROR, "[$tag] $msg")
    }
}