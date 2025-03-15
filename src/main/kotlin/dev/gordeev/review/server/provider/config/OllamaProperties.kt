package dev.gordeev.review.server.provider.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "ollama")
class OllamaProperties {
    var baseUrl: String = "http://192.168.0.154:11434"
    var model: String = "qwen2.5-coder:3b"
    var temperature: Double = 0.7
    var maxTokens: Int = 4096
}
