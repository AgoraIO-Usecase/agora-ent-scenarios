package io.agora.scene.cantata.live.bean

import io.agora.rtc2.Constants
import io.agora.scene.cantata.live.fragmentdialog.EarPhoneCallback
import io.agora.scene.cantata.live.fragmentdialog.MusicSettingCallback

/**
 * 耳返模式，0(自动), 1(强制OpenSL), 2(强制Oboe)
 */
enum class EarBackMode(val value: Int) {
    Auto(0),
    OpenSL(1),
    Oboe(2),
}

/**
 * 控制台设置
 */
class MusicSettingBean constructor(

    private var mSettingCallback: MusicSettingCallback
) {

    companion object {
        const val DEFAULT_MIC_VOL = 100 // 默认人声音量100
        const val DEFAULT_ACC_VOL = 50 // 默认伴奏音量50
        const val DEFAULT_REMOTE_VOL = 30 // 默认远端音量 30
    }

    var mEarPhoneCallback: EarPhoneCallback? = null

    /**
     * 耳返开关
     */
    var mEarBackEnable: Boolean = false
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onEarChanged(newValue)
        }

    /**
     * 耳返音量
     */
    var mEarBackVolume = 100
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onEarBackVolumeChanged(newValue)
        }

    /**
     * 耳返模式：0(自动), 1(强制OpenSL), 2(强制Oboe)
     */
    var mEarBackMode = EarBackMode.Auto
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onEarBackModeChanged(newValue.value)
        }

    /**
     * 是否有耳机
     */
    var mHasEarPhone = false
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mEarPhoneCallback?.onHasEarPhoneChanged(newValue)
        }

    /**
     * 耳返延迟
     */
    var mEarBackDelay = 0
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mEarPhoneCallback?.onEarMonitorDelay(newValue)
        }

    /**
     * 人声音量
     */
    var mMicVolume = DEFAULT_MIC_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onMicVolChanged(newValue)
        }

    /**
     * 伴奏音量
     */
    var mAccVolume = DEFAULT_ACC_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onAccVolChanged(newValue)
        }

    /**
     *  远端音量
     */
    var mRemoteVolume = DEFAULT_REMOTE_VOL
        set(newValue) {
            field = newValue
            mSettingCallback.onRemoteVolumeChanged(newValue)
        }

    /**
     * 音效, 默认 大合唱
     */
    var mAudioEffect: Int = Constants.ROOM_ACOUSTICS_CHORUS
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onAudioEffectChanged(newValue)
        }

    // ------------------ 音效调整 ------------------
    fun getEffectIndex(index: Int): Int {
        when (index) {
            // 大合唱
            0 -> return Constants.ROOM_ACOUSTICS_CHORUS
            1 -> return Constants.AUDIO_EFFECT_OFF
            2 -> return Constants.ROOM_ACOUSTICS_KTV
            3 -> return Constants.ROOM_ACOUSTICS_VOCAL_CONCERT
            4 -> return Constants.ROOM_ACOUSTICS_STUDIO
            5 -> return Constants.ROOM_ACOUSTICS_PHONOGRAPH
            6 -> return Constants.ROOM_ACOUSTICS_SPACIAL
            7 -> return Constants.ROOM_ACOUSTICS_ETHEREAL
            8 -> return Constants.STYLE_TRANSFORMATION_POPULAR
            9 -> return Constants.STYLE_TRANSFORMATION_RNB
        }
        // 大合唱
        return Constants.ROOM_ACOUSTICS_CHORUS
    }
}