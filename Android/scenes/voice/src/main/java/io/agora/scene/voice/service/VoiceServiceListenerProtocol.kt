package io.agora.scene.voice.service

import io.agora.scene.voice.imkit.bean.ChatMessageData
import io.agora.scene.voice.model.VoiceMemberModel

/**
 * @author create by zhangwei03
 *
 * IM KV callback protocol
 */
interface VoiceServiceListenerProtocol {

    fun onChatTokenWillExpire(){}
    /**
     * Receive gift message
     * @param roomId HuanXin IMSDK chatroom id
     * @param message
     */
    fun onReceiveGift(roomId: String, message: ChatMessageData?){}

    /**
     * Receive normal message
     * @param message
     */
    fun onReceiveTextMsg(roomId: String,message: ChatMessageData?){}

    /**
     * Receive mic request message
     * @param message Message object
     */
    fun onReceiveSeatRequest( message: ChatMessageData) {}

    /**
     * Receive cancel mic request message
     * @param chatUid HuanXin IM SDK user id
     */
    fun onReceiveSeatRequestRejected(chatUid: String) {}

    /**
     * Receive invitation message
     * @param message IM message object
     */
    fun onReceiveSeatInvitation(message: ChatMessageData) {}

    /**
     * Receive invitation rejection message
     * @param chatUid
     */
    fun onReceiveSeatInvitationRejected(chatUid: String, message: ChatMessageData?) {}

//    /**
//     * Receive request rejection message
//     * @param roomId HuanXin IM SDK chatroom id
//     */
//    fun onReceiveSeatRequestRejected(roomId: String, message: ChatMessageData) {}

    /**
     * Chatroom announcement update
     * @param roomId HuanXin IM SDK chatroom id
     * @param content Announcement content changes
     */
    fun onAnnouncementChanged(roomId: String, content: String) {}

    /**
     * User joined chatroom callback, with all user information
     * @param roomId HuanXin IM SDK chatroom id
     * @param voiceMember User data
     */
    fun onUserJoinedRoom(roomId: String, voiceMember: VoiceMemberModel) {}

    /**
     * User left room
     * @param roomId HuanXin IM SDK chatroom id
     * @param chatUid HuanXin user id who left
     */
    fun onUserLeftRoom(roomId: String, chatUid: String) {}

    /**
     * Chatroom member kicked from room
     * @param roomId HuanXin IM SDK chatroom id
     * @param reason Reason for being kicked
     */
    fun onUserBeKicked(roomId: String, reason: VoiceRoomServiceKickedReason) {}

    /**
     * Room destroyed
     */
    fun onRoomDestroyed(roomId: String){}

    /**
     * Room expired
     *
     * @param roomId
     */
    fun onRoomRoomExpire(roomId: String){}

    /**
     * Chatroom custom attributes changed
     * @param roomId HuanXin IM SDK chatroom id
     * @param attributeMap Changed attribute key-value pairs
     * @param fromId Who triggered the change
     */
    fun onAttributeMapUpdated(roomId: String, attributeMap: Map<String, String>, fromId: String) {}

    /**
     * RTM room user count
     *
     */
    fun onSyncUserCountUpdate(userCount: Int){}
}

enum class VoiceRoomServiceKickedReason{
    removed,
    destroyed,
    offLined,
}
