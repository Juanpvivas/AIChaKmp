package com.juanpvivas.aichatjp.ui.chat

sealed interface ChatUiState {
    data object Empty : ChatUiState
    data class Success(
        val messages: List<ChatMessage>,
        val isLoading: Boolean = false
    ) : ChatUiState
    data class Error(val message: String) : ChatUiState
}
