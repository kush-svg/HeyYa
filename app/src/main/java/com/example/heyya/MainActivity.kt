package com.example.heyya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.heyya.core.ui.theme.HeyyaTheme
import com.example.heyya.features.auth.presentation.AuthViewModel
import com.example.heyya.features.auth.presentation.LoginScreen
import com.example.heyya.features.chat_detail.presentation.MessageScreen
import com.example.heyya.features.chats.presentation.ChatsScreen

class MainActivity : ComponentActivity() {

    // Use viewModels() to ensure the same instance is used if needed
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HeyyaTheme {
                val navController = rememberNavController()

                // Observe authentication state
                val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

                // Decide start destination based on Firebase Auth status
                val startDestination = if (isAuthenticated) "chats" else "auth"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    // 1. Auth / Login Screen
                    composable("auth") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToHome = {
                                // Navigate and clear backstack so user can't go back to login
                                navController.navigate("chats") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 2. Chat List Screen
                    composable("chats") {
                        ChatsScreen(
                            onNavigateToChatDetail = { userId, userName ->
                                // Navigate to Detail with arguments
                                navController.navigate("chat_detail/$userId/$userName")
                            }
                        )
                    }

                    // 3. Chat Detail / Message Screen
                    composable(
                        route = "chat_detail/{userId}/{userName}",
                        arguments = listOf(
                            navArgument("userId") { type = NavType.StringType },
                            navArgument("userName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""

                        // We generate a roomId. For 1-on-1, a common pattern is
                        // sorting the two IDs alphabetically.
                        val currentUserId = authViewModel.getCurrentUser()?.uid ?: ""
                        val roomId = if (currentUserId < userId)
                            "${currentUserId}_$userId" else "${userId}_$currentUserId"

                        MessageScreen(
                            roomId = roomId,
                            senderId = currentUserId,
                            senderName = authViewModel.getCurrentUser()?.name ?: "",
                            chatPartnerName = userName,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
