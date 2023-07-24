package io.agora.scene.ktv.singrelay.live.listener;

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
import io.agora.scene.ktv.singrelay.live.RoomLivingViewModel;
import io.agora.scene.ktv.singrelay.service.RoomSelSongModel;
import io.agora.scene.ktv.singrelay.widget.song.OnSongActionListener;
import io.agora.scene.ktv.singrelay.widget.song.SongDialog;
import io.agora.scene.ktv.singrelay.widget.song.SongItem;

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
    public void onStartSingRelayGame(@NonNull SongDialog dialog) {
        mViewModel.startSingRelayGame();
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
