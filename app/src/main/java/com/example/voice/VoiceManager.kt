package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    EXECUTING,
    AWAITING_CONFIRMATION,
    SPEAKING,
    INTERRUPTED,
    ERROR
}

interface VoiceEngineProvider {
    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit)
    fun stopListening()
    fun speak(text: String, onFinished: (() -> Unit)? = null)
    fun stopSpeech()
    fun shutdown()
}

class VoiceManager(private val context: Context) : VoiceEngineProvider {
    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    var speechRate: Float = 1.0f
    var speechPitch: Float = 1.0f
    var isVoiceOutputEnabled: Boolean = true

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _voiceState.value = VoiceState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        if (_voiceState.value == VoiceState.SPEAKING) {
                            _voiceState.value = VoiceState.IDLE
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        _voiceState.value = VoiceState.IDLE
                    }
                })
            } else {
                Log.e("VoiceManager", "TTS initialization failed: $status")
            }
        }
    }

    override fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        stopSpeech() // Stop speaking when user wants to speak

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition is not available on this device.")
            _voiceState.value = VoiceState.ERROR
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = VoiceState.LISTENING
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = VoiceState.LISTENING
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _audioRms.value = rmsdB.coerceIn(0f, 10f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = VoiceState.PROCESSING
                        _audioRms.value = 0f
                    }

                    override fun onError(error: Int) {
                        _voiceState.value = VoiceState.IDLE
                        _audioRms.value = 0f
                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking again."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            else -> "Speech recognition error ($error)"
                        }
                        onError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        _voiceState.value = VoiceState.IDLE
                        _audioRms.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            onResult(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            // Can show live partial transcription
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-IN", "en-US"))
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
            _voiceState.value = VoiceState.LISTENING
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error starting speech recognition", e)
            _voiceState.value = VoiceState.ERROR
            onError("Failed to start speech recognition: ${e.message}")
        }
    }

    override fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _audioRms.value = 0f
            if (_voiceState.value == VoiceState.LISTENING) {
                _voiceState.value = VoiceState.IDLE
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error stopping listening", e)
        }
    }

    override fun speak(text: String, onFinished: (() -> Unit)?) {
        if (!isVoiceOutputEnabled || text.isBlank() || !isTtsInitialized) {
            onFinished?.invoke()
            return
        }

        try {
            textToSpeech?.setSpeechRate(speechRate)
            textToSpeech?.setPitch(speechPitch)

            // Auto-detect Hindi vs English
            val containsDevanagari = text.any { it in '\u0900'..'\u097F' }
            val locale = if (containsDevanagari) Locale.forLanguageTag("hi-IN") else Locale.US
            textToSpeech?.language = locale

            // Strip code blocks or markdown headers from spoken text for natural voice
            val spokenText = cleanTextForSpeech(text)

            val utteranceId = "Avani_TTS_${System.currentTimeMillis()}"
            _voiceState.value = VoiceState.SPEAKING
            textToSpeech?.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e("VoiceManager", "TTS speak failed", e)
            _voiceState.value = VoiceState.IDLE
            onFinished?.invoke()
        }
    }

    override fun stopSpeech() {
        try {
            textToSpeech?.stop()
            if (_voiceState.value == VoiceState.SPEAKING) {
                _voiceState.value = VoiceState.INTERRUPTED
                _voiceState.value = VoiceState.IDLE
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error stopping TTS", e)
        }
    }

    fun updateState(state: VoiceState) {
        _voiceState.value = state
    }

    override fun shutdown() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error shutting down VoiceManager", e)
        }
    }

    private fun cleanTextForSpeech(input: String): String {
        return input
            .replace(Regex("```[\\s\\S]*?```"), "Code attached.")
            .replace(Regex("[#*`_\\[\\]]"), "")
            .replace(Regex("https?://\\S+"), "web link")
            .take(350) // Keep spoken answer comfortably concise
    }
}
