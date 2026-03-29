package com.encryptpad.app.ui.state

import com.encryptpad.app.data.model.EncryptedDocument

// Home Screen UI State
sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(
        val documents: List<EncryptedDocument>,
        val searchQuery: String = "",
        val sortOption: SortOption = SortOption.DATE_DESC
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

// Document Screen UI State
sealed class DocumentUiState {
    data object Loading : DocumentUiState()
    data object PasswordRequired : DocumentUiState()
    data class Unlocked(
        val document: EncryptedDocument,
        val content: String,
        val isModified: Boolean = false
    ) : DocumentUiState()
    data class Error(val message: String) : DocumentUiState()
}

// Sort options for documents
enum class SortOption(val displayName: String) {
    DATE_DESC("Newest first"),
    DATE_ASC("Oldest first"),
    NAME_ASC("Name A-Z"),
    NAME_DESC("Name Z-A")
}
