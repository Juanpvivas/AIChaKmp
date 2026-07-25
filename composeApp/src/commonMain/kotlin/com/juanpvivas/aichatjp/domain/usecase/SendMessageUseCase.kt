package com.juanpvivas.aichatjp.domain.usecase

import com.juanpvivas.aichatjp.domain.model.ChatMessage
import com.juanpvivas.aichatjp.domain.repository.ChatRepository

class SendMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        conversationId: Long,
        content: String,
    ): Result<ChatMessage> = chatRepository.sendMessage(conversationId, content)
}
