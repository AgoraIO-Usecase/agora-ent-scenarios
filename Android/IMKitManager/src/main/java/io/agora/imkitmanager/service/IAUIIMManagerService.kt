package io.agora.imkitmanager.service

import io.agora.imkitmanager.model.AUIChatUserInfo
import io.agora.imkitmanager.service.http.CreateChatRoomResponse

interface IAUIIMManagerService {

    fun registerRespObserver(observer: AUIIMManagerRespObserver?)

    /**
     * 解绑响应事件回调
     *
     * @param observer 响应事件回调
     */
    fun unRegisterRespObserver(observer: AUIIMManagerRespObserver?)


    /**
     *  发送聊天室消息
     *  - text: 文本内容
     *  - userInfo: 用户信息
     */
    fun sendMessage(text: String, completion: (AgoraChatTextMessage?, Exception?) -> Unit)

    /**
     * 登录环信
     */
    fun loginChat(completion: (error: Exception?) -> Unit)

    /**
     * 创建环信聊天室
     *
     * @param input
     * @param completion
     * @receiver
     */
    fun createChatRoom(
        roomName: String = "",
        description: String = "",
        completion: (CreateChatRoomResponse?, Exception?) -> Unit
    )

    fun joinChatRoom(chatRoomId: String, completion: (error: Exception?) -> Unit)

    /**
     * 退出聊天室
     */
    fun userQuitRoom(completion: (error: Exception?) -> Unit)

    /**
     * 销毁聊天室
     */
    fun userDestroyedChatroom()

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