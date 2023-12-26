package io.agora.scene.ktv.singbattle.widget.song;

import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.utils.UiUtil;
import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleItemChooseSongListBinding;
import io.agora.scene.widget.basic.BindingSingleAdapter;
import io.agora.scene.widget.basic.BindingViewHolder;

/**
 * The holder of Item ChooseSong
 */
abstract class SongChooseViewAdapter extends BindingSingleAdapter<SongItem, KtvSingbattleItemChooseSongListBinding> {

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder<KtvSingbattleItemChooseSongListBinding> holder, int position) {
        SongItem data = getItem(position);
        KtvSingbattleItemChooseSongListBinding binding = holder.binding;
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
                .transform(new RoundedCorners(UiUtil.dp2px(10)))
                .into(binding.coverItemSongList);
        if (data.isChosen) {
            binding.btnItemSongList.setEnabled(false);
            binding.btnItemSongList.setText(R.string.ktv_singbattle_room_chosen_song_list);
            binding.btnItemSongList.setOnClickListener(null);
        } else {
            binding.btnItemSongList.setEnabled(true);
            binding.btnItemSongList.setText(R.string.ktv_singbattle_room_choose_song);
            binding.btnItemSongList.setOnClickListener(v -> onSongChosen(data, position));
            binding.btnItemSongList.setEnabled(data.enable);
        }
    }

    abstract void onSongChosen(SongItem song, int position);
}