package io.agora.scene.widget.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.base.utils.KTVUtil;
import io.agora.scene.widget.databinding.DialogNoNetBinding;

public class NoNetDialog extends BaseDialog<DialogNoNetBinding> {
    public NoNetDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected DialogNoNetBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return DialogNoNetBinding.inflate(inflater);
    }

    @Override
    protected void initView() {
        getBinding().btnHasKnow.setOnClickListener(view -> {
            dismiss();
        });
    }


    @Override
    protected void setGravity() {
        getWindow().setLayout(
                KTVUtil.dp2px(295),
                KTVUtil.dp2px(276)
        );
        getWindow().getAttributes().gravity = Gravity.CENTER;
    }
}
