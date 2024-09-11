package io.agora.scene.aichat.list.logic

import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.service.api.AIAgentResult

fun EaseProfile.toAIAgentModel(aiAgent: AIAgentResult): AIAgentModel {
    val num = aiAgent.index % 3
    val bgResName = "aichat_agent_bg_$num"
    val aiAgentModel = AIAgentModel(
        index = aiAgent.index,
        id = this.id,
        name = this.name ?: "",
        description = this.sign ?: "",
        avatar = this.avatar ?: "",
        prompt = this.prompt ?: "",
        background = bgResName,
    )
    return aiAgentModel
}

fun AIAgentResult.toAIAgentModel(): AIAgentModel {
    val num = this.index % 3
    val bgResName = "aichat_agent_bg_$num"
    val aiAgentModel = AIAgentModel(
        index = this.index,
        id = this.username,
        name = this.username,
        description = this.username,
        avatar = "",
        prompt = "",
        background = bgResName,
    )
    return aiAgentModel
}

data class AIAgentModel constructor(
    val index: Int = 0,
    val name: String,
    var prompt: String = "",
    val description: String,
    val avatar: String,
    val background: String,
    val id: String
) {
    val disPlayName: String get() = name.ifEmpty { id }
}

data class AIConversationModel constructor(
    val conversationId: String,
    val unreadMsgCount: Int,
    val name: String,
    val conversationDes: String,
    val avatar: String,
    val background: String,
    val pinnedTime: Long,
)