package io.agora.scene.aichat.imkit.widget.messageLayout

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

@SuppressLint("WrongConstant")
class EaseCustomLayoutManager @JvmOverloads constructor(
    private val context: Context,
    private val orientation: Int = VERTICAL,
    private val reverseLayout: Boolean = false
) : LinearLayoutManager(context, orientation, reverseLayout) {
    private var isNeedStackFromEnd = false
    private var maxViewHeight = 0

    /**
     * Whether need to set stack from end
     * @param isStackFromEnd
     */
    fun setIsStackFromEnd(isStackFromEnd: Boolean) {
        isNeedStackFromEnd = isStackFromEnd
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
        if (!isNeedStackFromEnd) {
            return
        }
        val itemCount = itemCount
        var totalHeight = 0
        for (i in 0 until itemCount) {
            val subView = findViewByPosition(i)
            if (subView != null) {
                val measuredHeight = subView.measuredHeight
                val paddingBottom = subView.paddingBottom
                val paddingTop = subView.paddingTop
                val itemHeight = measuredHeight + paddingBottom + paddingTop
                totalHeight += itemHeight
                // Not add the marginTop and marginBottom
            }
        }
        if (totalHeight == 0 || height == 0) {
            if (itemCount >= MIN_STACK_FROM_END_COUNT) {
                if (!stackFromEnd) {
                    stackFromEnd = true
                }
            } else {
                if (stackFromEnd) {
                    stackFromEnd = false
                }
            }
            return
        }
        maxViewHeight = Math.max(maxViewHeight, height)
        if (totalHeight < maxViewHeight) {
            if (stackFromEnd) {
                stackFromEnd = false
            }
        } else if (!stackFromEnd) {
            stackFromEnd = true
        }
    }

    companion object {
        private const val MIN_STACK_FROM_END_COUNT = 10
    }
}