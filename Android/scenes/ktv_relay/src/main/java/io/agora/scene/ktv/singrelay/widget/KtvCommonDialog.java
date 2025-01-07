package io.agora.scene.ktv.singrelay.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.base.utils.KtExtendKt;
import io.agora.scene.ktv.singrelay.databinding.KtvRelayDialogCommonBinding;

public class KtvCommonDialog extends BaseDialog<KtvRelayDialogCommonBinding> {
    public KtvCommonDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected KtvRelayDialogCommonBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return KtvRelayDialogCommonBinding.inflate(inflater);
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

    public void setDescText(String desc) {
        getBinding().tvDesc.setText(desc);
        getBinding().tvDesc.setVisibility(View.VISIBLE);
    }

    public void setDialogBtnText(String leftText, String rightText) {
        getBinding().btnRight.setText(rightText);
        if (TextUtils.isEmpty(leftText)) {
            getBinding().btnLeft.setVisibility(View.GONE);
        } else {
            getBinding().btnLeft.setText(leftText);
        }
    }

    @Override
    protected void setGravity() {
        getWindow().setLayout(
                (int) KtExtendKt.getDp(300),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        getWindow().getAttributes().gravity = Gravity.CENTER;
    }
}
