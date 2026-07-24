package com.juanpvivas.aichatjp.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

actual fun initKoin(config: KoinAppDeclaration?) {
    startKoin {
        config?.invoke(this)
        modules(commonModule, iosPlatformModule)
    }
}
