package io.agora.scene.ktv.widget.voiceHighlight;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvItemHighlightVoiceBinding;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;

public class VoiceHighlightHolder extends BaseRecyclerViewAdapter.BaseViewHolder<KtvItemHighlightVoiceBinding, VoiceHighlightBean> {
    public VoiceHighlightHolder(@NonNull KtvItemHighlightVoiceBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(@Nullable VoiceHighlightBean data, int selectedIndex) {
        GlideApp.with(mBinding.getRoot())
                .load(data.user.getHeadUrl())
                .error(R.mipmap.userimage)
                .transform(new CenterCropRoundCornerTransform(100))
                .into(mBinding.ivBg);
        mBinding.tvTitle.setText(data.user.getName());
        mBinding.select.setVisibility(data.isSelect() ? View.VISIBLE : View.GONE);
    }
}
