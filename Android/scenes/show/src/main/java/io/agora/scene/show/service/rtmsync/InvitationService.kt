package io.agora.scene.show.service.rtmsync

import androidx.annotation.IntDef
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper
import java.util.UUID

class InvitationService(
    private val channelName: String,
    private val syncManager: SyncManager,
    private val interactionService: InteractionService
) {

    private val key = "invitation"

    private val observerHelper = ObservableHelper<(InvitationInfo) -> Unit>()

    private val messageRetainer = MessageRetainer(syncManager.rtmManager, channelName, key).apply {
        subscribe { msg ->
            val invitationInfo = GsonTools.toBeanSafely(
                msg.content,
                InvitationInfo::class.java
            )
            if (invitationInfo != null) {
                if(invitationInfo.type != InvitationType.INVITING){
                    removeMessages{ it.publisherId == msg.publisherId }
                }
                observerHelper.notifyEventHandlers {
                    it.invoke(invitationInfo)
                }
            }
        }
    }

    fun sendInvitation(
        userId: String,
        success: ((InvitationInfo) -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            failure?.invoke(Throwable("scene is null"))
            return
        }

        val userInfo = scene.userService.getUserInfo(userId)
        if (userInfo == null) {
            failure?.invoke(Throwable("user info is null"))
            return
        }

        val invitationInfo = InvitationInfo(
            userId = userId,
            userName = userInfo.userName,
            type = InvitationType.INVITING
        )
        messageRetainer.sendMessage(
            GsonTools.beanToString(invitationInfo) ?: "",
            userId,
            success = {
                success?.invoke(invitationInfo)
            },
            failure
        )
    }

    fun acceptInvitation(
        invitationId: String,
        success: (() -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        val inviteMessage = messageRetainer.getMessage {
            GsonTools.toBeanSafely(
                it.content,
                InvitationInfo::class.java
            )?.id == invitationId
        }

        if (inviteMessage == null) {
            failure?.invoke(Throwable("invitation message is null"))
            return
        }

        var invitationInfo = GsonTools.toBeanSafely(
            inviteMessage.content,
            InvitationInfo::class.java
        )
        if (invitationInfo == null) {
            failure?.invoke(Throwable("invitation info is null"))
            return
        }

        invitationInfo = invitationInfo.copy(type = InvitationType.ACCEPT)
        messageRetainer.sendMessage(
            GsonTools.beanToString(invitationInfo) ?: "",
            inviteMessage.publisherId,
            success = {
                messageRetainer.removeMessages{ it.publisherId == inviteMessage.publisherId }
            }
        )
        interactionService.startLinkingInteraction(
            userId = invitationInfo.userId,
            success = {
                success?.invoke()
            },
            failure = failure
        )
    }

    fun rejectInvitation(
        invitationId: String,
        success: ((InvitationInfo) -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        val inviteMessage = messageRetainer.getMessage {
            GsonTools.toBeanSafely(
                it.content,
                InvitationInfo::class.java
            )?.id == invitationId
        }

        if (inviteMessage == null) {
            failure?.invoke(Throwable("invitation message is null"))
            return
        }

        var invitationInfo = GsonTools.toBeanSafely(
            inviteMessage.content,
            InvitationInfo::class.java
        )
        if (invitationInfo == null) {
            failure?.invoke(Throwable("invitation info is null"))
            return
        }

        invitationInfo = invitationInfo.copy(type = InvitationType.ACCEPT)
        messageRetainer.sendMessage(
            GsonTools.beanToString(invitationInfo) ?: "",
            invitationInfo.userId,
            success = {
                messageRetainer.removeMessages{ it.publisherId == inviteMessage.publisherId }
                success?.invoke(invitationInfo)
            },
            failure
        )
    }

    fun subscribe(onUpdate: (InvitationInfo) -> Unit) {
        observerHelper.subscribeEvent(onUpdate)
    }

    fun unSubscribe(onUpdate: (InvitationInfo) -> Unit) {
        observerHelper.unSubscribeEvent(onUpdate)
    }

    fun release() {
        observerHelper.unSubscribeAll()
        messageRetainer.release()
    }

}

@IntDef(InvitationType.INVITING, InvitationType.ACCEPT, InvitationType.REJECT)
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class InvitationType {
    companion object {
        const val INVITING = 0
        const val ACCEPT = 1
        const val REJECT = 2
    }
}

data class InvitationInfo(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    @InvitationType val type: Int,
)
