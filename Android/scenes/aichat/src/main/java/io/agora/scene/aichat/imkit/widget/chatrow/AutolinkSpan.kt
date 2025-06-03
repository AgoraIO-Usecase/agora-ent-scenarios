package io.agora.scene.aichat.imkit.widget.chatrow

import android.text.style.URLSpan
import android.view.View
import io.agora.scene.aichat.R

class AutolinkSpan(url: String?) : URLSpan(url) {
    override fun onClick(widget: View) {
        if (widget.getTag(R.id.action_chat_long_click) != null) {
            widget.setTag(R.id.action_chat_long_click, null)
            return
        }
        super.onClick(widget)
    }
}