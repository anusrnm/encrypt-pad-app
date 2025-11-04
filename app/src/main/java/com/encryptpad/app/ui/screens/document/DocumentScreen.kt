package com.encryptpad.app.ui.screens.document

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(
    viewModel: DocumentViewModel,
    documentId: String?,
    onNavigateBack: () -> Unit
) {
    val content by viewModel.content.collectAsState()
    val fileName by viewModel.fileName.collectAsState()
    val isEncrypted by viewModel.isEncrypted.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordDialogMode by remember { mutableStateOf(PasswordDialogMode.ENCRYPT) }

    LaunchedEffect(documentId) {
        if (documentId != null) {
            passwordDialogMode = PasswordDialogMode.DECRYPT
            showPasswordDialog = true
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            kotlinx.coroutines.delay(1500)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fileName.ifEmpty { "New Document" }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            passwordDialogMode = if (isEncrypted) {
                                PasswordDialogMode.CHANGE_ENCRYPTION
                            } else {
                                PasswordDialogMode.ENCRYPT
                            }
                            showPasswordDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = if (isEncrypted) Icons.Filled.Lock else Icons.Outlined.Lock,
                            contentDescription = if (isEncrypted) "Encrypted" else "Not Encrypted",
                            tint = if (isEncrypted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            if (isEncrypted) {
                                passwordDialogMode = PasswordDialogMode.SAVE
                                showPasswordDialog = true
                            } else {
                                viewModel.saveDocument(null)
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = fileName,
                            onValueChange = { viewModel.updateFileName(it) },
                            label = { Text("File Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            singleLine = true
                        )

                        BasicTextField(
                            value = content,
                            onValueChange = { viewModel.updateContent(it) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (content.isEmpty()) {
                                        Text(
                                            text = "Start typing...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            if (saveSuccess) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text("Document saved successfully!")
                }
            }
        }
    }

    if (showPasswordDialog) {
        PasswordDialog(
            mode = passwordDialogMode,
            onDismiss = {
                showPasswordDialog = false
                if (passwordDialogMode == PasswordDialogMode.DECRYPT && documentId != null) {
                    onNavigateBack()
                }
            },
            onConfirm = { password ->
                when (passwordDialogMode) {
                    PasswordDialogMode.ENCRYPT -> {
                        viewModel.setEncryption(true, password)
                    }
                    PasswordDialogMode.DECRYPT -> {
                        if (documentId != null) {
                            viewModel.loadDocument(documentId, password)
                        }
                    }
                    PasswordDialogMode.SAVE -> {
                        viewModel.saveDocument(password)
                    }
                    PasswordDialogMode.CHANGE_ENCRYPTION -> {
                        viewModel.setEncryption(!isEncrypted, password)
                    }
                }
                showPasswordDialog = false
            }
        )
    }
}

enum class PasswordDialogMode {
    ENCRYPT, DECRYPT, SAVE, CHANGE_ENCRYPTION
}

@Composable
fun PasswordDialog(
    mode: PasswordDialogMode,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val title = when (mode) {
        PasswordDialogMode.ENCRYPT -> "Encrypt Document"
        PasswordDialogMode.DECRYPT -> "Enter Password"
        PasswordDialogMode.SAVE -> "Save Encrypted Document"
        PasswordDialogMode.CHANGE_ENCRYPTION -> "Change Encryption"
    }

    val needsConfirmation = mode == PasswordDialogMode.ENCRYPT || mode == PasswordDialogMode.CHANGE_ENCRYPTION

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        showError = false
                    },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (needsConfirmation) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            showError = false
                        },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (needsConfirmation && password != confirmPassword) {
                        showError = true
                    } else if (password.isNotEmpty()) {
                        onConfirm(password)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
