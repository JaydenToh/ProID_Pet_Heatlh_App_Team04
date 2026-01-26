package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// --- Unified Design System Colors ---
private val LightBackground = Color(0xFFF5F5F7)
private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF757575)
private val DarkButton = Color(0xFF1A1A1A)
private val BorderLight = Color(0xFFE0E0E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    navController: NavController,
    appState: AppState
) {
    // 1. Single Mixed Filter Row
    val filterOptions = listOf("All", "Anxiety", "Breathing", "Grounding", "Education", "Journaling")

    var selectedChip by rememberSaveable { mutableStateOf("All") }

    // 2. Resource Data
    val allResources = remember {
        listOf(ResourceItem(
            id = "res_1",
            focusArea = FocusArea.DEPRESSION, // Changed from ANXIETY
            title = "Coping with Depression", // Changed from "Breathing Reset"
            description = "Understand depression, recognize signs, and learn simple ways to cope.",
            minutes = 5,
            type = ResourceType.QUIZ,
            xp = 15,
            category = "Depression"
        ),
            ResourceItem(
                id = "res_2",
                focusArea = FocusArea.ANXIETY,
                title = "5-4-3-2-1 Grounding",
                description = "Connect with your senses to stop panic.",
                minutes = 5,
                type = ResourceType.TASK,
                xp = 20,
                category = "Grounding"
            ),
            ResourceItem(
                id = "res_3",
                focusArea = FocusArea.ANXIETY,
                title = "Understanding Panic",
                description = "Learn the physiology behind anxiety attacks.",
                minutes = 7,
                type = ResourceType.TASK,
                xp = 25,
                category = "Education"
            ),
            ResourceItem(
                id = "res_4",
                focusArea = FocusArea.ANXIETY,
                title = "Box Breathing",
                description = "Navy SEAL technique for immediate calm.",
                minutes = 4,
                type = ResourceType.QUIZ,
                xp = 10,
                category = "Breathing"
            ),
            ResourceItem(
                id = "res_5",
                focusArea = FocusArea.DEPRESSION,
                title = "Mood Journaling",
                description = "Write down your thoughts to process emotions.",
                minutes = 10,
                type = ResourceType.TASK,
                xp = 15,
                category = "Journaling"
            )
        )
    }

    // 3. Filter Logic
    val filtered = remember(selectedChip, allResources) {
        allResources.filter { item ->
            when (selectedChip) {
                "All" -> true
                "Anxiety" -> item.focusArea == FocusArea.ANXIETY
                else -> item.category.equals(selectedChip, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Resources",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = LightBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filterOptions.forEach { label ->
                    ModernFilterChip(
                        selected = selectedChip == label,
                        onClick = { selectedChip = label },
                        label = label
                    )
                }
            }

            // --- REMOVED PROGRESS BAR HERE ---

            Spacer(Modifier.height(10.dp))

            // Resource List
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No resources found.", color = TextSecondary)
                    }
                } else {
                    filtered.forEach { item ->
                        ResourceCard(item = item, onClick = {
                            if (item.id == "res_1") {
                                navController.navigate("resource_1")
                            }
                        })
                    }
                }
            }
        }
    }
}

// --- Components ---

@Composable
private fun ModernFilterChip(selected: Boolean, onClick: () -> Unit, label: String) {
    val containerColor = if (selected) DarkButton else CardWhite
    val contentColor = if (selected) Color.White else TextPrimary
    val border = if (selected) null else BorderStroke(1.dp, BorderLight)

    Surface(
        onClick = onClick, color = containerColor, contentColor = contentColor,
        shape = RoundedCornerShape(50), border = border, modifier = Modifier.height(36.dp),
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium))
        }
    }
}

@Composable
private fun ResourceCard(item: ResourceItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = LightBackground, shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    // Show Category if available, else FocusArea
                    val tagText = if (item.category.isNotEmpty()) item.category else item.focusArea.label
                    Text(
                        text = tagText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(text = "+${item.xp} XP", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.height(16.dp))

            // Content
            Text(text = item.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary))
            Spacer(Modifier.height(6.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, color = TextSecondary, lineHeight = 22.sp))
            Spacer(Modifier.height(16.dp))

            // Footer
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ResourceMetaItem(icon = Icons.Default.AccessTime, text = "${item.minutes} min")
                val typeIcon = if(item.type == ResourceType.QUIZ) Icons.Default.Quiz else Icons.Default.Assignment
                ResourceMetaItem(icon = typeIcon, text = item.type.label)
            }
        }
    }
}

@Composable
fun ResourceMetaItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary, fontWeight = FontWeight.Medium))
    }
}