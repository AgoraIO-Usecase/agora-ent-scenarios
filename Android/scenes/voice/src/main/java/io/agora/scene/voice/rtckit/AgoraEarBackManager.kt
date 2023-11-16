package io.agora.scene.voice.rtckit

import android.util.Log
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngineEx

enum class AgoraEarBackMode {
    Default,
    OpenSL,
    Oboe
}
data class AgoraEarBackParams (
    var isOn: Boolean = false,
    var isForbidden: Boolean = true,// 下麦时禁用耳返
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
        if (params.isOn != isOn) {
            params.isOn = isOn
            updateEnableInEarMonitoring()
        }
    }
    fun setForbidden(isOn: Boolean) {
        if (params.isForbidden != isOn) {
            params.isForbidden = isOn
            updateEnableInEarMonitoring()
        }
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
            Log.d(TAG, "ear back delay: $value")
            mOnEarBackDelayChanged?.invoke(value)
        }
    }

    fun updateEnableInEarMonitoring() {
        if (!params.isForbidden && params.isOn) {
            mRtcEngine.enableInEarMonitoring(true, Constants.EAR_MONITORING_FILTER_BUILT_IN_AUDIO_FILTERS or Constants.EAR_MONITORING_FILTER_NOISE_SUPPRESSION)
        } else {
            mRtcEngine.enableInEarMonitoring(false)
        }
    }
}