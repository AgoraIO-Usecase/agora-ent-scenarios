package io.agora.scene.base.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadUtils {
    private static final String TAG = "DownloadUtils";

    private static final String CACHE_FOLDER = "assets";

    private final OkHttpClient okHttpClient =
            new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS).build();

    private static DownloadUtils instance;

    public static DownloadUtils getInstance() {
        if (instance == null) {
            instance = new DownloadUtils();
        }
        return instance;
    }

    private DownloadUtils() {
    }

    @Nullable
    public void download(Context context, String url, FileDownloadSuccessCallback callback, FileDownloadFailureCallback error) {
        File folder = new File(context.getExternalCacheDir(), CACHE_FOLDER);
        if (!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(folder, url.substring(url.lastIndexOf("/") + 1));
        if (file.exists()) {
            callback.onSuccess(file);
            return;
        }
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                error.onFailed(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                if (body == null) {
                    error.onFailed((Exception) new Throwable("body is empty"));
                    return;
                }

                long total = body.contentLength();

                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = body.byteStream();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        Log.d(TAG, file.getName() + ", progress: " + progress);
                    }
                    fos.flush();
                    Log.d(TAG, file.getName() + " onComplete");
                    callback.onSuccess(file);
                } catch (Exception e) {
                    error.onFailed(e);
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    public interface FileDownloadSuccessCallback {
        void onSuccess(File file);
    }

    public interface FileDownloadFailureCallback {
        void onFailed(Exception exception);
    }
}
