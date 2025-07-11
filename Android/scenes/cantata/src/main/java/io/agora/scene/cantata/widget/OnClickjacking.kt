package io.agora.scene.cantata.widget

import android.util.Log
import android.view.View

private var lastClickTime: Long = 0
private const val clickDelay: Long = 1000

interface OnClickJackingListener : View.OnClickListener {

    override fun onClick(v: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= clickDelay) {
            lastClickTime = currentTime
            onClickJacking(v)
        } else {
            Log.d("OnClickJackingListener", "Click time is too short")
        }
    }

    abstract fun onClickJacking(view: View)
}