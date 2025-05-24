package dev.gordeev.review.server.service.database

import dev.gordeev.review.server.model.FileData
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DatabaseService(private val jdbcTemplate: JdbcTemplate) {

    private val logger = LoggerFactory.getLogger(DatabaseService::class.java)

    /**
     * Performs a semantic search against the embeddings table for a given repository.
     *
     * @param repoName The name of the repository to search
     * @param query The search query
     * @param limit The maximum number of results to return (defaults to 10)
     * @return List of results containing chunk text and distance as strings
     */
    fun semanticSearch(repoName: String, query: String, limit: Int = 10): List<Map<String, String>> {
        val tableName = sanitizeTableName(repoName)
        val embeddingsTable = "${tableName}_contents_embeddings"

        val sql = """
        SELECT 
            chunk,
            embedding <=> ai.ollama_embed('nomic-embed-text', ?) as distance
        FROM $embeddingsTable
        ORDER BY distance
        LIMIT ?
    """.trimIndent()

        try {
            val rawResults = jdbcTemplate.queryForList(sql, query, limit)
            // Convert the results to ensure all values are strings
            val results = rawResults.map { row ->
                mapOf(
                    "chunk" to (row["chunk"] as String),
                    "distance" to (row["distance"]?.toString() ?: "")
                )
            }
            logger.info("Semantic search in $embeddingsTable returned ${results.size} results for query: '$query'")
            return results
        } catch (e: Exception) {
            logger.error("Error performing semantic search in table $embeddingsTable: ${e.message}", e)
            return emptyList()
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
        createVectorizer(tableName)

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

    private fun createVectorizer(tableName: String) {
        val vectorizerSql = """
        SELECT ai.create_vectorizer(
            'public.$tableName'::regclass,
            destination => '${tableName}_contents_embeddings',
            embedding => ai.embedding_ollama('nomic-embed-text', 768),
            chunking => ai.chunking_recursive_character_text_splitter('content'),
            formatting => ai.formatting_python_template('${"\$path, \$file_name, \$content"}')
        )
        """.trimIndent()

        try {
            jdbcTemplate.execute(vectorizerSql)
            logger.info("Vectorizer created for table $tableName.")
        } catch (e: Exception) {
            logger.error("Error creating vectorizer for table $tableName: ${e.message}", e)
            // You might want to decide whether this is a critical error or not
            // If vectorizer creation is optional, you might not want to re-throw
            throw e
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

    /**
     * Checks if the vectorizer has completed processing for a given repository.
     *
     * @param repoName The name of the repository to check
     * @return true if processing is complete, false otherwise
     */
    private fun isVectorizerProcessingComplete(repoName: String): Boolean {
        val tableName = sanitizeTableName(repoName)
        val sql = "SELECT source_table, pending_items FROM ai.vectorizer_status WHERE source_table = 'public.$tableName'"

        try {
            val results = jdbcTemplate.queryForList(sql)
            if (results.isEmpty()) {
                logger.info("No vectorizer found for table $tableName")
                return false
            }

            val pendingItems = results[0]["pending_items"] as Number
            val isComplete = pendingItems.toInt() == 0

            logger.info("Vectorizer for table $tableName: processing ${if (isComplete) "complete" else "in progress with $pendingItems pending items"}")
            return isComplete
        } catch (e: Exception) {
            logger.error("Error checking vectorizer status for table $tableName: ${e.message}", e)
            return false
        }
    }

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
}
