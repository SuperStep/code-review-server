package dev.gordeev.review.server.provider.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("gemini")
class GeminiConfig {
    lateinit var token: String
}