package io.agora.scene.voice.imkit.manager;

import java.util.List;
import java.util.Map;

import io.agora.scene.voice.imkit.bean.ChatMessageData;

public interface ChatroomListener {
    //收到正常文本消息
    void receiveTextMessage(String roomId, ChatMessageData message);
    //收到礼物消息
    void receiveGift(String roomId, ChatMessageData message);
    //接收申请消息
    default void receiveApplySite(String roomId,ChatMessageData message){}
    //接收取消申请消息 //确认？
    default void receiveCancelApplySite(String roomId,ChatMessageData message){}
    //接收邀请消息
    default void receiveInviteSite(String roomId,ChatMessageData message){}
    //接收拒绝邀请消息
    default void receiveInviteRefusedSite(String roomId,ChatMessageData message){}
    //接收拒绝申请消息
    default void receiveDeclineApply(String roomId,ChatMessageData message){}
    //用户加入房间 后面采用自定义消息
    default void userJoinedRoom(String roomId,String uid){}
    //用户离开房间
    default void onMemberExited(String roomId,String s1,String s2){}
    //聊天室公告更新
    default void announcementChanged(String roomId,String announcement){}
    //聊天室成员被踢出房间
    default void userBeKicked(String roomId,int reason){}
    //聊天室属性变更
    default void roomAttributesDidUpdated(String roomId, Map<String, String> attributeMap, String fromId){}
    //聊天室属性移除
    default void roomAttributesDidRemoved(String roomId, List<String> keyList, String fromId){}
    //token即将过期
    default void onTokenWillExpire(){}
    //收到系统消息
    default void receiveSystem(String roomId, ChatMessageData message){}
    //机器人音量更新
    default void voiceRoomUpdateRobotVolume(String roomId, String volume){}
    //聊天室销毁
    default void onRoomDestroyed(String roomId){}
}
