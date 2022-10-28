package io.agora.scene.ktv.live.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.base.manager.RoomManager;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvFragmentSongOrderListBinding;
import io.agora.scene.ktv.databinding.KtvItemChoosedSongListBinding;
import io.agora.scene.ktv.live.holder.ChosenSongViewHolder;

/**
 * 已点歌单列表
 */
public class SongOrdersFragment extends BaseViewBindingFragment<KtvFragmentSongOrderListBinding> implements OnItemClickListener<MemberMusicModel> {
    private SongsViewModel songsViewModel;

    @NonNull
    @Override
    protected KtvFragmentSongOrderListBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        return KtvFragmentSongOrderListBinding.inflate(layoutInflater);
    }

    private BaseRecyclerViewAdapter<KtvItemChoosedSongListBinding, MemberMusicModel, ChosenSongViewHolder> mAdapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void initView() {
        songsViewModel = new ViewModelProvider(this).get(SongsViewModel.class);
        songsViewModel.setLifecycleOwner(this);
        mAdapter = new BaseRecyclerViewAdapter<>(RoomManager.getInstance().getMusics(), this, ChosenSongViewHolder.class);
        getBinding().list.setAdapter(mAdapter);
    }

    @Override
    public void requestData() {
        songsViewModel.getSongOrdersList();
        songsViewModel.setISingleCallback((type, o) -> {
            if (type == 0) {
                RoomManager.getInstance().getLiveDataMusics().observe(getViewLifecycleOwner()
                        , memberMusicModels -> mAdapter.setDataList(memberMusicModels));
            }
        });
    }

    @Override
    public void onItemClick(@NonNull MemberMusicModel data, View view, int position, long viewType) {
        if (view.getId() == R.id.ivToDel) {
            songsViewModel.requestDeleteSong(data, position);
        } else if (view.getId() == R.id.ivToTop) {
            songsViewModel.requestTopSong(data);
        }
    }
}
