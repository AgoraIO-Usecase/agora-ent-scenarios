package io.agora.scene.ktv.widget.voiceHighight;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.agora.scene.base.component.BaseBottomSheetDialogFragment;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvDialogVoiceHighlightBinding;
import io.agora.scene.ktv.databinding.KtvItemHighlightVoiceBinding;
import io.agora.scene.ktv.live.holder.EffectVoiceHolder;
import io.agora.scene.ktv.service.RoomSeatModel;
import io.agora.scene.widget.DividerDecoration;

public class VoiceHighlightDialog extends BaseBottomSheetDialogFragment<KtvDialogVoiceHighlightBinding> implements OnItemClickListener<VoiceHighlightBean> {

    private BaseRecyclerViewAdapter<KtvItemHighlightVoiceBinding, VoiceHighlightBean, VoiceHighlightHolder> adapter;
    private @NonNull OnVoiceHighlightDialogListener mListener;
    private RecyclerView mRecyclerView;

    public VoiceHighlightDialog(OnVoiceHighlightDialogListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.mRecyclerView);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerView.addItemDecoration(new DividerDecoration(3));
        mRecyclerView.setItemAnimator(null);

        mListener.onUserListLoad();
    }

    @Override
    public void onItemClick(@NonNull VoiceHighlightBean data, View view, int position, long viewType) {
        OnItemClickListener.super.onItemClick(data, view, position, viewType);
        Log.d("hugo", "onItemClick    " + position);

        for (int i = 0; i < adapter.dataList.size(); i++) {
            adapter.dataList.get(i).setSelect(i == position);
            adapter.notifyItemChanged(i);
        }
        mListener.onUserItemChosen(data);
    }

    public void setUserList(List<VoiceHighlightBean> list) {
        adapter = new BaseRecyclerViewAdapter<>(list, this, VoiceHighlightHolder.class);
        mRecyclerView.setAdapter(adapter);
    }
}
