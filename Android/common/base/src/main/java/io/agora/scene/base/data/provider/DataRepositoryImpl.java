package io.agora.scene.base.data.provider;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.agora.scene.base.data.model.KTVBaseResponse;
import io.agora.scene.base.data.model.MusicModelBase;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DataRepositoryImpl implements IDataRepository {


    @Override
    public Observable<KTVBaseResponse<MusicModelBase>> getMusic(@NonNull String musicId) {
        Map<String, Object> dicParameters = new HashMap<>();
        dicParameters.put("id", musicId);
        return null;
    }

    private OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    public Completable download(@NonNull File file, @NonNull String url) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter emitter) throws Exception {
                Log.d("down", file.getName() + ", url: " + url);

                if (file.isDirectory()) {
                    emitter.onError(new Throwable("file is a Directory"));
                    return;
                }

                Request request = new Request.Builder().url(url).build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        emitter.onError(e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        ResponseBody body = response.body();
                        if (body == null) {
                            emitter.onError(new Throwable("body is empty"));
                            return;
                        }

                        long total = body.contentLength();

                        if (file.exists() && file.length() == total) {
                            emitter.onComplete();
                            return;
                        }

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
                                Log.d("down", file.getName() + ", progress: " + progress);
                            }
                            fos.flush();
                            // 下载完成
                            Log.d("down", file.getName() + " onComplete");
                            emitter.onComplete();
                        } catch (Exception e) {
                            emitter.onError(e);
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
        });
    }
}
