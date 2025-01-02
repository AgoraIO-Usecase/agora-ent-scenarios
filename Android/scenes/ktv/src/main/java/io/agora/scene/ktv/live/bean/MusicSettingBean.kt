package io.agora.scene.ktv.live.bean

import io.agora.rtc2.Constants
import io.agora.scene.ktv.live.fragmentdialog.EarPhoneCallback
import io.agora.scene.ktv.live.fragmentdialog.MusicSettingCallback

/**
 * Aec level
 *
 * @property value
 * @constructor Create empty A e c level
 */
enum class AECLevel(val value: Int) {
    /**
     * Standard
     *
     * @constructor Create empty Standard
     */
    Standard(0),

    /**
     * High
     *
     * @constructor Create empty High
     */
    High(1),

    /**
     * Ultra high
     *
     * @constructor Create empty Ultra high
     */
    UltraHigh(2),
}

/**
 * AINS mode
 *
 * @property value
 * @constructor Create empty AINS mode
 */
enum class AINSMode(val value: Int) {
    /**
     * Close
     *
     * @constructor Create empty Close
     */
    Close(0),

    /**
     * Medium
     *
     * @constructor Create empty Medium
     */
    Medium(1),

    /**
     * High
     *
     * @constructor Create empty High
     */
    High(2),
}

/**
 * Ear back mode
 *
 * @property value
 * @constructor Create empty Ear back mode
 */
enum class EarBackMode(val value: Int) {
    /**
     * Auto
     *
     * @constructor Create empty Auto
     */
    Auto(0),

    /**
     * Open s l
     *
     * @constructor Create empty Open s l
     */
    OpenSL(1),

    /**
     * Oboe
     *
     * @constructor Create empty Oboe
     */
    Oboe(2),
}

/**
 * Scoring difficulty mode
 *
 * @property value
 * @constructor Create empty Scoring difficulty mode
 */
enum class ScoringDifficultyMode(val value: Int) {
    /**
     * Low
     *
     * @constructor Create empty Low
     */
    Low(0),

    /**
     * Recommend
     *
     * @constructor Create empty Recommend
     */
    Recommend(15),

    /**
     * High
     *
     * @constructor Create empty High
     */
    High(30),
}

/**
 * Music setting bean
 *
 * @property mSettingCallback
 * @constructor Create empty Music setting bean
 */
class MusicSettingBean constructor(private val mSettingCallback: MusicSettingCallback) {

    companion object {
        /**
         * If the role is lead singer or chorus, during singing:
         * - Voice volume and accompaniment volume maintain original settings
         * - Remote volume automatically switches to 30
         * 
         * If the role is lead singer or chorus, when paused/switching songs:
         * - Voice volume and accompaniment volume maintain original settings
         * - Remote volume automatically switches to 100
         * 
         * If the role is on-stage host (not joined chorus but on stage):
         * - Voice volume, accompaniment volume and remote volume maintain original settings
         */
        const val DEFAULT_MIC_VOL = 100 // Default microphone volume 100
        const val DEFAULT_ACC_VOL = 50 // Default accompaniment volume 50
        const val DEFAULT_REMOTE_SINGER_VOL = 30  // Default remote volume 30 when singing (lead/chorus)
        const val DEFAULT_REMOTE_VOL = 100  // Default remote volume 100 when not singing (lead/chorus)
        const val DEFAULT_EAR_BACK_VOL = 100  // Default ear monitoring volume

        const val DEFAULT_AIAEC_STRENGTH = 1  // Default AIAEC strength level 1
    }

    var mEarPhoneCallback: EarPhoneCallback? = null

    /**
     * Ear monitoring switch
     */
    var mEarBackEnable: Boolean = false
        set(newValue) {
            field = newValue
            mSettingCallback.onEarChanged(newValue)
        }

    /**
     * Ear monitoring mode: 0(Auto), 1(Force OpenSL), 2(Force Oboe)
     */
    var mEarBackMode = EarBackMode.Auto
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onEarBackModeChanged(newValue.value)
        }

    /**
     * Ear monitoring volume
     */
    var mEarBackVolume = DEFAULT_EAR_BACK_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onEarBackVolumeChanged(newValue)
        }

    /**
     * Whether earphones are connected
     */
    var mHasEarPhone = false
        set(newValue) {
            field = newValue
            mEarPhoneCallback?.onHasEarPhoneChanged(newValue)
        }

    /**
     * Ear monitoring delay
     */
    var mEarBackDelay = 0
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mEarPhoneCallback?.onEarMonitorDelay(newValue)
        }

    /**
     * Voice volume
     * If the role is lead singer or chorus, during singing:
     * - Voice volume and accompaniment volume maintain original settings
     * - Remote volume automatically switches to 30
     * 
     * If the role is lead singer or chorus, when paused/switching songs:
     * - Voice volume and accompaniment volume maintain original settings
     * - Remote volume automatically switches to 100
     * 
     * If the role is on-stage host (not joined chorus but on stage):
     * - Voice volume, accompaniment volume and remote volume maintain original settings
     */
    var mMicVolume = DEFAULT_MIC_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onMicVolChanged(newValue)
        }

    /**
     * Accompaniment volume
     * If the role is lead singer or chorus, during singing:
     * - Voice volume and accompaniment volume maintain original settings
     * - Remote volume automatically switches to 30
     * 
     * If the role is lead singer or chorus, when paused/switching songs:
     * - Voice volume and accompaniment volume maintain original settings
     * - Remote volume automatically switches to 100
     * 
     * If the role is on-stage host (not joined chorus but on stage):
     * - Voice volume, accompaniment volume and remote volume maintain original settings
     */
    var mAccVolume = DEFAULT_ACC_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onAccVolChanged(newValue)
        }

    /**
     * Remote volume
     * If the role is lead singer or chorus, during singing:
     * - Voice volume and accompaniment volume maintain original settings
     * - Remote volume automatically switches to 30
     * 
     * If the role is lead singer or chorus, when paused/switching songs:
     * - Voice volume and accompaniment volume maintain original settings
     * - Remote volume automatically switches to 100
     * 
     * If the role is on-stage host (not joined chorus but on stage):
     * - Voice volume, accompaniment volume and remote volume maintain original settings
     */
    var mRemoteVolume = DEFAULT_REMOTE_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onRemoteVolChanged(newValue)
        }

    /**
     * Audio effect, default ktv
     */
    var mAudioEffect: Int = Constants.ROOM_ACOUSTICS_KTV
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onAudioEffectChanged(newValue)
        }

    /**
     * Update audio effect
     *
     * @param audioEffect
     */
    fun updateAudioEffect(audioEffect: Int) {
        this.mAudioEffect = audioEffect
    }

    /**
     * Scoring difficulty, low difficulty 0, recommended difficulty 15, high difficulty 30
     * Only settable before the song starts, not switchable during singing
     */
    var mScoringDifficultyMode = ScoringDifficultyMode.Recommend
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onScoringDifficultyChanged(newValue.value)
        }

    /**
     * Professional mode
     */
    var mProfessionalModeEnable = false
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onProfessionalModeChanged(newValue)
        }

    /**
     * MultiPath switch
     */
    var mMultiPathEnable = true
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onMultiPathChanged(newValue)
        }

    /**
     * Audio quality 0(16K),1(24K),2(48K)
     */
    var mAecLevel = AECLevel.Standard
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onAECLevelChanged(newValue.value)
        }

    /**
     * Background noise reduction mode
     * 0(Close), 1(Medium), 2(High)
     */
    var mAinsMode = AINSMode.Close
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onAINSModeChanged(newValue.value)
        }

    /**
     * Low latency mode
     */
    var mLowLatencyMode = true
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onLowLatencyModeChanged(newValue)
        }

    /**
     * AIAEC switch
     */
    var mAIAECEnable = true
        set(newValue) {
            field = newValue
            mSettingCallback.onAIAECChanged(newValue)
        }

    /**
     * AIAEC strength
     */
    var mAIAECStrength: Int = DEFAULT_AIAEC_STRENGTH
        set(newValue) {
            field = newValue
            mSettingCallback.onAIAECStrengthSelect(newValue)
        }

    /**
     * Get effect index
     *
     * @param index
     * @return
     */
    fun getEffectIndex(index: Int): Int {
        when (index) {
            0 -> return Constants.ROOM_ACOUSTICS_KTV
            1 -> return Constants.AUDIO_EFFECT_OFF
            2 -> return Constants.ROOM_ACOUSTICS_VOCAL_CONCERT
            3 -> return Constants.ROOM_ACOUSTICS_STUDIO
            4 -> return Constants.ROOM_ACOUSTICS_PHONOGRAPH
            5 -> return Constants.ROOM_ACOUSTICS_SPACIAL
            6 -> return Constants.ROOM_ACOUSTICS_ETHEREAL
            7 -> return Constants.STYLE_TRANSFORMATION_POPULAR
            8 -> return Constants.STYLE_TRANSFORMATION_RNB
        }
        // Default ktv
        return Constants.ROOM_ACOUSTICS_KTV
    }
}
