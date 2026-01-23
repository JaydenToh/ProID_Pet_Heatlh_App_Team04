package com.example.myapplication

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

// --- Design System Colors ---
private val LightBackground = Color(0xFFF5F5F7)
private val CardWhite = Color(0xFFFFFFFF)
private val DarkButton = Color(0xFF1A1A1A)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF757575)
private val BorderLight = Color(0xFFE0E0E0)

// Data Model
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
    var currentScreen by remember { mutableStateOf(ScreenState.INFO) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isPlayingBreathing by remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid

    val questions = remember {
        listOf(
            Question("How many seconds should you inhale?", listOf("2 seconds", "4 seconds", "10 seconds"), 1),
            Question("What is the main goal of deep breathing?", listOf("Run faster", "Calm the nervous system", "Stay awake"), 1),
            Question("You should breathe deeply into your...", listOf("Shoulders", "Chest", "Belly/Diaphragm"), 2),
            Question("Exhaling slowly helps to...", listOf("Increase heart rate", "Lower stress", "Improve vision"), 1),
            Question("When is the best time to practice?", listOf("Only when angry", "Anytime", "Never"), 1)
        )
    }

    LaunchedEffect(isPlayingBreathing) {
        if (isPlayingBreathing) {
            delay(3000)
            isPlayingBreathing = false
        }
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Breathing Reset",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentScreen == ScreenState.INFO) navController.popBackStack()
                        else currentScreen = ScreenState.INFO
                    }) {
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
        Crossfade(
            targetState = currentScreen,
            label = "ScreenTransition",
            modifier = Modifier.padding(padding)
        ) { screen ->
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
                        if (userId != null) {
                            updateUserXP(userId, 15)
                        }
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
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clickable { onPetClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPlaying) "🌬️" else "▶️",
                    fontSize = 72.sp
                )
                if (!isPlaying) {
                    Text(
                        text = "Tap to Start Exercise",
                        style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                    )
                } else {
                    Text(
                        text = "Breathe In...",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "About Breathing Reset",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 24.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Controlled breathing helps reduce cortisol levels and physically signals your brain to relax.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextSecondary,
                lineHeight = 24.sp,
                fontSize = 16.sp
            )
        )

        Spacer(Modifier.height(24.dp))

        // Metadata Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                color = CardWhite,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "⏱ 3 min",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Surface(
                color = CardWhite,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Text(
                    text = "⚡ +15 XP",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onStartQuiz,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkButton),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Start Quiz", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.weight(1f))
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // --- 1. Progress Bar (Pinned Top) ---
        val progress by animateFloatAsState(targetValue = (currentIndex + 1).toFloat() / totalQuestions)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = DarkButton,
            trackColor = Color.White
        )

        // --- 2. CENTERED BLOCK (Shifted Upwards) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Spacer (Smaller weight pulls content UP)
            Spacer(modifier = Modifier.weight(0.5f))

            // Question Count
            Text(
                text = "Question ${currentIndex + 1} of $totalQuestions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            )

            Spacer(Modifier.height(16.dp))

            // The Question
            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    fontSize = 26.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // The Options
            question.options.forEachIndexed { index, option ->
                val isSelected = selectedOption == index
                val borderColor = if (isSelected) DarkButton else BorderLight
                val borderWidth = if (isSelected) 2.5.dp else 1.dp

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onOptionSelected(index) },
                    shape = RoundedCornerShape(20.dp),
                    color = CardWhite,
                    border = BorderStroke(borderWidth, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = TextPrimary,
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = DarkButton,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // The Button
            Button(
                onClick = onNext,
                enabled = selectedOption != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkButton,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Next", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            // Bottom Spacer (Larger weight pushes content UP)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun CompletionContent(onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 80.sp)
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Excellent Work!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                fontSize = 28.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "You've completed the Breathing Reset quiz and earned points for your health journey.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextSecondary,
                lineHeight = 26.sp,
                fontSize = 18.sp
            )
        )

        Spacer(Modifier.height(40.dp))

        // XP Reward Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, BorderLight),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "+15 XP",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = DarkButton,
                        fontSize = 40.sp
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Added to Account",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkButton),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Complete & Exit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp))
        }
    }
}

fun updateUserXP(userId: String, points: Long) {
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("users")
        .document(userId)
        .collection("companion")
        .document("CAT")

    // Assuming you want to give wallet XP for task completion
    docRef.update("xp", FieldValue.increment(points))
        .addOnSuccessListener { Log.d("Firebase", "XP updated successfully!") }
        .addOnFailureListener { e -> Log.e("Firebase", "Error updating XP", e) }
}