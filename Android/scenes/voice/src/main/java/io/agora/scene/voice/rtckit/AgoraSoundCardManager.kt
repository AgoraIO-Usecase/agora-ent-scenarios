package io.agora.scene.voice.rtckit

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
    val presetValue: Int,
    val gainValue: Float,
    val gender: Int,
    val effect: Int,
) {
    Close(-1,-1f,-1,-1),
    Sound1001(4,1f,0,0),
    Sound1002(4,1f,0,1),
    Sound1003(4,1f,1,0),
    Sound1004(4,1f,1,1),
    Sound2001(4,1f,0,2),
    Sound2002(4,1f,1,2),
    Sound2003(4,1f,0,3),
    Sound2004(4,1f,1,3),
    Sound2005(4,1f,0,4),
    Sound2006(4,1f,1,4)
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
            presetSound = if (isEnable) AgoraPresetSound.Sound1001 else AgoraPresetSound.Close
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