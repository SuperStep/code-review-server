package dev.gordeev.review.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Configuration properties for AI provider settings.
 * Maps properties under the 'ai' prefix in application.yml.
 */
@Configuration
@ConfigurationProperties("ai")
data class AiReviewProperties(
    /**
     * The selected AI provider. Options: ollama, gemini.
     */
    var provider: String = "gemini", // Default value matches the YAML
    /**
     * Configuration specific to the code review functionality.
     */
    var review: ReviewProperties = ReviewProperties(),
    /**
     * Read timeout for any AI provider.
     */
    var readTimeoutSec: Long = 60,
)

/**
 * Configuration properties related to AI code reviews.
 * Maps properties under 'ai.review'.
 */
data class ReviewProperties(
    /**
     * Configuration for the prompts used in AI code reviews.
     */
    var prompt: PromptProperties = PromptProperties()
)

/**
 * Defines the prompt template with placeholders for variables.
 * Maps properties under 'ai.review.prompt'.
 */
data class PromptProperties(
    /**
     * The prompt template with placeholders for variables like ${title}, ${diff}, etc.
     */
    var template: String = ""
)
