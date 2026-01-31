package com.encryptpad.app.data.model

import java.util.Date

data class EncryptedDocument(
    val id: String,
    val name: String,
    val content: String,
    val isEncrypted: Boolean,
    val lastModified: Date,
    val filePath: String? = null
)
