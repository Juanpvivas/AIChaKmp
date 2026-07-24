package com.juanpvivas.aichatjp.domain.usecase

import com.juanpvivas.aichatjp.data.repository.fake.FakeConversationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateConversationUseCaseTest {

    private val fakeRepository = FakeConversationRepository()
    private val useCase = CreateConversationUseCase(fakeRepository)

    @Test
    fun createConversation_success_returnsId() = runTest {
        val result = useCase("Test conversation")

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun createConversation_returnsSequentialIds() = runTest {
        val id1 = useCase("First").getOrNull()
        val id2 = useCase("Second").getOrNull()

        assertEquals(1L, id1)
        assertEquals(2L, id2)
    }
}
