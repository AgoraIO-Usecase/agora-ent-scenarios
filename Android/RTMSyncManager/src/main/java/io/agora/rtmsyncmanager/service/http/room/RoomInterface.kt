package io.agora.rtmsyncmanager.service.http.room

import io.agora.rtmsyncmanager.service.http.CommonResp
import io.agora.rtmsyncmanager.service.http.room.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * RoomInterface is an interface that defines the API endpoints for room operations.
 * It includes methods for creating, destroying, listing, querying, and updating rooms.
 */
interface RoomInterface {

    /**
     * Create a new room.
     * @param req The request body containing the room details.
     * @return A Call object that can be used to send the request.
     */
    @POST("room/create")
    fun createRoom(@Body req: CreateRoomReq): Call<CommonResp<CreateRoomResp>>

    /**
     * Destroy an existing room.
     * @param req The request body containing the room details.
     * @return A Call object that can be used to send the request.
     */
    @POST("room/destroy")
    fun destroyRoom(@Body req: RoomUserReq): Call<CommonResp<DestroyRoomResp>>

    /**
     * Fetch a list of rooms.
     * @param req The request body containing the pagination details.
     * @return A Call object that can be used to send the request.
     */
    @POST("room/list")
    fun fetchRoomList(@Body req: RoomListReq): Call<CommonResp<RoomListResp>>

    /**
     * Query the details of a specific room.
     * @param req The request body containing the room details.
     * @return A Call object that can be used to send the request.
     */
    @POST("room/query")
    fun queryRoomInfo(@Body req: QueryRoomReq): Call<CommonResp<QueryRoomResp>>

    /**
     * Update the details of a specific room.
     * @param req The request body containing the room details.
     * @return A Call object that can be used to send the request.
     */
    @POST("room/update")
    fun updateRoomInfo(@Body req: UpdateRoomReq): Call<CommonResp<String>>

}