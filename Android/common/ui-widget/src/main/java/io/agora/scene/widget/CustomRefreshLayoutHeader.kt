package io.agora.scene.widget

import android.content.Context
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState


class CustomRefreshLayoutHeader(context: Context) : ClassicsHeader(context) {

    fun setInitialStatus() {
        this.visibility = GONE
    }

    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {
        super.onStateChanged(refreshLayout, oldState, newState)
        // 当刷新状态改变时，根据新的状态来控制提示布局的显示与隐藏
        if (newState == RefreshState.None) {
            this.visibility = GONE
        } else {
            this.visibility = VISIBLE
        }
    }
}
