package com.encryptpad.app.data.repository

import android.content.Context
import com.encryptpad.app.data.encryption.EncryptionManager
import com.encryptpad.app.data.model.EncryptedDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

class DocumentRepository(
    private val context: Context,
    private val encryptionManager: EncryptionManager
) {
    private val documentsDir = File(context.filesDir, "documents").apply { mkdirs() }

    // Heuristic fallback for legacy metadata that doesn't include an isEncrypted flag.
    // Encrypted payloads are Base64-encoded bytes (IV + ciphertext), while plaintext is not.
    private fun inferEncryptionFromContent(fileContent: String): Boolean {
        val normalized = fileContent.trim()
        if (normalized.isEmpty()) return false

        return try {
            val decoded = java.util.Base64.getDecoder().decode(normalized)
            decoded.size > 12 && java.util.Base64.getEncoder().encodeToString(decoded) == normalized
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    private fun generateUniqueFilename(baseName: String): String {
        val sanitized = sanitizeFilename(baseName)
        var filename = "$sanitized.enc"
        var counter = 1
        
        while (File(documentsDir, filename).exists()) {
            filename = "${sanitized}_$counter.enc"
            counter++
        }
        
        return filename
    }

    suspend fun saveDocument(
        name: String,
        content: String,
        isEncrypted: Boolean,
        password: String? = null,
        documentId: String? = null,
        useBiometric: Boolean = false
    ): Result<EncryptedDocument> = withContext(Dispatchers.IO) {
        try {
            val filename = documentId ?: generateUniqueFilename(name)
            val file = File(documentsDir, filename)

            // Save content directly (encrypted or plain)
            val dataToSave = if (isEncrypted && password != null) {
                encryptionManager.encrypt(content, password)
            } else {
                content
            }

            file.writeText(dataToSave)
            
            // Save metadata (including isEncrypted flag)
            val metaFile = File(documentsDir, "$filename.meta")
            val now = System.currentTimeMillis()
            val createdAt = if (metaFile.exists()) {
                metaFile.readLines().firstOrNull()?.toLongOrNull() ?: now
            } else {
                now
            }
            metaFile.writeText("$createdAt\n$now\n$useBiometric\n$isEncrypted")

            val document = EncryptedDocument(
                id = filename,
                title = name,
                content = content,
                isEncrypted = isEncrypted,
                lastModified = Date(now),
                createdAt = createdAt,
                lastModifiedTimestamp = now,
                useBiometric = useBiometric,
                filePath = file.absolutePath
            )

            Result.success(document)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadDocument(
        documentId: String,
        password: String? = null
    ): Result<EncryptedDocument> = withContext(Dispatchers.IO) {
        try {
            val file = File(documentsDir, documentId)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Document not found"))
            }

            val fileContent = file.readText()
            
            // Decrypt if password provided
            val content = if (password != null) {
                try {
                    encryptionManager.decrypt(fileContent, password)
                } catch (e: Exception) {
                    return@withContext Result.failure(Exception("Incorrect password"))
                }
            } else {
                fileContent
            }

            // Read metadata
            val metaFile = File(documentsDir, "$documentId.meta")
            data class DocMeta(val createdAt: Long, val lastModified: Long, val useBiometric: Boolean, val isEncrypted: Boolean)
            val meta = if (metaFile.exists()) {
                val lines = metaFile.readLines()
                val inferredIsEncrypted = inferEncryptionFromContent(fileContent)
                DocMeta(
                    createdAt = lines.getOrNull(0)?.toLongOrNull() ?: file.lastModified(),
                    lastModified = lines.getOrNull(1)?.toLongOrNull() ?: file.lastModified(),
                    useBiometric = lines.getOrNull(2)?.toBooleanStrictOrNull() ?: false,
                    isEncrypted = lines.getOrNull(3)?.toBooleanStrictOrNull() ?: inferredIsEncrypted
                )
            } else {
                DocMeta(
                    createdAt = file.lastModified(),
                    lastModified = file.lastModified(),
                    useBiometric = false,
                    isEncrypted = inferEncryptionFromContent(fileContent)
                )
            }

            // Extract document name from filename
            val documentName = file.nameWithoutExtension.replace("_", " ")

            val document = EncryptedDocument(
                id = documentId,
                title = documentName,
                content = content,
                isEncrypted = meta.isEncrypted,
                lastModified = Date(meta.lastModified),
                createdAt = meta.createdAt,
                lastModifiedTimestamp = meta.lastModified,
                useBiometric = meta.useBiometric,
                filePath = file.absolutePath
            )

            Result.success(document)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listDocuments(): Result<List<EncryptedDocument>> = withContext(Dispatchers.IO) {
        try {
            val documents = documentsDir.listFiles()?.mapNotNull { file ->
                if (file.extension == "enc") {
                    try {
                        // Extract name from filename
                        val documentName = file.nameWithoutExtension.replace("_", " ")
                        
                        // Read metadata (including isEncrypted flag)
                        val metaFile = File(documentsDir, "${file.name}.meta")
                        val (createdAt, lastModified, useBiometric, isEncrypted) = if (metaFile.exists()) {
                            val lines = metaFile.readLines()
                            val inferredIsEncrypted = inferEncryptionFromContent(file.readText())
                            listOf(
                                lines.getOrNull(0)?.toLongOrNull() ?: file.lastModified(),
                                lines.getOrNull(1)?.toLongOrNull() ?: file.lastModified(),
                                lines.getOrNull(2)?.toBooleanStrictOrNull() ?: false,
                                lines.getOrNull(3)?.toBooleanStrictOrNull() ?: inferredIsEncrypted
                            )
                        } else {
                            listOf(
                                file.lastModified(),
                                file.lastModified(),
                                false,
                                inferEncryptionFromContent(file.readText())
                            )
                        }
                        
                        EncryptedDocument(
                            id = file.name,
                            title = documentName,
                            content = "",
                            isEncrypted = isEncrypted as Boolean,
                            lastModified = Date(lastModified as Long),
                            createdAt = createdAt as Long,
                            lastModifiedTimestamp = lastModified,
                            useBiometric = useBiometric as Boolean,
                            filePath = file.absolutePath
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else null
            } ?: emptyList()

            Result.success(documents.sortedByDescending { it.lastModified })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllDocuments(): List<EncryptedDocument> = withContext(Dispatchers.IO) {
        listDocuments().getOrDefault(emptyList())
    }
    
    suspend fun getRawContent(documentId: String): String = withContext(Dispatchers.IO) {
        val file = File(documentsDir, documentId)
        if (file.exists()) file.readText() else ""
    }
    
    suspend fun importDocument(
        title: String,
        encryptedContent: String,
        createdAt: Long,
        lastModifiedTimestamp: Long,
        useBiometric: Boolean,
        isEncrypted: Boolean = true
    ): Result<EncryptedDocument> = withContext(Dispatchers.IO) {
        try {
            val filename = generateUniqueFilename(title)
            val file = File(documentsDir, filename)
            file.writeText(encryptedContent)
            
            // Save metadata
            val metaFile = File(documentsDir, "$filename.meta")
            metaFile.writeText("$createdAt\n$lastModifiedTimestamp\n$useBiometric\n$isEncrypted")
            
            val document = EncryptedDocument(
                id = filename,
                title = title,
                content = "",
                isEncrypted = isEncrypted,
                lastModified = Date(lastModifiedTimestamp),
                createdAt = createdAt,
                lastModifiedTimestamp = lastModifiedTimestamp,
                useBiometric = useBiometric,
                filePath = file.absolutePath
            )
            
            Result.success(document)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(documentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(documentsDir, documentId)
            val metaFile = File(documentsDir, "$documentId.meta")
            if (file.exists()) {
                file.delete()
            }
            if (metaFile.exists()) {
                metaFile.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a document is encrypted without loading its content.
     * Returns true if encrypted, false if not encrypted or if document doesn't exist.
     */
    suspend fun isDocumentEncrypted(documentId: String): Boolean = withContext(Dispatchers.IO) {
        val documentFile = File(documentsDir, documentId)
        if (!documentFile.exists()) {
            return@withContext false
        }

        val metaFile = File(documentsDir, "$documentId.meta")
        if (metaFile.exists()) {
            val lines = metaFile.readLines()
            lines.getOrNull(3)?.toBooleanStrictOrNull() ?: inferEncryptionFromContent(documentFile.readText())
        } else {
            inferEncryptionFromContent(documentFile.readText())
        }
    }
}
