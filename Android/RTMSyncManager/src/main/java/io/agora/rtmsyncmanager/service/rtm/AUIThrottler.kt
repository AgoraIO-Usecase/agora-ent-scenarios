package io.agora.rtmsyncmanager.service.rtm

import android.os.Handler
import android.os.Looper

/**
 * A class that provides a mechanism to delay the execution of a task and ensure that only the last task is executed.
 * It is useful in scenarios where a task is triggered frequently, but it's not necessary to execute all the tasks.
 * Instead, only the last task is needed to be executed after a certain delay.
 */
class AUIThrottler() {
    // A Handler associated with the main thread's Looper
    private val handler = Handler(Looper.getMainLooper())
    // A Runnable that represents the task to be executed
    private var runnable: Runnable? = null

    /**
     * Triggers the execution of the last task after a specified delay.
     * If a task is already scheduled, it will be cancelled and replaced by the new task.
     * @param delay The delay (in milliseconds) after which the task should be executed.
     * @param execute The task to be executed.
     */
    fun triggerLastEvent(delay: Long, execute: () -> Unit) {
        // If a task is already scheduled, cancel it
        runnable?.let {
            handler.removeCallbacks(it)
            runnable = null
        }
        // Create a new task
        runnable = Runnable {
            execute.invoke()
            runnable = null
        }
        // Schedule the task for execution after a delay
        runnable?.let {
            handler.postDelayed(it, delay)
        }
    }

    /**
     * Immediately triggers the execution of the task.
     * If a task is already scheduled, it will be cancelled and replaced by the new task.
     * The task is executed on the main thread.
     */
    fun triggerNow() {
        runnable?.let {
            // Cancel any scheduled tasks
            handler.removeCallbacksAndMessages(null)
            runnable = null
            // If the current thread is the main thread, execute the task immediately
            if (Thread.currentThread() == handler.looper.thread) {
                it.run()
            } else {
                // Otherwise, post the task to the main thread
                handler.post(it)
            }
        }
    }

    /**
     * Cancels all scheduled tasks.
     */
    fun clean() {
        handler.removeCallbacksAndMessages(null)
    }
}