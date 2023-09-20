package io.agora.scene.cantata.ui.dialog

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
        private set
    var audioEffectParams2 = 0
        private set
    var remoteVolume = 40
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
}