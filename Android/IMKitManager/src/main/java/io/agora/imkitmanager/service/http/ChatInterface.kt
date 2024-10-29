package io.agora.imkitmanager.service.http

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatInterface {

    @Headers("Content-Type: application/json")
    @POST("webdemo/im/chat/create")
    fun createChatRoom(@Body req: CreateChatRoomRequest): Call<ChatCommonResp<CreateChatRoomResponse>>

}