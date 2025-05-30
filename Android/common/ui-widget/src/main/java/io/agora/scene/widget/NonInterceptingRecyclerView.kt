package io.agora.scene.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class NonInterceptingRecyclerView constructor(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        // Return false to not intercept any touch events, letting them pass to child or parent views
        return false
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return false
    }
}
