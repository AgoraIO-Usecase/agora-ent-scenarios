package io.agora.scene.cantata.live.bean

import io.agora.rtc2.Constants
import io.agora.scene.cantata.live.fragmentdialog.EarPhoneCallback
import io.agora.scene.cantata.live.fragmentdialog.MusicSettingCallback

/**
 * Earback mode, 0(auto), 1(force OpenSL), 2(force Oboe)
 */
enum class EarBackMode(val value: Int) {
    Auto(0),
    OpenSL(1),
    Oboe(2),
}

/**
 * Console settings
 */
class MusicSettingBean constructor(

    private var mSettingCallback: MusicSettingCallback
) {

    companion object {
        const val DEFAULT_MIC_VOL = 100 // Default microphone volume 100
        const val DEFAULT_ACC_VOL = 50 // Default accompaniment volume 50
        const val DEFAULT_REMOTE_VOL = 30 // Default remote volume 30
    }

    var mEarPhoneCallback: EarPhoneCallback? = null

    /**
     * Earback switch
     */
    var mEarBackEnable: Boolean = false
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onEarChanged(newValue)
        }

    /**
     * Earback volume
     */
    var mEarBackVolume = 100
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onEarBackVolumeChanged(newValue)
        }

    /**
     * Earback mode: 0(auto), 1(force OpenSL), 2(force Oboe)
     */
    var mEarBackMode = EarBackMode.Auto
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onEarBackModeChanged(newValue.value)
        }

    /**
     * Whether there is a headphone
     */
    var mHasEarPhone = false
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mEarPhoneCallback?.onHasEarPhoneChanged(newValue)
        }

    /**
     * Earback delay
     */
    var mEarBackDelay = 0
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mEarPhoneCallback?.onEarMonitorDelay(newValue)
        }

    /**
     * Microphone volume
     */
    var mMicVolume = DEFAULT_MIC_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onMicVolChanged(newValue)
        }

    /**
     * Accompaniment volume
     */
    var mAccVolume = DEFAULT_ACC_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onAccVolChanged(newValue)
        }

    /**
     * Remote volume
     */
    var mRemoteVolume = DEFAULT_REMOTE_VOL
        set(newValue) {
            field = newValue
            mSettingCallback.onRemoteVolumeChanged(newValue)
        }

    /**
     * Audio effect, default chorus
     */
    var mAudioEffect: Int = Constants.ROOM_ACOUSTICS_CHORUS
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onAudioEffectChanged(newValue)
        }

    // ------------------ Audio effect adjustment ------------------
    fun getEffectIndex(index: Int): Int {
        when (index) {
            // Chorus
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
        // Chorus
        return Constants.ROOM_ACOUSTICS_CHORUS
    }
}