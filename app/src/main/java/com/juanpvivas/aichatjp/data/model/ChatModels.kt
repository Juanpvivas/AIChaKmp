package com.juanpvivas.aichatjp.data.model

data class Conversation(
    val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: Long = 0,
    val conversationId: Long = 0,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
