package io.agora.scene.voice.imkit.manager;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.ChatRoomChangeListener;
import io.agora.ConnectionListener;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatRoom;
import io.agora.chat.ChatRoomManager;
import io.agora.chat.Conversation;
import io.agora.chat.CustomMessageBody;
import io.agora.chat.TextMessageBody;
import io.agora.chat.adapter.EMAChatRoomManagerListener;
import io.agora.scene.base.utils.GsonTools;
import io.agora.scene.base.utils.ThreadManager;
import io.agora.scene.voice.VoiceLogger;
import io.agora.scene.voice.global.VoiceCenter;
import io.agora.scene.voice.imkit.bean.ChatMessageData;
import io.agora.scene.voice.imkit.custorm.CustomMsgHelper;
import io.agora.scene.voice.imkit.custorm.CustomMsgType;
import io.agora.scene.voice.imkit.custorm.MsgConstant;
import io.agora.scene.voice.imkit.custorm.OnCustomMsgReceiveListener;
import io.agora.scene.voice.imkit.custorm.OnMsgCallBack;
import io.agora.scene.voice.model.VoiceGiftModel;
import io.agora.scene.voice.model.VoiceMemberModel;
import io.agora.scene.voice.model.VoiceMicInfoModel;
import io.agora.scene.voice.model.VoiceRankUserModel;
import io.agora.scene.voice.model.VoiceRoomInfo;
import io.agora.scene.voice.model.VoiceRoomModel;
import io.agora.scene.voice.service.VoiceRoomServiceKickedReason;
import io.agora.util.EMLog;

public class ChatroomIMManager implements ChatRoomChangeListener, ConnectionListener {
    private static ChatroomIMManager instance;

    private ChatroomIMManager() {
    }

    private String chatroomId;
    private boolean isOwner;
    private ArrayList<ChatMessageData> data = new ArrayList<>();
    public OnChatroomEventReceiveListener chatroomEventListener;
    public OnChatroomConnectionListener chatroomConnectionListener;
    private ChatroomProtocolDelegate delegate;
    private ChatroomCacheManager cacheManager = ChatroomCacheManager.Companion.getCacheManager();
    private static final String TAG = "ChatroomIMManager";

    public static ChatroomIMManager getInstance() {
        if (instance == null) {
            synchronized (ChatroomIMManager.class) {
                if (instance == null) {
                    instance = new ChatroomIMManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize in detail page to prevent chatroomId from being empty or incorrect
     *
     * @param chatroomId
     */
    public void init(String chatroomId, boolean isOwner) {
        this.chatroomId = chatroomId;
        this.isOwner = isOwner;
        // Set message listener
        setCustomMsgListener();
        // Set chatroom status change listener
        setChatRoomChangeListener();
        // Set connection listener
        setConnectionListener();
        // Set related room information
        CustomMsgHelper.getInstance().setChatRoomInfo(chatroomId);
        // Set voice chat room protocol delegate
        delegate = new ChatroomProtocolDelegate(chatroomId);
        clearCache();
    }

    public boolean isOwner() {
        return this.isOwner;
    }

    public String getCurrentRoomId() {
        return this.chatroomId;
    }

    private ChatRoomManager getChatRoomManager() {
        return ChatClient.getInstance().chatroomManager();
    }

    /**
     * Set custom message listener
     */
    public void setCustomMsgListener() {
        CustomMsgHelper.getInstance().init();
    }

    public void setChatRoomChangeListener() {
        ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(this);
    }

    public void removeChatRoomChangeListener() {
        ChatClient.getInstance().chatroomManager().removeChatRoomListener(this);
    }

    public void setConnectionListener() {
        ChatClient.getInstance().addConnectionListener(this);
    }

    public void removeChatRoomConnectionListener() {
        ChatClient.getInstance().removeConnectionListener(this);
    }

    public void setChatRoomEventListener(OnChatroomEventReceiveListener listener) {
        this.chatroomEventListener = listener;
    }

    public void setChatRoomConnectionListener(OnChatroomConnectionListener listener) {
        this.chatroomConnectionListener = listener;
    }

    /**
     * Remove custom message listener
     */
    public void removeCustomMsgListener() {
        CustomMsgHelper.getInstance().removeListener();
    }

    /**
     * Send text message
     *
     * @param content
     * @param callBack
     */
    public void sendTxtMsg(String content, String nickName, OnMsgCallBack callBack) {
        ChatMessage message = ChatMessage.createTextSendMessage(content, chatroomId);
        message.setAttribute("userName", nickName);
        message.setChatType(ChatMessage.ChatType.ChatRoom);
        message.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {
                CustomMsgHelper.getInstance().addSendText(parseChatMessage(message));
                callBack.onSuccess(parseChatMessage(message));
            }

            @Override
            public void onError(int i, String s) {
                callBack.onError(i, s);
            }

            @Override
            public void onProgress(int i, String s) {
                callBack.onProgress(i, s);
            }
        });
        ChatClient.getInstance().chatManager().sendMessage(message);
    }

    /**
     * Insert welcome message
     *
     * @param content
     * @param nick
     */
    public void saveWelcomeMsg(String content, String nick) {
        ChatMessage message = ChatMessage.createSendMessage(ChatMessage.Type.TXT);
        message.setChatType(ChatMessage.ChatType.ChatRoom);
        message.setTo(chatroomId);
        TextMessageBody textMessageBody = new TextMessageBody(content);
        message.setBody(textMessageBody);
        message.setAttribute("userName", nick);
        ChatClient.getInstance().chatManager().saveMessage(message);
    }

    public ArrayList<ChatMessageData> getMessageData(String chatroomId) {
        data.clear();
        Conversation conversation = ChatClient.getInstance().chatManager().getConversation(chatroomId, Conversation.ConversationType.ChatRoom, true);
        if (conversation != null) {
            EMLog.e("getMessageData", conversation.getAllMessages().size() + "");
            for (ChatMessage allMessage : conversation.getAllMessages()) {
                if (allMessage.getBody() instanceof TextMessageBody
                        || allMessage.getBody() instanceof CustomMessageBody &&
                        (((CustomMessageBody) allMessage.getBody()).event()).equals(CustomMsgType.CHATROOM_SYSTEM.getName())) {
                    data.add(parseChatMessage(allMessage));
                }
            }
        }
        return data;
    }

    public ChatMessageData parseChatMessage(ChatMessage chatMessage) {
        ChatMessageData chatMessageData = new ChatMessageData();
        chatMessageData.setForm(chatMessage.getFrom());
        chatMessageData.setTo(chatMessage.getTo());
        chatMessageData.setConversationId(chatMessage.conversationId());
        chatMessageData.setMessageId(chatMessage.getMsgId());
        if (chatMessage.getBody() instanceof TextMessageBody) {
            chatMessageData.setType("text");
            chatMessageData.setContent(((TextMessageBody) chatMessage.getBody()).getMessage());
        } else if (chatMessage.getBody() instanceof CustomMessageBody) {
            chatMessageData.setType("custom");
            chatMessageData.setEvent(((CustomMessageBody) chatMessage.getBody()).event());
            chatMessageData.setCustomParams(((CustomMessageBody) chatMessage.getBody()).getParams());
        }
        chatMessageData.setExt(chatMessage.ext());
        return chatMessageData;
    }

    public void setOnCustomMsgReceiveListener(OnCustomMsgReceiveListener listener) {
        CustomMsgHelper.getInstance().setOnCustomMsgReceiveListener(listener);
    }

    public String getUserName(ChatMessageData msg) {
        String userName = "";
        Map<String, String> params = msg.getCustomParams();
        if (params.containsKey("userName")) {
            userName = params.get("userName");
        }
        if (TextUtils.isEmpty(userName)) {
            Map<String, Object> ext = msg.getExt();
            if (ext.containsKey("userName")) {
                userName = (String) ext.get("userName");
            }
        }
        VoiceLogger.d("Helper", "getUserName: " + userName);
        return userName;
    }

    public String getSystemUserName(ChatMessageData msg) {
        String jsonString = "";
        String userName = "";
        Map<String, String> params = msg.getCustomParams();
        if (params.containsKey("user")) {
            jsonString = params.get("user");
            VoiceLogger.d("getSystemUserName", "jsonString: " + jsonString);
            if (!TextUtils.isEmpty(jsonString)) {
                try {
                    assert jsonString != null;
                    JSONObject jsonObject = new JSONObject(jsonString);
                    userName = jsonObject.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        VoiceLogger.d("getSystemUserName", "userName: " + userName);
        return userName;
    }

    public String getUserPortrait(ChatMessageData msg) {
        String userPortrait = "";
        Map<String, String> ext = msg.getCustomParams();
        if (ext.containsKey("portrait")) {
            userPortrait = ext.get("portrait");
        }
        return userPortrait;
    }

    /**
     * Parse Gift message to get giftModel
     *
     * @param msg
     * @return
     */
    public VoiceGiftModel getGiftModel(ChatMessageData msg) {
        Map<String, String> giftMap = CustomMsgHelper.getInstance().getCustomMsgParams(msg);
        if (giftMap != null) {
            VoiceGiftModel voiceGiftModel = new VoiceGiftModel();
            voiceGiftModel.setGift_id(giftMap.get(MsgConstant.CUSTOM_GIFT_KEY_ID));
            voiceGiftModel.setGift_count(giftMap.get(MsgConstant.CUSTOM_GIFT_KEY_NUM));
            voiceGiftModel.setGift_name(giftMap.get(MsgConstant.CUSTOM_GIFT_NAME));
            voiceGiftModel.setGift_price(giftMap.get(MsgConstant.CUSTOM_GIFT_PRICE));
            voiceGiftModel.setUserName(giftMap.get(MsgConstant.CUSTOM_GIFT_USERNAME));
            voiceGiftModel.setPortrait(giftMap.get(MsgConstant.CUSTOM_GIFT_PORTRAIT));
            return voiceGiftModel;
        }
        return null;
    }

    /**
     * Parse message to get VoiceMemberModel object
     *
     * @param msg
     * @return
     */
    public VoiceMemberModel getVoiceMemberModel(ChatMessageData msg) {
        Map<String, String> map = CustomMsgHelper.getInstance().getCustomMsgParams(msg);
        if (map.containsKey("user")) {
            return GsonTools.toBean(map.get("user"), VoiceMemberModel.class);
        }
        return null;
    }

    /**
     * Get reason for member's non-voluntary room exit
     *
     * @param reason
     * @return
     */
    public VoiceRoomServiceKickedReason getKickReason(int reason) {
        switch (reason) {
            case EMAChatRoomManagerListener.BE_KICKED:
                return VoiceRoomServiceKickedReason.removed;
            case EMAChatRoomManagerListener.DESTROYED:
                return VoiceRoomServiceKickedReason.destroyed;
            case EMAChatRoomManagerListener.BE_KICKED_FOR_OFFLINE:
                return VoiceRoomServiceKickedReason.offLined;
            default:
                return null;
        }
    }


    @Override
    public void onChatRoomDestroyed(String roomId, String s1) {
        if (chatroomEventListener != null && TextUtils.equals(roomId, chatroomId))
            chatroomEventListener.onRoomDestroyed(roomId);
    }

    @Override
    public void onMemberJoined(String roomId, String s1) {
        if (chatroomEventListener != null && TextUtils.equals(roomId, chatroomId))
            chatroomEventListener.onMemberJoined(roomId, s1);
    }

    @Override
    public void onMemberExited(String roomId, String s1, String s2) {
        if (chatroomEventListener != null && TextUtils.equals(roomId, chatroomId))
            chatroomEventListener.onMemberExited(roomId, s1, s2);
    }

    @Override
    public void onRemovedFromChatRoom(int i, String roomId, String s1, String s2) {
        if (chatroomEventListener != null && TextUtils.equals(roomId, chatroomId))
            chatroomEventListener.onKicked(roomId, i);
    }

    @Override
    public void onMuteListAdded(String s, List<String> list, long l) {

    }

    @Override
    public void onMuteListRemoved(String s, List<String> list) {

    }

    @Override
    public void onWhiteListAdded(String s, List<String> list) {

    }

    @Override
    public void onWhiteListRemoved(String s, List<String> list) {

    }

    @Override
    public void onAllMemberMuteStateChanged(String s, boolean b) {

    }

    @Override
    public void onAdminAdded(String s, String s1) {

    }

    @Override
    public void onAdminRemoved(String s, String s1) {

    }

    @Override
    public void onOwnerChanged(String s, String s1, String s2) {

    }

    @Override
    public void onAnnouncementChanged(String roomId, String announcement) {
        if (chatroomEventListener != null && TextUtils.equals(roomId, chatroomId))
            chatroomEventListener.onAnnouncementChanged(roomId, announcement);
    }

    @Override
    public void onAttributesUpdate(String roomId, Map<String, String> attributeMap, String from) {
        if (chatroomEventListener != null && TextUtils.equals(roomId, chatroomId))
            chatroomEventListener.onAttributesUpdate(roomId, attributeMap, from);
    }

    @Override
    public void onAttributesRemoved(String roomId, List<String> keyList, String from) {
        if (chatroomEventListener != null && TextUtils.equals(roomId, chatroomId))
            chatroomEventListener.onAttributesRemoved(roomId, keyList, from);
    }

    //////////////////////Connection///////////////////////////

    @Override
    public void onConnected() {
        if (chatroomConnectionListener != null)
            chatroomConnectionListener.onConnected();
    }

    @Override
    public void onDisconnected(int code) {
        if (chatroomConnectionListener != null)
            chatroomConnectionListener.onDisconnected(code);
    }

    @Override
    public void onTokenExpired() {
        if (chatroomConnectionListener != null)
            chatroomConnectionListener.onTokenExpired();
    }

    @Override
    public void onTokenWillExpire() {
        if (chatroomConnectionListener != null)
            chatroomConnectionListener.onTokenWillExpire();
    }

    public void renewToken(String newToken) {
        ChatClient.getInstance().renewToken(newToken);
    }

    public boolean isLoggedIn() {
        return ChatClient.getInstance().isLoggedIn();
    }

    public void login(String uid, String token, CallBack callBack) {
        ChatClient.getInstance().loginWithToken(uid, token, new CallBack() {
            @Override
            public void onSuccess() {
                ThreadManager.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onSuccess();
                    }
                });
                VoiceLogger.d("ChatroomConfigManager", "Login success");
            }

            @Override
            public void onError(int code, String msg) {
                ThreadManager.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onError(code, msg);
                    }
                });
                VoiceLogger.e("ChatroomConfigManager", "Login onError code:" + code + " desc: " + msg);
            }
        });
    }

    public void login(String uid, String token) {
        ChatClient.getInstance().loginWithAgoraToken(uid, token, new CallBack() {
            @Override
            public void onSuccess() {
                VoiceLogger.d("ChatroomConfigManager", "Login success");
            }

            @Override
            public void onError(int code, String msg) {
                VoiceLogger.e("ChatroomConfigManager", "Login onError code:" + code + " desc: " + msg);
            }
        });
    }

    public void logout(boolean unbind, CallBack callBack) {
        ChatClient.getInstance().logout(unbind, new CallBack() {
            @Override
            public void onSuccess() {
                callBack.onSuccess();
            }

            @Override
            public void onError(int i, String s) {
                callBack.onError(i, s);
            }
        });
    }

    public void logout(boolean unbind) {
        ChatClient.getInstance().logout(unbind, new CallBack() {
            @Override
            public void onSuccess() {
                EMLog.d(TAG, "logout onSuccess");
            }

            @Override
            public void onError(int code, String desc) {
                EMLog.e(TAG, "logout onError code: " + code + "  " + desc);
            }
        });
    }

    /**
     * Join room
     *
     * @param chatroomId
     * @param callBack
     */
    public void joinRoom(String chatroomId, ValueCallBack<ChatRoom> callBack) {
        ChatClient.getInstance().chatroomManager()
                .joinChatRoom(chatroomId, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
                        ThreadManager.getInstance().runOnMainThreadDelay(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onSuccess(value);
                            }
                        }, 200);
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        ThreadManager.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onError(error, errorMsg);
                            }
                        });
                    }
                });
    }

    /**
     * Leave room
     */
    public void leaveChatRoom(String chatroomId) {
        ChatClient.getInstance().chatroomManager().leaveChatRoom(chatroomId);
    }

    /**
     * Destroy room
     */
    public void asyncDestroyChatRoom(String chatroomId, CallBack callBack) {
        ChatClient.getInstance().chatroomManager().asyncDestroyChatRoom(chatroomId, new CallBack() {
            @Override
            public void onSuccess() {
                ThreadManager.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onSuccess();
                    }
                });
            }

            @Override
            public void onError(int code, String error) {
                ThreadManager.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onError(code, error);
                    }
                });
            }
        });
    }

    /**
     * Get current user entity
     */
    public VoiceMemberModel getMySelfModel() {
        return delegate.getMySelfModel();
    }

    public void clearCache() {
        delegate.clearCache();
    }

    /**
     * Initialize mic information
     *
     * @param callBack
     */
    public void initMicInfo(CallBack callBack) {
        VoiceMemberModel voiceMemberModel = new VoiceMemberModel(
                VoiceCenter.getUserId(),
                VoiceCenter.getChatUid(),
                VoiceCenter.getNickname(),
                VoiceCenter.getHeadUrl(),
                VoiceCenter.getRtcUid(),
                0, 1);
        VoiceLogger.d(TAG, "initMicInfo:" + voiceMemberModel);
        delegate.initMicInfo(voiceMemberModel, callBack);
    }

    /**
     * Get details
     * @param voiceRoomModel
     * @param callBack
     */
    public void fetchRoomDetail(VoiceRoomModel voiceRoomModel, ValueCallBack<VoiceRoomInfo> callBack) {
        VoiceMemberModel owner = voiceRoomModel.getOwner();
        if (owner != null && TextUtils.equals(owner.getUserId(), VoiceCenter.getUserId())) {
            initMicInfo(new CallBack() {
                @Override
                public void onSuccess() {
                    delegate.fetchRoomDetail(voiceRoomModel, callBack);
                }

                @Override
                public void onError(int code, String error) {
                    callBack.onError(code, error);
                }
            });
        } else {
            delegate.fetchRoomDetail(voiceRoomModel, callBack);
        }
    }

    /**
     * Invite to mic
     *
     * @param chatUid
     * @param callBack
     */
    public void invitationMic(String chatUid, int micIndex, CallBack callBack) {
        delegate.invitationMic(chatUid, micIndex, callBack);
    }


    /**
     * Mute specific mic position
     *
     * @param micIndex
     * @param callBack
     */
    public void forbidMic(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack) {
        delegate.forbidMic(micIndex, callBack);
    }

    /**
     * Unmute specific mic position
     *
     * @param micIndex
     * @param callBack
     */
    public void unForbidMic(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack) {
        delegate.unForbidMic(micIndex, callBack);
    }

    /**
     * Lock mic
     *
     * @param micIndex
     * @param callBack
     */
    public void lockMic(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack) {
        delegate.lockMic(micIndex, callBack);
    }

    /**
     * Unlock mic
     *
     * @param micIndex
     * @param callBack
     */
    public void unLockMic(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack) {
        delegate.unLockMic(micIndex, callBack);
    }

    /**
     * Kick user off mic
     *
     * @param micIndex
     * @param callBack
     */
    public void kickOff(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack) {
        delegate.kickOff(micIndex, callBack);
    }

    /**
     * Leave mic
     *
     * @param micIndex
     * @param callBack
     */
    public void leaveMic(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack) {
        delegate.leaveMic(micIndex, callBack);
    }

    /**
     * Mute local
     *
     * @param micIndex
     * @param callBack
     */
    public void muteLocal(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack) {
        delegate.muteLocal(micIndex, callBack);
    }

    /**
     * Unmute local
     *
     * @param micIndex
     * @param callBack
     */
    public void unMuteLocal(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack) {
        delegate.unMuteLocal(micIndex, callBack);
    }

    /**
     * Change mic position
     *
     * @param oldIndex
     * @param newIndex
     * @param callBack
     */
    public void changeMic(int oldIndex, int newIndex, ValueCallBack<Map<Integer, VoiceMicInfoModel>> callBack) {
        delegate.changeMic(oldIndex, newIndex, callBack);
    }


    /**
     * Accept invitation
     *
     * @param callBack
     */
    public void acceptMicSeatInvitation(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack) {
        delegate.acceptMicSeatInvitation(VoiceCenter.getChatUid(), micIndex, callBack);
    }

    /**
     * Decline invitation
     *
     * @param chatUid
     * @param callBack
     */
    public void refuseInvite(String chatUid, CallBack callBack) {
        delegate.refuseInviteToMic(chatUid, callBack);
    }

    /**
     * Apply for mic
     *
     * @param micIndex
     * @param callBack
     */
    public void startMicSeatApply(int micIndex, CallBack callBack) {
        delegate.startMicSeatApply(micIndex, callBack);
    }

    /**
     * Accept application
     *
     * @param callBack
     */
    public void acceptMicSeatApply(int micIndex, String chatUid, ValueCallBack<VoiceMicInfoModel> callBack) {
        delegate.acceptMicSeatApply(chatUid, micIndex, callBack);
    }

    /**
     * Cancel mic application
     *
     * @param chatroomId IM room ID for canceling application
     * @param chatUid
     * @param callBack
     */
    public void cancelMicSeatApply(String chatroomId, String chatUid, CallBack callBack) {
        delegate.cancelSubmitMic(chatroomId, chatUid, callBack);
    }


    /**
     * Update announcement
     *
     * @param content
     */
    public void updateAnnouncement(String content, CallBack callBack) {
        delegate.updateAnnouncement(content, callBack);
    }

    /**
     * Raised hand list
     *
     * @return
     */
    public List<VoiceMemberModel> fetchRaisedList() {
        return delegate.fetchRaisedList();
    }

    /**
     * Enable/Disable robot
     *
     * @param enable   true to enable robot, false to disable robot
     * @param callBack
     */
    public void enableRobot(Boolean enable, ValueCallBack<Boolean> callBack) {
        delegate.enableRobot(enable, callBack);
    }

    /**
     * Update robot volume
     *
     * @param volume
     * @param callBack
     */
    public void updateRobotVolume(int volume, CallBack callBack) {
        delegate.updateRobotVolume(volume, callBack);
    }

    /**
     * Get invitation list (filtering members already on mic)
     */
    public List<VoiceMemberModel> fetchRoomInviteMembers() {
        return delegate.fetchRoomInviteMembers();
    }

    /**
     * Get all audience list in room
     */
    public List<VoiceMemberModel> fetchRoomMembers() {
        return delegate.fetchRoomMembers();
    }

    /**
     * Update user list
     */
    public void updateRoomMembers(CallBack callBack) {
        delegate.updateRoomMember(cacheManager.getMemberList(), callBack);
    }

    /**
     * Get ranking list
     */
    public void fetchGiftContribute(ValueCallBack<List<VoiceRankUserModel>> callBack) {
        delegate.fetchGiftContribute(callBack);
    }

    /**
     * Update ranking list
     *
     * @param giftModel
     * @param callBack
     */
    public void updateRankList(String chatUid, VoiceGiftModel giftModel, CallBack callBack) {
        delegate.updateRankList(chatUid, giftModel, callBack);
    }

    /**
     * Update local kv cache
     *
     * @param kvMap
     */
    public void updateMicInfoCache(Map<String, String> kvMap) {
        delegate.updateMicInfoCache(kvMap);
    }

    /**
     * Update total gift amount
     */
    public void updateAmount(String chatUid, int amount, CallBack callBack) {
        delegate.updateGiftAmount(chatUid, amount, callBack);
    }

    /**
     * Update total clicks
     */
    public void increaseClickCount(String chatUid, CallBack callBack) {
        delegate.increaseClickCount(chatUid, callBack);
    }

    ////////////////////////////Local Cache Management//////////////////////////////

    /**
     * Set gift amount directly from server data
     */
    public void setGiftAmountCache(int amount) {
        cacheManager.setGiftAmountCache(amount);
    }

    /**
     * Get total gift amount in room
     */
    public int getGiftAmountCache() {
        return cacheManager.getGiftAmountCache();
    }

    /**
     * Get click count from server data
     * @param count
     */
    public void setClickCountCache(int count) {
        cacheManager.setClickCountCache(count);
    }

    /**
     * Get viewer count
     */
    public int getClickCountCache() {
        return cacheManager.getClickCountCache();
    }

    /**
     * Set mic application list
     */
    public void setSubmitMicList(VoiceMemberModel voiceMemberModel) {
        cacheManager.setSubmitMicList(voiceMemberModel);
    }

    /**
     * Remove specific member from application list
     */
    public void removeSubmitMember(String chatUid) {
        cacheManager.removeSubmitMember(chatUid);
    }

    /**
     * Set member list
     */
    public void setMemberList(VoiceMemberModel voiceMemberModel) {
        cacheManager.setMemberList(voiceMemberModel);
    }

    /**
     * Check if specified ID is in application list
     */
    public boolean checkMember(String chatUid) {
        return cacheManager.getSubmitMic(chatUid) != null;
    }

    /**
     * Check if member in invitation list is already on mic
     */
    public boolean checkInvitationMember(String chatUid) {
        return cacheManager.checkInvitationByChatUid(chatUid);
    }

    /**
     * Remove specific member from member list (called in member exit callback)
     */
    public void removeMember(String chatUid) {
        cacheManager.removeMember(chatUid);
    }

    /**
     * Set ranking list
     */
    public void setRankList(VoiceRankUserModel voiceRankUserModel) {
        cacheManager.setRankList(voiceRankUserModel);
    }

    /**
     * Get ranking list
     */
    public List<VoiceRankUserModel> getRankList() {
        return cacheManager.getRankList();
    }

    /**
     * Get VoiceMicInfoModel by chatUid
     *
     * @param chatUid
     * @return
     */
    public int getMicIndexByChatUid(String chatUid) {
        VoiceMicInfoModel bean = cacheManager.getMicInfoByChatUid(chatUid);
        if (bean != null) {
            return bean.getMicIndex();
        } else {
            return -1;
        }
    }

    /**
     * Remove members from room
     *
     * @param userList
     */
    public void removeMemberToRoom(List<String> userList, ValueCallBack<ChatRoom> callBack) {
        getChatRoomManager().asyncRemoveChatRoomMembers(chatroomId, userList, callBack);
    }
}
