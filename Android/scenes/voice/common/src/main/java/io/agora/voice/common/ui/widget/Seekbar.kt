package io.agora.voice.common.utils

import android.widget.SeekBar

/**
 * @author create by zhangwei03
 *
 *
 */
public inline fun SeekBar.doOnProgressChanged(
    crossinline action: (seekBar: SeekBar?, progress: Int, fromUser: Boolean) -> Unit
): SeekBar.OnSeekBarChangeListener = setOnSeekBarChangeListener(onProgressChanged = action)

public inline fun SeekBar.onStartTrackingTouch(
    crossinline action: (seekBar: SeekBar?) -> Unit
): SeekBar.OnSeekBarChangeListener = setOnSeekBarChangeListener(onStartTrackingTouch = action)

public inline fun SeekBar.onStopTrackingTouch(
    crossinline action: (seekBar: SeekBar?) -> Unit
): SeekBar.OnSeekBarChangeListener = setOnSeekBarChangeListener(onStopTrackingTouch = action)

public inline fun SeekBar.setOnSeekBarChangeListener(
    crossinline onProgressChanged: (seekBar: SeekBar?, progress: Int, fromUser: Boolean) -> Unit = { _, _, _ -> },
    crossinline onStartTrackingTouch: (seekBar: SeekBar?) -> Unit = { },
    crossinline onStopTrackingTouch: (seekBar: SeekBar?) -> Unit = {}
): SeekBar.OnSeekBarChangeListener {
    val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onProgressChanged.invoke(seekBar, progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            onStartTrackingTouch.invoke(seekBar)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            onStopTrackingTouch.invoke(seekBar)
        }

    }
    setOnSeekBarChangeListener(seekBarChangeListener)
    return seekBarChangeListener
}