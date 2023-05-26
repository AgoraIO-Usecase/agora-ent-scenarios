package io.agora.scene.ktv.widget.voiceHighight;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvItemHighlightVoiceBinding;
import io.agora.scene.ktv.service.RoomSeatModel;

public class VoiceHighlightHolder extends BaseRecyclerViewAdapter.BaseViewHolder<KtvItemHighlightVoiceBinding, VoiceHighlightBean> {
    public VoiceHighlightHolder(@NonNull KtvItemHighlightVoiceBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(@Nullable VoiceHighlightBean data, int selectedIndex) {
        mBinding.ivBg.setImageResource(R.mipmap.userimage);
        mBinding.tvTitle.setText(data.user.getName());
        mBinding.select.setVisibility(data.isSelect() ? View.VISIBLE : View.GONE);
    }
}
