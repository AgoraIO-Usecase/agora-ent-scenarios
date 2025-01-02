package io.agora.scene.ktv.singbattle.live.listener;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.List;

import io.agora.scene.base.utils.LiveDataUtils;
import io.agora.scene.ktv.singbattle.live.RoomLivingViewModel;
import io.agora.scene.ktv.singbattle.service.RoomSelSongModel;
import io.agora.scene.ktv.singbattle.widget.song.OnSongActionListener;
import io.agora.scene.ktv.singbattle.widget.song.SongDialog;
import io.agora.scene.ktv.singbattle.widget.song.SongItem;

public class SongActionListenerImpl implements OnSongActionListener {
    private final LifecycleOwner mLifecycleOwner;
    private final RoomLivingViewModel mViewModel;
    private final boolean isChorus;

    public SongActionListenerImpl(
            LifecycleOwner activity,
            RoomLivingViewModel viewModel,
            boolean isChorus) {
        mLifecycleOwner = activity;
        mViewModel = viewModel;
        this.isChorus = isChorus;
    }

    @Override
    public void onChooseSongRefreshing(@NonNull SongDialog dialog) {
        // Choose song - list refresh
        LiveDataUtils.observerThenRemove(mLifecycleOwner, mViewModel.getSongList(), list -> {
            if (dialog.isVisible()) {
                dialog.setChooseRefreshingResult(transSongModel(list));
            }
        });
    }

    @Override
    public void onChooseSongChosen(@NonNull SongDialog dialog, @NonNull SongItem songItem) {
        // Choose song
        RoomSelSongModel songModel = songItem.getTag(RoomSelSongModel.class);
        LiveDataUtils.observerThenRemove(mLifecycleOwner, mViewModel.chooseSong(songModel), success -> {
            if (success && dialog.isVisible()) {
                dialog.setChooseSongItemStatus(songItem, true);
            } else if (!success) {
                songItem.loading = false;
                dialog.setChooseSongItemStatus(songItem, false);
            }
        });
    }

    @Override
    public void onChosenSongDeleteClicked(@NonNull SongDialog dialog, @NonNull SongItem song) {
        // Delete song
        RoomSelSongModel songModel = song.getTag(RoomSelSongModel.class);
        mViewModel.deleteSong(songModel);
    }

    @Override
    public void onChosenSongTopClicked(@NonNull SongDialog dialog, @NonNull SongItem song) {
        // Top song
        RoomSelSongModel songModel = song.getTag(RoomSelSongModel.class);
        mViewModel.topUpSong(songModel);
    }

    @Override
    public void onStartSingBattleGame(@NonNull SongDialog dialog) {
        mViewModel.startSingBattleGame();
    }

    public static List<SongItem> transSongModel(@Nullable List<RoomSelSongModel> data) {
        ArrayList<SongItem> list = new ArrayList<>();
        if (data != null) {
            for (RoomSelSongModel song : data) {
                SongItem item = new SongItem(
                        song.getSongNo(),
                        song.getSongName(),
                        song.getImageUrl(),
                        song.getSinger(),
                        song.getName(),
                        !TextUtils.isEmpty(song.getName()),
                        song.getUserNo()
                );
                item.setTag(song);
                list.add(item);
            }
        }
        return list;
    }
}
