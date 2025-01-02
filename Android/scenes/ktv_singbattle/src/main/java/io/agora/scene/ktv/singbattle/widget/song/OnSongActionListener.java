package io.agora.scene.ktv.singbattle.widget.song;

import androidx.annotation.NonNull;

public interface OnSongActionListener {

    /**
     * Choose song - callback when pull down to refresh
     */
    void onChooseSongRefreshing(@NonNull SongDialog dialog);

    /**
     * Choose song - callback when item "choose song" button clicked
     */
    void onChooseSongChosen(@NonNull SongDialog dialog, @NonNull SongItem songItem);

    /**
     * Chosen song list - callback when item "delete" clicked
     */
    void onChosenSongDeleteClicked(@NonNull SongDialog dialog, @NonNull SongItem song);

    /**
     * Chosen song list - callback when item "top" clicked
     */
    void onChosenSongTopClicked(@NonNull SongDialog dialog, @NonNull SongItem song);

    void onStartSingBattleGame(@NonNull SongDialog dialog);
}