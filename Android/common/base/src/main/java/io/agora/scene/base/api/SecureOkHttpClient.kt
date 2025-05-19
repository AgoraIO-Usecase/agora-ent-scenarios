package io.agora.scene.base.api

import io.agora.scene.base.api.intercepter.HttpLogger
import okhttp3.OkHttpClient
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object SecureOkHttpClient {
    private fun createTrustManager(): X509TrustManager {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers = trustManagerFactory.trustManagers
        if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
            throw IllegalStateException("Unexpected default trust managers: ${trustManagers.contentToString()}")
        }
        return trustManagers[0] as X509TrustManager
    }

    @JvmStatic
    fun createWithSeconds(seconds: Long): OkHttpClient.Builder {
        return create(
            readTimeout = seconds.seconds,
            writeTimeout = seconds.seconds,
            connectTimeout = seconds.seconds
        )
    }

    @JvmStatic
    fun create(
        readTimeout: Duration = 30.seconds,
        writeTimeout: Duration = 30.seconds,
        connectTimeout: Duration = 30.seconds
    ): OkHttpClient.Builder {
        val trustManager = createTrustManager()
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), null)
        }

        return OkHttpClient.Builder()
            .writeTimeout(writeTimeout)
            .readTimeout(readTimeout)
            .connectTimeout(connectTimeout)
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { hostname, session ->
                HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
            }
            .protocols(listOf(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1))
            .addInterceptor(HttpLogger())
    }
} 