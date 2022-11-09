package io.agora.scene.ktv.live.listener;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.agora.scene.base.utils.LiveDataUtils;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.live.RoomLivingViewModel;
import io.agora.scene.ktv.service.VLRoomSelSongModel;
import io.agora.scene.ktv.widget.song.OnSongActionListener;
import io.agora.scene.ktv.widget.song.SongDialog;
import io.agora.scene.ktv.widget.song.SongItem;

public class SongActionListenerImpl implements OnSongActionListener {
    private static final Map<Integer, RoomLivingViewModel.SongType> sSongTypeMap = new LinkedHashMap<>();

    static {
        sSongTypeMap.put(R.string.song_rank_2, RoomLivingViewModel.SongType.HI_SONG);
        sSongTypeMap.put(R.string.song_rank_3, RoomLivingViewModel.SongType.TICKTOK);
        sSongTypeMap.put(R.string.song_rank_4, RoomLivingViewModel.SongType.CLASSICAL);
        sSongTypeMap.put(R.string.song_rank_5, RoomLivingViewModel.SongType.KTV);
    }

    private final LifecycleOwner mLifecycleOwner;
    private final RoomLivingViewModel mViewModel;
    private final boolean isChorus;
    private int mCurrPage = 1;


    public SongActionListenerImpl(
            LifecycleOwner activity,
            RoomLivingViewModel viewModel,
            boolean isChorus) {
        mLifecycleOwner = activity;
        mViewModel = viewModel;
        this.isChorus = isChorus;
    }

    @Override
    public void onChooseSongRefreshing(@NonNull SongDialog dialog, int index) {
        // 点歌-列表刷新
        mCurrPage = 1;
        LiveDataUtils.observerThenRemove(mLifecycleOwner,
                mViewModel.getSongList(getSongType(index), mCurrPage),
                list -> {
                    dialog.setChooseRefreshingResult(transSongModel(list));
                });
    }

    @Override
    public void onChooseSongLoadMore(@NonNull SongDialog dialog, int index) {
        // 点歌-列表加载更多
        mCurrPage++;
        LiveDataUtils.observerThenRemove(mLifecycleOwner,
                mViewModel.getSongList(getSongType(index), mCurrPage),
                list -> {
                    dialog.setChooseLoadMoreResult(transSongModel(list),
                            list.size() > 0);
                });
    }

    @Override
    public void onChooseSongSearching(@NonNull SongDialog dialog, String condition) {
        // 点歌-搜索
        LiveDataUtils.observerThenRemove(mLifecycleOwner,
                mViewModel.searchSong(condition),
                list -> {
                    dialog.setChooseSearchResult(transSongModel(list));
                });
    }

    @Override
    public void onChooseSongChosen(@NonNull SongDialog dialog, @NonNull SongItem songItem) {
        // 点歌
        VLRoomSelSongModel songModel = songItem.getTag(VLRoomSelSongModel.class);
        LiveDataUtils.observerThenRemove(mLifecycleOwner,
                mViewModel.chooseSong(songModel, isChorus),
                success -> {
                    if (success) {
                        dialog.setChooseSongItemStatus(songItem, true);
                    }
                });
    }

    @Override
    public void onChosenSongDeleteClicked(@NonNull SongDialog dialog, @NonNull SongItem song) {
        // 删歌
        VLRoomSelSongModel songModel = song.getTag(VLRoomSelSongModel.class);
        mViewModel.deleteSong(songModel);
    }

    @Override
    public void onChosenSongTopClicked(@NonNull SongDialog dialog, @NonNull SongItem song) {
        // 置顶
        VLRoomSelSongModel songModel = song.getTag(VLRoomSelSongModel.class);
        mViewModel.topUpSong(songModel);
    }

    public List<String> getSongTypeTitles(Context context) {
        List<String> titles = new ArrayList<>();
        for (Map.Entry<Integer, RoomLivingViewModel.SongType> entry : sSongTypeMap.entrySet()) {
            titles.add(context.getString(entry.getKey()));
        }
        return titles;
    }

    private RoomLivingViewModel.SongType getSongType(int index) {
        int i = 0;
        for (Map.Entry<Integer, RoomLivingViewModel.SongType> entry : sSongTypeMap.entrySet()) {
            if (index == i) {
                return entry.getValue();
            }
            i++;
        }
        throw new RuntimeException("songsDialogGetSongType out of index: " + index);
    }

    public static List<SongItem> transSongModel(@Nullable List<VLRoomSelSongModel> data) {
        ArrayList<SongItem> list = new ArrayList<>();
        if (data != null) {
            for (VLRoomSelSongModel song : data) {
                SongItem item = new SongItem(
                        song.getSongNo(),
                        song.getSongName(),
                        song.getImageUrl(),

                        song.getName(),
                        !TextUtils.isEmpty(song.getName()),
                        song.isChorus()
                );
                item.setTag(song);
                list.add(item);
            }
        }
        return list;
    }
}
