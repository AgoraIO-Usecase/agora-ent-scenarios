package io.agora.rtmsyncmanager.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObservableHelper<EventHandler> {

    private final List<EventHandler> eventHandlerList = Collections.synchronizedList(new ArrayList<>());
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void subscribeEvent(@Nullable EventHandler eventHandler) {
        if (eventHandler == null || eventHandlerList.contains(eventHandler)) {
            return;
        }
        eventHandlerList.add(eventHandler);
    }

    public void unSubscribeEvent(@Nullable EventHandler eventHandler) {
        if (eventHandler == null) {
            return;
        }
        eventHandlerList.remove(eventHandler);
    }

    public void unSubscribeAll() {
        eventHandlerList.clear();
        mainHandler.removeCallbacksAndMessages(null);
    }

    public void notifyEventHandlers(@NonNull EventHandlerRunnable<EventHandler> runnable) {
        for (EventHandler eventHandler : eventHandlerList) {
            if (mainHandler.getLooper().getThread() != Thread.currentThread()) {
                mainHandler.post(() -> runnable.run(eventHandler));
            } else {
                runnable.run(eventHandler);
            }
        }
    }

    public interface EventHandlerRunnable<EventHandler> {
        void run(EventHandler eventHandler);
    }

}
