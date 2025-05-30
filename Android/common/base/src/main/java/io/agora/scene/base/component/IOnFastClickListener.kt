package io.agora.scene.base.component

import android.util.Log
import android.view.View

abstract class OnFastClickListener constructor(val delay:Long = 1000L):View.OnClickListener {

    private var lastClickTime: Long = 0L

    override fun onClick(v: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= delay) {
            // Execute click operation
            lastClickTime = currentTime
            onClickJacking(v)
        }else{
            Log.d("OnFastClickListener","Click time is too short")
        }
    }

    abstract fun onClickJacking(view: View)
}