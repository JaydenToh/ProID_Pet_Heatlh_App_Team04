package com.example.myapplication

enum class ResourceType(val label: String) {
    QUIZ("Quiz"),
    TASK("Task")
}

data class ResourceItem(
    val id: String,              // unique ID
    val focusArea: FocusArea,    // Anxiety, Sleep, Stress, etc.
    val title: String,           // "Breathing Reset"
    val description: String,     // short explanation
    val minutes: Int,            // 3
    val type: ResourceType,      // QUIZ / TASK
    val xp: Int                  // 15
)
