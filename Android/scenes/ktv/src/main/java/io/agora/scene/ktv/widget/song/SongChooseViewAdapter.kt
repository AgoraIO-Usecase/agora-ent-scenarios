package io.agora.scene.ktv.widget.song

import android.view.View
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.agora.scene.base.GlideApp
import io.agora.scene.base.utils.UiUtil
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvItemChooseSongListBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

/**
 * The holder of Item ChooseSong
 */
internal abstract class SongChooseViewAdapter : BindingSingleAdapter<SongItem?, KtvItemChooseSongListBinding>() {
    override fun onBindViewHolder(holder: BindingViewHolder<KtvItemChooseSongListBinding>, position: Int) {
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
            .transform(RoundedCorners(UiUtil.dp2px(10)))
            .into(binding.coverItemSongList)
        if (data.isChosen) {
            binding.btnItemSongList.isEnabled = false
            binding.btnItemSongList.setText(R.string.ktv_room_chosen_song_list)
            binding.btnItemSongList.setOnClickListener(null)
        } else {
            if (data.loading) {
                binding.btnItemSongList.isEnabled = false
                binding.btnItemSongList.setText(R.string.ktv_room_choose_song_loading)
            } else {
                binding.btnItemSongList.isEnabled = true
                binding.btnItemSongList.setText(R.string.ktv_room_choose_song)
            }
            binding.btnItemSongList.setOnClickListener { v: View? ->
                data.loading = true
                replace(position, data)
                onSongChosen(data, position)
            }
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