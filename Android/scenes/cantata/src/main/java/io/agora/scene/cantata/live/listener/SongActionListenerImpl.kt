package io.agora.scene.cantata.live.listener

import android.text.TextUtils
import androidx.lifecycle.LifecycleOwner
import io.agora.scene.base.utils.LiveDataUtils
import io.agora.scene.cantata.live.RoomLivingViewModel
import io.agora.scene.cantata.service.RoomSelSongModel
import io.agora.scene.cantata.widget.song.OnSongActionListener
import io.agora.scene.cantata.widget.song.SongDialog
import io.agora.scene.cantata.widget.song.SongItem

/**
 * Song Selection Panel Listener
 */
class SongActionListenerImpl constructor(
    private val mLifecycleOwner: LifecycleOwner,
    private val mViewModel: RoomLivingViewModel,
    private val isChorus: Boolean
) : OnSongActionListener {
    override fun onChooseSongRefreshing(dialog: SongDialog) {
        // Song selection - list refresh
        LiveDataUtils.observerThenRemove(
            mLifecycleOwner,
            mViewModel.getSongList()
        ) { list: List<RoomSelSongModel>? ->
            if (dialog.isVisible) {
                dialog.setChooseRefreshingResult(transSongModel(list))
            }
        }
    }

    override fun onChooseSongChosen(dialog: SongDialog, songItem: SongItem) {
        // Select song
        val songModel = songItem.getTag(RoomSelSongModel::class.java) ?: return
        LiveDataUtils.observerThenRemove(
            mLifecycleOwner,
            mViewModel.chooseSong(songModel, isChorus)
        ) { success: Boolean ->
            if (success && dialog.isVisible) {
                dialog.setChooseSongItemStatus(songItem, true)
            } else if (!success) { // order failed
                songItem.loading = false
                dialog.setChooseSongItemStatus(songItem, false)
            }
        }
    }

    override fun onChosenSongDeleteClicked(dialog: SongDialog, song: SongItem) {
        // Delete song
        val songModel = song.getTag(RoomSelSongModel::class.java)?:return
        mViewModel.deleteSong(songModel)
    }

    override fun onChosenSongTopClicked(dialog: SongDialog, song: SongItem) {
        // Move to top
        val songModel = song.getTag(RoomSelSongModel::class.java)?:return
        mViewModel.topUpSong(songModel)
    }

    companion object {
        fun transSongModel(data: List<RoomSelSongModel>?): List<SongItem> {
            val list = ArrayList<SongItem>()
            if (data != null) {
                for (song in data) {
                    val item = SongItem(
                        song.songNo,
                        song.songName,
                        song.imageUrl,
                        song.singer,
                        song.name!!,
                        !TextUtils.isEmpty(song.name),
                        song.userNo
                    )
                    item.setTag(song)
                    list.add(item)
                }
            }
            return list
        }
    }
}