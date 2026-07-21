package com.juanpvivas.aichatjp.data.remote.mapper

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import javax.inject.Inject

class ChatMessageMapper @Inject constructor() {

    fun toChatMessage(content: String): ChatMessage {
        return ChatMessage(
            role = ChatRole.User,
            content = content
        )
    }
}
