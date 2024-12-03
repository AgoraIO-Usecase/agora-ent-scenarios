package io.agora.scene.ktv.live.listener;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.List;

import io.agora.scene.base.utils.LiveDataUtils;
import io.agora.scene.ktv.live.RoomLivingViewModel;
import io.agora.scene.ktv.service.ChosenSongInfo;
import io.agora.scene.ktv.widget.song.OnSongActionListener;
import io.agora.scene.ktv.widget.song.SongDialog;
import io.agora.scene.ktv.widget.song.SongItem;

/**
 * 点歌台 listener
 */
public class SongActionListenerImpl implements OnSongActionListener {
    private final LifecycleOwner mLifecycleOwner;
    private final RoomLivingViewModel mViewModel;
    private final boolean isChorus;

    /**
     * Instantiates a new Song action listener.
     *
     * @param activity  the activity
     * @param viewModel the view model
     * @param isChorus  the is chorus
     */
    public SongActionListenerImpl(
            LifecycleOwner activity,
            RoomLivingViewModel viewModel,
            boolean isChorus) {
        this.mLifecycleOwner = activity;
        this.mViewModel = viewModel;
        this.isChorus = isChorus;
    }

    @Override
    public void onChooseSongRefreshing(@NonNull SongDialog dialog) {
        // order- refresh song list
        LiveDataUtils.observerThenRemove(mLifecycleOwner, mViewModel.getSongList(), list -> {
            if (dialog.isVisible()) {
                dialog.setChooseRefreshingResult(transSongModel(list));
            }
        });
    }

    @Override
    public void onChooseSongChosen(@NonNull SongDialog dialog, @NonNull SongItem songItem) {
        LiveDataUtils.observerThenRemove(mLifecycleOwner, mViewModel.chooseSong(songItem, isChorus), success -> {
            if (success && dialog.isVisible()) {
                dialog.setChooseSongItemStatus(songItem, true);
            } else if (!success) { // order failed
                songItem.loading = false;
                dialog.setChooseSongItemStatus(songItem, false);
            }
        });
    }

    @Override
    public void onChosenSongDeleteClicked(@NonNull SongDialog dialog, @NonNull SongItem song) {
        // delete song
        ChosenSongInfo songModel = song.getTag(ChosenSongInfo.class);
        mViewModel.deleteSong(songModel);
    }

    @Override
    public void onChosenSongTopClicked(@NonNull SongDialog dialog, @NonNull SongItem song) {
        // pin song
        ChosenSongInfo songModel = song.getTag(ChosenSongInfo.class);
        mViewModel.pinSong(songModel);
    }

    /**
     * Trans song model list.
     *
     * @param data the data
     * @return the list
     */
    public static List<SongItem> transSongModel(@Nullable List<ChosenSongInfo> data) {
        ArrayList<SongItem> list = new ArrayList<>();
        if (data != null) {
            for (ChosenSongInfo song : data) {
                String userName = "";
                String chooserUserId = "";
                if (song.getOwner() != null) {
                    userName = song.getOwner().userName;
                    chooserUserId = song.getOwner().userId;
                }
                SongItem item = new SongItem(
                        song.getSongNo(),
                        song.getSongName(),
                        song.getImageUrl(),
                        song.getSinger(),
                        userName,
                        !TextUtils.isEmpty(userName),
                        chooserUserId
                );
                item.setTag(song);
                list.add(item);
            }
        }
        return list;
    }
}
