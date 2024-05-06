package io.agora.rtmsyncmanager.service.http.token

import io.agora.rtmsyncmanager.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenInterface {

    @POST("token006/generate")
    fun tokenGenerate006(@Body req: TokenGenerateReq): Call<CommonResp<TokenGenerateResp>>

    @POST("token/generate")
    fun tokenGenerate(@Body req: TokenGenerateReq): Call<CommonResp<TokenGenerateResp>>

}