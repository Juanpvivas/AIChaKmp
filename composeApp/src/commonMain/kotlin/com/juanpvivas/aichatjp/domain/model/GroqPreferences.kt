package com.juanpvivas.aichatjp.domain.model

data class GroqPreferences(
    val preferredModelId: String? = null,
    val autoDetectModels: Boolean = true,
    val cacheModels: Boolean = true,
)
