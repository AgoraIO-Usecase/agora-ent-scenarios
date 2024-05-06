package io.agora.rtmsyncmanager.service.http.room

import io.agora.rtmsyncmanager.service.http.CommonResp
import io.agora.rtmsyncmanager.service.http.room.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RoomInterface {

    @POST("room/create")
    fun createRoom(@Body req: CreateRoomReq): Call<CommonResp<CreateRoomResp>>

    @POST("room/destroy")
    fun destroyRoom(@Body req: RoomUserReq): Call<CommonResp<DestroyRoomResp>>

    @POST("room/list")
    fun fetchRoomList(@Body req: RoomListReq): Call<CommonResp<RoomListResp>>

    @POST("room/query")
    fun queryRoomInfo(@Body req: QueryRoomReq): Call<CommonResp<QueryRoomResp>>

    @POST("room/update")
    fun updateRoomInfo(@Body req: UpdateRoomReq): Call<CommonResp<String>>

}