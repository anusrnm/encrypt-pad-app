package com.example.encryptpadapp

import android.app.Application

object PasswordManager {
    // Blocking demo prompt replacement: in a real app, present a secure UI and return the password.
    fun promptPasswordSync(app: Application): CharArray {
        // TODO: implement a secure, non-blocking prompt in the UI layer.
        return "change-me".toCharArray()
    }

    fun clear(pw: CharArray) {
        pw.fill('\u0000')
    }
}
