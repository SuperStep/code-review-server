package dev.gordeev.review.server.config

import dev.gordeev.review.server.provider.config.GeminiConfig
import gordeev.dev.aicodereview.provider.AiReviewProvider
import gordeev.dev.aicodereview.provider.GeminiReviewProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Config {
    @Bean
    fun aiReviewProvider(geminiConfig: GeminiConfig): AiReviewProvider {
        return GeminiReviewProvider(geminiConfig);
    }
}