package io.agora.imkitmanager.service

import io.agora.CallBack
import io.agora.imkitmanager.service.http.ChatIMConfig
import io.agora.imkitmanager.service.http.ChatRoomConfig
import io.agora.imkitmanager.service.http.CreateChatRoomResponse
import io.agora.chat.ChatMessage
import io.agora.chat.adapter.EMAError
import io.agora.imkitmanager.AUIChatEventHandler
import io.agora.imkitmanager.AUIChatManager
import io.agora.imkitmanager.model.AUIChatRoomContext
import io.agora.imkitmanager.model.AgoraChatMessage
import io.agora.imkitmanager.service.callback.AUIChatMsgCallback
import io.agora.imkitmanager.service.http.CHATROOM_CREATE_TYPE_ROOM
import io.agora.imkitmanager.service.http.CHATROOM_CREATE_TYPE_USER
import io.agora.imkitmanager.service.http.ChatCommonResp
import io.agora.imkitmanager.service.http.CreateChatRoomRequest
import io.agora.imkitmanager.service.http.ChatHttpManager
import io.agora.imkitmanager.service.http.ChatUserConfig
import io.agora.imkitmanager.service.http.CreateChatRoomInput
import io.agora.imkitmanager.utils.AUIChatLogger
import io.agora.imkitmanager.utils.ObservableHelper
import io.agora.imkitmanager.utils.ThreadManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AUIIMManagerServiceImpl constructor(private val chatManager: AUIChatManager) :
    IAUIIMManagerService, AUIChatEventHandler {

    private val tag = "AUIIMManagerServiceImpl"

    private val chatRoomContext = AUIChatRoomContext.shared()

    private val observableHelper = ObservableHelper<IAUIIMManagerService.AUIIMManagerRespObserver>()

    private var mCurChatRoomId: String = ""


    override fun registerRespObserver(observer: IAUIIMManagerService.AUIIMManagerRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIIMManagerService.AUIIMManagerRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun sendMessage(
        text: String, completion: (IAUIIMManagerService.AgoraChatTextMessage?, Exception?) -> Unit
    ) {
        chatManager.sendTxtMsg(
            mCurChatRoomId,
            text,
            chatRoomContext.currentUserInfo,
            object : AUIChatMsgCallback {
                override fun onResult(error: Exception?, message: AgoraChatMessage?) {
                    super.onResult(error, message)
                    if (error != null) {
                        completion.invoke(null, error)
                        return
                    }

                    completion.invoke(
                        IAUIIMManagerService.AgoraChatTextMessage(
                            message?.messageId,
                            message?.content, chatRoomContext.currentUserInfo
                        ), null
                    )
                }
            })
    }

    override fun loginChat(completion: (error: Exception?) -> Unit) {
        val createChatRoomInput = CreateChatRoomInput(type = CHATROOM_CREATE_TYPE_USER)
        innerCreateChatRoom(createChatRoomInput) { resp, error ->
            if (error == null) { // success
                innerLogin {
                    completion.invoke(it)
                }
            } else {
                completion.invoke(error)
            }
        }
    }

    // 房主创建并加入房间
    override fun createChatRoom(
        roomName: String,
        description: String,
        completion: (CreateChatRoomResponse?, Exception?) -> Unit
    ) {
        if (!chatManager.isLoggedIn()) {
            completion.invoke(null, Exception("IM createChatRoom >> not login."))
            return
        }
        val createChatRoomInput = CreateChatRoomInput(
            chatRoomName = roomName,
            chatDescription = description,
            chatRoomOwner = AUIChatRoomContext.shared().currentUserInfo.userId,
            type = CHATROOM_CREATE_TYPE_ROOM
        )
        innerCreateChatRoom(createChatRoomInput) { resp, error ->
            if (error == null) { // success
                val chatId = resp?.chatId ?: ""
                if (chatId.isNotEmpty()) {
                    innerJoinChatRoom(chatId, callback = { error ->
                        if (error == null) {
                            completion.invoke(resp, null)
                        } else {
                            completion.invoke(null, error)
                        }
                    })
                } else {
                    completion.invoke(null, Exception("IM createChatRoom >> chatId null."))
                }
            } else {
                completion.invoke(null, error)
            }
        }
    }

    override fun joinChatRoom(chatRoomId: String, completion: (error: Exception?) -> Unit) {
        if (!chatManager.isLoggedIn()) {
            completion.invoke(Exception("IM joinChatRoom >> not login."))
            return
        }
        innerJoinChatRoom(chatRoomId, callback = { error ->
            completion.invoke(error)
        })
    }

    private fun innerCreateChatRoom(
        input: CreateChatRoomInput,
        completion: (CreateChatRoomResponse?, Exception?) -> Unit
    ) {
        val commonConfig = chatRoomContext.mCommonConfig ?: run {
            completion.invoke(null, Exception("IM " + "createChatRoom >> commonConfig null."))
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
                        completion.invoke(null, Exception("IM createChatRoom >> onResponse resp null."))
                        return
                    }
                    AUIChatRoomContext.shared().setupImToken(resp.chatToken ?: "")
                    ThreadManager.getInstance().runOnMainThread {
                        completion.invoke(resp, null)
                    }
                }

                override fun onFailure(call: Call<ChatCommonResp<CreateChatRoomResponse>>, t: Throwable) {
                    ThreadManager.getInstance().runOnMainThread {
                        completion.invoke(null, Exception("IM createChatRoom >> onFailure ${t.message}"))
                    }
                }
            })
    }

    private fun innerJoinChatRoom(chatId: String, callback: (error: Exception?) -> Unit) {
        chatManager.initManager()
        chatManager.subscribeChatMsg(this)
        chatManager.joinRoom(chatId, object : AUIChatMsgCallback {
            override fun onOriginalResult(
                error: Exception?,
                message: ChatMessage?
            ) {
                super.onOriginalResult(error, message)
                if (error != null) {
                    callback.invoke(Exception("joinChatRoom >> IM join chat room failed! -- $error"))
                    return
                }
                mCurChatRoomId = chatId

                val textMsg = IAUIIMManagerService.AgoraChatTextMessage(
                    message?.msgId, message?.body?.toString(), null
                )
                observableHelper.notifyEventHandlers {
                    it.onUserDidJoinRoom(chatId, textMsg)
                }
            }
        })
    }

    override fun userQuitRoom(completion: (error: Exception?) -> Unit) {
        chatManager.leaveChatRoom()
        chatManager.logoutChat()
        chatManager.unsubscribeChatMsg(this)
        chatManager.clear()
        AUIChatRoomContext.shared().cleanRoom(mCurChatRoomId)
        mCurChatRoomId = ""

        completion.invoke(null)
    }

    override fun userDestroyedChatroom() {
        chatManager.asyncDestroyChatRoom(object : CallBack {
            override fun onSuccess() {
                AUIChatLogger.logger().d(tag, message = "userDestroyedChatroom success")
            }

            override fun onError(code: Int, error: String?) {
                AUIChatLogger.logger().e(tag, message = "userDestroyedChatroom error -- code=$code, message=$error")
            }
        })
        chatManager.logoutChat()
        chatManager.unsubscribeChatMsg(this)
        chatManager.clear()
        AUIChatRoomContext.shared().cleanRoom(mCurChatRoomId)
        mCurChatRoomId = ""
    }

    private fun innerLogin(completion: (Exception?) -> Unit) {
        val chatUserId = chatRoomContext.currentUserInfo.userId
        val chatUserToken = AUIChatRoomContext.shared().mChatToken
        if (chatUserId.isEmpty() || chatUserToken.isEmpty()) {
            AUIChatLogger.logger().d(tag, message = "login >> parameters are empty. chatUserId=$chatUserId")
            return
        }
        chatManager.loginChat(chatUserId, chatUserToken, object : CallBack {
            override fun onSuccess() {
                ThreadManager.getInstance().runOnMainThread {
                    completion.invoke(null)
                }
            }

            override fun onError(code: Int, error: String?) {
                ThreadManager.getInstance().runOnMainThread {
                    if (code == EMAError.USER_ALREADY_LOGIN) {
                        completion.invoke(null)
                    } else {
                        completion.invoke(Exception("loginIM error code=$code, message=$error"))
                    }
                }
            }
        })
    }

    override fun onReceiveMemberJoinedMsg(chatRoomId: String?, message: AgoraChatMessage?) {
        super.onReceiveMemberJoinedMsg(chatRoomId, message)
        message ?: return
        chatRoomId ?: return
        if (this.mCurChatRoomId != chatRoomId) return
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