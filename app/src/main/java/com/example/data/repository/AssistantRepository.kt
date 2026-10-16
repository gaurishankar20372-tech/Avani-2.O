package com.example.data.repository

import com.example.data.local.ActionHistoryDao
import com.example.data.local.ConversationDao
import com.example.data.local.MemoryDao
import com.example.data.local.ProjectDao
import com.example.data.local.TaskDao
import com.example.data.model.ActionHistoryEntry
import com.example.data.model.AssistantTask
import com.example.data.model.ConversationMessage
import com.example.data.model.ProjectNote
import com.example.data.model.ProjectRecord
import com.example.data.model.SavedMemory
import kotlinx.coroutines.flow.Flow

class AssistantRepository(
    private val conversationDao: ConversationDao,
    private val projectDao: ProjectDao,
    private val memoryDao: MemoryDao,
    private val taskDao: TaskDao,
    private val actionHistoryDao: ActionHistoryDao
) {
    // Conversation
    val allMessages: Flow<List<ConversationMessage>> = conversationDao.getAllMessages()
    suspend fun getRecentMessages(limit: Int = 10): List<ConversationMessage> = conversationDao.getRecentMessages(limit)
    suspend fun saveMessage(message: ConversationMessage): Long = conversationDao.insertMessage(message)
    suspend fun clearConversation() = conversationDao.clearAll()

    // Projects (Project Memory)
    val allProjects: Flow<List<ProjectRecord>> = projectDao.getAllProjects()
    suspend fun getProjectById(id: Long): ProjectRecord? = projectDao.getProjectById(id)
    suspend fun searchProjects(query: String): List<ProjectRecord> = projectDao.searchProjects(query)
    suspend fun saveProject(project: ProjectRecord): Long = projectDao.insertProject(project)
    suspend fun updateProject(project: ProjectRecord) = projectDao.updateProject(project)
    suspend fun deleteProject(project: ProjectRecord) = projectDao.deleteProject(project)
    fun getProjectNotes(projectId: Long): Flow<List<ProjectNote>> = projectDao.getNotesForProject(projectId)
    suspend fun addProjectNote(note: ProjectNote): Long = projectDao.insertProjectNote(note)

    // Personal Memories
    val allMemories: Flow<List<SavedMemory>> = memoryDao.getAllMemories()
    fun getMemoriesByCategory(category: String): Flow<List<SavedMemory>> = memoryDao.getMemoriesByCategory(category)
    suspend fun searchMemories(query: String): List<SavedMemory> = memoryDao.searchMemories(query)
    suspend fun saveMemory(memory: SavedMemory): Long = memoryDao.insertMemory(memory)
    suspend fun updateMemory(memory: SavedMemory) = memoryDao.updateMemory(memory)
    suspend fun deleteMemory(memory: SavedMemory) = memoryDao.deleteMemory(memory)
    suspend fun clearMemories() = memoryDao.clearAll()

    // Tasks & Reminders
    val allTasks: Flow<List<AssistantTask>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<AssistantTask>> = taskDao.getPendingTasks()
    suspend fun saveTask(task: AssistantTask): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: AssistantTask) = taskDao.updateTask(task)
    suspend fun deleteTask(task: AssistantTask) = taskDao.deleteTask(task)
    suspend fun clearCompletedTasks() = taskDao.deleteCompletedTasks()

    // Action History
    val allHistory: Flow<List<ActionHistoryEntry>> = actionHistoryDao.getAllHistory()
    fun getHistoryByState(state: String): Flow<List<ActionHistoryEntry>> = actionHistoryDao.getHistoryByState(state)
    suspend fun logAction(entry: ActionHistoryEntry): Long = actionHistoryDao.insertAction(entry)
    suspend fun clearHistory() = actionHistoryDao.clearHistory()
}
