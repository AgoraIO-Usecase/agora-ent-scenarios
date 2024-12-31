package io.agora.scene.voice.ui.widget.barrage

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
import io.agora.scene.voice.R

class SubtitleLayout : RelativeLayout, StatusChangeListener {
    private var mWidth = 0
    private var mHeight = 0
    private lateinit var mBaseLayout: ConstraintLayout
    lateinit var mContent: SubtitleView
    private var mainHandler: Handler = Handler(Looper.getMainLooper())
    private var task: Runnable? = null
    private var listener: SubtitleStatusChangeListener? = null
    private var isRunning:Boolean = false
    private val contentList = mutableListOf<String>()
    private var content:String = ""

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
        val params: ViewGroup.LayoutParams = mBaseLayout.layoutParams
        params.height = mHeight
        params.width = mWidth
        requestLayout()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val view = LayoutInflater.from(context).inflate(R.layout.voice_subtitle_layout, this)
        mBaseLayout = view.findViewById(R.id.base_layout)
        mContent = view.findViewById(R.id.content)

        mContent.setTextColor(resources.getColor(io.agora.scene.widget.R.color.white))
        mContent.movementMethod = ScrollingMovementMethod.getInstance()
        mContent.animation = AnimationUtils.loadAnimation(context,R.anim.voice_subtitle_anim_enter)
        mContent.setSubtitleStatusChanged(this)
    }

    fun hideSubtitleView(){
        mBaseLayout.animation = AnimationUtils.loadAnimation(context,R.anim.voice_subtitle_anim_exit)
        mContent.text = ""
        mContent.visibility = GONE
        mBaseLayout.visibility = GONE
    }

    fun showSubtitleView(content:String){
        if (TextUtils.isEmpty(content)) return
        contentList.add(content)
        startTask()
    }

    override fun onShortSubtitleShow(textView: TextView) {
        listener?.onShortSubtitleShow(textView)
        Handler(Looper.getMainLooper()).postDelayed({
            stopTask()
        }, 3000)
    }

    override fun onLongSubtitleRollEnd(textView: TextView) {
        listener?.onLongSubtitleRollEnd(textView)
        stopTask()
    }

    interface SubtitleStatusChangeListener{
        /**
         * When the number of subtitles is less than the current line limit
         */
        fun onShortSubtitleShow(textView: TextView)

        /**
         * When the number of subtitles is longer than the current line limit and is fully displayed
         */
        fun onLongSubtitleRollEnd(textView: TextView)
    }

    fun setSubtitleStatusChangeListener(listener: SubtitleStatusChangeListener){
        this.listener = listener
    }

    private fun startTask() {
        if (!isRunning){
            handler.postDelayed(Runnable {
                if (contentList.size > 0){
                    content = contentList[0]
                    setSubtitle(content)
                    isRunning = true
                }
            }.also { task = it }, 1000)
        }
    }

    private fun stopTask() {
        hideSubtitleView()
        if (contentList.contains(content)){
            contentList.remove(content)
        }
        if (contentList.size == 0){
            task?.let {
                mainHandler.removeCallbacks(it)
                isRunning = false
            }
        }else{
            task?.let {
                isRunning = false
                handler.postDelayed(it, 1000)
            }
        }
    }

    private fun setSubtitle(content:String){
        mBaseLayout.animation = AnimationUtils.loadAnimation(context,R.anim.voice_subtitle_anim_enter)
        mBaseLayout.visibility = visibility
        mContent.text = content
        mContent.visibility = visibility
        mBaseLayout.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    fun clearTask(){
        task?.let {
            mainHandler.removeCallbacks(it)
            isRunning = false
        }
    }
}