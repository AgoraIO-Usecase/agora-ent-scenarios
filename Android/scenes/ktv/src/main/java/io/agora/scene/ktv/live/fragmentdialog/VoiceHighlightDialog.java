package io.agora.scene.ktv.live.fragmentdialog;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.component.BaseBottomSheetDialogFragment;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvDialogVoiceHighlightBinding;
import io.agora.scene.ktv.live.bean.MusicSettingBean;
import io.agora.scene.ktv.live.bean.VoiceHighlightBean;
import io.agora.scene.widget.DividerDecoration;

public class VoiceHighlightDialog extends BaseBottomSheetDialogFragment<KtvDialogVoiceHighlightBinding> implements OnItemClickListener<VoiceHighlightBean> {

    public static final String TAG = "VoiceHighlightDialog";
    private BaseRecyclerViewAdapter<io.agora.scene.ktv.databinding.KtvItemHighlightVoiceBinding, VoiceHighlightBean, VoiceHighlightHolder> adapter;
    private final @NonNull OnVoiceHighlightDialogListener mListener;
    private final MusicSettingBean mSettings;
    private RecyclerView mRecyclerView;
    private Boolean hasHigher = false;

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
        if (hasHigher) {
            ToastUtils.showToast(R.string.ktv_highlight_limit);
            return;
        }
        hasHigher = true;
        for (int i = 0; i < adapter.dataList.size(); i++) {
            adapter.dataList.get(i).setSelect(i == position);
            adapter.notifyItemChanged(i);
        }
        mListener.onUserItemChosen(data);
        mSettings.setMHighLighterUid(data.user.getRtcUid());
    }

    public void setUserList(List<VoiceHighlightBean> list) {
        list.forEach(bean -> {
            if (bean.user.getRtcUid().equals(mSettings.getMHighLighterUid())) {
                bean.setSelect(true);
            }
        });
        adapter = new BaseRecyclerViewAdapter<>(list, this, VoiceHighlightHolder.class);
        mRecyclerView.setAdapter(adapter);
    }

    public void reset() {
        this.hasHigher = false;
    }

    public class VoiceHighlightHolder extends BaseRecyclerViewAdapter.BaseViewHolder<io.agora.scene.ktv.databinding.KtvItemHighlightVoiceBinding, VoiceHighlightBean> {
        public VoiceHighlightHolder(@NonNull io.agora.scene.ktv.databinding.KtvItemHighlightVoiceBinding mBinding) {
            super(mBinding);
        }

        @Override
        public void binding(@Nullable VoiceHighlightBean data, int selectedIndex) {
            GlideApp.with(mBinding.getRoot())
                    .load(data.user.getHeadUrl())
                    .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mBinding.ivBg);
            mBinding.tvTitle.setText(data.user.getName());
            mBinding.select.setVisibility(data.isSelect() ? View.VISIBLE : View.GONE);
        }
    }

    public interface OnVoiceHighlightDialogListener {
        void onUserListLoad();
        void onUserItemChosen(VoiceHighlightBean user);
    }
}
