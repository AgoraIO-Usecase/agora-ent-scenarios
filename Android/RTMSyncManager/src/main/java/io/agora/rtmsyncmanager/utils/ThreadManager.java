package io.agora.rtmsyncmanager.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class for managing threads in the Agora RTM Sync Manager.
 *
 * This class provides methods for running tasks on the main thread and checking if the current thread is the main thread.
 */
public final class ThreadManager {
    private static volatile ThreadManager instance;

    private Handler mMainThreadHandler;

    private Executor mIOThreadExecutor;

    /**
     * Private constructor for ThreadManager.
     *
     * Initializes the main thread handler.
     */
    private ThreadManager() {
        init();
    }

    /**
     * Retrieves the instance of the ThreadManager.
     *
     * If the instance does not exist, it is created.
     *
     * @return The instance of the ThreadManager.
     */
    public static ThreadManager getInstance() {
        if (instance == null) {
            synchronized (ThreadManager.class) {
                if (instance == null) {
                    instance = new ThreadManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the main thread handler.
     */
    private void init() {
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        int KEEP_ALIVE_TIME = 1;
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingDeque<>();
        mIOThreadExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES,
                NUMBER_OF_CORES * 2,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                taskQueue,
                new InternalThreadFactory(Process.THREAD_PRIORITY_BACKGROUND));

        mMainThreadHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Runs a task on the main thread.
     *
     * If the current thread is the main thread, the task is run immediately.
     * Otherwise, the task is posted to the main thread handler.
     *
     * @param runnable The task to run.
     */
    public void runOnMainThread(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (isMainThread()) {
            runnable.run();
        } else {
            mMainThreadHandler.post(runnable);
        }
    }

    /**
     * Checks if the current thread is the main thread.
     *
     * @return True if the current thread is the main thread, false otherwise.
     */
    public boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }


    /**
     * Switch to an asynchronous thread
     *
     * @param runnable
     */
    public void runOnIOThread(Runnable runnable) {
        mIOThreadExecutor.execute(runnable);
    }
}