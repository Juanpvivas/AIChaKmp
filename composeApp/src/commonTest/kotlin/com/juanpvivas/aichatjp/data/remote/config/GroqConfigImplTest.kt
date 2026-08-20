package com.juanpvivas.aichatjp.data.remote.config

import com.juanpvivas.aichatjp.domain.model.GroqPreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GroqConfigImplTest {
    @Test
    fun getBaseUrl_returnsGroqBaseUrl() {
        val config = createConfig()
        assertEquals("https://api.groq.com/openai/v1/", config.getBaseUrl())
    }

    @Test
    fun getTimeoutSeconds_returns60() {
        val config = createConfig()
        assertEquals(60L, config.getTimeoutSeconds())
    }

    @Test
    fun resolveChatModel_returnsPreferredModel_whenSet() =
        runTest {
            val preferences = GroqPreferences(preferredModelId = "custom-model")
            val config = createConfig(preferences = preferences)
            val result = config.resolveChatModel()
            assertEquals("custom-model", result)
        }

    @Test
    fun resolveChatModel_returnsDefaultModel_whenNoPreferred() =
        runTest {
            val config = createConfig()
            val result = config.resolveChatModel()
            assertEquals("qwen/qwen3.6-27b", result)
        }

    private fun createConfig(
        preferences: GroqPreferences = GroqPreferences(),
        modelResolver: GroqModelResolver = GroqModelResolver(),
    ) = GroqConfigImpl(
        preferences = preferences,
        modelResolver = modelResolver,
    )
}
