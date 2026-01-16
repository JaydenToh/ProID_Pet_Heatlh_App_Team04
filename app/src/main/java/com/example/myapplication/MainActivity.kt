package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    LOGIN,
    SIGNUP,
    SELECT_FOCUS,
    CHOOSE_COMPANION,
    HOME
}

@Composable
fun AppRoot() {
    var screen by rememberSaveable { mutableStateOf(Screen.LOGIN) }

    // app state
    var selectedFocus by rememberSaveable { mutableStateOf(setOf<FocusArea>()) }
    var selectedCompanion by rememberSaveable { mutableStateOf<Companion?>(null) }

    when (screen) {
        Screen.LOGIN -> LoginScreen(
            onLogin = { _, _ ->
                // TEMP: accept any login
                screen = Screen.SELECT_FOCUS
            },
            onSignUpStudent = {
                screen = Screen.SIGNUP
            }
        )

        Screen.SIGNUP -> SignupScreen(
            onBack = { screen = Screen.LOGIN },
            onCreateAccount = { _, _, _, _, _ ->
                // TEMP: after signup, continue flow
                screen = Screen.SELECT_FOCUS
            }
        )

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
            onExploreResources = { }
        )
    }
}
