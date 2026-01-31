package com.encryptpad.app.di

import com.encryptpad.app.data.backup.BackupManager
import com.encryptpad.app.data.encryption.BiometricAuthManager
import com.encryptpad.app.data.encryption.EncryptionManager
import com.encryptpad.app.data.repository.DocumentRepository
import com.encryptpad.app.ui.screens.document.DocumentViewModel
import com.encryptpad.app.ui.screens.home.HomeViewModel
import com.encryptpad.app.ui.screens.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Data layer
    single { EncryptionManager(androidContext()) }
    single { DocumentRepository(androidContext(), get()) }
    single { BiometricAuthManager(androidContext()) }
    single { BackupManager(androidContext(), get()) }
    
    // ViewModels
    viewModel { HomeViewModel(get(), get()) }
    viewModel { params -> DocumentViewModel(params.get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
