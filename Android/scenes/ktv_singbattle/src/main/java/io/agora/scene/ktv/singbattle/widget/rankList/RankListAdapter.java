package io.agora.scene.ktv.singbattle.widget.rankList;


import android.view.View;

import androidx.annotation.NonNull;

import io.agora.scene.base.GlideApp;
import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.KtvItemRankListBinding;
import io.agora.scene.widget.basic.BindingSingleAdapter;
import io.agora.scene.widget.basic.BindingViewHolder;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;

public class RankListAdapter extends BindingSingleAdapter<RankItem, KtvItemRankListBinding> {
    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder<KtvItemRankListBinding> holder, int position) {
        RankItem item = getItem(position);
        KtvItemRankListBinding mBinding = holder.binding;
        if (position == 0) {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_game_rank_list_1_background);
            mBinding.tvTank.setBackgroundResource(R.mipmap.ktv_game_rank_1);
            mBinding.tvTank.setText("");
        } else if (position == 1) {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_game_rank_list_2_background);
            mBinding.tvTank.setBackgroundResource(R.mipmap.ktv_game_rank_2);
            mBinding.tvTank.setText("");
        } else if (position == 2) {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_game_rank_list_3_background);
            mBinding.tvTank.setBackgroundResource(R.mipmap.ktv_game_rank_3);
            mBinding.tvTank.setText("");
        } else {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_game_rank_list_default_background);
            mBinding.tvTank.setBackgroundResource(0);
            mBinding.tvTank.setText("" + (position + 1));
        }
        mBinding.tvPlayer.setText(item.userName);

        if (item.songNum == -1) {
            mBinding.tvSongNum.setText("-");
        } else {
            mBinding.tvSongNum.setText(item.songNum + "首");
        }

        if (item.score == -1) {
            mBinding.tvScore.setText("-");
        } else {
            mBinding.tvScore.setText(item.score + "分");
        }

        if (item.poster.equals("")) {
            mBinding.ivHeader.setVisibility(View.INVISIBLE);
        } else {
            GlideApp.with(mBinding.getRoot())
                    .load(item.poster)
                    .error(R.mipmap.userimage)
                    .transform(new CenterCropRoundCornerTransform(100))
                    .into(mBinding.ivHeader);
        }
    }
}
