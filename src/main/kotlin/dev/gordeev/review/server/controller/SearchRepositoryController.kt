package dev.gordeev.review.server.controller

import dev.gordeev.review.server.service.database.RagService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/search")
class SearchRepositoryController(val ragService: RagService) {
    @GetMapping
    fun search(repo: String, text: String): List<String> {
        return ragService.semanticSearch(repo, text, 10)
            .map { it.values }.flatten()
    }
}