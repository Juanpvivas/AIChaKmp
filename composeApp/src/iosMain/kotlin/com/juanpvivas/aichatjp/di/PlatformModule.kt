package com.juanpvivas.aichatjp.di

import androidx.room.Room
import com.juanpvivas.aichatjp.data.local.database.AiChaDatabase
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Suppress("UNCHECKED_CAST")
val iosPlatformModule =
    module {
        single<AiChaDatabase> {
            val fileManager = NSFileManager.defaultManager
            val urls =
                fileManager.URLsForDirectory(
                    NSDocumentDirectory,
                    NSUserDomainMask,
                ) as List<NSURL>
            val dbPath = urls.first().path + "/aicha_database"
            Room.databaseBuilder<AiChaDatabase>(name = dbPath)
                .build()
        }
    }
