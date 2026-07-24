package com.juanpvivas.aichatjp.ui.navigation

object Routes {
    const val CHAT = "chat/{conversationId}"
    const val HISTORY = "history"

    fun chat(conversationId: Long = 0L) = "chat/$conversationId"
}
