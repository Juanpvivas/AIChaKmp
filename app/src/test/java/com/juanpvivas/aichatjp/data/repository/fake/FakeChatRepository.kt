package com.juanpvivas.aichatjp.data.repository.fake

import com.juanpvivas.aichatjp.data.model.ChatMessage
import com.juanpvivas.aichatjp.data.model.Conversation
import com.juanpvivas.aichatjp.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakeChatRepository : ChatRepository {

    private val messages = mutableListOf<ChatMessage>()
    private val conversations = mutableListOf<Conversation>()
    private val _conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())

    var shouldReturnError = false
    var errorMessage = "Test error"
    var nextConversationId = 1L
    var nextMessageId = 1L

    override suspend fun sendMessage(userMessage: String): String {
        if (shouldReturnError) throw RuntimeException(errorMessage)

        messages.add(
            ChatMessage(
                id = nextMessageId++,
                conversationId = 1,
                content = userMessage,
                isFromUser = true,
                timestamp = System.currentTimeMillis()
            )
        )

        val response = "Response to: $userMessage"
        messages.add(
            ChatMessage(
                id = nextMessageId++,
                conversationId = 1,
                content = response,
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
        )

        return response
    }

    override fun clearHistory() {
        messages.clear()
    }

    override fun getConversations(): Flow<List<Conversation>> = _conversationsFlow

    override fun getMessages(conversationId: Long): Flow<List<ChatMessage>> {
        return flowOf(messages.filter { it.conversationId == conversationId })
    }

    override suspend fun getMessagesSync(conversationId: Long): List<ChatMessage> {
        return messages.filter { it.conversationId == conversationId }
    }

    override suspend fun createConversation(title: String): Long {
        val id = nextConversationId++
        conversations.add(
            Conversation(
                id = id,
                title = title,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        _conversationsFlow.value = conversations.toList()
        return id
    }

    override suspend fun deleteConversation(conversationId: Long) {
        conversations.removeAll { it.id == conversationId }
        messages.removeAll { it.conversationId == conversationId }
        _conversationsFlow.value = conversations.toList()
    }

    fun addConversation(conversation: Conversation) {
        conversations.add(conversation)
        _conversationsFlow.value = conversations.toList()
    }
}
