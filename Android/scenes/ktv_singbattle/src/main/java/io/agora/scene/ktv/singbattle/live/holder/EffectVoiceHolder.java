package io.agora.scene.ktv.singbattle.live.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.ktv.singbattle.bean.EffectVoiceBean;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleItemEffectvoiceBinding;

public class EffectVoiceHolder extends BaseRecyclerViewAdapter.BaseViewHolder<KtvSingbattleItemEffectvoiceBinding, EffectVoiceBean> {

    public EffectVoiceHolder(@NonNull KtvSingbattleItemEffectvoiceBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(@Nullable EffectVoiceBean data, int selectedIndex) {
        mBinding.ivBg.setImageResource(data.getResId());
        mBinding.tvTitle.setText(data.getTitle());
        mBinding.select.setVisibility(data.isSelect() ? View.VISIBLE : View.GONE);
    }

}
