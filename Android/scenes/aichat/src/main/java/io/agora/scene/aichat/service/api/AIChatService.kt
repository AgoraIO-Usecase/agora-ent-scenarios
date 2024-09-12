package io.agora.scene.aichat.service.api

import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.service.AIAgentManager
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
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
        @Path("appId") appId: String = AIChatCenter.mAppId,
        @Path("username") username: String,
        @FieldMap fields: Map<String, String>,
    ): AIBaseResponse<Any>

    @GET("{appId}/chat/common/bots")
    suspend fun fetchPublicAgent(@Path("appId") appId: String = AIChatCenter.mAppId): AIBaseResponse<List<AIAgentResult>>

    @DELETE("{appId}/chat/users/{username}/toDeleteAgent/{toDeleteUsername}")
    suspend fun deleteChatUser(
        @Path("appId") appId: String = AIChatCenter.mAppId,
        @Path("username") username: String,
        @Path("toDeleteUsername") toDeleteUsername: String,
    ): AIBaseResponse<Any>

    @POST("{appId}/voice/tts")
    suspend fun requestTts(
        @Path("appId") appId: String = AIChatCenter.mAppId,
        @Body req: TTSReq
    ): AIBaseResponse<TTSResult>

    @POST("{appId}/chat/agent/channelName/{channelName}")
    suspend fun startVoiceCall(
        @Path("appId") appId: String = AIChatCenter.mAppId,
        @Path("channelName") channelName: String,
        @Body req: StartVoiceCallReq
    ): AIBaseResponse<Any>

    @DELETE("{appId}/chat/agent/channelName/{channelName}")
    suspend fun stopVoiceCall(
        @Path("appId") appId: String = AIChatCenter.mAppId,
        @Path("channelName") channelName: String,
    ): AIBaseResponse<Any>

    @DELETE("{appId}/chat/agent/channelName/{channelName}/ping")
    suspend fun voiceCallPing(
        @Path("appId") appId: String = AIChatCenter.mAppId,
        @Path("channelName") channelName: String,
    ): AIBaseResponse<Any>

    @FormUrlEncoded
    @PUT("{appId}/chat/agent/channelName/{channelName}/ping")
    suspend fun updateVoiceCall(
        @Path("appId") appId: String = AIChatCenter.mAppId,
        @Path("channelName") channelName: String,
        @Field("interruptEnabled") interruptEnabled: Boolean,
    ): AIBaseResponse<Any>

    @POST("{appId}/chat/agent/channelName/{channelName}/interrupt")
    suspend fun interruptVoiceCall(
        @Path("appId") appId: String = AIChatCenter.mAppId,
        @Path("channelName") channelName: String,
    ): AIBaseResponse<Any>
}