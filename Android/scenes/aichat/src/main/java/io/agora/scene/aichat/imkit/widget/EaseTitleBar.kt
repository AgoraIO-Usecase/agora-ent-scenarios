package io.agora.scene.aichat.imkit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.imageview.ShapeableImageView
import io.agora.scene.aichat.R
import io.agora.scene.aichat.databinding.EaseWidgetTitleBarBinding
import io.agora.scene.aichat.ext.DoubleOverlayImageView

class EaseTitleBar @JvmOverloads constructor(
    private val context: Context, attrs: AttributeSet? = null, defStyle: Int = androidx.appcompat.R.attr.toolbarStyle
) : ConstraintLayout(context, attrs, defStyle) {

    private val binding: EaseWidgetTitleBarBinding by lazy {
        EaseWidgetTitleBarBinding.inflate(LayoutInflater.from(context), this)
    }

    init {
        parseStyle(context, attrs, defStyle)
    }


    private fun parseStyle(context: Context, attrs: AttributeSet?, defStyle: Int) {
        context.obtainStyledAttributes(attrs, R.styleable.EaseTitleBar, defStyle, 0).let { a ->
            val showCommonImage = a.getBoolean(R.styleable.EaseTitleBar_showCommonImage, true)
            binding.ivCommonImage.isVisible = showCommonImage
            val showDoubleOverlayImage = a.getBoolean(R.styleable.EaseTitleBar_showDoubleOverlayImage, false)
            if (showCommonImage) {
                binding.ivOverlayImage.isVisible = false
            } else {
                binding.ivOverlayImage.isVisible = showDoubleOverlayImage
            }
            val showMoreIcon = a.getBoolean(R.styleable.EaseTitleBar_showMoreIcon, false)
            binding.ivMoreIcon.isVisible = showMoreIcon
            a.recycle()
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
    }


    val tvTitle: TextView get() = binding.tvTitle

    val tvSubTitle: TextView get() = binding.tvSubtitle

    val chatAvatarImage: ShapeableImageView get() = binding.ivCommonImage

    val groupAvatarImage: DoubleOverlayImageView get() = binding.ivOverlayImage

    val ivMoreIcon: AppCompatImageView get() = binding.ivMoreIcon

    /**
     * Set back icon click listener.
     */
    fun setBackClickListener(listener: OnClickListener?) {
        binding.ivBackIcon.setOnClickListener(listener)
    }

    /**
     * Set more icon click listener.
     */
    fun setMoreClickListener(listener: OnClickListener?) {
        binding.ivMoreIcon.setOnClickListener(listener)
    }
}