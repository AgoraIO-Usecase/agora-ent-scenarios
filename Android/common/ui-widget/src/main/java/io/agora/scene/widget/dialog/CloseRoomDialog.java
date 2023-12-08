package io.agora.scene.widget.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.base.utils.UiUtil;
import io.agora.scene.widget.databinding.DialogCloseRoomBinding;

public class CloseRoomDialog extends BaseDialog<DialogCloseRoomBinding> {
    public CloseRoomDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected DialogCloseRoomBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return DialogCloseRoomBinding.inflate(inflater);
    }

    @Override
    protected void initView() {
        getBinding().btnRight.setOnClickListener(view -> {
            getOnButtonClickListener().onRightButtonClick();
            dismiss();
        });
    }

    @Override
    protected void setGravity() {
//        getWindow().setLayout(
//                UiUtil.dp2px(300),
//                UiUtil.dp2px(120)
//        );
        getWindow().getAttributes().gravity = Gravity.CENTER;
    }
}
