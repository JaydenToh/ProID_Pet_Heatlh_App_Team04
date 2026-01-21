package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import com.airbnb.lottie.compose.*
import com.airbnb.lottie.LottieComposition
import com.google.firebase.firestore.FieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionDetailScreen(
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
    var xp = 0
    var foodBasics = 0
    var pettingCount by remember { mutableStateOf(0) } // Track petting count

    // Placeholder data
    var streakDays = 0
    var totalXp = 0
    var completed = 0

    // Fetch companion data from Firebase inside LaunchedEffect
    LaunchedEffect(key1 = selectedCompanion) {
        try {
            val doc = db.collection("companion").document("yourUid") // Use actual user UID here
                .get()
                .await()
            val data = doc.data
            if (data != null) {
                xp = data["xp"] as? Int ?: 0
                totalXp = data["totalXp"] as? Int ?: 0
                foodBasics = data["foodBasics"] as? Int ?: 0
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching companion data: ", e)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Companion Details") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cat Interaction Area with clickable pet (petting)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Handle pet interaction (petting) logic
                        pettingCount += 1
                        xp += 10  // Increase XP for petting
                        foodBasics -= 1  // Decrease food for interaction
                    }
            ) {
                // Cat Animation (Lottie)
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cat_petting))
                LottieAnimation(
                    modifier = Modifier.fillMaxWidth(),
                    composition = composition,
                    iterations = LottieConstants.IterateForever
                )
                Text(
                    text = "Click to interact with your pet",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Display Pet Info
            Text(text = "Your Wellness Pet", style = MaterialTheme.typography.labelSmall)
            Text(text = petName, style = MaterialTheme.typography.titleMedium)
            Text(text = petStageText, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(10.dp))

            // XP Progress Bar
            LinearProgressIndicator(
                progress = (xp.toFloat() / xpGoal.toFloat()).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$xp XP", style = MaterialTheme.typography.labelSmall)
                Text("$xpGoal XP", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feed Pet Button
            Button(
                onClick = {
                    if (foodBasics > 0) {
                        foodBasics -= 1
                        xp += 20 // Increase XP for feeding
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Feed Pet (Food Left: $foodBasics)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Level Up Pet Button
            Button(
                onClick = {
                    if (xp >= xpGoal) {
                        // Handle leveling up logic
                        xp = 0 // Reset XP
                        // Increment pet level (stored in Firestore)
                        scope.launch {
                            db.collection("companion")
                                .document("yourUid") // Replace with user ID
                                .update("level", FieldValue.increment(1))
                                .await()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Level Up Pet")
            }
        }
    }
}

