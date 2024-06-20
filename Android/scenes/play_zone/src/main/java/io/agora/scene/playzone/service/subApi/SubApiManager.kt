package io.agora.scene.playzone.service.subApi

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import io.agora.rtmsyncmanager.service.callback.AUIException
import io.agora.scene.playzone.BuildConfig
import io.agora.scene.playzone.PlayZoneLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


class SubApiManager {

    companion object {
        fun <T> errorFromResponse(response: Response<T>): AUIException {
            val errorMsg = response.errorBody()?.string()
            var code = response.code()
            var msg = errorMsg
            if (errorMsg != null) {
                try {
                    val obj = JSONObject(errorMsg)
                    if (obj.has("code")) {
                        code = obj.getInt("code")
                    }
                    if (obj.has("message")) {
                        msg = obj.getString("message")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return AUIException(code, msg)
        }

        private var baseUrl = "https://sim-asc.sudden.ltd/"
        private var retrofit: Retrofit? = null

        fun <T> getService(clazz: Class<T>): T {
            initRetrofit()
            return retrofit!!.create(clazz)
        }

        private fun initRetrofit() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .client(
                        OkHttpClient.Builder()
                            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                            .addInterceptor(CurlInterceptor(object : Logger {
                                override fun log(message: String) {
                                    Log.v("Ok2Curl", message)
                                }
                            }))
                            .build()
                    )
                    .baseUrl("$baseUrl")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
        }


        private val gson =
            GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .registerTypeAdapter(TypeToken.get(JSONObject::class.java).type, object : TypeAdapter<JSONObject>() {
                    @Throws(IOException::class)
                    override fun write(jsonWriter: JsonWriter, value: JSONObject) {
                        jsonWriter.jsonValue(value.toString())
                    }

                    @Throws(IOException::class)
                    override fun read(jsonReader: JsonReader): JSONObject? {
                        return null
                    }
                })
                .enableComplexMapKeySerialization()
                .create()

        private fun auth(): String {

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

    private val tag = "SubApiManager"
    private val apiInterface by lazy {
        getService(SubApiService::class.java)
    }

    fun getGameApiInfo(completion: (error: Exception?, gameApi: SubGameApiInfo?) -> Unit) {
        apiInterface.gameConfig(auth())
            .enqueue(object : retrofit2.Callback<SubGameApiInfo> {
                override fun onResponse(call: Call<SubGameApiInfo>, response: Response<SubGameApiInfo>) {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        completion.invoke(null, body)
                    } else {
                        completion.invoke(errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<SubGameApiInfo>, t: Throwable) {
                    completion.invoke(Exception(t.message), null)
                }
            })
    }

    fun getSubGameList(url: String, completion: (error: Exception?, list: List<SubGameInfo>) -> Unit) {
        PlayZoneLogger.d(tag, "getGameList start")
        val requestModel = SubGameListRequestModel(BuildConfig.sub_appid, BuildConfig.sub_appSecret)
        apiInterface.gameList(url, requestModel)
            .enqueue(object : retrofit2.Callback<SubCommonResp<SubGameResp>> {
                override fun onResponse(
                    call: Call<SubCommonResp<SubGameResp>>,
                    response: Response<SubCommonResp<SubGameResp>>
                ) {
                    val rsp = response.body()?.data
                    PlayZoneLogger.d(tag, "zzzzzz getGameList return ${rsp?.mg_info_list?.size}")
                    PlayZoneLogger.d(tag, "zzzzzz getGameList return $rsp")
                    rsp?.mg_info_list?.forEach {
                        PlayZoneLogger.d(tag, "zzzzzz ${it.name.zh_CN} ${it.mg_id} ${it.thumbnail192x192.zh_CN}")
                    }
                    if (response.body()?.ret_code == 0 && rsp != null) { // success
                        completion.invoke(null, rsp.mg_info_list ?: emptyList())
                    } else {
                        completion.invoke(errorFromResponse(response), emptyList())
                    }
                }

                override fun onFailure(call: Call<SubCommonResp<SubGameResp>>, t: Throwable) {
                    completion.invoke(Exception(t.message), emptyList())
                }
            })
    }

}