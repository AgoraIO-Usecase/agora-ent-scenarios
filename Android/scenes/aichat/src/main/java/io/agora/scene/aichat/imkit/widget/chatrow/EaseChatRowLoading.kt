package io.agora.scene.aichat.imkit.widget.chatrow

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.github.penfeizhou.animation.apng.APNGDrawable
import io.agora.scene.aichat.R
import io.agora.scene.base.component.AgoraApplication

class EaseChatRowLoading constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean = false
) : EaseChatRow(context, attrs, defStyleAttr, isSender) {

    private var loadingDrawable: APNGDrawable? = null

    private val image: ImageView? by lazy { findViewById(R.id.image) }

    override fun onInflateView() {
        inflater.inflate(R.layout.ease_row_loading_message, this)
    }

    override fun onSetUpView() {
        loadingDrawable = APNGDrawable.fromAsset(AgoraApplication.the().applicationContext, "aichat_text_loading.png")
        loadingDrawable?.setLoopLimit(-1)
        image?.setImageDrawable(loadingDrawable)
    }
}