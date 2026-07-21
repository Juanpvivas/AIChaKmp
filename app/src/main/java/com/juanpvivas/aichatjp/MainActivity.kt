package com.juanpvivas.aichatjp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.juanpvivas.aichatjp.ui.navigation.AppNavGraph
import com.juanpvivas.aichatjp.ui.theme.AiChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiChatTheme {
                AppNavGraph()
            }
        }
    }
}
