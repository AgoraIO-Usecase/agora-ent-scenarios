package io.agora.scene.base.component;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import io.agora.scene.base.R;


public abstract class BaseDialog<T extends ViewBinding> extends Dialog {
    private T _binding;

    public BaseDialog(@NonNull Context context) {
        super(context, R.style.dialog_complete);
        initConfig();
    }

    public T getBinding() {
        return _binding;
    }

    protected abstract T getViewBinding(LayoutInflater inflater);

    protected abstract void initView();

    protected OnButtonClickListener onButtonClickListener;

    public OnButtonClickListener getOnButtonClickListener() {
        return onButtonClickListener;
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    private void initConfig() {
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        _binding = getViewBinding(getLayoutInflater());
        setContentView(_binding.getRoot());
        setGravity();
        initView();
    }

    protected void setGravity() {
        getWindow().getAttributes().gravity = Gravity.CENTER;
    }

    @Override
    public void dismiss() {
        if (isShowing()) {
            super.dismiss();
        }
    }

}
