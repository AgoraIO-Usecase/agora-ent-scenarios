package io.agora.voice.baseui.interfaces;

import android.util.Log;

import androidx.annotation.NonNull;

import io.agora.voice.baseui.general.callback.OnResourceParseCallback;
import io.agora.voice.baseui.general.enums.Status;
import io.agora.voice.baseui.general.net.Resource;
import io.agora.voice.buddy.tool.LogTools;
import io.agora.voice.buddy.tool.ThreadManager;

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
                    LogTools.logE(response.getMessage(), "parseResource ");
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
