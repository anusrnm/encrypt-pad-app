package com.encryptpad.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encryptpad.app.data.encryption.BiometricAuthManager
import com.encryptpad.app.data.model.EncryptedDocument
import com.encryptpad.app.data.repository.DocumentRepository
import com.encryptpad.app.ui.state.HomeUiState
import com.encryptpad.app.ui.state.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: DocumentRepository,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var allDocuments: List<EncryptedDocument> = emptyList()
    
    val canUseBiometric: Boolean get() = biometricAuthManager.canAuthenticate()

    fun loadDocuments() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            repository.listDocuments()
                .onSuccess { docs ->
                    allDocuments = docs
                    val currentState = _uiState.value
                    val (query, sort) = if (currentState is HomeUiState.Success) {
                        currentState.searchQuery to currentState.sortOption
                    } else {
                        "" to SortOption.DATE_DESC
                    }
                    _uiState.value = HomeUiState.Success(
                        documents = filterAndSort(docs, query, sort),
                        searchQuery = query,
                        sortOption = sort
                    )
                }
                .onFailure { e ->
                    _uiState.value = HomeUiState.Error(e.message ?: "Failed to load documents")
                }
        }
    }
    
    fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(
                searchQuery = query,
                documents = filterAndSort(allDocuments, query, currentState.sortOption)
            )
        }
    }
    
    fun updateSortOption(option: SortOption) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(
                sortOption = option,
                documents = filterAndSort(allDocuments, currentState.searchQuery, option)
            )
        }
    }
    
    private fun filterAndSort(
        docs: List<EncryptedDocument>,
        query: String,
        sort: SortOption
    ): List<EncryptedDocument> {
        val filtered = if (query.isBlank()) docs else {
            docs.filter { it.title.contains(query, ignoreCase = true) }
        }
        return when (sort) {
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.lastModifiedTimestamp }
            SortOption.DATE_ASC -> filtered.sortedBy { it.lastModifiedTimestamp }
            SortOption.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
        }
    }

    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            repository.deleteDocument(documentId)
                .onSuccess {
                    loadDocuments()
                }
                .onFailure { e ->
                    _uiState.value = HomeUiState.Error(e.message ?: "Failed to delete document")
                }
        }
    }
}
