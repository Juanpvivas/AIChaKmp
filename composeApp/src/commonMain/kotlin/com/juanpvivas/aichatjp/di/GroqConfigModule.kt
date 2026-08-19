package com.juanpvivas.aichatjp.di

import com.juanpvivas.aichatjp.data.remote.config.GroqConfigImpl
import com.juanpvivas.aichatjp.domain.config.GroqConfig
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val groqConfigModule =
    module {
        singleOf(::GroqConfigImpl) bind GroqConfig::class
    }
