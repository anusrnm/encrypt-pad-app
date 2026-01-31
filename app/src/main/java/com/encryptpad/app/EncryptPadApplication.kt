package com.encryptpad.app

import android.app.Application
import com.encryptpad.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class EncryptPadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@EncryptPadApplication)
            modules(appModule)
        }
    }
}
