package io.agora.voice.rtckit.internal

import android.content.Context
import io.agora.voice.buddy.config.ConfigConstants
import io.agora.rtc2.*
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.rtckit.annotation.SoundSelection
import io.agora.voice.rtckit.internal.base.BaseMediaPlayerEngine
import io.agora.voice.rtckit.internal.base.RtcBaseAudioEngine
import io.agora.voice.rtckit.internal.base.RtcBaseDeNoiseEngine
import io.agora.voice.rtckit.internal.base.RtcBaseSoundEffectEngine
import io.agora.voice.rtckit.internal.base.RtcBaseSpatialAudioEngine
import io.agora.voice.rtckit.internal.impl.AgoraAudioEngine
import io.agora.voice.rtckit.internal.impl.AgoraMediaPlayerEngine
import io.agora.voice.rtckit.internal.impl.AgoraRtcDeNoiseEngine
import io.agora.voice.rtckit.internal.impl.AgoraRtcSoundEffectEngine
import io.agora.voice.rtckit.internal.impl.AgoraRtcSpatialAudioEngine
import io.agora.voice.rtckit.open.config.RtcChannelConfig
import io.agora.voice.rtckit.open.config.RtcInitConfig
import io.agora.voice.rtckit.open.status.RtcErrorStatus

/**
 * @author create by zhangwei03
 *
 *  agora 引擎，创建 client, 获取BaseEngine
 */
internal class AgoraRtcClientEx : RtcBaseClientEx<RtcEngineEx>() {

    private var eventHandler: AgoraRtcEventHandler? = null
    private var context: Context? = null
    private var initConfig: RtcInitConfig? = null

    override fun createClient(
        context: Context, config: RtcInitConfig, rtcClientListener: IRtcClientListener
    ): RtcEngineEx? {
        this.context = context
        this.initConfig = config
        this.rtcListener = rtcClientListener
        this.eventHandler = AgoraRtcEventHandler(rtcListener)
        if (!checkCreate()) return rtcEngine
        return rtcEngine
    }

    private fun checkCreate(): Boolean {
        if (rtcEngine != null || context == null) {
            return false
        }
        synchronized(AgoraRtcClientEx::class.java) {
            if (rtcEngine != null) return false
            //初始化RTC
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = initConfig?.appId
            config.mEventHandler = eventHandler
//            config.mLogConfig = RtcEngineConfig.LogConfig().apply {
//                level = Constants.LogLevel.getValue(Constants.LogLevel.LOG_LEVEL_ERROR)
//            }
            config.addExtension("agora_ai_noise_suppression_extension")
            try {
                rtcEngine = RtcEngineEx.create(config) as RtcEngineEx?
            } catch (e: Exception) {
                e.printStackTrace()
                "rtc engine init error:${e.message}".logE(TAG)
                return false
            }
            return true
        }
    }

    override fun joinChannel(config: RtcChannelConfig) {
        checkJoinChannel(config)
    }

    private fun checkJoinChannel(config: RtcChannelConfig): Boolean {
        if (config.roomId.isEmpty() || config.userId < 0) {
            val errMsg = "join channel error roomId or rtcUid illegal!(roomId:${config.roomId},rtcUid:${config.userId})"
            errMsg.logE(TAG)
            rtcListener?.onError(RtcErrorStatus(IRtcEngineEventHandler.ErrorCode.ERR_FAILED, errMsg))
            return false
        }

        rtcEngine?.apply {
            when (config.soundType) {
                SoundSelection.SocialChat, SoundSelection.Karaoke -> { // 社交语聊，ktv
                    setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                    setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY)
                    setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
                }
                SoundSelection.GamingBuddy -> { // 游戏陪玩
                    setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
                }
                else -> { //专业主播
                    setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY)
                    setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
                    setParameters("{\"che.audio.custom_payload_type\":73}")
                    setParameters("{\"che.audio.custom_bitrate\":128000}")
                    // setRecordingDeviceVolume(128) 4.0.1上才支持
                    setParameters("{\"che.audio.input_channels\":2}")
                }
            }
        }
        if (config.broadcaster) {
            // 音效默认50
            rtcEngine?.adjustAudioMixingVolume(ConfigConstants.RotDefaultVolume)
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        } else {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
        val status = rtcEngine?.joinChannel(config.appToken, config.roomId, "", config.userId)
        // 启用用户音量提示。
        rtcEngine?.enableAudioVolumeIndication(1000, 3, false)
        if (status != IRtcEngineEventHandler.ErrorCode.ERR_OK) {
            val errorMsg = "join channel error status not ERR_OK!"
            errorMsg.logE(TAG)
            rtcListener?.onError(RtcErrorStatus(status ?: IRtcEngineEventHandler.ErrorCode.ERR_FAILED, errorMsg))
            return false
        }
        return true
    }

    override fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }

    override fun switchRole(broadcaster: Boolean) {
        if (broadcaster) {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        } else {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
    }

    override fun createAudioEngine(): RtcBaseAudioEngine<RtcEngineEx> {
        return AgoraAudioEngine()
    }

    override fun createDeNoiseEngine(): RtcBaseDeNoiseEngine<RtcEngineEx> {
        return AgoraRtcDeNoiseEngine()
    }

    override fun createSoundEffectEngine(): RtcBaseSoundEffectEngine<RtcEngineEx> {
        return AgoraRtcSoundEffectEngine()
    }

    override fun createSpatialAudioEngine(): RtcBaseSpatialAudioEngine<RtcEngineEx> {
        return AgoraRtcSpatialAudioEngine()
    }

    override fun createMediaPlayerEngine(): BaseMediaPlayerEngine<RtcEngineEx>? {
        return AgoraMediaPlayerEngine()
    }

    override fun destroy() {
        rtcEngine?.leaveChannel()
        eventHandler?.destroy()
        context = null
        super.destroy()
    }
}