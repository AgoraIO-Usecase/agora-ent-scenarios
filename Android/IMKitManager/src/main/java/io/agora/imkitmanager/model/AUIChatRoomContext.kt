package io.agora.imkitmanager.model

import java.io.Serializable

// 环信用户
data class AUIChatUserInfo constructor(
    var userId: String = "",
    var userName: String = "",
    var userAvatar: String = "",
) : Serializable

// 环信聊天室
data class AUIChatRoomInfo(
    var ownerUserId: String = "", //环信聊天室的管理员。
    var chatRoomId: String = "" //环信聊天室ID
) : Serializable

class AUIChatRoomContext private constructor() {

    companion object {
        @Volatile
        private var instance: AUIChatRoomContext? = null

        fun shared(): AUIChatRoomContext {
            if (instance == null) {
                synchronized(AUIChatRoomContext::class.java) {
                    if (instance == null) {
                        instance = AUIChatRoomContext()
                    }
                }
            }
            return instance!!
        }
    }

    var mChatToken: String = ""
        private set(value) {
            field = value
        }

    fun setupChatToken(chatToken: String) {
        if (chatToken.isNotEmpty()) {
            mChatToken = chatToken
        }
    }

    fun clearChatToken(){
        mChatToken = ""
    }

    var mCommonConfig: AUIChatCommonConfig? = null

    var currentUserInfo: AUIChatUserInfo = AUIChatUserInfo()

    // key chatRoomId，value chatRoomInfo
    private val chatRoomInfoMap = mutableMapOf<String, AUIChatRoomInfo>()

    fun setCommonConfig(config: AUIChatCommonConfig) {
        mCommonConfig = config
        currentUserInfo = config.owner
    }

    fun requireCommonConfig(): AUIChatCommonConfig {
        if (mCommonConfig == null) {
            throw RuntimeException("mCommonConfig is null now!")
        }
        return mCommonConfig!!
    }

    fun isRoomOwner(roomId: String): Boolean {
        return isRoomOwner(roomId, currentUserInfo.userId)
    }

    fun isRoomOwner(chatRoomId: String, userId: String?): Boolean {
        val roomOwnerId = chatRoomInfoMap[chatRoomId]?.ownerUserId
        if (roomOwnerId == null || userId == null) {
            return false
        }
        return roomOwnerId == userId
    }

    fun insertRoomInfo(info: AUIChatRoomInfo) {
        if (info.chatRoomId.isNotEmpty() && info.ownerUserId.isNotEmpty()) {
            chatRoomInfoMap[info.chatRoomId] = info
        }
    }

    fun cleanRoom(roomId: String) {
        chatRoomInfoMap.remove(roomId)
    }
}
