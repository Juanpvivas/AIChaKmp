package com.juanpvivas.aichatjp.composeapp

import androidx.compose.runtime.Composable
import com.juanpvivas.aichatjp.ui.navigation.AppNavGraph
import com.juanpvivas.aichatjp.ui.theme.AiChatTheme

@Composable
fun App() {
    AiChatTheme {
        AppNavGraph()
    }
}
