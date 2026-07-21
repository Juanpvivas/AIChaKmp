package com.juanpvivas.aichatjp.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.juanpvivas.aichatjp.ui.history.HistoryUiState
import com.juanpvivas.aichatjp.ui.history.HistoryViewModel

@Composable
fun ChatRoute(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
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
            historyViewModel.onNewConversation()
        },
        onDeleteConversation = historyViewModel::onDeleteConversation,
        modifier = modifier
    )
}
