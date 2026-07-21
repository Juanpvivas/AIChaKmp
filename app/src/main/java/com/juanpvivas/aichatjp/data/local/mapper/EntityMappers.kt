package com.juanpvivas.aichatjp.data.local.mapper

import com.juanpvivas.aichatjp.data.local.entity.ConversationEntity
import com.juanpvivas.aichatjp.data.local.entity.MessageEntity
import com.juanpvivas.aichatjp.data.model.ChatMessage
import com.juanpvivas.aichatjp.data.model.Conversation

fun ConversationEntity.toDomain(): Conversation {
    return Conversation(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Conversation.toEntity(): ConversationEntity {
    return ConversationEntity(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun MessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        conversationId = conversationId,
        content = content,
        isFromUser = isFromUser,
        timestamp = timestamp
    )
}

fun ChatMessage.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        conversationId = conversationId,
        content = content,
        isFromUser = isFromUser,
        timestamp = timestamp
    )
}
