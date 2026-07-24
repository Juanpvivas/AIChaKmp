package com.juanpvivas.aichatjp.data.local

import android.content.Context
import androidx.room.Room
import com.juanpvivas.aichatjp.data.local.database.AiChaDatabase

actual fun createRoomDatabase(): AiChaDatabase {
    val context = Class.forName("com.juanpvivas.aichatjp.AppKt")
        .getField("applicationContext")
        .get(null) as Context
    return Room.databaseBuilder(context, AiChaDatabase::class.java, "aicha_database")
        .build()
}
