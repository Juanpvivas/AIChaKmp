package com.juanpvivas.aichatjp.core

import platform.Foundation.NSBundle

actual fun groqApiKey(): String = NSBundle.mainBundle.infoDictionary?.get("GROQ_API_KEY") as? String ?: ""
