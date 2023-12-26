package io.agora.scene.showTo1v1.ui.view

import android.util.Log
import android.view.View

abstract class OnClickJackingListener constructor(private val clickDelay: Long = 1000) :
    View.OnClickListener {

    private var lastClickTime: Long = 0
    override fun onClick(v: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= clickDelay) {
            // 执行点击操作
            lastClickTime = currentTime
            onClickJacking(v)
        }else{
            Log.d("OnClickJackingListener","Click time is too short")
        }
    }

    abstract fun onClickJacking(view: View)
}