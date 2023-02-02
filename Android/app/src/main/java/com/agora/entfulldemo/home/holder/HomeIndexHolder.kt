package com.agora.entfulldemo.home.holder

import android.view.View
import androidx.core.view.isGone
import com.agora.entfulldemo.databinding.AppItemHomeIndexBinding
import com.agora.entfulldemo.home.constructor.ScenesModel
import io.agora.scene.base.component.BaseRecyclerViewAdapter

/**
 * @author create by zhangwei03
 */
class HomeIndexHolder(mBinding: AppItemHomeIndexBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<AppItemHomeIndexBinding, ScenesModel?>(mBinding) {

    override fun binding(scensModel: ScenesModel?, selectedIndex: Int) {
        scensModel?.apply {
            mBinding.tvUnActive.isGone = active
            mBinding.tvScenesName.text = name
            mBinding.ivScenesBg.setBackgroundResource(background)
            mBinding.ivScenesIcon.setBackgroundResource(icon)
            mBinding.tvTip.visibility = if (tip.isEmpty()) View.GONE else View.VISIBLE
            mBinding.tvTip.text = tip
        }
    }
}