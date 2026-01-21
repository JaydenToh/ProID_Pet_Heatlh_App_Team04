package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember
import kotlin.collections.plus

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Surface provides the background color to prevent the "gray screen"
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val appState = remember { AppState() }
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("MentorSetup") {
            MentorSetupScreen(navController = navController)
        }

        composable("student_dashboard") {
            SelectFocusScreen(
                navController = navController,
                appState = appState
            )
        }
        composable("choose_companion") {
            ChooseCompanionScreen(navController = navController, appState = appState)
        }

        composable("home") {
            HomeScreen(navController = navController, appState = appState)
        }
        composable("companion_details") {
            CompanionDetailScreen(navController = navController, appState = appState)
        }
        composable(route = "shop") {
            ShopScreen(navController = navController)
        }
        composable(route = "resources") {
            ResourcesScreen(navController = navController, appState = appState)
        }
        composable("resource_1") {
            Resource1(navController = navController)
        }
    }
}
