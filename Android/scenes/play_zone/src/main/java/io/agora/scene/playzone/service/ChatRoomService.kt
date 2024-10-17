package io.agora.scene.playzone.service

import io.agora.imkitmanager.model.AUIChatCommonConfig
import io.agora.imkitmanager.model.AUIChatUserInfo
import io.agora.imkitmanager.model.ChatLogCallback
import io.agora.imkitmanager.service.AUIIMManagerServiceImpl
import io.agora.imkitmanager.service.IAUIIMManagerService
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import io.agora.scene.playzone.BuildConfig
import io.agora.scene.playzone.PlayCenter
import io.agora.scene.playzone.PlayLogger

class PlayChatRoomService {

    companion object {
        private const val TAG = "Chat_Service_LOG"

        private var innerChatRoomService: PlayChatRoomService? = null

        val chatRoomService: PlayChatRoomService
            get() {
                if (innerChatRoomService == null) {
                    innerChatRoomService = PlayChatRoomService()
                }
                return innerChatRoomService!!
            }

        fun reset() {
            innerChatRoomService = null
        }
    }


    val imManagerService: IAUIIMManagerService

    init {
        val cxt = AgoraApplication.the().applicationContext
        val chatRoomConfig = AUIChatCommonConfig(
            context = cxt,
            appId = PlayCenter.mAppId,
            owner = AUIChatUserInfo(
                userId = UserManager.getInstance().user.id.toString(),
                userName = UserManager.getInstance().user.name,
                userAvatar = UserManager.getInstance().user.headUrl,
            ),
            host = ServerConfig.toolBoxUrl,
            imAppKey = BuildConfig.IM_APP_KEY,
            chatLogCallback = object : ChatLogCallback {
                override fun onDebugInfo(tag: String, message: String) {
                    PlayLogger.d(TAG, "$tag $message")
                }

                override fun onErrorInfo(tag: String, message: String) {
                    PlayLogger.e(TAG, "$tag $message")
                }
            }
        )
        imManagerService = AUIIMManagerServiceImpl(chatRoomConfig)
    }
}