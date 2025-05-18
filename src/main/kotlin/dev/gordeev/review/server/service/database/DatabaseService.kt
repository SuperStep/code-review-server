package dev.gordeev.review.server.service.database

import dev.gordeev.review.server.model.FileData
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DatabaseService(private val jdbcTemplate: JdbcTemplate) {

    private val logger = LoggerFactory.getLogger(DatabaseService::class.java)

    private fun sanitizeTableName(repoName: String): String {
        // Sanitize to prevent SQL injection and ensure valid table name
        // Replace non-alphanumeric characters with underscore, ensure it starts with a letter or underscore
        val sanitized = repoName.replace(Regex("[^a-zA-Z0-9_]"), "_").lowercase()
        return if (sanitized.firstOrNull()?.isLetter() == true || sanitized.startsWith("_")) {
            sanitized
        } else {
            "repo_$sanitized"
        }
    }

    @Transactional
    fun createTableAndSaveData(repoName: String, fileDataList: List<FileData>) {
        if (fileDataList.isEmpty()) {
            logger.info("No file data to save for repository: $repoName")
            return
        }

        val tableName = sanitizeTableName(repoName)
        logger.info("Preparing to save data to table: $tableName for repository: $repoName")

        createTableIfNotExists(tableName)

        val sql = "INSERT INTO $tableName (path, file_name, content) VALUES (?, ?, ?)"
        val batchArgs = fileDataList.map { arrayOf(it.path, it.fileName, it.content) }

        try {
            val rowsAffectedArray = jdbcTemplate.batchUpdate(sql, batchArgs)
            val totalRowsAffected = rowsAffectedArray.sum()
            logger.info("Successfully saved $totalRowsAffected records to table $tableName for repository $repoName.")
        } catch (e: Exception) {
            logger.error("Error batch inserting data into $tableName for repository $repoName: ${e.message}", e)
            // Depending on the exception, you might want to re-throw or handle more gracefully
            throw e // Re-throw to ensure transaction rollback if needed
        }
    }

    private fun createTableIfNotExists(tableName: String) {
        // Note: Using TEXT for content, consider BYTEA for binary or large content if needed,
        // or a CLOB/TEXT type appropriate for your specific RAG model needs.
        // Path and file_name could have unique constraints if desired.
        val createTableSql = """
        CREATE TABLE IF NOT EXISTS $tableName (
            id SERIAL PRIMARY KEY,
            path TEXT NOT NULL,
            file_name TEXT NOT NULL,
            content TEXT,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            UNIQUE (path, file_name) -- Optional: ensure unique file paths
        )
        """.trimIndent()
        try {
            jdbcTemplate.execute(createTableSql)
            logger.info("Table $tableName ensured to exist.")
        } catch (e: Exception) {
            logger.error("Error creating table $tableName: ${e.message}", e)
            throw e // Critical error, propagate
        }
    }

    fun tableExists(tableName: String): Boolean {
        val sql = """
            SELECT EXISTS (
                SELECT FROM information_schema.tables
                WHERE table_schema = current_schema()
                AND table_name = ?
            )
        """.trimIndent()
        return jdbcTemplate.queryForObject(sql, Boolean::class.java, tableName.lowercase()) ?: false
    }

    @Transactional
    fun clearTable(repoName: String) {
        val tableName = sanitizeTableName(repoName)
        if (tableExists(tableName)) {
            val sql = "DELETE FROM $tableName"
            try {
                val rowsDeleted = jdbcTemplate.update(sql)
                logger.info("Cleared $rowsDeleted rows from table $tableName for repository $repoName.")
            } catch (e: Exception) {
                logger.error("Error clearing table $tableName: ${e.message}", e)
                throw e
            }
        } else {
            logger.info("Table $tableName does not exist, skipping clear operation.")
        }
    }
}
