package com.juanpvivas.aichatjp.domain.usecase

import com.juanpvivas.aichatjp.domain.model.Conversation
import com.juanpvivas.aichatjp.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow

class ObserveConversationsUseCase(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(): Flow<List<Conversation>> =
        conversationRepository.getConversations()
}
