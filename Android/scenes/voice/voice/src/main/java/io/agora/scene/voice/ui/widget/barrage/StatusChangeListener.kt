package io.agora.scene.voice.ui.widget.barrage

import android.widget.TextView

interface StatusChangeListener {
    /**
     * 当字幕数未超过当前行数限制时回调
     */
    fun onShortSubtitleShow(textView: TextView)

    /**
     * 当字幕数较长超出行数限制并完整展示后回调
     */
    fun onLongSubtitleRollEnd(textView: TextView)
}