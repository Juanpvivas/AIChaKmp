package com.juanpvivas.aichatjp.domain.model

data class GroqModel(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val inputModalities: List<String>,
    val outputModalities: List<String>,
    val contextWindow: Int,
    val maxCompletionTokens: Int,
)
