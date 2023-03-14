package io.agora.voice.common.utils

import android.content.Context
import android.content.res.Resources
import android.util.Size
import android.util.TypedValue
import java.math.RoundingMode
import java.text.DecimalFormat

object DeviceTools {

    @JvmStatic
    val Number.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )

    @JvmStatic
    val Number.sp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )

    @JvmStatic
    fun getDisplaySize(): Size {
        val metrics = Resources.getSystem().displayMetrics
        return Size(metrics.widthPixels, metrics.heightPixels)
    }

    @JvmStatic
    fun Int.number2K(): String {
        if (this < 1000) return this.toString()
        val format = DecimalFormat("0.#")
        //未保留小数的舍弃规则，RoundingMode.FLOOR表示直接舍弃。
        format.roundingMode = RoundingMode.FLOOR
        return "${format.format(this / 1000f)}k"
    }

    @JvmStatic
    fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    @JvmStatic
    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }
}

