package com.juanpvivas.aichatjp.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HistoryRoute(
    onConversationSelected: (Long) -> Unit,
    onNewConversation: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HistoryScreen(
        uiState = uiState,
        onConversationSelected = { conversation ->
            viewModel.onConversationSelected(conversation)
            onConversationSelected(conversation.id)
        },
        onNewConversation = {
            viewModel.onNewConversation()
            onNewConversation()
        },
        onDeleteConversation = viewModel::onDeleteConversation
    )
}
