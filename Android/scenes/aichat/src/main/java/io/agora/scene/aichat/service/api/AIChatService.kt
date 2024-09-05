package io.agora.scene.aichat.service.api

import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.list.logic.model.AIAgentModel
import io.agora.scene.aichat.service.AIAgentManager
import io.agora.scene.base.api.base.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

val aiChatService: AIChatService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    AIAgentManager.getApi(AIChatService::class.java)
}

interface AIChatService {

    @POST("{appId}/chat/token")
    suspend fun generateChatToken(
        @Path("appId") appId: String = AIChatCenter.mAppId,
        @Body req: AICreateTokenReq
    ): AIBaseResponse<AITokenResult>

    @POST("{appId}/chat/users")
    suspend fun createChatUser(
        @Path("appId") appId: String = AIChatCenter.mAppId,
        @Body req: AICreateUserReq
    ): AIBaseResponse<AIUserResult>

    @POST("{appId}/chat/users/{ownerUsername}/contacts/users/{friendUsername}")
    suspend fun addChatUser(
        @Path("appId") appId: String = AIChatCenter.mAppId,
        @Path("ownerUsername") ownerUsername: String,
        @Path("friendUsername") friendUsername: String,
    ): AIBaseResponse<Any>

    @FormUrlEncoded
    @PUT("{appId}/chat/metadata/user/{username}")
    suspend fun updateMetadata(
        @Path("appId") appId: String,
        @Path("username") username: String,
        @FieldMap fields: Map<String, String>,
    ): AIBaseResponse<Any>

    @GET("{appId}/chat/common/bots")
    suspend fun fetchPublicAgent(@Path("appId") appId: String = AIChatCenter.mAppId): AIBaseResponse<List<AIAgentResult>>

    @DELETE("{appId}/chat/users/{username}/toDeleteAgent/{toDeleteUsername}")
    suspend fun deleteChatUser(
        @Path("appId") appId: String,
        @Path("username") username: String,
        @Path("toDeleteUsername") toDeleteUsername: String,
    ): AIBaseResponse<Any>

    @POST("{appId}/voice/tts")
    suspend fun requestTts(@Path("appId") appId: String,@Body req: TTSReq): AIBaseResponse<TTSResult>

    companion object {

        // 模拟网络获取公开智能体
        suspend fun requestPublicBot(): BaseResponse<List<AIAgentModel>> = withContext(Dispatchers.IO) {
            val response = BaseResponse<List<AIAgentModel>>().apply {
                code = 0
                data = mutableListOf(
//                    AIAgentModel("智能客服1", "", "智能客服11", "", "101"),
//                    AIAgentModel("智能客服2", "", "智能客服22", "", "102"),
//                    AIAgentModel("智能客服3", "", "智能客服33", "", "103"),
//                    AIAgentModel("智能客服4", "", "智能客服44", "", "104")
                )
            }

            return@withContext response
        }
    }

}