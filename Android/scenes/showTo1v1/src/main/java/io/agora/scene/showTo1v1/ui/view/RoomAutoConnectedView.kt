package io.agora.scene.showTo1v1.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.constraintlayout.widget.ConstraintLayout
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomAutoConnectedViewBinding
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomListGuideViewBinding

class RoomAutoConnectedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ShowTo1v1RoomAutoConnectedViewBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ShowTo1v1RoomAutoConnectedViewBinding.inflate(inflater, this, true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}