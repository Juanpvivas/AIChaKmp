package com.juanpvivas.aichatjp.di

import com.juanpvivas.aichatjp.data.remote.config.GroqConfigImpl
import com.juanpvivas.aichatjp.data.remote.config.GroqModelResolver
import com.juanpvivas.aichatjp.domain.config.GroqConfig
import com.juanpvivas.aichatjp.domain.model.GroqPreferences
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val groqConfigModule =
    module {
        singleOf(::GroqModelResolver)
        single { GroqPreferences() }
        singleOf(::GroqConfigImpl) bind GroqConfig::class
    }
