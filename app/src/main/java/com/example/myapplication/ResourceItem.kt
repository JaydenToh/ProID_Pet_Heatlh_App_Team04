package com.example.myapplication

enum class ResourceType(val label: String) {
    QUIZ("Quiz"),
    TASKS("Tasks")
}

data class ResourceItem(
    val category: String,     // e.g. "Anxiety"
    val title: String,        // e.g. "Breathing Reset"
    val minutes: Int,         // e.g. 3
    val type: ResourceType,   // Quiz / Tasks
    val xp: Int               // e.g. 15
)
