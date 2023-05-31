package io.agora.scene.ktv.widget.voiceHighight;

import android.os.Build;
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
import io.agora.scene.ktv.widget.MusicSettingBean;
import io.agora.scene.widget.DividerDecoration;

public class VoiceHighlightDialog extends BaseBottomSheetDialogFragment<KtvDialogVoiceHighlightBinding> implements OnItemClickListener<VoiceHighlightBean> {

    private BaseRecyclerViewAdapter<KtvItemHighlightVoiceBinding, VoiceHighlightBean, VoiceHighlightHolder> adapter;
    private final @NonNull OnVoiceHighlightDialogListener mListener;
    private final MusicSettingBean mSettings;
    private RecyclerView mRecyclerView;

    public VoiceHighlightDialog(@NonNull OnVoiceHighlightDialogListener listener, MusicSettingBean settings) {
        this.mListener = listener;
        this.mSettings = settings;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.mRecyclerView);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mRecyclerView.addItemDecoration(new DividerDecoration(4, 24, 8));
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
        mSettings.setHighLighterUid(data.user.getRtcUid());
    }

    public void setUserList(List<VoiceHighlightBean> list) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list.forEach(bean -> {
                if (bean.user.getRtcUid().equals(mSettings.getHighLighterUid())) {
                    bean.setSelect(true);
                }
            });
        }
        adapter = new BaseRecyclerViewAdapter<>(list, this, VoiceHighlightHolder.class);
        mRecyclerView.setAdapter(adapter);

    }
}
