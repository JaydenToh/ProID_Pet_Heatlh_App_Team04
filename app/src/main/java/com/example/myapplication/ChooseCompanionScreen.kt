package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// --- Design System Colors ---
private val LightBackground = Color(0xFFF5F5F7)
private val CardWhite = Color(0xFFFFFFFF)
private val DarkButton = Color(0xFF1A1A1A)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF757575)
private val BorderUnselected = Color(0xFFE0E0E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseCompanionScreen(
    navController: NavController,
    appState: AppState
) {
    val selected = appState.selectedCompanion
    val scope = rememberCoroutineScope()
    // val firebaseHelper = remember { FirebaseHelper() }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = LightBackground
                )
            )

        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Spacer (Reduced weight to 0.5f to shift content up)
            Spacer(modifier = Modifier.weight(0.5f))

            // 2. Header
            Text(
                text = "Choose Your Companion",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = TextPrimary
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Pick a companion that will grow with\nyou on your wellness journey.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    color = TextSecondary
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // 3. The List of Cards (FILTERED)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Filter out Rabbit here
                Companion.entries.filter { it.title != "Rabbit" }.forEach { pet ->
                    DefinedCompanionCard(
                        pet = pet,
                        selected = pet == selected,
                        onClick = { appState.selectedCompanion = pet }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Button Section
            Button(
                onClick = {
                    val pet = selected ?: return@Button
                    val currentUser = FirebaseAuth.getInstance().currentUser

                    if (currentUser != null) {
                        val userId = currentUser.uid
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                        scope.launch {
                            try {
                                // 1. Prepare the initial companion data
                                val initialCompanionData = mapOf(
                                    "foodBasics" to 0,
                                    "level" to 1,
                                    "petProgress" to 0,
                                    "xp" to 0,
                                    "xpGoal" to 100
                                )

                                // 2. Save to users -> {userId} -> companion -> {PET_NAME}
                                // We use .set() to ensure the document is created if it doesn't exist
                                db.collection("users")
                                    .document(userId)
                                    .collection("companion")
                                    .document(pet.title.uppercase()) // Matches "CAT" in your screenshot
                                    .set(initialCompanionData)
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "Companion ${pet.title} successfully created")

                                        // 3. Navigate only AFTER the save is successful
                                        navController.navigate("home") {
                                            popUpTo("student_dashboard") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Failed to save companion", e)
                                    }

                            } catch (e: Exception) {
                                Log.e("Firestore", "Error in scope", e)
                            }
                        }
                    }
                },
                enabled = selected != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkButton,
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Start Journey",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // 5. Bottom Spacer (Heavier weight (1f) pushes content upwards)
            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DefinedCompanionCard(
    pet: Companion,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) DarkButton else BorderUnselected
    val borderWidth = if (selected) 2.5.dp else 1.dp
    val elevation = if (selected) 6.dp else 2.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pet.emoji,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 42.sp)
            )

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = pet.subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                )
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(DarkButton, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}