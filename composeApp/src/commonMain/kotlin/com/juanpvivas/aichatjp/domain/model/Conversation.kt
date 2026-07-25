package com.juanpvivas.aichatjp.domain.model

import kotlin.time.Clock
import kotlin.time.Instant

data class Conversation(
    val id: Long = 0,
    val title: String,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
)
