package io.agora.scene.ktv.live.fragment.dialog

import android.util.Log
import io.agora.scene.ktv.live.AgoraPresetSound

class SoundCardSettingBean constructor(private var parameterCallback: ((preset: Int, gain: Float, gender: Int, effect: Int) -> Unit)) {

    private var presetSound: AgoraPresetSound = AgoraPresetSound.Close
    private var isEnable: Boolean = false
    private var gainValue: Float = -1f
    private var presetValue: Int = -1
    private var gender: Int = -1
    private var effect: Int = -1

    private var mEarPhoneCallback: EarPhoneCallback? = null

    fun setEarPhoneCallback(earPhoneCallback: EarPhoneCallback) {
        mEarPhoneCallback = earPhoneCallback
    }

    fun setHasEarPhone(hasEarPhone: Boolean) {
        mEarPhoneCallback?.onHasEarPhoneChanged(hasEarPhone)
    }

    private val tag: String = "SoundCardSettingBean"

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
        parameterCallback.invoke(presetValue, gainValue, gender, effect)
    }
}

interface EarPhoneCallback {
    fun onHasEarPhoneChanged(hasEarPhone: Boolean)
}