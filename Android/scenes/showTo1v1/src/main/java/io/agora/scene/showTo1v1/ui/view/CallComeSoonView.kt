package io.agora.scene.showTo1v1.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.databinding.ShowTo1v1ComeSoonViewBinding

class CallComeSoonView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ShowTo1v1ComeSoonViewBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ShowTo1v1ComeSoonViewBinding.inflate(inflater, this, true)
    }

    fun setComeBackSoonViewStyle(isLocal: Boolean) {
        if (isLocal) {
            binding.layoutVideoEmpty.setBackgroundResource(R.drawable.show_to1v1_come_soon_local_bg)
            binding.tvComeSoon.text = context.getString(R.string.show_to1v1_come_soon_local)
        } else {
            binding.layoutVideoEmpty.setBackgroundResource(R.color.show_to1v1_come_soon_background)
        }
    }
}