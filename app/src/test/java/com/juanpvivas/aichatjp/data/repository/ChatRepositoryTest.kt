package com.juanpvivas.aichatjp.data.repository

import android.util.Log
import com.juanpvivas.aichatjp.data.repository.fake.FakeChatRepository
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryTest {

    private lateinit var repository: FakeChatRepository

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        repository = FakeChatRepository()
    }

    @After
    fun tearDown() {
        repository.clearHistory()
    }

    @Test
    fun `sendMessage returns AI response`() = runTest {
        val response = repository.sendMessage("Hello")
        assertEquals("Response to: Hello", response)
    }

    @Test
    fun `sendMessage throws exception when error`() = runTest {
        repository.shouldReturnError = true

        try {
            repository.sendMessage("Hello")
            assertTrue("Should have thrown exception", false)
        } catch (e: RuntimeException) {
            assertEquals("Test error", e.message)
        }
    }

    @Test
    fun `clearHistory clears messages`() = runTest {
        repository.sendMessage("Hello")
        repository.clearHistory()

        val messages = repository.getMessagesSync(1)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `createConversation returns id`() = runTest {
        val id = repository.createConversation("Test conversation")
        assertTrue(id > 0)
    }

    @Test
    fun `deleteConversation removes conversation`() = runTest {
        val id = repository.createConversation("Test conversation")
        repository.deleteConversation(id)

        val messages = repository.getMessagesSync(id)
        assertTrue(messages.isEmpty())
    }
}
