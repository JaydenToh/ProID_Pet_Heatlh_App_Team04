package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddIcCall
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.SpatialAudioOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    appState: AppState
) {
// 1. Initialize Firebase services at the very top
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    val firebaseHelper = remember { FirebaseHelper() }
    val scope = rememberCoroutineScope()

    // 2. Define state variables for data that will change
    var assignedMentor by remember { mutableStateOf<MentorProfile?>(null) }
    var xp by remember { mutableStateOf(0) }
    var totalXp by remember { mutableStateOf(0) }
    var streakDays by remember { mutableStateOf(0) }
    var completed by remember { mutableStateOf(0) }
    var isMatching by remember { mutableStateOf(false) }

    val selectedCompanion = appState.selectedCompanion
    val petName = selectedCompanion?.title ?: "Your Pet"
    val petEmoji = selectedCompanion?.emoji ?: "🐱"
    val petStageText = "Stage 1/5"
    val xpGoal = 100

    // Fetch companion data from Firestore inside LaunchedEffect
    LaunchedEffect(key1 = userId, key2 = selectedCompanion) {
        val uid = userId ?: return@LaunchedEffect

        try {
            // Fetch Student/Mentee Document
            val userDoc = db.collection("users").document(uid).get().await()

            // Get the mentor ID stored in the student's document
            val currentMentorId = userDoc.getString("currentMentor")
            Log.d("MentorDebug", "Mentor Id: $currentMentorId")
            if (currentMentorId != null) {

                val mentorDoc = db.collection("users").document("SLxy1G2nRfM9ggUbQV9rGjbEEj03").get().await()
                Log.d("MentorDebug", "Mentor Doc: $mentorDoc")

                val mentorUid = mentorDoc.id

                assignedMentor = mentorDoc.toObject(MentorProfile::class.java)?.copy(uid = mentorUid)
                Log.d("MentorDebug", "Mentor INtital: $assignedMentor")

            }

            // Fetch Companion Data
            if (selectedCompanion != null) {
                val compDoc = db.collection("users").document(uid)
                    .collection("companion").document(selectedCompanion.title)
                    .get().await()

                val data = compDoc.data
                if (data != null) {
                    // Use Long cast for safety
                    xp = (data["xp"] as? Long)?.toInt() ?: 0
                    totalXp = (data["totalXp"] as? Long)?.toInt() ?: 0
                }
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching data", e)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Home") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "🔥  $streakDays day streak",
                style = MaterialTheme.typography.titleSmall
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(petEmoji, style = MaterialTheme.typography.headlineSmall)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your Wellness Pet",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = petName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = petStageText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = (xp.toFloat() / xpGoal.toFloat()).coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "$xp XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$xpGoal XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Pet logo button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(16.dp)
                    .clickable {
                        navController.navigate("companion_details")
                    },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Click to interact with your pet"
                    )
                }
            }
            // Add Shop Button (link to Shop Screen)
            Button(
                onClick = { navController.navigate("shop") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Go to Shop")
            }

            // Resources Button (link to ResourcesScreen)
            Button(
                onClick = { navController.navigate("resources") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Explore Resources")
            }
            Text("Your Mentor", style = MaterialTheme.typography.titleSmall)
            Log.d("MentorDebug", "Mentor: $assignedMentor")

            if (assignedMentor != null) {
                Log.d("MentorDebug", "Mentor: $assignedMentor")
                MentorDetailCard(
                    mentor = assignedMentor!!,
                    navController = navController,
                    currentStudentId = userId ?: "" // Pass the userId here
                )
            } else {
                Text("Looking for your mentor...", style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total XP",
                    value = totalXp.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Completed",
                    value = completed.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MentorDetailCard(
    mentor: MentorProfile,
    navController: NavController, // Add this parameter
    currentStudentId: String // Add this parameter
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), // Ensure row takes full width
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mentor Icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape) // Rounded icon
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(mentor.name.take(1), color = Color.White)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) { // Use weight to push the icon to the right
                Text(text = mentor.name, style = MaterialTheme.typography.titleMedium)
                Text(text = mentor.bio, style = MaterialTheme.typography.bodySmall, maxLines = 1)

                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    mentor.supportAreas.take(2).forEach { area ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = area,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Chat Button
            IconButton(
                onClick = {
                    val mentorId = mentor.uid // Dynamic Mentor ID from DocumentId
                    val studentId = currentStudentId

                    // Prevent navigation if IDs are missing
                    if (studentId.isNotEmpty() && mentorId.isNotEmpty()) {
                        val chatId = if (studentId < mentorId) "${studentId}_${mentorId}" else "${mentorId}_${studentId}"
                        navController.navigate("chat_screen/$chatId/$studentId")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = "Chat with Mentor",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
