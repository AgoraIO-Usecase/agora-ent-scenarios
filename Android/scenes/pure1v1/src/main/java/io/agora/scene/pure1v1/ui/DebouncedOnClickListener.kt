package io.agora.scene.pure1v1.ui

import android.os.SystemClock
import android.view.View

class DebouncedOnClickListener(private val intervalMillis: Long = 1000L, private val onClickListener: View.OnClickListener) : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime < intervalMillis) {
            return
        }

        lastClickTime = currentTime
        onClickListener.onClick(v)
    }
}