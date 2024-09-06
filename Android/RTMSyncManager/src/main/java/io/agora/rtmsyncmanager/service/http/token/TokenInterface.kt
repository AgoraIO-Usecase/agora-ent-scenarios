package io.agora.rtmsyncmanager.service.http.token

import io.agora.rtmsyncmanager.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * TokenInterface is an interface that defines the API endpoints for token operations.
 * It includes methods for generating tokens.
 */
interface TokenInterface {

    /**
     * Generate a token for version 006.
     * @param req The request body containing the token generation details.
     * @return A Call object that can be used to send the request.
     */
    @POST("token006/generate")
    fun tokenGenerate006(@Body req: TokenGenerateReq): Call<CommonResp<TokenGenerateResp>>

    /**
     * Generate a token.
     * @param req The request body containing the token generation details.
     * @return A Call object that can be used to send the request.
     */
    @POST("token/generate")
    fun tokenGenerate(@Body req: TokenGenerateReq): Call<CommonResp<TokenGenerateResp>>

}