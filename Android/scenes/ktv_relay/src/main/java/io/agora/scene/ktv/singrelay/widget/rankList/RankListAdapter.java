package io.agora.scene.ktv.singrelay.widget.rankList;


import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.request.RequestOptions;

import io.agora.scene.base.GlideApp;
import io.agora.scene.ktv.singrelay.KTVLogger;
import io.agora.scene.ktv.singrelay.R;
import io.agora.scene.ktv.singrelay.databinding.KtvRelayItemRankListBinding;
import io.agora.scene.widget.basic.BindingSingleAdapter;
import io.agora.scene.widget.basic.BindingViewHolder;

public class RankListAdapter extends BindingSingleAdapter<RankItem, KtvRelayItemRankListBinding> {
    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder<KtvRelayItemRankListBinding> holder, int position) {
        RankItem item = getItem(position);
        KtvRelayItemRankListBinding mBinding = holder.binding;
        if (position == 0) {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_relay_game_rank_list_1_background);
            mBinding.tvTank.setBackgroundResource(R.mipmap.ktv_game_rank_1);
            mBinding.tvTank.setText("");
        } else if (position == 1) {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_relay_game_rank_list_2_background);
            mBinding.tvTank.setBackgroundResource(R.mipmap.ktv_game_rank_2);
            mBinding.tvTank.setText("");
        } else if (position == 2) {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_relay_game_rank_list_3_background);
            mBinding.tvTank.setBackgroundResource(R.mipmap.ktv_game_rank_3);
            mBinding.tvTank.setText("");
        } else {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_relay_game_rank_list_default_background);
            mBinding.tvTank.setBackgroundResource(0);
            mBinding.tvTank.setText("" + (position + 1));
        }
        mBinding.tvPlayer.setText(item.userName);

        if (item.songNum == -1) {
            mBinding.tvSongNum.setText("-");
        } else {
            mBinding.tvSongNum.setText(item.songNum + "段");
        }

        if (item.score == -1) {
            mBinding.tvScore.setText("-");
        } else {
            mBinding.tvScore.setText(item.score + "分");
        }

        if (item.poster.equals("")) {
            KTVLogger.d("hugo", "hugo");
            mBinding.ivHeader.setVisibility(View.INVISIBLE);
        } else {
            mBinding.ivHeader.setVisibility(View.VISIBLE);
            GlideApp.with(mBinding.getRoot())
                    .load(item.poster)
                    .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mBinding.ivHeader);
        }
    }
}
