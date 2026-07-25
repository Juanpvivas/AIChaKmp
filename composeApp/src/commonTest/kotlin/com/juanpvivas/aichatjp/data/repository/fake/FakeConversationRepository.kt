package com.juanpvivas.aichatjp.data.repository.fake

import com.juanpvivas.aichatjp.domain.model.Conversation
import com.juanpvivas.aichatjp.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeConversationRepository : ConversationRepository {
    private val conversations = MutableStateFlow<List<Conversation>>(emptyList())
    private var nextId = 1L

    override fun getConversations(): Flow<List<Conversation>> = conversations

    override suspend fun createConversation(title: String): Result<Long> {
        val id = nextId++
        val conversation = Conversation(id = id, title = title)
        conversations.update { it + conversation }
        return Result.success(id)
    }

    override suspend fun deleteConversation(conversationId: Long): Result<Unit> {
        conversations.update { list -> list.filter { it.id != conversationId } }
        return Result.success(Unit)
    }
}
