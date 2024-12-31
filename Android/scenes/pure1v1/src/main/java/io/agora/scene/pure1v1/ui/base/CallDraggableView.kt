package io.agora.scene.pure1v1.ui.base

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1DraggableViewBinding

class CallDraggableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val TAG = "CallDraggableView"
    private val binding: Pure1v1DraggableViewBinding
    private var startX = 0f
    private var startY = 0f

    private var onViewClick: (() -> Unit)? = null
    private var touchDownTime: Long = 0
    private val clickInterval = 150

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = Pure1v1DraggableViewBinding.inflate(inflater, this, true)
        setupDragAction()
    }

    fun setUserName(name: String) {
        binding.tvUserName.text = name
    }

    fun setSmallType(small: Boolean) {
        binding.tvUserName.visibility = if (small) View.VISIBLE else View.INVISIBLE
        binding.llContainer.clipToOutline = small
    }

    val canvasContainer: ViewGroup
        get() { return binding.llContainer }

    fun setOnViewClick(action: (() -> Unit)?) {
        onViewClick = action
    }

    fun setComeBackSoonViewStyle(isLocal: Boolean) {
        binding.comeSoonView.setComeBackSoonViewStyle(isLocal)
    }

    fun showComeBackSoonView(show: Boolean) {
        binding.comeSoonView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupDragAction() {
        setOnTouchListener(object: OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                Log.d(TAG, "draggabel view action $event)")
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Record the starting position of the press
                        startX = event.rawX
                        startY = event.rawY

                        touchDownTime = System.currentTimeMillis()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Calculate the offset
                        val offsetX = event.rawX - startX
                        val offsetY = event.rawY - startY

                        // Get the parent view's width and height
                        val parent = parent as ViewGroup
                        val parentWidth = parent.width
                        val parentHeight = parent.height

                        // Get the view's width and height
                        val width = width
                        val height = height

                        // Calculate the position after dragging, limited within the parent view
                        val left = (left + offsetX).coerceIn(0f, (parentWidth - width).toFloat())
                        val top = (top + offsetY).coerceIn(0f, (parentHeight - height).toFloat())
                        val right = left + width
                        val bottom = top + height

                        // Move the view to the new position
                        layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

                        // Update the starting position
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