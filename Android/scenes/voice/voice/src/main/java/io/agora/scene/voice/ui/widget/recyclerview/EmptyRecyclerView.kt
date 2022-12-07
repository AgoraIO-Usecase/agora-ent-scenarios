package io.agora.scene.voice.ui.widget.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * @author create by zhangwei03
 */
class EmptyRecyclerView  : RecyclerView {

    var mEmptyView: View? = null

    private val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            val lAdapter = adapter
            if (lAdapter != null && mEmptyView != null) {
                if (lAdapter.itemCount == 0) {
                    mEmptyView?.visibility = VISIBLE
                } else {
                    mEmptyView?.visibility = GONE
                }
            }
        }
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /****通过这个方法设置空布局 */
    fun setEmptyView(view: View?) {
        view?.let {
            mEmptyView = it
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyObserver)
        emptyObserver.onChanged()
    }
}