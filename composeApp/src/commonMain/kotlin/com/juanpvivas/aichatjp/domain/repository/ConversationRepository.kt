package com.juanpvivas.aichatjp.domain.repository

import com.juanpvivas.aichatjp.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getConversations(): Flow<List<Conversation>>

    suspend fun createConversation(title: String): Result<Long>

    suspend fun deleteConversation(conversationId: Long): Result<Unit>
}
