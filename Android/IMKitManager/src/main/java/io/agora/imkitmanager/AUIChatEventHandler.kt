package io.agora.imkitmanager

import io.agora.imkitmanager.model.AgoraChatMessage

interface AUIChatEventHandler {

    /**
     * 接收到普通消息
     * @param message
     */
    fun onReceiveTextMsg(chatRoomId: String?,message: AgoraChatMessage?){}

    /**
     * 收到系统消息(成员加入)
     * @param roomId 环信聊天室id
     * @param message
     */
    fun onReceiveMemberJoinedMsg(chatRoomId: String?, message: AgoraChatMessage?){}


    /**
     * 聊天室成员被踢出房间
     * @param roomId 环信IM SDK聊天室id
     * @param reason 被踢出房间
     */
    fun onUserBeKicked(chatRoomId: String?, reason: VoiceRoomServiceKickedReason?) {}


    /**
     * 房间销毁
     */
    fun onRoomDestroyed(chatRoomId: String?){}

    /**
     * IM 连接监听
     */
    fun onConnected(){}

    /**
     * IM 断开连接监听
     */
    fun onDisconnected(code:Int){}

}

enum class VoiceRoomServiceKickedReason{
    removed,
    destroyed,
    offLined,
}