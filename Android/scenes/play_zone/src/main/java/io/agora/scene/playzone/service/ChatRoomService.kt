package io.agora.scene.playzone.service

import android.content.Context
import io.agora.imkitmanager.AUIChatManager
import io.agora.imkitmanager.model.AUIChatCommonConfig
import io.agora.imkitmanager.model.AUIChatUserInfo
import io.agora.imkitmanager.service.AUIIMManagerServiceImpl
import io.agora.imkitmanager.service.IAUIIMManagerService
import io.agora.imkitmanager.service.http.ChatHttpManager
import io.agora.imkitmanager.utils.AUIChatLogger
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import io.agora.scene.playzone.BuildConfig
import io.agora.scene.playzone.PlayCenter
import io.agora.scene.playzone.PlayLogger

class PlayChatRoomService constructor(val cxt:Context){

    companion object {
        private const val TAG = "Chat_Service_LOG"

        private var innerChatRoomService: PlayChatRoomService? = null

        val chatRoomService: PlayChatRoomService
            get() {
                if (innerChatRoomService == null) {
                    innerChatRoomService = PlayChatRoomService(AgoraApplication.the())
                }
                return innerChatRoomService!!
            }

        fun reset() {
            innerChatRoomService = null
        }
    }


    val chatManager: AUIChatManager
    val imManagerService: IAUIIMManagerService

    init {
        val cxt = AgoraApplication.the().applicationContext
        // chat ImManager
        ChatHttpManager.setBaseURL(ServerConfig.toolBoxUrl)
        AUIChatLogger.initLogger(
            AUIChatLogger.Config(
                cxt, "Play_IM",
                logCallback = object : AUIChatLogger.AUILogCallback {
                    override fun onLogDebug(tag: String, message: String) {
                        PlayLogger.d(TAG, "$tag $message")
                    }

                    override fun onLogInfo(tag: String, message: String) {
                        PlayLogger.d(TAG, "$tag $message")
                    }

                    override fun onLogWarning(tag: String, message: String) {
                        PlayLogger.w(TAG, "$tag $message")
                    }

                    override fun onLogError(tag: String, message: String) {
                        PlayLogger.e(TAG, "$tag $message")
                    }
                })
        )

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
        )
        chatManager = AUIChatManager(chatRoomConfig)
        imManagerService = AUIIMManagerServiceImpl(chatManager)
    }
}