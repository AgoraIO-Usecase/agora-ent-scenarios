package io.agora.scene.cantata.ui.widget.lrcView

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import io.agora.karaoke_view.v11.ScoringView
import io.agora.scene.cantata.R

class MyScoringView : ScoringView {
    private var mStartLineLinearGradient: LinearGradient? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            val colorWhite35 = resources.getColor(R.color.white_35_percent)
            val colorWhite = resources.getColor(R.color.white)
            mStartLineLinearGradient = LinearGradient(
                0f,
                0f,
                0f,
                (bottom - top).toFloat(),
                intArrayOf(colorWhite35, colorWhite, colorWhite35),
                null,
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun drawOverpastWallAndStartLine(canvas: Canvas) {
        drawOverpastWall(canvas)

        // Same as drawStartLine, but with extra gradient color on it
        mStartLinePaint.shader = mStartLineLinearGradient
        mStartLinePaint.color = mLocalPitchIndicatorColor
        mStartLinePaint.isAntiAlias = true
        mStartLinePaint.strokeWidth = 3f
        canvas.drawLine(mCenterXOfStartPoint, 0f, mCenterXOfStartPoint, height.toFloat(), mStartLinePaint)
    }
}