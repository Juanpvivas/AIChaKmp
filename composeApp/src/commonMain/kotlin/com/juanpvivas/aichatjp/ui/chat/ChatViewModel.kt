package com.juanpvivas.aichatjp.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpvivas.aichatjp.domain.model.ChatMessage
import com.juanpvivas.aichatjp.domain.usecase.ObserveConversationHistoryUseCase
import com.juanpvivas.aichatjp.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeConversationHistoryUseCase: ObserveConversationHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Empty)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConversationId: Long? = null

    fun loadConversation(conversationId: Long) {
        currentConversationId = conversationId
        viewModelScope.launch {
            observeConversationHistoryUseCase(conversationId).collect { messages ->
                _uiState.value = if (messages.isEmpty()) {
                    ChatUiState.Empty
                } else {
                    ChatUiState.Success(messages = messages)
                }
            }
        }
    }

    fun sendMessage(content: String) {
        val conversationId = currentConversationId ?: return
        val currentState = _uiState.value
        if (currentState is ChatUiState.Success) {
            _uiState.value = currentState.copy(isLoading = true)
        }

        viewModelScope.launch {
            sendMessageUseCase(conversationId, content)
                .onFailure { error ->
                    _uiState.value = ChatUiState.Error(
                        error.message ?: "Unknown error"
                    )
                }
        }
    }

    fun clearHistory() {
        currentConversationId = null
        _uiState.value = ChatUiState.Empty
    }
}
