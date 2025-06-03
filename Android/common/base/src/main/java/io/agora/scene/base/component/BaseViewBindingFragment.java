package io.agora.scene.base.component;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import androidx.viewbinding.ViewBinding;

import io.agora.scene.base.R;


public abstract class BaseViewBindingFragment<T extends ViewBinding> extends BaseBindingFragment<T> {
    private View loadingView;

    private void addLoadingView() {
        if (this.loadingView == null) {
            this.loadingView = LayoutInflater.from(getActivity()).inflate(R.layout.view_base_loading, (ViewGroup) null, false);
            loadingView.setBackground(null);
            ((ViewGroup) this.getBinding().getRoot()).addView(this.loadingView, new LayoutParams(-1, -1));
        }
        loadingView.post(() -> loadingView.setVisibility(View.VISIBLE));

    }

    public final void showLoadingView() {
        this.addLoadingView();
        loadingView.postDelayed(this::hideLoadingView, 5000);
    }

    public final void hideLoadingView() {
        if (this.loadingView != null) {
            if (getBinding() != null) {
                View rootView = getBinding().getRoot();
                rootView.post(() -> {
                    if (loadingView != null) {
                        loadingView.setVisibility(View.GONE);
                    }
                });
            }
        }
    }
}
