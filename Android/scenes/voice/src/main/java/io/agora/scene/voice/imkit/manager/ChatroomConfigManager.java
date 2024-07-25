package io.agora.scene.voice.imkit.manager;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatOptions;
import io.agora.scene.voice.imkit.bean.ChatMessageData;
import io.agora.scene.voice.imkit.custorm.CustomMsgHelper;
import io.agora.scene.voice.imkit.custorm.OnCustomMsgReceiveListener;
import io.agora.scene.voice.model.VoiceMemberModel;
import io.agora.scene.voice.service.VoiceRoomServiceKickedReason;
import io.agora.scene.voice.service.VoiceChatServiceListenerProtocol;
import io.agora.scene.voice.service.VoiceServiceProtocol;
import io.agora.util.EMLog;
import io.agora.voice.common.utils.LogTools;

public class ChatroomConfigManager {
    private static final String TAG = ChatroomConfigManager.class.getSimpleName();
    private static ChatroomConfigManager mInstance;
    private Context mContext;
    private VoiceServiceProtocol voiceServiceProtocol;

    ChatroomConfigManager() {
    }

    public static ChatroomConfigManager getInstance() {
        if (mInstance == null) {
            synchronized (ChatroomConfigManager.class) {
                if (mInstance == null) {
                    mInstance = new ChatroomConfigManager();
                }
            }
        }
        return mInstance;
    }

    public void initRoomConfig(Context context, String imKey) {
        this.mContext = context;
        ChatOptions options = initChatOptions(imKey);
        if (!isMainProcess(context)) {
            LogTools.e(TAG, "enter the service process!");
            return;
        }
        ChatClient.getInstance().init(context, options);
        registerListener();
        voiceServiceProtocol = VoiceServiceProtocol.getImplInstance();
    }

    private void registerListener() {
        ChatroomIMManager.getInstance().setChatRoomConnectionListener(new OnChatroomConnectionListener() {
            @Override
            public void onConnected() {
                EMLog.i(TAG, "onConnected");
            }

            @Override
            public void onDisconnected(int error) {
                EMLog.i(TAG, "onDisconnected =" + error);
            }

            @Override
            public void onTokenWillExpire() {

            }

            @Override
            public void onTokenExpired() {

            }
        });
        ChatroomIMManager.getInstance().setOnCustomMsgReceiveListener(new OnCustomMsgReceiveListener() {
            @Override
            public void onReceiveGiftMsg(ChatMessageData message) {
                voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                    listener.onReceiveGift(message.getConversationId(), message);
                });
            }

            @Override
            public void onReceivePraiseMsg(ChatMessageData message) {

            }

            @Override
            public void onReceiveNormalMsg(ChatMessageData message) {
                voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                    listener.onReceiveTextMsg(message.getConversationId(), message);
                });
            }

            @Override
            public void onReceiveApplySite(ChatMessageData message) {
                LogTools.d(TAG, "onReceiveSeatRequest");
                voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                    listener.onReceiveSeatRequest(message);
                });
            }

            @Override
            public void onReceiveCancelApplySite(ChatMessageData message) {
                LogTools.d(TAG, "onReceiveSeatRequestRejected");
                voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                    listener.onReceiveSeatRequestRejected(message.getFrom());
                });
            }

            @Override
            public void onReceiveInviteSite(ChatMessageData message) {
                LogTools.d(TAG, "onReceiveSeatInvitation");
                voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                    listener.onReceiveSeatInvitation(message);
                });
            }

            @Override
            public void onReceiveInviteRefusedSite(ChatMessageData message) {
                LogTools.d(TAG, "onReceiveSeatInvitationRejected");
                voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                    listener.onReceiveSeatInvitationRejected(message.getConversationId(), message);
                });
            }

            @Override
            public void onReceiveDeclineApply(ChatMessageData message) {

            }

            @Override
            public void onReceiveSystem(ChatMessageData message) {
                VoiceMemberModel voiceMemberModel = ChatroomIMManager.getInstance().getVoiceMemberModel(message);
                if (voiceMemberModel != null) {
                    voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                        listener.onUserJoinedRoom(message.getConversationId(), voiceMemberModel);
                    });
                }
            }
        });

        ChatroomIMManager.getInstance().setChatRoomEventListener(new OnChatroomEventReceiveListener() {
            @Override
            public void onRoomDestroyed(String roomId) {
                voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                    listener.onRoomDestroyed(roomId);
                });
            }

            @Override
            public void onMemberJoined(String roomId, String name) {

            }

            @Override
            public void onMemberExited(String roomId, String reason, String name) {
                voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                    listener.onUserLeftRoom(roomId, name);
                });
            }

            @Override
            public void onKicked(String roomId, int reason) {
                VoiceRoomServiceKickedReason kickedReason = ChatroomIMManager.getInstance().getKickReason(reason);
                voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                    listener.onUserBeKicked(roomId, kickedReason);
                });
            }

            @Override
            public void onAnnouncementChanged(String roomId, String announcement) {
                voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                    listener.onAnnouncementChanged(roomId, announcement);
                });
            }

            @Override
            public void onAttributesUpdate(String roomId, Map<String, String> attributeMap, String from) {
                voiceServiceProtocol.getSubscribeEvents().notifyEventHandlers(listener -> {
                    listener.onSeatUpdated(roomId, attributeMap, from);
                });
            }

            @Override
            public void onAttributesRemoved(String roomId, List<String> keyList, String from) {
            }
        });
    }

    public Context getContext() {
        return mContext;
    }

    private ChatOptions initChatOptions(String imKey) {
        ChatOptions options = new ChatOptions();
        options.setAppKey(imKey);
        options.setAutoLogin(false);
        return options;
    }

    private boolean isMainProcess(Context context) {
        String processName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            processName = getProcessNameByApplication();
        } else {
            processName = getProcessNameByReflection();
        }
        return context.getApplicationInfo().packageName.equals(processName);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private String getProcessNameByApplication() {
        return Application.getProcessName();
    }

    private String getProcessNameByReflection() {
        String processName = null;
        try {
            final Method declaredMethod = Class.forName("android.app.ActivityThread", false, Application.class.getClassLoader())
                    .getDeclaredMethod("currentProcessName", (Class<?>[]) new Class[0]);
            declaredMethod.setAccessible(true);
            final Object invoke = declaredMethod.invoke(null, new Object[0]);
            if (invoke instanceof String) {
                processName = (String) invoke;
            }
        } catch (Throwable e) {
        }
        return processName;
    }

    public void removeChatRoomListener(VoiceChatServiceListenerProtocol listener) {
        if (listener != null) {
            ChatroomIMManager.getInstance().removeChatRoomChangeListener();
            ChatroomIMManager.getInstance().removeChatRoomConnectionListener();
            CustomMsgHelper.getInstance().removeListener();
        }
    }

}
