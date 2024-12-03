package io.agora.scene.cantata.widget.song

interface OnSongActionListener {
    /**
     * 点歌-当下拉刷新时回调
     */
    fun onChooseSongRefreshing(dialog: SongDialog)

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