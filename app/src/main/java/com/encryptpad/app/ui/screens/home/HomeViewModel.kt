package com.encryptpad.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.encryptpad.app.data.model.EncryptedDocument
import com.encryptpad.app.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DocumentRepository(application)

    private val _documents = MutableStateFlow<List<EncryptedDocument>>(emptyList())
    val documents: StateFlow<List<EncryptedDocument>> = _documents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadDocuments() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.listDocuments()
                .onSuccess { docs ->
                    _documents.value = docs
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to load documents"
                }

            _isLoading.value = false
        }
    }

    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            repository.deleteDocument(documentId)
                .onSuccess {
                    loadDocuments()
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to delete document"
                }
        }
    }
}
