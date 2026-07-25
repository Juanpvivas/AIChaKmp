package com.juanpvivas.aichatjp.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.juanpvivas.aichatjp.data.local.dao.ConversationDao
import com.juanpvivas.aichatjp.data.local.dao.MessageDao
import com.juanpvivas.aichatjp.data.local.entity.ConversationEntity
import com.juanpvivas.aichatjp.data.local.entity.MessageEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false,
)
@ConstructedBy(AiChaDatabaseConstructor::class)
abstract class AiChaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao

    abstract fun messageDao(): MessageDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AiChaDatabaseConstructor : RoomDatabaseConstructor<AiChaDatabase> {
    override fun initialize(): AiChaDatabase
}
