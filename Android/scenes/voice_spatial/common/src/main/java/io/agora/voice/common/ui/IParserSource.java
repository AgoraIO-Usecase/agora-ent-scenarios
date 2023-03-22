package io.agora.voice.common.ui;

import androidx.annotation.NonNull;

import io.agora.voice.common.net.OnResourceParseCallback;
import io.agora.voice.common.net.Resource;
import io.agora.voice.common.net.Status;
import io.agora.voice.common.utils.LogTools;
import io.agora.voice.common.utils.ThreadManager;

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
                    LogTools.e("parseResource", response.getMessage());
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
