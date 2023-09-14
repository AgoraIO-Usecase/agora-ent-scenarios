package io.agora.scene.cantata.ui.widget.lrcView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import io.agora.scene.cantata.R

class GradeView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    private val mDefaultBackgroundRectF = RectF()
    private val mDefaultBackgroundPaint = Paint()
    private val mGradeSeparatorIndicatorPaint = Paint()
    private val mGradeSeparatorLabelIndicatorPaint = Paint()
    private var mWidth = 0f
    private var mHeight = 0f
    private val mCumulativeScoreBarRectF = RectF()
    private val mCumulativeScoreBarPaint = Paint()
    private var mCumulativeLinearGradient: LinearGradient? = null
    private var mCumulativeScore = 0
    private var mPerfectScore = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            mWidth = (right - left).toFloat()
            mHeight = (bottom - top).toFloat()
            mDefaultBackgroundRectF.top = 0f
            mDefaultBackgroundRectF.left = 0f
            mDefaultBackgroundRectF.right = mWidth
            mDefaultBackgroundRectF.bottom = mHeight
        }
    }

    private val isCanvasNotReady: Boolean
        private get() = mWidth <= 0 && mHeight <= 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isCanvasNotReady) { // Fail fast
            return
        }
        mGradeSeparatorIndicatorPaint.shader = null
        mGradeSeparatorIndicatorPaint.isAntiAlias = true
        mGradeSeparatorLabelIndicatorPaint.shader = null
        mGradeSeparatorLabelIndicatorPaint.isAntiAlias = true
        mGradeSeparatorLabelIndicatorPaint.textSize = 32f
        mGradeSeparatorLabelIndicatorPaint.style = Paint.Style.FILL
        mGradeSeparatorLabelIndicatorPaint.textAlign = Paint.Align.CENTER
        mDefaultBackgroundPaint.shader = null
        var colorOfBackground = 0
        var colorOfContentGray = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colorOfBackground = resources.getColor(
                R.color.cantata_grade_view_bg,
                null
            )
            colorOfContentGray = resources.getColor(R.color.cantata_grade_view_content_gray, null)
        } else {
            colorOfBackground = resources.getColor(R.color.cantata_grade_view_bg)
            colorOfContentGray = resources.getColor(R.color.cantata_grade_view_content_gray)
        }
        mDefaultBackgroundPaint.color = colorOfBackground
        mGradeSeparatorIndicatorPaint.color = colorOfContentGray
        mGradeSeparatorLabelIndicatorPaint.color = colorOfContentGray
        val fontMetrics = mGradeSeparatorLabelIndicatorPaint.fontMetrics
        val offsetForLabelX = mGradeSeparatorLabelIndicatorPaint.measureText("S")
        val baseLineForLabel = mHeight / 2 - fontMetrics.top / 2 - fontMetrics.bottom / 2
        mDefaultBackgroundPaint.isAntiAlias = true
        canvas.drawRoundRect(mDefaultBackgroundRectF, mHeight / 2, mHeight / 2, mDefaultBackgroundPaint)
        canvas.drawLine(
            (mWidth * xRadioOfGradeC),
            0f,
            (mWidth * xRadioOfGradeC),
            mHeight,
            mGradeSeparatorIndicatorPaint
        )
        canvas.drawText(
            "C",
            (mWidth * xRadioOfGradeC + offsetForLabelX),
            baseLineForLabel,
            mGradeSeparatorLabelIndicatorPaint
        )
        canvas.drawLine(
            (mWidth * xRadioOfGradeB),
            0f,
            (mWidth * xRadioOfGradeB),
            mHeight,
            mGradeSeparatorIndicatorPaint
        )
        canvas.drawText(
            "B",
            (mWidth * xRadioOfGradeB + offsetForLabelX),
            baseLineForLabel,
            mGradeSeparatorLabelIndicatorPaint
        )
        canvas.drawLine(
            (mWidth * xRadioOfGradeA),
            0f,
            (mWidth * xRadioOfGradeA),
            mHeight,
            mGradeSeparatorIndicatorPaint
        )
        canvas.drawText(
            "A",
            (mWidth * xRadioOfGradeA + offsetForLabelX),
            baseLineForLabel,
            mGradeSeparatorLabelIndicatorPaint
        )
        canvas.drawLine(
            (mWidth * xRadioOfGradeS),
            0f,
            (mWidth * xRadioOfGradeS),
            mHeight,
            mGradeSeparatorIndicatorPaint
        )
        canvas.drawText(
            "S",
            (mWidth * xRadioOfGradeS + offsetForLabelX),
            baseLineForLabel,
            mGradeSeparatorLabelIndicatorPaint
        )
        if (mCumulativeLinearGradient == null) {
            buildDefaultCumulativeScoreBarStyle(Color.parseColor("#FF99f5FF"), Color.parseColor("#FF1B6FFF"))
        }
        mCumulativeScoreBarPaint.shader = mCumulativeLinearGradient
        mCumulativeScoreBarPaint.isAntiAlias = true
        canvas.drawRoundRect(mCumulativeScoreBarRectF, mHeight / 2, mHeight / 2, mCumulativeScoreBarPaint)
    }

    fun setScore(score: Int, cumulativeScore: Int, perfectScore: Int) {
        mCumulativeScore = cumulativeScore
        mPerfectScore = perfectScore
        val startColor = Color.parseColor("#FF99F5FF")
        if (mCumulativeScore <= perfectScore * 0.1) {
            buildDefaultCumulativeScoreBarStyle(startColor, startColor)
        } else {
            val currentWidthOfScoreBar = mWidth * cumulativeScore / perfectScore
            val middleColor = Color.parseColor("#FF1B6FFF")
            mCumulativeLinearGradient =
                if (mCumulativeScore > perfectScore * 0.1 && mCumulativeScore < perfectScore * 0.8) {
                    LinearGradient(
                        0f,
                        0f,
                        currentWidthOfScoreBar,
                        mHeight,
                        startColor,
                        middleColor,
                        Shader.TileMode.CLAMP
                    )
                } else {
                    val endColor = Color.parseColor("#FFD598FF")
                    LinearGradient(
                        0f,
                        0f,
                        currentWidthOfScoreBar,
                        mHeight,
                        intArrayOf(startColor, middleColor, endColor),
                        null,
                        Shader.TileMode.CLAMP
                    )
                }
            mCumulativeScoreBarRectF.top = 0f
            mCumulativeScoreBarRectF.bottom = mHeight
            mCumulativeScoreBarRectF.left = 0f
            mCumulativeScoreBarRectF.right = currentWidthOfScoreBar
        }
        invalidate()
    }

    private fun buildDefaultCumulativeScoreBarStyle(fromColor: Int, toColor: Int) {
        if (mHeight <= 0) {
            return
        }
        mCumulativeLinearGradient = LinearGradient(0f, 0f, mHeight, mHeight, fromColor, toColor, Shader.TileMode.CLAMP)
        mCumulativeScoreBarRectF.top = 0f
        mCumulativeScoreBarRectF.bottom = mHeight
        mCumulativeScoreBarRectF.left = 0f
        mCumulativeScoreBarRectF.right = mHeight
    }

    companion object {
        /**
         * Separate this view by 5 parts(->C, C->B, B->A, A->S, S->)
         * 0.6, 0.7, 0.8, 0.9 by PRD
         */
        private const val xRadioOfGradeC = 0.6f
        private const val xRadioOfGradeB = 0.7f
        private const val xRadioOfGradeA = 0.8f
        private const val xRadioOfGradeS = 0.9f
    }
}