package io.agora.scene.base.utils;

import android.os.SystemClock;
import android.util.Log;

import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.agora.scene.base.ServerConfig;

public class TimeUtils {
    private static final Executor workerExecutor = Executors.newSingleThreadExecutor();
    private static volatile boolean hasSync = false;
    private static volatile long diff = 0;
    private static final String TAG = "TimeUtils";

    public static long currentTimeMillis() {
        if (!hasSync) {
            CountDownLatch latch = new CountDownLatch(1);
            workerExecutor.execute(() -> {
                try {
                    URL url = new URL(ServerConfig.getToolBoxUrl());
                    URLConnection uc = url.openConnection();
                    long startTime = SystemClock.elapsedRealtime();
                    uc.connect();// connect
                    long ld = uc.getDate();
                    diff = ld + (SystemClock.elapsedRealtime() - startTime) - System.currentTimeMillis();
                    hasSync = true;
                    Log.d(TAG, "diff success, diff=" + diff);
                } catch (Exception e) {
                    Log.e(TAG, "get data failed", e);
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Log.e(TAG, "wait too long", e);
            }
        }
        return System.currentTimeMillis() + diff;
    }

}
