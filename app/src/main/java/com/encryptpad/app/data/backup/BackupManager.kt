package com.encryptpad.app.data.backup

import android.content.Context
import android.net.Uri
import com.encryptpad.app.data.model.BackupDocument
import com.encryptpad.app.data.model.DocumentBackup
import com.encryptpad.app.data.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

class BackupManager(
    private val context: Context,
    private val documentRepository: DocumentRepository
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val KEY_LENGTH = 256
        private const val ITERATION_COUNT = 100000
        private const val GCM_TAG_LENGTH = 128
        private const val SALT_LENGTH = 16
        private const val IV_LENGTH = 12
    }
    
    suspend fun exportBackup(uri: Uri, password: String) = withContext(Dispatchers.IO) {
        val documents = documentRepository.getAllDocuments()
        val backupDocuments = documents.map { doc ->
            val rawContent = documentRepository.getRawContent(doc.id)
            BackupDocument(
                id = doc.id,
                title = doc.title,
                encryptedContent = rawContent,
                createdAt = doc.createdAt,
                lastModifiedTimestamp = doc.lastModifiedTimestamp,
                useBiometric = doc.useBiometric
            )
        }
        
        val backup = DocumentBackup(
            version = 1,
            createdAt = System.currentTimeMillis(),
            documents = backupDocuments
        )
        
        val jsonData = json.encodeToString(backup)
        val encryptedData = encryptWithPassword(jsonData, password)
        
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(encryptedData.toByteArray())
        }
    }
    
    suspend fun importBackup(uri: Uri, password: String): Int = withContext(Dispatchers.IO) {
        val encryptedData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().readText()
        } ?: throw Exception("Could not read backup file")
        
        val jsonData = decryptWithPassword(encryptedData, password)
        val backup = json.decodeFromString<DocumentBackup>(jsonData)
        
        var importedCount = 0
        backup.documents.forEach { backupDoc ->
            try {
                documentRepository.importDocument(
                    title = backupDoc.title,
                    encryptedContent = backupDoc.encryptedContent,
                    createdAt = backupDoc.createdAt,
                    lastModifiedTimestamp = backupDoc.lastModifiedTimestamp,
                    useBiometric = backupDoc.useBiometric
                )
                importedCount++
            } catch (e: Exception) {
                // Skip documents that fail to import
            }
        }
        
        importedCount
    }
    
    private fun encryptWithPassword(data: String, password: String): String {
        val salt = ByteArray(SALT_LENGTH).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(IV_LENGTH).apply { SecureRandom().nextBytes(this) }
        
        val keySpec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val secretKeyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val keyBytes = secretKeyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        
        // Combine salt + iv + encrypted data
        val combined = salt + iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }
    
    private fun decryptWithPassword(encryptedData: String, password: String): String {
        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        
        val salt = combined.copyOfRange(0, SALT_LENGTH)
        val iv = combined.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
        val encrypted = combined.copyOfRange(SALT_LENGTH + IV_LENGTH, combined.size)
        
        val keySpec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val secretKeyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val keyBytes = secretKeyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val decryptedBytes = cipher.doFinal(encrypted)
        
        return String(decryptedBytes)
    }
}
