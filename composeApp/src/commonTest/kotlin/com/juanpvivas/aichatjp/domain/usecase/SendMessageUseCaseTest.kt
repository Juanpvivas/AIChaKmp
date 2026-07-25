package com.juanpvivas.aichatjp.domain.usecase

import com.juanpvivas.aichatjp.data.repository.fake.FakeChatRepository
import com.juanpvivas.aichatjp.domain.model.ChatMessage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SendMessageUseCaseTest {
    private val fakeRepository = FakeChatRepository()
    private val useCase = SendMessageUseCase(fakeRepository)

    @Test
    fun sendMessage_success_returnsAiMessage() =
        runTest {
            val aiMessage = ChatMessage(content = "Hello from AI", isFromUser = false)
            fakeRepository.setSendMessageResult(Result.success(aiMessage))

            val result = useCase(conversationId = 1L, content = "Hello")

            assertTrue(result.isSuccess)
            assertEquals("Hello from AI", result.getOrNull()?.content)
        }

    @Test
    fun sendMessage_failure_returnsError() =
        runTest {
            fakeRepository.setSendMessageResult(Result.failure(Exception("Network error")))

            val result = useCase(conversationId = 1L, content = "Hello")

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }
}
