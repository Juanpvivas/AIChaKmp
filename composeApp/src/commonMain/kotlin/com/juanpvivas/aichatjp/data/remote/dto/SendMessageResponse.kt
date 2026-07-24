package com.juanpvivas.aichatjp.data.remote.dto

data class SendMessageResponse(
    val content: String,
    val model: String,
    val usage: UsageDto?
)

data class UsageDto(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)
