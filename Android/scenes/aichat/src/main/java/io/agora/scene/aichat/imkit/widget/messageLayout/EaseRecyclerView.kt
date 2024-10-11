package io.agora.scene.aichat.imkit.widget.messageLayout

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class EaseRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    override fun getBottomFadingEdgeStrength(): Float {
        return 0f
    }
}