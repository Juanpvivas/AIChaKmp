package com.juanpvivas.aichatjp.data.remote.impl

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.juanpvivas.aichatjp.core.AppLogger
import com.juanpvivas.aichatjp.data.remote.ChatRemoteDataSource
import com.juanpvivas.aichatjp.data.remote.dto.SendMessageResponse
import com.juanpvivas.aichatjp.data.remote.dto.UsageDto
import com.juanpvivas.aichatjp.data.remote.mapper.ChatMessageMapper
import javax.inject.Inject

class ChatRemoteDataSourceImpl @Inject constructor(
    private val openAI: OpenAI,
    private val chatMessageMapper: ChatMessageMapper
) : ChatRemoteDataSource {

    override suspend fun sendMessage(messages: List<String>): SendMessageResponse {
        AppLogger.i("ChatRemoteDataSource.sendMessage called")

        val chatMessages = messages.map { chatMessageMapper.toChatMessage(it) }

        val request = ChatCompletionRequest(
            model = ModelId("llama-3.3-70b-versatile"),
            messages = chatMessages
        )

        AppLogger.d("Request: model=${request.model.id}, messages=${chatMessages.size}")

        val completion = openAI.chatCompletion(request)
        val choice = completion.choices.first()

        AppLogger.d("Completion received, model=${completion.model.id}")

        return SendMessageResponse(
            content = choice.message.content ?: "",
            model = completion.model.id,
            usage = completion.usage?.let {
                UsageDto(
                    promptTokens = it.promptTokens ?: 0,
                    completionTokens = it.completionTokens ?: 0,
                    totalTokens = it.totalTokens ?: 0
                )
            }
        )
    }
}
