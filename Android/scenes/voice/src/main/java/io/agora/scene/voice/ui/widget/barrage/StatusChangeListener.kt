package io.agora.scene.voice.ui.widget.barrage

import android.widget.TextView

interface StatusChangeListener {
    /**
     * Callback when subtitle length does not exceed current line limit
     */
    fun onShortSubtitleShow(textView: TextView)

    /**
     * Callback when subtitle is long (exceeds line limit) and has finished displaying completely
     */
    fun onLongSubtitleRollEnd(textView: TextView)
}