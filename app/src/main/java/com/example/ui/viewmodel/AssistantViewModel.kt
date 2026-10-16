package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.actions.ActionRouter
import com.example.actions.AndroidActionExecutor
import com.example.actions.RoutingDecision
import com.example.data.local.AvaniDatabase
import com.example.data.model.ActionHistoryEntry
import com.example.data.model.AssistantTask
import com.example.data.model.CapabilityRecord
import com.example.data.model.ConversationMessage
import com.example.data.model.PendingAction
import com.example.data.model.ProjectNote
import com.example.data.model.ProjectRecord
import com.example.data.model.SavedMemory
import com.example.data.repository.AssistantRepository
import com.example.gemini.GeminiAssistantService
import com.example.gemini.GeminiResponse
import com.example.security.EmergencyStopManager
import com.example.security.PrivacyManager
import com.example.voice.VoiceManager
import com.example.voice.VoiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppMode {
    VOICE_MODE, // Live Human-Like Voice Conversation (Gemini Live / ChatGPT Voice style)
    CHAT_MODE   // Pure Text Chat (ChatGPT / Google Gemini text style)
}

data class AssistantUiState(
    val appMode: AppMode = AppMode.VOICE_MODE,
    val isLiveContinuousLoop: Boolean = true,
    val liveUserSubtitle: String = "",
    val liveAssistantSubtitle: String = "",
    val isAssistantActive: Boolean = true,
    val isEmergencyStopped: Boolean = false,
    val voiceState: VoiceState = VoiceState.IDLE,
    val audioRms: Float = 0f,
    val statusText: String = "Ready",
    val pendingConfirmation: PendingAction? = null,
    val isExplainBeforeAction: Boolean = false,
    val isOfflineMode: Boolean = false,
    val isBackgroundServiceActive: Boolean = false,
    val isVoiceOutputEnabled: Boolean = true,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val generatedCodeOutput: String? = null,
    val codeGenerationStage: String? = null
)

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AvaniDatabase.getDatabase(application)
    val repository = AssistantRepository(
        database.conversationDao(),
        database.projectDao(),
        database.memoryDao(),
        database.taskDao(),
        database.actionHistoryDao()
    )

    val voiceManager = VoiceManager(application)
    private val actionExecutor = AndroidActionExecutor(application)
    private val actionRouter = ActionRouter(actionExecutor, repository)
    val privacyManager = PrivacyManager(application)
    private val emergencyStopManager = EmergencyStopManager()
    private val geminiService = GeminiAssistantService()

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    // Database reactive streams
    val messages: StateFlow<List<ConversationMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectRecord>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<AssistantTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<SavedMemory>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val actionHistory: StateFlow<List<ActionHistoryEntry>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe voice state and audio levels
        viewModelScope.launch {
            combine(
                voiceManager.voiceState,
                voiceManager.audioRms,
                emergencyStopManager.isAssistantActive,
                emergencyStopManager.isEmergencyStopped
            ) { voiceState, rms, active, stopped ->
                _uiState.value = _uiState.value.copy(
                    voiceState = voiceState,
                    audioRms = rms,
                    isAssistantActive = active,
                    isEmergencyStopped = stopped,
                    statusText = when {
                        stopped -> "EMERGENCY STOPPED"
                        !active -> "Assistant is OFF"
                        voiceState == VoiceState.LISTENING -> "Listening to you..."
                        voiceState == VoiceState.PROCESSING -> "Avani reasoning..."
                        voiceState == VoiceState.EXECUTING -> "Executing action..."
                        voiceState == VoiceState.SPEAKING -> "Avani speaking..."
                        else -> "Avani 2.O ready"
                    }
                )
            }.collect {}
        }

        // Observe Background Service state
        viewModelScope.launch {
            com.example.service.AvaniBackgroundService.isRunning.collect { running ->
                _uiState.value = _uiState.value.copy(isBackgroundServiceActive = running)
            }
        }

        // Seed default starter project and memory if empty
        viewModelScope.launch {
            val existing = repository.getRecentMessages(1)
            if (existing.isEmpty()) {
                repository.saveMessage(
                    ConversationMessage(
                        role = "assistant",
                        content = "नमस्ते! मैं अवनी 2.O हूँ, आपकी इंटेलिजेंट पर्सनल वॉइस असिस्टेंट। आप मुझसे बोलकर ऐप खोल सकते हैं, कॉल/मैसेज कर सकते हैं, कोड बनवा सकते हैं और प्रोजेक्ट्स ट्रैक कर सकते हैं। नीचे माइक बटन दबाकर बोलें!"
                    )
                )
                repository.saveProject(
                    ProjectRecord(
                        name = "Avani 2.O Android",
                        description = "Next-generation personal voice assistant with Gemini NLU and Android automation.",
                        technologyStack = "Kotlin, Jetpack Compose, Room, OkHttp",
                        completedTasks = "Voice interface, Phone action router, Project memory, Emergency stop",
                        pendingTasks = "Add more custom automation recipes",
                        stage = "Active Development"
                    )
                )
                repository.saveMemory(
                    SavedMemory(
                        category = "Preference",
                        content = "Responds in Hindi, Hinglish, or English automatically based on user's query."
                    )
                )
                repository.saveTask(
                    AssistantTask(
                        title = "Explore Avani 2.O Voice Commands",
                        description = "Try saying 'YouTube kholo' or 'Avani, ek reminder banao'.",
                        priority = "High"
                    )
                )
            }
        }
    }

    fun startListening() {
        if (!_uiState.value.isAssistantActive || _uiState.value.isEmergencyStopped) {
            return
        }

        voiceManager.startListening(
            onResult = { text ->
                processUserInput(text)
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(statusText = error)
            }
        )
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun setAppMode(mode: AppMode) {
        _uiState.value = _uiState.value.copy(appMode = mode)
        if (mode == AppMode.CHAT_MODE) {
            voiceManager.stopSpeech()
            voiceManager.stopListening()
        }
    }

    fun toggleLiveContinuousLoop(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isLiveContinuousLoop = enabled)
    }

    fun processUserInput(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank() || !_uiState.value.isAssistantActive || _uiState.value.isEmergencyStopped) {
            return
        }

        val isVoiceMode = _uiState.value.appMode == AppMode.VOICE_MODE

        viewModelScope.launch {
            // Save user message
            repository.saveMessage(
                ConversationMessage(
                    role = "user",
                    content = trimmed
                )
            )

            _uiState.value = _uiState.value.copy(
                statusText = if (isVoiceMode) "Avani thinking..." else "Generating response...",
                voiceState = VoiceState.PROCESSING,
                liveUserSubtitle = trimmed
            )

            // Prepare memory context
            val recentMemories = memories.value.take(4).joinToString("; ") { it.content }
            val recentHistory = messages.value.takeLast(6).map { it.role to it.content }

            val response = geminiService.processUserTurn(
                userPrompt = trimmed,
                recentHistory = recentHistory,
                systemMemoryContext = recentMemories,
                isVoiceMode = isVoiceMode
            )

            when (response) {
                is GeminiResponse.FunctionCall -> {
                    handleFunctionCall(response.functionName, response.arguments, isVoiceMode)
                }

                is GeminiResponse.Text -> {
                    repository.saveMessage(
                        ConversationMessage(
                            role = "assistant",
                            content = response.reply
                        )
                    )
                    _uiState.value = _uiState.value.copy(
                        statusText = "Ready",
                        voiceState = VoiceState.IDLE,
                        liveAssistantSubtitle = response.reply
                    )
                    if (isVoiceMode) {
                        voiceManager.speak(response.reply) {
                            if (_uiState.value.appMode == AppMode.VOICE_MODE &&
                                _uiState.value.isLiveContinuousLoop &&
                                !_uiState.value.isEmergencyStopped &&
                                _uiState.value.isAssistantActive
                            ) {
                                startListening()
                            }
                        }
                    }
                }

                is GeminiResponse.Error -> {
                    val errMsg = "Error: ${response.message}"
                    repository.saveMessage(
                        ConversationMessage(
                            role = "assistant",
                            content = errMsg
                        )
                    )
                    _uiState.value = _uiState.value.copy(
                        statusText = errMsg,
                        voiceState = VoiceState.IDLE,
                        liveAssistantSubtitle = errMsg
                    )
                    if (isVoiceMode) {
                        voiceManager.speak(errMsg)
                    }
                }
            }
        }
    }

    private suspend fun handleFunctionCall(name: String, args: Map<String, Any?>, isVoiceMode: Boolean = false) {
        actionRouter.isExplainBeforeActionMode = _uiState.value.isExplainBeforeAction

        when (val decision = actionRouter.routeToolCall(name, args)) {
            is RoutingDecision.Executed -> {
                val result = decision.result
                repository.saveMessage(
                    ConversationMessage(
                        role = "assistant",
                        content = result.message,
                        toolName = name,
                        toolStatus = if (result.success) "success" else "failed"
                    )
                )
                _uiState.value = _uiState.value.copy(
                    statusText = result.message,
                    voiceState = VoiceState.IDLE,
                    liveAssistantSubtitle = result.message
                )
                if (isVoiceMode) {
                    voiceManager.speak(result.message) {
                        if (_uiState.value.appMode == AppMode.VOICE_MODE &&
                            _uiState.value.isLiveContinuousLoop &&
                            !_uiState.value.isEmergencyStopped &&
                            _uiState.value.isAssistantActive
                        ) {
                            startListening()
                        }
                    }
                }
            }

            is RoutingDecision.NeedsConfirmation -> {
                voiceManager.stopSpeech()
                _uiState.value = _uiState.value.copy(
                    pendingConfirmation = decision.pendingAction,
                    voiceState = VoiceState.AWAITING_CONFIRMATION,
                    statusText = "Confirmation needed: ${decision.pendingAction.details}",
                    liveAssistantSubtitle = "Confirmation needed: ${decision.pendingAction.details}"
                )
                if (isVoiceMode) {
                    val prompt = "Aapki permission chahiye: ${decision.pendingAction.details}. Kya aage badhoon?"
                    voiceManager.speak(prompt)
                }
            }

            is RoutingDecision.Information -> {
                repository.saveMessage(
                    ConversationMessage(
                        role = "assistant",
                        content = decision.message,
                        toolName = name,
                        toolStatus = "success"
                    )
                )
                _uiState.value = _uiState.value.copy(
                    statusText = "Ready",
                    voiceState = VoiceState.IDLE,
                    liveAssistantSubtitle = decision.message
                )
                if (isVoiceMode) {
                    voiceManager.speak(decision.message) {
                        if (_uiState.value.appMode == AppMode.VOICE_MODE &&
                            _uiState.value.isLiveContinuousLoop &&
                            !_uiState.value.isEmergencyStopped &&
                            _uiState.value.isAssistantActive
                        ) {
                            startListening()
                        }
                    }
                }
            }
        }
    }

    fun confirmPendingAction() {
        val pending = _uiState.value.pendingConfirmation ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                pendingConfirmation = null,
                voiceState = VoiceState.EXECUTING,
                statusText = "Executing action..."
            )
            val result = pending.executeBlock()
            repository.saveMessage(
                ConversationMessage(
                    role = "assistant",
                    content = result.message,
                    toolName = pending.actionType,
                    toolStatus = if (result.success) "success" else "failed"
                )
            )
            voiceManager.speak(result.message)
            _uiState.value = _uiState.value.copy(
                voiceState = VoiceState.IDLE,
                statusText = result.message
            )
        }
    }

    fun rejectPendingAction() {
        val pending = _uiState.value.pendingConfirmation
        _uiState.value = _uiState.value.copy(
            pendingConfirmation = null,
            voiceState = VoiceState.IDLE,
            statusText = "Action cancelled"
        )
        if (pending != null) {
            viewModelScope.launch {
                repository.logAction(
                    ActionHistoryEntry(
                        actionType = pending.actionType,
                        target = pending.target,
                        confirmationState = "REJECTED",
                        executionState = "STOPPED",
                        resultSummary = "User declined action.",
                        riskLevel = pending.riskLevel.name
                    )
                )
                val reply = "Action cancelled."
                repository.saveMessage(
                    ConversationMessage(role = "assistant", content = reply)
                )
                voiceManager.speak(reply)
            }
        }
    }

    fun triggerEmergencyStop() {
        emergencyStopManager.triggerEmergencyStop()
        voiceManager.stopSpeech()
        voiceManager.stopListening()
        _uiState.value = _uiState.value.copy(
            pendingConfirmation = null,
            voiceState = VoiceState.INTERRUPTED,
            statusText = "EMERGENCY STOP ACTIVATED"
        )
    }

    fun resumeFromEmergencyStop() {
        emergencyStopManager.resumeFromEmergencyStop()
        _uiState.value = _uiState.value.copy(
            voiceState = VoiceState.IDLE,
            statusText = "Avani 2.O Resumed"
        )
    }

    fun toggleMasterAssistant(enabled: Boolean) {
        emergencyStopManager.toggleAssistantActive(enabled)
        if (!enabled) {
            voiceManager.stopSpeech()
            voiceManager.stopListening()
        }
    }

    fun toggleExplainBeforeAction(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isExplainBeforeAction = enabled)
    }

    fun toggleOfflineMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isOfflineMode = enabled)
    }

    fun toggleBackgroundService(enabled: Boolean) {
        if (enabled) {
            com.example.service.AvaniBackgroundService.startService(getApplication())
        } else {
            com.example.service.AvaniBackgroundService.stopService(getApplication())
        }
    }

    fun toggleVoiceOutput(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isVoiceOutputEnabled = enabled)
        voiceManager.isVoiceOutputEnabled = enabled
        if (!enabled) {
            voiceManager.stopSpeech()
        }
    }

    fun updateSpeechRate(rate: Float) {
        _uiState.value = _uiState.value.copy(speechRate = rate)
        voiceManager.speechRate = rate
    }

    fun updateSpeechPitch(pitch: Float) {
        _uiState.value = _uiState.value.copy(speechPitch = pitch)
        voiceManager.speechPitch = pitch
    }

    // Code & Project Builder
    fun generateCodeWorkspace(language: String, requirements: String, projectType: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                codeGenerationStage = "Generating $language structure...",
                generatedCodeOutput = null
            )
            val prompt = "Create a complete production-grade implementation for $projectType in $language. Requirements: $requirements. Provide complete code files with file paths and execution instructions."
            val response = geminiService.processUserTurn(prompt, emptyList())
            val outputText = when (response) {
                is GeminiResponse.Text -> response.reply
                is GeminiResponse.FunctionCall -> "Generated implementation for $language: ${response.arguments}"
                is GeminiResponse.Error -> "Code generation error: ${response.message}"
            }
            _uiState.value = _uiState.value.copy(
                codeGenerationStage = "Completed",
                generatedCodeOutput = outputText
            )
            repository.saveMessage(
                ConversationMessage(
                    role = "assistant",
                    content = "Generated $language code for $projectType:\n$outputText"
                )
            )
        }
    }

    // Project CRUD
    fun addProject(name: String, desc: String, techStack: String) {
        viewModelScope.launch {
            repository.saveProject(
                ProjectRecord(
                    name = name,
                    description = desc,
                    technologyStack = techStack
                )
            )
        }
    }

    fun updateProject(project: ProjectRecord) {
        viewModelScope.launch {
            repository.updateProject(project)
        }
    }

    fun deleteProject(project: ProjectRecord) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    // Task CRUD
    fun addTask(title: String, desc: String = "", priority: String = "Medium", isReminder: Boolean = false) {
        viewModelScope.launch {
            repository.saveTask(
                AssistantTask(
                    title = title,
                    description = desc,
                    priority = priority,
                    isReminder = isReminder
                )
            )
        }
    }

    fun toggleTaskComplete(task: AssistantTask) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: AssistantTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Memory CRUD
    fun addMemory(category: String, content: String) {
        viewModelScope.launch {
            repository.saveMemory(
                SavedMemory(
                    category = category,
                    content = content
                )
            )
        }
    }

    fun deleteMemory(memory: SavedMemory) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearMemories()
        }
    }

    fun clearActionHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            repository.clearConversation()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}
