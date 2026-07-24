package com.juanpvivas.aichatjp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpvivas.aichatjp.domain.model.Conversation
import com.juanpvivas.aichatjp.domain.usecase.CreateConversationUseCase
import com.juanpvivas.aichatjp.domain.usecase.ObserveConversationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val observeConversationsUseCase: ObserveConversationsUseCase,
    private val createConversationUseCase: CreateConversationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Empty)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            observeConversationsUseCase().collect { conversations ->
                _uiState.value = if (conversations.isEmpty()) {
                    HistoryUiState.Empty
                } else {
                    HistoryUiState.Success(conversations = conversations)
                }
            }
        }
    }

    fun createConversation(title: String) {
        viewModelScope.launch {
            createConversationUseCase(title)
        }
    }
}
