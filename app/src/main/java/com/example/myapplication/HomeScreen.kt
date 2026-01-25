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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.platform.LocalUriHandler


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
    val petId = selectedCompanion?.name ?: "CAT"
    val petName = selectedCompanion?.title ?: "Your Pet"
    val petEmoji = selectedCompanion?.emoji ?: "🐱"
    val xpGoal = 100
    val uriHandler = LocalUriHandler.current

    var petProgress by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var streakDays by remember { mutableIntStateOf(0) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    // State for the Mentor Data
    var assignedMentor by remember { mutableStateOf<MentorProfile?>(null) }
    var xp by remember { mutableStateOf(0) }
    var totalXp by remember { mutableStateOf(0) }

    // 1. Listen for Pet Progress (Growth)
    LaunchedEffect(userId, petId) {
        if (userId != null) {
            val docRef = db.collection("users")
                .document(userId)
                .collection("companion")
                .document(petId)

            docRef.addSnapshotListener { snapshot, e ->
                if (snapshot != null && snapshot.exists()) {
                    petProgress = (snapshot.getLong("petProgress") ?: 0L).toInt()
                    level = (snapshot.getLong("level") ?: 1L).toInt()
                }
            }
        }
    }

    // 2. Fetch Mentor & Companion Stats
    LaunchedEffect(key1 = userId, key2 = selectedCompanion) {
        val uid = userId ?: return@LaunchedEffect

        try {
            // A. Fetch Student Document
            val userDoc = db.collection("users").document(uid).get().await()
            val currentMentorId = userDoc.getString("currentMentor")

            Log.d("MentorDebug", "Current Mentor Id: $currentMentorId")

            if (!currentMentorId.isNullOrEmpty()) {
                // CASE 1: Student ALREADY has a mentor. Fetch and SHOW.
                val mentorDoc = db.collection("users").document(currentMentorId).get().await()

                if (mentorDoc.exists()) {
                    val mentorUid = mentorDoc.id
                    assignedMentor = mentorDoc.toObject(MentorProfile::class.java)?.copy(uid = mentorUid)
                }
            }
            // CASE 2: If currentMentorId is empty, we do NOTHING.
            // assignedMentor remains null, so the card stays hidden.

            // B. Fetch Companion Data
            if (selectedCompanion != null) {
                val compDoc = db.collection("users").document(uid)
                    .collection("companion").document(selectedCompanion.title)
                    .get().await()

                val data = compDoc.data
                if (data != null) {
                    xp = (data["xp"] as? Long)?.toInt() ?: 0
                    totalXp = (data["totalXp"] as? Long)?.toInt() ?: 0
                }
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching data", e)
        }
    }

    val progressAnimation by animateFloatAsState(
        targetValue = (petProgress.toFloat() / xpGoal.toFloat()).coerceIn(0f, 1f),
        label = "ProgressAnimation"
    )

    Scaffold(
        containerColor = CleanColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Home", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CleanColors.Background)
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
            // ... (Streak Badge and Pet Card code remains the same) ...

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
                    Box(
                        modifier = Modifier.size(110.dp).clip(CircleShape).background(CleanColors.Background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(petEmoji, fontSize = 56.sp)
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(petName, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp), color = CleanColors.TextPrimary)
                    Text("Level $level", style = MaterialTheme.typography.titleMedium.copy(color = CleanColors.TextSecondary, fontWeight = FontWeight.Medium, fontSize = 18.sp))
                    Spacer(Modifier.height(24.dp))
                    LinearProgressIndicator(
                        progress = { progressAnimation },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = CleanColors.Accent,
                        trackColor = CleanColors.ProgressTrack
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$petProgress XP", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = CleanColors.TextSecondary)
                        Text("$xpGoal XP", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = CleanColors.TextSecondary)
                    }
                }
            }

            // 3. Play Button
            Button(
                onClick = { navController.navigate("companion_details") },
                modifier = Modifier.fillMaxWidth().height(60.dp).shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CleanColors.TextPrimary, contentColor = Color.White)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text("Play with $petName", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp))
            }

            Spacer(Modifier.height(4.dp))

            // --- CHANGED SECTION START ---
            // Only display the "Your Mentor" section if a mentor is actually assigned
            if (assignedMentor != null) {
                Text("Your Mentor", style = MaterialTheme.typography.titleSmall)

                MentorDetailCard(
                    mentor = assignedMentor!!,
                    navController = navController,
                    currentStudentId = userId ?: ""
                )
            }
            // --- CHANGED SECTION END ---

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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


@Composable
fun MentorDetailCard(
    mentor: MentorProfile,
    navController: NavController,
    currentStudentId: String
) {
    // 1. Surface container matching the "Explore Resources" ActionRow style
    Surface(
        onClick = {
            val mentorId = mentor.uid
            val studentId = currentStudentId
            if (studentId.isNotEmpty() && mentorId.isNotEmpty()) {
                val chatId = if (studentId < mentorId) "${studentId}_${mentorId}" else "${mentorId}_${studentId}"
                navController.navigate("chat_screen/$chatId/$studentId")
            }
        },
        shape = RoundedCornerShape(20.dp),
        color = CleanColors.CardBackground,
        border = BorderStroke(1.dp, CleanColors.Border),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 2. Circular Avatar with background matching the Search icon in ActionRow
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(CleanColors.Background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mentor.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CleanColors.TextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 3. Mentor Info Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mentor.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = CleanColors.TextPrimary
                )

                if (mentor.bio.isNotEmpty()) {
                    Text(
                        text = mentor.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanColors.TextSecondary,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(8.dp))

                // 4. Focus area chips matching the "Resources" tags
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    mentor.supportAreas.take(2).forEach { area ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF5F5F5) // Light gray from photo_6310023668066619172_x
                        ) {
                            Text(
                                text = area,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = CleanColors.TextSecondary
                                )
                            )
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Call Button
                IconButton(
                    onClick = {
                        navController.navigate("calling_screen/${mentor.name}")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call",
                        tint = CleanColors.TextPrimary, // Darker color for emphasis
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        val mentorId = mentor.uid
                        val studentId = currentStudentId
                        if (studentId.isNotEmpty() && mentorId.isNotEmpty()) {
                            val chatId = if (studentId < mentorId) "${studentId}_${mentorId}" else "${mentorId}_${studentId}"
                            navController.navigate("chat_screen/$chatId/$studentId")
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat",
                        tint = CleanColors.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            // --- END MODIFIED ACTIONS ---
        }
    }
}

