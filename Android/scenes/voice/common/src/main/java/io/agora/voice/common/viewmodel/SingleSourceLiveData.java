package io.agora.voice.common.viewmodel;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used when setting up and listening to a single data source: LiveData
 * It is convenient to automatically cancel the listening of the previous data source when you need to switch data sources
 *
 * @param <T> Type of data source to monitor
 */
public class SingleSourceLiveData<T> extends MutableLiveData<T> {
    private final AtomicBoolean mPending = new AtomicBoolean(false);
    private LiveData<T> lastSource;
    private T lastData;
    private final Observer<T> observer = new Observer<T>() {
        @Override
        public void onChanged(T t) {
            if (t != null && t == lastData) {
                return;
            }

            lastData = t;
            setValue(t);
        }
    };

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull final Observer<? super T> observer) {
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                if (mPending.compareAndSet(true, false)) {
                    observer.onChanged(t);
                }
            }
        });
    }

    @MainThread
    public void setValue(@Nullable T t) {
        mPending.set(true);
        super.setValue(t);
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    public void call() {
        setValue(null);
    }

    /**
     * Set the data source, and unlisten the data source when it has been set
     *
     * @param source
     */
    public void setSource(LiveData<T> source) {
        if (lastSource == source) {
            return;
        }

        if (lastSource != null) {
            lastSource.removeObserver(observer);
        }
        lastSource = source;

        if (hasActiveObservers()) {
            lastSource.observeForever(observer);
        }
    }

    @Override
    protected void onActive() {
        super.onActive();

        if (lastSource != null) {
            lastSource.observeForever(observer);
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();

        if (lastSource != null) {
            lastSource.removeObserver(observer);
        }
    }
}
