package com.encryptpad.app.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.encryptpad.app.data.backup.BackupManager
import com.encryptpad.app.data.encryption.BiometricAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val biometricAvailable: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(
    private val backupManager: BackupManager,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        checkBiometricAvailability()
    }
    
    private fun checkBiometricAvailability() {
        _uiState.value = _uiState.value.copy(
            biometricAvailable = biometricAuthManager.canAuthenticate()
        )
    }
    
    fun exportBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, message = null)
            try {
                backupManager.exportBackup(uri, password)
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    message = "Backup exported successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    message = "Export failed: ${e.message}"
                )
            }
        }
    }
    
    fun importBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, message = null)
            try {
                val count = backupManager.importBackup(uri, password)
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    message = "Imported $count documents successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    message = "Import failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
