package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MentorProfile(
    val name: String = "",
    val bio: String = "",
    val supportAreas: List<String> = emptyList(),
    val availability: String = "",
    val isVerified: Boolean = false
)

@Composable
fun MentorSetupScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    val supportAreas = listOf("Anxiety", "Stress", "Depression", "Motivation", "Obesity", "Sleep")
    val selectedAreas = remember { mutableStateListOf<String>() }

    val availabilityOptions = listOf("Weekdays", "Weeknights", "Weekends", "Flexible")
    var selectedAvailability by remember { mutableStateOf("Weeknights") }

    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Setup Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = WellnessCharcoal)
        Spacer(modifier = Modifier.height(24.dp))

        WellnessTextField(value = name, onValueChange = { name = it }, label = "Your Name *")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Text("Areas You Support *", modifier = Modifier.padding(vertical = 16.dp), fontWeight = FontWeight.Bold)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Replaces mainAxisSpacing
            verticalArrangement = Arrangement.spacedBy(8.dp)     // Replaces crossAxisSpacing
        ) {
            supportAreas.forEach { area ->
                FilterChip(
                    selected = selectedAreas.contains(area),
                    onClick = {
                        if (selectedAreas.contains(area)) selectedAreas.remove(area)
                        else selectedAreas.add(area)
                    },
                    label = { Text(area) },
                    modifier = Modifier.padding(vertical = 4.dp) // Optional extra padding
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Availability *", modifier = Modifier.padding(top = 24.dp, bottom = 16.dp), fontWeight = FontWeight.Bold)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availabilityOptions.forEach { option ->
                FilterChip(
                    selected = selectedAvailability == option,
                    onClick = { selectedAvailability = option },
                    label = { Text(option) },
                    // Customizing colors to match your Charcoal theme
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WellnessCharcoal,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    val profile = MentorProfile(name, bio, selectedAreas.toList(), selectedAvailability)
                    auth.currentUser?.uid?.let { uid ->
                        db.collection("users").document(uid).set(profile, SetOptions.merge()).await()
                        navController.navigate("mentor_dashboard")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WellnessCharcoal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Profile", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}