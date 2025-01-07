package io.agora.scene.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import io.agora.scene.base.utils.dp

class CustomVolumeSeekBarView : View {

    private val totalPitch = 11

    /**
     * selected pitch
     */
    @JvmField
    var currentPitch: Int = 5

    private val paddingBottom = 3.dp

    private val mSelectBlueColor = ContextCompat.getColor(context, R.color.blue_9F)
    private val mUnSelectBlueColor = ContextCompat.getColor(context, R.color.white)

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)


    constructor(context: Context?) : super(context) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        mPaint.strokeWidth = 3.dp
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawStartLine(canvas)
    }

    private fun drawStartLine(canvas: Canvas) {
        for (i in 0 until totalPitch) {
            if (i <= currentPitch) {
                mPaint.color = mSelectBlueColor
            } else {
                mPaint.color = mUnSelectBlueColor
            }
            val left = i * 17.dp
            val top = bottom - paddingBottom - ((i + 1) * 2).dp
            val right = left + paddingBottom
            val bottom = bottom - paddingBottom
            canvas.drawRect(left, top, right, bottom.toFloat(), mPaint)
        }
    }

    fun currentPitchPlus() {
        if (currentPitch == 11) {
            return
        }
        currentPitch++
        invalidate()
    }

    fun currentPitchMinus() {
        if (currentPitch < 0) {
            return
        }
        currentPitch--
        invalidate()
    }
}
