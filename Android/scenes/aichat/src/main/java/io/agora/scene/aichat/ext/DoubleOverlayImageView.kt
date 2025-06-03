package io.agora.scene.aichat.ext

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.imageview.ShapeableImageView
import io.agora.scene.aichat.R

fun ShapeableImageView.setGradientBackground(
    @ColorInt colors: IntArray,
    orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TL_BR,
) {
    val gradientDrawable = GradientDrawable(
        orientation, // 渐变方向
        colors // 渐变颜色
    )
//        gradientDrawable.gradientType = GradientDrawable.SWEEP_GRADIENT
    // 设置描边
    gradientDrawable.shape = GradientDrawable.OVAL
    // 将渐变背景设置到 ShapeableImageView
    background = gradientDrawable
    // 设置透明的描边颜色以防冲突
    strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
}

class DoubleOverlayImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var ivBaseImageView: ShapeableImageView

    var ivOverlayImageView: ShapeableImageView

    var ivBaseImageViewBg: ShapeableImageView

    var ivOverlayImageViewBg: ShapeableImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.aichat_double_overlay_imageview, this, true)
        ivBaseImageView = findViewById(R.id.ivBaseImageView)
        ivOverlayImageView = findViewById(R.id.ivOverlayImageView)
        ivBaseImageViewBg = findViewById(R.id.ivBaseImageViewBg)
        ivOverlayImageViewBg = findViewById(R.id.ivOverlayImageViewBg)
    }
}