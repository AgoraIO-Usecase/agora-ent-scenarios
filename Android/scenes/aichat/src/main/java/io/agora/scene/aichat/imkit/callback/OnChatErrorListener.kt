package io.agora.scene.aichat.imkit.callback

interface OnChatErrorListener {
    /**
     * Wrong message in chat
     * @param code
     * @param errorMsg
     */
    fun onChatError(code: Int, errorMsg: String?)
}