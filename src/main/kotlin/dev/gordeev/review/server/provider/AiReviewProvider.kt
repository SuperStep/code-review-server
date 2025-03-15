package gordeev.dev.aicodereview.provider

interface AiReviewProvider {
    fun getReview(prompt: String): String?
}
