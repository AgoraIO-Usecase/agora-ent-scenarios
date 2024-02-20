package io.agora.rtmsyncmanager.service.http.user

import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.user.CreateUserReq
import io.agora.auikit.service.http.user.CreateUserRsp
import io.agora.auikit.service.http.user.KickUserReq
import io.agora.auikit.service.http.user.KickUserRsp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserInterface {
    @POST("chatRoom/users/create")
    fun createUser(@Body req: CreateUserReq): Call<CommonResp<CreateUserRsp>>

    @POST("users/kickOut")
    fun kickOut(@Body req: KickUserReq): Call<CommonResp<KickUserRsp>>
}