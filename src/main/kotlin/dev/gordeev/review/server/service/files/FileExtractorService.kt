package dev.gordeev.review.server.service.files

import dev.gordeev.review.server.model.FileData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.charset.MalformedInputException
import java.nio.file.Files
import java.nio.file.Path

@Service
class FileExtractorService {

    private val logger = LoggerFactory.getLogger(FileExtractorService::class.java)
    private val maxFileSizeMb = 10 // Max file size in MB to process to avoid OOM on very large files
    private val maxFileSizeBytes = maxFileSizeMb * 1024 * 1024

    fun extractDataFromRepository(
        repoPath: File,
        repoName: String, // For logging and context
        includedExtensions: List<String>?
    ): List<FileData> {
        val fileDataList = mutableListOf<FileData>()
        val basePath = repoPath.toPath()

        logger.info("Starting file extraction for repository '$repoName' at $basePath")

        try {
            Files.walk(basePath).use { stream ->
                stream.filter { Files.isRegularFile(it) && !isGitInternalFile(it, basePath) }
                    .forEach { filePath ->
                        val fileName = filePath.fileName.toString()
                        val relativePath = basePath.relativize(filePath).toString()

                        if (shouldProcessFile(filePath, fileName, includedExtensions)) {
                            try {
                                val content = Files.readString(filePath) // Uses UTF-8 by default
                                fileDataList.add(FileData(relativePath, fileName, content))
                                logger.debug("Extracted: $relativePath")
                            } catch (e: MalformedInputException) {
                                logger.warn("Skipping file with non-UTF-8 encoding or binary content: $relativePath - ${e.message}")
                            } catch (e: OutOfMemoryError) {
                                logger.error("OutOfMemoryError reading file: $relativePath. Skipping this file.")
                            } catch (e: Exception) {
                                logger.error("Error reading file $relativePath: ${e.message}", e)
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            logger.error("Error walking directory $basePath for repository '$repoName': ${e.message}", e)
        }

        logger.info("Finished file extraction for repository '$repoName'. Found ${fileDataList.size} files.")
        return fileDataList
    }

    private fun isGitInternalFile(filePath: Path, basePath: Path): Boolean {
        return basePath.relativize(filePath).startsWith(".git")
    }

    private fun shouldProcessFile(filePath: Path, fileName: String, includedExtensions: List<String>?): Boolean {
        if (Files.size(filePath) > maxFileSizeBytes) {
            logger.warn("Skipping large file (>${maxFileSizeMb}MB): $fileName at $filePath")
            return false
        }

        return if (includedExtensions.isNullOrEmpty()) {
            true // Process all files if no extensions are specified
        } else {
            includedExtensions.any { ext -> fileName.endsWith(ext, ignoreCase = true) }
        }
    }
}
