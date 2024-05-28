package io.agora.scene.show.service.rtmsync

import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmMessageRespObserver
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper
import io.agora.rtmsyncmanager.utils.ThreadManager
import org.json.JSONObject

internal class MessageRetainerProvider(
    private val rtmManager: AUIRtmManager,
) {
    private val messageRetainerMap = mutableMapOf<String, MutableList<MessageRetainer<*>>>()

    fun <T> getMessageRetainer(
        channelName: String,
        type: Int,
        clazz: Class<T>,
        predicate: (cache: T, received: T) -> Boolean
    ): MessageRetainer<T> {
        val list = messageRetainerMap.getOrPut(channelName) { mutableListOf() }
        list.find { it.type == type }?.let {
            return it as MessageRetainer<T>
        }
        val retainer = MessageRetainer(
            rtmManager,
            channelName,
            type,
            clazz,
            predicate
        )
        list.add(retainer)
        return retainer
    }


    fun clean(channelName: String) {
        messageRetainerMap[channelName]?.forEach {
            it.release()
        }
        messageRetainerMap.remove(channelName)
    }

    fun destroy() {
        messageRetainerMap.forEach {
            it.value.forEach { retainer ->
                retainer.release()
            }
        }
        messageRetainerMap.clear()
    }

}


/**
 * 消息持有者。用于管理消息的发送和接收，以及消息的缓存。消息结构如下：
 * {"content":"{\"id\":\"123\",\"content\":\"hello\"}","type":1}
 *
 * @param T 消息类型
 * @property rtmManager RTM管理器
 * @property channelName 频道名称
 * @property type 消息类型
 * @property clazz 消息类型的类
 * @property updatePredicate 更新判断器
 */
internal class MessageRetainer<T>(
    private val rtmManager: AUIRtmManager,
    private val channelName: String,
    val type: Int,
    private val clazz: Class<T>,
    private val updatePredicate: (cache: T, received: T) -> Boolean
) {

    private val messageList = mutableListOf<T>()
    private val onUpdateObservableHelper = ObservableHelper<(T) -> Unit>()
    private val onAddObservableHelper = ObservableHelper<(T) -> Unit>()

    private val messageObserver = object : AUIRtmMessageRespObserver {
        override fun onMessageReceive(
            channelName: String,
            publisherId: String,
            message: String
        ) {
            if (message.getMessageType() != type) {
                return
            }
            val msg = GsonTools.toBean(message.getMessageContent(), clazz) ?: return
            insertMessage(msg, true)
        }
    }

    private fun insertMessage(msg: T, notify: Boolean) {
        val index = messageList.indexOfFirst { updatePredicate(it, msg) }
        if (index == -1) {
            messageList.add(msg)
            if (notify) {
                onAddObservableHelper.notifyEventHandlers {
                    it.invoke(msg)
                }
            }

        } else {
            messageList[index] = msg
            if (notify) {
                onUpdateObservableHelper.notifyEventHandlers {
                    it.invoke(msg)
                }
            }
        }
    }

    init {
        rtmManager.subscribeMessage(messageObserver)
    }

    fun release() {
        rtmManager.unsubscribeMessage(messageObserver)
        onAddObservableHelper.unSubscribeAll()
        onUpdateObservableHelper.unSubscribeAll()
    }

    fun subscribe(onAdd: ((T) -> Unit)? = null, onUpdate: ((T) -> Unit)? = null) {
        onAddObservableHelper.subscribeEvent(onAdd)
        onUpdateObservableHelper.subscribeEvent(onUpdate)
    }

    fun unsubscribe(onAdd: ((T) -> Unit)? = null, onUpdate: ((T) -> Unit)? = null) {
        onAddObservableHelper.unSubscribeEvent(onAdd)
        onUpdateObservableHelper.unSubscribeEvent(onUpdate)
    }

    fun sendMessage(
        message: T,
        userId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    ) {
        val typeMessage = GsonTools.beanToString(message)?.withMessageType(type)
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
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(it))
                }
                return@publish
            }
            if (userId.isNotEmpty()) {
                insertMessage(message, false)
            }
            ThreadManager.getInstance().runOnMainThread {
                success?.invoke()
            }
        }
    }

    fun getMessages(): List<T> {
        return messageList
    }

    fun removeMessage(predicate: (T) -> Boolean): T? {
        val found = messageList.findLast(predicate)
        if (found != null) {
            messageList.remove(found)
        }
        return found
    }

    fun cleanMessages(filter: ((T) -> Boolean)? = null) {
        if (filter != null) {
            messageList.removeIf(filter)
        } else {
            messageList.clear()
        }
    }

    // 获取消息类型
    private fun String.getMessageType(): Int {
        val json = JSONObject(this)
        return json.optInt("type")
    }

    // 获取消息内容
    private fun String.getMessageContent(): String {
        val json = JSONObject(this)
        return json.optString("content")
    }


    // 给消息内容string带上消息类型组成新的json字符串
    private fun String.withMessageType(type: Int): String {
        val json = JSONObject()
        json.put("type", type)
        json.put("content", this)
        return json.toString()
    }

}