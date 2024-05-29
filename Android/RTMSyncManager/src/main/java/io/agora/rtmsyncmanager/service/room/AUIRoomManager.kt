package io.agora.rtmsyncmanager.service.room

import io.agora.rtmsyncmanager.service.http.CommonResp
import io.agora.rtmsyncmanager.service.http.HttpManager
import io.agora.rtmsyncmanager.service.http.Utils
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.service.callback.AUICallback
import io.agora.rtmsyncmanager.service.callback.AUIException
import io.agora.rtmsyncmanager.service.callback.AUIRoomCallback
import io.agora.rtmsyncmanager.service.callback.AUIRoomListCallback
import io.agora.rtmsyncmanager.service.http.room.*
import io.agora.rtmsyncmanager.utils.AUILogger
import retrofit2.Call
import retrofit2.Response

class AUIRoomManager {

    private val tag = "AUIRoomManager"
    private val roomInterface by lazy {
        HttpManager.getService(RoomInterface::class.java)
    }

    fun createRoom(
        appId: String,
        sceneId: String,
        roomInfo: AUIRoomInfo,
        callback: AUIRoomCallback?
    ) {
        AUILogger.logger().d(tag, "createRoom sceneId:$sceneId, roomInfo:$roomInfo")
        val roomId = roomInfo.roomId
        roomInterface.createRoom(CreateRoomReq(
            appId,
            sceneId,
            roomId,
            roomInfo
        ))
            .enqueue(object : retrofit2.Callback<CommonResp<CreateRoomResp>> {
                override fun onResponse(
                    call: Call<CommonResp<CreateRoomResp>>,
                    response: Response<CommonResp<CreateRoomResp>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        // success
                        val info = rsp.payload
                        val rInfo = AUIRoomInfo()
                        rInfo.roomId = info.roomId
                        rInfo.roomName = info.roomName
                        rInfo.roomOwner = roomInfo.roomOwner
                        rInfo.customPayload = roomInfo.customPayload
                        rInfo.createTime = rsp.createTime
                        AUIRoomContext.shared().insertRoomInfo(rInfo)
                        callback?.onResult(null, rInfo)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<CreateRoomResp>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message), null)
                }
            })
    }

    fun destroyRoom(
        appId: String,
        sceneId: String,
        roomId: String,
        callback: AUICallback?
    ) {
        AUILogger.logger().d(tag, "destroyRoom sceneId:$sceneId, roomId:$roomId")
        roomInterface.destroyRoom(RoomUserReq(appId, sceneId, roomId))
            .enqueue(object : retrofit2.Callback<CommonResp<DestroyRoomResp>> {
                override fun onResponse(
                    call: Call<CommonResp<DestroyRoomResp>>,
                    response: Response<CommonResp<DestroyRoomResp>>
                ) {
                    if (response.code() == 200) {
                        // success
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response))
                    }
                }

                override fun onFailure(call: Call<CommonResp<DestroyRoomResp>>, t: Throwable) {
                    callback?.onResult(
                        AUIException(
                            -1,
                            t.message
                        )
                    )
                }
            })
    }

    fun getRoomInfoList(
        appId: String,
        sceneId: String,
        lastCreateTime: Long?,
        pageSize: Int,
        callback: AUIRoomListCallback?
    ) {
        AUILogger.logger().d(tag, "getRoomInfoList sceneId:$sceneId, lastCreateTime:$lastCreateTime, pageSize:$pageSize")
        roomInterface.fetchRoomList(RoomListReq(appId, sceneId, pageSize, lastCreateTime))
            .enqueue(object : retrofit2.Callback<CommonResp<RoomListResp>> {
                override fun onResponse(
                    call: Call<CommonResp<RoomListResp>>,
                    response: Response<CommonResp<RoomListResp>>
                ) {
                    val roomList = response.body()?.data?.getRoomList()
                    val ts = response.body()?.ts
                    if (roomList != null) {
                        AUIRoomContext.shared().resetRoomMap(roomList)
                        callback?.onResult(null, roomList, ts)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null, ts)
                    }
                }

                override fun onFailure(call: Call<CommonResp<RoomListResp>>, t: Throwable) {
                    callback?.onResult(
                        AUIException(
                            -1,
                            t.message
                        ), null, null
                    )
                }
            })
    }

    fun getRoomInfo(
        appId: String,
        sceneId: String,
        roomId: String,
        callback: AUIRoomCallback?
    ) {
        AUILogger.logger().d(tag, "getRoomInfo sceneId:$sceneId, roomId:$roomId")
        roomInterface.queryRoomInfo(QueryRoomReq(appId, sceneId, roomId))
            .enqueue(object : retrofit2.Callback<CommonResp<QueryRoomResp>> {
                override fun onResponse(
                    call: Call<CommonResp<QueryRoomResp>>,
                    response: Response<CommonResp<QueryRoomResp>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        AUIRoomContext.shared().insertRoomInfo(rsp.payload)
                        // success
                        callback?.onResult(null, rsp.payload)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<QueryRoomResp>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message), null)
                }
            })
    }

    fun updateRoomInfo(
        appId: String,
        sceneId: String,
        roomInfo: AUIRoomInfo,
        callback: AUIRoomCallback?
    ) {
        AUILogger.logger().d(tag, "updateRoomInfo sceneId:$sceneId, roomInfo:$roomInfo")
        val roomId = roomInfo.roomId
        roomInterface.updateRoomInfo(UpdateRoomReq(
            appId,
            sceneId,
            roomId,
            roomInfo
        ))
            .enqueue(object : retrofit2.Callback<CommonResp<String>> {
                override fun onResponse(
                    call: Call<CommonResp<String>>,
                    response: Response<CommonResp<String>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        AUIRoomContext.shared().insertRoomInfo(roomInfo)
                        // success
                        callback?.onResult(null, roomInfo)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<String>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message), null)
                }
            })
    }
}