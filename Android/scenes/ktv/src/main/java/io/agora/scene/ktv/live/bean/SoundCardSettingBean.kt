package io.agora.scene.ktv.live.bean

import android.util.Log
import io.agora.scene.ktv.live.fragmentdialog.AgoraPresetSound

/**
 * Sound card setting bean
 *
 * @property parameterCallback
 * @constructor Create empty Sound card setting bean
 */
class SoundCardSettingBean constructor(private var parameterCallback: ((preset: Int, gain: Float, gender: Int, effect: Int) -> Unit)) {

    private var presetSound: AgoraPresetSound = AgoraPresetSound.Close
    private var isEnable: Boolean = false
    private var gainValue: Float = -1f
    private var presetValue: Int = -1
    private var gender: Int = -1
    private var effect: Int = -1


    private val tag: String = "SoundCardSettingBean"

    /**
     * Is enable
     *
     * @return
     */
    fun isEnable(): Boolean = isEnable

    /**
     * Preset sound
     *
     * @return
     */
    fun presetSound(): AgoraPresetSound = presetSound

    /**
     * Gain value
     *
     * @return
     */
    fun gainValue(): Float = gainValue

    /**
     * Preset value
     *
     * @return
     */
    fun presetValue(): Int = presetValue

    /**
     * Enable
     *
     * @param enable
     * @param force
     * @param callback
     * @receiver
     */
    fun enable(enable: Boolean, force: Boolean, callback: () -> Unit) {
        if (this.isEnable != enable || force) {
            this.isEnable = enable
            presetSound = if (isEnable) AgoraPresetSound.Sound2001 else AgoraPresetSound.Close
            gainValue = presetSound.gainValue
            presetValue = presetSound.presetValue
            gender = presetSound.gender
            effect = presetSound.effect
            setSoundCardParameters()
            callback.invoke()
            Log.d(tag, "enable $isEnable")
        }
    }

    /**
     * Set preset sound
     *
     * @param presetSound
     * @param callback
     * @receiver
     */// 设置预设音效
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

    /**
     * Set gain value
     *
     * @param gainValue
     */// 设置增益调节
    fun setGainValue(gainValue: Float) {
        this.gainValue = gainValue
        setSoundCardParameters()
        Log.d(tag, "setGainValue $gainValue")
    }

    /**
     * Set preset value
     *
     * @param presetValue
     */// 预设值，麦克风类型
    fun setPresetValue(presetValue: Int) {
        this.presetValue = presetValue
        setSoundCardParameters()
        Log.d(tag, "setPresetValue $presetValue")
    }

    private fun setSoundCardParameters() {
        parameterCallback.invoke(presetValue, gainValue, gender, effect)
    }
}