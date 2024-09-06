package io.agora.scene.ktv.live.bean

import io.agora.rtc2.Constants
import io.agora.scene.ktv.live.fragmentdialog.EarPhoneCallback
import io.agora.scene.ktv.live.fragmentdialog.MusicSettingCallback

/**
 * A e c level
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
 * A i n s mode
 *
 * @property value
 * @constructor Create empty A i n s mode
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
         *若身份是主唱和伴唱，在演唱时，人声音量、伴泰音量保持原先设置，远端音量自动切为30
         *若身份是主唱和伴唱，演唱暂停/切歌，人声音量、伴奏音量保持原先设置，远端音量自动转为100
         *若身份为麦上主播（没有加入合唱但在麦上），人声音量、伴奏音量、远端音量均保持原先设置
         */
        const val DEFAULT_MIC_VOL = 100 // 默认人声音量100
        const val DEFAULT_ACC_VOL = 50 // 默认伴奏音量50
        const val DEFAULT_REMOTE_SINGER_VOL = 30  // 主唱/伴唱，演唱时默认远端音量30
        const val DEFAULT_REMOTE_VOL = 100  // 主唱/伴唱，非演唱时默认远端音量100
        const val DEFAULT_EAR_BACK_VOL = 100  // 默认耳返音量

        const val DEFAULT_AIAEC_STRENGTH = 1  // 默认aiaec, 强度为1
    }

    var mEarPhoneCallback: EarPhoneCallback? = null

    /**
     * 耳返开关
     */
    var mEarBackEnable: Boolean = false
        set(newValue) {
            field = newValue
            mSettingCallback.onEarChanged(newValue)
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
     * 耳返音量
     */
    var mEarBackVolume = DEFAULT_EAR_BACK_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onEarBackVolumeChanged(newValue)
        }

    /**
     * 是否有耳机
     */
    var mHasEarPhone = false
        set(newValue) {
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
     * 若身份是主唱和伴唱，在演唱时，人声音量、伴泰音量保持原先设置，远端音量自动切为30
     * 若身份是主唱和伴唱，演唱暂停/切歌，人声音量、伴奏音量保持原先设置，远端音量自动转为100
     * 若身份为麦上主播（没有加入合唱但在麦上），人声音量、伴奏音量、远端音量均保持原先设置
     */
    var mMicVolume = DEFAULT_MIC_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onMicVolChanged(newValue)
        }

    /**
     * 伴奏音量
     * 若身份是主唱和伴唱，在演唱时，人声音量、伴泰音量保持原先设置，远端音量自动切为30
     * 若身份是主唱和伴唱，演唱暂停/切歌，人声音量、伴奏音量保持原先设置，远端音量自动转为100
     * 若身份为麦上主播（没有加入合唱但在麦上），人声音量、伴奏音量、远端音量均保持原先设置
     */
    var mAccVolume = DEFAULT_ACC_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onAccVolChanged(newValue)
        }

    /**
     * 远端音量
     * 若身份是主唱和伴唱，在演唱时，人声音量、伴泰音量保持原先设置，远端音量自动切为30
     * 若身份是主唱和伴唱，演唱暂停/切歌，人声音量、伴奏音量保持原先设置，远端音量自动转为100
     * 若身份为麦上主播（没有加入合唱但在麦上），人声音量、伴奏音量、远端音量均保持原先设置
     */
    var mRemoteVolume = DEFAULT_REMOTE_VOL
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onRemoteVolChanged(newValue)
        }

    /**
     * 音效, 默认 ktv
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
     *  打分难度,低难度0，推荐难度15，高难度30
     *  仅在歌曲开始之前可设置，歌曲演唱期间不可切换
     */
    var mScoringDifficultyMode = ScoringDifficultyMode.Recommend
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onScoringDifficultyChanged(newValue.value)
        }

    /**
     * 专业模式
     */
    var mProfessionalModeEnable = false
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onProfessionalModeChanged(newValue)
        }

    /**
     * multiPath 开关
     */
    var mMultiPathEnable = true
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onMultiPathChanged(newValue)
        }

    /**
     * 音质 0(16K),1(24K),2(48K)
     */
    var mAecLevel = AECLevel.Standard
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onAECLevelChanged(newValue.value)
        }

    /**
     *  背景音降噪 0(关闭), 1(中), 2(高)
     */
    var mAinsMode = AINSMode.Close
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onAINSModeChanged(newValue.value)
        }

    /**
     * 低延时模式
     */
    var mLowLatencyMode = true
        set(newValue) {
            if (field == newValue) return
            field = newValue
            mSettingCallback.onLowLatencyModeChanged(newValue)
        }

    /**
     * AIAEC 开关
     */
    var mAIAECEnable = true
        set(newValue) {
            field = newValue
            mSettingCallback.onAIAECChanged(newValue)
        }

    /**
     * AIAEC 强度
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
        // 默认 ktv
        return Constants.ROOM_ACOUSTICS_KTV
    }
}