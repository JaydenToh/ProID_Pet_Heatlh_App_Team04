package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import com.google.firebase.firestore.DocumentId
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.VisualTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MentorProfile(
    @DocumentId val uid: String = "",
    val name: String = "",
    val bio: String = "",
    val role: String = "MENTOR",
    val supportAreas: List<String> = emptyList(),
    val availability: List<String> = emptyList(),
    val currentMentee: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MentorSetupScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    val supportAreas = listOf("Anxiety", "Stress", "Depression", "Motivation", "Obesity", "Sleep")
    val selectedAreas = remember { mutableStateListOf<String>() }

    val availabilityOptions = listOf("Weekdays", "Weeknights", "Weekends", "Flexible")
    val selectedAvailability = remember { mutableStateListOf<String>() }

    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        containerColor = WellnessBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile Setup", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WellnessBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. Shadowed Card for Personal Info - Matches photo_6310023668066619168_x.jpg style
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = WellnessWhite,
                border = BorderStroke(2.dp, WellnessBlack),
                modifier = Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Basic Info", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))

                    // Uses pill-style input similar to Explore Resources bar
                    WellnessTextField(value = name, onValueChange = { name = it }, label = "Full Name")

                    Spacer(Modifier.height(12.dp))

                    // Large rounded bio field
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        placeholder = { Text("Share your mentoring experience...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WellnessBlack,
                            unfocusedBorderColor = WellnessBorder,
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Focus Areas - Inspired by "Select Your Focus" (photo_6310023668066619169_x.jpg)
            Text(
                "Areas You Support",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold,
                color = WellnessBlack
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                supportAreas.forEach { area ->
                    val isSelected = selectedAreas.contains(area)
                    Surface(
                        modifier = Modifier.clickable {
                            if (isSelected) selectedAreas.remove(area) else selectedAreas.add(area)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) WellnessBlack else WellnessWhite,
                        border = BorderStroke(1.dp, if (isSelected) WellnessBlack else WellnessBorder)
                    ) {
                        Text(
                            text = area,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else WellnessBlack,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Availability Section
            Text(
                "Availability",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold,
                color = WellnessBlack
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                availabilityOptions.forEach { option ->
                    val isSelected = selectedAvailability.contains(option)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedAvailability.remove(option)
                            else selectedAvailability.add(option)
                        },
                        label = { Text(option) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WellnessBlack,
                            selectedLabelColor = Color.White,
                            containerColor = WellnessWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = WellnessBorder,
                            selectedBorderColor = WellnessBlack,
                            borderWidth = 1.dp,
                            enabled = true,
                            selected = true,
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    scope.launch {
                        val profile = MentorProfile(
                            name = name,
                            bio = bio,
                            supportAreas = selectedAreas.toList(),
                            availability = selectedAvailability.toList(),
                        )
                        auth.currentUser?.uid?.let { uid ->
                            db.collection("users").document(uid).set(profile, SetOptions.merge()).await()
                            navController.navigate("MentorDashboard")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(70.dp).padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WellnessBlack),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save and Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}