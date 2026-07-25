package com.juanpvivas.aichatjp.domain.usecase

import com.juanpvivas.aichatjp.domain.repository.ConversationRepository

class DeleteConversationUseCase(
    private val conversationRepository: ConversationRepository,
) {
    suspend operator fun invoke(conversationId: Long): Result<Unit> =
        conversationRepository.deleteConversation(conversationId)
}
