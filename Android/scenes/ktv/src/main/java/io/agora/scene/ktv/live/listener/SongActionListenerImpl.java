package io.agora.scene.ktv.live.listener;

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
    private final LinkedHashMap<Integer, String> songTypeMap;
    private int mCurrPage = 1;


    /**
     * Instantiates a new Song action listener.
     *
     * @param activity    the activity
     * @param viewModel   the view model
     * @param songTypeMap the song type map
     * @param isChorus    the is chorus
     */
    public SongActionListenerImpl(
            LifecycleOwner activity,
            RoomLivingViewModel viewModel,
            LinkedHashMap<Integer, String> songTypeMap,
            boolean isChorus) {
        this.songTypeMap = songTypeMap;
        mLifecycleOwner = activity;
        mViewModel = viewModel;
        this.isChorus = isChorus;
    }

    @Override
    public void onChooseSongRefreshing(@NonNull SongDialog dialog, int index) {
        // 点歌-列表刷新
        mCurrPage = 1;
        int songType = getSongType(index);
        if (songType==-1) {
            Log.e("KTV","getSongType null");
            return;
        }

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

        int songType = getSongType(index);
        if (songType == -1) {
            Log.e("KTV", "getSongType null");
            return;
        }
        LiveDataUtils.observerThenRemove(mLifecycleOwner, mViewModel.getSongList(songType, mCurrPage), list -> {
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
        LiveDataUtils.observerThenRemove(mLifecycleOwner, mViewModel.chooseSong(songItem, isChorus), success -> {
            if (success && dialog.isVisible()) {
                dialog.setChooseSongItemStatus(songItem, true);
            }
        });
    }

    @Override
    public void onChosenSongDeleteClicked(@NonNull SongDialog dialog, @NonNull SongItem song) {
        // 删歌
        ChosenSongInfo songModel = song.getTag(ChosenSongInfo.class);
        mViewModel.deleteSong(songModel);
    }

    @Override
    public void onChosenSongTopClicked(@NonNull SongDialog dialog, @NonNull SongItem song) {
        // 置顶
        ChosenSongInfo songModel = song.getTag(ChosenSongInfo.class);
        mViewModel.topUpSong(songModel);
    }

    /**
     * Gets song type titles.
     *
     * @param context the context
     * @return the song type titles
     */
    public List<String> getSongTypeTitles(Context context) {
        List<String> titles = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : songTypeMap.entrySet()) {
            titles.add(entry.getValue());
        }
        return titles;
    }

    /**
     * Gets song type list.
     *
     * @return the song type list
     */
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
        return -1;
//        throw new RuntimeException("songsDialogGetSongType out of index: " + index);
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
                String chooserUserId = "" ;
                if (song.getOwner()!=null){
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
