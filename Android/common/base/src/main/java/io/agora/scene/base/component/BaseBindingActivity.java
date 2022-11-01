package io.agora.scene.base.component;

import android.view.LayoutInflater;
import android.view.View;

import androidx.viewbinding.ViewBinding;

public abstract class BaseBindingActivity<T extends ViewBinding> extends BaseActivity {
    private T _binding;

    public T getBinding() {
        return _binding;
    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public View getLayoutView() {
        _binding = getViewBinding(getLayoutInflater());
        return _binding.getRoot();
    }


    protected abstract T getViewBinding(LayoutInflater inflater);

}
