package io.agora.scene.ktv.singbattle.live.listener;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.agora.scene.base.utils.LiveDataUtils;
import io.agora.scene.ktv.singbattle.live.RoomLivingViewModel;
import io.agora.scene.ktv.singbattle.service.RoomSelSongModel;
import io.agora.scene.ktv.singbattle.widget.song.OnSongActionListener;
import io.agora.scene.ktv.singbattle.widget.song.SongDialog;
import io.agora.scene.ktv.singbattle.widget.song.SongItem;

public class SongActionListenerImpl implements OnSongActionListener {
    private final LifecycleOwner mLifecycleOwner;
    private final RoomLivingViewModel mViewModel;
    private final LinkedHashMap<Integer, String> songTypeMap;
    private int mCurrPage = 1;

    public SongActionListenerImpl(
            LifecycleOwner activity,
            RoomLivingViewModel viewModel,
            LinkedHashMap<Integer, String> songTypeMap) {
        this.songTypeMap = songTypeMap;
        mLifecycleOwner = activity;
        mViewModel = viewModel;
    }

    @Override
    public void onChooseSongRefreshing(@NonNull SongDialog dialog, int index) {
        // 点歌-列表刷新
        mCurrPage = 1;
        int songType = getSongType(index);
        Log.e("liu0228", "index = " + index + "    songType = " + songType);
        LiveDataUtils.observerThenRemove(mLifecycleOwner, mViewModel.getSongList(songType, mCurrPage), list -> {
            if (dialog.isVisible()) {
                dialog.setChooseRefreshingResult(transSongModel(list), index);
            }
        });
    }

    @Override
    public void onChooseSongLoadMore(@NonNull SongDialog dialog, int index) {
        // 点歌-列表加载更多
        mCurrPage++;
        LiveDataUtils.observerThenRemove(mLifecycleOwner, mViewModel.getSongList(getSongType(index), mCurrPage), list -> {
            if (dialog.isVisible()) {
                dialog.setChooseLoadMoreResult(transSongModel(list), list.size() > 0, index);
            }
        });
    }

    @Override
    public void onChooseSongSearching(@NonNull SongDialog dialog, String condition) {
        // 点歌-搜索
        LiveDataUtils.observerThenRemove(mLifecycleOwner,
                mViewModel.searchSong(condition),
                list -> {
                    if (dialog.isVisible()) {
                        dialog.setChooseSearchResult(transSongModel(list));
                    }
                });
    }

    @Override
    public void onChooseSongChosen(@NonNull SongDialog dialog, @NonNull SongItem songItem) {
        // 点歌
        RoomSelSongModel songModel = songItem.getTag(RoomSelSongModel.class);
        LiveDataUtils.observerThenRemove(mLifecycleOwner, mViewModel.chooseSong(songModel), success -> {
            if (success && dialog.isVisible()) {
                dialog.setChooseSongItemStatus(songItem, true);
            }
        });
    }

    @Override
    public void onChosenSongDeleteClicked(@NonNull SongDialog dialog, @NonNull SongItem song) {
        // 删歌
        RoomSelSongModel songModel = song.getTag(RoomSelSongModel.class);
        mViewModel.deleteSong(songModel);
    }

    @Override
    public void onChosenSongTopClicked(@NonNull SongDialog dialog, @NonNull SongItem song) {
        // 置顶
        RoomSelSongModel songModel = song.getTag(RoomSelSongModel.class);
        mViewModel.topUpSong(songModel);
    }

    @Override
    public void onStartSingBattleGame(@NonNull SongDialog dialog) {
        mViewModel.startSingBattleGame();
    }

    public List<String> getSongTypeTitles(Context context) {
        List<String> titles = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : songTypeMap.entrySet()) {
            titles.add(entry.getValue());
        }
        return titles;
    }

    public List<Integer> getSongTypeList() {
        List<Integer> list = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : songTypeMap.entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }

    private int getSongType(int index) {
        int i = 0;
        for (Map.Entry<Integer, String> entry : songTypeMap.entrySet()) {
            if (index == i) {
                return entry.getKey();
            }
            i++;
        }
        throw new RuntimeException("songsDialogGetSongType out of index: " + index);
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
