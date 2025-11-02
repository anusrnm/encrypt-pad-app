package com.example.encryptpadapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class EditorViewModel(app: Application) : AndroidViewModel(app) {
    private val autoSaveDebounceMs = 1500L

    val text = MutableStateFlow("")
    val isSaving = MutableStateFlow(false)

    private var debounceJobToken = 0L

    private val file = File(getApplication<Application>().filesDir, "document.epad")
    private val encryption = EncryptionManager(getApplication())

    fun onTextChanged(newText: String) {
        text.value = newText
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        debounceJobToken += 1
        val token = debounceJobToken
        viewModelScope.launch {
            delay(autoSaveDebounceMs)
            if (token != debounceJobToken) return@launch
            requestSave()
        }
    }

    fun requestSave() {
        val current = text.value
        isSaving.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val password = PasswordManager.promptPasswordSync(getApplication())
                try {
                    encryption.saveFileAtomic(file, password, current.toByteArray(Charsets.UTF_8))
                } finally {
                    PasswordManager.clear(password)
                }
            } finally {
                isSaving.value = false
            }
        }
    }
}
