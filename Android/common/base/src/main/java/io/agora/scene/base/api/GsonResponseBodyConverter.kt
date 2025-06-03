package io.agora.scene.base.api

import android.text.TextUtils
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException
import java.lang.reflect.Type

// Custom Gson response body converter
class GsonResponseBodyConverter<T>(
    private val gson: Gson,
    private val type: Type
) : Converter<ResponseBody, T> {
    val TAG = "SCallBack"

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T? {
        val response = value.string()
        // First parse JSON response to Response class. If code==200, parse to entity base class, otherwise throw exception
        //        BaseResponse httpResult = gson.fromJson(response, BaseResponse.class);
        //        if (httpResult != null || TextUtils.equals(httpResult.returnCode, ServiceErrorCode.RESPONSE_SUCCESS)) {
        // When status is 200, parse directly. No parsing exception possible since generic type matches success data format
        return if (!TextUtils.isEmpty(response)) {
            gson.fromJson<T>(response, type)
        } else {
            null
        }
    }
}