package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ActionHistoryEntry
import com.example.data.model.AssistantTask
import com.example.data.model.ConversationMessage
import com.example.data.model.ProjectNote
import com.example.data.model.ProjectRecord
import com.example.data.model.SavedMemory

@Database(
    entities = [
        ConversationMessage::class,
        ProjectRecord::class,
        ProjectNote::class,
        SavedMemory::class,
        AssistantTask::class,
        ActionHistoryEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AvaniDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun projectDao(): ProjectDao
    abstract fun memoryDao(): MemoryDao
    abstract fun taskDao(): TaskDao
    abstract fun actionHistoryDao(): ActionHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AvaniDatabase? = null

        fun getDatabase(context: Context): AvaniDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AvaniDatabase::class.java,
                    "avani_assistant_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
