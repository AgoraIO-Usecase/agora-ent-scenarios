package io.agora.rtmsyncmanager.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that helps manage event handlers in an observable pattern.
 * <p>
 * This class provides methods for subscribing and unsubscribing event handlers,
 * as well as notifying all subscribed event handlers of an event.
 *
 * @param <EventHandler> The type of the event handlers.
 */
public class ObservableHelper<EventHandler> {

    /**
     * A thread-safe list of event handlers.
     */
    private final List<EventHandler> eventHandlerList = Collections.synchronizedList(new ArrayList<>());

    /**
     * A handler for the main thread.
     */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Subscribes an event handler to this observable.
     * <p>
     * If the event handler is null or already subscribed, this method does nothing.
     *
     * @param eventHandler The event handler to subscribe.
     */
    public void subscribeEvent(@Nullable EventHandler eventHandler) {
        if (eventHandler == null || eventHandlerList.contains(eventHandler)) {
            return;
        }
        eventHandlerList.add(eventHandler);
    }

    /**
     * Unsubscribes an event handler from this observable.
     * <p>
     * If the event handler is null, this method does nothing.
     *
     * @param eventHandler The event handler to unsubscribe.
     */
    public void unSubscribeEvent(@Nullable EventHandler eventHandler) {
        if (eventHandler == null) {
            return;
        }
        eventHandlerList.remove(eventHandler);
    }

    /**
     * Unsubscribes all event handlers from this observable and removes all callbacks and messages from the main handler.
     */
    public void unSubscribeAll() {
        eventHandlerList.clear();
        mainHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Notifies all subscribed event handlers of an event.
     * <p>
     * This method runs the provided runnable for each subscribed event handler.
     * If the current thread is not the main thread, the runnable is posted to the main handler.
     * Otherwise, the runnable is run immediately.
     *
     * @param runnable The runnable to run for each event handler.
     */
    public void notifyEventHandlers(@NonNull EventHandlerRunnable<EventHandler> runnable) {
        for (EventHandler eventHandler : eventHandlerList) {
            if (mainHandler.getLooper().getThread() != Thread.currentThread()) {
                mainHandler.post(() -> runnable.run(eventHandler));
            } else {
                runnable.run(eventHandler);
            }
        }
    }

    /**
     * Interface for a runnable that takes an event handler as a parameter.
     * <p>
     * This interface is used by the notifyEventHandlers method to run a task for each subscribed event handler.
     *
     * @param <EventHandler> The type of the event handler.
     */
    public interface EventHandlerRunnable<EventHandler> {
        /**
         * Runs this runnable with the provided event handler.
         *
         * @param eventHandler The event handler to run this runnable with.
         */
        void run(EventHandler eventHandler);
    }

}
