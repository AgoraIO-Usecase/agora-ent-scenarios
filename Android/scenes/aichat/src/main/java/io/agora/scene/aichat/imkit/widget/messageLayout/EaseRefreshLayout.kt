package io.agora.scene.aichat.imkit.widget.messageLayout

import android.content.Context
import android.util.AttributeSet
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener

class EaseRefreshLayout @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null
): SmartRefreshLayout(context, attrs) {

    override fun finishLoadMore(): RefreshLayout {
        return super.finishLoadMore()
    }

    override fun finishLoadMoreWithNoMoreData(): RefreshLayout {
        return super.finishLoadMoreWithNoMoreData()
    }

    override fun finishRefresh(): RefreshLayout {
        return super.finishRefresh()
    }

    override fun setEnableLoadMore(enable: Boolean): RefreshLayout {
        return super.setEnableLoadMore(enable)
    }

    override fun setEnableRefresh(enable: Boolean): RefreshLayout {
        return super.setEnableRefresh(enable)
    }

    override fun setOnRefreshListener(listener: OnRefreshListener?): RefreshLayout {
        return super.setOnRefreshListener(listener)
    }

    override fun setOnLoadMoreListener(listener: OnLoadMoreListener?): RefreshLayout {
        return super.setOnLoadMoreListener(listener)
    }

}