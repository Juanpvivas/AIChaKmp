package com.juanpvivas.aichatjp.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

expect fun initKoin(config: KoinAppDeclaration? = null)
