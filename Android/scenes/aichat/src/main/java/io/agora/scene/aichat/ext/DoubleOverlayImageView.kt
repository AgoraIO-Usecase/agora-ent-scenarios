package io.agora.scene.aichat.ext

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.imageview.ShapeableImageView
import io.agora.scene.aichat.R

class DoubleOverlayImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var ivBaseImageView: ShapeableImageView? = null
        private set(value) {
            field = value
        }
    private var ivOverlayImageView: ShapeableImageView? = null
        private set(value) {
            field = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.aichat_double_overlay_imageview, this, true)
        ivBaseImageView = findViewById(R.id.ivBaseImageView)
        ivOverlayImageView = findViewById(R.id.ivOverlayImageView)
    }


}