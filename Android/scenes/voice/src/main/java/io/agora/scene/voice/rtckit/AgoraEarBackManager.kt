package io.agora.scene.voice.rtckit

import io.agora.rtc2.RtcEngineEx

data class AgoraEarBackParams (
    var isOn: Boolean = false,
    var volume: Int = 50,
    var mode: Int = 0,
){}

class AgoraEarBackManager(
    private val mRtcEngine: RtcEngineEx,
) {

    val params = AgoraEarBackParams()

    private val TAG: String = "EAR_BACK_MANAGER_LOG"

    fun setOn(isOn: Boolean) {
        params.isOn = isOn
        mRtcEngine.enableInEarMonitoring(isOn)
    }

    fun setVolume(value: Int) {
        params.volume = value
        mRtcEngine.setInEarMonitoringVolume(value)
    }

    fun setMode(mode: Int) {
        params.mode = mode
    }

}