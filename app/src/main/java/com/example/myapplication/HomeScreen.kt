package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Define a simple, clean color palette locally to override the purple theme
object CleanColors {
    val Background = Color(0xFFF8F9FA) // Very light gray (almost white)
    val CardBackground = Color(0xFFFFFFFF) // Pure white
    val TextPrimary = Color(0xFF1A1C1E) // Soft Black
    val TextSecondary = Color(0xFF6C757D) // Medium Gray
    val Accent = Color(0xFFFFA726) // Soft Orange for the Pet/Streak (Warmth)
    val ProgressTrack = Color(0xFFEEEEEE)
    val Border = Color(0xFFE0E0E0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    appState: AppState
) {
    val selectedCompanion = appState.selectedCompanion
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    val petName = selectedCompanion?.title ?: "Your Pet"
    val petEmoji = selectedCompanion?.emoji ?: "🐱"
    val petStageText = "Stage 1/5"
    val xpGoal = 100

    var xp by remember { mutableStateOf(0) }
    var streakDays by remember { mutableStateOf(0) }
    var totalXp by remember { mutableStateOf(0) }

    // (Your existing Fetch logic remains here)
    LaunchedEffect(key1 = selectedCompanion) {
        try {
            // Mock data for preview, ensure you use real UID in prod
            val doc = db.collection("companion").document("yourUid").get().await()
            val data = doc.data
            if (data != null) {
                xp = (data["xp"] as? Int) ?: 0
                totalXp = (data["totalXp"] as? Int) ?: 0
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching companion data: ", e)
        }
    }

    Scaffold(
        containerColor = CleanColors.Background, // Removes the default background color
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Home",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = CleanColors.TextPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CleanColors.Background
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp), // Increased side padding for a cleaner look
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. Streak Badge (Clean Pill Shape)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CleanColors.CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CleanColors.Border),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$streakDays Day Streak",
                            style = MaterialTheme.typography.labelLarge,
                            color = CleanColors.TextPrimary
                        )
                    }
                }
            }

            // 2. Main Pet Card (White, Clean, Shadow)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CleanColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pet Emoji Circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(CleanColors.Background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(petEmoji, fontSize = 48.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = petName,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = CleanColors.TextPrimary
                    )
                    Text(
                        text = petStageText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CleanColors.TextSecondary
                    )

                    Spacer(Modifier.height(20.dp))

                    // Progress Bar (Slimmer and cleaner)
                    LinearProgressIndicator(
                        progress = (xp.toFloat() / xpGoal.toFloat()).coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CleanColors.Accent, // Orange/Yellow instead of Purple
                        trackColor = CleanColors.ProgressTrack
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "$xp XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = CleanColors.TextSecondary
                        )
                        Text(
                            "$xpGoal XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = CleanColors.TextSecondary
                        )
                    }
                }
            }

            // 3. Main Action Button (Interact)
            // Changed from a big box to a clean button that stands out slightly
            Button(
                onClick = { navController.navigate("companion_details") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CleanColors.TextPrimary, // Black/Dark Grey button is very modern
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Play with $petName", style = MaterialTheme.typography.titleSmall)
            }

            Spacer(Modifier.height(8.dp))

            // 4. Secondary Actions (List Style)
            // Instead of chunky buttons, we use clean rows
            Text(
                "Menu",
                style = MaterialTheme.typography.titleSmall,
                color = CleanColors.TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )

            ActionRow(
                title = "Request a Mentor",
                icon = Icons.Default.School,
                onClick = { navController.navigate("mentor_request") }
            )

            ActionRow(
                title = "Explore Resources",
                icon = Icons.Default.Search,
                onClick = { navController.navigate("resources") }
            )
        }
    }
}

// Helper Component for cleaner list items
@Composable
fun ActionRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = CleanColors.CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, CleanColors.Border),
        modifier = Modifier.fillMaxWidth().height(60.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CleanColors.TextPrimary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = CleanColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = CleanColors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}