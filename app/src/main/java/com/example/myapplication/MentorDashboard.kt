package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun MentorDashboard(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // State to hold the mentor profile data
    var profile by remember { mutableStateOf<MentorProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    // Add this state to hold the student's profile
    var assignedStudent by remember { mutableStateOf<MentorProfile?>(null) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val mentor = document.toObject(MentorProfile::class.java)
                profile = mentor

                val menteeId = mentor?.currentMentee
                if (!menteeId.isNullOrEmpty()) {
                    Log.d("MENTORDEBUG","HIII ${menteeId}")
                    db.collection("users").document(menteeId).get()
                        .addOnSuccessListener { studentDoc ->
                            assignedStudent = studentDoc.toObject(MentorProfile::class.java)?.copy(uid = menteeId)
                            Log.d("MENTORDEBUG","${menteeId}")
                            Log.d("MENTORDEBUG","HII ${assignedStudent}")
                            isLoading = false
                        }
                        .addOnFailureListener { e->
                            isLoading = false
                            Log.e("MENTORDEBUG", "FETCH FAILED: ${e.message}", e)
                        }
                } else {
                    isLoading = false // No student assigned, stop loading
                }
            }
            .addOnFailureListener {
                isLoading = false // Database error, stop loading
            }
    }
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        Scaffold(
            containerColor = Color(0xFFFBFBFE),
            topBar = {
                // Using standard TopAppBar for better alignment control
                TopAppBar(
                    title = {
                        Column(modifier = Modifier.padding(start = 4.dp)) { // Slight nudge for alignment
                            Text(
                                "Welcome back,",
                                style = MaterialTheme.typography.labelLarge,
                                color = WellnessSubtext
                            )
                            Text(
                                profile?.name ?: "Mentor",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = WellnessCharcoal
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                auth.signOut()
                                navController.navigate("login") {
                                    // Clears the entire backstack so user can't "Go Back" into the dashboard
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Logout, // Or Icons.Default.ExitToApp
                                contentDescription = "Logout",
                                tint = WellnessCharcoal
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp), // Matched with LoginScreen padding
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    MentorProfileHeader(profile)
                }

                item {
                    Text(
                        "Current Mentees",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = WellnessCharcoal,
                        modifier = Modifier.padding(top = 8.dp)
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
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF0F0F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.People,
                                    null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.LightGray
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No Students Yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "You haven't been assigned any\nstudents. An admin will assign\nstudents to you.",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color.Gray,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MentorProfileHeader(profile: MentorProfile?) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, WellnessGrayBorder)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circular Avatar in Charcoal/Gray
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFF5F5F5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        profile?.name?.take(1) ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = WellnessCharcoal,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Pending Verification",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = WellnessSubtext
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        profile?.name ?: "Loading...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = WellnessCharcoal
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                profile?.bio ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = WellnessSubtext,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(20.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profile?.supportAreas?.forEach { area ->
                    Surface(
                        color = Color.White,
                        border = BorderStroke(1.dp, WellnessGrayBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            area,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = WellnessCharcoal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernStudentCard(student: MentorProfile, onChatClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = BorderStroke(1.dp, WellnessGrayBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(Color(0xFFF5F5F5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(student.name.take(1), fontWeight = FontWeight.Bold, color = WellnessCharcoal)
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(student.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WellnessCharcoal)
                Text("Assigned Mentee", style = MaterialTheme.typography.labelSmall, color = WellnessSubtext)
            }

            IconButton(
                onClick = onChatClick,
                modifier = Modifier.background(WellnessCharcoal, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.Default.Chat,
                    contentDescription = "Chat",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}