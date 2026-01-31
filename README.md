# EncryptPad - Encrypted Text Editor

An Android application built with Jetpack Compose that allows you to create, edit, and save encrypted text files securely.

## Features

- 📝 **Text Editor**: Clean and intuitive text editing interface
- 🔒 **Encryption**: AES-256-GCM encryption using Android Keystore
- 📱 **Modern UI**: Built with Jetpack Compose and Material 3 Design
- 💾 **File Management**: Save and manage multiple documents
- 🔐 **Password Protection**: Secure your documents with passwords
- 🌙 **Dynamic Theming**: Supports light/dark themes with Material You

## Technology Stack

- **Language**: Kotlin 2.0.20 (latest)
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Security**: Android Keystore, AES-256-GCM encryption
- **Navigation**: Jetpack Navigation Compose
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)

## Project Structure

```
app/
├── data/
│   ├── encryption/
│   │   └── EncryptionManager.kt       # Handles AES encryption/decryption
│   ├── model/
│   │   └── EncryptedDocument.kt       # Document data model
│   └── repository/
│       └── DocumentRepository.kt       # Data layer for document operations
├── ui/
│   ├── navigation/
│   │   └── EncryptPadNavigation.kt    # Navigation setup
│   ├── screens/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt          # Document list screen
│   │   │   └── HomeViewModel.kt
│   │   └── document/
│   │       ├── DocumentScreen.kt      # Text editor screen
│   │       └── DocumentViewModel.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── MainActivity.kt
```

## Security Features

- **AES-256-GCM Encryption**: Industry-standard encryption algorithm
- **Android Keystore**: Keys are stored securely in hardware-backed keystore
- **No Plain Text Storage**: Encrypted documents are never stored in plain text
- **Password-based Access**: Each encrypted document requires password to decrypt

## Building the Project

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK 35
- Gradle 8.7.0

### Build Instructions

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd encrypt-pad-app
   ```

2. Open the project in Android Studio

3. Sync Gradle files

4. Run the app on an emulator or physical device (API 26+)

### Gradle Wrapper

If you don't have Gradle installed, use the wrapper:

**Windows:**
```cmd
gradlew.bat assembleDebug
```

**Linux/Mac:**
```bash
./gradlew assembleDebug
```

### Quick Compilation (without building APK)

For faster error checking without building the full APK:

**Compile Kotlin sources only** (fastest for syntax checking):
```cmd
# Windows
gradlew.bat compileDebugKotlin

# Linux/Mac
./gradlew compileDebugKotlin
```

**Compile all sources** (includes Java/Kotlin):
```cmd
# Windows
gradlew.bat compileDebugSources

# Linux/Mac
./gradlew compileDebugSources
```

**Run lint checks**:
```cmd
# Windows
gradlew.bat lintDebug

# Linux/Mac
./gradlew lintDebug
```

**Build without assembling APK**:
```cmd
# Windows
gradlew.bat build -x assembleDebug

# Linux/Mac
./gradlew build -x assembleDebug
```

**Clean output** (for better readability):
```cmd
# Add --console=plain flag to any command
gradlew.bat compileDebugKotlin --console=plain
```

## Usage

1. **Create New Document**: Tap the + button on the home screen
2. **Edit Document**: Type your content in the editor
3. **Enable Encryption**: Tap the lock icon and set a password
4. **Save Document**: Tap the save icon
5. **Open Document**: Tap any document from the list (enter password if encrypted)
6. **Delete Document**: Swipe or tap the delete icon on any document

## Dependencies

- AndroidX Core KTX 1.15.0
- Jetpack Compose BOM 2024.10.01
- Material 3 1.3.1
- Navigation Compose 2.8.3
- Lifecycle ViewModel Compose 2.8.7
- Security Crypto 1.1.0-alpha06
- Datastore Preferences 1.1.1

## License

See LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
