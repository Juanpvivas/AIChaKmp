package com.juanpvivas.aichatjp.domain.usecase

import com.juanpvivas.aichatjp.domain.repository.ConversationRepository

class CreateConversationUseCase(
    private val conversationRepository: ConversationRepository,
) {
    suspend operator fun invoke(title: String): Result<Long> = conversationRepository.createConversation(title)
}
