package io.agora.imkitmanager.service

import io.agora.imkitmanager.model.AUIChatEntity
import io.agora.imkitmanager.model.AUIChatRoomInfo
import io.agora.imkitmanager.model.AUIChatUserInfo
import io.agora.imkitmanager.service.http.CreateChatRoomResponse
import io.agora.imkitmanager.ui.AUIChatInfo
import io.agora.imkitmanager.ui.IAUIChatListView
import io.agora.imkitmanager.ui.impl.AUIChatListView

interface IAUIIMManagerService {

    fun registerRespObserver(observer: AUIIMManagerRespObserver?)

    /**
     * 解绑响应事件回调
     *
     * @param observer 响应事件回调
     */
    fun unRegisterRespObserver(observer: AUIIMManagerRespObserver?)

    /**
     * 插入本地消息，
     *
     * @param text
     * @param index 插入位置，-1 插入尾部
     * @param completion
     * @receiver
     */
    fun insertLocalMessage(
        text: String, index: Int,
        completion: (chatMessage: AgoraChatTextMessage?, error: Exception?) -> Unit
    )

    /**
     * 发送聊天室消息
     *
     * @param text
     * @param completion
     * @param localMsg
     * @receiver
     */
    fun sendMessage(
        text: String, completion: (chatMessage: AgoraChatTextMessage?, error: Exception?) -> Unit,
    )

    /**
     * 登录环信
     *
     * @param completion
     * @receiver
     */
    fun loginChat(completion: (error: Exception?) -> Unit)

    /**
     * 登出环信
     *
     * @param completion
     * @receiver
     */
    fun logoutChat(completion: (error: Exception?) -> Unit)

    /**
     * 创建环信聊天室
     *
     * @param roomName
     * @param description
     * @param completion
     * @receiver
     */
    fun createChatRoom(
        roomName: String = "", description: String = "", completion: (chatId: String?, error: Exception?) -> Unit
    )

    /**
     * 加入环信聊天室
     *
     * @param chatRoomInfo
     * @param completion
     * @receiver
     */
    fun joinChatRoom(chatRoomInfo: AUIChatRoomInfo, completion: (error: Exception?) -> Unit)

    /**
     * 退出环信聊天室
     *
     * @param completion
     * @receiver
     */
    fun leaveChatRoom(completion: (error: Exception?) -> Unit)

    fun setChatListView(view: IAUIChatListView)

    data class AgoraChatTextMessage(
        val messageId: String?,
        val content: String?,
        val user: AUIChatUserInfo?
    )

    interface AUIIMManagerRespObserver {

        /**
         * 接收到消息
         */
        fun messageDidReceive(chatRoomId: String, message: AgoraChatTextMessage)

        /**
         * 用户加入聊天室
         */
        fun onUserDidJoinRoom(chatRoomId: String, message: AgoraChatTextMessage)
    }
}