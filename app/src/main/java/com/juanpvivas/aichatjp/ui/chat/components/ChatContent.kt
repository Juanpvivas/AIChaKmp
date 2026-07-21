package com.juanpvivas.aichatjp.ui.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.juanpvivas.aichatjp.ui.chat.ChatUiState

@Composable
fun ChatContent(
    uiState: ChatUiState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is ChatUiState.Empty -> ChatEmptyMessage(modifier = modifier)
        is ChatUiState.Success -> {
            ChatMessageList(
                messages = uiState.messages,
                modifier = modifier
            )
            if (uiState.isLoading) {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        is ChatUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
