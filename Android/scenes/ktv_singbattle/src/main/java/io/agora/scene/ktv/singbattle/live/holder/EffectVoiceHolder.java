package io.agora.scene.ktv.singbattle.live.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.ktv.singbattle.bean.EffectVoiceBean;
import io.agora.scene.ktv.singbattle.databinding.KtvItemEffectvoiceBinding;
import io.agora.scene.ktv.singbattle.databinding.KtvItemMvBinding;

/**
 * ---------------------------------------------------------------------------------------------
 * 功能描述:
 * ---------------------------------------------------------------------------------------------
 * 时　　间: 2023/3/1
 * ---------------------------------------------------------------------------------------------
 * 代码创建: Leo
 * ---------------------------------------------------------------------------------------------
 * 代码备注:
 * ---------------------------------------------------------------------------------------------
 **/
public class EffectVoiceHolder extends BaseRecyclerViewAdapter.BaseViewHolder<KtvItemEffectvoiceBinding, EffectVoiceBean> {

    public EffectVoiceHolder(@NonNull KtvItemEffectvoiceBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(@Nullable EffectVoiceBean data, int selectedIndex) {
        mBinding.ivBg.setImageResource(data.getResId());
        mBinding.tvTitle.setText(data.getTitle());
        mBinding.select.setVisibility(data.isSelect() ? View.VISIBLE : View.GONE);
    }

}
