package gordeev.dev.aicodereview.provider

import com.google.gson.Gson
import dev.gordeev.review.server.provider.config.GeminiConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class GeminiReviewProvider(val geminiConfig: GeminiConfig) : AiReviewProvider {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getReview(prompt: String): String? {

        if (geminiConfig.token.isBlank()) {
            logger.error("Gemini token is not set!")
            return null
        }

        val client = HttpClient.newHttpClient()
        val requestBody = Gson().toJson(
            mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to prompt)
                        )
                    )
                )
            )
        )
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=${geminiConfig.token}"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()


        try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                val body = response.body()
                val jsonResponse = Gson().fromJson(body, Map::class.java)
                return extractGeminiResponse(jsonResponse)
            } else {
                logger.error("Error communicating with Gemini: ${response.statusCode()} - ${response.body()}")
                return null
            }

        } catch (e: Exception) {
            logger.error("Error communicating with Gemini: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    private fun extractGeminiResponse(jsonResponse: Map<*, *>): String {
        val candidates = jsonResponse["candidates"] as? List<*> ?: return "No response from AI."
        val firstCandidate = candidates.firstOrNull() as? Map<*, *> ?: return "No response from AI."
        val content = firstCandidate["content"] as? Map<*, *> ?: return "No response from AI."
        val parts = content["parts"] as? List<*> ?: return "No response from AI."
        val firstPart = parts.firstOrNull() as? Map<*, *> ?: return "No response from AI."
        return firstPart["text"] as? String ?: "No response from AI."
    }
}
