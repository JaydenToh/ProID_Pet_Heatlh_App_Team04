package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    navController: NavController,
    appState: AppState
) {
    val selectedFocus = appState.selectedFocus

    // Convert selected focus to chip labels (always include "All")
    val focusChips = remember(selectedFocus) {
        val list = selectedFocus.map { it.label }
        listOf("All") + list.ifEmpty { listOf("Anxiety", "Stress", "Depression", "Motivation", "Sleep") }
    }

    var selectedChip by rememberSaveable { mutableStateOf("All") }

    // Sample resources (update these later / move to DB)
    val allResources = remember {
        listOf(
            ResourceItem(
                id = "res_1",
                focusArea = FocusArea.ANXIETY,
                title = "Breathing Reset",
                description = "A short breathing exercise to calm your mind.",
                minutes = 3,
                type = ResourceType.QUIZ,
                xp = 15
            ),
            ResourceItem(
                id = "res_2",
                focusArea = FocusArea.SLEEP,
                title = "Sleep Hygiene Basics",
                description = "Simple habits that improve sleep quality.",
                minutes = 5,
                type = ResourceType.TASK,
                xp = 20
            ),
            ResourceItem(
                id = "res_3",
                focusArea = FocusArea.MOTIVATION,
                title = "Small Wins Plan",
                description = "Build momentum with small achievable goals.",
                minutes = 4,
                type = ResourceType.TASK,
                xp = 15
            ),
            ResourceItem(
                id = "res_4",
                focusArea = FocusArea.STRESS,
                title = "Stress Response Reset",
                description = "Understand and reset your stress response.",
                minutes = 4,
                type = ResourceType.QUIZ,
                xp = 15
            ),
            ResourceItem(
                id = "res_5",
                focusArea = FocusArea.DEPRESSION,
                title = "Mood Journal Check-in",
                description = "Reflect and track your mood with prompts.",
                minutes = 5,
                type = ResourceType.TASK,
                xp = 15
            ),
        )
    }

    val filtered = remember(selectedChip, allResources) {
        if (selectedChip == "All") allResources
        else allResources.filter { it.focusArea.label.equals(selectedChip, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resources") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Back") }
                },
                actions = {
                    Text(
                        "≡",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                focusChips.forEach { label ->
                    FilterChip(
                        selected = selectedChip == label,
                        onClick = { selectedChip = label },
                        label = { Text(label) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = 0.35f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                )
            }

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filtered.forEach { item ->
                    ResourceCard(item = item, onClick = {
                        if (item.id == "res_1") {
                            navController.navigate("resource_1") // Navigate to Resource1 screen (Breathing Reset)
                        }
                        // Handle navigation for other resources if needed
                    })
                }
            }
        }
    }
}

@Composable
private fun ResourceCard(
    item: ResourceItem,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { /* no-op */ },
                    label = { Text(item.focusArea.label) }
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "+${item.xp} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "⏱ ${item.minutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.type.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
