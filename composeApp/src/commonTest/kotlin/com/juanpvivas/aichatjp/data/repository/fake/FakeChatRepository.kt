package com.juanpvivas.aichatjp.data.repository.fake

import com.juanpvivas.aichatjp.domain.model.ChatMessage
import com.juanpvivas.aichatjp.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeChatRepository : ChatRepository {
    private val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private var sendMessageResult: Result<ChatMessage> =
        Result.success(
            ChatMessage(content = "AI response", isFromUser = false),
        )

    fun setSendMessageResult(result: Result<ChatMessage>) {
        sendMessageResult = result
    }

    override suspend fun sendMessage(
        conversationId: Long,
        content: String,
    ): Result<ChatMessage> {
        val userMessage =
            ChatMessage(
                conversationId = conversationId,
                content = content,
                isFromUser = true,
            )
        messages.update { it + userMessage }

        val result = sendMessageResult
        result.onSuccess { aiMessage ->
            messages.update { it + aiMessage.copy(conversationId = conversationId) }
        }
        return result
    }

    override fun getMessages(conversationId: Long): Flow<List<ChatMessage>> = messages

    override suspend fun getMessagesSync(conversationId: Long): Result<List<ChatMessage>> =
        Result.success(messages.value.filter { it.conversationId == conversationId })
}
