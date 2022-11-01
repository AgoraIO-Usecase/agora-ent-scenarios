package io.agora.scene.base.component;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;

public class BaseViewModel extends ViewModel {
    /**
     * 页面回调
     */
    ISingleCallback<Integer, Object> iSingleCallback = null;

    LifecycleOwner lifecycleOwner = null;

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    public ISingleCallback<Integer, Object> getISingleCallback() {
        return iSingleCallback;
    }

    public void setISingleCallback(ISingleCallback<Integer, Object> iSingleCallback) {
        this.iSingleCallback = iSingleCallback;
    }
}
