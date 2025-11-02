package com.example.encryptpadapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm: EditorViewModel = viewModel()
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    EditorScreen(vm)
                }
            }
        }
    }
}

@Composable
fun EditorScreen(vm: EditorViewModel) {
    val text by vm.text.collectAsState()
    val saving by vm.isSaving.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { vm.onTextChanged(it) },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            placeholder = { Text("Start typing...") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { vm.requestSave() }) { Text("Save now") }
            Text(text = if (saving) "Saving..." else "Idle")
        }
    }
}
