package com.juanpvivas.aichatjp.domain.config

import com.juanpvivas.aichatjp.domain.model.GroqModel

interface GroqConfig {
    suspend fun getAvailableModels(): List<GroqModel>

    suspend fun resolveChatModel(): String

    fun getApiKey(): String

    fun getBaseUrl(): String

    fun getTimeoutSeconds(): Long
}
