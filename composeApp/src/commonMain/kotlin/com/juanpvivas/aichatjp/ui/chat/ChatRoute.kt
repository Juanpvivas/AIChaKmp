package com.juanpvivas.aichatjp.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatRoute(
    conversationId: Long,
    onNavigateToHistory: () -> Unit = {}
) {
    val viewModel: ChatViewModel = koinViewModel()

    val uiState by viewModel.uiState.collectAsState()

    ChatScreen(
        uiState = uiState,
        onSendMessage = viewModel::sendMessage
    )
}
