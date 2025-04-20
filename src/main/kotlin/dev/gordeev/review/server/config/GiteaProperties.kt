package dev.gordeev.review.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "gitea")
data class GiteaProperties(
    var baseUrl: String = "", // e.g., https://gitea.example.com/api/v1
    var token: String = "",
    var owner: String = "", // Repository owner (user or organization)
    var repository: String = "" // Repository name
)
