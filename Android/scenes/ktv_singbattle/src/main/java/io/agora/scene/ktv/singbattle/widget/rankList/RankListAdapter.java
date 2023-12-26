package io.agora.scene.ktv.singbattle.widget.rankList;


import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.request.RequestOptions;

import io.agora.scene.base.GlideApp;
import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleItemRankListBinding;
import io.agora.scene.widget.basic.BindingSingleAdapter;
import io.agora.scene.widget.basic.BindingViewHolder;

public class RankListAdapter extends BindingSingleAdapter<RankItem, KtvSingbattleItemRankListBinding> {

    private final Context mContext;

    public RankListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder<KtvSingbattleItemRankListBinding> holder, int position) {
        RankItem item = getItem(position);
        KtvSingbattleItemRankListBinding mBinding = holder.binding;
        if (position == 0) {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_singbattle_game_rank_list_1_background);
            mBinding.tvTank.setBackgroundResource(R.mipmap.ktv_game_rank_1);
            mBinding.tvTank.setText("");
        } else if (position == 1) {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_singbattle_game_rank_list_2_background);
            mBinding.tvTank.setBackgroundResource(R.mipmap.ktv_game_rank_2);
            mBinding.tvTank.setText("");
        } else if (position == 2) {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_singbattle_game_rank_list_3_background);
            mBinding.tvTank.setBackgroundResource(R.mipmap.ktv_game_rank_3);
            mBinding.tvTank.setText("");
        } else {
            mBinding.ivBg.setBackgroundResource(R.mipmap.ktv_singbattle_game_rank_list_default_background);
            mBinding.tvTank.setBackgroundResource(0);
            mBinding.tvTank.setText(String.valueOf(position + 1));
        }
        if (item != null && mContext != null) {
            mBinding.tvPlayer.setText(item.userName);
            if (item.songNum == -1) {
                mBinding.tvSongNum.setText("-");
            } else {
                mBinding.tvSongNum.setText(mContext.getString(R.string.ktv_singbattle_song_num_formatter, item.songNum));
            }

            if (item.score == -1) {
                mBinding.tvScore.setText("-");
            } else {
                mBinding.tvScore.setText(mContext.getString(R.string.ktv_singbattle_score_formatter, String.valueOf(item.score)));
            }

            if (item.poster.equals("null")) {
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
}
