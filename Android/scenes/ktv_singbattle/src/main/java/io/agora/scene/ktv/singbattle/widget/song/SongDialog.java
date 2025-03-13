package io.agora.scene.ktv.singbattle.widget.song;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import io.agora.scene.base.component.BaseBottomSheetDialogFragment;
import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleDialogChooseSongBinding;
import io.agora.scene.ktv.singbattle.live.listener.SongActionListenerImpl;
import io.agora.scene.widget.toast.CustomToast;
import io.agora.scene.widget.utils.UiUtils;

/**
 * Choose song menu
 */
public class SongDialog extends BaseBottomSheetDialogFragment<KtvSingbattleDialogChooseSongBinding> {

    private OnSongActionListener chooseSongListener;

    private final SongChosenFragment songChosenFragment = new SongChosenFragment();
    private final SongChooseFragment songChooseFragment = new SongChooseFragment();
    private int chosenSongCount = 0;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(requireDialog().getWindow().getDecorView(), (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            mBinding.pager.setPadding(0, 0, 0, inset.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        mBinding.rBtnChooseSong.setChecked(true);
        mBinding.pager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        mBinding.getRoot().getRootView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mBinding.getRoot().getRootView().setOnSystemUiVisibilityChangeListener(visibility -> {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            uiOptions |= 0x00001000;
            mBinding.getRoot().getRootView().setSystemUiVisibility(uiOptions);
        });

        songChooseFragment.setListener(new SongChooseFragment.Listener() {
            @Override
            public void onSongItemChosen(@NonNull SongItem songItem) {
                if (UiUtils.isFastClick(500)) {
                    return;
                }
                if (chooseSongListener != null) {
                    chooseSongListener.onChooseSongChosen(SongDialog.this, songItem);
                }
            }

            @Override
            public void onSongsSearching(String condition) {
                if (chooseSongListener != null) {
                    chooseSongListener.onChooseSongSearching(SongDialog.this, condition);
                }
            }

            @Override
            public void onSongsRefreshing() {
                if (chooseSongListener != null) {
                    chooseSongListener.onChooseSongRefreshing(SongDialog.this, 0);
                }
            }

            @Override
            public void onSongsLoadMore() {
                if (chooseSongListener != null) {
                    chooseSongListener.onChooseSongLoadMore(SongDialog.this, 0);
                }
            }
        });
        songChosenFragment.setListener(new SongChosenFragment.Listener() {
            @Override
            public void onSongDeleteClicked(SongItem song) {
                if (chooseSongListener != null) {
                    chooseSongListener.onChosenSongDeleteClicked(SongDialog.this, song);
                }
            }

            @Override
            public void onSongTopClicked(SongItem song) {
                if (chooseSongListener != null) {
                    chooseSongListener.onChosenSongTopClicked(SongDialog.this, song);
                }
            }
        });
        Fragment[] fragments = new Fragment[]{songChooseFragment, songChosenFragment};
        mBinding.pager.setSaveEnabled(false);
        mBinding.pager.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), getViewLifecycleOwner().getLifecycle()) {
            @Override
            public int getItemCount() {
                return fragments.length;
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments[position];
            }
        });
        mBinding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    mBinding.rBtnChooseSong.setChecked(true);
                } else {
                    mBinding.rBtnChorus.setChecked(true);
                }
            }
        });
        mBinding.ilGameSong.btStartGame.setOnClickListener(v -> {
            if (chosenSong < 2) {
                CustomToast.show(R.string.ktv_singbattle_at_least_four);
                return;
            }
            if (chooseSongListener != null) {
                chooseSongListener.onStartSingBattleGame(this);
            }
            if (getDialog() != null) {
                getDialog().dismiss();
            }
        });
        setChosenSongCount(chosenSongCount);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBinding.radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.rBtnChooseSong) {
                mBinding.pager.setCurrentItem(0);
            } else if(i == R.id.rBtnChorus){
                mBinding.pager.setCurrentItem(1);
            }
        });
    }

    /**
     * Set event listener
     */
    public void setChooseSongListener(SongActionListenerImpl chooseSongListener) {
        this.chooseSongListener = chooseSongListener;
        chooseSongListener.getSongTypeList();
    }

    /**
     * Choose song - update item selected status
     */
    public void setChooseSongItemStatus(SongItem songItem, boolean isChosen) {
        songChooseFragment.setSongItemStatus(songItem, isChosen);
    }

    /**
     * 点歌-更新搜索列表
     */
    public void setChooseSearchResult(List<SongItem> list) {
        songChooseFragment.setSearchResult(list);
    }

    /**
     * 点歌-下拉刷新重置列表
     */
    public void setChooseRefreshingResult(List<SongItem> list,int index) {
        songChooseFragment.setRefreshingResult(list);
    }

    /**
     * 点歌-加载更多刷新列表
     */
    public void setChooseLoadMoreResult(List<SongItem> list, boolean hasMore, int index) {
        songChooseFragment.setLoadMoreResult(list, hasMore);
    }

    /**
     * 已点歌单-设置是否可以做删除置顶等操作
     */
    public void setChosenControllable(boolean controllable) {
        songChosenFragment.setControllable(controllable);
    }

    /**
     * 已点歌单-重置列表
     */
    private int chosenSong = 0;
    public void resetChosenSongList(List<SongItem> songs) {
        songChosenFragment.resetSongList(songs);
        this.chosenSongCount = songChosenFragment.getSongSize();
        setChosenSongCount(songChosenFragment.getSongSize());

        if (mBinding == null) return;
        mBinding.ilGameSong.tvSongNum.setText("已点 " + songs.size() + "/8");
        chosenSong = songs.size();
        if (songs.size() <= 3) {
            mBinding.ilGameSong.btStartGame.setBackgroundResource(R.mipmap.ktv_start_game_disabled);
        } else if (songs.size() <= 8) {
            mBinding.ilGameSong.btStartGame.setBackgroundResource(R.mipmap.ktv_start_game);
            mBinding.ilGameSong.btStartGame.setEnabled(true);
        }

        if (songs.size() == 8) {
            songChooseFragment.setSongItemDisable(false);
            CustomToast.show(R.string.ktv_singbattle_song_is_enough);
        } else if (songs.size() < 8) {
            songChooseFragment.setSongItemDisable(true);
        }
//        songChooseFragment.setRestSongStatus(songs);
    }

    /**
     * Chosen song list - add song
     */
    public void addChosenSongItem(SongItem song) {
        songChosenFragment.addSongItem(song);
        setChosenSongCount(songChosenFragment.getSongSize());
    }

    /**
     * Chosen song list - delete song
     */
    public void deleteChosenSongItem(SongItem song) {
        songChosenFragment.deleteSongItem(song);
        setChosenSongCount(songChosenFragment.getSongSize());
    }

    /**
     * 已点歌单-置顶歌曲
     */
    public void topUpChosenSongItem(SongItem song) {
        songChosenFragment.topUpSongItem(song);
    }

    private void setChosenSongCount(int count) {
        if(mBinding == null){
            return;
        }
        if (count > 0) {
            mBinding.tvChoosedSongCount.setVisibility(View.VISIBLE);
            if (count > 99) {
                count = 99;
            }
            mBinding.tvChoosedSongCount.setText(String.valueOf(count));
        } else {
            mBinding.tvChoosedSongCount.setVisibility(View.GONE);
        }
    }

    public void post(Runnable r) {

    }
}
