package com.example.myapplication

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddIcCall
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.SpatialAudioOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// --- Consistent Design System Colors ---
object CleanColors {
    val Background = Color(0xFFF5F5F7)
    val CardBackground = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF1A1A1A)
    val TextSecondary = Color(0xFF757575)
    val Accent = Color(0xFF1A1A1A)
    val ProgressTrack = Color(0xFFE0E0E0)
    val Border = Color(0xFFE0E0E0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    appState: AppState
) {
    val selectedCompanion = appState.selectedCompanion

    // FIX 1: Use .name (e.g., "CAT") instead of .title ("Cat") to match Firestore ID
    val petId = selectedCompanion?.name ?: "CAT"

    val petName = selectedCompanion?.title ?: "Your Pet"
    val petEmoji = selectedCompanion?.emoji ?: "🐱"
    val xpGoal = 100

    // STATE: Tracking 'petProgress' (Growth) and 'level'
    var petProgress by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var streakDays by remember { mutableIntStateOf(0) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    // Real-time listener
    LaunchedEffect(userId, petId) {
        if (userId != null) {
            val docRef = db.collection("users")
                .document(userId)
                .collection("companion")
                .document(petId) // This must be "CAT"

            docRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("HomeScreen", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    // FIX 2: Explicitly fetch 'petProgress' for the XP bar
                    petProgress = (snapshot.getLong("petProgress") ?: 0L).toInt()
                    level = (snapshot.getLong("level") ?: 1L).toInt()
                } else {
                    Log.d("HomeScreen", "Document does not exist yet")
                }
            }
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

    // Animate based on petProgress
    val progressAnimation by animateFloatAsState(
        targetValue = (petProgress.toFloat() / xpGoal.toFloat()).coerceIn(0f, 1f),
        label = "ProgressAnimation"
    )

    Scaffold(
        containerColor = CleanColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Home",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 1. Streak Badge
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CleanColors.CardBackground,
                    border = BorderStroke(1.dp, CleanColors.Border),
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$streakDays Day Streak",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CleanColors.TextPrimary
                            )
                        )
                    }
                }
            }

            // 2. Main Pet Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CleanColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pet Emoji Circle
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(CleanColors.Background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(petEmoji, fontSize = 56.sp)
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = petName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                        color = CleanColors.TextPrimary
                    )
                    Text(
                        text = "Level $level", // Display correct level
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CleanColors.TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { progressAnimation },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = CleanColors.Accent,
                        trackColor = CleanColors.ProgressTrack
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Display correct petProgress
                        Text(
                            "$petProgress XP",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = CleanColors.TextSecondary
                        )
                        Text(
                            "$xpGoal XP",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = CleanColors.TextSecondary
                        )
                    }
                }
            }

            // 3. Play Button
            Button(
                onClick = { navController.navigate("companion_details") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CleanColors.TextPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Play with $petName",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }

            Spacer(Modifier.height(4.dp))

            // 4. Menu Actions
            Text(
                "Menu",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = CleanColors.TextPrimary,
                modifier = Modifier.padding(start = 4.dp)
            )
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

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
}

// Helper Component
@Composable
fun ActionRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = CleanColors.CardBackground,
        border = BorderStroke(1.dp, CleanColors.Border),
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CleanColors.Background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CleanColors.TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = CleanColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = CleanColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
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
