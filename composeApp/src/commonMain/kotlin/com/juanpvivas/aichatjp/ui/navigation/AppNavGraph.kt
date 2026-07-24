package com.juanpvivas.aichatjp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.juanpvivas.aichatjp.ui.chat.ChatRoute
import com.juanpvivas.aichatjp.ui.history.HistoryRoute

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.CHAT,
        modifier = modifier
    ) {
        composable(Routes.CHAT) {
            ChatRoute(
                conversationId = 0L,
                onNavigateToHistory = {
                    navController.navigate(Routes.HISTORY)
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryRoute(
                onConversationSelected = { conversation ->
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.CHAT) { inclusive = true }
                    }
                },
                onNewConversation = {
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.CHAT) { inclusive = true }
                    }
                }
            )
        }
    }
}
