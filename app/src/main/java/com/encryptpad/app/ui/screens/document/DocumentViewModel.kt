package com.encryptpad.app.ui.screens.document

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.encryptpad.app.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DocumentRepository(application)

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _isEncrypted = MutableStateFlow(false)
    val isEncrypted: StateFlow<Boolean> = _isEncrypted.asStateFlow()

    private val _password = MutableStateFlow<String?>(null)

    private val _documentId = MutableStateFlow<String?>(null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    fun updateFileName(newFileName: String) {
        _fileName.value = newFileName
    }

    fun setEncryption(encrypted: Boolean, password: String?) {
        _isEncrypted.value = encrypted
        _password.value = password
    }

    fun loadDocument(documentId: String, password: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.loadDocument(documentId, password)
                .onSuccess { document ->
                    _documentId.value = document.id
                    _content.value = document.content
                    _fileName.value = document.name
                    _isEncrypted.value = document.isEncrypted
                    _password.value = password
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to load document"
                }

            _isLoading.value = false
        }
    }

    fun saveDocument(password: String?) {
        viewModelScope.launch {
            if (_fileName.value.isEmpty()) {
                _error.value = "Please enter a file name"
                return@launch
            }

            _isLoading.value = true
            _error.value = null
            _saveSuccess.value = false

            val effectivePassword = password ?: _password.value

            repository.saveDocument(
                name = _fileName.value,
                content = _content.value,
                isEncrypted = _isEncrypted.value,
                password = effectivePassword,
                documentId = _documentId.value
            )
                .onSuccess { document ->
                    _documentId.value = document.id
                    _saveSuccess.value = true
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to save document"
                }

            _isLoading.value = false
        }
    }
}
