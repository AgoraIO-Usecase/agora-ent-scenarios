package io.agora.scene.ktv.widget.song;

import androidx.annotation.NonNull;

/**
 * The interface On song action listener.
 */
public interface OnSongActionListener {

    /**
     * Order - callback on refresh
     *
     * @param dialog the dialog
     */
    void onChooseSongRefreshing(@NonNull SongDialog dialog);

    /**
     * Order - callback when the ‘select song’ button is clicked on an item.
     *
     * @param dialog   the dialog
     * @param songItem the song item
     */
    void onChooseSongChosen(@NonNull SongDialog dialog, @NonNull SongItem songItem);

    /**
     * Queued song list - callback when the ‘delete’ button on an item is clicked.
     *
     * @param dialog the dialog
     * @param song   the song
     */
    void onChosenSongDeleteClicked(@NonNull SongDialog dialog, @NonNull SongItem song);

    /**
     * Queued song list - callback when the ‘pin’ button on an item is clicked.
     *
     * @param dialog the dialog
     * @param song   the song
     */
    void onChosenSongTopClicked(@NonNull SongDialog dialog, @NonNull SongItem song);

}
