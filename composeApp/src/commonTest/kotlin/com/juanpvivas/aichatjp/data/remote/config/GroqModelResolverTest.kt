package com.juanpvivas.aichatjp.data.remote.config

import com.juanpvivas.aichatjp.domain.model.GroqModel
import kotlin.test.Test
import kotlin.test.assertEquals

class GroqModelResolverTest {
    private val resolver = GroqModelResolver()

    @Test
    fun resolveBestChatModel_returnsDefault_whenModelsEmpty() {
        val result = resolver.resolveBestChatModel(emptyList())
        assertEquals("qwen/qwen3.6-27b", result)
    }

    @Test
    fun resolveBestChatModel_returnsPreferred_whenAvailable() {
        val models =
            listOf(
                createModel("model-a"),
                createModel("model-b"),
            )
        val result = resolver.resolveBestChatModel(models, "model-b")
        assertEquals("model-b", result)
    }

    @Test
    fun resolveBestChatModel_returnsBestModel_whenPreferredNotAvailable() {
        val models =
            listOf(
                createModel("model-a"),
                createModel("model-b"),
            )
        val result = resolver.resolveBestChatModel(models, "model-c")
        assertEquals("model-a", result)
    }

    @Test
    fun resolveBestChatModel_excludesWhisperModels() {
        val models =
            listOf(
                createModel("whisper-large-v3"),
                createModel("whisper-large-v3-turbo"),
                createModel("qwen/qwen3.6-27b"),
            )
        val result = resolver.resolveBestChatModel(models)
        assertEquals("qwen/qwen3.6-27b", result)
    }

    @Test
    fun resolveBestChatModel_excludesPromptGuardModels() {
        val models =
            listOf(
                createModel("meta-llama/llama-prompt-guard-2-22m"),
                createModel("meta-llama/llama-prompt-guard-2-86m"),
                createModel("qwen/qwen3.6-27b"),
            )
        val result = resolver.resolveBestChatModel(models)
        assertEquals("qwen/qwen3.6-27b", result)
    }

    @Test
    fun resolveBestChatModel_excludesTtsModels() {
        val models =
            listOf(
                createModel("tts-1"),
                createModel("tts-1-hd"),
                createModel("qwen/qwen3.6-27b"),
            )
        val result = resolver.resolveBestChatModel(models)
        assertEquals("qwen/qwen3.6-27b", result)
    }

    @Test
    fun resolveBestChatModel_excludesDallEModels() {
        val models =
            listOf(
                createModel("dall-e-2"),
                createModel("dall-e-3"),
                createModel("qwen/qwen3.6-27b"),
            )
        val result = resolver.resolveBestChatModel(models)
        assertEquals("qwen/qwen3.6-27b", result)
    }

    @Test
    fun resolveBestChatModel_excludesInactiveModels() {
        val models =
            listOf(
                createModel("model-a", isActive = false),
                createModel("model-b", isActive = true),
            )
        val result = resolver.resolveBestChatModel(models)
        assertEquals("model-b", result)
    }

    @Test
    fun resolveBestChatModel_excludesModelsWithoutTextInput() {
        val models =
            listOf(
                createModel("audio-model", inputModalities = listOf("audio")),
                createModel("text-model", inputModalities = listOf("text")),
            )
        val result = resolver.resolveBestChatModel(models)
        assertEquals("text-model", result)
    }

    @Test
    fun resolveBestChatModel_excludesModelsWithoutTextOutput() {
        val models =
            listOf(
                createModel("audio-model", outputModalities = listOf("audio")),
                createModel("text-model", outputModalities = listOf("text")),
            )
        val result = resolver.resolveBestChatModel(models)
        assertEquals("text-model", result)
    }

    @Test
    fun resolveBestChatModel_prefersQwenOverOtherModels() {
        val models =
            listOf(
                createModel("other-model"),
                createModel("qwen/qwen3.6-27b"),
            )
        val result = resolver.resolveBestChatModel(models)
        assertEquals("qwen/qwen3.6-27b", result)
    }

    @Test
    fun resolveBestChatModel_prefersLlamaOverOtherModels() {
        val models =
            listOf(
                createModel("other-model"),
                createModel("llama3-70b-8192"),
            )
        val result = resolver.resolveBestChatModel(models)
        assertEquals("llama3-70b-8192", result)
    }

    @Test
    fun resolveBestChatModel_prefersModelWithLargerContext() {
        val models =
            listOf(
                createModel("model-small", contextWindow = 4096),
                createModel("model-large", contextWindow = 131072),
            )
        val result = resolver.resolveBestChatModel(models)
        assertEquals("model-large", result)
    }

    @Test
    fun resolveBestChatModel_returnsDefault_whenAllModelsExcluded() {
        val models =
            listOf(
                createModel("whisper-large-v3"),
                createModel("meta-llama/llama-prompt-guard-2-22m"),
            )
        val result = resolver.resolveBestChatModel(models)
        assertEquals("qwen/qwen3.6-27b", result)
    }

    private fun createModel(
        id: String,
        isActive: Boolean = true,
        inputModalities: List<String> = listOf("text"),
        outputModalities: List<String> = listOf("text"),
        contextWindow: Int = 131072,
    ) = GroqModel(
        id = id,
        name = id,
        isActive = isActive,
        inputModalities = inputModalities,
        outputModalities = outputModalities,
        contextWindow = contextWindow,
        maxCompletionTokens = 16384,
    )
}
