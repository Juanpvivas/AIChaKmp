package com.juanpvivas.aichatjp.core

actual fun groqApiKey(): String {
    val BuildConfig = Class.forName("com.juanpvivas.aichatjp.BuildConfig")
    return BuildConfig.getField("GROQ_API_KEY").get(null) as? String ?: ""
}
