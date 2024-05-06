package io.agora.scene.joy.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class NonInterceptingRecyclerView constructor(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        // 返回false表示不拦截任何触摸事件，让它们传递到子视图或父视图
        return false
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return false
    }
}
