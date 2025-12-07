package com.annguyen.personalfinancetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.annguyen.personalfinancetracker.data.repository.AuthRepository
import com.annguyen.personalfinancetracker.ui.navigation.NavGraph
import com.annguyen.personalfinancetracker.ui.navigation.Screen
import com.annguyen.personalfinancetracker.ui.theme.PersonalfinancetrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalfinancetrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FinanceTrackerApp()
                }
            }
        }
    }
}

@Composable
fun FinanceTrackerApp() {
    val navController = rememberNavController()
    val authRepository = AuthRepository()
    val authState by authRepository.authState.collectAsState(initial = authRepository.currentUser)
    
    val startDestination = if (authState != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }
    
    LaunchedEffect(authState) {
        if (authState == null) {
            // User logged out, navigate to login
            if (navController.currentDestination?.route != Screen.Login.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            // User logged in, navigate to home if on auth screens
            if (navController.currentDestination?.route == Screen.Login.route ||
                navController.currentDestination?.route == Screen.SignUp.route) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
    
    NavGraph(
        navController = navController,
        startDestination = startDestination,
        userId = authState?.uid ?: ""
    )
}