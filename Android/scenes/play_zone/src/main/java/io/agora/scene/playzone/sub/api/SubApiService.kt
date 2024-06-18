package io.agora.scene.playzone.sub.api

import io.agora.scene.playzone.BuildConfig
import retrofit2.Call
import retrofit2.http.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

interface SubApiService {
    companion object {
        const val TAG = "SubApiService"

        private val auth: String
            get() {
                // 实际的AppId和AppSecret
                val appId = BuildConfig.sub_appid
                val appSecret = BuildConfig.sub_appSecret
                // 将AppSecret转换为字节数组
                val secretKey = appSecret.toByteArray()
                val secretKeySpec = SecretKeySpec(secretKey, "HmacMD5")

                // 初始化 Mac 对象
                val mac = Mac.getInstance("HmacMD5")
                mac.init(secretKeySpec)

                // 计算 HMAC
                val hmacBytes = mac.doFinal(appId.toByteArray())

                // 将字节数组转换为十六进制字符串
                val appServerSign = hmacBytes.joinToString("") { "%02x".format(it) }
                return appServerSign
            }
    }

    @GET("{auth}")
    fun gameConfig(@Path("auth") auth: String): Call<SubGameApiInfo>

    @POST("")
    fun gameList(@Url url: String, @Body requestModel: SubGameListRequestModel): Call<SubCommonResp<SubGameResp>>

//    @POST("$get_mg_list")
//    fun gameList(@Body requestModel: SubGameRequestModel): Call<SubCommonResp<SubGameResp>>
}

