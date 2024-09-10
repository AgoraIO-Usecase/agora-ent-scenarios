package io.agora.scene.aichat.list.logic.model

import io.agora.scene.aichat.imkit.model.EaseUser
import io.agora.scene.aichat.imkit.model.getNickname
import io.agora.scene.aichat.service.api.AIAgentResult
import org.json.JSONObject

fun EaseUser.toAIAgentModel(aiAgent: AIAgentResult): AIAgentModel {
    val num = aiAgent.index % 3
    val bgResName = "aichat_agent_bg_$num"
    val aiAgentModel = AIAgentModel(
        index = aiAgent.index,
        id = this.userId,
        name = this.getNickname() ?: "",
        description = this.sign ?: "",
        avatar = this.avatar ?: "",
        background = bgResName,
    )
    try {
        val js = JSONObject(this.ext)
        aiAgentModel.prompt = js.optString("prompt")
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
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