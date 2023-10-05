package io.agora.scene.widget.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.base.component.ISingleCallback;
import io.agora.scene.base.utils.UiUtil;
import io.agora.scene.widget.databinding.DialogInputPasswordBinding;

public class InputPasswordDialog extends BaseDialog<DialogInputPasswordBinding> {
    public InputPasswordDialog(@NonNull Context context) {
        super(context);
    }

    public ISingleCallback<Integer, Object> iSingleCallback;

    @NonNull
    @Override
    protected DialogInputPasswordBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return DialogInputPasswordBinding.inflate(inflater);
    }

    @Override
    protected void initView() {
        getBinding().btnCancel.setOnClickListener(view -> {
            dismiss();
        });
        getBinding().btnDefine.setOnClickListener(view -> {
            iSingleCallback.onSingleCallback(0, getBinding().etDeviceName.getText().toString());
            dismiss();
        });
        getBinding().etDeviceName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null && editable.length() > 0) {
                    getBinding().iBtnClear.setVisibility(View.VISIBLE);
                } else {
                    getBinding().iBtnClear.setVisibility(View.GONE);
                }
            }
        });
        getBinding().iBtnClear.setOnClickListener(view -> {
            clearContent();
        });
    }

    /**
     * 设置title
     */
    public void setDialogTitle(String title) {
        getBinding().tvTitle.setText(title);
    }
    /**
     * 清空密码输入框
     */
    public void clearContent() {
        getBinding().etDeviceName.setText("");
    }

    /**
     * 输入提示
     */
    public void setDialogInputHint(String title) {
        getBinding().tvTitle.setText(title);
        getBinding().etDeviceName.setHint(title);
    }

    @Override
    protected void setGravity() {
//        getWindow().setLayout(
//                UiUtil.dp2px(300),
//                UiUtil.dp2px(230)
//        );
        getWindow().getAttributes().gravity = Gravity.CENTER;
    }
}
