package com.encryptpad.app.data.encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptionManager {
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
    }

    private fun getOrCreateSecretKey(alias: String): SecretKey {
        val existingKey = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createSecretKey(alias)
    }

    private fun createSecretKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM, ANDROID_KEY_STORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(ENCRYPTION_BLOCK_MODE)
            .setEncryptionPaddings(ENCRYPTION_PADDING)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    fun encrypt(plainText: String, password: String): String {
        try {
            val keyAlias = "encryptpad_key_${password.hashCode()}"
            val secretKey = getOrCreateSecretKey(keyAlias)

            val cipher = Cipher.getInstance("$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

            // Combine IV and encrypted data
            val combined = iv + encryptedBytes
            return Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            throw EncryptionException("Encryption failed: ${e.message}", e)
        }
    }

    fun decrypt(encryptedText: String, password: String): String {
        try {
            val keyAlias = "encryptpad_key_${password.hashCode()}"
            
            if (!keyStore.containsAlias(keyAlias)) {
                throw EncryptionException("Incorrect password or key not found")
            }

            val secretKey = getOrCreateSecretKey(keyAlias)
            val combined = Base64.getDecoder().decode(encryptedText)

            // Extract IV and encrypted data
            val iv = combined.copyOfRange(0, 12)
            val encryptedBytes = combined.copyOfRange(12, combined.size)

            val cipher = Cipher.getInstance("$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw EncryptionException("Decryption failed: ${e.message}", e)
        }
    }

    fun deleteKey(password: String) {
        val keyAlias = "encryptpad_key_${password.hashCode()}"
        if (keyStore.containsAlias(keyAlias)) {
            keyStore.deleteEntry(keyAlias)
        }
    }
}

class EncryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)
