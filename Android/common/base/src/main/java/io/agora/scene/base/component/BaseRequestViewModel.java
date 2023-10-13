package io.agora.scene.base.component;


import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class BaseRequestViewModel extends BaseViewModel {
    private CompositeDisposable mCompositeDisposable;


    public BaseRequestViewModel() {
        if (isNeedEventBus()) {
            EventBus.getDefault().register(this);
        }
    }

    public void addDispose(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }
        if (isNeedEventBus()) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void clearDispose(){
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }
    }

    protected boolean isNeedEventBus() {
        return false;
    }
}
