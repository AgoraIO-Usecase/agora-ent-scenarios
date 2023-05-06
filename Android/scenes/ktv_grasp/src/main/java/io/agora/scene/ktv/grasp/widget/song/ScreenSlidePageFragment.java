package io.agora.scene.ktv.grasp.widget.song;

import android.os.Bundle;
import android.util.Log;
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

import io.agora.scene.base.utils.GsonUtil;
import io.agora.scene.ktv.grasp.R;

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
    private int position;
    private final SongChooseViewAdapter mRankListAdapter = new SongChooseViewAdapter() {
        @Override
        void onSongChosen(SongItem song, int position) {
            if (callBack == null) {
                return;
            }
            callBack.onClickSongItem(song);
        }
    };

    public ScreenSlidePageFragment() {

    }

    public ScreenSlidePageFragment setCallBack(OnScreenSlidePageFragmentCallBack callBack, int position) {
        this.callBack = callBack;
        this.position = position;
        return this;
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
            if (callBack == null) {
                return;
            }
            callBack.onRefresh(refreshLayout);
        });
        smartRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
            if (callBack == null) {
                return;
            }
            callBack.onLoadMore(refreshLayout);
        });
    }

    public void setRefreshingResult(List<SongItem> list) {
        mRankListAdapter.resetAll(list);

        if (smartRefreshLayout == null) {
            return;
        }
        smartRefreshLayout.setEnableLoadMore(true);
        smartRefreshLayout.finishRefresh();
    }

    public void setLoadMoreResult(List<SongItem> list, boolean hasMore) {
        mRankListAdapter.insertAll(list);
        if (smartRefreshLayout == null) {
            return;
        }
        smartRefreshLayout.finishLoadMore();
        smartRefreshLayout.setEnableLoadMore(hasMore);
    }

    public void setSongItemStatus(SongItem songItem, boolean isChosen) {
        Log.e("liu0228", "setSongItemStatus    songItem = " + GsonUtil.getInstance().toJson(songItem) + "    isChosen = " + isChosen);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("liu0228", "onDestroyView    " + position);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("liu0228", "onDestroy    " + position);
    }
}
