package com.example.myapplication

// --- 1. Define the Enum here ---
enum class ResourceType(val label: String) {
    QUIZ(label = "Quiz"),
    TASK(label = "Task")
}

// --- 2. The Data Class ---
data class ResourceItem(
    val id: String,
    val focusArea: FocusArea,
    val title: String,
    val description: String,
    val minutes: Int,
    val type: ResourceType, // Now this will work
    val xp: Int,
    val category: String    // The new field for "Breathing", "Grounding", etc.
)