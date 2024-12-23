package io.agora.scene.base.api

import okhttp3.OkHttpClient
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext

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
    fun create(): OkHttpClient.Builder {
        val trustManager = createTrustManager()
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), null)
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { hostname, session ->
                HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
            }
            .protocols(listOf(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1))
    }
} 