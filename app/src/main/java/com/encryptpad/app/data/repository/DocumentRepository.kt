package com.encryptpad.app.data.repository

import android.content.Context
import com.encryptpad.app.data.encryption.EncryptionManager
import com.encryptpad.app.data.model.EncryptedDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.UUID

class DocumentRepository(private val context: Context) {
    private val encryptionManager = EncryptionManager()
    private val documentsDir = File(context.filesDir, "documents").apply { mkdirs() }

    suspend fun saveDocument(
        name: String,
        content: String,
        isEncrypted: Boolean,
        password: String? = null,
        documentId: String? = null
    ): Result<EncryptedDocument> = withContext(Dispatchers.IO) {
        try {
            val id = documentId ?: UUID.randomUUID().toString()
            val file = File(documentsDir, "$id.txt")

            val contentToSave = if (isEncrypted && password != null) {
                encryptionManager.encrypt(content, password)
            } else {
                content
            }

            file.writeText(contentToSave)

            val document = EncryptedDocument(
                id = id,
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
            val file = File(documentsDir, "$documentId.txt")
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Document not found"))
            }

            val fileContent = file.readText()
            val isEncrypted = password != null

            val content = if (password != null) {
                try {
                    encryptionManager.decrypt(fileContent, password)
                } catch (e: Exception) {
                    return@withContext Result.failure(Exception("Incorrect password"))
                }
            } else {
                fileContent
            }

            val document = EncryptedDocument(
                id = documentId,
                name = file.nameWithoutExtension,
                content = content,
                isEncrypted = isEncrypted,
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
                if (file.extension == "txt") {
                    EncryptedDocument(
                        id = file.nameWithoutExtension,
                        name = file.nameWithoutExtension,
                        content = "",
                        isEncrypted = false,
                        lastModified = Date(file.lastModified()),
                        filePath = file.absolutePath
                    )
                } else null
            } ?: emptyList()

            Result.success(documents.sortedByDescending { it.lastModified })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(documentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(documentsDir, "$documentId.txt")
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
