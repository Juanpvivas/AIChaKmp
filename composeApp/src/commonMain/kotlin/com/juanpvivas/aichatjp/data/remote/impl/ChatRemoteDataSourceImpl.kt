package com.juanpvivas.aichatjp.data.remote.impl

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.juanpvivas.aichatjp.core.httpClientEngine
import com.juanpvivas.aichatjp.data.remote.ChatRemoteDataSource
import com.juanpvivas.aichatjp.data.remote.dto.SendMessageResponse
import com.juanpvivas.aichatjp.data.remote.mapper.toChatMessage
import com.juanpvivas.aichatjp.data.remote.mapper.toDto
import com.juanpvivas.aichatjp.domain.config.GroqConfig
import kotlin.time.Duration.Companion.seconds

class ChatRemoteDataSourceImpl(
    private val groqConfig: GroqConfig,
) : ChatRemoteDataSource {
    private val openAI: OpenAI by lazy {
        OpenAI(
            config =
                OpenAIConfig(
                    token = groqConfig.getApiKey(),
                    host = OpenAIHost(baseUrl = groqConfig.getBaseUrl()),
                    timeout = com.aallam.openai.api.http.Timeout(socket = groqConfig.getTimeoutSeconds().seconds),
                    engine = httpClientEngine(),
                ),
        )
    }

    override suspend fun sendMessage(messages: List<String>): SendMessageResponse {
        val chatMessages = messages.map { it.toChatMessage() }
        val modelId = groqConfig.resolveChatModel()

        val request =
            ChatCompletionRequest(
                model = ModelId(modelId),
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
