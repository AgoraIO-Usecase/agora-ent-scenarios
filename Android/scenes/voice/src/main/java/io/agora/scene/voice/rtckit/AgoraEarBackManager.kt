package io.agora.scene.voice.rtckit

import io.agora.rtc2.RtcEngineEx
import io.agora.scene.voice.R

enum class AgoraEarBackMode {
    Default,
    OpenSL,
    Oboe
}
data class AgoraEarBackParams (
    var isOn: Boolean = false,
    var volume: Int = 100,
    var mode: AgoraEarBackMode = AgoraEarBackMode.Default,
    var delay: Int = 0,
){}

class AgoraEarBackManager(
    private val mRtcEngine: RtcEngineEx,
) {

    private val TAG: String = "EAR_BACK_MANAGER_LOG"

    var mOnEarBackDelayChanged: ((Int) -> Unit)? = null

    val params = AgoraEarBackParams()

    fun setOn(isOn: Boolean) {
        params.isOn = isOn
        mRtcEngine.enableInEarMonitoring(isOn)
    }

    fun setVolume(value: Int) {
        params.volume = value
        mRtcEngine.setInEarMonitoringVolume(value)
    }

    fun setOnEarBackDelayChanged(action: ((Int) -> Unit)?) {
        mOnEarBackDelayChanged = action
    }

    fun setMode(mode: AgoraEarBackMode) {
        if (params.mode == mode) {
            return
        }
        params.mode = mode
        when (mode) {
            AgoraEarBackMode.Default -> {
                mRtcEngine.setParameters("{\"che.audio.opensl.mode\":0}")
            }
            AgoraEarBackMode.OpenSL -> {
                mRtcEngine.setParameters("{\"che.audio.opensl.mode\":0}")
            }
            AgoraEarBackMode.Oboe -> {
                mRtcEngine.setParameters("{\"che.audio.oboe.enable\":true}")
            }
        }
    }

    fun updateDelay(value: Int) {
        if (params.delay != value) {
            params.delay = value
            mOnEarBackDelayChanged?.invoke(value)
        }
    }
}