package io.agora.imkitmanager.ui.impl

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ImageSpan

class AUICenteredImageSpan : ImageSpan {
    private var mMarginLeft = 0
    private var mMarginRight = 0

    constructor(context: Context?, drawableRes: Int) : super(context!!, drawableRes) {}
    constructor(context: Context?, drawableRes: Int, marginLeft: Int, marginRight: Int) : super(
        context!!, drawableRes
    ) {
        mMarginLeft = marginLeft
        mMarginRight = marginRight
    }

    override fun draw(
        canvas: Canvas, text: CharSequence,
        start: Int, end: Int, x: Float,
        top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        // image to draw
        val b = drawable
        // font metrics of text to be replaced
        val fm = paint.fontMetricsInt
        val transY = ((y + fm.descent + y + fm.ascent) / 2
                - b.bounds.bottom / 2)
        canvas.save()
        canvas.translate(x - 2, transY.toFloat())
        b.draw(canvas)
        canvas.restore()
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return mMarginLeft + super.getSize(paint, text, start, end, fm) + mMarginRight
    }
}