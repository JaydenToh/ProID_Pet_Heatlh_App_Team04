package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

// Reusing your color object for consistency
object CheckinColors {
    val Background = Color(0xFFF8F9FA)
    val CardBg = Color(0xFFFFFFFF)
    val Primary = Color(0xFF1A1C1E)
    val Selected = Color(0xFF1A1C1E)
    val Unselected = Color(0xFFF0F0F0)
    val Success = Color(0xFF66BB6A)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessCheckinScreen(
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // --- QUESTIONS DATA ---
    val questions = listOf(
        "How often do you feel nervous, anxious or on edge?",
        "How often are you unable to stop worrying?",
        "How often do you worry about different things?",
        "How often do you become easily annoyed and irritable?",
        "How often do you feel afraid, as if something bad might happen?"
    )
    val options = listOf("Not at all", "Few days a week", "All the time")

    // --- STATE ---
    // Maps Question Index -> Selected Answer String
    val selectedAnswers = remember { mutableStateMapOf<Int, String>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    // Check if all questions have an answer
    val allAnswered = questions.indices.all { selectedAnswers.containsKey(it) }

    // --- SUBMIT LOGIC ---
    fun submitSurvey() {
        if (userId == null) return
        isSubmitting = true

        val checkinData = hashMapOf(
            "date" to Date(),
            "type" to "anxiety_checkin",
            "answers" to selectedAnswers.toSortedMap().values.toList(), // Ensure order
            "questions" to questions
        )

        val userRef = db.collection("users").document(userId)
        val petRef = userRef.collection("companion").document("CAT")

        db.runBatch { batch ->
            // 1. Save Survey
            val newDoc = userRef.collection("checkins").document()
            batch.set(newDoc, checkinData)

            // 2. Reward XP
            batch.update(petRef, "xp", FieldValue.increment(50))
            batch.update(petRef, "petProgress", FieldValue.increment(10))
        }.addOnSuccessListener {
            isSubmitting = false
            isSuccess = true
        }.addOnFailureListener {
            isSubmitting = false
        }
    }

    Scaffold(
        containerColor = CheckinColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Check-in", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CheckinColors.Background
                )
            )
        }
    ) { padding ->
        if (isSuccess) {
            // --- SUCCESS SCREEN ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CheckinColors.Success,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Check-in Complete!",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "+50 XP Earned",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF42A5F5)
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = CheckinColors.Primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(200.dp).height(50.dp)
                ) {
                    Text("Back to Companion")
                }
            }
        } else {
            // --- SURVEY FORM ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "Please answer honestly so we can help track your wellness journey.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Render all questions
                questions.forEachIndexed { index, question ->
                    QuestionCard(
                        index = index + 1,
                        question = question,
                        options = options,
                        selectedOption = selectedAnswers[index],
                        onOptionSelected = { option ->
                            selectedAnswers[index] = option
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                }

                Spacer(Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = { submitSurvey() },
                    enabled = allAnswered && !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CheckinColors.Primary,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        navController.navigate("home")
                    } else {
                        Text(
                            text = if (allAnswered) "Submit Check-in" else "Answer All to Submit",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// --- HELPER COMPONENT: SINGLE QUESTION CARD ---
@Composable
fun QuestionCard(
    index: Int,
    question: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CheckinColors.CardBg),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Question Number & Text
            Text(
                text = "Question $index",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 24.sp),
                color = CheckinColors.Primary
            )
            Spacer(Modifier.height(20.dp))

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEach { option ->
                    val isSelected = (option == selectedOption)

                    Surface(
                        onClick = { onOptionSelected(option) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CheckinColors.Primary else CheckinColors.Unselected,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            // Radio Circle Visual
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else Color.Gray.copy(0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = CheckinColors.Primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = if (isSelected) Color.White else CheckinColors.Primary
                            )
                        }
                    }
                }
            }
        }
    }
}