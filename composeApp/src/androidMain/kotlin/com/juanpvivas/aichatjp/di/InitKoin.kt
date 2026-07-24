package com.juanpvivas.aichatjp.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

lateinit var androidContext: Context

actual fun initKoin(config: KoinAppDeclaration?) {
    startKoin {
        androidContext(androidContext)
        config?.invoke(this)
        modules(commonModule, androidPlatformModule)
    }
}
