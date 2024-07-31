package io.agora.scene.voice.spatial.global;

import android.util.Log;

import androidx.annotation.NonNull;

import io.agora.scene.voice.spatial.net.OnResourceParseCallback;
import io.agora.scene.voice.spatial.net.Resource;
import io.agora.scene.voice.spatial.net.Status;
import io.agora.scene.voice.spatial.utils.ThreadManager;

/**
 * @author create by zhangwei03
 */
public interface IParserSource {
    /**
     * Parse Resource<T>
     *
     * @param response
     * @param callback
     * @param <T>
     */
    default <T> void parseResource(Resource<T> response, @NonNull OnResourceParseCallback<T> callback) {
        if (response == null) {
            return;
        }
        if (response.status == Status.SUCCESS) {
            ThreadManager.getInstance().runOnMainThread(() -> {
                callback.onHideLoading();
                callback.onSuccess(response.data);
            });
        } else if (response.status == Status.ERROR) {
            ThreadManager.getInstance().runOnMainThread(() -> {
                callback.onHideLoading();
                if (!callback.hideErrorMsg) {
                    Log.e("parseResource", response.getMessage());
                }
                callback.onError(response.errorCode, response.getMessage());
            });
        } else if (response.status == Status.LOADING) {
            ThreadManager.getInstance().runOnMainThread(() -> {
                callback.onLoading(response.data);
            });
        }
    }
}
