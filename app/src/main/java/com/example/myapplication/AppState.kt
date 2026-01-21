package com.example.myapplication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AppState {
    var selectedCompanion by mutableStateOf<Companion?>(null)
    var selectedFocus by mutableStateOf(setOf<FocusArea>())
}
