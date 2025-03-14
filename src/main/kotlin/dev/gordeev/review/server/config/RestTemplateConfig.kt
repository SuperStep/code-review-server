package dev.gordeev.review.server.config

import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

@Configuration
class RestClientConfig(val settings: BitbucketProperties) {

    @Bean
    fun createOkHttpClient(): OkHttpClient {
        try {
            if(settings.certificatePath.isEmpty()
                && settings.certificatePassword.isEmpty()) {
                return OkHttpClient.Builder()
                    .build()
            }
            val keyStoreFile = File(settings.certificatePath)
            val keyStore = KeyStore.getInstance("PKCS12")
            keyStore.load(FileInputStream(keyStoreFile), settings.certificatePassword.toCharArray())

            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, settings.certificatePassword.toCharArray())

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagerFactory.keyManagers, null, null)

            return OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, getTrustManager())
                .build()
        } catch (e: Exception) {
            // We can't show notifications here since we don't have a Project reference
            // The exception will be caught in the calling method
            throw IOException("Failed to create HTTP client: ${e.message}", e)
        }
    }

    private fun getTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }
}