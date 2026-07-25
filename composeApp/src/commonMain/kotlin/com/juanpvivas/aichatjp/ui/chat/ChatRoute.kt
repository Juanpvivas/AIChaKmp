package com.juanpvivas.aichatjp.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.juanpvivas.aichatjp.ui.history.HistoryUiState
import com.juanpvivas.aichatjp.ui.history.HistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatRoute(
    conversationId: Long,
    onNavigateToHistory: () -> Unit = {}
) {
    val chatViewModel: ChatViewModel = koinViewModel()
    val historyViewModel: HistoryViewModel = koinViewModel()

    val chatUiState by chatViewModel.uiState.collectAsState()
    val historyUiState by historyViewModel.uiState.collectAsState()

    val successState = historyUiState as? HistoryUiState.Success
    val historyConversations = successState?.conversations ?: emptyList()
    val isHistoryLoading = successState?.isLoading ?: false

    ChatScreen(
        uiState = chatUiState,
        historyConversations = historyConversations,
        isHistoryLoading = isHistoryLoading,
        onSendMessage = chatViewModel::sendMessage,
        onConversationSelected = { conversationId ->
            chatViewModel.loadConversation(conversationId)
        },
        onNewConversation = {
            chatViewModel.clearHistory()
        },
        onDeleteConversation = { conversationId ->
            historyViewModel.deleteConversation(conversationId)
        }
    )
}
