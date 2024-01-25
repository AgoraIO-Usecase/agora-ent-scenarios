package io.agora.scene.pure1v1.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.constraintlayout.widget.ConstraintLayout
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1DraggableViewBinding
import io.agora.scene.pure1v1.databinding.Pure1v1RoomListGuideViewBinding

class RoomListGuideView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: Pure1v1RoomListGuideViewBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = Pure1v1RoomListGuideViewBinding.inflate(inflater, this, true)
        setupGuideAnimation()
    }

    override fun onDetachedFromWindow() {
        binding.ivFinger.clearAnimation()
        super.onDetachedFromWindow()
    }

    private fun setupGuideAnimation() {
        val anim = TranslateAnimation(
            0f, 0f,0f,
            resources.getDimension(R.dimen.pure1v1_room_list_guide_height))
        anim.duration = 2000
        anim.repeatCount = Animation.INFINITE
        anim.repeatMode = Animation.REVERSE
        binding.ivFinger.startAnimation(anim)
    }

}