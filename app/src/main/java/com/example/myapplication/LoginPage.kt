package com.example.myapplication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.launch
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "WellnessConnect", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = WellnessCharcoal)
        Text(text = "Peer-to-peer wellness support", fontSize = 14.sp, color = WellnessSubtext)

        Spacer(modifier = Modifier.height(40.dp))

        // Role Selection Cards
        RoleSelectionCard(
            title = "I'm a Student",
            description = "Get matched with a mentor",
            isSelected = selectedRole == "MENTEE",
            onClick = { selectedRole = "MENTEE"; errorMessage = null }
        )

        Spacer(modifier = Modifier.height(12.dp))

        RoleSelectionCard(
            title = "I'm a Mentor",
            description = "Support Mentees in need",
            isSelected = selectedRole == "MENTOR",
            onClick = { selectedRole = "MENTOR"; isSignUp = false; errorMessage = null }
        )

        Spacer(modifier = Modifier.height(32.dp))


        WellnessTextField(value = email, onValueChange = { email = it }, label = "Username")
        Spacer(modifier = Modifier.height(12.dp))
        WellnessTextField(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)

        // 2. Error Message Display
        if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        if (isSuccess) {
            Surface(
                color = Color(0xFFE8F5E9), // Soft green background
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account created! Redirecting to login...",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
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

                    isLoading = false
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
                            // --- SUCCESS LOGIN FLOW ---
                            if (selectedRole == "MENTOR") {
//                                navController.navigate("") navigate to mentor dashboard
                            } else {
//                                navController.navigate("") navigate to student dashboard
                            }
                        }
                    } else {
                        errorMessage = if (isSignUp) "Sign up failed. Try again." else "Invalid credentials."
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
                Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Toggle Sign Up
        Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
            if (selectedRole == "MENTEE") {
                TextButton(onClick = { isSignUp = !isSignUp; errorMessage = null }) {
                    Text(
                        text = if (isSignUp) "Already have an account? Login" else "New student? Sign up here",
                        color = WellnessCharcoal, fontSize = 13.sp
                    )
                }
            } else {
                Text(text = "New mentors will be asked to complete their profile", fontSize = 12.sp, color = WellnessSubtext)
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