package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppRoot()
            }
        }
    }
}

enum class Screen {
    SELECT_FOCUS,
    CHOOSE_COMPANION,
    HOME,
    RESOURCES
}

@Composable
fun AppRoot() {
    var screen by rememberSaveable { mutableStateOf(Screen.SELECT_FOCUS) }

    var selectedFocus by rememberSaveable { mutableStateOf(setOf<FocusArea>()) }
    var selectedCompanion by rememberSaveable { mutableStateOf<Companion?>(null) }

    when (screen) {
        Screen.SELECT_FOCUS -> SelectFocusScreen(
            selected = selectedFocus,
            onToggle = { area ->
                selectedFocus =
                    if (selectedFocus.contains(area)) selectedFocus - area else selectedFocus + area
            },
            onContinue = { screen = Screen.CHOOSE_COMPANION }
        )

        Screen.CHOOSE_COMPANION -> ChooseCompanionScreen(
            selected = selectedCompanion,
            onSelect = { selectedCompanion = it },
            onBack = { screen = Screen.SELECT_FOCUS },
            onStart = { screen = Screen.HOME }
        )

        Screen.HOME -> HomeScreen(
            petName = "Kitten",
            petStageText = "Stage 1/5",
            xp = 0,
            xpGoal = 100,
            streakDays = 0,
            totalXp = 0,
            completed = 0,
            onRequestMentor = { },
            onExploreResources = { screen = Screen.RESOURCES } // ✅ Home -> Resources
        )

        Screen.RESOURCES -> ResourcesScreen(
            selectedFocus = selectedFocus,
            onBack = { screen = Screen.HOME }
        )
    }
}
