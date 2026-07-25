package com.juanpvivas.aichatjp.core

actual fun groqApiKey(): String {
    val buildConfigClass = Class.forName("com.juanpvivas.aichatjp.BuildConfig")
    return buildConfigClass.getField("GROQ_API_KEY").get(null) as? String ?: ""
}
