package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.actions.ActionRouter
import com.example.actions.AndroidActionExecutor
import com.example.actions.RoutingDecision
import com.example.data.local.AvaniDatabase
import com.example.data.model.ConversationMessage
import com.example.data.repository.AssistantRepository
import com.example.gemini.GeminiAssistantService
import com.example.gemini.GeminiResponse
import com.example.voice.VoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AvaniBackgroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var voiceManager: VoiceManager? = null
    private var geminiService: GeminiAssistantService? = null
    private var actionRouter: ActionRouter? = null
    private var repository: AssistantRepository? = null

    companion object {
        const val CHANNEL_ID = "avani_background_channel"
        const val NOTIFICATION_ID = 2026
        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_LISTEN = "com.example.service.ACTION_LISTEN"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, AvaniBackgroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AvaniBackgroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val db = AvaniDatabase.getDatabase(applicationContext)
        repository = AssistantRepository(
            db.conversationDao(),
            db.projectDao(),
            db.memoryDao(),
            db.taskDao(),
            db.actionHistoryDao()
        )
        voiceManager = VoiceManager(applicationContext)
        geminiService = GeminiAssistantService()
        val executor = AndroidActionExecutor(applicationContext)
        actionRouter = ActionRouter(executor, repository!!)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForegroundAssistant()
                return START_NOT_STICKY
            }
            ACTION_LISTEN -> {
                triggerVoiceListening()
            }
            else -> {
                startForegroundAssistant()
            }
        }
        return START_STICKY
    }

    private fun startForegroundAssistant() {
        _isRunning.value = true
        val notification = buildNotification("Avani 2.O Active", "Listening in background. Tap 'Mic' to speak.")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        voiceManager?.speak("Avani 2.O background mode active hai. Main aapke phone par kisi bhi app ke sath kaam karne ke liye ready hoon.")
    }

    private fun triggerVoiceListening() {
        updateNotification("Listening...", "Bolna shuru karein...")
        voiceManager?.startListening(
            onResult = { query ->
                handleVoiceCommand(query)
            },
            onError = { err ->
                updateNotification("Avani 2.O Active", err)
            }
        )
    }

    private fun handleVoiceCommand(query: String) {
        updateNotification("Processing...", "\"$query\"")
        serviceScope.launch {
            repository?.saveMessage(ConversationMessage(role = "user", content = query))

            val response = geminiService?.processUserTurn(
                userPrompt = query,
                recentHistory = emptyList()
            )

            when (response) {
                is GeminiResponse.FunctionCall -> {
                    updateNotification("Executing...", response.functionName)
                    val decision = actionRouter?.routeToolCall(response.functionName, response.arguments)
                    when (decision) {
                        is RoutingDecision.Executed -> {
                            val msg = decision.result.message
                            voiceManager?.speak(msg)
                            updateNotification("Avani 2.O Active", msg)
                            repository?.saveMessage(ConversationMessage(role = "assistant", content = msg, toolName = response.functionName))
                        }
                        is RoutingDecision.Information -> {
                            voiceManager?.speak(decision.message)
                            updateNotification("Avani 2.O Active", decision.message)
                            repository?.saveMessage(ConversationMessage(role = "assistant", content = decision.message))
                        }
                        is RoutingDecision.NeedsConfirmation -> {
                            val prompt = "Confirmation needed: ${decision.pendingAction.details}. Open app to approve."
                            voiceManager?.speak(prompt)
                            updateNotification("Confirmation Needed", decision.pendingAction.details)
                        }
                        null -> {}
                    }
                }
                is GeminiResponse.Text -> {
                    voiceManager?.speak(response.reply)
                    updateNotification("Avani 2.O Active", response.reply.take(50))
                    repository?.saveMessage(ConversationMessage(role = "assistant", content = response.reply))
                }
                is GeminiResponse.Error -> {
                    val err = "Error: ${response.message}"
                    voiceManager?.speak(err)
                    updateNotification("Avani 2.O Active", err)
                }
                null -> {}
            }
        }
    }

    private fun buildNotification(title: String, text: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val listenIntent = Intent(this, AvaniBackgroundService::class.java).apply {
            action = ACTION_LISTEN
        }
        val listenPendingIntent = PendingIntent.getService(
            this,
            1,
            listenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AvaniBackgroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_btn_speak_now, "Speak", listenPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Avani", stopPendingIntent)
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(title, text))
    }

    private fun stopForegroundAssistant() {
        _isRunning.value = false
        voiceManager?.stopSpeech()
        voiceManager?.stopListening()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Avani 2.O Background Assistant",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Keeps Avani active in the background for instant voice commands across apps."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isRunning.value = false
        serviceScope.cancel()
        voiceManager?.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
