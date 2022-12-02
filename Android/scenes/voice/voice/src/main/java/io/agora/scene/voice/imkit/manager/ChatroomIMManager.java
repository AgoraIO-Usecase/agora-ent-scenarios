package io.agora.scene.voice.imkit.manager;

import android.text.TextUtils;
import android.util.Log;

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
import io.agora.chat.Conversation;
import io.agora.chat.CustomMessageBody;
import io.agora.chat.TextMessageBody;
import io.agora.scene.voice.imkit.bean.ChatMessageData;
import io.agora.scene.voice.imkit.custorm.CustomMsgHelper;
import io.agora.scene.voice.imkit.custorm.CustomMsgType;
import io.agora.scene.voice.imkit.custorm.OnCustomMsgReceiveListener;
import io.agora.scene.voice.imkit.custorm.OnMsgCallBack;
import io.agora.scene.voice.global.VoiceBuddyFactory;
import io.agora.scene.voice.model.VoiceGiftModel;
import io.agora.scene.voice.model.VoiceMemberModel;
import io.agora.scene.voice.model.VoiceMicInfoModel;
import io.agora.scene.voice.model.VoiceRankUserModel;
import io.agora.scene.voice.model.VoiceRoomInfo;
import io.agora.scene.voice.model.VoiceRoomModel;
import io.agora.util.EMLog;
import io.agora.voice.common.utils.LogTools;

public class ChatroomIMManager implements ChatRoomChangeListener, ConnectionListener {
    private static ChatroomIMManager instance;
    private ChatroomIMManager(){}
    private String chatroomId;
    private ArrayList<ChatMessageData> data = new ArrayList<>();
    public OnChatroomEventReceiveListener chatroomEventListener;
    public OnChatroomConnectionListener chatroomConnectionListener;
    private ChatroomProtocolDelegate delegate;
    private static final String TAG = "ChatroomIMManager";

    public static ChatroomIMManager getInstance() {
        if(instance == null) {
            synchronized (ChatroomIMManager.class) {
                if(instance == null) {
                    instance = new ChatroomIMManager();
                }
            }
        }
        return instance;
    }

    /**
     * 需要在详情页时候初始化，防止chatroomId为空或不正确
     * @param chatroomId
     */
    public void init(String chatroomId) {
        this.chatroomId = chatroomId;
        //设置消息监听
        setCustomMsgListener();
        //设置聊天室状态变化监听
        setChatRoomChangeListener();
        //设置连接监听
        setConnectionListener();
        //设置相关的房间信息
        CustomMsgHelper.getInstance().setChatRoomInfo(chatroomId);
        //设置语聊房协议代理
        delegate = new ChatroomProtocolDelegate(chatroomId);
        clearCache();
    }


    public String getCurrentRoomId() {
        return this.chatroomId;
    }

    /**
     * 设置自定义消息监听
     */
    public void setCustomMsgListener() {
        CustomMsgHelper.getInstance().init();
    }

    public void setChatRoomChangeListener(){
        ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(this);
    }

    public void removeChatRoomChangeListener(){
        ChatClient.getInstance().chatroomManager().removeChatRoomListener(this);
    }

    public void setConnectionListener(){
        ChatClient.getInstance().addConnectionListener(this);
    }

    public void removeChatRoomConnectionListener(){
        ChatClient.getInstance().removeConnectionListener(this);
    }

    public void setChatRoomEventListener(OnChatroomEventReceiveListener listener){
        this.chatroomEventListener = listener;
    }

    public void setChatRoomConnectionListener(OnChatroomConnectionListener listener){
        this.chatroomConnectionListener = listener;
    }

    /**
     * 移除自定义消息监听
     */
    public void removeCustomMsgListener() {
        CustomMsgHelper.getInstance().removeListener();
    }

    /**
     * 发送文本消息
     * @param content
     * @param callBack
     */
    public void sendTxtMsg(String content,String nickName, OnMsgCallBack callBack) {
        ChatMessage message = ChatMessage.createTextSendMessage(content, chatroomId);
        message.setAttribute("userName",nickName);
        message.setChatType(ChatMessage.ChatType.ChatRoom);
        message.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {
                CustomMsgHelper.getInstance().addSendText(parseChatMessage(message));
                callBack.onSuccess(parseChatMessage(message));
            }

            @Override
            public void onError(int i, String s) {
                callBack.onError(message.getMsgId(), i, s);
            }

            @Override
            public void onProgress(int i, String s) {
                callBack.onProgress(i, s);
            }
        });
        ChatClient.getInstance().chatManager().sendMessage(message);
    }

    /**
     * 发送礼物消息
     * @param giftId
     * @param num
     * @param nickName
     * @param callBack
     */
    public void sendGiftMsg(String nickName,String portrait,String giftId,int num,String price,String giftName, OnMsgCallBack callBack) {
        CustomMsgHelper.getInstance().sendGiftMsg(nickName,portrait,giftId,num,price,giftName ,new OnMsgCallBack() {
            @Override
            public void onSuccess(ChatMessageData message) {
                if(callBack != null) {
                    callBack.onSuccess(message);
                }
            }

            @Override
            public void onProgress(int i, String s) {
                super.onProgress(i, s);
                if(callBack != null) {
                    callBack.onProgress(i, s);
                }
            }

            @Override
            public void onError(String messageId, int code, String error) {
                if(callBack != null) {
                    callBack.onError(messageId, code, error);
                }
            }
        });
    }

    /**
     * 插入欢迎消息
     * @param content
     * @param nick
     */
    public void saveWelcomeMsg(String content,String nick){
        ChatMessage message = ChatMessage.createSendMessage(ChatMessage.Type.TXT);
        message.setChatType(ChatMessage.ChatType.ChatRoom);
        message.setTo(chatroomId);
        TextMessageBody textMessageBody = new TextMessageBody(content);
        message.setBody(textMessageBody);
        message.setAttribute("userName",nick);
        ChatClient.getInstance().chatManager().saveMessage(message);
    }

    public ArrayList<ChatMessageData> getMessageData(String chatroomId){
        data.clear();
        Conversation conversation = ChatClient.getInstance().chatManager().getConversation(chatroomId, Conversation.ConversationType.ChatRoom, true);
        if (conversation != null){
            EMLog.e("getMessageData",conversation.getAllMessages().size() + "");
            for (ChatMessage allMessage : conversation.getAllMessages()) {
                if (allMessage.getBody() instanceof TextMessageBody
                || allMessage.getBody() instanceof CustomMessageBody &&
                (((CustomMessageBody) allMessage.getBody()).event()).equals(CustomMsgType.CHATROOM_SYSTEM.getName())){
                    data.add(parseChatMessage(allMessage));
                }
            }
        }
        return data;
    }

    public ChatMessageData parseChatMessage(ChatMessage chatMessage){
        ChatMessageData chatMessageData = new ChatMessageData();
        chatMessageData.setForm(chatMessage.getFrom());
        chatMessageData.setTo(chatMessage.getTo());
        chatMessageData.setConversationId(chatMessage.conversationId());
        chatMessageData.setMessageId(chatMessage.getMsgId());
        if (chatMessage.getBody() instanceof TextMessageBody){
            chatMessageData.setType("text");
            chatMessageData.setContent(((TextMessageBody) chatMessage.getBody()).getMessage());
        }else if (chatMessage.getBody() instanceof CustomMessageBody){
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

    public String getUserName(ChatMessageData msg){
        String userName = "";
        Map<String,String> params = msg.getCustomParams();
        if (params.containsKey("userName")){
            userName = params.get("userName");
        }
        if (TextUtils.isEmpty(userName)){
            Map<String,Object> ext = msg.getExt();
            if (ext.containsKey("userName")){
                userName = (String) ext.get("userName");
            }
        }
        Log.e("Helper","getUserName: " + userName);
        return userName;
    }

    public String getSystemUserName(ChatMessageData msg){
        String jsonString = "";
        String userName = "";
        Map<String,String> params = msg.getCustomParams();
        if (params.containsKey("user")){
            jsonString = params.get("user");
            Log.e("getSystemUserName","jsonString: " + jsonString);
            if (!TextUtils.isEmpty(jsonString)){
                try {
                    assert jsonString != null;
                    JSONObject jsonObject = new JSONObject(jsonString);
                    userName = jsonObject.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e("getSystemUserName","userName: " + userName);
        return userName;
    }

    public String getUserPortrait(ChatMessageData msg){
        String userPortrait = "";
        Map<String,String> ext = msg.getCustomParams();
        if (ext.containsKey("portrait")){
            userPortrait = ext.get("portrait");
        }
        return userPortrait;
    }


    @Override
    public void onChatRoomDestroyed(String roomId, String s1) {
        if (chatroomEventListener != null && TextUtils.equals(roomId,chatroomId))
            chatroomEventListener.onRoomDestroyed(roomId);
    }

    @Override
    public void onMemberJoined(String roomId, String s1) {
        if (chatroomEventListener != null && TextUtils.equals(roomId,chatroomId))
            chatroomEventListener.onMemberJoined(roomId,s1);
    }

    @Override
    public void onMemberExited(String roomId, String s1, String s2) {
        if (chatroomEventListener != null && TextUtils.equals(roomId,chatroomId))
            chatroomEventListener.onMemberExited(roomId,s1,s2);
    }

    @Override
    public void onRemovedFromChatRoom(int i, String roomId, String s1, String s2) {
        if (chatroomEventListener != null && TextUtils.equals(roomId,chatroomId))
            chatroomEventListener.onKicked(roomId,i);
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
        if (chatroomEventListener != null && TextUtils.equals(roomId,chatroomId))
            chatroomEventListener.onAnnouncementChanged(roomId,announcement);
    }

    @Override
    public void onAttributesUpdate(String roomId, Map<String, String> attributeMap, String from) {
        if (chatroomEventListener != null && TextUtils.equals(roomId,chatroomId))
            chatroomEventListener.onAttributesUpdate(roomId,attributeMap,from);
    }

    @Override
    public void onAttributesRemoved(String roomId, List<String> keyList, String from) {
        if (chatroomEventListener != null && TextUtils.equals(roomId,chatroomId))
            chatroomEventListener.onAttributesRemoved(roomId,keyList,from);
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

    public void renewToken(String newToken){
        ChatClient.getInstance().renewToken(newToken);
    }

    public boolean isLoggedIn(){
        return ChatClient.getInstance().isLoggedIn();
    }

    public void login(String uid,String token,CallBack callBack){
        ChatClient.getInstance().loginWithAgoraToken(uid, token, new CallBack() {
            @Override
            public void onSuccess() {
                callBack.onSuccess();
                Log.d("ChatroomConfigManager","Login success");
            }

            @Override
            public void onError(int code, String msg) {
                Log.e("ChatroomConfigManager", "Login onError code:" + code + " desc: " + msg);
                callBack.onError(code,msg);
            }
        });
    }

    public void login(String uid,String token){
        ChatClient.getInstance().loginWithAgoraToken(uid, token, new CallBack() {
            @Override
            public void onSuccess() {
                Log.d("ChatroomConfigManager","Login success");
            }

            @Override
            public void onError(int code, String msg) {
                Log.e("ChatroomConfigManager", "Login onError code:" + code + " desc: " + msg);
            }
        });
    }

    public void logout(boolean unbind,CallBack callBack){
        ChatClient.getInstance().logout(unbind, new CallBack() {
            @Override
            public void onSuccess() {
                callBack.onSuccess();
            }

            @Override
            public void onError(int i, String s) {
                callBack.onError(i,s);
            }
        });
    }

    public void logout(boolean unbind){
        ChatClient.getInstance().logout(unbind, new CallBack() {
            @Override
            public void onSuccess() {
                EMLog.d(TAG,"logout onSuccess");
            }

            @Override
            public void onError(int code, String desc) {
                EMLog.e(TAG,"logout onError code: " + code + "  " + desc);
            }
        });
    }

    /**
     * 获取当前用户实体类
     */
    public VoiceMemberModel getMySelfModel(){
       return delegate.getMySelfModel();
    }

    public void clearCache(){
        delegate.clearCache();
    }

    /**
     * 初始化麦位信息
     * @param roomType
     * @param callBack
     */
    public void initMicInfo(int roomType,CallBack callBack){
        VoiceMemberModel voiceMemberModel = new VoiceMemberModel(
                VoiceBuddyFactory.get().getVoiceBuddy().userId(),
                VoiceBuddyFactory.get().getVoiceBuddy().chatUserName(),
                VoiceBuddyFactory.get().getVoiceBuddy().nickName(),
                VoiceBuddyFactory.get().getVoiceBuddy().headUrl(),
                VoiceBuddyFactory.get().getVoiceBuddy().rtcUid(),
                0);
        LogTools.logE("initMicInfo:" + voiceMemberModel, TAG);
        delegate.initMicInfo(roomType,voiceMemberModel,callBack);
    }

    /**
     * 获取详情
     * @param voiceRoomModel
     * @param callBack
     */
    public void fetchRoomDetail(VoiceRoomModel voiceRoomModel, ValueCallBack<VoiceRoomInfo> callBack) {
        // 麦位信息
        VoiceMemberModel owner = voiceRoomModel.getOwner();
        if (owner != null && TextUtils.equals(owner.getUserId(), VoiceBuddyFactory.get().getVoiceBuddy().userId())) {
            initMicInfo(voiceRoomModel.getRoomType(), new CallBack() {
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
     * 邀请上麦
     * @param chatUid
     * @param callBack
     */
    public void invitationMic(String chatUid,CallBack callBack){delegate.invitationMic(chatUid,null,callBack);}

    public void invitationMic(String chatUid,int micIndex,CallBack callBack){delegate.invitationMic(chatUid,micIndex,callBack);}


    /**
     * 禁言指定麦位置
     * @param micIndex
     * @param callBack
     */
    public void forbidMic(int micIndex,ValueCallBack<VoiceMicInfoModel> callBack){
        delegate.forbidMic(micIndex,callBack);
    }

    /**
     * 取消禁言指定麦位置
     * @param micIndex
     * @param callBack
     */
    public void unForbidMic(int micIndex,ValueCallBack<VoiceMicInfoModel> callBack){
        delegate.unForbidMic(micIndex,callBack);
    }

    /**
     * 锁麦
     * @param micIndex
     * @param callBack
     */
    public void lockMic(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack){
        delegate.lockMic(micIndex,callBack);
    }

    /**
     * 取消锁麦
     * @param micIndex
     * @param callBack
     */
    public void unLockMic(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack){
        delegate.unLockMic(micIndex, callBack);
    }

    /**
     * 踢用户下麦
     * @param micIndex
     * @param callBack
     */
    public void kickOff(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack){
        delegate.kickOff(micIndex, callBack);
    }

    /**
     * 下麦
     * @param micIndex
     * @param callBack
     */
    public void leaveMic(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack){
        delegate.leaveMic(micIndex,callBack);
    }

    /**
     * 关麦
     * @param micIndex
     * @param callBack
     */
    public void muteLocal(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack){
        delegate.muteLocal(micIndex,callBack);
    }

    /**
     * 取消关麦
     * @param micIndex
     * @param callBack
     */
    public void unMuteLocal(int micIndex, ValueCallBack<VoiceMicInfoModel> callBack){
        delegate.unMuteLocal(micIndex,callBack);
    }

    /**
     * 换麦
     * @param oldIndex
     * @param newIndex
     * @param callBack
     */
    public void changeMic(int oldIndex,int newIndex,ValueCallBack<Map<Integer,VoiceMicInfoModel>> callBack){
        delegate.changeMic(oldIndex,newIndex,callBack);
    }


    /**
     * 接受邀请
     * @param callBack
     */
    public void acceptMicSeatInvitation(ValueCallBack<VoiceMicInfoModel> callBack){
        delegate.acceptMicSeatInvitation(VoiceBuddyFactory.get().getVoiceBuddy().chatUserName(),null,callBack);
    }

    /**
     * 拒绝邀请
     * @param chatUid
     * @param callBack
     */
    public void refuseInvite(String chatUid,CallBack callBack){ delegate.refuseInviteToMic(chatUid,callBack);}

    /**
     * 申请上麦
     * @param micIndex
     * @param callBack
     */
    public void startMicSeatApply(int micIndex, CallBack callBack){ delegate.startMicSeatApply(micIndex,callBack);}
    public void startMicSeatApply(CallBack callBack){ delegate.startMicSeatApply(null,callBack);}


    /**
     * 同意申请
     * @param callBack
     */
    public void acceptMicSeatApply(String chatUid,ValueCallBack<VoiceMicInfoModel> callBack){
        delegate.acceptMicSeatApply(chatUid,null,callBack);
    }

    /**
     * 取消上麦
     * @param chatUid
     * @param callBack
     */
    public void cancelMicSeatApply(String chatUid,CallBack callBack){
        delegate.cancelSubmitMic(chatUid,callBack);
    }


    /**
     * 更新公告
     * @param content
     */
    public void updateAnnouncement(String content,CallBack callBack){
        delegate.updateAnnouncement(content, callBack);
    }

    /**
     * 举手列表
     * @return
     */
    public List<VoiceMemberModel> fetchRaisedList(){
        return delegate.fetchRaisedList();
    }

    /**
     * 是否启用机器人
     * @param enable true 启动机器人，false 关闭机器人
     * @param callBack
     */
    public void enableRobot(Boolean enable,ValueCallBack<Boolean> callBack){
        delegate.enableRobot(enable,callBack);
    }

    /**
     * 更新机器人音量
     * @param volume
     * @param callBack
     */
    public void updateRobotVolume(int volume,CallBack callBack){
        delegate.updateRobotVolume(volume,callBack);
    }

    /**
     * 获取用户列表
     */
    public List<VoiceMemberModel> fetchRoomMembers(){
        return delegate.fetchRoomMembers();
    }

    /**
     * 获取排行榜列表
     */
    public void fetchGiftContribute(ValueCallBack<List<VoiceRankUserModel>> callBack){
        delegate.fetchGiftContribute(callBack);
    }

    /**
     * 更新排行榜
     * @param giftModel
     * @param callBack
     */
    public void updateRankList(String chatUid,VoiceGiftModel giftModel, CallBack callBack){
        delegate.updateRankList(chatUid,giftModel,callBack);
    }

    /**
     * 更新本地kv缓存
     * @param kvMap
     */
    public void updateMicInfoCache(Map<String,String> kvMap){
        delegate.updateMicInfoCache(kvMap);
    }

    /**
     * 更新礼物总数
     */
    public void updateAmount(String chatUid,int amount,CallBack callBack){
        delegate.updateGiftAmount(chatUid,amount,callBack);
    }

}
