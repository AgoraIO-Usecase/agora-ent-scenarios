package io.agora.scene.pure1v1.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1RoomComeSoonViewBinding

class CallComeSoonView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: Pure1v1RoomComeSoonViewBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = Pure1v1RoomComeSoonViewBinding.inflate(inflater, this, true)
    }

    fun setComeBackSoonViewStyle(isLocal: Boolean) {
        if (isLocal) {
            binding.layoutVideoEmpty.setBackgroundResource(R.drawable.pure1v1_come_soon_bg)
            binding.tvComeSoon.text = context.getString(R.string.pure1v1_come_soon_local)
        } else {
            binding.layoutVideoEmpty.setBackgroundResource(R.color.pure1v1_come_soon_background)
        }
    }
}