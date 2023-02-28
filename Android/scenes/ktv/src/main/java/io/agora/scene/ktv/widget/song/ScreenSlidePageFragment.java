package io.agora.scene.ktv.widget.song;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.List;

import io.agora.scene.ktv.R;

/**
 * ---------------------------------------------------------------------------------------------
 * 功能描述:
 * ---------------------------------------------------------------------------------------------
 * 时　　间: 2023/2/28
 * ---------------------------------------------------------------------------------------------
 * 代码创建: Leo
 * ---------------------------------------------------------------------------------------------
 * 代码备注:
 * ---------------------------------------------------------------------------------------------
 **/
public class ScreenSlidePageFragment extends Fragment {

    private SmartRefreshLayout smartRefreshLayout;
    private RecyclerView rvRankList;
    private OnScreenSlidePageFragmentCallBack callBack;
    private final SongChooseViewAdapter mRankListAdapter = new SongChooseViewAdapter() {
        @Override
        void onSongChosen(SongItem song, int position) {
            callBack.onClickSongItem(song);
        }
    };

    public ScreenSlidePageFragment(OnScreenSlidePageFragmentCallBack callBack) {
        this.callBack = callBack;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.fragment_screen_slide_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        smartRefreshLayout = view.findViewById(R.id.smart_refresh_layout);
        rvRankList = view.findViewById(R.id.rvRankList);
        rvRankList.setAdapter(mRankListAdapter);
        smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            callBack.onRefresh(refreshLayout);
        });
        smartRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
            callBack.onLoadMore(refreshLayout);
        });
    }

    public void setRefreshingResult(List<SongItem> list) {
        mRankListAdapter.resetAll(list);

        smartRefreshLayout.setEnableLoadMore(true);
        smartRefreshLayout.finishRefresh();
    }

    public void setLoadMoreResult(List<SongItem> list, boolean hasMore) {
        mRankListAdapter.insertAll(list);
        smartRefreshLayout.finishLoadMore();
        smartRefreshLayout.setEnableLoadMore(hasMore);
    }

    public void setSongItemStatus(SongItem songItem, boolean isChosen) {
        int itemCount = mRankListAdapter.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            SongItem item = mRankListAdapter.getItem(i);
            if (item.songNo.equals(songItem.songNo)) {
                item.isChosen = isChosen;
                mRankListAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    public void onTabSelected(int position) {
        mRankListAdapter.resetAll(null);
    }


    public interface OnScreenSlidePageFragmentCallBack {

        void onRefresh(@NonNull RefreshLayout refreshLayout);

        void onLoadMore(@NonNull RefreshLayout refreshLayout);

        void onClickSongItem(SongItem songItem);

    }
}
