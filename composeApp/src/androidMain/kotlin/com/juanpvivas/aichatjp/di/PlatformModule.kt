package com.juanpvivas.aichatjp.di

import com.juanpvivas.aichatjp.data.local.createRoomDatabase
import com.juanpvivas.aichatjp.data.local.database.AiChaDatabase
import org.koin.dsl.module

val androidPlatformModule = module {
    single<AiChaDatabase> { createRoomDatabase() }
}
