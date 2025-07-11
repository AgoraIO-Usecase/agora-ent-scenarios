package io.agora.imkitmanager

import android.content.Context
import android.text.TextUtils
import io.agora.CallBack
import io.agora.ChatRoomChangeListener
import io.agora.MessageListener
import io.agora.ValueCallBack
import io.agora.chat.ChatClient
import io.agora.chat.ChatMessage
import io.agora.chat.ChatOptions
import io.agora.chat.ChatRoom
import io.agora.chat.CustomMessageBody
import io.agora.chat.TextMessageBody
import io.agora.chat.adapter.EMAChatRoomManagerListener
import io.agora.imkitmanager.model.AUIChatCommonConfig
import io.agora.imkitmanager.model.AUIChatEntity
import io.agora.imkitmanager.model.AUICustomMsgType
import io.agora.imkitmanager.model.AUIChatRoomContext
import io.agora.imkitmanager.model.AUIChatUserInfo
import io.agora.imkitmanager.model.AgoraChatMessage
import io.agora.imkitmanager.service.callback.AUIChatMsgCallback
import io.agora.imkitmanager.ui.AUIChatInfoType
import io.agora.imkitmanager.utils.GsonTools
import io.agora.imkitmanager.utils.ProcessUtils
import org.json.JSONObject

class AUIChatManager constructor(private val commonConfig: AUIChatCommonConfig) : MessageListener,
    ChatRoomChangeListener {

    private val tag = "AUIChatManager"
    private val chatEventHandlers = mutableListOf<AUIChatEventHandler>()

    private var chatRoomId: String = ""

    private val currentMsgList: ArrayList<AUIChatEntity> = ArrayList()

    init {

        createChatClient(commonConfig.context, commonConfig.imAppKey)
    }

    private val chatLogCallback get() = AUIChatRoomContext.shared().mCommonConfig?.chatLogCallback

    private fun createChatClient(context: Context, appKey: String) {
        // ------------------ 初始化IM ------------------
        val options = ChatOptions()
        options.appKey = appKey
        options.autoLogin = false
        if (!ProcessUtils.isMainProcess(context)) {
            return
        }
        ChatClient.getInstance().init(context, options)
    }


    fun subscribeChatMsg(delegate: AUIChatEventHandler) {
        chatEventHandlers.add(delegate)
    }

    fun unsubscribeChatMsg(delegate: AUIChatEventHandler?) {
        chatEventHandlers.remove(delegate)
    }

    private fun setOnManagerListener() {
        chatLogCallback?.onDebugInfo(tag, "setOnManagerListener")
        if (ChatClient.getInstance().isSdkInited) {
            ChatClient.getInstance().chatManager().addMessageListener(this)
            ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(this)
        }
    }

    fun clear() {
        chatRoomId = ""
        currentMsgList.clear()
    }

    fun loginChat(userId: String, userToken: String, callback: CallBack) {
        chatLogCallback?.onDebugInfo(tag, "loginChat called")
        ChatClient.getInstance().loginWithToken(userId, userToken, object : CallBack {
            override fun onSuccess() {
                callback.onSuccess()
                chatLogCallback?.onDebugInfo(tag, "loginChat success")
            }

            override fun onError(code: Int, error: String?) {
                callback.onError(code, error)
                chatLogCallback?.onErrorInfo(tag, "loginChat error code=$code message=$error")
            }
        })
    }

    fun logoutChat(callback: CallBack? = null) {
        chatLogCallback?.onDebugInfo(tag, "logoutChat called")
        if (ChatClient.getInstance().isSdkInited) {
            ChatClient.getInstance().logout(false, object : CallBack {
                override fun onSuccess() {
                    callback?.onSuccess()
                    chatLogCallback?.onDebugInfo(tag, "logoutChat success")
                }

                override fun onError(code: Int, error: String?) {
                    callback?.onError(code, error)
                    chatLogCallback?.onErrorInfo(tag, "logoutChat success")
                }
            })
        }
    }

    /**
     * 加入房间
     */
    fun joinChatRoom(chatRoomId: String, callback: AUIChatMsgCallback) {
        currentMsgList.clear()
        chatLogCallback?.onDebugInfo(tag, "joinChatRoom called")
        this.chatRoomId = chatRoomId
        setOnManagerListener()
        ChatClient.getInstance().chatroomManager().joinChatRoom(chatRoomId, object : ValueCallBack<ChatRoom> {
            override fun onSuccess(value: ChatRoom?) {
                chatLogCallback?.onDebugInfo(tag, "joinChatRoom onSuccess $chatRoomId")
                //加入成功后 返回成员加入消息
                sendJoinMsg(chatRoomId, AUIChatRoomContext.shared().currentUserInfo, object : AUIChatMsgCallback {
                    override fun onOriginalResult(error: Exception?, message: ChatMessage?) {
                        // 无论发送加入房间消息是否成功，用户都已经加入房间了
                        callback.onOriginalResult(null, message)
                    }
                })
            }

            override fun onError(code: Int, errorMsg: String?) {
                chatLogCallback?.onErrorInfo(tag, "joinChatRoom onError $chatRoomId $code $errorMsg")
                this@AUIChatManager.chatRoomId = ""
                callback.onOriginalResult(
                    Exception("joinChatRoom error code=$code,message=$errorMsg"), null
                )
            }
        })
    }

    /**
     * 离开房间
     */
    fun leaveChatRoom() {
        chatLogCallback?.onDebugInfo(tag, "leaveChatRoom called")
        if (ChatClient.getInstance().isSdkInited) {
            ChatClient.getInstance().chatManager().removeMessageListener(this)
            ChatClient.getInstance().chatroomManager().removeChatRoomListener(this)
            ChatClient.getInstance().chatroomManager().leaveChatRoom(chatRoomId)
        }
    }

    /**
     * 销毁房间
     *
     */
    fun destroyChatRoom() {
        chatLogCallback?.onDebugInfo(tag, "destroyChatRoom called")
        if (ChatClient.getInstance().isSdkInited) {
            // 同步销毁房间阻塞线程
            ChatClient.getInstance().chatroomManager().destroyChatRoom(chatRoomId)
        }
    }

    /**
     * 销毁房间
     */
    fun asyncDestroyChatRoom(callBack: CallBack?) {
        chatLogCallback?.onDebugInfo(tag, "asyncDestroyChatRoom called")
        ChatClient.getInstance().chatroomManager().asyncDestroyChatRoom(chatRoomId, object : CallBack {
            override fun onSuccess() {
                chatLogCallback?.onDebugInfo(tag, "asyncDestroyChatRoom success")
                callBack?.onSuccess()
            }

            override fun onError(code: Int, error: String) {
                chatLogCallback?.onErrorInfo(tag, "asyncDestroyChatRoom onError $code $error")
                callBack?.onError(code, error)
            }
        })
    }


    fun isLoggedIn(): Boolean {
        return ChatClient.getInstance().isSdkInited && ChatClient.getInstance().isLoggedIn
    }

    override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
        messages?.forEach {
            if (it.type == ChatMessage.Type.TXT) {
                parseMsgChatEntity(it)
                try {
                    for (listener in chatEventHandlers) {
                        listener.onReceiveTextMsg(chatRoomId, parseChatMessage(it))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // 先判断是否自定义消息
            if (it.type == ChatMessage.Type.CUSTOM) {
                val body = it.body as CustomMessageBody
                val event = body.event()
                val msgType: AUICustomMsgType? = getCustomMsgType(event)

                // 再排除单聊
                if (it.chatType != ChatMessage.ChatType.Chat) {
                    val username: String = it.to
                    // 判断是否同一个聊天室或者群组 并且 event不为空
                    if (TextUtils.equals(username, chatRoomId) && !TextUtils.isEmpty(event)) {
                        when (msgType) {
                            AUICustomMsgType.AUIChatRoomJoinedMember -> {
                                parseMsgChatEntity(it)
                                try {
                                    for (listener in chatEventHandlers) {
                                        listener.onReceiveMemberJoinedMsg(chatRoomId, parseChatMessage(it))
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    /**
     *  解析消息 AgoraChatMessage（原消息）
     */
    private fun parseChatMessage(chatMessage: ChatMessage): AgoraChatMessage {
        var chatId: String? = ""
        var messageId: String? = ""
        var content: String? = ""
        var user: AUIChatUserInfo? =
            AUIChatUserInfo()

        val attr = chatMessage.getStringAttribute("user", "")
        if (!attr.isNullOrEmpty()) {
            GsonTools.toBean(attr, AgoraChatMessage::class.java)?.let { it ->
                user = it.user
            }
        }
        chatId = chatMessage.conversationId()
        messageId = chatMessage.msgId
        if (chatMessage.body is TextMessageBody) {
            content = (chatMessage.body as TextMessageBody).message
        }
        return AgoraChatMessage(chatId, messageId, content, user)
    }

    /**
     * 解析消息 AUIChatEntity （ui渲染消息）
     */
    fun parseMsgChatEntity(chatMessage: ChatMessage) {
        var content = ""
        var type = AUIChatInfoType.Common
        var customMsgType: AUICustomMsgType? = null
        val chatUser = AUIChatUserInfo()
        if (chatMessage.body is TextMessageBody) {
            content = (chatMessage.body as TextMessageBody).message
            val attr = chatMessage.getStringAttribute("user", "")
            if (!attr.isNullOrEmpty()) {
                val json = JSONObject(attr)
                chatUser.userId = json.get("userId") as String
                chatUser.userName = json.get("userName") as String
                chatUser.userAvatar = json.get("userAvatar") as String
            }
        } else if (chatMessage.body is CustomMessageBody) {
            type = AUIChatInfoType.Custom
            val params = (chatMessage.body as CustomMessageBody).params
            val event = (chatMessage.body as CustomMessageBody).event()
            customMsgType = AUICustomMsgType.fromName(event)
            val attr = params["user"]
            if (!attr.isNullOrEmpty()) {
                val json = JSONObject(attr)
                chatUser.userId = json.get("userId") as String
                chatUser.userName = json.get("userName") as String
                chatUser.userAvatar = json.get("userAvatar") as String
            }
        }
        currentMsgList.add(
            AUIChatEntity(
                type = type,
                chatUser = chatUser,
                content = content,
                customMsgType = customMsgType
            )
        )
    }

    fun getMsgList(): ArrayList<AUIChatEntity> {
        return currentMsgList
    }

    /**
     * 发送文本消息
     * @param content
     * @param callBack
     */
    fun sendTxtMsg(roomId: String?, content: String?, userInfo: AUIChatUserInfo?, callBack: AUIChatMsgCallback) {
        if (!isLoggedIn()) {
            return
        }
        val message = ChatMessage.createTextSendMessage(content, roomId)
        message?.let {
            it.setAttribute("user", GsonTools.beanToString(userInfo))
            it.chatType = ChatMessage.ChatType.ChatRoom
            it.setMessageStatusCallback(object : CallBack {
                override fun onSuccess() {
                    parseMsgChatEntity(it)
                    callBack.onResult(null, parseChatMessage(it))
                }

                override fun onError(code: Int, error: String) {
                    callBack.onResult(Exception("IM sendTxtMsg error code=$code, message=$error"), null)
                }

                override fun onProgress(i: Int, s: String) {

                }
            })
            ChatClient.getInstance().chatManager().sendMessage(it)
        }
    }

    fun sendJoinMsg(roomId: String?, userInfo: AUIChatUserInfo?, callBack: AUIChatMsgCallback) {
        if (!isLoggedIn()) {
            return
        }
        val messages = ChatMessage.createSendMessage(ChatMessage.Type.CUSTOM)
        messages.to = roomId
        val customBody = CustomMessageBody(AUICustomMsgType.AUIChatRoomJoinedMember.name)
        val ext = mutableMapOf<String, String>()
        ext["user"] = GsonTools.beanToString(userInfo).toString()
        customBody.params = ext
        messages.let {
            it.body = customBody
            it?.chatType = ChatMessage.ChatType.ChatRoom
            it?.setMessageStatusCallback(object : CallBack {
                override fun onSuccess() {
                    parseMsgChatEntity(it)
                    callBack.onOriginalResult(null, it)
                }

                override fun onError(code: Int, error: String) {
                    callBack.onOriginalResult(Exception("IM sendTxtMsg error code=$code, message=$error"), null)
                }

                override fun onProgress(i: Int, s: String) {

                }
            })
            ChatClient.getInstance().chatManager().sendMessage(it)
        }
    }

//    /**
//     * 插入欢迎消息
//     * @param content
//     */
//    fun saveWelcomeMsg(name: String, content: String) {
//        currentMsgList.clear()
//        val auiChatEntity = AUIChatEntity(
//            chatUser = AUIChatUserInfo("", name, ""),
//            content = content,
//            type = AUIChatInfoType.Common
//        )
//        currentMsgList.add(auiChatEntity)
//    }

    /**
     * 插入本地消息
     *
     * @param index
     * @param content
     */
    fun insertLocalMsg(index: Int, content: String) {
        val auiChatEntity = AUIChatEntity(
            type = AUIChatInfoType.Local,
            content = content,
        )
        if (index == 0) {
            val firstContent = currentMsgList.firstOrNull()?.content ?: ""
            // 防止重复插入
            if (firstContent.isNotEmpty() && firstContent == content) {
                return
            }
            currentMsgList.add(0, auiChatEntity)
        } else {
            if (index >= currentMsgList.size || index < 0) {
                currentMsgList.add(auiChatEntity)
            } else {
                currentMsgList.add(index, auiChatEntity)
            }
        }
    }

    /**
     * 获取自定义消息类型
     * @param event
     * @return
     */
    private fun getCustomMsgType(event: String?): AUICustomMsgType? {
        return if (TextUtils.isEmpty(event)) {
            null
        } else {
            AUICustomMsgType.fromName(event)
        }
    }

    /**
     * 获取成员非主动退出房间原因
     * @param reason
     * @return
     */
    private fun getKickReason(reason: Int): VoiceRoomServiceKickedReason? {
        return when (reason) {
            EMAChatRoomManagerListener.BE_KICKED -> VoiceRoomServiceKickedReason.removed
            EMAChatRoomManagerListener.DESTROYED -> VoiceRoomServiceKickedReason.destroyed
            EMAChatRoomManagerListener.BE_KICKED_FOR_OFFLINE -> VoiceRoomServiceKickedReason.offLined
            else -> null
        }
    }

    override fun onAnnouncementChanged(chatRoomId: String?, announcement: String?) {
    }

    override fun onRemovedFromChatRoom(
        reason: Int, chatRoomId: String?, roomName: String?, participant: String?
    ) {
        try {
            for (listener in chatEventHandlers) {
                listener.onUserBeKicked(chatRoomId, getKickReason(reason))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onChatRoomDestroyed(chatRoomId: String?, roomName: String?) {
        try {
            for (listener in chatEventHandlers) {
                listener.onRoomDestroyed(chatRoomId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMemberJoined(roomId: String?, participant: String?) {}

    override fun onMemberExited(roomId: String?, roomName: String?, participant: String?) {}

    override fun onMuteListAdded(
        chatRoomId: String?, mutes: MutableList<String>?, expireTime: Long
    ) {
    }

    override fun onMuteListRemoved(chatRoomId: String?, mutes: MutableList<String>?) {}

    override fun onWhiteListAdded(chatRoomId: String?, whitelist: MutableList<String>?) {}

    override fun onWhiteListRemoved(chatRoomId: String?, whitelist: MutableList<String>?) {}

    override fun onAllMemberMuteStateChanged(chatRoomId: String?, isMuted: Boolean) {}

    override fun onAdminAdded(chatRoomId: String?, admin: String?) {}

    override fun onAdminRemoved(chatRoomId: String?, admin: String?) {}

    override fun onOwnerChanged(chatRoomId: String?, newOwner: String?, oldOwner: String?) {}
}