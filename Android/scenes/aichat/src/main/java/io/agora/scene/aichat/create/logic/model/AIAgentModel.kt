package io.agora.scene.aichat.create.logic.model

data class AIAgentModel constructor(
    val name: String,
    val description: String,
    val avatar: String,
    val background: String,
    val id: String
) {

    val fullHeadUrl
        get() = if (this.avatar.startsWith("http")) {
            this.avatar
        } else {
            "file:///android_asset/" + this.avatar + ".png"
        }

    val fullBackgroundUrl
        get() = if (this.background.startsWith("http")) {
            this.background
        } else {
            "file:///android_asset/" + this.background + ".png"
        }
}