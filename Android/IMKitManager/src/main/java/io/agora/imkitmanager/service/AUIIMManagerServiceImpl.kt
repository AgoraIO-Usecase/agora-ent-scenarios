package io.agora.imkitmanager.service

import android.os.Handler
import android.os.Looper
import io.agora.CallBack
import io.agora.imkitmanager.service.http.ChatIMConfig
import io.agora.imkitmanager.service.http.ChatRoomConfig
import io.agora.imkitmanager.service.http.CreateChatRoomResponse
import io.agora.chat.ChatMessage
import io.agora.chat.adapter.EMAError
import io.agora.imkitmanager.AUIChatEventHandler
import io.agora.imkitmanager.AUIChatManager
import io.agora.imkitmanager.model.AUIChatRoomContext
import io.agora.imkitmanager.model.AUIChatRoomInfo
import io.agora.imkitmanager.model.AgoraChatMessage
import io.agora.imkitmanager.service.callback.AUIChatMsgCallback
import io.agora.imkitmanager.service.http.CHATROOM_CREATE_TYPE_ROOM
import io.agora.imkitmanager.service.http.CHATROOM_CREATE_TYPE_USER
import io.agora.imkitmanager.service.http.ChatCommonResp
import io.agora.imkitmanager.service.http.CreateChatRoomRequest
import io.agora.imkitmanager.service.http.ChatHttpManager
import io.agora.imkitmanager.service.http.ChatUserConfig
import io.agora.imkitmanager.service.http.CreateChatRoomInput
import io.agora.imkitmanager.ui.AUIChatInfo
import io.agora.imkitmanager.ui.IAUIChatListView
import io.agora.imkitmanager.utils.ObservableHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AUIIMManagerServiceImpl constructor(private val chatManager: AUIChatManager) :
    IAUIIMManagerService, AUIChatEventHandler {

    private val chatRoomContext = AUIChatRoomContext.shared()

    private val mainHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private fun runOnMainThread(runnable: Runnable) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            runnable.run()
        } else {
            mainHandler.post(runnable)
        }
    }

    init {
        ChatHttpManager.setBaseURL(chatRoomContext.requireCommonConfig().host)
    }

    private val observableHelper = ObservableHelper<IAUIIMManagerService.AUIIMManagerRespObserver>()

    private var mCurChatRoomId: String = ""

    private var mChatListView: IAUIChatListView? = null

    override fun registerRespObserver(observer: IAUIIMManagerService.AUIIMManagerRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIIMManagerService.AUIIMManagerRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun setChatListView(view: IAUIChatListView) {
        mChatListView = view
    }

    override fun sendMessage(
        text: String, completion: (IAUIIMManagerService.AgoraChatTextMessage?, Exception?) -> Unit, localMsg: Boolean
    ) {
        innerLoginChat { loginError ->
            if (loginError != null) {
                completion.invoke(null, Exception("sendChatMessage ==> ${loginError.message}"))
                return@innerLoginChat
            }
            if (localMsg) {
                chatManager.insertLocalMsg(text)
                runOnMainThread {
                    mChatListView?.let { chatListView ->
                        chatListView.refreshSelectLast(chatManager.getMsgList().map {
                            AUIChatInfo(
                                userId = it.chatUser?.userId ?: "",
                                userName = it.chatUser?.userName ?: "",
                                content = it.content,
                                joined = it.joined,
                                localMsg = it.localMsg
                            )
                        })
                    }
                    val chatTextMessage = IAUIIMManagerService.AgoraChatTextMessage(
                        "", text, null
                    )
                    completion.invoke(chatTextMessage, null)
                }
                return@innerLoginChat
            }
            chatManager.sendTxtMsg(
                mCurChatRoomId,
                text,
                chatRoomContext.currentUserInfo,
                object : AUIChatMsgCallback {
                    override fun onResult(error: Exception?, message: AgoraChatMessage?) {
                        super.onResult(error, message)
                        runOnMainThread {
                            if (error != null) {
                                completion.invoke(null, error)
                                return@runOnMainThread
                            }
                            val chatTextMessage = IAUIIMManagerService.AgoraChatTextMessage(
                                message?.messageId,
                                message?.content, chatRoomContext.currentUserInfo
                            )
                            completion.invoke(chatTextMessage, null)

                            mChatListView?.let { chatListView ->
                                chatListView.refreshSelectLast(chatManager.getMsgList().map {
                                    AUIChatInfo(
                                        userId = it.chatUser?.userId ?: "",
                                        userName = it.chatUser?.userName ?: "",
                                        content = it.content,
                                        joined = it.joined,
                                        localMsg = it.localMsg
                                    )
                                })
                            }
                        }
                    }
                })
        }
    }

    /**
     * 登录环信
     *
     * @param completion
     * @receiver
     */
    override fun loginChat(completion: (error: Exception?) -> Unit) {
        innerLoginChat { loginError ->
            runOnMainThread {
                if (loginError != null) {
                    completion.invoke(Exception("loginChat ==> ${loginError.message}"))
                } else {
                    completion.invoke(null)
                }
            }
        }
    }

    override fun logoutChat(completion: (error: Exception?) -> Unit) {
        chatManager.logoutChat(null)
        AUIChatRoomContext.shared().clearChatToken()
    }

    /**
     * Inner login chat
     *
     * @param completion
     * @receiver
     */
    private fun innerLoginChat(completion: (error: Exception?) -> Unit) {
        if (chatManager.isLoggedIn()) {
            completion.invoke(null)
            return
        }
        val createChatRoomInput = CreateChatRoomInput(type = CHATROOM_CREATE_TYPE_USER)
        innerCreateUserOrChaRoom(createChatRoomInput) { resp, error ->
            if (error == null && resp != null) { // success
                val chatUserId = chatRoomContext.currentUserInfo.userId
                val chatUserToken = AUIChatRoomContext.shared().mChatToken
                chatManager.loginChat(chatUserId, chatUserToken, object : CallBack {
                    override fun onSuccess() {
                        completion.invoke(null)
                    }

                    override fun onError(code: Int, error: String?) {
                        if (code == EMAError.USER_ALREADY_LOGIN) {
                            completion.invoke(null)
                        } else {
                            completion.invoke(Exception("code=$code, message=$error"))
                        }
                    }
                })
            } else {
                completion.invoke(error)
            }
        }
    }

    /**
     * 创建环信聊天室并登录
     *
     * @param roomName
     * @param description
     * @param completion
     * @receiver
     */
    override fun createChatRoom(
        roomName: String, description: String, completion: (chatId: String?, error: Exception?) -> Unit
    ) {
        // check login
        innerLoginChat { loginError ->
            if (loginError != null) {
                runOnMainThread {
                    completion.invoke(null, Exception("createChatRoom ==> ${loginError.message}"))
                }
                return@innerLoginChat
            }
            val chatRoomOwner = AUIChatRoomContext.shared().currentUserInfo.userId
            val createChatRoomInput = CreateChatRoomInput(
                chatRoomName = roomName,
                chatDescription = description,
                chatRoomOwner = chatRoomOwner,
                type = CHATROOM_CREATE_TYPE_ROOM
            )
            // create chat room
            innerCreateUserOrChaRoom(createChatRoomInput) { resp, error ->
                if (error == null && resp != null) { // success
                    runOnMainThread {
                        val chatId = resp.chatId ?: ""
                        if (chatId.isEmpty()) {
                            completion.invoke(null, Exception("createChatRoom >> but resp.chatId null."))
                        } else {
                            AUIChatRoomContext.shared().insertRoomInfo(
                                AUIChatRoomInfo(chatRoomOwner, chatId)
                            )
                            completion.invoke(chatId, null)
                        }
                    }
                } else {
                    runOnMainThread {
                        completion.invoke(null, error)
                    }
                }
            }
        }
    }

    /**
     * 加入环信聊天室
     *
     * @param chatRoomInfo
     * @param completion
     * @receiver
     */
    override fun joinChatRoom(chatRoomInfo: AUIChatRoomInfo, completion: (error: Exception?) -> Unit) {
        mChatListView?.setOwnerId(chatRoomInfo.ownerUserId)
        // check login
        innerLoginChat { loginError ->
            if (loginError != null) {
                runOnMainThread {
                    completion.invoke(Exception("joinChatRoom >> ${loginError.message}"))
                }
                return@innerLoginChat
            }
            // join chat room
            val chatId = chatRoomInfo.chatRoomId
            chatManager.subscribeChatMsg(this)
            chatManager.joinChatRoom(chatId, object : AUIChatMsgCallback {
                override fun onOriginalResult(error: Exception?, message: ChatMessage?) {
                    super.onOriginalResult(error, message)
                    if (error != null) {
                        runOnMainThread {
                            completion.invoke(Exception("joinChatRoom >> IM join chat room failed! -- $error"))
                        }
                        return
                    }
                    mCurChatRoomId = chatId

                    val textMsg = IAUIIMManagerService.AgoraChatTextMessage(
                        message?.msgId, message?.body?.toString(), null
                    )
                    runOnMainThread {
                        completion.invoke(null)
                    }

                    mChatListView?.let { chatListView ->
                        runOnMainThread {
                            chatListView.refreshSelectLast(chatManager.getMsgList().map {
                                AUIChatInfo(
                                    userId = it.chatUser?.userId ?: "",
                                    userName = it.chatUser?.userName ?: "",
                                    content = it.content,
                                    joined = it.joined,
                                    localMsg = it.localMsg
                                )
                            })
                        }
                    }

                    observableHelper.notifyEventHandlers {
                        it.onUserDidJoinRoom(chatId, textMsg)
                    }
                }
            })
        }
    }

    // 创建IM 用户 或者创建房间
    private fun innerCreateUserOrChaRoom(
        input: CreateChatRoomInput, completion: (CreateChatRoomResponse?, Exception?) -> Unit
    ) {
        val action = when (input.type) {
            CHATROOM_CREATE_TYPE_USER -> "createUser"
            CHATROOM_CREATE_TYPE_ROOM -> "createRoom"
            else -> "createUserAndRoom"
        }
        val commonConfig = chatRoomContext.mCommonConfig ?: run {
            completion.invoke(null, Exception("inner $action >> commonConfig null."))
            return
        }
        val request = CreateChatRoomRequest(
            appId = commonConfig.appId,
            type = input.type,
            imConfig = ChatIMConfig().apply {
                this.appKey = commonConfig.imAppKey
                this.clientId = commonConfig.imClientId
                this.clientSecret = commonConfig.imClientSecret
            },
        )
        val chatUserConfig = ChatUserConfig().apply {
            this.username = chatRoomContext.currentUserInfo.userId
            this.nickname = chatRoomContext.currentUserInfo.userName
        }
        val chatRoomConfig = ChatRoomConfig().apply {
            if (!input.chatRoomId.isNullOrEmpty()) {
                this.chatRoomId = input.chatRoomId
            } else {
                this.chatRoomName = input.chatRoomName
                this.description = input.chatDescription
                this.roomOwner = input.chatRoomOwner
            }
        }
        when (input.type) {
            CHATROOM_CREATE_TYPE_USER -> request.chatUserConfig = chatUserConfig
            CHATROOM_CREATE_TYPE_ROOM -> request.chatRoomConfig = chatRoomConfig
            else -> {
                request.chatUserConfig = chatUserConfig
                request.chatRoomConfig = chatRoomConfig
            }
        }
        ChatHttpManager.chatInterface.createChatRoom(request)
            .enqueue(object : Callback<ChatCommonResp<CreateChatRoomResponse>> {
                override fun onResponse(
                    call: Call<ChatCommonResp<CreateChatRoomResponse>>,
                    response: Response<ChatCommonResp<CreateChatRoomResponse>>
                ) {
                    val resp = response.body()?.data
                    if (resp == null) {
                        completion.invoke(null, Exception("$action >> onResponse resp null."))
                        return
                    }
                    runOnMainThread {
                        AUIChatRoomContext.shared().setupChatToken(resp.chatToken ?: "")
                    }
                    completion.invoke(resp, null)
                }

                override fun onFailure(call: Call<ChatCommonResp<CreateChatRoomResponse>>, t: Throwable) {
                    completion.invoke(null, Exception("$action >> onFailure ${t.message}"))
                }
            })
    }

    override fun leaveChatRoom(completion: (error: Exception?) -> Unit) {
        chatManager.leaveChatRoom()
        if (AUIChatRoomContext.shared().isRoomOwner(mCurChatRoomId)) {
            chatManager.asyncDestroyChatRoom(null)
        }
        AUIChatRoomContext.shared().cleanRoom(mCurChatRoomId)
        chatManager.unsubscribeChatMsg(this)
        chatManager.clear()
        mCurChatRoomId = ""
        mChatListView = null
        completion.invoke(null)
    }

    override fun onReceiveMemberJoinedMsg(chatRoomId: String?, message: AgoraChatMessage?) {
        super.onReceiveMemberJoinedMsg(chatRoomId, message)
        message ?: return
        chatRoomId ?: return
        if (this.mCurChatRoomId != chatRoomId) return

        mChatListView?.let { chatListView ->
            runOnMainThread {
                chatListView.refreshSelectLast(chatManager.getMsgList().map {
                    AUIChatInfo(
                        userId = it.chatUser?.userId ?: "",
                        userName = it.chatUser?.userName ?: "",
                        content = it.content,
                        joined = it.joined,
                        localMsg = it.localMsg
                    )
                })
            }
        }

        observableHelper.notifyEventHandlers {
            it.onUserDidJoinRoom(
                chatRoomId, IAUIIMManagerService.AgoraChatTextMessage(
                    message.messageId,
                    message.content,
                    message.user
                )
            )
        }
    }

    override fun onReceiveTextMsg(chatRoomId: String?, message: AgoraChatMessage?) {
        super.onReceiveTextMsg(chatRoomId, message)
        message ?: return
        chatRoomId ?: return
        if (this.mCurChatRoomId != chatRoomId) return

        mChatListView?.let { chatListView ->
            runOnMainThread {
                chatListView.refreshSelectLast(chatManager.getMsgList().map {
                    AUIChatInfo(
                        userId = it.chatUser?.userId ?: "",
                        userName = it.chatUser?.userName ?: "",
                        content = it.content,
                        joined = it.joined,
                        localMsg = it.localMsg
                    )
                })
            }
        }

        observableHelper.notifyEventHandlers {
            it.messageDidReceive(
                chatRoomId,
                IAUIIMManagerService.AgoraChatTextMessage(
                    message.messageId,
                    message.content,
                    message.user
                )
            )
        }
    }
}