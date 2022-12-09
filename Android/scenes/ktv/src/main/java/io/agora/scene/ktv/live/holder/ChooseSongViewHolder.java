package io.agora.scene.ktv.live.holder;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.data.model.MusicModelNew;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvItemChooseSongListBinding;
import io.agora.scene.ktv.manager.RoomManager;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;

/**
 * The holder of Item ChooseSong
 */
public class ChooseSongViewHolder extends BaseRecyclerViewAdapter.BaseViewHolder<KtvItemChooseSongListBinding, MusicModelNew> {
    public ChooseSongViewHolder(@NonNull KtvItemChooseSongListBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(MusicModelNew data, int selectedIndex) {
        if (data != null) {
            mBinding.titleItemSongList.setText(data.songName);
            mBinding.titleItemSongList.setOnLongClickListener(v -> {
                v.setSelected(!v.isSelected());
                return true;
            });
            mBinding.coverItemSongList.setVisibility(View.VISIBLE);
            GlideApp.with(itemView).load(data.imageUrl)
                    .transform(new CenterCropRoundCornerTransform(10))
                    .into(mBinding.coverItemSongList);
            if (RoomManager.getInstance().isInMusicOrderList(data)) {
                mBinding.btnItemSongList.setEnabled(false);
                mBinding.btnItemSongList.setText(R.string.ktv_room_choosed_song);
                mBinding.btnItemSongList.setOnClickListener(null);
            } else {
                mBinding.btnItemSongList.setEnabled(true);
                mBinding.btnItemSongList.setText(R.string.ktv_room_choose_song);
                mBinding.btnItemSongList.setOnClickListener(this::onItemClick);
            }
        }
    }
}