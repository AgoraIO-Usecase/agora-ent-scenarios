package io.agora.scene.aichat.list.logic.api

import io.agora.scene.aichat.list.logic.model.AIAgentModel
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
                    AIAgentModel("智能客服1", "", "智能客服11", "", "101"),
                    AIAgentModel("智能客服2", "", "智能客服22", "", "102"),
                    AIAgentModel("智能客服3", "", "智能客服33", "", "103"),
                    AIAgentModel("智能客服4", "", "智能客服44", "", "104")
                )
            }

            return@withContext response
        }

        // 模拟网络获取公开智能体
        suspend fun requestPrivateBot(): BaseResponse<List<AIAgentModel>> = withContext(Dispatchers.IO) {
            val response = BaseResponse<List<AIAgentModel>>().apply {
                code = 0
                data = mutableListOf(
                    AIAgentModel("智能客服1", "我创建的智能客服11", "", "", "101"),
                    AIAgentModel("智能客服2", "我创建的智能客服22", "", "", "102"),
                    AIAgentModel("智能客服3", "我创建的智能客服33", "", "", "103"),
                    AIAgentModel("智能客服4", "我创建的智能客服44", "", "", "104"),
                    AIAgentModel("智能客服5", "我创建的智能客服55", "", "", "105"),
                    AIAgentModel("智能客服6", "我创建的智能客服66", "", "", "106"),
                    AIAgentModel("智能客服7", "我创建的智能客服77", "", "", "107")
                )
            }

            return@withContext response
        }
    }

}