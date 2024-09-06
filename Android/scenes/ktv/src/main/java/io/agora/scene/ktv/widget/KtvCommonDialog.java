package io.agora.scene.ktv.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.base.utils.UiUtil;
import io.agora.scene.ktv.databinding.KtvDialogCommonBinding;

/**
 * 默认弹框
 */
public class KtvCommonDialog extends BaseDialog<KtvDialogCommonBinding> {
    public KtvCommonDialog(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected KtvDialogCommonBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return KtvDialogCommonBinding.inflate(inflater);
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

    /**
     * Sets dialog btn text.
     *
     * @param leftText  the left text
     * @param rightText the right text
     */
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
//        getWindow().setLayout(
//                UiUtil.dp2px(300),
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        );
        getWindow().getAttributes().gravity = Gravity.CENTER;
    }
}
