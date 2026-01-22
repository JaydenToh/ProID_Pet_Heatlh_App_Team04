package com.example.myapplication
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.VisualTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Wellness Palette
val WellnessCharcoal = Color(0xFF1A1C2E)
val WellnessGrayBorder = Color(0xFFE0E0E0)
val WellnessSubtext = Color(0xFF757575)
@Composable
fun LoginScreen(navController: NavController) {
    // 1. Initialize Firebase and Coroutine State
    val scope = rememberCoroutineScope()
    val firebaseHelper = remember { FirebaseHelper() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var selectedRole by remember { mutableStateOf("MENTEE") }
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    Scaffold(containerColor = WellnessBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "WellnessConnect",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold, // Matches "Home" header
                color = WellnessBlack
            )
            Text(
                text = "Peer-to-peer wellness support",
                color = WellnessGrayText,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Updated Role Selection with 24.dp corners
            RoleSelectionCard(
                title = "I'm a Student",
                isSelected = selectedRole == "MENTEE",
                onClick = { selectedRole = "MENTEE" },
                description = "Student Card"
            )
            Spacer(modifier = Modifier.height(12.dp))
            RoleSelectionCard(
                title = "I'm a Mentor",
                isSelected = selectedRole == "MENTOR",
                onClick = { selectedRole = "MENTOR" },
                description = "Mentor Card"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Pill-shaped input fields
            WellnessTextField(value = email, onValueChange = { email = it }, label = "Email")
            Spacer(modifier = Modifier.height(12.dp))
            WellnessTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Black button style matching "Play with Cat"
            Button(
                onClick = {

                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null

                    scope.launch {
                        val success = if (isSignUp) {
                            firebaseHelper.signUp(email, password, "MENTEE")
                        } else {
                            firebaseHelper.signIn(email, password)
                        }

                        if (success) {
                            if (isSignUp) {
                                // --- SUCCESS SIGN UP FLOW ---
                                isSuccess = true
                                kotlinx.coroutines.delay(2000)
                                isSuccess = false
                                isSignUp = false // Redirect back to login view
                                email = "" // Clear fields for security
                                password = ""
                            } else {
                                val userId =
                                    FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                                val db = FirebaseFirestore.getInstance()

                                try {
                                    Log.d("LoginDebug", "$userId")
                                    // Fetch the document and WAIT for it to finish before moving to the next line
                                    val doc = db.collection("users").document(userId).get().await()
                                    if (doc.exists()) {
                                        val role = doc.getString("role") ?: "MENTEE"
                                        val name = doc.getString("name") ?: ""
                                        val bio = doc.getString("bio") ?: ""
                                        Log.d(
                                            "LoginDebug",
                                            "Role: $role, Name: '$name', Bio: '$bio'"
                                        )
                                        isLoading = false

                                        when (role) {
                                            "MENTOR" -> {
                                                if (name.isEmpty() || bio.isEmpty()) {
                                                    isLoading = false
                                                    navController.navigate("MentorSetup")
                                                } else {
                                                    isLoading = false
                                                    navController.navigate("MentorDashboard")
                                                    Log.d(
                                                        "HI",
                                                        "Role: $role, Name: '$name', Bio: '$bio'"
                                                    )
                                                }
                                            }

                                            "MENTEE" -> {
                                                navController.navigate("student_dashboard")
                                            }
                                        }
                                    } else {
                                        Log.e(
                                            "LoginDebug",
                                            "ERROR: Document does not exist in Firestore for this UID"
                                        )
                                        navController.navigate("MentorSetup")
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Error fetching user data: ${e.message}"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WellnessCharcoal),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    val buttonText = if (selectedRole == "MENTOR") "Continue"
                    else if (isSignUp) "Create Account"
                    else "Sign In"
                    Text(
                        buttonText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Toggle Sign Up
            Box(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedRole == "MENTEE") {
                    TextButton(onClick = {
                        // Redirect to the separate SignUpScreen composable
                        navController.navigate("signup")
                        errorMessage = null
                    }) {
                        Text(
                            text = "New student? Sign up here",
                            color = WellnessCharcoal,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Text(
                        text = "New mentors will be asked to complete their profile",
                        fontSize = 12.sp,
                        color = WellnessSubtext
                    )
                }
            }
        }
    }
}

@Composable
fun RoleSelectionCard(title: String, description: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(
                width = if (isSelected) 2.dp else 2.dp,
                color = if (isSelected) WellnessCharcoal else WellnessGrayBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = WellnessCharcoal, fontSize = 16.sp)
            Text(description, color = WellnessSubtext, fontSize = 12.sp)
        }
    }
}

@Composable
fun WellnessTextField(value: String, onValueChange: (String) -> Unit, label: String, isPassword: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WellnessCharcoal,
            unfocusedBorderColor = WellnessGrayBorder
        )
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val firebaseHelper = remember { FirebaseHelper() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = WellnessBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registration", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WellnessBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WellnessBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Student-specific registration card with shadow
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = WellnessWhite,
                border = BorderStroke(2.dp, WellnessBlack),
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Start Journey",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Enter details to join the community", color = WellnessGrayText)

                    Spacer(modifier = Modifier.height(24.dp))

                    WellnessTextField(email, { email = it }, "Student Email")
                    Spacer(modifier = Modifier.height(16.dp))
                    WellnessTextField(password, { password = it }, "Password", isPassword = true)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                // Force the role to "MENTEE"
                                val success = firebaseHelper.signUp(email, password, "MENTEE")
                                isLoading = false
                                if (success) {
                                    navController.navigate("login") {
                                        popUpTo("signup") {
                                            inclusive = true
                                        }
                                    }
                                } else {
                                    errorMessage = "Registration failed."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WellnessCharcoal),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White) else Text("Sign Up")
                    }
                }
            }
        }
    }
}
