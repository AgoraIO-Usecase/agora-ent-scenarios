package io.agora.scene.ktv.live.holder;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvItemChoosedSongListBinding;
import io.agora.scene.ktv.manager.RoomManager;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;

/**
 * The holder of Item ChooseSong
 */
public class ChosenSongViewHolder extends BaseRecyclerViewAdapter.BaseViewHolder<KtvItemChoosedSongListBinding, MemberMusicModel> {
    public ChosenSongViewHolder(@NonNull KtvItemChoosedSongListBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(MemberMusicModel item, int selectedIndex) {
        if (item != null) {
            mBinding.tvNo.setText(String.valueOf(getAdapterPosition() + 1));
            mBinding.tvMusicName.setText(item.songName);
            mBinding.tvChooser.setText(item.name);
            GlideApp.with(itemView).load(item.imageUrl)
                    .transform(new CenterCropRoundCornerTransform(10))
                    .into(mBinding.ivCover);
            if (getAdapterPosition() == 0) {
                mBinding.tvSing.setVisibility(View.VISIBLE);
                mBinding.ivSinging.setVisibility(View.VISIBLE);
                mBinding.ivToDel.setVisibility(View.GONE);
                mBinding.ivToTop.setVisibility(View.GONE);
            } else if (getAdapterPosition() == 1 && RoomManager.getInstance().getMine().isMaster) {
                mBinding.ivToDel.setOnClickListener(this::onItemClick);
                mBinding.tvSing.setVisibility(View.GONE);
                mBinding.ivSinging.setVisibility(View.GONE);
                mBinding.ivToDel.setVisibility(View.VISIBLE);
                mBinding.ivToTop.setVisibility(View.GONE);
            } else if (RoomManager.mMine.isMaster) {
                mBinding.ivToDel.setOnClickListener(this::onItemClick);
                mBinding.ivToTop.setOnClickListener(this::onItemClick);
                mBinding.tvSing.setVisibility(View.GONE);
                mBinding.ivSinging.setVisibility(View.GONE);
                mBinding.ivToDel.setVisibility(View.VISIBLE);
                mBinding.ivToTop.setVisibility(View.VISIBLE);
            } else {
                mBinding.tvSing.setVisibility(View.GONE);
                mBinding.ivSinging.setVisibility(View.GONE);
                mBinding.ivToDel.setVisibility(View.GONE);
                mBinding.ivToTop.setVisibility(View.GONE);
            }
            if (item.isChorus) {
                mBinding.tvChorus.setText(mBinding.tvChorus.getContext().getString(R.string.song_ordering_person_chorus));
            } else {
                mBinding.tvChorus.setText(mBinding.tvChorus.getContext().getString(R.string.song_ordering_person));
            }
        }
    }
}