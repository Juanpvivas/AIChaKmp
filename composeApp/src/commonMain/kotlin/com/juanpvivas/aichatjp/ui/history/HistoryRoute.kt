package com.juanpvivas.aichatjp.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.juanpvivas.aichatjp.domain.model.Conversation
import aicha.composeapp.generated.resources.Res
import aicha.composeapp.generated.resources.new_conversation_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryRoute(
    onConversationSelected: (Conversation) -> Unit,
    onNewConversation: () -> Unit
) {
    val viewModel: HistoryViewModel = koinViewModel()

    val uiState by viewModel.uiState.collectAsState()

    val newConversationTitle = stringResource(Res.string.new_conversation_title)

    HistoryScreen(
        uiState = uiState,
        onConversationSelected = onConversationSelected,
        onNewConversation = {
            viewModel.createConversation(newConversationTitle)
            onNewConversation()
        },
        onDeleteConversation = { /* TODO: implement delete */ }
    )
}
