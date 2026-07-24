package com.juanpvivas.aichatjp.domain.usecase

import com.juanpvivas.aichatjp.domain.model.ChatMessage
import com.juanpvivas.aichatjp.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ObserveConversationHistoryUseCase(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(conversationId: Long): Flow<List<ChatMessage>> =
        chatRepository.getMessages(conversationId)
}
