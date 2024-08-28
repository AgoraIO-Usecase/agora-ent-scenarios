package io.agora.scene.aichat.ext

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.base.utils.dp

val maxSwipeDistance = 80.dp // 限定最大侧滑距离

/**
 * Swipe to delete callback
 *
 * @property recyclerView
 * @property icon
 * @constructor
 *
 * @param swipeDirs
 */
class SwipeToDeleteCallback constructor(
    private val recyclerView: RecyclerView, private val icon: Drawable,
    swipeDirs: Int = ItemTouchHelper.LEFT
) : ItemTouchHelper.SimpleCallback(0, swipeDirs) {

    private var swipeBack = false

    private var currentItemViewHolder: RecyclerView.ViewHolder? = null
    private var iconRect: Rect? = null

    var onClickDeleteCallback: ((RecyclerView.ViewHolder) -> Unit)? = null

    init {
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            val gestureDetector = GestureDetector(recyclerView.context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return true
                }
            })

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (gestureDetector.onTouchEvent(e)) {
                    val currentItemViewHolder = currentItemViewHolder
                    val iconRect = iconRect

                    if (currentItemViewHolder != null && iconRect != null && iconRect.contains(e.x.toInt(), e.y.toInt())) {
                        onClickDeleteCallback?.invoke(currentItemViewHolder)
                        return true
                    } else {
                        // 没有侧滑状态，处理正常的 itemView 点击事件
                        clearCurrentSwipedView()
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    fun getCurrentItemViewHolder(): RecyclerView.ViewHolder? {
        return currentItemViewHolder
    }

    fun getIconRect(): Rect? {
        return iconRect
    }

    // 手动清除当前侧滑的 viewHolder 状态（用于在点击 itemView 时恢复状态）
    fun clearCurrentSwipedView() {
        currentItemViewHolder?.let {
            getDefaultUIUtil().clearView(it.itemView)
            currentItemViewHolder = null
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 1f
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return super.getMovementFlags(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        // 如果有当前的侧滑项，并且不是当前的 viewHolder，则将前一个复位
        if (currentItemViewHolder != null && currentItemViewHolder != viewHolder) {
            getDefaultUIUtil().clearView(currentItemViewHolder!!.itemView)
            currentItemViewHolder = null
        }
        // 设置新的侧滑项
        currentItemViewHolder = viewHolder

        val itemView = viewHolder.itemView

        val iconMargin = (itemView.height - icon.intrinsicHeight) / 2

        // 绘制删除图标
        val iconTop = itemView.top + iconMargin
        val iconBottom = iconTop + icon.intrinsicHeight
        val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
        val iconRight = itemView.right - iconMargin
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        icon.draw(c)

        // 保存当前ViewHolder和图标的点击区域
        currentItemViewHolder = viewHolder
        iconRect = Rect(iconLeft, iconTop, iconRight, iconBottom)

        // 确保视图滑动到固定距离
        val finalDX = if (dX < -maxSwipeDistance) -maxSwipeDistance else dX
        super.onChildDraw(c, recyclerView, viewHolder, finalDX, dY, actionState, isCurrentlyActive)
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX >= maxSwipeDistance / 2) {
            swipeBack = true
        }
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
