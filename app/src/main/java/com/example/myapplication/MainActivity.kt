package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.MentorProfile

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
//data class MentorProfile(
//    val name: String = "",
//    val bio: String = "",
//    val email: String = "",
//    val role: String = "",
//    val availability: String = "",
//    val supportAreas: List<String> = emptyList(),
//    val verified: Boolean = false
//)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("signup") {
            SignUpScreen(navController = navController)
        }

        composable("MentorSetup") {
            MentorSetupScreen(navController = navController)
        }

        composable("student_dashboard") {
            // StudentDashboard(navController = navController)
        }

        composable("MentorDashboard") {
            MentorDashboard(navController = navController)
        }

    }
}

