package io.agora.scene.ktv.singbattle.widget.rankList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        mAdapter.resetAll(list);
    }
}
