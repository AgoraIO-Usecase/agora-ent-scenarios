package io.agora.scene.widget.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import io.agora.scene.base.component.BaseDialog;
import io.agora.scene.widget.databinding.DialogInputPasswordBinding;

public class InputPasswordDialog extends BaseDialog<DialogInputPasswordBinding> {
    public InputPasswordDialog(@NonNull Context context) {
        super(context);
    }

    /**
     * The On define click listener.
     */
    public OnDefineClickListener onDefineClickListener;


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
            onDefineClickListener.onDefineClicked( getBinding().etDeviceName.getText().toString());
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
     * set title
     */
    public void setDialogTitle(String title) {
        getBinding().tvTitle.setText(title);
    }
    /**
     * clear content
     */
    public void clearContent() {
        getBinding().etDeviceName.setText("");
    }

    /**
     * input hint
     */
    public void setDialogInputHint(String title) {
        getBinding().tvTitle.setText(title);
        getBinding().etDeviceName.setHint(title);
    }

    @Override
    protected void setGravity() {
        getWindow().getAttributes().gravity = Gravity.CENTER;
    }

    /**
     * The interface On define click listener.
     */
    public interface OnDefineClickListener {
        /**
         * On define clicked.
         *
         * @param password the password
         */
        void onDefineClicked(String password);
    }
}
