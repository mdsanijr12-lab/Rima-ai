package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.ConversationDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity

@Database(
    entities = [ConversationEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RimaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: RimaDatabase? = null

        fun getDatabase(context: Context): RimaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RimaDatabase::class.java,
                    "rima_ai_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
