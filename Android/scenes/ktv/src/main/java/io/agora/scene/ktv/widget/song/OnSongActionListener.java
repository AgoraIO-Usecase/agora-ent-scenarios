package io.agora.scene.ktv.widget.song;

import androidx.annotation.NonNull;

/**
 * The interface On song action listener.
 */
public interface OnSongActionListener {

    /**
     * 点歌-当下拉刷新时回调
     *
     * @param dialog the dialog
     * @param index  the index
     */
    void onChooseSongRefreshing(@NonNull SongDialog dialog, int index);

    /**
     * 点歌-当加载更多时回调
     *
     * @param dialog the dialog
     * @param index  the index
     */
    void onChooseSongLoadMore(@NonNull SongDialog dialog, int index);

    /**
     * 点歌-当点击搜索时回调
     *
     * @param dialog    the dialog
     * @param condition the condition
     */
    void onChooseSongSearching(@NonNull SongDialog dialog, String condition);

    /**
     * 点歌-item"点歌"按钮点击时回调
     *
     * @param dialog   the dialog
     * @param songItem the song item
     */
    void onChooseSongChosen(@NonNull SongDialog dialog, @NonNull SongItem songItem);

    /**
     * 已点歌单-当item"删除"点击时回调
     *
     * @param dialog the dialog
     * @param song   the song
     */
    void onChosenSongDeleteClicked(@NonNull SongDialog dialog, @NonNull SongItem song);

    /**
     * 已点歌单-当item"置顶"点击时回调
     *
     * @param dialog the dialog
     * @param song   the song
     */
    void onChosenSongTopClicked(@NonNull SongDialog dialog, @NonNull SongItem song);

}
