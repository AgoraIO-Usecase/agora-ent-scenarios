package io.agora.scene.ktv.widget.song;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.ktv.databinding.KtvFragmentSongListBinding;

/**
 * 歌单列表
 */
public final class SongChooseFragment extends BaseViewBindingFragment<KtvFragmentSongListBinding>
        implements OnItemClickListener<SongItem> {

    private Listener listener;
    private final List<Runnable> pendingViewCreatedRuns = new ArrayList<>();

    private final SongChooseViewAdapter mSearchAdapter = new SongChooseViewAdapter() {
        @Override
        void onSongChosen(SongItem song, int position) {
            onSongItemChosen(song);
        }
    };

    private ScreenSlidePageFragment[] fragments = new ScreenSlidePageFragment[4];


    @NonNull
    @Override
    protected KtvFragmentSongListBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        return KtvFragmentSongListBinding.inflate(layoutInflater);
    }

    @Override
    public void initView() {
        getBinding().tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (fragments[position] != null) {
                    fragments[position].onTabSelected(position);
                }
                onSongsRefreshing(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        ScreenSlidePageFragment.OnScreenSlidePageFragmentCallBack callBack = new ScreenSlidePageFragment.OnScreenSlidePageFragmentCallBack() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                onSongsRefreshing(getBinding().tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                onSongsLoadMore(getBinding().tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onClickSongItem(SongItem songItem) {
                onSongItemChosen(songItem);
            }

        };
        getBinding().mViewPager2.setAdapter(new FragmentStateAdapter(getActivity()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (fragments[position] == null) {
                    fragments[position] = new ScreenSlidePageFragment();
                }
                fragments[position].setCallBack(callBack, position);
                return fragments[position];
            }

            @Override
            public int getItemCount() {
                return fragments.length;
            }
        });
        getBinding().mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (getBinding().tabLayout.getSelectedTabPosition() == position) {
                    return;
                }
                TabLayout.Tab tabAt = getBinding().tabLayout.getTabAt(position);
                if (tabAt == null) {
                    return;
                }
                getBinding().tabLayout.selectTab(tabAt);
            }
        });
        getBinding().recyclerSearchResult.setAdapter(mSearchAdapter);

        Iterator<Runnable> iterator = pendingViewCreatedRuns.iterator();
        while (iterator.hasNext()) {
            Runnable next = iterator.next();
            next.run();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Objects.requireNonNull(getBinding().etSearch.getText()).toString().equals("")) {
            onSongsSearching(getBinding().etSearch.getText().toString());
        }
    }

    @Override
    public void initListener() {
        getBinding().llEmpty.setOnClickListener(v -> onSongsRefreshing(getBinding().tabLayout.getSelectedTabPosition()));
        getBinding().etSearch.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH) {
                    onSongsSearching(getBinding().etSearch.getText().toString());
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
                    getBinding().mViewPager2.setVisibility(View.VISIBLE);
                    getBinding().recyclerSearchResult.setVisibility(View.GONE);
                    getBinding().hScrollView.setVisibility(View.VISIBLE);
                    getBinding().llEmpty.setVisibility(View.GONE);
                } else {
                    getBinding().iBtnClear.setVisibility(View.VISIBLE);
                    getBinding().mViewPager2.setVisibility(View.GONE);
                    getBinding().recyclerSearchResult.setVisibility(View.VISIBLE);
                    getBinding().hScrollView.setVisibility(View.GONE);
                    getBinding().llEmpty.setVisibility(View.GONE);
                }
            }
        });
        getBinding().iBtnClear.setOnClickListener(view -> getBinding().etSearch.setText(""));
    }

    private void runOnViewCreated(Runnable runnable) {
        View view = getView();
        if (view == null) {
            pendingViewCreatedRuns.add(runnable);
        } else {
            runnable.run();
        }
    }

    int getCurrentTabIndex() {
        if (getView() == null) {
            return 0;
        }
        return getBinding().tabLayout.getSelectedTabPosition();
    }

    void setSongTagsTitle(List<String> titles, List<Integer> types, int defaultIndex) {
        runOnViewCreated(() -> {
            for (String title : titles) {
                getBinding().tabLayout.addTab(getBinding().tabLayout.newTab().setText(title));
            }
            TabLayout.Tab tabAt = getBinding().tabLayout.getTabAt(defaultIndex);
            if (tabAt != null) {
                getBinding().tabLayout.selectTab(tabAt);
            }


        });
    }

    void setSongItemStatus(SongItem songItem, boolean isChosen) {
        if (getBinding().recyclerSearchResult.getVisibility() == View.VISIBLE) {
            int searchCount = mSearchAdapter.getItemCount();
            for (int i = 0; i < searchCount; i++) {
                SongItem item = mSearchAdapter.getItem(i);
                if (item.songNo.equals(songItem.songNo)) {
                    item.isChosen = isChosen;
                    mSearchAdapter.notifyItemChanged(i);
                    break;
                }
            }
        } else {
            for (ScreenSlidePageFragment fragment : fragments) {
                if (fragment == null) {
                    continue;
                }
                fragment.setSongItemStatus(songItem, isChosen);
            }
        }
    }

    void setSearchResult(List<SongItem> list) {
        if (list == null || list.isEmpty()) {
            getBinding().llEmpty.setVisibility(View.VISIBLE);
        } else {
            getBinding().llEmpty.setVisibility(View.GONE);
        }
        mSearchAdapter.resetAll(list);
    }

    void setRefreshingResult(List<SongItem> list, int index) {
        if (list == null || list.isEmpty()) {
            getBinding().llEmpty.setVisibility(View.VISIBLE);
        } else {
            getBinding().llEmpty.setVisibility(View.GONE);
        }
        if (fragments[index] != null) {
            fragments[index].setRefreshingResult(list);
        }
        enableTabLayoutClick(true);
    }

    void setLoadMoreResult(List<SongItem> list, boolean hasMore, int index) {
        if (fragments[index] != null) {
            fragments[index].setLoadMoreResult(list, hasMore);
        }
        enableTabLayoutClick(true);
    }

    private void enableTabLayoutClick(boolean enable) {
        KtvFragmentSongListBinding binding = getBinding();
        if (binding == null) {
            return;
        }
        TabLayout tabLayout = binding.tabLayout;
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).view.setClickable(enable);
        }
    }


    private void onSongItemChosen(@NonNull SongItem songItem) {
        if (listener != null) {
            listener.onSongItemChosen(songItem);
        }
    }

    private void onSongsSearching(String condition) {
        if (listener != null) {
            listener.onSongsSearching(condition);
        }
    }

    private void onSongsRefreshing(int tagIndex) {
        enableTabLayoutClick(false);
        if (listener != null) {
            listener.onSongsRefreshing(tagIndex);
        }
        getBinding().mViewPager2.setCurrentItem(tagIndex);
    }

    private void onSongsLoadMore(int tagIndex) {
        enableTabLayoutClick(false);
        if (listener != null) {
            listener.onSongsLoadMore(tagIndex);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onSongItemChosen(@NonNull SongItem songItem);

        void onSongsSearching(String condition);

        void onSongsRefreshing(int tagIndex);

        void onSongsLoadMore(int tagIndex);
    }
}
