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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    selectedFocus: Set<FocusArea>,
    onBack: () -> Unit
) {
    // Convert your selected focus to chip labels
    val focusChips = remember(selectedFocus) {
        val list = selectedFocus.map { it.label }
        // Always include "All" at the front
        listOf("All") + list.ifEmpty { listOf("Anxiety", "Stress", "Depression", "Motivation", "Sleep") }
    }

    var selectedChip by rememberSaveable { mutableStateOf("All") }

    // Sample resources (you can expand later)
    val allResources = remember {
        listOf(
            ResourceItem("Anxiety", "Breathing Reset", 3, ResourceType.QUIZ, 15),
            ResourceItem("Sleep", "Sleep Hygiene Basics", 5, ResourceType.TASKS, 20),
            ResourceItem("Motivation", "Small Wins Plan", 4, ResourceType.TASKS, 15),
            ResourceItem("Stress", "Stress Response Reset", 4, ResourceType.QUIZ, 15),
            ResourceItem("Depression", "Mood Journal Check-in", 5, ResourceType.TASKS, 15),
        )
    }

    val filtered = remember(selectedChip, allResources) {
        if (selectedChip == "All") allResources
        else allResources.filter { it.category.equals(selectedChip, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resources") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
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
            // Chips row (like your mock)
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

            // Optional: small fake scroll bar area to mimic your UI
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

            // Resource list
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filtered.forEach { item ->
                    ResourceCard(item = item, onClick = { /* later open detail */ })
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
                    label = { Text(item.category) }
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
