package dev.gordeev.review.server.provider

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import dev.gordeev.review.server.provider.config.OllamaProperties
import gordeev.dev.aicodereview.provider.AiReviewProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

class OllamaReviewProvider(
    private val ollamaProperties: OllamaProperties,
    private val okHttpClient: OkHttpClient,
    private val objectMapper: ObjectMapper
) : AiReviewProvider {

    private val logger = LoggerFactory.getLogger(OllamaReviewProvider::class.java)

    override fun getReview(prompt: String): String? {
        try {
            val requestBody = OllamaRequest(
                model = ollamaProperties.model,
                prompt = prompt,
                temperature = ollamaProperties.temperature,
                maxTokens = ollamaProperties.maxTokens,
            )

            val request = Request.Builder()
                .url("${ollamaProperties.baseUrl}/api/generate")
                .post(objectMapper.writeValueAsString(requestBody).toRequestBody(APPLICATION_JSON_VALUE.toMediaType()))
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.error("Failed to get review from Ollama API: ${response.code}")
                    return null
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    logger.error("Empty response from Ollama API")
                    return null
                }

                val ollamaResponse = objectMapper.readValue(responseBody, OllamaResponse::class.java)
                logger.info("Ollama comment: {}", ollamaResponse.response)
                return ollamaResponse.response
            }
        } catch (e: Exception) {
            logger.error("Error getting review from Ollama API", e)
            return null
        }
    }

    data class OllamaRequest(
        val model: String,
        val prompt: String,
        val temperature: Double = 0.7,
        @JsonProperty("max_tokens")
        val maxTokens: Int = 4096,
        val stream: Boolean = false
    )

    data class OllamaResponse(
        val model: String,
        val response: String,
        @JsonProperty("created_at")
        val createdAt: String
    )
}
