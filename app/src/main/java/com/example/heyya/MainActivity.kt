package com.example.heyya

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import com.example.heyya.features.search.presentation.SearchScreen
import com.example.heyya.features.search.presentation.SearchViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HeyyaTheme {
                val context = LocalContext.current

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Toast.makeText(context, "Contacts Access Granted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Contacts Access Denied", Toast.LENGTH_SHORT).show()
                    }
                }

                // 2. Check and Request Permission
                LaunchedEffect(Unit) {
                    val permissionCheck = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    )

                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }
                val navController = rememberNavController()

                val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

                val startDestination = if (isAuthenticated) "chats" else "auth"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("auth") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToHome = {
                                navController.navigate("chats") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("chats") {
                        ChatsScreen(
                            onNavigateToChatDetail = { userId, userName ->
                                navController.navigate("chat_detail/$userId/$userName")
                            },
                            onNavigateToSearch = {
                                navController.navigate("search")
                            }
                        )
                    }

                    composable("search") {
                        val searchViewModel: SearchViewModel by viewModels()
                        SearchScreen(
                            viewModel = searchViewModel,
                            onNavigateToChatDetail = { userId, userName ->
                                navController.navigate("chat_detail/$userId/$userName")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "chat_detail/{userId}/{userName}",
                        arguments = listOf(
                            navArgument("userId") { type = NavType.StringType },
                            navArgument("userName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""

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
