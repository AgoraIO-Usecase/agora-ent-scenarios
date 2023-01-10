package io.agora.scene.voice.viewmodel.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import io.agora.voice.common.utils.ThreadManager;

public class BaseRepository {

    /**
     * return a new liveData
     * @param item
     * @param <T>
     * @return
     */
    public <T> LiveData<T> createLiveData(T item) {
        return new MutableLiveData<>(item);
    }

    /**
     * Run on UI thread
     * @param runnable
     */
    public void runOnMainThread(Runnable runnable) {
        ThreadManager.getInstance().runOnMainThread(runnable);
    }

    /**
     * In asynchronous thread
     * @param runnable
     */
    public void runOnIOThread(Runnable runnable) {
        ThreadManager.getInstance().runOnIOThread(runnable);
    }

}
