package io.agora.voice.buddy.tool

import android.os.SystemClock
import android.view.View

/**
 * @author create by zhangwei03
 *
 * 防玻璃点击
 */
object FastClickTools {
    var lastClickTime = SystemClock.elapsedRealtime()

    var lastClickViewId = -1

    private const val FAST_CLICK_INTERVAL = 1000

    @JvmStatic
    fun isFastClick(view: View, interval: Int = FAST_CLICK_INTERVAL): Boolean {
        val currentClickTime = SystemClock.elapsedRealtime()
        var isFastClick = false
        if (lastClickViewId == view.id) {
            if (currentClickTime - lastClickTime < interval) {
                isFastClick = true
            }
        }
        lastClickViewId = view.id
        lastClickTime = currentClickTime
        return isFastClick
    }
}