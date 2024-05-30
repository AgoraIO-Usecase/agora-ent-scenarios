package io.agora.scene.ktv.widget.song;

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

import io.agora.rtmsyncmanager.utils.GsonTools;
import io.agora.scene.base.api.apiutils.GsonUtils;
import io.agora.scene.ktv.R;

/**
 * The type Screen slide page fragment.
 */
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

    /**
     * Instantiates a new Screen slide page fragment.
     */
    public ScreenSlidePageFragment() {

    }

    /**
     * Sets call back.
     *
     * @param callBack the call back
     * @param position the position
     * @return the call back
     */
    public ScreenSlidePageFragment setCallBack(OnScreenSlidePageFragmentCallBack callBack, int position) {
        this.callBack = callBack;
        this.position = position;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.ktv_fragment_screen_slide_page, container, false);
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

    /**
     * Sets refreshing result.
     *
     * @param list the list
     */
    public void setRefreshingResult(List<SongItem> list) {
        mRankListAdapter.resetAll(list);

        if (smartRefreshLayout == null) {
            return;
        }
        smartRefreshLayout.setEnableLoadMore(true);
        smartRefreshLayout.finishRefresh();
    }

    /**
     * Sets load more result.
     *
     * @param list    the list
     * @param hasMore the has more
     */
    public void setLoadMoreResult(List<SongItem> list, boolean hasMore) {
        mRankListAdapter.insertAll(list);
        if (smartRefreshLayout == null) {
            return;
        }
        smartRefreshLayout.finishLoadMore();
        smartRefreshLayout.setEnableLoadMore(hasMore);
    }

    /**
     * Sets song item status.
     *
     * @param songItem the song item
     * @param isChosen the is chosen
     */
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

    /**
     * On tab selected.
     *
     * @param position the position
     */
    public void onTabSelected(int position) {
        mRankListAdapter.resetAll(null);
    }


    /**
     * The interface On screen slide page fragment call back.
     */
    public interface OnScreenSlidePageFragmentCallBack {

        /**
         * On refresh.
         *
         * @param refreshLayout the refresh layout
         */
        void onRefresh(@NonNull RefreshLayout refreshLayout);

        /**
         * On load more.
         *
         * @param refreshLayout the refresh layout
         */
        void onLoadMore(@NonNull RefreshLayout refreshLayout);

        /**
         * On click song item.
         *
         * @param songItem the song item
         */
        void onClickSongItem(SongItem songItem);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
