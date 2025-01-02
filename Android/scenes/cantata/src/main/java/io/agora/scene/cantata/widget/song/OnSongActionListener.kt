package io.agora.scene.cantata.widget.song

interface OnSongActionListener {
    /**
     * Song selection - Callback when pull-to-refresh
     */
    fun onChooseSongRefreshing(dialog: SongDialog)

    /**
     * Song selection - Callback when "Choose Song" button is clicked on item
     */
    fun onChooseSongChosen(dialog: SongDialog, songItem: SongItem)

    /**
     * Selected song list - Callback when "Delete" is clicked on item
     */
    fun onChosenSongDeleteClicked(dialog: SongDialog, song: SongItem)

    /**
     * Selected song list - Callback when "Top" is clicked on item
     */
    fun onChosenSongTopClicked(dialog: SongDialog, song: SongItem)
}