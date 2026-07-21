package com.juanpvivas.aichatjp.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.juanpvivas.aichatjp.R
import com.juanpvivas.aichatjp.data.model.Conversation
import com.juanpvivas.aichatjp.ui.history.components.HistoryItem
import com.juanpvivas.aichatjp.ui.theme.AiChatTheme

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onConversationSelected: (Conversation) -> Unit,
    onNewConversation: () -> Unit,
    onDeleteConversation: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con boton nueva conversacion
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onNewConversation) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text(text = stringResource(R.string.new_conversation))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido
        when (uiState) {
            is HistoryUiState.Empty -> {
                EmptyHistoryMessage(modifier = Modifier.weight(1f))
            }
            is HistoryUiState.Success -> {
                if (uiState.isLoading) {
                    LoadingHistory(modifier = Modifier.weight(1f))
                } else {
                    ConversationList(
                        conversations = uiState.conversations,
                        onConversationSelected = onConversationSelected,
                        onDeleteConversation = onDeleteConversation,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            is HistoryUiState.Error -> {
                ErrorMessage(
                    message = uiState.message,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingHistory(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ConversationList(
    conversations: List<Conversation>,
    onConversationSelected: (Conversation) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(conversations) { conversation ->
            HistoryItem(
                conversation = conversation,
                onClick = { onConversationSelected(conversation) },
                onDelete = { onDeleteConversation(conversation.id) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    AiChatTheme {
        HistoryScreen(
            uiState = HistoryUiState.Empty,
            onConversationSelected = {},
            onNewConversation = {},
            onDeleteConversation = {}
        )
    }
}
