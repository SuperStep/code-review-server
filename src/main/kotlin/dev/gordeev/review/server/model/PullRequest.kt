package dev.gordeev.review.server.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class PullRequest(
    val id: Long,
    val title: String,
    val fromRef: Reference,
    val toRef: Reference,
    val author: Author,
    val createdDate: Long,
    val updatedDate: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Reference(
    val id: String,
    val displayId: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Author(
    @JsonProperty("user")
    val user: UserRef
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserRef(
    val name: String
)
