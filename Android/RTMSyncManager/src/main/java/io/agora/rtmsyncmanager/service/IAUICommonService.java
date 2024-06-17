package io.agora.rtmsyncmanager.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.agora.rtmsyncmanager.service.arbiter.AUIArbiter;
import io.agora.rtmsyncmanager.model.AUIRoomContext;
import io.agora.rtmsyncmanager.service.callback.AUICallback;

/**
 * IAUICommonService is an interface that defines the common services for all users.
 * It includes methods for registering and unregistering response observers, initializing and deinitializing services,
 * cleaning user information, getting room context, and getting the channel name and lock owner ID.
 *
 * @param <Observer> The type of the response observer.
 */
public interface IAUICommonService<Observer> {

    /**
     * Register a response observer. Multiple observers can be registered.
     * @param observer The response observer.
     */
    void registerRespObserver(@Nullable Observer observer);

    /**
     * Unregister a response observer.
     * @param observer The response observer.
     */
    void unRegisterRespObserver(@Nullable Observer observer);

    /**
     * Deinitialize the service.
     * @param completion The callback to be invoked when the operation is complete.
     */
    default void deInitService(@Nullable AUICallback completion) { }

    /**
     * Initialize the service.
     * @param completion The callback to be invoked when the operation is complete.
     */
    default void initService(@Nullable AUICallback completion) { }

    /**
     * Clean the information of a specific user.
     * @param userId The ID of the user.
     * @param completion The callback to be invoked when the operation is complete.
     */
    default void cleanUserInfo(@NonNull String userId, @Nullable AUICallback completion) { }

    /**
     * This method is called when the room setup is successful.
     */
    default void serviceDidLoad() { }

    /**
     * Get the current room context.
     * @return The room context.
     */
    default @NonNull AUIRoomContext getRoomContext() {
        return AUIRoomContext.shared();
    }

    /**
     * Get the channel name.
     * @return The channel name.
     */
    @NonNull String getChannelName();

    /**
     * Get the ID of the lock owner.
     * @return The ID of the lock owner.
     */
    default @NonNull String getLockOwnerId() {
        AUIArbiter arbiter = AUIRoomContext.shared().getArbiter(getChannelName());
        if (arbiter == null) {
            return "";
        }
        return arbiter.lockOwnerId();
    }
}