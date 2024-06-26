package io.agora.scene.show.service.rtmsync

import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmMessageRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserLeaveReason
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserRespObserver
import io.agora.rtmsyncmanager.utils.AUILogger
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
    private val tag = "MessageRetainer($channelName, $key)"
    private val messageList = mutableListOf<MessageInfo>()
    private val observableHelper = ObservableHelper<(MessageInfo)->Unit>()

    private val messageObserver = object : AUIRtmMessageRespObserver {
        override fun onMessageReceive(
            channelName: String,
            publisherId: String,
            message: String
        ) {
            val msg = GsonTools.toBeanSafely(message, MessageInfo::class.java) ?: return
            if (key != msg.key || this@MessageRetainer.channelName != msg.channelName) {
                return
            }
            AUILogger.logger().d(tag, "onMessageReceive $msg")
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
            userInfo: Map<String, Any>,
            reason: AUIRtmUserLeaveReason
        ) {
            if (this@MessageRetainer.channelName != channelName) {
                return
            }
            AUILogger.logger().d(tag, "onUserDidLeaved $userId")
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
        AUILogger.logger().d(tag, "release")
        rtmManager.unsubscribeMessage(messageObserver)
        observableHelper.unSubscribeAll()
        rtmManager.unsubscribeUser(userObserver)
    }

    fun subscribe(onReceived: (MessageInfo)->Unit) {
        AUILogger.logger().d(tag, "subscribe $onReceived")
        observableHelper.subscribeEvent(onReceived)
    }

    fun unsubscribe(onReceived: (MessageInfo)->Unit) {
        AUILogger.logger().d(tag, "unsubscribe $onReceived")
        observableHelper.unSubscribeEvent(onReceived)
    }

    fun sendMessage(
        message: String,
        userId: String,
        channelName: String = this@MessageRetainer.channelName,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    ) {
        val messageInfo = MessageInfo(key = key, publisherId = AUIRoomContext.shared().currentUserInfo.userId, channelName = channelName, content = message)
        val typeMessage = GsonTools.beanToString(messageInfo)
        if (typeMessage == null) {
            AUILogger.logger().d(tag, "sendMessage >> message serialize failed : $message")
            error?.invoke(RuntimeException("message serialize failed"))
            return
        }
        AUILogger.logger().d(tag, "sendMessage >> publish $channelName $userId $typeMessage")
        rtmManager.publish(
            channelName,
            userId,
            typeMessage
        ) {
            AUILogger.logger().d(tag, "sendMessage >> publish complete: $it")
            if (it != null) {
                error?.invoke(RuntimeException(it))
                return@publish
            }
            if (userId.isNotEmpty()) {
                AUILogger.logger().d(tag, "sendMessage >> insertMessage $messageInfo")
                insertMessage(messageInfo, false)
            }
            success?.invoke()
        }
    }

    fun getMessage(predicate: (MessageInfo) -> Boolean): MessageInfo? {
        return messageList.findLast(predicate)
    }

    fun removeMessage(id: String) {
        AUILogger.logger().d(tag, "removeMessage $id")
        messageList.removeIf { it.id == id }
    }

    fun removeMessages(filter: ((MessageInfo) -> Boolean)? = null) {
        AUILogger.logger().d(tag, "removeMessages $filter")
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
    val channelName: String,
    val publisherId: String,
    val content: String
)