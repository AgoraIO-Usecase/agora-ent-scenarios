package io.agora.scene.cantata.ui.widget.song

interface OnSongActionListener {
    /**
     * 点歌-当下拉刷新时回调
     */
    fun onChooseSongRefreshing(dialog: SongDialog, index: Int)

    /**
     * 点歌-当加载更多时回调
     */
    fun onChooseSongLoadMore(dialog: SongDialog, index: Int)

    /**
     * 点歌-当点击搜索时回调
     */
    fun onChooseSongSearching(dialog: SongDialog, condition: String?)

    /**
     * 点歌-item"点歌"按钮点击时回调
     */
    fun onChooseSongChosen(dialog: SongDialog, songItem: SongItem)

    /**
     * 已点歌单-当item"删除"点击时回调
     */
    fun onChosenSongDeleteClicked(dialog: SongDialog, song: SongItem)

    /**
     * 已点歌单-当item"置顶"点击时回调
     */
    fun onChosenSongTopClicked(dialog: SongDialog, song: SongItem)
}