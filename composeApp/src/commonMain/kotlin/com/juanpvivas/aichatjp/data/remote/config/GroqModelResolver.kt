package com.juanpvivas.aichatjp.data.remote.config

import com.juanpvivas.aichatjp.domain.model.GroqModel

class GroqModelResolver {
    private companion object {
        const val DEFAULT_MODEL = "qwen/qwen3.6-27b"
        val EXCLUDED_MODEL_PATTERNS = listOf("whisper", "prompt-guard", "tts", "dall-e")
        val PREFERRED_MODELS = listOf("qwen", "llama", "mixtral", "gemma")
    }

    fun resolveBestChatModel(
        models: List<GroqModel>,
        preferredModelId: String? = null,
    ): String {
        if (models.isEmpty()) {
            return preferredModelId ?: DEFAULT_MODEL
        }

        preferredModelId?.let { preferred ->
            val preferredModel = models.find { it.id == preferred && it.isActive }
            if (preferredModel != null) {
                return preferred
            }
        }

        val chatModels =
            models.filter { model ->
                model.isActive &&
                    "text" in model.inputModalities &&
                    "text" in model.outputModalities &&
                    EXCLUDED_MODEL_PATTERNS.none { pattern ->
                        model.id.contains(pattern, ignoreCase = true)
                    }
            }

        if (chatModels.isEmpty()) {
            return preferredModelId ?: DEFAULT_MODEL
        }

        val bestModel =
            chatModels.sortedWith(
                compareByDescending<GroqModel> { model ->
                    PREFERRED_MODELS.indexOfFirst { preferred ->
                        model.id.contains(preferred, ignoreCase = true)
                    }.let { if (it == -1) Int.MIN_VALUE else it }
                }.thenByDescending { it.contextWindow },
            ).first()

        return bestModel.id
    }
}
