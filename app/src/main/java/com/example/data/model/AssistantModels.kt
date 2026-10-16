package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Roles in conversation: user, assistant, system, tool
 */
@Entity(tableName = "conversation_messages")
data class ConversationMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolName: String? = null,
    val toolStatus: String? = null, // "pending", "success", "failed"
    val isSpoken: Boolean = false
)

/**
 * Persistent project record stored in Project Memory
 */
@Entity(tableName = "project_records")
data class ProjectRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val technologyStack: String = "Kotlin, Jetpack Compose",
    val completedTasks: String = "", // Comma- or newline-separated
    val pendingTasks: String = "",
    val knownIssues: String = "",
    val stage: String = "In Progress", // "Planning", "In Progress", "Testing", "Completed"
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Notes associated with a specific project
 */
@Entity(tableName = "project_notes")
data class ProjectNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Personal memory preferences and user-approved facts
 */
@Entity(tableName = "saved_memories")
data class SavedMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "Preference", "Fact", "Language", "Voice", "Custom"
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Tasks, notes, and scheduled reminders
 */
@Entity(tableName = "assistant_tasks")
data class AssistantTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueAt: Long? = null,
    val priority: String = "Medium", // "Low", "Medium", "High"
    val isCompleted: Boolean = false,
    val isReminder: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Action history entry for audit trail
 */
@Entity(tableName = "action_history")
data class ActionHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String, // "OPEN_APP", "CALL", "MESSAGE", "SEARCH", "REMINDER", "PROJECT_UPDATE", "CODE_GEN"
    val target: String,
    val confirmationState: String, // "NOT_REQUIRED", "CONFIRMED", "CANCELLED", "REJECTED"
    val executionState: String, // "SUCCESS", "FAILED", "BLOCKED_PERMISSION", "STOPPED"
    val resultSummary: String,
    val riskLevel: String = "LOW" // "LOW", "MODERATE", "HIGH"
)

/**
 * Dynamic capability discovery item
 */
data class CapabilityRecord(
    val id: String,
    val name: String,
    val availability: CapabilityStatus,
    val requiredPermissions: List<String> = emptyList(),
    val explanation: String,
    val category: String // "Phone Control", "Productivity", "Intelligence", "Safety"
)

enum class CapabilityStatus {
    AVAILABLE_NOW,
    REQUIRES_PERMISSION,
    REQUIRES_CONFIGURATION,
    NOT_SUPPORTED
}

enum class RiskLevel {
    LOW,
    MODERATE,
    HIGH
}

data class PendingAction(
    val actionType: String,
    val target: String,
    val details: String,
    val riskLevel: RiskLevel,
    val executeBlock: suspend () -> ActionResult
)

data class ActionResult(
    val success: Boolean,
    val message: String,
    val executionState: String = if (success) "SUCCESS" else "FAILED"
)
