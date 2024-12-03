package io.agora.scene.cantata.service.api

import io.agora.scene.cantata.service.api.KtvCommonResp
import io.agora.scene.cantata.service.api.KtvSongApiListModel
import retrofit2.Call
import retrofit2.http.GET

/**
 * Ktv song service
 *
 * @constructor Create empty Ktv song service
 */
internal interface KtvApiService {
    @GET("ktv/songs")
    fun fetchSongList(): Call<KtvCommonResp<KtvSongApiListModel>>
}