package io.agora.scene.cantata.ui.dialog

interface MusicSettingCallback {
    fun onEarChanged(isEar: Boolean)
    fun onMicVolChanged(vol: Int)
    fun onMusicVolChanged(vol: Int)
    fun onEffectChanged(effect: Int)
    fun onBeautifierPresetChanged(effect: Int)
    fun setAudioEffectParameters(param1: Int, param2: Int)
    fun onToneChanged(newToneValue: Int)
    fun onRemoteVolumeChanged(volume: Int)
    fun onEarBackVolumeChanged(volume: Int)
    fun onEnjoyingModeEnabled(enable: Boolean)
}