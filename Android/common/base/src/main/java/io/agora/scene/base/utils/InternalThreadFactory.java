package io.agora.scene.base.utils;

import android.os.Process;

import androidx.annotation.RestrictTo;

import java.util.concurrent.ThreadFactory;

/**
 * the factory to use when the executor creates a new thread
 */
@RestrictTo({RestrictTo.Scope.LIBRARY})
public class InternalThreadFactory implements ThreadFactory {
    private final int mThreadPriority;

    public InternalThreadFactory(int threadPriority) {
        mThreadPriority = threadPriority;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Runnable wrapperRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(mThreadPriority);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                runnable.run();
            }
        };
        return new Thread(wrapperRunnable);
    }
}
