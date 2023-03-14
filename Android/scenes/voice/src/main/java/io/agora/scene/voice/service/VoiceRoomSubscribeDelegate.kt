package io.agora.scene.voice.service

import io.agora.scene.voice.imkit.bean.ChatMessageData
import io.agora.scene.voice.model.VoiceMemberModel

/**
 * @author create by zhangwei03
 *
 * im kv 回调协议
 */
interface VoiceRoomSubscribeDelegate {
    /**
     * 收到礼物消息
     * @param roomId 环信IMSDK聊天室id
     * @param message
     */
    fun onReceiveGift(roomId: String, message: ChatMessageData?){}

    /**
     * 接收到普通消息
     * @param message
     */
    fun onReceiveTextMsg(roomId: String,message: ChatMessageData?){}

    /**
     * 收到上麦申请消息
     * @param message 消息对象
     */
    fun onReceiveSeatRequest( message: ChatMessageData) {}

    /**
     * 收到取消上麦申请消息
     * @param chatUid 环信IM SDK 用户id
     */
    fun onReceiveSeatRequestRejected(chatUid: String) {}

    /**
     * 接收邀请消息
     * @param message IM消息对象
     */
    fun onReceiveSeatInvitation(message: ChatMessageData) {}

    /**
     * 接收拒绝邀请消息
     *  @param chatUid
     */
    fun onReceiveSeatInvitationRejected(chatUid: String, message: ChatMessageData?) {}

//    /**
//     * 接收拒绝申请消息
//     *  @param roomId 环信IM SDK聊天室id
//     */
//    fun onReceiveSeatRequestRejected(roomId: String, message: ChatMessageData) {}

    /**
     * 聊天室公告更新
     * @param roomId 环信IM SDK聊天室id
     * @param content 公告变化内容
     */
    fun onAnnouncementChanged(roomId: String, content: String) {}

    /**
     * 用户加入聊天室回调，带所有用户信息
     *  @param roomId 环信IM SDK聊天室id
     *  @param voiceMember 用户数据
     */
    fun onUserJoinedRoom(roomId: String, voiceMember: VoiceMemberModel) {}

    /**
     * 用户离开房间
     * @param roomId 环信IM SDK聊天室id
     * @param chatUid 离开的环信用户id
     */
    fun onUserLeftRoom(roomId: String, chatUid: String) {}

    /**
     * 聊天室成员被踢出房间
     * @param roomId 环信IM SDK聊天室id
     * @param reason 被踢出房间
     */
    fun onUserBeKicked(roomId: String, reason: VoiceRoomServiceKickedReason) {}

    /**
     * 房间销毁
     */
    fun onRoomDestroyed(roomId: String){}

    /**
     *  聊天室自定义麦位属性发生变化
     * @param roomId 环信IM SDK聊天室id
     * @param attributeMap 变换的属性kv
     * @param fromId 谁操作发生的变化
     */
    fun onSeatUpdated(roomId: String, attributeMap: Map<String, String>, fromId: String) {}
}

enum class VoiceRoomServiceKickedReason{
    removed,
    destroyed,
    offLined,
}
