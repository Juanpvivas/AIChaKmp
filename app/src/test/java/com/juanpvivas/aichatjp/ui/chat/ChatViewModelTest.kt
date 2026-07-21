package com.juanpvivas.aichatjp.ui.chat

import android.util.Log
import app.cash.turbine.test
import com.juanpvivas.aichatjp.data.repository.fake.FakeChatRepository
import com.juanpvivas.aichatjp.testutil.createTestDispatcher
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
class ChatViewModelTest {

    private lateinit var repository: FakeChatRepository
    private lateinit var viewModel: ChatViewModel
    private val testDispatcher = createTestDispatcher()

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        repository = FakeChatRepository()
        viewModel = ChatViewModel(repository, testDispatcher)
    }

    @After
    fun tearDown() {
        repository.clearHistory()
    }

    @Test
    fun `initial state is Empty`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ChatUiState.Empty)
        }
    }

    @Test
    fun `sendMessage updates state to Success with user message`() = runTest {
        viewModel.sendMessage("Hello")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ChatUiState.Success)
            val successState = state as ChatUiState.Success
            assertTrue(successState.messages.isNotEmpty())
            assertEquals("Hello", successState.messages[0].text)
            assertTrue(successState.messages[0].fromUser)
        }
    }

    @Test
    fun `sendMessage receives AI response`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial Empty state

            viewModel.sendMessage("Hello")

            val loadingState = awaitItem()
            assertTrue(loadingState is ChatUiState.Success)
            assertTrue((loadingState as ChatUiState.Success).isLoading)

            val responseState = awaitItem()
            assertTrue(responseState is ChatUiState.Success)
            val successState = responseState as ChatUiState.Success
            assertEquals(2, successState.messages.size)
            assertEquals("Hello", successState.messages[0].text)
            assertTrue(successState.messages[0].fromUser)
            assertEquals("Response to: Hello", successState.messages[1].text)
            assertTrue(!successState.messages[1].fromUser)
            assertTrue(!successState.isLoading)
        }
    }

    @Test
    fun `sendMessage with blank text does nothing`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial Empty state

            viewModel.sendMessage("")

            expectNoEvents()
        }
    }

    @Test
    fun `sendMessage with error updates state to Error`() = runTest {
        repository.shouldReturnError = true

        viewModel.uiState.test {
            awaitItem() // Initial Empty state

            viewModel.sendMessage("Hello")

            val loadingState = awaitItem()
            assertTrue(loadingState is ChatUiState.Success)

            val errorState = awaitItem()
            assertTrue(errorState is ChatUiState.Error)
            assertEquals("Test error", (errorState as ChatUiState.Error).message)
        }
    }

    @Test
    fun `clearHistory resets state to Empty`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial Empty state

            viewModel.sendMessage("Hello")
            awaitItem() // Loading state
            awaitItem() // Response state

            viewModel.clearHistory()

            val state = awaitItem()
            assertTrue(state is ChatUiState.Empty)
        }
    }
}
