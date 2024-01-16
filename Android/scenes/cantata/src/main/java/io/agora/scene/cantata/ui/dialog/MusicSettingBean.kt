package io.agora.scene.cantata.ui.dialog

import io.agora.rtc2.Constants

interface EarPhoneCallback {
    fun onHasEarPhoneChanged(hasEarPhone: Boolean)
    fun onEarMonitorDelay(earsBackDelay: Int)
}

class MusicSettingBean constructor(
    var audioEffect: Int,
    private var isEar: Boolean,
    private var volMic: Int,
    private var volMusic: Int,
    private var toneValue: Int,
    private var mCallback: MusicSettingCallback
) {

    fun setAudioEffectPreset(audioEffect: Int) {
        this.audioEffect = audioEffect
        mCallback.onEffectChanged(audioEffect)
    }

    var beautifier = 0
        set(beautifier) {
            field = beautifier
            mCallback.onBeautifierPresetChanged(beautifier)
        }
    var audioEffectParams1 = 0
    var audioEffectParams2 = 0
    var remoteVolume = 30
        set(newValue) {
            field = newValue
            mCallback.onRemoteVolumeChanged(newValue)
        }

    val callback: MusicSettingCallback
        get() = mCallback

    fun isEar(): Boolean {
        return isEar
    }

    fun setEar(ear: Boolean) {
        isEar = ear
        mCallback.onEarChanged(ear)
    }

    fun getVolMic(): Int {
        return volMic
    }

    fun setVolMic(volMic: Int) {
        this.volMic = volMic
        mCallback.onMicVolChanged(volMic)
    }

    fun getVolMusic(): Int {
        return volMusic
    }

    fun setVolMusic(volMusic: Int) {
        this.volMusic = volMusic
        mCallback.onMusicVolChanged(volMusic)
    }

    fun setAudioEffectParameters(params1: Int, params2: Int) {
        audioEffectParams1 = params1
        audioEffectParams2 = params2
        mCallback.setAudioEffectParameters(params1, params2)
    }

    fun getToneValue(): Int {
        return toneValue
    }

    fun setToneValue(newToneValue: Int) {
        toneValue = newToneValue
        mCallback.onToneChanged(newToneValue)
    }

    var earBackVolume = 100 // 耳返音量
        set(value) {
            field = value
            mCallback.onEarBackVolumeChanged(value)
        }

    var earBackMode = 0 // 耳返模式：0(自动), 1(强制OpenSL), 2(强制Oboe)

    var hasEarPhone = false // 是否有耳机
        set(hasEarPhone) {
            field = hasEarPhone
            mEarPhoneCallback?.onHasEarPhoneChanged(hasEarPhone)
        }

    var earBackDelay = 0 // 耳返延迟
        set(value) {
            field = value
        }

    var mEarPhoneCallback: EarPhoneCallback? = null
        set(value) {
            field = value
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