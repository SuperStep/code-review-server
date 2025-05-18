package dev.gordeev.review.server.service.git.impl

import dev.gordeev.review.server.config.GitConfig
import dev.gordeev.review.server.config.RepositoryConfig
import dev.gordeev.review.server.service.git.GitService
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.RefSpec
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import java.util.UUID

@Service
class GitServiceImpl(private val gitConfig: GitConfig) : GitService {

    private val logger = LoggerFactory.getLogger(GitServiceImpl::class.java)

    init {
        // Ensure the work directory exists
        val workDir = File(gitConfig.workDirectory)
        if (!workDir.exists()) {
            workDir.mkdirs()
        }
    }

    override fun getDiffBetweenBranches(repoUrl: String, branch1: String, branch2: String): String {
        val repoName = extractRepoName(repoUrl)
        val tempDir = File("${gitConfig.workDirectory}${File.separator}$repoName-${UUID.randomUUID()}")
        tempDir.mkdirs()

        try {
            // Clone the repository
            val git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(tempDir)
                .setNoCheckout(true) // Don't checkout any branch initially
                .call()

            // Fetch only the required branches
            git.fetch()
                .setRefSpecs(
                    RefSpec("refs/heads/$branch1:refs/heads/$branch1"),
                    RefSpec("refs/heads/$branch2:refs/heads/$branch2")
                )
                .call()

            val repository = git.repository
            val diffOutput = generateDiff(repository, branch1, branch2)

            git.close()
            return diffOutput
        } finally {
            // Clean up
            tempDir.deleteRecursively()
        }
    }

    private fun extractRepoName(repoUrl: String): String {
        // Extract repo name from URL (e.g., "repo-name" from "https://github.com/user/repo-name.git")
        val lastSlashIndex = repoUrl.lastIndexOf('/')
        var repoName = if (lastSlashIndex != -1) repoUrl.substring(lastSlashIndex + 1) else repoUrl

        // Remove .git suffix if present
        if (repoName.endsWith(".git")) {
            repoName = repoName.substring(0, repoName.length - 4)
        }

        return repoName
    }

    private fun generateDiff(repository: Repository, branch1: String, branch2: String): String {
        val git = Git(repository)
        val revWalk = RevWalk(repository)
        val outputStream = ByteArrayOutputStream()

        try {
            // Get commit IDs for both branches
            val branch1Id = repository.resolve("refs/heads/$branch1")
            val branch2Id = repository.resolve("refs/heads/$branch2")

            if (branch1Id == null || branch2Id == null) {
                throw IllegalArgumentException("One or both branches not found")
            }

            // Get the commit objects
            val commit1 = revWalk.parseCommit(branch1Id)
            val commit2 = revWalk.parseCommit(branch2Id)

            // Create a diff formatter
            val formatter = DiffFormatter(outputStream)
            formatter.setRepository(repository)

            // Generate the diff
            val reader = repository.newObjectReader()
            val tree1 = commit1.tree
            val tree2 = commit2.tree

            formatter.format(tree1, tree2)
            formatter.flush()

            return outputStream.toString()
        } finally {
            revWalk.dispose()
            outputStream.close()
        }
    }

    override fun cloneRepository(repoConfig: RepositoryConfig, clonePath: Path): File? {
        val localPath = clonePath.resolve(repoConfig.name.sanitizeForPath()).toFile()

        if (localPath.exists()) {
            logger.info("Repository ${repoConfig.name} already cloned at $localPath. Skipping clone.")
            // Optionally, you could add logic here to pull latest changes
            // For now, we assume if it exists, it's up-to-date for this run.
            // Or delete and re-clone:
            // localPath.deleteRecursively()
            // logger.info("Deleted existing directory $localPath for re-cloning.")
        }

        logger.info("Cloning repository ${repoConfig.name} from ${repoConfig.url} to $localPath")
        try {
            val cloneCommand = Git.cloneRepository()
                .setURI(repoConfig.url)
                .setDirectory(localPath)

            repoConfig.branch?.let {
                cloneCommand.setBranch(it)
                logger.info("Cloning branch: $it")
            }
            // Add authentication if needed, e.g. for private repositories
            // .setCredentialsProvider(UsernamePasswordCredentialsProvider("USERNAME", "PASSWORD"))

            val git = cloneCommand.call()
            git.close() // Close the git object to release resources
            logger.info("Successfully cloned ${repoConfig.name}")
            return localPath
        } catch (e: GitAPIException) {
            logger.error("Error cloning repository ${repoConfig.name}: ${e.message}", e)
            // Clean up partially cloned directory if cloning failed
            if (localPath.exists()) {
                localPath.deleteRecursively()
            }
            return null
        }
    }

    private fun String.sanitizeForPath(): String {
        return this.replace(Regex("[^a-zA-Z0-9_.-]"), "_")
    }
}