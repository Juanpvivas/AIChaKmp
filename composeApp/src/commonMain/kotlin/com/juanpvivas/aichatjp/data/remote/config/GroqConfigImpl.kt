package com.juanpvivas.aichatjp.data.remote.config

import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.juanpvivas.aichatjp.core.groqApiKey
import com.juanpvivas.aichatjp.core.httpClientEngine
import com.juanpvivas.aichatjp.domain.config.GroqConfig
import com.juanpvivas.aichatjp.domain.model.GroqModel
import kotlin.time.Duration.Companion.seconds

class GroqConfigImpl : GroqConfig {
    private companion object {
        const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/"
        const val DEFAULT_MODEL = "qwen/qwen3.6-27b"
        const val TIMEOUT_SECONDS = 60L
        val EXCLUDED_MODEL_PATTERNS = listOf("whisper", "prompt-guard", "tts", "dall-e")
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
        cachedModels?.let { return it }

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
            cachedModels = groqModels
            groqModels
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun resolveChatModel(): String {
        val models = getAvailableModels()

        if (models.isEmpty()) {
            return DEFAULT_MODEL
        }

        val chatModel =
            models.firstOrNull { model ->
                model.isActive &&
                    "text" in model.inputModalities &&
                    "text" in model.outputModalities &&
                    EXCLUDED_MODEL_PATTERNS.none { pattern ->
                        model.id.contains(pattern, ignoreCase = true)
                    }
            }

        return chatModel?.id ?: DEFAULT_MODEL
    }

    override fun getApiKey(): String = groqApiKey()

    override fun getBaseUrl(): String = GROQ_BASE_URL

    override fun getTimeoutSeconds(): Long = TIMEOUT_SECONDS
}
