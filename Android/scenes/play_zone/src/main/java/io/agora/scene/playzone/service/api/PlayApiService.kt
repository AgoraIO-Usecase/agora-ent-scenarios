package io.agora.scene.playzone.service.api

import retrofit2.Call
import retrofit2.http.*

interface PlayApiService {
    companion object {
        const val TAG = "LeisureApiService"
    }

    @GET("/toolbox/v1/configs/{scenario}")
    fun gameConfig(@Path("scenario") scenario: String): Call<PlayZoneCommonResp<PlayZoneGameListModel>>
}

//{"code":0,"data"data:{"carousel":[{"index":1,"url":""},{"index":2,"url":""},{"index":3,"url":""}]},"msg":"success", "tip":"this is demo api, don't use in production!"}