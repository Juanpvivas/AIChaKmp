package com.juanpvivas.aichatjp.di

import android.content.Context
import androidx.room.Room
import com.juanpvivas.aichatjp.data.local.database.AiChaDatabase
import org.koin.dsl.module

val androidPlatformModule = module {
    single<AiChaDatabase> {
        val context = get<Context>()
        Room.databaseBuilder(context, AiChaDatabase::class.java, "aicha_database")
            .build()
    }
}
