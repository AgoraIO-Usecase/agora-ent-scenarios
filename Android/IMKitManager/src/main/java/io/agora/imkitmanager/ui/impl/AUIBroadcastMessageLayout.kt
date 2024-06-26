package io.agora.imkitmanager.ui.impl

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import io.agora.imkitmanager.ui.listener.AUIStatusChangeListener
import io.agora.imkitmanager.R
import io.agora.imkitmanager.ui.IAUIChatListView

class AUIBroadcastMessageLayout : RelativeLayout, AUIStatusChangeListener, IAUIChatListView {
    //View宽度
    private var mWidth = 0

    //View高度
    private var mHeight = 0
    private lateinit var broadcastLayout: ConstraintLayout
    lateinit var broadcastView: AUIBroadcastMessageView
    private var mainHandler: Handler = Handler(Looper.getMainLooper())
    private var task: Runnable? = null
    private var listener: SubtitleStatusChangeListener? = null
    private var isRunning: Boolean = false
    private val contentList = mutableListOf<String>()
    private var content: String = ""
    private var delayMillis = 3000L

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(context, attrs)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        initLayout()
    }

    private fun initLayout() {
        val params: ViewGroup.LayoutParams = broadcastLayout.layoutParams
        params.height = mHeight
        params.width = mWidth
        requestLayout()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val view = LayoutInflater.from(context).inflate(R.layout.aui_broadcast_message_layout, this)
        broadcastLayout = view.findViewById(R.id.base_layout)
        broadcastView = view.findViewById(R.id.content)

        broadcastView.setTextColor(resources.getColor(R.color.aui_white))
        broadcastView.movementMethod = ScrollingMovementMethod.getInstance()
        broadcastView.animation = AnimationUtils.loadAnimation(context, R.anim.aui_subtitle_anim_enter)
        broadcastView.setSubtitleStatusChanged(this)
    }

    private fun hideSubtitleView() {
        broadcastLayout.animation = AnimationUtils.loadAnimation(context, R.anim.aui_subtitle_anim_exit)
        broadcastView.text = ""
        broadcastView.visibility = GONE
        broadcastLayout.visibility = GONE
    }

    override fun showSubtitleView(content: String) {
        if (TextUtils.isEmpty(content)) return
        contentList.add(content)
        startTask()
    }

    override fun onShortSubtitleShow(textView: TextView) {
        listener?.onShortSubtitleShow(textView)
        Handler(Looper.getMainLooper()).postDelayed({
            stopTask()
        }, delayMillis)
    }

    /**
     * 设置自动清理时间
     */
    fun setDelayMillis(delayMillis: Long) {
        this.delayMillis = delayMillis
    }

    override fun onLongSubtitleRollEnd(textView: TextView) {
        listener?.onLongSubtitleRollEnd(textView)
        stopTask()
    }

    override fun setScrollSpeed(speed: Int) {
        broadcastView.setScrollSpeed(speed)
    }

    interface SubtitleStatusChangeListener {
        /**
         * 当字幕数未超过当前行数限制完整展示后回调
         */
        fun onShortSubtitleShow(textView: TextView)

        /**
         * 当字幕数较长超出行数限制并完整展示后回调
         */
        fun onLongSubtitleRollEnd(textView: TextView)
    }

    override fun setSubtitleStatusChangeListener(listener: SubtitleStatusChangeListener) {
        this.listener = listener
    }

    // 开启任务
    private fun startTask() {
        if (!isRunning) {
            handler.postDelayed(Runnable {
                // 在这里执行具体的任务
                if (contentList.size > 0) {
                    content = contentList[0]
                    setSubtitle(content)
                    isRunning = true
                }
            }.also { task = it }, 1000)
        }
    }

    // 停止任务
    private fun stopTask() {
        hideSubtitleView()
        if (contentList.contains(content)) {
            contentList.remove(content)
        }
        if (contentList.size == 0) {
            task?.let {
                mainHandler.removeCallbacks(it)
                isRunning = false
            }
        } else {
            // 任务执行完后再次调用postDelayed开启下一次任务
            task?.let {
                isRunning = false
                handler.postDelayed(it, 1000)
            }
        }
    }

    private fun setSubtitle(content: String) {
        broadcastLayout.animation = AnimationUtils.loadAnimation(context, R.anim.aui_subtitle_anim_enter)
        broadcastLayout.visibility = visibility
        broadcastView.text = content
        broadcastView.visibility = visibility
        broadcastLayout.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    //销毁语聊房页面时调用 移除 task
    fun clearTask() {
        task?.let {
            mainHandler.removeCallbacks(it)
            isRunning = false
        }
    }
}