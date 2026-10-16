package com.example.security

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.data.model.CapabilityRecord
import com.example.data.model.CapabilityStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmergencyStopManager {
    private val _isAssistantActive = MutableStateFlow(true)
    val isAssistantActive: StateFlow<Boolean> = _isAssistantActive.asStateFlow()

    private val _isEmergencyStopped = MutableStateFlow(false)
    val isEmergencyStopped: StateFlow<Boolean> = _isEmergencyStopped.asStateFlow()

    fun toggleAssistantActive(active: Boolean) {
        _isAssistantActive.value = active
        if (!active) {
            _isEmergencyStopped.value = false
        }
    }

    fun triggerEmergencyStop() {
        _isEmergencyStopped.value = true
    }

    fun resumeFromEmergencyStop() {
        _isEmergencyStopped.value = false
    }
}

class PrivacyManager(private val context: Context) {

    fun hasRecordAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    fun hasReadContactsPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    fun hasCallPhonePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    fun hasNotificationPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun getCapabilityAudit(): List<CapabilityRecord> {
        val audioGranted = hasRecordAudioPermission()
        val contactsGranted = hasReadContactsPermission()
        val callGranted = hasCallPhonePermission()
        val notifGranted = hasNotificationPermission()

        return listOf(
            CapabilityRecord(
                id = "cap_voice_input",
                name = "Voice Input & Speech Recognition",
                availability = if (audioGranted) CapabilityStatus.AVAILABLE_NOW else CapabilityStatus.REQUIRES_PERMISSION,
                requiredPermissions = listOf("RECORD_AUDIO"),
                explanation = "Allows you to speak commands naturally to Avani in Hindi, English, and Hinglish.",
                category = "Voice & Speech"
            ),
            CapabilityRecord(
                id = "cap_tts",
                name = "Voice Speech Output (Text-to-Speech)",
                availability = CapabilityStatus.AVAILABLE_NOW,
                explanation = "Speaks responses aloud naturally with Hindi and English accent adaptation.",
                category = "Voice & Speech"
            ),
            CapabilityRecord(
                id = "cap_app_launch",
                name = "Open Installed Applications",
                availability = CapabilityStatus.AVAILABLE_NOW,
                explanation = "Opens apps like WhatsApp, YouTube, Chrome, Camera, and Settings via Android system intents.",
                category = "Phone Control"
            ),
            CapabilityRecord(
                id = "cap_contact_search",
                name = "Smart Contact Resolution",
                availability = if (contactsGranted) CapabilityStatus.AVAILABLE_NOW else CapabilityStatus.REQUIRES_PERMISSION,
                requiredPermissions = listOf("READ_CONTACTS"),
                explanation = "Finds authorized phone numbers for family, friends, or colleagues when requested.",
                category = "Phone Control"
            ),
            CapabilityRecord(
                id = "cap_phone_dial",
                name = "Phone Dialer & Direct Calling",
                availability = if (callGranted) CapabilityStatus.AVAILABLE_NOW else CapabilityStatus.AVAILABLE_NOW, // Dialer always works
                requiredPermissions = listOf("CALL_PHONE (for direct calls)"),
                explanation = "Opens Android dialer with the number filled, or dials directly when authorized.",
                category = "Phone Control"
            ),
            CapabilityRecord(
                id = "cap_messaging",
                name = "WhatsApp & SMS Message Preparation",
                availability = CapabilityStatus.AVAILABLE_NOW,
                explanation = "Prepares messages and opens WhatsApp conversation or SMS draft safely with user confirmation.",
                category = "Phone Control"
            ),
            CapabilityRecord(
                id = "cap_project_memory",
                name = "Project Memory System",
                availability = CapabilityStatus.AVAILABLE_NOW,
                explanation = "Persists your software projects, architecture, tech stack, and task history across sessions.",
                category = "Intelligence"
            ),
            CapabilityRecord(
                id = "cap_personal_memory",
                name = "Personal Memory & Preferences",
                availability = CapabilityStatus.AVAILABLE_NOW,
                explanation = "Remembers user-approved facts, preferred language, and custom preferences securely.",
                category = "Intelligence"
            ),
            CapabilityRecord(
                id = "cap_reminders",
                name = "Tasks, Notes & Reminders",
                availability = if (notifGranted) CapabilityStatus.AVAILABLE_NOW else CapabilityStatus.REQUIRES_PERMISSION,
                requiredPermissions = listOf("POST_NOTIFICATIONS"),
                explanation = "Creates prioritized to-dos and scheduled reminders with Android notifications.",
                category = "Productivity"
            ),
            CapabilityRecord(
                id = "cap_code_builder",
                name = "Build & Code Workspace",
                availability = CapabilityStatus.AVAILABLE_NOW,
                explanation = "Generates Kotlin, Android, web code, project file structures, and debug explanations.",
                category = "Intelligence"
            ),
            CapabilityRecord(
                id = "cap_emergency_stop",
                name = "Emergency STOP & Explain-Before-Action",
                availability = CapabilityStatus.AVAILABLE_NOW,
                explanation = "Instantly halts speech and action execution with a single tap or master OFF toggle.",
                category = "Safety"
            )
        )
    }
}
