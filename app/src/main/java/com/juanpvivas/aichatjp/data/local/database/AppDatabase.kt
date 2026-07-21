package com.juanpvivas.aichatjp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.juanpvivas.aichatjp.data.local.dao.ConversationDao
import com.juanpvivas.aichatjp.data.local.dao.MessageDao
import com.juanpvivas.aichatjp.data.local.entity.ConversationEntity
import com.juanpvivas.aichatjp.data.local.entity.MessageEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
