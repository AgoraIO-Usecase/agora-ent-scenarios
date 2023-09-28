package io.agora.scene.cantata.ui.widget.song

import android.view.View
import io.agora.scene.base.GlideApp
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataItemChooseSongListBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform

/**
 * The holder of Item ChooseSong
 */
internal abstract class SongChooseViewAdapter : BindingSingleAdapter<SongItem, CantataItemChooseSongListBinding>() {
    override fun onBindViewHolder(holder: BindingViewHolder<CantataItemChooseSongListBinding>, position: Int) {
        val data = getItem(position)?:return
        val binding = holder.binding
        binding.titleItemSongList.text = data.songName
        binding.titleItemSongList.setOnLongClickListener { v: View ->
            v.isSelected = !v.isSelected
            true
        }
        binding.tvSinger.text = data.singer
        binding.coverItemSongList.visibility = View.VISIBLE
        GlideApp.with(binding.coverItemSongList).load(data.imageUrl)
            .fallback(R.drawable.cantata_ic_song_default)
            .error(R.drawable.cantata_ic_song_default)
            .transform(CenterCropRoundCornerTransform(10))
            .into(binding.coverItemSongList)
        if (data.isChosen) {
            binding.btnItemSongList.isEnabled = false
            binding.btnItemSongList.setText(R.string.cantata_room_chosen_song_list)
            binding.btnItemSongList.setOnClickListener(null)
        } else {
            binding.btnItemSongList.isEnabled = true
            binding.btnItemSongList.setText(R.string.cantata_room_choose_song)
            binding.btnItemSongList.setOnClickListener { v: View? -> onSongChosen(data, position) }
        }
    }

    abstract fun onSongChosen(song: SongItem, position: Int)
}