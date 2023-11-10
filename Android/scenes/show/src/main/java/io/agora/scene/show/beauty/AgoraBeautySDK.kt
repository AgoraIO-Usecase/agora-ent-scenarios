package io.agora.scene.show.beauty

import android.util.Log
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.BeautyOptions

object AgoraBeautySDK {
    private const val TAG = "AgoraBeautySDK"
    private var rtcEngine: RtcEngine? = null
    private var enable = false

    // 美颜配置
    val beautyConfig = BeautyConfig()

    fun initBeautySDK(rtcEngine: RtcEngine) {
        this.rtcEngine = rtcEngine
        rtcEngine.enableExtension("agora_video_filters_clear_vision", "clear_vision", true)
        beautyConfig.resume()
    }

    fun unInitBeautySDK() {
        rtcEngine?.setBeautyEffectOptions(false, beautyConfig.beautyOption)
        rtcEngine?.enableExtension("agora_video_filters_clear_vision", "clear_vision", false)
        rtcEngine = null
        beautyConfig.reset()
        enable = false
    }

    fun enable(enable: Boolean) {
        val rtc = rtcEngine ?: return
        rtc.setBeautyEffectOptions(enable, beautyConfig.beautyOption)
        this.enable = enable
    }

    class BeautyConfig {
        internal val beautyOption = BeautyOptions()

        // 磨皮
        var smooth: Float = beautyOption.smoothnessLevel
            set(value) {
                field = value
                beautyOption.smoothnessLevel = value
                rtcEngine?.setBeautyEffectOptions(enable, beautyOption)
            }

        // 美白
        var whiten: Float = beautyOption.lighteningLevel
            set(value) {
                field = value
                beautyOption.lighteningLevel = value
                rtcEngine?.setBeautyEffectOptions(enable, beautyOption)
            }

        // 红润
        var redden = beautyOption.rednessLevel
            set(value) {
                field = value
                beautyOption.rednessLevel = value
                rtcEngine?.setBeautyEffectOptions(enable, beautyOption)
            }

        // 锐化
        var sharpen = beautyOption.sharpnessLevel
            set(value) {
                field = value
                beautyOption.sharpnessLevel = value
                rtcEngine?.setBeautyEffectOptions(enable, beautyOption)
            }

        internal fun reset() {
            smooth = 0.5f
            whiten = 0.6f
            redden = 0.1f
            sharpen = 0.3f
        }

        internal fun resume() {
            try {
                val fields = this.javaClass.declaredFields
                fields.forEach {
                    val get = it.get(this)
                    if (get != null) {
                        it.isAccessible = true
                        it.set(this, get)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "BeautyConfig resume error.", e)
            }
        }
    }
}