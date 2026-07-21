package com.juanpvivas.aichatjp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.juanpvivas.aichatjp.ui.chat.ChatRoute

object Routes {
    const val CHAT = "chat"
    const val HISTORY = "history"
}

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
            ChatRoute()
        }
        // TODO: Agregar ruta de historial cuando se implemente
        // composable(Routes.HISTORY) {
        //     HistoryRoute()
        // }
    }
}
