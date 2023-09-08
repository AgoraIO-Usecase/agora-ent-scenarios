package io.agora.scene.showTo1v1.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import io.agora.scene.showTo1v1.databinding.ShowTo1v1DraggableViewBinding

class CallDraggableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val TAG = "CallDraggableView"
    private val binding: ShowTo1v1DraggableViewBinding
    private var startX = 0f
    private var startY = 0f

    private var onViewClick: (() -> Unit)? = null
    private var touchDownTime: Long = 0
    private val clickInterval = 150

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ShowTo1v1DraggableViewBinding.inflate(inflater, this, true)
        setupDragAction()
    }

    fun setUserName(name: String) {
        binding.tvUserName.text = name
    }

    val canvasContainer: ViewGroup
        get() { return binding.llContainer }

    fun setOnViewClick(action: (() -> Unit)) {
        onViewClick = action
    }

    private fun setupDragAction() {
        setOnTouchListener(object: OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                Log.d(TAG, "draggabel view action $event)")
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 记录按下的起始位置
                        startX = event.rawX
                        startY = event.rawY

                        touchDownTime = System.currentTimeMillis()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // 计算偏移量
                        val offsetX = event.rawX - startX
                        val offsetY = event.rawY - startY

                        // 获取父视图的宽高
                        val parent = parent as ViewGroup
                        val parentWidth = parent.width
                        val parentHeight = parent.height

                        // 获取本视图的宽高
                        val width = width
                        val height = height

                        // 计算拖拽后的位置，限制在父视图范围内
                        val left = (left + offsetX).coerceIn(0f, (parentWidth - width).toFloat())
                        val top = (top + offsetY).coerceIn(0f, (parentHeight - height).toFloat())
                        val right = left + width
                        val bottom = top + height

                        // 移动本视图到新的位置
                        layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

                        // 更新起始位置
                        startX = event.rawX
                        startY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - touchDownTime < clickInterval) {
                            onViewClick?.invoke()
                        }
                        return true
                    }
                    else -> {
                        return false
                    }
                }
                return true
            }
        })
    }
}