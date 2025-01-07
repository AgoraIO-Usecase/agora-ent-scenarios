package io.agora.scene.ktv.singbattle.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.base.utils.KtExtendKt;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleDialogUserSeatMenuBinding;

/**
 * Room user menu             
 */
public class UserLeaveSeatMenuDialog extends BaseDialog<KtvSingbattleDialogUserSeatMenuBinding> {
    public UserLeaveSeatMenuDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected KtvSingbattleDialogUserSeatMenuBinding getViewBinding(@NonNull LayoutInflater layoutInflater) {
        return KtvSingbattleDialogUserSeatMenuBinding.inflate(layoutInflater);
    }

    public void setAgoraMember(String name, String headUrl) {
        getBinding().tvName.setText(name);
        Glide.with(getContext())
                .load(headUrl).error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                .into(getBinding().ivUser);
    }

    @Override
    public void initView() {
        setCanceledOnTouchOutside(true);
        getWindow().setWindowAnimations(io.agora.scene.widget.R.style.popup_window_style_bottom);
        getBinding().btSeatoff.setOnClickListener(this::seatOff);
        getBinding().btLeaveChorus.setOnClickListener(this::leaveChorus);
    }

    private void seatOff(View v) {
        if (getOnButtonClickListener() != null) {
            dismiss();
            getOnButtonClickListener().onRightButtonClick();
        }
    }

    private void leaveChorus(View v) {
        if (getOnButtonClickListener() != null) {
            dismiss();
            getOnButtonClickListener().onLeftButtonClick();
        }
    }

    @Override
    protected void setGravity() {
        getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) KtExtendKt.getDp(220)
        );
        getWindow().getAttributes().gravity = Gravity.BOTTOM;
    }
}
