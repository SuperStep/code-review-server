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
    var review: ReviewProperties = ReviewProperties()
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
 * Defines the different parts of the prompt sent to the AI for code review.
 * Maps properties under 'ai.review.prompt'.
 */
data class PromptProperties(
    /**
     * The initial part of the prompt, setting the context for the AI.
     */
    var start: String = "",
    /**
     * The part of the prompt introducing the code diff.
     */
    var diff: String = "",
    /**
     * Additional instructions or formatting requirements for the AI's response.
     */
    var additional: String = ""
)
