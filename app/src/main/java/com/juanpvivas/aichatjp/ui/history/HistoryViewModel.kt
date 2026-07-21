package com.juanpvivas.aichatjp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpvivas.aichatjp.core.AppLogger
import com.juanpvivas.aichatjp.data.model.Conversation
import com.juanpvivas.aichatjp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Empty)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { HistoryUiState.Success(conversations = emptyList(), isLoading = true) }

            chatRepository.getConversations()
                .catch { e ->
                    AppLogger.e("Error loading conversations", e)
                    _uiState.update { HistoryUiState.Error(e.message ?: "Error desconocido") }
                }
                .collect { conversations ->
                    AppLogger.d("Loaded ${conversations.size} conversations")
                    _uiState.update {
                        if (conversations.isEmpty()) {
                            HistoryUiState.Empty
                        } else {
                            HistoryUiState.Success(conversations = conversations)
                        }
                    }
                }
        }
    }

    fun onConversationSelected(conversation: Conversation) {
        AppLogger.i("Conversation selected: ${conversation.id}")
        // La navegacion se maneja en el Route
    }

    fun onNewConversation() {
        AppLogger.i("New conversation requested")
        chatRepository.clearHistory()
        // La navegacion se maneja en el Route
    }

    fun onDeleteConversation(conversationId: Long) {
        viewModelScope.launch(ioDispatcher) {
            AppLogger.i("Deleting conversation: $conversationId")
            chatRepository.deleteConversation(conversationId)
        }
    }
}
