package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// --- Design System Colors ---
private val LightBackground = Color(0xFFF5F5F7)
private val CardWhite = Color(0xFFFFFFFF)
private val DarkButton = Color(0xFF1A1A1A)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF757575)
private val BorderUnselected = Color(0xFFE0E0E0) // Light grey for definition

@Composable
fun SelectFocusScreen(
    navController: NavController,
    appState: AppState
) {
    val selectedFocus = appState.selectedFocus
    val rows = FocusArea.entries.chunked(2)

    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = LightBackground
        // Removed bottomBar so the button can move to the center
    ) { padding ->
        // Main Centered Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()) // Allow scrolling if screen is short
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Spacer: Pushes everything down to the center
            Spacer(modifier = Modifier.weight(1f))

            // 2. Header Section
            Text(
                text = "Select Your Focus",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = TextPrimary
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Choose the wellness areas you'd like to focus on.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    color = TextSecondary
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. The Grid Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { area ->
                            DefinedFocusCard(
                                area = area,
                                selected = selectedFocus.contains(area),
                                onClick = {
                                    appState.selectedFocus =
                                        if (selectedFocus.contains(area)) selectedFocus - area
                                        else selectedFocus + area
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(125.dp) // Fixed generous height
                            )
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Button Section (Now centered below the grid)
            Button(
                onClick = {
                    if (selectedFocus.isNotEmpty()) {
                        currentUser?.let {
                            scope.launch {
                                try {
                                    val selectedFocusLabels = selectedFocus.map { it.label }
                                    db.collection("users")
                                        .document(it.uid)
                                        .update("focus", selectedFocusLabels)
                                        .await()
                                    navController.navigate("choose_companion") {
                                        launchSingleTop = true
                                    }
                                } catch (e: Exception) {
                                    Log.e("Firebase", "Error saving focus data: ", e)
                                }
                            }
                        }
                    }
                },
                enabled = selectedFocus.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkButton,
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.1f))
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You can select multiple",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            // 5. Bottom Spacer: Balances the top spacer
            Spacer(modifier = Modifier.weight(1f))

            // Extra padding for bottom safety
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DefinedFocusCard(
    area: FocusArea,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // VISUAL DEFINITION LOGIC:
    // Selected: Bold Dark Border
    // Unselected: Thin Light Grey Border (defines the box better)
    val borderColor = if (selected) DarkButton else BorderUnselected
    val borderWidth = if (selected) 2.5.dp else 1.dp

    // Shadows: Slightly deeper shadow for better separation
    val elevation = if (selected) 6.dp else 2.dp

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = area.emoji,
                    fontSize = 38.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = area.label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                )
            }

            // Checkmark Overlay
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(24.dp)
                        .background(DarkButton, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}