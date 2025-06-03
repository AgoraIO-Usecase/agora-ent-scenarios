package io.agora.scene.showTo1v1.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.contains
import androidx.core.view.isVisible
import io.agora.scene.showTo1v1.databinding.ShowTo1v1DraggableViewBinding

class CallDraggableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

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

    fun setSmallType(small: Boolean) {
        binding.tvUserName.visibility = if (small) View.VISIBLE else View.INVISIBLE
        binding.llContainer.clipToOutline = small
    }

    fun setComeBackSoonViewStyle(isLocal: Boolean) {
        binding.comeSoonView.setComeBackSoonViewStyle(isLocal)
    }

    fun showComeBackSoonView(show: Boolean) {
        binding.comeSoonView.visibility = if (show) View.VISIBLE else View.GONE
    }

    val canvasContainer: ViewGroup
        get() { return binding.llContainer }

    fun canvasContainerAddView(view:View){
        if (!canvasContainer.contains(view)) {
            canvasContainer.addView(view)
        }
        canvasContainer.isVisible = true
    }

    fun setOnViewClick(action: (() -> Unit)?) {
        onViewClick = action
    }

    private fun setupDragAction() {
        setOnTouchListener(object: OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                Log.d(TAG, "draggabel view action $event)")
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Record the initial position when pressed
                        startX = event.rawX
                        startY = event.rawY

                        touchDownTime = System.currentTimeMillis()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Calculate offset
                        val offsetX = event.rawX - startX
                        val offsetY = event.rawY - startY

                        // Get parent view's width and height
                        val parent = parent as ViewGroup
                        val parentWidth = parent.width
                        val parentHeight = parent.height

                        // Get this view's width and height
                        val width = width
                        val height = height

                        // Calculate position after dragging, constrained within parent view
                        val left = (left + offsetX).coerceIn(0f, (parentWidth - width).toFloat())
                        val top = (top + offsetY).coerceIn(0f, (parentHeight - height).toFloat())
                        val right = left + width
                        val bottom = top + height

                        // Move this view to new position
                        layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

                        // Update initial position
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