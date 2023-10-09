package io.agora.scene.cantata.ui.dialog

interface EarPhoneCallback {
    fun onHasEarPhoneChanged(hasEarPhone: Boolean)
    fun onEarMonitorDelay(earsBackDelay: Int)
}

class MusicSettingBean constructor(
    private var isEar: Boolean,
    private var volMic: Int,
    private var volMusic: Int,
    private var toneValue: Int,
    private var mCallback: MusicSettingCallback
) {
    var effect = 0
        set(effect) {
            field = effect
            mCallback.onEffectChanged(effect)
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

    var earBackMode = 0 // 耳返模式：0(自动), 1(强制OpenSL), 2(强制Oboe)

    var hasEarPhone = false // 是否有耳机
        set(hasEarPhone) {
            field = hasEarPhone
            mEarPhoneCallback?.onHasEarPhoneChanged(hasEarPhone)
        }

    var earBackDelay = 0 // 耳返延迟
        set(value) {
            field = value
            mCallback.onEarBackVolumeChanged(value)
        }

    var mEarPhoneCallback: EarPhoneCallback? = null
        set(value) {
            field = value
        }

    var enjoyingMode: Boolean = false
        set(value) {
            field = value
            mCallback.onEnjoyingModeEnabled(value)
        }
}