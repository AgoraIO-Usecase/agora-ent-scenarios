package io.agora.scene.base.utils;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadManager {
    private static Handler a;
    private static Object b = new Object();
    public static final Executor NETWORK_EXECUTOR = a();
    private static Handler c;
    private static HandlerThread d;
    private static Handler e;
    private static HandlerThread f;

    public ThreadManager() {
    }

    private static Executor a() {
        Object var0 = null;
        if (Build.VERSION.SDK_INT >= 11) {
            var0 = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue());
        } else {
            Object var1 = null;

            try {
                Field var2 = AsyncTask.class.getDeclaredField("sExecutor");
                var2.setAccessible(true);
                var1 = (Executor) var2.get((Object) null);
            } catch (Exception var3) {
                var1 = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue());
            }

            var0 = var1;
        }

        if (var0 instanceof ThreadPoolExecutor) {
            ((ThreadPoolExecutor) var0).setCorePoolSize(3);
        }

        return (Executor) var0;
    }

    public static void init() {
    }

    public static void executeOnNetWorkThread(Runnable var0) {
        try {
            NETWORK_EXECUTOR.execute(var0);
        } catch (RejectedExecutionException var2) {

        }

    }

    public static Handler getMainHandler() {
        if (a == null) {
            Object var0 = b;
            synchronized (b) {
                if (a == null) {
                    a = new Handler(Looper.getMainLooper());
                }
            }
        }

        return a;
    }

    public static Handler getFileThreadHandler() {
        if (e == null) {
            Class var0 = ThreadManager.class;
            synchronized (ThreadManager.class) {
                f = new HandlerThread("SDK_FILE_RW");
                f.start();
                e = new Handler(f.getLooper());
            }
        }

        return e;
    }

    public static Looper getFileThreadLooper() {
        return getFileThreadHandler().getLooper();
    }

    public static Thread getSubThread() {
        if (d == null) {
            getSubThreadHandler();
        }

        return d;
    }

    public static Handler getSubThreadHandler() {
        if (c == null) {
            Class var0 = ThreadManager.class;
            synchronized (ThreadManager.class) {
                d = new HandlerThread("SDK_SUB");
                d.start();
                c = new Handler(d.getLooper());
            }
        }

        return c;
    }

    public static Looper getSubThreadLooper() {
        return getSubThreadHandler().getLooper();
    }

    public static void executeOnSubThread(Runnable var0) {
        getSubThreadHandler().post(var0);
    }

    public static void executeOnFileThread(Runnable var0) {
        getFileThreadHandler().post(var0);
    }

    public static Executor newSerialExecutor() {
        return new SerialExecutor();
    }

    private static class SerialExecutor implements Executor {
        final Queue<Runnable> a;
        Runnable b;

        private SerialExecutor() {
            this.a = new LinkedList();
        }

        @Override
        public synchronized void execute(final Runnable var1) {
            this.a.offer(new Runnable() {
                public void run() {
                    try {
                        var1.run();
                    } finally {
                        SerialExecutor.this.a();
                    }

                }
            });
            if (this.b == null) {
                this.a();
            }

        }

        protected synchronized void a() {
            if ((this.b = (Runnable) this.a.poll()) != null) {
                ThreadManager.NETWORK_EXECUTOR.execute(this.b);
            }

        }
    }
}
