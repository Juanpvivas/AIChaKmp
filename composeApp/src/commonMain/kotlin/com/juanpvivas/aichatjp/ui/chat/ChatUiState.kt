package com.juanpvivas.aichatjp.ui.chat

import com.juanpvivas.aichatjp.domain.model.ChatMessage

sealed interface ChatUiState {
    data object Empty : ChatUiState
    data class Success(
        val messages: List<ChatMessage>,
        val isLoading: Boolean = false
    ) : ChatUiState
    data class Error(val message: String? = null) : ChatUiState
}
