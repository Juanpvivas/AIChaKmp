package com.juanpvivas.aichatjp.data.remote.impl

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.juanpvivas.aichatjp.core.groqApiKey
import com.juanpvivas.aichatjp.core.httpClientEngine
import com.juanpvivas.aichatjp.data.remote.ChatRemoteDataSource
import com.juanpvivas.aichatjp.data.remote.dto.SendMessageResponse
import com.juanpvivas.aichatjp.data.remote.mapper.toChatMessage
import com.juanpvivas.aichatjp.data.remote.mapper.toDto
import kotlin.time.Duration.Companion.seconds

class ChatRemoteDataSourceImpl : ChatRemoteDataSource {
    private companion object {
        const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/"
        const val MODEL_ID = "qwen/qwen3.6-27b"
        const val TIMEOUT_SECONDS = 60L
    }

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

    override suspend fun sendMessage(messages: List<String>): SendMessageResponse {
        val chatMessages = messages.map { it.toChatMessage() }

        val request =
            ChatCompletionRequest(
                model = ModelId(MODEL_ID),
                messages = chatMessages,
            )

        val completion = openAI.chatCompletion(request)
        val choice = completion.choices.first()

        return SendMessageResponse(
            content = choice.message.content ?: "",
            model = completion.model.id,
            usage = completion.usage.toDto(),
        )
    }
}
