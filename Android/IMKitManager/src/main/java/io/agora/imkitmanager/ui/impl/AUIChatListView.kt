package io.agora.imkitmanager.ui.impl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View.OnTouchListener
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import io.agora.imkitmanager.R
import io.agora.imkitmanager.databinding.AuiChatListLayoutBinding
import io.agora.imkitmanager.service.IAUIIMManagerService
import io.agora.imkitmanager.ui.AUIChatInfo
import io.agora.imkitmanager.ui.IAUIChatListView
import io.agora.imkitmanager.ui.listener.AUIChatListItemClickListener
import io.agora.imkitmanager.utils.DeviceTools


class AUIChatListView : RelativeLayout, IAUIChatListView {
    private val mViewBinding = AuiChatListLayoutBinding.inflate(LayoutInflater.from(context))
    private var listener: AUIChatListItemClickListener? = null
    private var isScrollBottom = false
    private var adapter: AUIChatListAdapter? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        addView(mViewBinding.root)

        initListener()
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        mViewBinding.listview.setStyledAttributes(context, attrs)

        val typedArray2 = context.obtainStyledAttributes(attrs, R.styleable.AUIChatListView, 0, 0)
        adapter = AUIChatListAdapter(context, typedArray2)
        val linearLayout = LinearLayoutManager(context)
        mViewBinding.listview.layoutManager = linearLayout

        //设置item 间距
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val drawable = GradientDrawable()
        drawable.setSize(0, DeviceTools.dp2px(context, 6f))
        itemDecoration.setDrawable(drawable)
        mViewBinding.listview.addItemDecoration(itemDecoration)
        mViewBinding.listview.adapter = adapter
    }

    fun setOwnerId(ownerId: String) {
        adapter?.setOwner(ownerId)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        mViewBinding.listview.setOnTouchListener(OnTouchListener { v, event ->
            listener?.onChatListViewClickListener()
            false
        })

        adapter?.setMessageViewListener(object : AUIChatListAdapter.MessageViewListener {
            override fun onItemClickListener(message: AUIChatInfo?) {
                listener?.onItemClickListener(message)
            }
        })
        mViewBinding.root.isActivated = false

        mViewBinding.listview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager?
                val lastVisibleItemPosition = lm!!.findLastVisibleItemPosition()
                val totalCount = lm.itemCount
                if (lastVisibleItemPosition == totalCount - 1) {
                    val findLastVisibleItemPosition = lm.findLastVisibleItemPosition()
                    if (findLastVisibleItemPosition == lm.itemCount - 1) {
                        isScrollBottom = true
                    }
                } else {
                    isScrollBottom = false
                }
            }
        })
    }

    override fun setChatListItemClickListener(listener: AUIChatListItemClickListener?) {
        this.listener = listener
    }

    override fun refresh(msgList: List<AUIChatInfo>) {
        if (adapter != null) {
            if (isScrollBottom) {
                refreshSelectLast(msgList)
            } else {
                post {
                    adapter?.refresh(msgList)
                }
            }
        }
    }

    override fun refreshSelectLast(msgList: List<AUIChatInfo>?) {
        if (adapter != null) {
            post {
                msgList?.let { adapter?.refresh(it) }
                val position = adapter?.itemCount
                if (position != null && position > 0) {
                    mViewBinding.listview.smoothScrollToPosition(position - 1)
                }
            }
        }
    }

    /**
     * 控制滑动速度的LinearLayoutManager
     */
    class ScrollSpeedLinearLayoutManger(private val context: Context) :
        LinearLayoutManager(context) {
        private var MILLISECONDS_PER_INCH = 0.03f
        override fun smoothScrollToPosition(
            recyclerView: RecyclerView,
            state: RecyclerView.State,
            position: Int
        ) {
            val linearSmoothScroller: LinearSmoothScroller =
                object : LinearSmoothScroller(recyclerView.context) {
                    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                        return this@ScrollSpeedLinearLayoutManger
                            .computeScrollVectorForPosition(targetPosition)
                    }

                    //This returns the milliseconds it takes to
                    //scroll one pixel.
                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                        return MILLISECONDS_PER_INCH / displayMetrics.density
                        //返回滑动一个pixel需要多少毫秒
                    }
                }
            linearSmoothScroller.targetPosition = position
            startSmoothScroll(linearSmoothScroller)
        }

        fun setSpeedSlow() {
            //自己在这里用density去乘，希望不同分辨率设备上滑动速度相同
            //0.3f是自己估摸的一个值，可以根据不同需求自己修改
            MILLISECONDS_PER_INCH = context.resources.displayMetrics.density * 0.3f
        }

        fun setSpeedFast() {
            MILLISECONDS_PER_INCH = context.resources.displayMetrics.density * 0.03f
        }
    }
}