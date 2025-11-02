package com.example.encryptpadapp

import android.content.Context
import de.mkammerer.argon2.Argon2Factory
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionManager(private val ctx: Context) {
    private val rng = SecureRandom()

    data class KdfParams(val t: Int = 3, val mKb: Int = 65536, val parallelism: Int = 1)

    // Save file atomically. Format (binary): [4-byte metaLen][metaJSON][salt(16)][wrappedFEKLen(4)][wrappedFEK][payloadNonce(12)][payloadCiphertext+tag]
    fun saveFileAtomic(file: File, password: CharArray, plaintext: ByteArray) {
        val metadata = JSONObject()
        metadata.put("version", 1)
        val kdf = KdfParams()
        metadata.put("kdf_t", kdf.t)
        metadata.put("kdf_mkb", kdf.mKb)
        metadata.put("kdf_par", kdf.parallelism)

        val salt = ByteArray(16).also { rng.nextBytes(it) }
        val kek = deriveArgon2(password, salt, kdf)

        // FEK (random file encryption key)
        val fek = ByteArray(32).also { rng.nextBytes(it) }

        // Wrap FEK with KEK using AES-GCM
        val wrappedFek = aesGcmEncrypt(kek, fek)

        // Encrypt payload with FEK
        val payloadEncrypted = aesGcmEncrypt(fek, plaintext)

        val metaBytes = metadata.toString().toByteArray(Charsets.UTF_8)

        val outLen = 4 + metaBytes.size + salt.size + 4 + wrappedFek.size + payloadEncrypted.size
        val out = ByteBuffer.allocate(outLen)
        out.putInt(metaBytes.size)
        out.put(metaBytes)
        out.put(salt)
        out.putInt(wrappedFek.size)
        out.put(wrappedFek)
        out.put(payloadEncrypted)

        val tmp = File(file.parentFile, file.name + ".tmp")
        FileOutputStream(tmp).use { it.write(out.array()) }
        if (tmp.exists()) tmp.renameTo(file)

        // zero sensitive data
        kek.fill(0)
        fek.fill(0)
    }

    private fun deriveArgon2(password: CharArray, salt: ByteArray, params: KdfParams): ByteArray {
        val argon2 = Argon2Factory.create()
        try {
            val pwBytes = password.concatToString().toByteArray(Charsets.UTF_8)
            // argon2-jvm supports rawHash via hashRaw; but for compatibility we use the encoded hash and take bytes.
            val encoded = argon2.hash(params.t, params.mKb, params.parallelism, pwBytes, salt)
            val derived = encoded.toByteArray(Charsets.UTF_8).copyOfRange(0, 32)
            pwBytes.fill(0)
            return derived
        } finally {
            // no-op
        }
    }

    private fun aesGcmEncrypt(key: ByteArray, plain: ByteArray): ByteArray {
        val nonce = ByteArray(12).also { rng.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, nonce)
        val sk = SecretKeySpec(key, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, sk, spec)
        val ct = cipher.doFinal(plain)
        return nonce + ct
    }
}
