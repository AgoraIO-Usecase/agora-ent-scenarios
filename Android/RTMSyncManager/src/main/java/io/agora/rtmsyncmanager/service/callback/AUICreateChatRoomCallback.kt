package io.agora.rtmsyncmanager.service.callback

interface AUICreateChatRoomCallback {
    fun onResult(error: AUIException?, chatRoomId: String?)
}