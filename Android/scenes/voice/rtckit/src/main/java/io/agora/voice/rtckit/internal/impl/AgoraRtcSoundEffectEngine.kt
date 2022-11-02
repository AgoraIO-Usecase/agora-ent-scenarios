package io.agora.voice.rtckit.internal.impl

import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngineEx
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.rtckit.internal.base.RtcBaseSoundEffectEngine

/**
 * @author create by zhangwei03
 *
 * agora 音效管理 统一修改为 AudioMixing 方式
 */
internal class AgoraRtcSoundEffectEngine : RtcBaseSoundEffectEngine<RtcEngineEx>() {
    override fun playEffect(
        soundId: Int, filePath: String, loopBack: Boolean, cycle: Int, soundSpeakerType: Int
    ): Boolean {
        "startAudioMixing soundId:$soundId, filePath:$filePath, loopBack:$loopBack, cycle:$cycle ".logE("AgoraRtcSoundEffectEngine")
//        val result = engine?.playEffect(soundId, filePath, loopCount, 1.0, 0.0, 100.0, true)
        engine?.stopAudioMixing()
        val result = engine?.startAudioMixing(filePath, loopBack, cycle)
        return (result == IRtcEngineEventHandler.ErrorCode.ERR_OK).also {
            if (it) {
                listener?.onAudioMixingFinished(soundId, false, soundSpeakerType)
            }
        }
    }

    override fun stopEffect(soundId: Int): Boolean {
        val result = engine?.stopAudioMixing()
        return result == IRtcEngineEventHandler.ErrorCode.ERR_OK
    }

    override fun pauseEffect(soundId: Int): Boolean {
        val result = engine?.pauseAudioMixing()
        return (result == IRtcEngineEventHandler.ErrorCode.ERR_OK)
    }

    override fun resumeEffect(soundId: Int): Boolean {
        val result = engine?.resumeAudioMixing()
        return result == IRtcEngineEventHandler.ErrorCode.ERR_OK
    }

    override fun stopAllEffect(): Boolean {
        val result = engine?.stopAudioMixing()
        return result == IRtcEngineEventHandler.ErrorCode.ERR_OK
    }

    override fun updateEffectVolume(volume: Int): Boolean {
        val result = engine?.adjustAudioMixingVolume(volume)
        return result == IRtcEngineEventHandler.ErrorCode.ERR_OK
    }
}