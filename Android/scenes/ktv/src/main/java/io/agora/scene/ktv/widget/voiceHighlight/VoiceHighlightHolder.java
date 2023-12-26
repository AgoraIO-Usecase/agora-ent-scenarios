package io.agora.scene.ktv.widget.voiceHighlight;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.RequestOptions;

import io.agora.scene.base.GlideApp;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvItemHighlightVoiceBinding;

public class VoiceHighlightHolder extends BaseRecyclerViewAdapter.BaseViewHolder<KtvItemHighlightVoiceBinding, VoiceHighlightBean> {
    public VoiceHighlightHolder(@NonNull KtvItemHighlightVoiceBinding mBinding) {
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
