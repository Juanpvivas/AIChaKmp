package com.juanpvivas.aichatjp.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.juanpvivas.aichatjp.domain.model.Conversation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryRoute(
    onConversationSelected: (Conversation) -> Unit,
    onNewConversation: () -> Unit
) {
    val viewModel: HistoryViewModel = koinViewModel()

    val uiState by viewModel.uiState.collectAsState()

    HistoryScreen(
        uiState = uiState,
        onConversationSelected = onConversationSelected,
        onNewConversation = {
            viewModel.createConversation("New conversation")
            onNewConversation()
        },
        onDeleteConversation = { /* TODO: implement delete */ }
    )
}
