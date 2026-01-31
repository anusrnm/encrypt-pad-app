package com.encryptpad.app.data.model

import kotlinx.serialization.Serializable
import java.util.Date

data class EncryptedDocument(
    val id: String,
    val title: String,
    val content: String,
    val isEncrypted: Boolean,
    val lastModified: Date,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedTimestamp: Long = System.currentTimeMillis(),
    val useBiometric: Boolean = false,
    val filePath: String? = null
) {
    // Keep 'name' as an alias for backwards compatibility
    val name: String get() = title
    
    companion object {
        fun create(
            id: String,
            title: String,
            content: String = "",
            isEncrypted: Boolean = true,
            useBiometric: Boolean = false
        ): EncryptedDocument {
            val now = System.currentTimeMillis()
            return EncryptedDocument(
                id = id,
                title = title,
                content = content,
                isEncrypted = isEncrypted,
                lastModified = Date(now),
                createdAt = now,
                lastModifiedTimestamp = now,
                useBiometric = useBiometric
            )
        }
    }
}

@Serializable
data class DocumentBackup(
    val version: Int,
    val createdAt: Long,
    val documents: List<BackupDocument>
)

@Serializable
data class BackupDocument(
    val id: String,
    val title: String,
    val encryptedContent: String,
    val createdAt: Long,
    val lastModifiedTimestamp: Long,
    val useBiometric: Boolean
)
