package gordeev.dev.aicodereview.provider

interface AiReviewProvider {
    fun getReview(diff: String): String?
}
