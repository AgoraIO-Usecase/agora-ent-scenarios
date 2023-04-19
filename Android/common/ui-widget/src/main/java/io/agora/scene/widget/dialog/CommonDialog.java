package io.agora.scene.widget.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.base.utils.UiUtil;
import io.agora.scene.widget.databinding.DialogCommonBinding;

public class CommonDialog extends BaseDialog<DialogCommonBinding> {
    public CommonDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected DialogCommonBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return DialogCommonBinding.inflate(inflater);
    }

    @Override
    protected void initView() {
        getBinding().btnLeft.setOnClickListener(view -> {
            getOnButtonClickListener().onLeftButtonClick();
            dismiss();
        });
        getBinding().btnRight.setOnClickListener(view -> {
            getOnButtonClickListener().onRightButtonClick();
            dismiss();
        });
    }

    public void setDialogTitle(String title) {
        getBinding().tvTitle.setText(title);
    }

    public void setDescText(String desc) {
        getBinding().tvDesc.setText(desc);
        getBinding().tvDesc.setVisibility(View.VISIBLE);
//        getWindow().setLayout(
//                UiUtil.dp2px(300),
//                UiUtil.dp2px(220)
//        );
    }

    public void setDialogBtnText(String leftText, String rightText) {
        getBinding().btnLeft.setText(leftText);
        if (TextUtils.isEmpty(rightText)) {
            getBinding().btnRight.setVisibility(View.GONE);
        } else {
            getBinding().btnRight.setText(rightText);
        }
    }

    @Override
    protected void setGravity() {
        getWindow().setLayout(
                UiUtil.dp2px(300),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        getWindow().getAttributes().gravity = Gravity.CENTER;
    }
}
