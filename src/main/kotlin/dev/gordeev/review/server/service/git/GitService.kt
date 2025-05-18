package dev.gordeev.review.server.service.git

import dev.gordeev.review.server.config.RepositoryConfig
import java.io.File
import java.nio.file.Path

interface GitService {
    /**
     * Fetches two branches from a remote repository and returns the diff between them
     * @param repoUrl The URL of the remote Git repositoryDiff
     * @param branch1 The name of the first branch
     * @param branch2 The name of the second branch
     * @return The diff between the branches as a string
     */
    fun getDiffBetweenBranches(repoUrl: String, branch1: String, branch2: String): String

    /**
     * Clones specified repository to path
     * @param repoConfig
     * @param clonePath
     */
    fun cloneRepository(repoConfig: RepositoryConfig, clonePath: Path): File?
}
