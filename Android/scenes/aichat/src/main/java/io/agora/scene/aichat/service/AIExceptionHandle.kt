package io.agora.scene.aichat.service

import android.net.ParseException
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import io.agora.scene.aichat.service.api.AIApiException
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException

object AIExceptionHandle {

    fun handleException(e: Throwable?): AIApiException {
        val ex: AIApiException
        e?.let {
            when (it) {
                is HttpException -> {
                    ex = AIApiException(it.code(), it.response()?.errorBody()?.string() ?: "")
                    return ex
                }

                is JsonParseException, is JSONException, is ParseException, is MalformedJsonException -> {
                    ex = AIApiException(-1, e.message)
                    return ex
                }

                is ConnectException -> {
                    ex = AIApiException(-1, e.message)
                    return ex
                }

                is javax.net.ssl.SSLException -> {
                    ex = AIApiException(-1, e.message)
                    return ex
                }

                is ConnectTimeoutException -> {
                    ex = AIApiException(-1, e.message)
                    return ex
                }

                is java.net.SocketTimeoutException -> {
                    ex = AIApiException(-1, e.message)
                    return ex
                }

                is java.net.UnknownHostException -> {
                    ex = AIApiException(-1, e.message)
                    return ex
                }

                is AIApiException -> return it

                else -> {
                    ex = AIApiException(-1, e.message)
                    return ex
                }
            }
        }
        ex = AIApiException(-1, e?.message ?: "")
        return ex
    }
}