package com.example.actions

import com.example.data.model.ActionHistoryEntry
import com.example.data.model.ActionResult
import com.example.data.model.AssistantTask
import com.example.data.model.PendingAction
import com.example.data.model.ProjectRecord
import com.example.data.model.RiskLevel
import com.example.data.model.SavedMemory
import com.example.data.repository.AssistantRepository

sealed class RoutingDecision {
    data class Executed(val result: ActionResult) : RoutingDecision()
    data class NeedsConfirmation(val pendingAction: PendingAction) : RoutingDecision()
    data class Information(val message: String) : RoutingDecision()
}

class ActionRouter(
    private val executor: AndroidActionExecutor,
    private val repository: AssistantRepository
) {
    var isExplainBeforeActionMode: Boolean = false

    suspend fun routeToolCall(
        functionName: String,
        arguments: Map<String, Any?>
    ): RoutingDecision {
        return when (functionName) {
            "open_app" -> {
                val appName = arguments["appName"]?.toString() ?: "App"
                val pkgName = arguments["packageName"]?.toString()
                val risk = RiskLevel.LOW
                val executeBlock: suspend () -> ActionResult = {
                    val res = executor.openApp(appName, pkgName)
                    logHistory("OPEN_APP", appName, "CONFIRMED", res, risk)
                    res
                }

                if (isExplainBeforeActionMode) {
                    RoutingDecision.NeedsConfirmation(
                        PendingAction("OPEN_APP", appName, "Open $appName", risk, executeBlock)
                    )
                } else {
                    RoutingDecision.Executed(executeBlock())
                }
            }

            "initiate_call" -> {
                val contactName = arguments["contactName"]?.toString() ?: "Unknown"
                val directPhone = arguments["phoneNumber"]?.toString()
                val candidates = if (directPhone.isNullOrBlank()) {
                    executor.contactResolver.findContacts(contactName)
                } else emptyList()

                val phoneToCall = directPhone ?: candidates.firstOrNull()?.phoneNumber ?: ""
                val displayName = candidates.firstOrNull()?.name ?: contactName
                val risk = RiskLevel.HIGH

                if (phoneToCall.isBlank()) {
                    // Open dialer directly
                    val res = executor.openDialer("", contactName)
                    logHistory("CALL", contactName, "NOT_REQUIRED", res, risk)
                    return RoutingDecision.Executed(res)
                }

                val executeBlock: suspend () -> ActionResult = {
                    val res = executor.openDialer(phoneToCall, displayName)
                    logHistory("CALL", "$displayName ($phoneToCall)", "CONFIRMED", res, risk)
                    res
                }

                // Calling is high risk - always requires confirmation
                RoutingDecision.NeedsConfirmation(
                    PendingAction(
                        actionType = "CALL",
                        target = displayName,
                        details = "Place a call to $displayName at $phoneToCall",
                        riskLevel = risk,
                        executeBlock = executeBlock
                    )
                )
            }

            "prepare_message" -> {
                val app = arguments["application"]?.toString() ?: "whatsapp"
                val recipient = arguments["recipient"]?.toString() ?: "Friend"
                val message = arguments["message"]?.toString() ?: ""
                val risk = RiskLevel.HIGH

                val executeBlock: suspend () -> ActionResult = {
                    val res = if (app.equals("whatsapp", ignoreCase = true)) {
                        executor.prepareWhatsAppMessage(null, message)
                    } else {
                        executor.shareText(message)
                    }
                    logHistory("MESSAGE", "$app to $recipient", "CONFIRMED", res, risk)
                    res
                }

                RoutingDecision.NeedsConfirmation(
                    PendingAction(
                        actionType = "MESSAGE",
                        target = "$app -> $recipient",
                        details = "Send $app message: \"$message\"",
                        riskLevel = risk,
                        executeBlock = executeBlock
                    )
                )
            }

            "search_web" -> {
                val query = arguments["query"]?.toString() ?: ""
                val risk = RiskLevel.LOW
                val res = executor.searchWeb(query)
                logHistory("SEARCH", query, "NOT_REQUIRED", res, risk)
                RoutingDecision.Executed(res)
            }

            "create_task" -> {
                val title = arguments["title"]?.toString() ?: "New Task"
                val priority = arguments["priority"]?.toString() ?: "Medium"
                repository.saveTask(
                    AssistantTask(
                        title = title,
                        priority = priority
                    )
                )
                val res = ActionResult(true, "Task '$title' save ho gaya hai.")
                logHistory("TASK", title, "NOT_REQUIRED", res, RiskLevel.LOW)
                RoutingDecision.Executed(res)
            }

            "create_reminder" -> {
                val title = arguments["title"]?.toString() ?: "Reminder"
                val reminderTime = arguments["reminderTime"]?.toString() ?: "Soon"
                repository.saveTask(
                    AssistantTask(
                        title = title,
                        description = "Time: $reminderTime",
                        isReminder = true
                    )
                )
                val res = ActionResult(true, "Remind karungi: '$title' ($reminderTime)")
                logHistory("REMINDER", title, "NOT_REQUIRED", res, RiskLevel.MODERATE)
                RoutingDecision.Executed(res)
            }

            "search_project_memory" -> {
                val query = arguments["query"]?.toString() ?: ""
                val projects = repository.searchProjects(query)
                val responseMsg = if (projects.isNotEmpty()) {
                    val p = projects.first()
                    "Project Memory found: '${p.name}' (${p.technologyStack}). Stage: ${p.stage}. Completed: ${p.completedTasks.ifBlank { "None yet" }}. Pending: ${p.pendingTasks.ifBlank { "All set" }}."
                } else {
                    "No project record found for '$query'. Would you like me to create this project in Project Memory?"
                }
                RoutingDecision.Information(responseMsg)
            }

            "create_project" -> {
                val name = arguments["name"]?.toString() ?: "Untitled Project"
                val desc = arguments["description"]?.toString() ?: ""
                val tech = arguments["technologyStack"]?.toString() ?: "Kotlin, Compose"
                repository.saveProject(
                    ProjectRecord(
                        name = name,
                        description = desc,
                        technologyStack = tech
                    )
                )
                val res = ActionResult(true, "Project '$name' Project Memory mein save kar liya gaya hai.")
                logHistory("PROJECT", name, "NOT_REQUIRED", res, RiskLevel.MODERATE)
                RoutingDecision.Executed(res)
            }

            "save_personal_memory" -> {
                val category = arguments["category"]?.toString() ?: "Preference"
                val content = arguments["content"]?.toString() ?: ""
                repository.saveMemory(
                    SavedMemory(
                        category = category,
                        content = content
                    )
                )
                val res = ActionResult(true, "Yad rakh liya gaya hai: '$content'")
                logHistory("MEMORY", category, "NOT_REQUIRED", res, RiskLevel.LOW)
                RoutingDecision.Executed(res)
            }

            "get_device_capabilities" -> {
                val info = "Avani 2.O Capabilities:\n1. Open phone apps (WhatsApp, YouTube, Chrome, Camera, Settings)\n2. Dial calls & contact resolution\n3. Prepare WhatsApp & SMS messages\n4. Search Google & web queries\n5. Project Memory (remember tech stack, tasks, bugs)\n6. Personal Memory (preferences, facts)\n7. Notes & Reminders\n8. Production Code Builder & Debugger\n9. Emergency Stop & Explain-Before-Action security"
                RoutingDecision.Information(info)
            }

            else -> {
                RoutingDecision.Information("Tool $functionName request received.")
            }
        }
    }

    private suspend fun logHistory(
        actionType: String,
        target: String,
        confirmationState: String,
        result: ActionResult,
        riskLevel: RiskLevel
    ) {
        repository.logAction(
            ActionHistoryEntry(
                actionType = actionType,
                target = target,
                confirmationState = confirmationState,
                executionState = result.executionState,
                resultSummary = result.message,
                riskLevel = riskLevel.name
            )
        )
    }
}
