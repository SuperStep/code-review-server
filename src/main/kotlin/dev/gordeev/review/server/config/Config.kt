package dev.gordeev.review.server.config

import com.fasterxml.jackson.databind.ObjectMapper
import dev.gordeev.review.server.provider.OllamaReviewProvider
import dev.gordeev.review.server.provider.config.GeminiConfig
import dev.gordeev.review.server.provider.config.OllamaProperties
import gordeev.dev.aicodereview.provider.AiReviewProvider
import gordeev.dev.aicodereview.provider.GeminiReviewProvider
import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class Config {

    @Bean
    @ConditionalOnProperty(name = ["ai.provider"], havingValue = "gemini", matchIfMissing = false)
    fun geminiProvider(geminiConfig: GeminiConfig): AiReviewProvider {
        return GeminiReviewProvider(geminiConfig)
    }

    @Bean
    @ConditionalOnProperty(name = ["ai.provider"], havingValue = "ollama", matchIfMissing = true)
    fun ollamaProvider(
        ollamaProperties: OllamaProperties,
        okHttpClient: OkHttpClient,
        objectMapper: ObjectMapper
    ): AiReviewProvider {
        return OllamaReviewProvider(ollamaProperties, okHttpClient, objectMapper)
    }

    @Bean
    fun okHttpClient(aiReviewProperties: AiReviewProperties): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(aiReviewProperties.readTimeoutSec, TimeUnit.SECONDS)
            .build()
    }
}