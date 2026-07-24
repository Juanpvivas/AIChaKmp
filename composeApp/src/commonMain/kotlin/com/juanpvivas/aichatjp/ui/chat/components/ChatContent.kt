package com.juanpvivas.aichatjp.ui.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.juanpvivas.aichatjp.domain.model.ChatMessage
import com.juanpvivas.aichatjp.ui.chat.ChatInputBar
import com.juanpvivas.aichatjp.ui.chat.ChatMessageList

@Composable
fun ChatContent(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        ChatMessageList(messages = messages)

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }

        ChatInputBar(
            onSendMessage = onSendMessage,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
