package dev.gordeev.review.server.service

import dev.gordeev.review.server.model.PullRequest
import dev.gordeev.review.server.model.PullRequestToReview
import dev.gordeev.review.server.service.git.GitDiffService
import gordeev.dev.aicodereview.provider.AiReviewProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class ReviewService(
    val gitDiffService: GitDiffService,
    val atReviewProvider: AiReviewProvider
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun review(pullRequestToReview: PullRequestToReview): String{

        val pullRequest = pullRequestToReview.pullRequest

        logger.info("Starting diff retrieval for PR #${pullRequest.id}")
        val startTime = System.nanoTime()

        val diff = gitDiffService.getDiffBetweenBranches(
            pullRequest.base.repo.cloneUrl,
            pullRequest.base.ref,
            pullRequest.head.ref
        )

        val endTime = System.nanoTime()
        val durationSeconds = (endTime - startTime) / 1_000_000_000.0
        logger.info("Diff retrieval completed for PR #${pullRequest.id} in $durationSeconds seconds")

        val prompt = """
            Ты проводишь ревью кода. Проверь следующий код на наличие ошибок, недочетов и лучших практик.
            
            Вот заголовок изменения: ${pullRequest.title}
            
            А вот изменения:
            ```
            ${diff}
            ```
           
            Пожалуйста, дай мне ответ на русском языке.
            1. Общая оценка
            2. Основные проблемы или вопросы
            3. Стиль и рекомендации по лучшим практикам
            4. Замечания по безопасности, если они применимы
            
            Так особенно учти комментарии автора? они очень важны, вот они: 
            ${pullRequestToReview.reviewComment}
        """.trimIndent()

        try {
            atReviewProvider.getReview(prompt).let {
                return it.toString()
            }
        } catch (e: Exception) {
            logger.error("Error generating AI review for PR #${pullRequest.id}", e)
            throw e
        }
    }
}