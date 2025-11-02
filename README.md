# encrypt-pad-app

Android-only secure text editor scaffold (Jetpack Compose) with envelope encryption and Argon2 KDF.

What is included
- Root Gradle files and `app` module
- Core Kotlin sources: `MainActivity`, `EditorViewModel`, `EncryptionManager`, `PasswordManager`
- Basic Compose UI and autosave wiring

Notes
- Argon2 is used via `argon2-jvm`. Tune Argon2 parameters on target devices.
- Envelope encryption: a random FEK encrypts the payload; FEK is wrapped by a password-derived KEK so password changes only re-wrap the FEK.
- Do not ship production secrets; review secure input methods and secure memory practices before release.

How to open
1. Open this folder in Android Studio.
2. Let Gradle sync and then run the `app` module on an emulator or device (minSdk 23).
