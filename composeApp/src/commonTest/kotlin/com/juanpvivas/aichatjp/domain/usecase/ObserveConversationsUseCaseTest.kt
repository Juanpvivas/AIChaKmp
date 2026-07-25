package com.juanpvivas.aichatjp.domain.usecase

import com.juanpvivas.aichatjp.data.repository.fake.FakeConversationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveConversationsUseCaseTest {
    private val fakeRepository = FakeConversationRepository()
    private val useCase = ObserveConversationsUseCase(fakeRepository)

    @Test
    fun invoke_returnsEmptyListInitially() =
        runTest {
            val conversations = useCase().first()

            assertTrue(conversations.isEmpty())
        }

    @Test
    fun invoke_returnsCreatedConversations() =
        runTest {
            fakeRepository.createConversation("First")
            fakeRepository.createConversation("Second")

            val conversations = useCase().first()

            assertEquals(2, conversations.size)
            assertEquals("First", conversations[0].title)
            assertEquals("Second", conversations[1].title)
        }
}
