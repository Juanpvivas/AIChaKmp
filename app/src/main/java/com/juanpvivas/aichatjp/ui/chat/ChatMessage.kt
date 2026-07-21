package com.juanpvivas.aichatjp.ui.chat

/** Modelo simple de un mensaje del chat. */
data class ChatMessage(
    val id: Long,
    val text: String,
    val fromUser: Boolean,
    val time: String,
)
