package io.agora.scene.ktv.live.fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.base.data.model.BaseMusicModel;
import io.agora.scene.base.data.model.MusicModelNew;
import io.agora.scene.base.manager.RoomManager;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvFragmentSongListBinding;
import io.agora.scene.ktv.databinding.KtvItemChooseSongListBinding;
import io.agora.scene.ktv.live.holder.ChooseSongViewHolder;
import io.agora.scene.widget.utils.AnimUtils;

/**
 * 歌单列表
 */
public class SongsFragment extends BaseViewBindingFragment<KtvFragmentSongListBinding> implements OnItemClickListener<MusicModelNew> {
    private BaseRecyclerViewAdapter<KtvItemChooseSongListBinding,
            MusicModelNew, ChooseSongViewHolder> mAdapter;
    private BaseRecyclerViewAdapter<KtvItemChooseSongListBinding,
            MusicModelNew, ChooseSongViewHolder> mSearchAdapter;
    private SongsViewModel songsViewModel;

    @NonNull
    @Override
    protected KtvFragmentSongListBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        return KtvFragmentSongListBinding.inflate(layoutInflater);
    }

    @Override
    public void initView() {
        songsViewModel = new ViewModelProvider(this).get(SongsViewModel.class);
        songsViewModel.setLifecycleOwner(this);
        mAdapter = new BaseRecyclerViewAdapter<>(null, this, ChooseSongViewHolder.class);
        getBinding().rvRankList.setAdapter(mAdapter);
        mSearchAdapter = new BaseRecyclerViewAdapter<>(null, this, ChooseSongViewHolder.class);
        getBinding().recyclerSearchResult.setAdapter(mSearchAdapter);
        getBinding().rBtnRand2.setChecked(true);
        getBinding().rBtnRand2.post(() -> {
            startLineAnim(R.id.rBtnRand2);
        });
    }

    @Override
    public void initListener() {
        songsViewModel.setISingleCallback((type, o) -> {
            if (type == 0) {
                mAdapter.notifyItemChanged(position);
            } else if (type == 1) {
                getBinding().smartRefreshLayout.finishLoadMore();
                getBinding().smartRefreshLayout.finishRefresh();
                onLoadMusics(((BaseMusicModel) o).records);
            } else if (type == 2) {
                mSearchAdapter.setDataList(((BaseMusicModel) o).records);
                if (mSearchAdapter.dataList.isEmpty()) {
                    getBinding().llEmpty.setVisibility(View.VISIBLE);
                } else {
                    getBinding().llEmpty.setVisibility(View.GONE);
                }
            }
        });
        getBinding().llEmpty.setOnClickListener(this::doLoadMusics);
        getBinding().etSearch.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH) {
                    doLoadMusics(view);
                }
                return true;
            }
            return false;
        });
        getBinding().etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable == null || editable.length() == 0) {
                    getBinding().iBtnClear.setVisibility(View.GONE);
                    getBinding().smartRefreshLayout.setVisibility(View.VISIBLE);
                    getBinding().recyclerSearchResult.setVisibility(View.GONE);
                    getBinding().hScrollView.setVisibility(View.VISIBLE);
                    getBinding().llEmpty.setVisibility(View.GONE);
                } else {
                    getBinding().iBtnClear.setVisibility(View.VISIBLE);
                    getBinding().smartRefreshLayout.setVisibility(View.GONE);
                    getBinding().recyclerSearchResult.setVisibility(View.VISIBLE);
                    getBinding().hScrollView.setVisibility(View.GONE);
                    getBinding().llEmpty.setVisibility(View.GONE);
                }
            }
        });
        getBinding().radioGroup.post(() -> {
            startLineAnim(R.id.rBtnRand2);
        });
        getBinding().iBtnClear.setOnClickListener(view -> getBinding().etSearch.setText(""));
        getBinding().radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            startLineAnim(i);
        });
        getBinding().smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            refreshData();
        });
        getBinding().smartRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
            if (songsViewModel.haveMore) {
                songsViewModel.page++;
                loadRank(hotType);
            } else {
                getBinding().smartRefreshLayout.finishLoadMore();
            }
        });
    }

    private void refreshData() {
        songsViewModel.haveMore = true;
        songsViewModel.page = 1;
        loadRank(hotType);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRank(hotType);
    }

    private int hotType = 1;

    private void startLineAnim(int id) {
        songsViewModel.page = 1;
        if (id == R.id.rBtnRand2) {
            hotType = 1;
            loadRank(1);
            AnimUtils.radioGroupLineAnim(getBinding().rBtnRand2, getBinding().ivLine);
        } else if (id == R.id.rBtnRand3) {
            loadRank(2);
            hotType = 2;
            AnimUtils.radioGroupLineAnim(getBinding().rBtnRand3, getBinding().ivLine);
        } else if (id == R.id.rBtnRand4) {
            loadRank(3);
            hotType = 3;
            AnimUtils.radioGroupLineAnim(getBinding().rBtnRand4, getBinding().ivLine);
        } else if (id == R.id.rBtnRand5) {
            loadRank(4);
            hotType = 4;
            AnimUtils.radioGroupLineAnim(getBinding().rBtnRand5, getBinding().ivLine);
        }
    }

    private void loadMusics(String searchKey) {
        if (TextUtils.isEmpty(searchKey)) return;
        songsViewModel.searchSong(searchKey);
    }

    private void loadRank(int hotType) {
        songsViewModel.getHotBinkList(hotType);
    }

    private void onLoadMusics(List<MusicModelNew> list) {
        if (mAdapter.dataList.isEmpty()) {
            getBinding().llEmpty.setVisibility(View.VISIBLE);
        } else {
            getBinding().llEmpty.setVisibility(View.GONE);
        }
        if (songsViewModel.page == 1) {
            mAdapter.setDataList(list);
        } else {
            int oldSize = mAdapter.dataList.size();
            mAdapter.dataList.addAll(list);
            mAdapter.notifyItemRangeInserted(oldSize, mAdapter.dataList.size() - oldSize - 1);
        }
    }

    private int position;

    private long lastClickTime = 0L;

    @Override
    public void onItemClick(@NonNull MusicModelNew data, View view, int position, long viewType) {
        if (System.currentTimeMillis() - lastClickTime < 1000) {
            return;
        }
        lastClickTime = System.currentTimeMillis();
        this.position = position;
        if (!RoomManager.getInstance().isInMusicOrderList(data)) {
            Log.d("cwtsw", "点歌 " + data.songName);
            songsViewModel.requestChooseSong(data);
        }
    }

    private void doLoadMusics(View v) {
//        getBinding().refreshLayoutFgSong.setRefreshing(false);
        loadMusics(getBinding().etSearch.getText().toString());
    }

    @Override
    public void onStart() {
        super.onStart();
        songsViewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        songsViewModel.onStop();
    }
}
