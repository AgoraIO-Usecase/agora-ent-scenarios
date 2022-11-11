package io.agora.scene.ktv.live.fragment.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.FragmentDialogMvBinding;
import io.agora.scene.ktv.databinding.KtvItemMvBinding;
import io.agora.scene.ktv.live.RoomLivingActivity;
import io.agora.scene.ktv.live.holder.MVHolder;
import io.agora.scene.ktv.manager.RoomManager;
import io.agora.scene.widget.DividerDecoration;

public class MVFragment extends BaseViewBindingFragment<FragmentDialogMvBinding> implements OnItemClickListener<Integer> {
    private BaseRecyclerViewAdapter<KtvItemMvBinding, Integer, MVHolder> mAdapter;
    private int index;
    public static final List<Integer> exampleBackgrounds = new ArrayList<>(
            Arrays.asList(
                    R.mipmap.mvbg4,
                    R.mipmap.mvbg8,
                    R.mipmap.mvbg2,
                    R.mipmap.mvbg7,
                    R.mipmap.mvbg3,
                    R.mipmap.mvbg9,
                    R.mipmap.mvbg5,
                    R.mipmap.mvbg6,
                    R.mipmap.mvbg1

            ));

    public MVFragment(int index) {
        super();
        this.index = index;
    }

    @NonNull
    @Override
    protected FragmentDialogMvBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        return FragmentDialogMvBinding.inflate(layoutInflater);
    }

    @Override
    public void initView() {
        mAdapter = new BaseRecyclerViewAdapter<>(exampleBackgrounds, this, MVHolder.class);
        mAdapter.selectedIndex = index;
        getBinding().rvList.setAdapter(mAdapter);
        getBinding().rvList.addItemDecoration(new DividerDecoration(3));
    }

    @Override
    public void initListener() {
        getBinding().ivBackIcon.setOnClickListener(view -> {
            ((RoomLivingActivity) requireActivity()).closeMenuDialog();
        });
    }

    @Override
    public void onItemClick(@NonNull Integer data, View view, int position, long viewType) {
        mAdapter.selectedIndex = position;
        RoomManager.mRoom.bgOption = String.valueOf(position);
        index = position;
        mAdapter.notifyDataSetChanged();
        ((RoomLivingActivity) requireActivity()).setPlayerBg(position);
    }
}
