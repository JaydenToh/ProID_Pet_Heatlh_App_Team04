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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
private val AccentBlue = Color(0xFFE3F2FD) // Soft blue for lesson headers

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

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid

    // Updated Questions based on the "Coping with Depression" Lesson
    val questions = remember {
        listOf(
            Question(
                text = "What is a key difference between normal sadness and depression?",
                options = listOf(
                    "Depression only lasts a few hours",
                    "Depression affects daily life and lasts longer",
                    "Sadness makes people more energetic"
                ),
                correctAnswerIndex = 1
            ),
            Question(
                text = "Which is a common sign of depression?",
                options = listOf(
                    "Always feeling happy",
                    "Persistent low mood or loss of interest",
                    "Better focus than usual"
                ),
                correctAnswerIndex = 1
            ),
            Question(
                text = "A healthy way to cope with depression is to:",
                options = listOf(
                    "Keep feelings to yourself",
                    "Reach out for support or talk to someone trusted",
                    "Ignore feelings and stay isolated"
                ),
                correctAnswerIndex = 1
            )
        )
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Coping with Depression",
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
fun InfoContent(onStartQuiz: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Image / Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(AccentBlue, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🛡️", fontSize = 40.sp)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Understanding Depression",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 24.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Learn to recognise signs and seek support.",
            style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // Lesson Content Cards
        LessonSection(
            icon = Icons.Default.Psychology,
            title = "1. What Is It?",
            content = "Feeling sad sometimes is normal, but depression is more than just sadness. It affects your mood, thoughts, and energy. If feelings last for weeks and affect daily life, it may be depression."
        )

        LessonSection(
            icon = Icons.Default.Lightbulb,
            title = "2. Common Signs",
            content = "• Persistent sadness or low mood\n• Loss of interest in hobbies\n• Sleep or appetite changes\n• Feeling tired or low energy\n• Difficulty concentrating"
        )



        LessonSection(
            icon = Icons.Default.Favorite,
            title = "3. Causes",
            content = "Depression can arise from a mix of factors, including stressful life events, ongoing pressure, family history, and brain chemistry. It can happen to anyone."
        )

        LessonSection(
            icon = Icons.Default.SelfImprovement,
            title = "4. Coping & Support",
            content = "• Talk with someone you trust\n• Maintain a healthy routine\n• Do enjoyable activities\n• Reach out for professional help\n\nThere is no shame in asking for help."
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onStartQuiz,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkButton),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Take Mini Quiz", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun LessonSection(icon: ImageVector, title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 20.sp)
            )
        }
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
        // --- 1. Progress Bar ---
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

        // --- 2. CENTERED BLOCK ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = "Question ${currentIndex + 1} of $totalQuestions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

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
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = TextPrimary,
                                fontSize = 16.sp
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
            text = "Lesson Complete!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                fontSize = 28.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "You've learned the basics of coping with depression. Reach out for help if you need it.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextSecondary,
                lineHeight = 26.sp,
                fontSize = 18.sp
            )
        )

        Spacer(Modifier.height(40.dp))

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

        Spacer(Modifier.height(24.dp))
        Surface(
            color = Color(0xFFE3F2FD), // Light Blue
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // You might need to import androidx.compose.material.icons.filled.Call
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Phone",
                    tint = Color(0xFF1565C0), // Darker Blue
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Reach out to NHG Polyclinics' Psychology Services at 6355 3000 if you or your loved ones need professional help.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF0D47A1), // Dark Text Blue
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

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
        .document("CAT") // Ideally, fetch the actual active pet ID here

    docRef.update("xp", FieldValue.increment(points))
        .addOnSuccessListener { Log.d("Firebase", "XP updated successfully!") }
        .addOnFailureListener { e -> Log.e("Firebase", "Error updating XP", e) }
}