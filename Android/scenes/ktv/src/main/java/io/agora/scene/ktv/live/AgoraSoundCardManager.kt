package io.agora.scene.ktv.live

import android.util.Log
import androidx.annotation.DrawableRes
import io.agora.rtc2.RtcEngineEx

data class PresetSoundModel constructor(
    val type: AgoraPresetSound,
    val name: String,
    val tips: String,
    @DrawableRes val resId: Int
) {
    override fun toString(): String {
        return name
    }
}

enum class AgoraPresetSound constructor(
    val gainValue: Float,
    val presetValue: Int,
    val gender: Int,
    val effect: Int,
) {
    Uncle(gainValue = 1.0f, presetValue = 4, gender = 0, effect = 2),
    Announcer(gainValue = 1.0f, presetValue = 4, gender = 1, effect = 2),
    Oba(gainValue = 1.0f, presetValue = 4, gender = 0, effect = 0),
    Lady(gainValue = 1.0f, presetValue = 4, gender = 1, effect = 0),
    Boy(gainValue = 1.0f, presetValue = 4, gender = 0, effect = 1),
    Sweet(gainValue = 1.0f, presetValue = 4, gender = 1, effect = 1),
    Close(gainValue = -1.0f, presetValue = -1, gender = -1, effect = -1)
}

class AgoraSoundCardManager constructor(private val rtcEngineEx: RtcEngineEx) {
    private var presetSound: AgoraPresetSound = AgoraPresetSound.Close
    private var isEnable: Boolean = false
    private var gainValue: Float = -1f
    private var presetValue: Int = -1
    private var gender: Int = -1
    private var effect: Int = -1

    private val tag: String = "AgoraSoundCardManager"

    fun isEnable(): Boolean = isEnable

    fun presetSound(): AgoraPresetSound = presetSound

    fun gainValue(): Float = gainValue

    fun presetValue(): Int = presetValue

    /**
     * 开启/关闭 虚拟声卡
     */
    fun enable(enable: Boolean, force: Boolean, callback: () -> Unit) {
        if (this.isEnable != enable || force) {
            this.isEnable = enable
            presetSound = if (isEnable) AgoraPresetSound.Uncle else AgoraPresetSound.Close

            gainValue = presetSound.gainValue
            presetValue = presetSound.presetValue
            gender = presetSound.gender
            effect = presetSound.effect
            setSoundCardParameters()
            callback.invoke()
            Log.d(tag, "enable $isEnable")
        }
    }

    // 设置预设音效
    fun setPresetSound(presetSound: AgoraPresetSound, callback: () -> Unit) {
        this.presetSound = presetSound
        gainValue = presetSound.gainValue
        presetValue = presetSound.presetValue
        gender = presetSound.gender
        effect = presetSound.effect
        setSoundCardParameters()
        callback.invoke()
        Log.d(tag, "setPresetSound $presetSound")
    }

    // 设置增益调节
    fun setGainValue(gainValue: Float) {
        this.gainValue = gainValue
        setSoundCardParameters()
        Log.d(tag, "setGainValue $gainValue")
    }

    // 预设值，麦克风类型
    fun setPresetValue(presetValue: Int) {
        this.presetValue = presetValue
        setSoundCardParameters()
        Log.d(tag, "setPresetValue $presetValue")
    }

    private fun setSoundCardParameters() {
        rtcEngineEx.setParameters("{\"che.audio.virtual_soundcard\":{\"preset\":$presetValue,\"gain\":$gainValue,\"gender\":$gender,\"effect\":$effect}}")
    }
}