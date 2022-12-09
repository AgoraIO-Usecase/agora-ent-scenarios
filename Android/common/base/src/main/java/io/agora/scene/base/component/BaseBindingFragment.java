package io.agora.scene.base.component;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

public abstract class BaseBindingFragment<T extends ViewBinding> extends BaseFragment {
    private View rootView;
    private T _binding;

    public T getBinding() {
        return _binding;
    }

    protected abstract T getViewBinding(LayoutInflater inflater, ViewGroup container);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (rootView == null) {
            _binding = getViewBinding(inflater, container);
            rootView = _binding.getRoot();
        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
        rootView = null;
    }

}
