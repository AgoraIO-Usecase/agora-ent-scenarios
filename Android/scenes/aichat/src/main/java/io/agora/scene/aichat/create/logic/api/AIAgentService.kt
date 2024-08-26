package io.agora.scene.aichat.create.logic.api

import io.agora.scene.aichat.create.logic.model.AIAgentModel
import io.agora.scene.base.api.base.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val aiAgentService: AIAgentService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    AIAgentManager.getApi(AIAgentService::class.java)
}

interface AIAgentService {

    companion object {

        // 模拟网络获取公开智能体
        suspend fun requestPublicBot(): BaseResponse<List<AIAgentModel>> = withContext(Dispatchers.IO) {
            val response = BaseResponse<List<AIAgentModel>>().apply {
                code = 0
                data = mutableListOf(
                    AIAgentModel("智能客服1", "", "智能客服11", "","101"),
                    AIAgentModel("智能客服2", "", "智能客服22", "","102"),
                    AIAgentModel("智能客服3", "", "智能客服33", "","103"),
                    AIAgentModel("智能客服4", "", "智能客服44", "","104")
                )
            }

            return@withContext response
        }
    }

}