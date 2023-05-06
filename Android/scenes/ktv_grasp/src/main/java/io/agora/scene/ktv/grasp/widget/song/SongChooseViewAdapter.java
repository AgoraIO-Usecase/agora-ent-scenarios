package io.agora.scene.ktv.grasp.widget.song;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.scene.base.GlideApp;
import io.agora.scene.ktv.grasp.R;
import io.agora.scene.ktv.grasp.databinding.KtvItemChooseSongListBinding;
import io.agora.scene.widget.basic.BindingSingleAdapter;
import io.agora.scene.widget.basic.BindingViewHolder;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;

/**
 * The holder of Item ChooseSong
 */
abstract class SongChooseViewAdapter extends BindingSingleAdapter<SongItem, KtvItemChooseSongListBinding> {

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder<KtvItemChooseSongListBinding> holder, int position) {
        SongItem data = getItem(position);
        KtvItemChooseSongListBinding binding = holder.binding;
        binding.titleItemSongList.setText(data.songName);
        binding.titleItemSongList.setOnLongClickListener(v -> {
            v.setSelected(!v.isSelected());
            return true;
        });
        binding.tvSinger.setText(data.singer);
        binding.coverItemSongList.setVisibility(View.VISIBLE);
        GlideApp.with(binding.coverItemSongList).load(data.imageUrl)
                .fallback(R.mipmap.ktv_ic_song_default)
                .error(R.mipmap.ktv_ic_song_default)
                .transform(new CenterCropRoundCornerTransform(10))
                .into(binding.coverItemSongList);
        if (data.isChosen) {
            binding.btnItemSongList.setEnabled(false);
            binding.btnItemSongList.setText(R.string.ktv_room_chosen_song_list);
            binding.btnItemSongList.setOnClickListener(null);
        } else {
            binding.btnItemSongList.setEnabled(true);
            binding.btnItemSongList.setText(R.string.ktv_room_choose_song);
            binding.btnItemSongList.setOnClickListener(v -> onSongChosen(data, position));
        }
    }

    abstract void onSongChosen(SongItem song, int position);
}