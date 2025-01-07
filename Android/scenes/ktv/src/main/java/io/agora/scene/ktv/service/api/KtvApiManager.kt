package io.agora.scene.ktv.service.api

import io.agora.scene.ktv.KTVLogger
import retrofit2.Call
import retrofit2.Response

class KtvApiManager {

    private val tag = "KtvApiManager"
    private val ktvApiInterface by lazy {
        KTVHttpManager.getService(KtvApiService::class.java)
    }

    fun getSongList(completion: (error: Exception?, list: List<KtvSongApiModel>) -> Unit) {
        KTVLogger.d(tag, "getSongList start")
        ktvApiInterface.fetchSongList()
            .enqueue(object : retrofit2.Callback<KtvCommonResp<KtvSongApiListModel>> {
                override fun onResponse(
                    call: Call<KtvCommonResp<KtvSongApiListModel>>,
                    response: Response<KtvCommonResp<KtvSongApiListModel>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) { // success
                        completion.invoke(null, rsp.songs)
                    } else {
                        completion.invoke(KTVHttpManager.errorFromResponse(response), emptyList())
                    }
                }

                override fun onFailure(call: Call<KtvCommonResp<KtvSongApiListModel>>, t: Throwable) {
                    completion.invoke(Exception(t.message), emptyList())
                }
            })
    }
}