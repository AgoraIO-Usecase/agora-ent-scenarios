package io.agora.scene.ktv.widget.song;

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
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvDialogChooseSongBinding;
import io.agora.scene.ktv.live.listener.SongActionListenerImpl;
import io.agora.scene.widget.utils.UiUtils;

/**
 * Song menu
 */
public class SongDialog extends BaseBottomSheetDialogFragment<KtvDialogChooseSongBinding> {

    private OnSongActionListener chooseSongListener;

    private final SongChosenFragment songChosenFragment = new SongChosenFragment();
    private final SongChooseFragment songChooseFragment = new SongChooseFragment();

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
                    chooseSongListener.onChooseSongRefreshing(SongDialog.this,0);
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
        setChosenSongCount(0);
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
     *  set event listener
     * @param chooseSongListener
     */
    public void setChooseSongListener(SongActionListenerImpl chooseSongListener) {
        this.chooseSongListener = chooseSongListener;
        chooseSongListener.getSongTypeList();
    }

    /**
     * Order - update item selected state.
     *
     * @param songItem the song item
     * @param isChosen the is chosen
     */
    public void setChooseSongItemStatus(SongItem songItem, boolean isChosen) {
        songChooseFragment.setSongItemStatus(songItem, isChosen);
    }

    /**
     * 点歌-更新搜索列表
     *
     * @param list the list
     */
    public void setChooseSearchResult(List<SongItem> list) {
        songChooseFragment.setSearchResult(list);
    }

    /**
     * 点歌-下拉刷新重置列表
     *
     * @param list  the list
     * @param index the index
     */
    public void setChooseRefreshingResult(List<SongItem> list,int index) {
        songChooseFragment.setRefreshingResult(list);
    }

    /**
     * 点歌-加载更多刷新列表
     *
     * @param list    the list
     * @param hasMore the has more
     * @param index   the index
     */
    public void setChooseLoadMoreResult(List<SongItem> list, boolean hasMore, int index) {
        songChooseFragment.setLoadMoreResult(list, hasMore);
    }

    /**
     * Queued song list - set whether actions like delete or pin to top are allowed.
     *
     * @param controllable the controllable
     */
    public void setChosenControllable(boolean controllable) {
        songChosenFragment.setControllable(controllable);
    }

    /**
     * Queued song list - reset the list.
     *
     * @param songs the songs
     */
    public void resetChosenSongList(List<SongItem> songs) {
        songChosenFragment.resetSongList(songs);
        setChosenSongCount(songChosenFragment.getSongSize());

        songChooseFragment.setRestSongStatus(songs);
    }

    /**
     *  Queued song list -add song item.
     *
     * @param song the song
     */
    public void addChosenSongItem(SongItem song) {
        songChosenFragment.addSongItem(song);
        setChosenSongCount(songChosenFragment.getSongSize());
    }

    /**
     *  Queued song list - delete song item.
     *
     * @param song the song
     */
    public void deleteChosenSongItem(SongItem song) {
        songChosenFragment.deleteSongItem(song);
        setChosenSongCount(songChosenFragment.getSongSize());
    }

    /**
     *  Queued song list - pin song item.
     *
     * @param song the song
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

}
