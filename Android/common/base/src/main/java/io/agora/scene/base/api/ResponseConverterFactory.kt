package io.agora.scene.base.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.agora.scene.base.api.apiutils.GsonUtils
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

//自定义一个响应变换工厂
class ResponseConverterFactory private constructor(private val gson: Gson) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type?,
        annotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<ResponseBody, *>? {
        //返回我们自定义的Gson响应体变换器
        return GsonResponseBodyConverter<ResponseBody>(gson, type!!)
    }

    override fun requestBodyConverter(
        type: Type?,
        parameterAnnotations: Array<Annotation>?,
        methodAnnotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<*, RequestBody>? {
        //返回我们自定义的Gson响应体变换器
        return GsonResponseBodyConverter(gson, type!!)
    }

    companion object {
        @JvmOverloads
        fun create(
            gson: Gson = GsonBuilder()//建造者模式设置不同的配置
                .serializeNulls()//序列化为null对象
                .disableHtmlEscaping()//防止对网址乱码 忽略对特殊字符的转换
                .registerTypeAdapter(String::class.java, GsonUtils.StringConverter())//对为null的字段进行转换
                .create()
        ): ResponseConverterFactory {
            return ResponseConverterFactory(gson)
        }
    }
}