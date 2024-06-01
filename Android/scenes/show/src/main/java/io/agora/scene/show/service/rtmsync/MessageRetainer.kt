package io.agora.scene.show.service.rtmsync

import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmMessageRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserRespObserver
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper
import java.util.UUID

/**
 * 消息持有者。用于管理消息的发送和接收，以及消息的缓存。消息结构如下：
 * {"id":"","content":"{\"id\":\"123\",\"content\":\"hello\"}","key":"xxx","publisherId":"xxx"}
 *
 * @param T 消息类型
 * @property rtmManager RTM管理器
 * @property channelName 频道名称
 * @property type 消息类型
 */
class MessageRetainer(
    private val rtmManager: AUIRtmManager,
    private val channelName: String,
    private val key: String,
) {

    private val messageList = mutableListOf<MessageInfo>()
    private val observableHelper = ObservableHelper<(MessageInfo)->Unit>()

    private val messageObserver = object : AUIRtmMessageRespObserver {
        override fun onMessageReceive(
            channelName: String,
            publisherId: String,
            message: String
        ) {
            val msg = GsonTools.toBean(message, MessageInfo::class.java) ?: return
            if (key != msg.key) {
                return
            }
            insertMessage(msg, true)
        }
    }

    private val userObserver = object : AUIRtmUserRespObserver {
        override fun onUserSnapshotRecv(
            channelName: String,
            userId: String,
            userList: List<Map<String, Any>>
        ) {

        }

        override fun onUserDidJoined(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>
        ) {

        }

        override fun onUserDidLeaved(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>
        ) {
            removeMessages(filter = { it.publisherId == userId })
        }

        override fun onUserDidUpdated(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>
        ) {

        }
    }

    private fun insertMessage(msg: MessageInfo, notify: Boolean) {
        val index = messageList.indexOfFirst { it.id == msg.id }
        if (index == -1) {
            messageList.add(msg)
            if (notify) {
                observableHelper.notifyEventHandlers {
                    it.invoke(msg)
                }
            }
        }
    }

    init {
        rtmManager.subscribeMessage(messageObserver)
        rtmManager.subscribeUser(userObserver)
    }

    fun release() {
        rtmManager.unsubscribeMessage(messageObserver)
        observableHelper.unSubscribeAll()
        rtmManager.unsubscribeUser(userObserver)
    }

    fun subscribe(onReceived: (MessageInfo)->Unit) {
        observableHelper.subscribeEvent(onReceived)
    }

    fun unsubscribe(onReceived: (MessageInfo)->Unit) {
        observableHelper.unSubscribeEvent(onReceived)
    }

    fun sendMessage(
        message: String,
        userId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    ) {
        val messageInfo = MessageInfo(key = key, publisherId = AUIRoomContext.shared().currentUserInfo.userId, content = message)
        val typeMessage = GsonTools.beanToString(messageInfo)
        if (typeMessage == null) {
            error?.invoke(RuntimeException("message serialize failed"))
            return
        }
        rtmManager.publish(
            channelName,
            userId,
            typeMessage
        ) {
            if (it != null) {
                error?.invoke(RuntimeException(it))
                return@publish
            }
            if (userId.isNotEmpty()) {
                insertMessage(messageInfo, false)
            }
            success?.invoke()
        }
    }

    fun getMessage(predicate: (MessageInfo) -> Boolean): MessageInfo? {
        val found = messageList.findLast(predicate)
        if (found != null) {
            messageList.remove(found)
        }
        return found
    }

    fun removeMessage(id: String) {
        messageList.removeIf { it.id == id }
    }

    fun removeMessages(filter: ((MessageInfo) -> Boolean)? = null) {
        if (filter != null) {
            messageList.removeIf(filter)
        } else {
            messageList.clear()
        }
    }
}

data class MessageInfo(
    val id: String = UUID.randomUUID().toString(),
    val key: String,
    val publisherId: String,
    val content: String
)