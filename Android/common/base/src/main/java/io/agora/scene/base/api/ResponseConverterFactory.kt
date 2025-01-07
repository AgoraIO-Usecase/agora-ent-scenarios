package io.agora.scene.base.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.agora.scene.base.api.apiutils.GsonUtils
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

// Custom response converter factory
class ResponseConverterFactory private constructor(private val gson: Gson) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        // Return our custom Gson response body converter
        return GsonResponseBodyConverter<ResponseBody>(gson, type)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        // Return our custom Gson response body converter
        return GsonResponseBodyConverter(gson, type)
    }

    companion object {
        @JvmOverloads
        fun create(
            gson: Gson = GsonBuilder()// Configure using builder pattern
                .serializeNulls()// Serialize null objects
                .disableHtmlEscaping()// Prevent URL encoding, ignore special character conversion
                .registerTypeAdapter(String::class.java, GsonUtils.StringConverter())// Convert null fields
                .create()
        ): ResponseConverterFactory {
            return ResponseConverterFactory(gson)
        }
    }
}