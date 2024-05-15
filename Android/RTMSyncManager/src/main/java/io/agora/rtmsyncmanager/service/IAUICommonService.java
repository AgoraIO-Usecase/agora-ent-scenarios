package io.agora.rtmsyncmanager.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.agora.rtmsyncmanager.service.arbiter.AUIArbiter;
import io.agora.rtmsyncmanager.model.AUIRoomContext;
import io.agora.rtmsyncmanager.service.callback.AUICallback;

public interface IAUICommonService<Observer> {

    /**
     * 绑定响应事件回调，可绑定多个
     *
     * @param observer 响应事件回调
     */
    void registerRespObserver(@Nullable Observer observer);

    /**
     * 解绑响应事件回调
     *
     * @param observer 响应事件回调
     */
    void unRegisterRespObserver(@Nullable Observer observer);

    default void deInitService(@Nullable AUICallback completion) {}

    default void initService(@Nullable AUICallback completion) {}

    default void cleanUserInfo(@NonNull String userId, @Nullable AUICallback completion) {}

    /**
     * room setup success
     */
    default void serviceDidLoad(){}

    /** 获取当前房间上下文
     *
     * @return
     */
    default @NonNull AUIRoomContext getRoomContext() { return AUIRoomContext.shared(); }

    @NonNull String getChannelName();

    default @NonNull String getLockOwnerId() {
        AUIArbiter arbiter = AUIRoomContext.shared().getArbiter(getChannelName());
        if(arbiter == null){
            return "";
        }
        return arbiter.lockOwnerId();
    }
}
