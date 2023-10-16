package io.agora.scene.ktv.singbattle.live.holder;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.ktv.singbattle.databinding.KtvItemMvBinding;


/**
 * MVModel List
 */
public class MVHolder extends BaseRecyclerViewAdapter.BaseViewHolder<KtvItemMvBinding, Integer> {

    public MVHolder(@NonNull KtvItemMvBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(Integer data, int selectedIndex) {
        if (getAdapterPosition() == selectedIndex) {
            mBinding.ivSelected.setVisibility(View.VISIBLE);
        } else {
            mBinding.ivSelected.setVisibility(View.GONE);
        }
        GlideApp.with(mBinding.getRoot())
                .load(data)
                .into(mBinding.ivCover);
    }
}