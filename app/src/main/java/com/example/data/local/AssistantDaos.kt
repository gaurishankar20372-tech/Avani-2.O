package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActionHistoryEntry
import com.example.data.model.AssistantTask
import com.example.data.model.ConversationMessage
import com.example.data.model.ProjectNote
import com.example.data.model.ProjectRecord
import com.example.data.model.SavedMemory
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ConversationMessage>>

    @Query("SELECT * FROM conversation_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ConversationMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationMessage): Long

    @Query("DELETE FROM conversation_messages")
    suspend fun clearAll()
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM project_records ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectRecord>>

    @Query("SELECT * FROM project_records WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectRecord?

    @Query("SELECT * FROM project_records WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    suspend fun searchProjects(query: String): List<ProjectRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectRecord): Long

    @Update
    suspend fun updateProject(project: ProjectRecord)

    @Delete
    suspend fun deleteProject(project: ProjectRecord)

    @Query("SELECT * FROM project_notes WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getNotesForProject(projectId: Long): Flow<List<ProjectNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectNote(note: ProjectNote): Long
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM saved_memories ORDER BY updatedAt DESC")
    fun getAllMemories(): Flow<List<SavedMemory>>

    @Query("SELECT * FROM saved_memories WHERE category = :category ORDER BY updatedAt DESC")
    fun getMemoriesByCategory(category: String): Flow<List<SavedMemory>>

    @Query("SELECT * FROM saved_memories WHERE content LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<SavedMemory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: SavedMemory): Long

    @Update
    suspend fun updateMemory(memory: SavedMemory)

    @Delete
    suspend fun deleteMemory(memory: SavedMemory)

    @Query("DELETE FROM saved_memories")
    suspend fun clearAll()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM assistant_tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<AssistantTask>>

    @Query("SELECT * FROM assistant_tasks WHERE isCompleted = 0 ORDER BY dueAt ASC, createdAt DESC")
    fun getPendingTasks(): Flow<List<AssistantTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AssistantTask): Long

    @Update
    suspend fun updateTask(task: AssistantTask)

    @Delete
    suspend fun deleteTask(task: AssistantTask)

    @Query("DELETE FROM assistant_tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()
}

@Dao
interface ActionHistoryDao {
    @Query("SELECT * FROM action_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ActionHistoryEntry>>

    @Query("SELECT * FROM action_history WHERE executionState = :state ORDER BY timestamp DESC")
    fun getHistoryByState(state: String): Flow<List<ActionHistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(entry: ActionHistoryEntry): Long

    @Query("DELETE FROM action_history")
    suspend fun clearHistory()
}
