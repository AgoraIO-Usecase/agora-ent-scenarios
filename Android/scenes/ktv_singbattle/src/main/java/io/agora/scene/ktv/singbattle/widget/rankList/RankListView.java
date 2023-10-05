package io.agora.scene.ktv.singbattle.widget.rankList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.agora.scene.ktv.singbattle.databinding.KtvLayoutGameRankListViewBinding;

public class RankListView extends FrameLayout {
    protected KtvLayoutGameRankListViewBinding mBinding;
    private final RankListAdapter mAdapter = new RankListAdapter();
    public RankListView(@NonNull Context context) {
        this(context, null);
    }

    public RankListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RankListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mBinding = KtvLayoutGameRankListViewBinding.inflate(LayoutInflater.from(context), this, true);
        mBinding.rvRankList.setAdapter(mAdapter);
    }

    public void resetRankList(List<RankItem> list) {
        List<RankItem> newList = new ArrayList<>();
        newList.addAll(list);
        if (list.size() < 3) {
            for (int i = 0; i < 3 - list.size(); i++) {
                RankItem emptyItem = new RankItem();
                emptyItem.score = -1;
                emptyItem.songNum = -1;
                emptyItem.userName = "暂无上榜";
                emptyItem.poster = "";
                newList.add(emptyItem);
            }
        }
        mAdapter.resetAll(newList);
    }
}
