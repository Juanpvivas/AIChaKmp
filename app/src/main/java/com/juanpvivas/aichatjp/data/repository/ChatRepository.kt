package com.juanpvivas.aichatjp.data.repository

import com.juanpvivas.aichatjp.data.model.ChatMessage
import com.juanpvivas.aichatjp.data.model.Conversation
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(userMessage: String): String
    fun clearHistory()
    fun getConversations(): Flow<List<Conversation>>
    fun getMessages(conversationId: Long): Flow<List<ChatMessage>>
    suspend fun getMessagesSync(conversationId: Long): List<ChatMessage>
    suspend fun createConversation(title: String): Long
    suspend fun deleteConversation(conversationId: Long)
}
