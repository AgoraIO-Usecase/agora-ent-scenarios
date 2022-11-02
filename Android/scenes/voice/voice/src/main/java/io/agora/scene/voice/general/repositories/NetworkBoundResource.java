package io.agora.scene.voice.general.repositories;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import io.agora.voice.baseui.general.callback.ResultCallBack;
import io.agora.voice.baseui.general.net.ErrorCode;
import io.agora.voice.baseui.general.net.Resource;
import io.agora.voice.baseui.general.net.Result;
import io.agora.voice.buddy.tool.ThreadManager;


/**
 * As a server to pull data and local data fusion class
 * @param <ResultType> Pulled data from the local database
 * @param <RequestType> Data pulled from the server
 */
public abstract class NetworkBoundResource<ResultType, RequestType> {
    private static final String TAG = "NetworkBoundResource";
    private ThreadManager mThreadManager;
    private final MediatorLiveData<Resource<ResultType>> result = new MediatorLiveData<>();
    private LiveData<ResultType> lastFailSource;

    public NetworkBoundResource() {
        mThreadManager = ThreadManager.getInstance();
        if(mThreadManager.isMainThread()) {
            init();
        }else {
            mThreadManager.runOnMainThread(this::init);
        }
    }

    /**
     * work on main thread
     */
    private void init() {
        result.setValue(Resource.loading(null));
        LiveData<ResultType> dbSource = safeLoadFromDb();
        result.addSource(dbSource, data -> {
            result.removeSource(dbSource);
            if(shouldFetch(data)) {
                fetchFromNetwork(dbSource);
            }else {
                result.addSource(dbSource, newData -> setValue(Resource.success(newData)));
            }
        });
    }

    /**
     * work on main thread
     * @param dbSource
     */
    private void fetchFromNetwork(LiveData<ResultType> dbSource) {
        // First display the data in the database, and after processing the network request data,
        // take it out of the database and display it again
        result.addSource(dbSource, newData-> setValue(Resource.loading(newData)));
        createCall(new ResultCallBack<LiveData<RequestType>>() {
            @Override
            public void onSuccess(LiveData<RequestType> apiResponse) {
                // Ensure that it is in the main thread after the callback
                mThreadManager.runOnMainThread(() -> {
                    result.addSource(apiResponse, response-> {
                        result.removeSource(apiResponse);
                        result.removeSource(dbSource);
                        if(response != null) {
                            // If the result is an Result structure, you need to determine the code, whether the request is successful
                            if(response instanceof Result) {
                                int code = ((Result) response).code;
                                if(code != ErrorCode.EM_NO_ERROR) {
                                    fetchFailed(code, dbSource, null);
                                }
                            }
                            // Handle the logic of saving to the database in an asynchronous thread
                            mThreadManager.runOnIOThread(() -> {
                                try {
                                    saveCallResult(processResponse(response));
                                } catch (Exception e) {
                                    Log.e(TAG, "save call result failed: " + e.toString());
                                }
                                //In order to obtain the latest data, it is necessary to retrieve the data from the database
                                // again to ensure the consistency of the page and the data
                                mThreadManager.runOnMainThread(() ->
                                        result.addSource(safeLoadFromDb(), newData -> {
                                            setValue(Resource.success(newData));
                                        }));
                            });

                        }else {
                            fetchFailed(ErrorCode.ERR_UNKNOWN, dbSource, null);
                        }
                    });
                });

            }

            @Override
            public void onError(int error, String errorMsg) {
                mThreadManager.runOnMainThread(() -> {
                    fetchFailed(error, dbSource, errorMsg);
                });
            }
        });


    }

    /**
     * Safely load data from the database. If the load fails, the data returns null.
     * @return
     */
    private LiveData<ResultType> safeLoadFromDb() {
        LiveData<ResultType> dbSource;
        try {
            dbSource = loadFromDb();
        } catch (Exception e) {
            Log.e(TAG, "safe load from db failed: " + e.toString());
            dbSource = new MutableLiveData<>(null);
        }
        return dbSource;
    }

    @MainThread
    private void fetchFailed(int code, LiveData<ResultType> dbSource, String message) {
        onFetchFailed();
        try {
            result.addSource(dbSource, newData -> setValue(Resource.error(code, message, newData)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MainThread
    private void setValue(Resource<ResultType> newValue) {
        if(result.getValue() != newValue) {
            result.setValue(newValue);
        }
    }

    /**
     * Process request response
     * @param response
     * @return
     */
    @WorkerThread
    protected RequestType processResponse(RequestType response) {
        return response;
    }

    /**
     * Called with the data in the database to decide whether to fetch
     * potentially updated data from the network.
     * @param data
     * @return
     */
    @MainThread
    protected abstract boolean shouldFetch(ResultType data);

    /**
     * Called to get the cached data from the database.
     * @return
     */
    @MainThread
    protected abstract LiveData<ResultType> loadFromDb();

    /**
     * This is designed as a callback mode to facilitate asynchronous operations in this method
     * @return
     */
    @MainThread
    protected abstract void createCall(ResultCallBack<LiveData<RequestType>> callBack);

    /**
     * Called to save the result of the API response into the database
     * @param item
     */
    @WorkerThread
    protected abstract void saveCallResult(RequestType item);

    /**
     * Called when the fetch fails. The child class may want to reset components like rate limiter.
     */
    protected void onFetchFailed() {}

    /**
     * Returns a LiveData object that represents the resource that's implemented
     * in the base class.
     * @return
     */
    protected LiveData<Resource<ResultType>> asLiveData() {
        return result;
    }
}
