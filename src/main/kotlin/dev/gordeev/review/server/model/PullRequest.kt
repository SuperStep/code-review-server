package dev.gordeev.review.server.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.OffsetDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class PullRequest(
    val id: Long,
    val title: String,
    val base: Reference,
    val head: Reference,
    val user: User,
    @JsonProperty("created_at")
    val createdDate: OffsetDateTime,
    @JsonProperty("updated_at")
    val updatedDate: OffsetDateTime
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Reference(
    val ref: String,
    val repo: Repo
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val login: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Repo(
    val name: String,
    @JsonProperty("ssh_url")
    val sshUrl: String,
    @JsonProperty("clone_url")
    val cloneUrl: String
)