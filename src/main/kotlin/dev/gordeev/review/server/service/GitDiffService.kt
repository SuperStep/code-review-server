package dev.gordeev.review.server.service

interface GitDiffService {
    /**
     * Fetches two branches from a remote repository and returns the diff between them
     * @param repoUrl The URL of the remote Git repository
     * @param branch1 The name of the first branch
     * @param branch2 The name of the second branch
     * @return The diff between the branches as a string
     */
    fun getDiffBetweenBranches(repoUrl: String, branch1: String, branch2: String): String
}
