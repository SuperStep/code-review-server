package dev.gordeev.review.server.job

import dev.gordeev.review.server.config.RepoIndexerConfig
import dev.gordeev.review.server.service.database.RagService
import dev.gordeev.review.server.service.files.FileExtractorService
import dev.gordeev.review.server.service.git.GitService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Paths

@Component
class RepositoryIndexerJob(
    private val repoIndexerConfig: RepoIndexerConfig,
    private val gitService: GitService,
    private val ragService: RagService,
    private val fileExtractorService: FileExtractorService
) : Job {
    override fun execute(context: JobExecutionContext) {

        logger.info("Starting repository processing...")

        val cloneBaseDir = Paths.get(repoIndexerConfig.clone.basePath)
        try {
            // Ensure base clone directory exists
            val baseDirFile = cloneBaseDir.toFile()
            if (!baseDirFile.exists()) {
                baseDirFile.mkdirs()
                logger.info("Created clone base directory: $cloneBaseDir")
            } else if (!baseDirFile.isDirectory) {
                logger.error("Clone base path $cloneBaseDir exists but is not a directory. Aborting.")
                return
            }

            repoIndexerConfig.repositories.forEach { repoConfig ->

                logger.info("Processing repository: ${repoConfig.name} from ${repoConfig.url}")

                var clonedRepoPath: File? = null
                try {
                    clonedRepoPath = gitService.cloneRepository(repoConfig, cloneBaseDir)

                    if (clonedRepoPath != null && clonedRepoPath.exists()) {

                        ragService.clearTable(repoConfig.name)

                        val fileDataList = fileExtractorService.extractDataFromRepository(
                            clonedRepoPath,
                            repoConfig.name,
                            repoConfig.includedExtensions
                        )

                        if (fileDataList.isNotEmpty()) {
                            ragService.createTableAndSaveData(repoConfig.name, fileDataList)
                            logger.info("Successfully processed and saved data for ${repoConfig.name}")
                        } else {
                            logger.info("No files extracted or suitable for processing in ${repoConfig.name}")
                        }
                    } else {
                        logger.warn("Skipping data extraction for ${repoConfig.name} due to cloning issues.")
                    }
                } catch (e: Exception) {
                    logger.error("Failed to process repository ${repoConfig.name}: ${e.message}", e)
                    // Continue with the next repository
                } finally {
                    // Clean up the cloned repository directory after processing
                    // You might want to make this configurable (e.g., keep for debugging)
                    clonedRepoPath?.let {
                        try {
                            it.deleteRecursively()
                            logger.info("Cleaned up cloned directory: $it")
                        } catch (ioe: Exception) {
                            logger.error("Error cleaning up cloned directory $it: ${ioe.message}", ioe)
                        }
                    }
                }
                logger.info("-----------------------------------------------------")
            }
        } catch (e: Exception) {
            logger.error("Fatal error during repository processing setup: ${e.message}", e)
        }
        logger.info("Repository processing finished.")

    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(ReviewProcessJob::class.java)
    }
}