package io.agora.scene.voice.imkit.custorm;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.MessageListener;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.CustomMessageBody;
import io.agora.scene.base.utils.GsonTools;
import io.agora.scene.voice.VoiceLogger;
import io.agora.scene.voice.global.VoiceCenter;
import io.agora.scene.voice.imkit.bean.ChatMessageData;
import io.agora.scene.voice.imkit.manager.ChatroomIMManager;
import io.agora.scene.voice.model.VoiceRoomApply;

/**
 * Custom message helper class (currently mainly used for gift, praise, and barrage messages in chatrooms).
 * Usage:
 * (1) Initialize {@link #init()}, add message listener, and initialize in the appropriate place based on business needs.
 * (2) Set chatroom information {@link #setChatRoomInfo(String)} to set the chatroom ID, used to filter chatroom messages
 * (3) Set custom message listener {@link #setOnCustomMsgReceiveListener(OnCustomMsgReceiveListener)}
 *     Used to receive different custom message types (currently only gift, praise, and barrage messages).
 * (4) Send custom message:
 *      a. If the custom message type is the same as the library and the parameters are the same or similar, you can directly call the following methods:
 *      {@link #sendGiftMsg(String,String,String,int,String,String, OnMsgCallBack)},
 *      {@link #sendPraiseMsg(int, OnMsgCallBack)},
 *      {@link #sendGiftMsg(Map, OnMsgCallBack)},
 *      {@link #sendPraiseMsg(Map, OnMsgCallBack)},
 *      b. If there are other custom message types, you can call the following methods:
 *      {@link #sendCustomMsg(String, Map, OnMsgCallBack)},
 *      {@link #sendCustomMsg(String, ChatMessage.ChatType, String, Map, OnMsgCallBack)}。
 * (5) Custom message type enumeration {@link CustomMsgType} defines the gift, praise, and barrage message types (distinguished by event).
 */
public class CustomMsgHelper implements MessageListener {
    private static CustomMsgHelper instance;
    private CustomMsgHelper(){}

    private String chatRoomId;
    private OnCustomMsgReceiveListener listener;
    private ArrayList<ChatMessageData> AllGiftList = new ArrayList<>();
    private ArrayList<ChatMessageData> AllNormalList = new ArrayList<>();

    public static CustomMsgHelper getInstance() {
        if(instance == null) {
            synchronized (CustomMsgHelper.class) {
                if(instance == null) {
                    instance = new CustomMsgHelper();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize based on business requirements, place it in application or other initialization places
     */
    public void init() {
        ChatClient.getInstance().chatManager().addMessageListener(this);
    }

    /**
     * Set the chat room ID
     * @param chatRoomId
     */
    public void setChatRoomInfo(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    /**
     * Set the listener for receiving messages
     * @param listener
     */
    public void setOnCustomMsgReceiveListener(OnCustomMsgReceiveListener listener) {
        this.listener = listener;
    }

    /**
     * Remove listener (after initialization in the page, remember to remove in the onDestroy() lifecycle)
     */
    public void removeListener() {
        ChatClient.getInstance().chatManager().removeMessageListener(this);
    }

    @Override
    public void onMessageReceived(List<ChatMessage> messages) {
        for (ChatMessage message : messages) {
            if (message.getType() == ChatMessage.Type.TXT){
                AllNormalList.add(ChatroomIMManager.getInstance().parseChatMessage(message));
                if(listener != null) {
                    listener.onReceiveNormalMsg(ChatroomIMManager.getInstance().parseChatMessage(message));
                }
            }
            // First determine if it is a custom message
            if(message.getType() != ChatMessage.Type.CUSTOM) {
                continue;
            }
            CustomMessageBody body = (CustomMessageBody) message.getBody();
            String event = body.event();
            CustomMsgType msgType = getCustomMsgType(event);

            if(msgType == null) {
                continue;
            }
            switch (msgType) {
                case CHATROOM_INVITE_SITE:
                    Map<String, String> inviteMap = getCustomMsgParams(ChatroomIMManager.getInstance().parseChatMessage(message));
                    if(listener != null) {
                        if (inviteMap.containsKey("chatroomId") && TextUtils.equals(chatRoomId,inviteMap.get("chatroomId"))){
                            listener.onReceiveInviteSite(ChatroomIMManager.getInstance().parseChatMessage(message));
                        }
                    }
                    break;
                case CHATROOM_APPLY_SITE:
                    Map<String, String> applyMap = getCustomMsgParams(ChatroomIMManager.getInstance().parseChatMessage(message));
                    if(listener != null ) {
                        if (applyMap.containsKey("chatroomId") && TextUtils.equals(chatRoomId,applyMap.get("chatroomId"))){
                            if (applyMap.containsKey("user")){
                                VoiceRoomApply voiceRoomApply = GsonTools.toBean(applyMap.get("user"),
                                        VoiceRoomApply.class);
                                if (voiceRoomApply != null && voiceRoomApply.getMember() != null){
                                    ChatroomIMManager.getInstance().setSubmitMicList(voiceRoomApply.getMember());
                                }
                            }
                            listener.onReceiveApplySite(ChatroomIMManager.getInstance().parseChatMessage(message));
                        }
                    }
                    break;
                case CHATROOM_CANCEL_APPLY_SITE:
                    if(listener != null) {
                        if (ChatroomIMManager.getInstance().checkMember(message.getFrom())){
                            ChatroomIMManager.getInstance().removeSubmitMember(message.getFrom());
                            listener.onReceiveCancelApplySite(ChatroomIMManager.getInstance().parseChatMessage(message));
                        }
                    }
                    break;
                case CHATROOM_INVITE_REFUSED_SITE:
                    if(listener != null) {
                        listener.onReceiveInviteRefusedSite(ChatroomIMManager.getInstance().parseChatMessage(message));
                    }
                    break;
                case CHATROOM_DECLINE_APPLY:
                    if(listener != null) {
                        listener.onReceiveDeclineApply(ChatroomIMManager.getInstance().parseChatMessage(message));
                    }
                    break;

            }
            // Exclude single chat
            if(message.getChatType() != ChatMessage.ChatType.GroupChat && message.getChatType() != ChatMessage.ChatType.ChatRoom) {
                continue;
            }
            String username = message.getTo();
            // Determine if it is the same chat room or group
            if(!TextUtils.equals(username, chatRoomId)) {
                continue;
            }
            // Determine if it is a custom message, then distinguish between gift, praise, and barrage messages
            // If event is empty, do not process
            if(TextUtils.isEmpty(event)) {
                continue;
            }
            // Return the message type of each message
            switch (msgType) {
                case CHATROOM_GIFT:
                    AllGiftList.add(ChatroomIMManager.getInstance().parseChatMessage(message));
                    if(listener != null) {
                        listener.onReceiveGiftMsg(ChatroomIMManager.getInstance().parseChatMessage(message));
                    }
                    break;
                case CHATROOM_PRAISE:
                    if(listener != null) {
                        listener.onReceivePraiseMsg(ChatroomIMManager.getInstance().parseChatMessage(message));
                    }
                    break;
                case CHATROOM_SYSTEM:
                    AllNormalList.add(ChatroomIMManager.getInstance().parseChatMessage(message));
                    if (listener != null){
                        listener.onReceiveSystem(ChatroomIMManager.getInstance().parseChatMessage(message));
                    }
                    break;
            }
        }
    }

    @Override
    public void onCmdMessageReceived(List<ChatMessage> list) {

    }

    @Override
    public void onMessageRead(List<ChatMessage> list) {

    }

    @Override
    public void onMessageDelivered(List<ChatMessage> list) {

    }

    @Override
    public void onMessageRecalled(List<ChatMessage> list) {

    }

    @Override
    public void onMessageChanged(ChatMessage emMessage, Object o) {

    }

    public ArrayList<ChatMessageData> getGiftData(String chatRoomId){
        ArrayList<ChatMessageData> data = new ArrayList<>();
        for (ChatMessageData chatMessageData : AllGiftList) {
            if (TextUtils.equals(chatRoomId,chatMessageData.getConversationId())){
                data.add(chatMessageData);
            }
        }
        AllGiftList.removeAll(data);
        return data;
    }

    public ArrayList<ChatMessageData> getNormalData(String chatRoomId){
        ArrayList<ChatMessageData> data = new ArrayList<>();
        for (ChatMessageData chatMessageData : AllNormalList) {
            if (TextUtils.equals(chatRoomId,chatMessageData.getConversationId())){
                data.add(chatMessageData);
            }
        }
        AllNormalList.removeAll(data);
        return data;
    }

    public void clear(){
        AllGiftList.clear();
        AllNormalList.clear();
    }

    public void addSendText(ChatMessageData data){
        AllNormalList.add(data);
    }

    /**
     * Send gift message
     * @param nickName  user name
     * @param portrait  user portrait
     * @param giftId    gift id
     * @param num       gift number
     * @param price     gift price
     * @param giftName  gift name
     * @param callBack  callback for sending message success or failure
     */
    public void sendGiftMsg(String nickName,String portrait,String giftId,int num,String price,String giftName, OnMsgCallBack callBack) {
        Map<String, String> params = new HashMap<>();
        params.put(MsgConstant.CUSTOM_GIFT_KEY_ID, giftId);
        params.put(MsgConstant.CUSTOM_GIFT_KEY_NUM, String.valueOf(num));
        params.put(MsgConstant.CUSTOM_GIFT_NAME,giftName);
        params.put(MsgConstant.CUSTOM_GIFT_PRICE,price);
        params.put(MsgConstant.CUSTOM_GIFT_USERNAME,nickName);
        params.put(MsgConstant.CUSTOM_GIFT_PORTRAIT,portrait);
        sendGiftMsg(params, callBack);
    }

    /**
     * Send system message (member join)
     * @param ownerId
     * @param callBack
     */
    public void sendSystemMsg(String ownerId, OnMsgCallBack callBack) {
        Map<String, String> params = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", VoiceCenter.getUserId());
            jsonObject.put("chat_uid", VoiceCenter.getChatUid());
            jsonObject.put("name", VoiceCenter.getNickname());
            jsonObject.put("portrait", VoiceCenter.getHeadUrl());
            jsonObject.put("rtc_uid", VoiceCenter.getRtcUid());
            jsonObject.put("mic_index", TextUtils.equals(ownerId, VoiceCenter.getChatUid()) ? "0" : "-1");
            jsonObject.put("micStatus", 1);
            params.put("user", jsonObject.toString());
            sendSystemMsg(params, callBack);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void sendSystemMsg(Map<String, String> params, final OnMsgCallBack callBack){
        if(params.size() <= 0) {
            return;
        }
        sendCustomMsg(CustomMsgType.CHATROOM_SYSTEM.getName(), params, callBack);
    }

    /**
     * Send gift message (multiple parameters)
     * @param params
     * @param callBack
     */
    public void sendGiftMsg(Map<String, String> params, final OnMsgCallBack callBack) {
        if(params.size() <= 0) {
            return;
        }
        sendCustomMsg(CustomMsgType.CHATROOM_GIFT.getName(), params, callBack);
    }

    /**
     * Send praise message
     * @param num
     * @param callBack
     */
    public void sendPraiseMsg(int num, OnMsgCallBack callBack) {
        if(num <= 0) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put(MsgConstant.CUSTOM_PRAISE_KEY_NUM, String.valueOf(num));
        sendPraiseMsg(params, callBack);
    }

    /**
     * Send praise message (multiple parameters)
     * @param params
     * @param callBack
     */
    public void sendPraiseMsg(Map<String, String> params, final OnMsgCallBack callBack) {
        if(params.size() <= 0) {
            return;
        }
        sendCustomMsg(CustomMsgType.CHATROOM_PRAISE.getName(), params, callBack);
    }

    /**
     * Send custom message
     * @param event
     * @param params
     * @param callBack
     */
    public void sendCustomMsg(String event, Map<String, String> params, final OnMsgCallBack callBack) {
        sendCustomMsg(chatRoomId, ChatMessage.ChatType.ChatRoom, event, params, callBack);
    }

    /**
     * Send custom message (targeted)
     * @param event
     * @param params
     * @param callBack
     */
    public void sendCustomSingleMsg(String to,String event, Map<String, String> params, final OnMsgCallBack callBack){
        sendCustomMsg(to, ChatMessage.ChatType.Chat, event, params, callBack);
    }

    /**
     * Send custom message
     * @param to
     * @param chatType
     * @param event
     * @param params
     * @param callBack
     */
    public void sendCustomMsg(String to, ChatMessage.ChatType chatType, String event, Map<String, String> params, final OnMsgCallBack callBack) {
        final ChatMessage sendMessage = ChatMessage.createSendMessage(ChatMessage.Type.CUSTOM);
        CustomMessageBody body = new CustomMessageBody(event);
        body.setParams(params);
        sendMessage.addBody(body);
        sendMessage.setTo(to);
        sendMessage.setChatType(chatType);
        sendMessage.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {
                if(callBack != null) {
                    if (event.equals(CustomMsgType.CHATROOM_SYSTEM.getName())){
                        AllNormalList.add(ChatroomIMManager.getInstance().parseChatMessage(sendMessage));
                    }else if (event.equals(CustomMsgType.CHATROOM_GIFT.getName())){
                        AllGiftList.add(ChatroomIMManager.getInstance().parseChatMessage(sendMessage));
                    }
                    callBack.onSuccess(ChatroomIMManager.getInstance().parseChatMessage(sendMessage));
                }
            }

            @Override
            public void onError(int i, String s) {
                if(callBack != null) {
                    callBack.onError(sendMessage.getMsgId(), i, s);
                }
            }

            @Override
            public void onProgress(int i, String s) {
                if(callBack != null) {
                    callBack.onProgress(i, s);
                }
            }
        });
        ChatClient.getInstance().chatManager().sendMessage(sendMessage);
    }

    /**
     * Get the ID of the gift in the gift message
     * @param msg
     * @return
     */
    public String getMsgGiftId(ChatMessageData msg) {
        if(!isGiftMsg(msg)) {
            return null;
        }

        Map<String, String> params = getCustomMsgParams(msg);
        VoiceLogger.d("getMsgGiftId","getMsgGiftId_1: "+params.toString());
        if(params.containsKey(MsgConstant.CUSTOM_GIFT_KEY_ID)) {
            VoiceLogger.d("getMsgGiftId",params.get(MsgConstant.CUSTOM_GIFT_KEY_ID));
            return params.get(MsgConstant.CUSTOM_GIFT_KEY_ID);
        }
        VoiceLogger.d("getMsgGiftId","getMsgGiftId_3");
        return null;
    }

    /**
     * Get the number of gifts in the gift message
     * @param msg
     * @return
     */
    public int getMsgGiftNum(ChatMessageData msg) {
        if(!isGiftMsg(msg)) {
            return 0;
        }
        Map<String, String> params = getCustomMsgParams(msg);
        if(params.containsKey(MsgConstant.CUSTOM_GIFT_KEY_NUM)) {
            String num = params.get(MsgConstant.CUSTOM_GIFT_KEY_NUM);
            if(TextUtils.isEmpty(num)) {
                return 0;
            }
            try {
                return Integer.valueOf(num);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * Get the number of praises in the praise message
     * @param msg
     * @return
     */
    public int getMsgPraiseNum(ChatMessageData msg) {
        if(!isPraiseMsg(msg)) {
            return 0;
        }
        Map<String, String> params = getCustomMsgParams(msg);
        if(params.containsKey(MsgConstant.CUSTOM_PRAISE_KEY_NUM)) {
            String num = params.get(MsgConstant.CUSTOM_PRAISE_KEY_NUM);
            if(TextUtils.isEmpty(num)) {
                return 0;
            }
            try {
                return Integer.valueOf(num);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }


    /**
     * Determine if it is a gift message
     * @param msg
     * @return
     */
    public boolean isGiftMsg(ChatMessageData msg) {
        return getCustomMsgType(getCustomEvent(msg)) == CustomMsgType.CHATROOM_GIFT;
    }

    /**
     * Determine if it is a praise message
     * @param msg
     * @return
     */
    public boolean isPraiseMsg(ChatMessageData msg) {
        return getCustomMsgType(getCustomEvent(msg)) == CustomMsgType.CHATROOM_PRAISE;
    }


    /**
     * Get the event field of the custom message
     * @param message
     * @return
     */
    public String getCustomEvent(ChatMessageData message) {
        if(message == null) {
            return null;
        }
        if(!message.getType().equals("custom")) {
            return null;
        }
        return message.getEvent();
    }

    /**
     * Get the parameters of the custom message
     * @param message
     * @return
     */
    public Map<String, String> getCustomMsgParams(ChatMessageData message) {
        if(message == null) {
            return null;
        }
        if(!message.getType().equals("custom")) {
            return null;
        }
        return message.getCustomParams();
    }

    /**
     * Get the ext parameter of the custom message
     * @param message
     * @return
     */
    public Map<String, Object> getCustomMsgExt(ChatMessageData message) {
        if(message == null) {
            return null;
        }
        if(!message.getType().equals("custom")) {
            return null;
        }
        return message.getExt();
    }

    public String getCustomVolume(ChatMessageData message){
        if(message == null) {
            return null;
        }
        if(!message.getType().equals("custom")) {
            return null;
        }
        if (message.getCustomParams().containsKey("volume")){
            return message.getCustomParams().get("volume");
        }
        return "";
    }

    /**
     * Get custom message type
     * @param event
     * @return
     */
    public CustomMsgType getCustomMsgType(String event) {
        if(TextUtils.isEmpty(event)) {
            return null;
        }
        return CustomMsgType.fromName(event);
    }
}
