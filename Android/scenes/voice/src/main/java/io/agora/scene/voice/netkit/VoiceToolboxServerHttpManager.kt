package io.agora.scene.voice.netkit

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.scene.base.AgoraTokenType
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.TokenGeneratorType
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.global.VoiceCenter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author create by zhangwei03
 */
object VoiceToolboxServerHttpManager {

    private val TAG = "VoiceToolboxServerHttpManager"

    private fun context(): Context {
        return AgoraApplication.the()
    }

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

    private val voiceApiInterface by lazy {
        VoiceHttpManager.getService(VoiceApiInterface::class.java)
    }

    /**
     * Generate RTC/RTM/Chat etc. Token007
     *
     * @param callback
     * @receiver
     */
    fun generateAllToken(callback: (token: String?, exception: Exception?) -> Unit) {
        TokenGenerator.generateToken(
            channelName = "", // Universal token
            uid = VoiceCenter.rtcUid.toString(),
            genType = TokenGeneratorType.Token007,
            tokenType = AgoraTokenType.Rtc,
            success = { token ->
                VoiceLogger.d(TAG, "generate tokens success")

                VoiceCenter.rtcToken = token
                callback.invoke(token, null)
            },
            failure = {
                VoiceLogger.e(TAG, "generate tokens failed,$it")
                callback.invoke(null, it)
            })
    }

    // Create IM user or create room
    fun createImRoom(
        roomName: String,
        roomOwner: String,
        chatroomId: String = "",
        type: Int = CHATROOM_CREATE_TYPE_USER_ROOM,
        completion: (CreateChatRoomResponse?, Exception?) -> Unit
    ) {
        val action = when (type) {
            CHATROOM_CREATE_TYPE_USER -> "createUser"
            CHATROOM_CREATE_TYPE_ROOM -> "createRoom"
            else -> "createUserAndRoom"
        }

        val request = CreateChatRoomRequest(
            appId = VoiceCenter.rtcAppId,
            type = type,
            imConfig = ChatIMConfig().apply {
                this.appKey = VoiceCenter.chatAppKey
                this.clientId = VoiceCenter.chatClientId
                this.clientSecret = VoiceCenter.chatClientSecret
            },
        )
        val chatUserConfig = ChatUserConfig().apply {
            this.username =  VoiceCenter.chatUid
            this.nickname = VoiceCenter.nickname
        }
        val chatRoomConfig = ChatRoomConfig().apply {
            if (chatroomId.isNotEmpty()) {
                this.chatRoomId = chatroomId
            } else {
                this.chatRoomName = roomName
                this.description = "Welcome!"
                this.roomOwner = roomOwner
            }
        }
        when (type) {
            CHATROOM_CREATE_TYPE_USER -> request.chatUserConfig = chatUserConfig
            CHATROOM_CREATE_TYPE_ROOM -> request.chatRoomConfig = chatRoomConfig
            else -> {
                request.chatUserConfig = chatUserConfig
                request.chatRoomConfig = chatRoomConfig
            }
        }
        voiceApiInterface.createChatRoom(request)
            .enqueue(object : Callback<ChatCommonResp<CreateChatRoomResponse>> {
                override fun onResponse(
                    call: Call<ChatCommonResp<CreateChatRoomResponse>>,
                    response: Response<ChatCommonResp<CreateChatRoomResponse>>
                ) {
                    val resp = response.body()?.data
                    if (resp == null) {
                        runOnMainThread{
                            completion.invoke(null, Exception("$action >> onResponse resp null."))
                        }
                        return
                    }
                    runOnMainThread{
                        completion.invoke(resp, null)
                    }
                }

                override fun onFailure(call: Call<ChatCommonResp<CreateChatRoomResponse>>, t: Throwable) {
                    runOnMainThread{
                        completion.invoke(null, Exception("$action >> onFailure ${t.message}"))
                    }
                }
            })
    }
}