package io.agora.scene.playzone.service.api

import io.agora.scene.playzone.BuildConfig
import retrofit2.Call
import retrofit2.http.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

interface SubApiService {
    companion object {
        const val TAG = "SubApiService"

        val auth: String
            get() {
                // Actual AppId and AppSecret
                val appId = BuildConfig.SUB_APP_ID
                val appSecret = BuildConfig.SUB_APP_SECRET
                // Convert AppSecret to byte array
                val secretKey = appSecret.toByteArray()
                val secretKeySpec = SecretKeySpec(secretKey, "HmacMD5")

                // Initialize Mac object
                val mac = Mac.getInstance("HmacMD5")
                mac.init(secretKeySpec)

                // Calculate HMAC
                val hmacBytes = mac.doFinal(appId.toByteArray())

                // Convert byte array to hexadecimal string
                val appServerSign = hmacBytes.joinToString("") { "%02x".format(it) }
                return appServerSign
            }
    }

    @GET("{auth}")
    fun gameConfig(@Path("auth") auth: String): Call<SubGameApiInfo>

    @POST
    fun gameList(@Url url: String, @Body requestModel: SubGameListRequestModel): Call<SubCommonResp<SubGameResp>>
}

