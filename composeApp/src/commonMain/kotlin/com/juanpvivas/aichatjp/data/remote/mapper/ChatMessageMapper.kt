package com.juanpvivas.aichatjp.data.remote.mapper

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.core.Usage
import com.juanpvivas.aichatjp.data.remote.dto.SendMessageResponse
import com.juanpvivas.aichatjp.data.remote.dto.UsageDto
import com.juanpvivas.aichatjp.domain.model.ChatMessage as DomainChatMessage

fun String.toChatMessage(role: ChatRole = ChatRole.User): ChatMessage =
    ChatMessage(role = role, content = this)

fun DomainChatMessage.toApiChatMessage(): ChatMessage =
    ChatMessage(
        role = if (isFromUser) ChatRole.User else ChatRole.Assistant,
        content = content
    )

fun SendMessageResponse.toDomain(): DomainChatMessage =
    DomainChatMessage(
        content = content,
        isFromUser = false
    )

fun Usage?.toDto(): UsageDto? =
    this?.let {
        UsageDto(
            promptTokens = it.promptTokens ?: 0,
            completionTokens = it.completionTokens ?: 0,
            totalTokens = it.totalTokens ?: 0
        )
    }
