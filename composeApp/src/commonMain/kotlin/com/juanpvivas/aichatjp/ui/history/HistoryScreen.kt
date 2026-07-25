package com.juanpvivas.aichatjp.ui.history

import aicha.composeapp.generated.resources.Res
import aicha.composeapp.generated.resources.history_empty_conversations
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.juanpvivas.aichatjp.domain.model.Conversation
import com.juanpvivas.aichatjp.ui.history.components.HistoryDrawerContent
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onConversationSelected: (Conversation) -> Unit,
    onNewConversation: () -> Unit,
    onDeleteConversation: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is HistoryUiState.Empty -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.history_empty_conversations),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        is HistoryUiState.Success -> {
            HistoryDrawerContent(
                conversations = uiState.conversations,
                isLoading = uiState.isLoading,
                onConversationSelected = onConversationSelected,
                onNewConversation = onNewConversation,
                onDeleteConversation = onDeleteConversation,
                modifier = modifier,
            )
        }
        is HistoryUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
