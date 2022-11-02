package io.agora.voice.baseui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class BaseUiWidget<B : ViewBinding> : ViewGroup {

    var binding: B? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
       binding = getViewBinding(context)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding = null
    }

    protected abstract fun getViewBinding(context: Context): B?
}