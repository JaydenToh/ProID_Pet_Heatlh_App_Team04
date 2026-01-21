package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseCompanionScreen(
    navController: NavController,
    appState: AppState
) {
    val selected = appState.selectedCompanion
    val scope = rememberCoroutineScope()
    val firebaseHelper = remember { FirebaseHelper() }  // Using FirebaseHelper to save companion data

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Companion") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("‹") }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = {
                        val pet = selected ?: return@Button

                        val currentUser = FirebaseAuth.getInstance().currentUser

                        if (currentUser != null) {
                            val userId = currentUser.uid // Use userId from FirebaseAuth

                            // Save selected companion to Firestore using FirebaseHelper
                            scope.launch {
                                try {
                                    firebaseHelper.saveCompanionState(
                                        userId = userId,
                                        selectedCompanion = pet.name,
                                        level = 1, // Starting level
                                        xp = 0,    // Starting XP
                                        xpGoal = 100, // XP goal
                                        foodBasics = 0 // Food basics for the companion
                                    )

                                    // Log the successful action
                                    Log.d("Navigation", "Companion saved and navigating to home")

                                    // Navigate to Home and clear back stack up to the student dashboard
                                    navController.navigate("home") {
                                        popUpTo("student_dashboard") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } catch (e: Exception) {
                                    // Handle Firestore errors
                                    Log.e("Firestore", "Error saving companion data", e)
                                }
                            }
                        }
                    },
                    enabled = selected != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Start Journey")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Pick a companion that will grow with\nyou on your wellness journey.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(Companion.entries) { pet ->
                    CompanionRow(
                        pet = pet,
                        selected = pet == selected,
                        onClick = { appState.selectedCompanion = pet }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompanionRow(
    pet: Companion,
    selected: Boolean,
    onClick: () -> Unit
) {

    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderWidth = if (selected) 2.dp else 1.dp

    val bgColor =
        if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clickable { onClick() },
        color = bgColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = pet.emoji, style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = pet.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = pet.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
