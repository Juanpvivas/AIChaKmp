package com.juanpvivas.aichatjp.domain.model

import kotlin.time.Clock
import kotlin.time.Instant

data class ChatMessage(
    val id: Long = 0,
    val conversationId: Long = 0,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Instant = Clock.System.now()
)
