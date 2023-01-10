package io.agora.scene.base.api

import android.text.TextUtils
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException
import java.lang.reflect.Type

//自定义Gson响应体变换器
class GsonResponseBodyConverter<T>(
    private val gson: Gson,
    private val type: Type
) : Converter<ResponseBody, T> {
    val TAG = "SCallBack"

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T? {
        val response = value.string()
        //先将返回的json数据解析到Response中，如果code==200，则解析到我们的实体基类中，否则抛异常
        //        BaseResponse httpResult = gson.fromJson(response, BaseResponse.class);
        //        if (httpResult != null || TextUtils.equals(httpResult.returnCode, ServiceErrorCode.RESPONSE_SUCCESS)) {
        //200的时候就直接解析，不可能出现解析异常。因为我们实体基类中传入的泛型，就是数据成功时候的格式
        return if (!TextUtils.isEmpty(response)) {
            gson.fromJson<T>(response, type)
        } else {
            null
        }
    }
}