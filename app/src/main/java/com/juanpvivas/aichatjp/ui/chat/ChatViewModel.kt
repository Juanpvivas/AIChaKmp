package com.juanpvivas.aichatjp.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpvivas.aichatjp.core.AppLogger
import com.juanpvivas.aichatjp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Empty)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var nextId = 0L

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        AppLogger.i("sendMessage: $text")

        val userMessage = ChatMessage(
            id = nextId++,
            text = text,
            fromUser = true,
            time = currentTime()
        )

        _uiState.update { currentState ->
            val messages = when (currentState) {
                is ChatUiState.Empty -> listOf(userMessage)
                is ChatUiState.Success -> currentState.messages + userMessage
                is ChatUiState.Error -> listOf(userMessage)
            }
            ChatUiState.Success(messages, isLoading = true)
        }

        viewModelScope.launch(ioDispatcher) {
            try {
                val response = chatRepository.sendMessage(text)
                AppLogger.i("Response received: ${response.take(100)}")

                val aiMessage = ChatMessage(
                    id = nextId++,
                    text = response,
                    fromUser = false,
                    time = currentTime()
                )

                _uiState.update { currentState ->
                    val messages = when (currentState) {
                        is ChatUiState.Success -> currentState.messages + aiMessage
                        else -> listOf(aiMessage)
                    }
                    ChatUiState.Success(messages)
                }
            } catch (e: Exception) {
                AppLogger.e("Error sending message", e)
                _uiState.update { currentState ->
                    val messages = when (currentState) {
                        is ChatUiState.Success -> currentState.messages
                        else -> emptyList()
                    }
                    ChatUiState.Error(e.message ?: "Error desconocido")
                }
            }
        }
    }

    fun clearHistory() {
        chatRepository.clearHistory()
        _uiState.update { ChatUiState.Empty }
        nextId = 0L
    }

    fun loadConversation(conversationId: Long) {
        AppLogger.i("Loading conversation: $conversationId")
        viewModelScope.launch(ioDispatcher) {
            try {
                chatRepository.clearHistory()
                val messages = chatRepository.getMessagesSync(conversationId)

                if (messages.isEmpty()) {
                    _uiState.update { ChatUiState.Empty }
                } else {
                    val chatMessages = messages.map { msg ->
                        ChatMessage(
                            id = nextId++,
                            text = msg.content,
                            fromUser = msg.isFromUser,
                            time = SimpleDateFormat("HH:mm", Locale.getDefault())
                                .format(Date(msg.timestamp))
                        )
                    }
                    _uiState.update { ChatUiState.Success(chatMessages) }
                }
            } catch (e: Exception) {
                AppLogger.e("Error loading conversation", e)
                _uiState.update { ChatUiState.Error(e.message ?: "Error cargando conversacion") }
            }
        }
    }

    private fun currentTime(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
