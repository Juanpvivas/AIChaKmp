package com.juanpvivas.aichatjp.ui.history

import com.juanpvivas.aichatjp.data.model.Conversation

sealed interface HistoryUiState {
    data object Empty : HistoryUiState
    data class Success(
        val conversations: List<Conversation>,
        val isLoading: Boolean = false
    ) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}
