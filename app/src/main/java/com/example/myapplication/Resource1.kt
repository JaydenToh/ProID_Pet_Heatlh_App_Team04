package com.example.myapplication

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Data model for our quiz questions
data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

enum class ScreenState {
    INFO, QUIZ, COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Resource1(navController: NavController) {
    // --- State Management ---
    var currentScreen by remember { mutableStateOf(ScreenState.INFO) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isPlayingBreathing by remember { mutableStateOf(false) }

    val questions = listOf(
        Question("How many seconds should you inhale?", listOf("2 seconds", "4 seconds", "10 seconds"), 1),
        Question("What is the main goal of deep breathing?", listOf("Run faster", "Calm the nervous system", "Stay awake"), 1),
        Question("You should breathe deeply into your...", listOf("Shoulders", "Chest", "Belly/Diaphragm"), 2),
        Question("Exhaling slowly helps to...", listOf("Increase heart rate", "Lower stress", "Improve vision"), 1),
        Question("When is the best time to practice?", listOf("Only when angry", "Anytime", "Never"), 1)
    )

    LaunchedEffect(isPlayingBreathing) {
        if (isPlayingBreathing) {
            delay(3000) // Simulate breathing session
            isPlayingBreathing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breathing Reset", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentScreen == ScreenState.INFO) navController.popBackStack()
                        else currentScreen = ScreenState.INFO
                    }) {
                        Text("Back", color = Color(0xFFFFA726), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        // Use Crossfade for smooth transitions between info and quiz
        Crossfade(targetState = currentScreen, label = "ScreenTransition", modifier = Modifier.padding(padding)) { screen ->
            when (screen) {
                ScreenState.INFO -> {
                    InfoContent(
                        isPlaying = isPlayingBreathing,
                        onPetClick = { isPlayingBreathing = true },
                        onStartQuiz = { currentScreen = ScreenState.QUIZ }
                    )
                }
                ScreenState.QUIZ -> {
                    QuizContent(
                        question = questions[currentQuestionIndex],
                        currentIndex = currentQuestionIndex,
                        totalQuestions = questions.size,
                        selectedOption = selectedOption,
                        onOptionSelected = { selectedOption = it },
                        onNext = {
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                selectedOption = null
                            } else {
                                currentScreen = ScreenState.COMPLETED
                            }
                        }
                    )
                }
                ScreenState.COMPLETED -> {
                    CompletionContent(onComplete = {
                        // Logic to add points would go here
                        navController.popBackStack()
                    })
                }
            }
        }
    }
}

@Composable
fun InfoContent(isPlaying: Boolean, onPetClick: () -> Unit, onStartQuiz: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ResourcePetArea(isPlaying = isPlaying, onPetClick = onPetClick)

        Spacer(Modifier.height(24.dp))

        Text("About Breathing Reset", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Controlled breathing helps reduce cortisol levels and physically signals your brain to relax. Practice this for 3 minutes before starting the quiz.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Duration: 3 mins", color = Color.Gray)
            Text("XP: +15", color = Color.Gray)
        }

        Spacer(modifier = Modifier.weight(1f))

        ResourceActionButton(
            text = "Start Quiz",
            subtext = "Test your knowledge",
            isActive = true,
            onClick = onStartQuiz
        )
    }
}

@Composable
fun QuizContent(
    question: Question,
    currentIndex: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    onOptionSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        // Progress Bar
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalQuestions },
            modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.LightGray, RoundedCornerShape(4.dp)),
            color = Color(0xFFFFA726)
        )
        Text("Question ${currentIndex + 1} of $totalQuestions", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(32.dp))

        Text(text = question.text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

        Spacer(Modifier.height(24.dp))

        question.options.forEachIndexed { index, option ->
            val isSelected = selectedOption == index
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) Color(0xFFFFA726) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (isSelected) Color(0xFFFFF3E0) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onOptionSelected(index) }
                    .padding(16.dp)
            ) {
                Text(option, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next Question", fontSize = 18.sp)
        }
    }
}

@Composable
fun CompletionContent(onComplete: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 60.sp)
        Text("Excellent Work!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("You've completed the Breathing Reset quiz and earned points for your health journey.",
            textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            modifier = Modifier.padding(24.dp)
        ) {
            Text("+15 XP Added to Account", modifier = Modifier.padding(16.dp), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Complete & Exit", fontSize = 18.sp)
        }
    }
}

// Reuse your components with minor updates for the layout
@Composable
fun ResourcePetArea(isPlaying: Boolean, onPetClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
            .clickable { onPetClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(if (isPlaying) "🌬️ Breathing..." else "Tap to Start Exercise", style = MaterialTheme.typography.titleLarge)
    }
}

fun updateUserXP(points: Long) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId != null) {
        // Path: users/{userId}/companion/CAT
        val docRef = db.collection("users")
            .document(userId)
            .collection("companion")
            .document("CAT")

        // Atomically increment the 'xp' field
        docRef.update("xp", FieldValue.increment(points))
            .addOnSuccessListener {
                println("XP successfully updated!")
            }
            .addOnFailureListener { e ->
                println("Error updating XP: $e")
            }
    }
}

@Composable
fun ResourceActionButton(text: String, subtext: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = isActive,
        modifier = Modifier.fillMaxWidth().height(72.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFA726),
        shadowElevation = 4.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(subtext, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
        }
    }
}