package io.agora.scene.ktv.live.fragmentdialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.base.utils.UiUtil;
import io.agora.scene.ktv.R;

/**
 * 房间用户菜单
 */
public class UserLeaveSeatMenuDialog extends BaseDialog<io.agora.scene.ktv.databinding.KtvDialogUserSeatMenuBinding> {
    public UserLeaveSeatMenuDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected io.agora.scene.ktv.databinding.KtvDialogUserSeatMenuBinding getViewBinding(@NonNull LayoutInflater layoutInflater) {
        return io.agora.scene.ktv.databinding.KtvDialogUserSeatMenuBinding.inflate(layoutInflater);
    }

    public void setAgoraMember(String name, String headUrl) {
        getBinding().tvName.setText(name);
        Glide.with(getContext())
                .load(headUrl).error(R.mipmap.default_user_avatar)
                .into(getBinding().ivUser);
    }

    @Override
    public void initView() {
        setCanceledOnTouchOutside(true);
        getWindow().setWindowAnimations(R.style.popup_window_style_bottom);
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
                UiUtil.dp2px(220)
        );
        getWindow().getAttributes().gravity = Gravity.BOTTOM;
    }
}
