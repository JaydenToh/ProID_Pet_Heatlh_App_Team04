package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFocusScreen(
    selected: Set<FocusArea>,
    onToggle: (FocusArea) -> Unit,
    onContinue: () -> Unit
) {
    val rows = FocusArea.entries.chunked(2)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Select Your Focus") }) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = onContinue,
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Continue")
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "You can select multiple.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Choose the wellness areas you'd like to focus on.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            // ✅ This area fills the remaining height, but cards do NOT stretch.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { area ->
                            FocusCard(
                                area = area,
                                selected = selected.contains(area),
                                onClick = { onToggle(area) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp) // ✅ fixed height (no stretching)
                            )
                        }

                        // If last row has 1 item, keep spacing consistent
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusCard(
    area: FocusArea,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    val border = if (selected) 2.dp else 1.dp
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline

    val bgColor =
        if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface

    val textColor =
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable { onClick() },
        shape = shape,
        color = bgColor,
        tonalElevation = 0.dp,   // ✅ remove shadow
        shadowElevation = 0.dp,  // ✅ remove shadow
        border = BorderStroke(border, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // ✅ Center emoji + label
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = area.emoji,
                    style = MaterialTheme.typography.headlineSmall,
                    color = textColor
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = area.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor
                )
            }

            // ✅ Clear selection indicator (top-right badge)
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
