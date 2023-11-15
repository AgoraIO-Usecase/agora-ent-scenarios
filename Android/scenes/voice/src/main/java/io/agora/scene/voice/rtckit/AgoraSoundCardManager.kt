package io.agora.scene.voice.rtckit

import android.util.Log
import io.agora.rtc2.RtcEngineEx
import io.agora.scene.voice.R

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

val AgoraPresetSound.titleStringID: Int
    get() = when (this) {
        AgoraPresetSound.Sound1001 -> R.string.voice_preset_sound_qingshu1
        AgoraPresetSound.Sound1002 -> R.string.voice_preset_sound_shaoyu1
        AgoraPresetSound.Sound1003 -> R.string.voice_preset_sound_qingnian1
        AgoraPresetSound.Sound1004 -> R.string.voice_preset_sound_shaoluo
        AgoraPresetSound.Sound2001 -> R.string.voice_preset_sound_dashu
        AgoraPresetSound.Sound2002 -> R.string.voice_preset_sound_mum
        AgoraPresetSound.Sound2003 -> R.string.voice_preset_sound_qingshu
        AgoraPresetSound.Sound2004 -> R.string.voice_preset_sound_yuma
        AgoraPresetSound.Sound2005 -> R.string.voice_preset_sound_qingnian
        AgoraPresetSound.Sound2006 -> R.string.voice_preset_sound_shaoyu
        else -> 0
    }

val AgoraPresetSound.infoStringID: Int
    get() = when (this) {
        AgoraPresetSound.Sound1001 -> R.string.voice_preset_sound_qingshu1_tips
        AgoraPresetSound.Sound1002 -> R.string.voice_preset_sound_shaoyu1_tips
        AgoraPresetSound.Sound1003 -> R.string.voice_preset_sound_qingnian1_tips
        AgoraPresetSound.Sound1004 -> R.string.voice_preset_sound_shaoluo_tips
        AgoraPresetSound.Sound2001 -> R.string.voice_preset_sound_dashu_tips
        AgoraPresetSound.Sound2002 -> R.string.voice_preset_sound_mum_tips
        AgoraPresetSound.Sound2003 -> R.string.voice_preset_sound_qingshu_tips
        AgoraPresetSound.Sound2004 -> R.string.voice_preset_sound_yuma_tips
        AgoraPresetSound.Sound2005 -> R.string.voice_preset_sound_qingnian_tips
        AgoraPresetSound.Sound2006 -> R.string.voice_preset_sound_shaoyu_tips
        else -> 0
    }

val AgoraPresetSound.resID: Int
    get() = when (this) {
        AgoraPresetSound.Sound1001 -> R.drawable.voice_ic_sound_card_1001
        AgoraPresetSound.Sound1002 -> R.drawable.voice_ic_sound_card_1002
        AgoraPresetSound.Sound1003 -> R.drawable.voice_ic_sound_card_1003
        AgoraPresetSound.Sound1004 -> R.drawable.voice_ic_sound_card_1004
        AgoraPresetSound.Sound2001 -> R.drawable.voice_ic_sound_card_2001
        AgoraPresetSound.Sound2002 -> R.drawable.voice_ic_sound_card_2002
        AgoraPresetSound.Sound2003 -> R.drawable.voice_ic_sound_card_2003
        AgoraPresetSound.Sound2004 -> R.drawable.voice_ic_sound_card_2004
        AgoraPresetSound.Sound2005 -> R.drawable.voice_ic_sound_card_2005
        AgoraPresetSound.Sound2006 -> R.drawable.voice_ic_sound_card_2006
        else -> 0
    }

class AgoraSoundCardManager constructor(private val rtcEngineEx: RtcEngineEx) {

    private var presetSound: AgoraPresetSound = AgoraPresetSound.Close
    private var isEnable: Boolean = false
    private var gainValue: Float = -1f
    private var presetValue: Int = -1
    private var gender: Int = -1
    private var effect: Int = -1
    private var isForbidden = true

    private val tag: String = "AgoraSoundCardManager"

    fun isEnable(): Boolean = isEnable

    fun presetSound(): AgoraPresetSound = presetSound

    fun gainValue(): Float = gainValue

    fun presetValue(): Int = presetValue

    fun isForbidden(): Boolean = isForbidden

    fun setForbidden(isOn: Boolean) {
        if (isForbidden != isOn) {
            isForbidden = isOn
        }
    }

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