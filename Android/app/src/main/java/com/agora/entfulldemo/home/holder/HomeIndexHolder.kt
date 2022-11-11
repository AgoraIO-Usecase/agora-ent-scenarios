package com.agora.entfulldemo.home.holder

import androidx.core.view.isGone
import com.agora.entfulldemo.databinding.ItemHomeIndexBinding
import com.agora.entfulldemo.home.constructor.ScenesModel
import io.agora.scene.base.component.BaseRecyclerViewAdapter

/**
 * @author create by zhangwei03
 */
class HomeIndexHolder(mBinding: ItemHomeIndexBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<ItemHomeIndexBinding, ScenesModel?>(mBinding) {

    override fun binding(scensModel: ScenesModel?, selectedIndex: Int) {
        scensModel?.apply {
            mBinding.tvUnActive.isGone = active
            mBinding.tvScenesName.text = name
            mBinding.ivScenesBg.setBackgroundResource(background)
            mBinding.ivScenesIcon.setBackgroundResource(icon)
        }
    }
}