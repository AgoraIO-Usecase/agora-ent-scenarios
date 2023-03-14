package io.agora.scene.voice.ui.widget.barrage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import io.agora.scene.voice.R

class SubtitleView : AppCompatTextView{

    companion object {
        val SPEED_FAST = 9
        val SPEED_MEDIUM = 6
        val SPEED_SLOW = 3
    }

    //View宽度
    private var mViewWidth = 0
    //View高度
    private var mViewHeight = 0
    //偏移量
    private var mScrollX = 0F
    //字幕模型
    private var mMarqueeMode = 3
    private val rect = Rect()
    private var listener: StatusChangeListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(context, attrs)
    }

    override fun isFocused(): Boolean {
        return true
    }

    override fun onDraw(canvas: Canvas?) {

        val textContentText = text.toString().trim()
        if (TextUtils.isEmpty(textContentText)) {
            return
        }
        if (textContentText.length <= getLineMaxNumber(this,mViewWidth)){
            super.onDraw(canvas)
            listener?.onShortSubtitleShow(this)
        }else{
            val x = mViewWidth - mScrollX
            val y = mViewHeight / 2F + getTextContentHeight() / 2
            canvas?.drawText(textContentText, x, y, paint)
            mScrollX += mMarqueeMode
            if (mScrollX >= (mViewWidth + getTextContentWidth())) {
                mScrollX = 0F
                listener?.onLongSubtitleRollEnd(this)
            }
            invalidate()
        }
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.voice_SubtitleView)
        mMarqueeMode = typeArray.getInt(R.styleable.voice_SubtitleView_voice_customScrollSpeed,mMarqueeMode)
        typeArray.recycle()
    }

    fun setScrollSpeed(speed: Int) {
        if (speed == SPEED_FAST || speed == SPEED_MEDIUM || speed == SPEED_SLOW) {
            mMarqueeMode = speed
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewWidth = MeasureSpec.getSize(widthMeasureSpec)
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec)
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        paint.color = color
    }

    /**
     * 测量文字宽度
     * @return 文字宽度
     */
    private fun getTextContentWidth(): Int {
        val textContent = text.toString().trim()
        if (!TextUtils.isEmpty(textContent)) {
            paint.getTextBounds(textContent, 0, textContent.length, rect)
            return rect.width()
        }
        return 0
    }

    /**
     * 测量文字高度
     * @return 文字高度
     */
    private fun getTextContentHeight(): Int {
        val textContent = text.toString().trim()
        if (!TextUtils.isEmpty(textContent)) {
            paint.getTextBounds(textContent, 0, textContent.length, rect)
            return rect.height()
        }
        return 0
    }

    /**
     * 获取textView 一行最大显示字数
     */
    private fun getLineMaxNumber(textView:TextView,maxWidth:Int):Int{
        if (TextUtils.isEmpty(text)) return 0
        val s = StaticLayout(text,paint,maxWidth, Layout.Alignment.ALIGN_NORMAL,1.0f,0f,false)
        return  s.getLineEnd(0)
    }

    fun setSubtitleStatusChanged(listener: StatusChangeListener){
        this.listener = listener
    }
}