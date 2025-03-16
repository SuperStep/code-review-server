package dev.gordeev.review.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "bitbucket")
class BitbucketProperties {
    lateinit var baseUrl: String
    lateinit var token: String
    lateinit var project: String
    lateinit var repository: String
    var certificatePath: String = ""
    var certificatePassword: String = ""
}