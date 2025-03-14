package dev.gordeev.review.server.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val name: String,
    val emailAddress: String,
    val displayName: String
)