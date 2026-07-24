package com.juanpvivas.aichatjp.data.local

import androidx.room.Room
import com.juanpvivas.aichatjp.data.local.database.AiChaDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Suppress("UNCHECKED_CAST")
actual fun createRoomDatabase(): AiChaDatabase {
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(
        NSDocumentDirectory,
        NSUserDomainMask
    ) as List<NSURL>
    val dbPath = urls.first().path + "/aicha_database"
    return Room.databaseBuilder<AiChaDatabase>(name = dbPath)
        .build()
}
