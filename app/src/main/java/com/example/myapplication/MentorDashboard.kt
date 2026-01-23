package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore

val WellnessBg = Color(0xFFF8F9FA) // Light grayish background from the "Resources" screen
val WellnessWhite = Color(0xFFFFFFFF)
val WellnessBlack = Color(0xFF1A1A1A) // Primary text and button color
val WellnessGrayText = Color(0xFF757575) // Subtext/Description color
val WellnessBorder = Color(0xFFEEEEEE) // Light border for cards

data class StudentProfile(
    @DocumentId val uid: String = "",
    val email: String = "",
    val role: String = "MENTEE",
    val focus: List<String> = emptyList(),
    val currentMentor: String = ""
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorDashboard(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var profile by remember { mutableStateOf<MentorProfile?>(null) }
    var assignedStudent by remember { mutableStateOf<StudentProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        // 1. Get the Mentor's profile first (to display name/bio)
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val mentor = doc.toObject(MentorProfile::class.java)
                profile = mentor

                // 2. SCAN users table to find the student linked to this mentor
                db.collection("users")
                    .whereEqualTo("role", "MENTEE")
                    .whereEqualTo("currentMentor", uid) // Matches the Mentor's Doc ID
                    .limit(1) // Assuming 1 mentee for this dashboard view
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            // Found the specific student linked to this mentor
                            val studentDoc = snapshot.documents.first()

                            // Convert to object and explicitly assign the document ID to uid
                            assignedStudent = studentDoc.toObject(StudentProfile::class.java)?.copy(uid = studentDoc.id)

                            Log.d("MentorDashboard", "Found linked mentee: ${studentDoc.id}")
                        } else {
                            Log.d("MentorDashboard", "No mentee has this user set as currentMentor")
                            assignedStudent = null
                        }
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        Log.e("MentorDashboard", "Error finding mentee", e)
                        isLoading = false
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MentorDashboard", "Error fetching profile", e)
                isLoading = false
            }
    }

    Scaffold(
        containerColor = WellnessBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Mentor Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("login") { popUpTo(0) }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = WellnessBlack)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WellnessBg)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WellnessBlack)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF0F0F0),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp), tint = WellnessGrayText)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = profile?.name ?: "Mentor",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WellnessBlack
                        )
                        Text(
                            text = profile?.bio ?:"Professional Mentor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WellnessGrayText
                        )
                    }
                }

                item {
                    Text(
                        "My Mentees",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                item {
                    if (assignedStudent != null) {
                        ModernStudentCard(
                            student = assignedStudent!!,
                            onChatClick = {
                                val studentId = assignedStudent!!.uid
                                val mentorId = auth.currentUser?.uid ?: ""
                                val chatId = if (studentId < mentorId) "${studentId}_${mentorId}" else "${mentorId}_${studentId}"
                                navController.navigate("chat_screen/$chatId/$mentorId")
                            }
                        )
                    } else {
                        EmptyStateCard()
                    }
                }
            }
        }
    }
}
@Composable
fun ModernStudentCard(student: StudentProfile, onChatClick: () -> Unit) {
    MenteeCard(student, onChatClick)
}
@Composable
fun MenteeCard(student: StudentProfile, onChatClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp) // Space for shadow to breathe
            .shadow(
                elevation = 10.dp, // Creates the soft diffused bottom shadow
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = WellnessWhite,
            border = BorderStroke(2.dp, WellnessBlack) // The bold black border from your image
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Active Mentee 1",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = WellnessBlack,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Focus Areas",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = WellnessGrayText,
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    student.focus.take(3).forEach { area ->
                        Surface(
                            color = Color(0xFFF0F0F0),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, WellnessBorder)
                        ) {
                            Text(
                                text = area,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = WellnessBlack,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Primary Black Button
                Button(
                    onClick = onChatClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WellnessBlack),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Open Chat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Keep in touch with your mentee to help them progress through their wellness journey.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WellnessGrayText,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
@Composable
fun EmptyStateCard() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = WellnessWhite,
        border = BorderStroke(1.dp, WellnessBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No Assigned Mentees",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "You haven't been assigned any students yet. Please check back later.",
                textAlign = TextAlign.Center,
                color = WellnessGrayText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}