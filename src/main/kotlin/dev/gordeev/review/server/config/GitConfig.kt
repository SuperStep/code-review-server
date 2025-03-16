package dev.gordeev.review.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.io.File

@ConfigurationProperties(prefix = "git")
class GitConfig {
    // Default to system temp directory if not specified
    var workDirectory: String = System.getProperty("java.io.tmpdir") + File.separator + "git-diffs"
}
