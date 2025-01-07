package io.agora.scene.ktv.singbattle.widget.song

import android.view.View
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.agora.scene.base.GlideApp
import io.agora.scene.base.utils.dp
import io.agora.scene.ktv.singbattle.R
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleItemChooseSongListBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

/**
 * The holder of Item ChooseSong
 */
internal abstract class SongChooseViewAdapter : BindingSingleAdapter<SongItem?, KtvSingbattleItemChooseSongListBinding>() {
    override fun onBindViewHolder(holder: BindingViewHolder<KtvSingbattleItemChooseSongListBinding>, position: Int) {
        val data = getItem(position)
        val binding = holder.binding
        binding.titleItemSongList.text = data!!.songName
        binding.titleItemSongList.setOnLongClickListener { v: View ->
            v.isSelected = !v.isSelected
            true
        }
        binding.tvSinger.text = data.singer
        binding.coverItemSongList.visibility = View.VISIBLE
        GlideApp.with(binding.coverItemSongList).load(data.imageUrl)
            .fallback(R.mipmap.ktv_ic_song_default)
            .error(R.mipmap.ktv_ic_song_default)
            .transform(RoundedCorners(10.dp.toInt()))
            .into(binding.coverItemSongList)
        if (data.isChosen) {
            binding.btnItemSongList.isEnabled = false
            binding.btnItemSongList.setText(R.string.ktv_singbattle_room_chosen_song_list)
            binding.btnItemSongList.setOnClickListener(null)
        } else {
            if (data.loading) {
                binding.btnItemSongList.isEnabled = false
                binding.btnItemSongList.setText(R.string.ktv_singbattle_room_choose_song_loading)
            } else {
                binding.btnItemSongList.isEnabled = true
                binding.btnItemSongList.setText(R.string.ktv_singbattle_room_choose_song)
            }
            binding.btnItemSongList.setOnClickListener { v: View? ->
                data.loading = true
                replace(position, data)
                onSongChosen(data, position)
            }
            binding.btnItemSongList.isEnabled = data.enable
        }
    }

    /**
     * On song chosen.
     *
     * @param song     the song
     * @param position the position
     */
    abstract fun onSongChosen(song: SongItem, position: Int)
}