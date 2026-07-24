package com.juanpvivas.aichatjp.domain.repository

import com.juanpvivas.aichatjp.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(conversationId: Long, content: String): Result<ChatMessage>
    fun getMessages(conversationId: Long): Flow<List<ChatMessage>>
    suspend fun getMessagesSync(conversationId: Long): Result<List<ChatMessage>>
}
