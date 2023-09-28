package io.agora.scene.cantata.ui.widget.song

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.agora.scene.base.utils.LiveDataUtils
import io.agora.scene.cantata.service.RoomSelSongModel
import io.agora.scene.cantata.ui.viewmodel.RoomLivingViewModel

class SongActionListenerImpl constructor(
    private val mLifecycleOwner: LifecycleOwner,
    private val mViewModel: RoomLivingViewModel,
    private val songTypeMap: LinkedHashMap<Int, String>,
    private val isChorus: Boolean
) : OnSongActionListener {
    private var mCurrPage = 1
    override fun onChooseSongRefreshing(dialog: SongDialog, index: Int) {
        // 点歌-列表刷新
        mCurrPage = 1
        val songType = getSongType(index)
        if (songType == -1) {
            Log.e("KTV", "getSongType null")
            return
        }
        LiveDataUtils.observerThenRemove(
            mLifecycleOwner,
            mViewModel.getSongList(songType, mCurrPage),
            Observer { list: List<RoomSelSongModel>? ->
                if (dialog.isVisible) {
                    dialog.setChooseRefreshingResult(transSongModel(list), index)
                }
            })
    }

    override fun onChooseSongLoadMore(dialog: SongDialog, index: Int) {
        // 点歌-列表加载更多
        mCurrPage++
        val songType = getSongType(index)
        if (songType == -1) {
            Log.e("KTV", "getSongType null")
            return
        }
        LiveDataUtils.observerThenRemove(
            mLifecycleOwner,
            mViewModel.getSongList(songType, mCurrPage),
            Observer { list: List<RoomSelSongModel> ->
                if (dialog.isVisible) {
                    dialog.setChooseLoadMoreResult(transSongModel(list), list.size > 0, index)
                }
            })
    }

    override fun onChooseSongSearching(dialog: SongDialog, condition: String?) {
        // 点歌-搜索
        LiveDataUtils.observerThenRemove(
            mLifecycleOwner,
            mViewModel.searchSong(condition!!)
        ) { list: List<RoomSelSongModel>? ->
            if (dialog.isVisible) {
                dialog.setChooseSearchResult(transSongModel(list))
            }
        }
    }

    override fun onChooseSongChosen(dialog: SongDialog, songItem: SongItem) {
        // 点歌
        val songModel = songItem.getTag(RoomSelSongModel::class.java) ?: return
        LiveDataUtils.observerThenRemove(
            mLifecycleOwner,
            mViewModel.chooseSong(songModel, isChorus)
        ) { success: Boolean ->
            if (success && dialog.isVisible) {
                dialog.setChooseSongItemStatus(songItem, true)
            }
        }
    }

    override fun onChosenSongDeleteClicked(dialog: SongDialog, song: SongItem) {
        // 删歌
        val songModel = song.getTag(RoomSelSongModel::class.java)?:return
        mViewModel.deleteSong(songModel)
    }

    override fun onChosenSongTopClicked(dialog: SongDialog, song: SongItem) {
        // 置顶
        val songModel = song.getTag(RoomSelSongModel::class.java)?:return
        mViewModel.topUpSong(songModel)
    }

    fun getSongTypeTitles(context: Context?): List<String> {
        val titles: MutableList<String> = ArrayList()
        for ((_, value) in songTypeMap) {
            titles.add(value)
        }
        return titles
    }

    val songTypeList: List<Int>
        get() {
            val list: MutableList<Int> = ArrayList()
            for ((key) in songTypeMap) {
                list.add(key)
            }
            return list
        }

    private fun getSongType(index: Int): Int {
        var i = 0
        for ((key) in songTypeMap) {
            if (index == i) {
                return key
            }
            i++
        }
        return -1
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