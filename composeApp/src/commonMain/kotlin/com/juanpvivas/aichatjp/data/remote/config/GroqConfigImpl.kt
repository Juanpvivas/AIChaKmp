package com.juanpvivas.aichatjp.data.remote.config

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.juanpvivas.aichatjp.core.groqApiKey
import com.juanpvivas.aichatjp.core.httpClientEngine
import com.juanpvivas.aichatjp.domain.config.GroqConfig
import com.juanpvivas.aichatjp.domain.model.GroqModel
import com.juanpvivas.aichatjp.domain.model.GroqPreferences
import kotlin.time.Duration.Companion.seconds

class GroqConfigImpl(
    private val preferences: GroqPreferences = GroqPreferences(),
    private val modelResolver: GroqModelResolver = GroqModelResolver(),
) : GroqConfig {
    private companion object {
        const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/"
        const val TIMEOUT_SECONDS = 60L
    }

    private var cachedModels: List<GroqModel>? = null

    private val openAI: OpenAI by lazy {
        OpenAI(
            config =
                OpenAIConfig(
                    token = groqApiKey(),
                    host = OpenAIHost(baseUrl = GROQ_BASE_URL),
                    timeout = com.aallam.openai.api.http.Timeout(socket = TIMEOUT_SECONDS.seconds),
                    engine = httpClientEngine(),
                ),
        )
    }

    override suspend fun getAvailableModels(): List<GroqModel> {
        if (!preferences.autoDetectModels) {
            return emptyList()
        }

        if (preferences.cacheModels) {
            cachedModels?.let { return it }
        }

        return try {
            val models = openAI.models()
            val groqModels =
                models.map { model ->
                    GroqModel(
                        id = model.id.id,
                        name = model.id.id,
                        isActive = true,
                        inputModalities = listOf("text"),
                        outputModalities = listOf("text"),
                        contextWindow = 131072,
                        maxCompletionTokens = 16384,
                    )
                }
            if (preferences.cacheModels) {
                cachedModels = groqModels
            }
            groqModels
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun resolveChatModel(): String {
        val models = getAvailableModels()
        return modelResolver.resolveBestChatModel(models, preferences.preferredModelId)
    }

    override fun getApiKey(): String = groqApiKey()

    override fun getBaseUrl(): String = GROQ_BASE_URL

    override fun getTimeoutSeconds(): Long = TIMEOUT_SECONDS
}
