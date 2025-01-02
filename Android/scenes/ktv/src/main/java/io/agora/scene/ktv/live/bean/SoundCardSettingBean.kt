package io.agora.scene.ktv.live.bean

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
        }
    }

    /**
     * Set preset sound
     *
     * @param presetSound
     * @param callback
     * @receiver
     */
    fun setPresetSound(presetSound: AgoraPresetSound, callback: () -> Unit) {
        this.presetSound = presetSound
        gainValue = presetSound.gainValue
        presetValue = presetSound.presetValue
        gender = presetSound.gender
        effect = presetSound.effect
        setSoundCardParameters()
        callback.invoke()
    }

    /**
     * Set gain value
     *
     * @param gainValue
     */
    fun setGainValue(gainValue: Float) {
        this.gainValue = gainValue
        setSoundCardParameters()
    }

    /**
     * Set preset value
     *
     * @param presetValue
     */
    fun setPresetValue(presetValue: Int) {
        this.presetValue = presetValue
        setSoundCardParameters()
    }

    private fun setSoundCardParameters() {
        parameterCallback.invoke(presetValue, gainValue, gender, effect)
    }
}