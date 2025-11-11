package com.encryptpad.app.data.repository

import android.content.Context
import com.encryptpad.app.data.encryption.EncryptionManager
import com.encryptpad.app.data.model.EncryptedDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

class DocumentRepository(private val context: Context) {
    private val encryptionManager = EncryptionManager()
    private val documentsDir = File(context.filesDir, "documents").apply { mkdirs() }

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
        documentId: String? = null
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

            val document = EncryptedDocument(
                id = filename,
                name = name,
                content = content,
                isEncrypted = isEncrypted,
                lastModified = Date(),
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

            // Extract document name from filename
            val documentName = file.nameWithoutExtension.replace("_", " ")

            val document = EncryptedDocument(
                id = documentId,
                name = documentName,
                content = content,
                isEncrypted = password != null,
                lastModified = Date(file.lastModified()),
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
                        
                        EncryptedDocument(
                            id = file.name,
                            name = documentName,
                            content = "",
                            isEncrypted = false, // Unknown until opened
                            lastModified = Date(file.lastModified()),
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

    suspend fun deleteDocument(documentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(documentsDir, documentId)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
