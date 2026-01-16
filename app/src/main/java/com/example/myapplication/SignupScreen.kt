package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onBack: () -> Unit,
    onCreateAccount: (username: String, password: String, displayName: String, age: Int, stayAnonymous: Boolean) -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var ageText by rememberSaveable { mutableStateOf("") }
    var stayAnonymous by rememberSaveable { mutableStateOf(false) }

    val ageInt = ageText.toIntOrNull()
    val canCreate =
        username.isNotBlank() &&
                password.isNotBlank() &&
                displayName.isNotBlank() &&
                (ageInt != null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(10.dp))

            Text(
                text = "Join WellnessConnect as a student",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(22.dp))

            FieldLabel("Username *")
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Choose a username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            FieldLabel("Password *")
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Choose a password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            FieldLabel("Name / Display Name *")
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                placeholder = { Text("Enter your name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            FieldLabel("Age *")
            OutlinedTextField(
                value = ageText,
                onValueChange = { input ->
                    ageText = input.filter { it.isDigit() }.take(3)
                },
                placeholder = { Text("Enter your age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                supportingText = {
                    if (ageText.isNotBlank() && ageInt == null) {
                        Text("Please enter a valid number.")
                    }
                }
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = stayAnonymous,
                    onCheckedChange = { stayAnonymous = it }
                )
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Stay Anonymous",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Your name will be hidden from\nmentors",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = {
                    onCreateAccount(
                        username.trim(),
                        password,
                        displayName.trim(),
                        ageInt ?: 0,
                        stayAnonymous
                    )
                },
                enabled = canCreate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Create Account")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    )
}
