package com.example.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class GeminiResponse {
    data class Text(val reply: String) : GeminiResponse()
    data class FunctionCall(val functionName: String, val arguments: Map<String, Any?>) : GeminiResponse()
    data class Error(val message: String) : GeminiResponse()
}

class GeminiAssistantService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun processUserTurn(
        userPrompt: String,
        recentHistory: List<Pair<String, String>>, // role, content
        systemMemoryContext: String = "",
        isVoiceMode: Boolean = false
    ): GeminiResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Local fallback rule execution when API key is not yet configured by user
            return@withContext executeLocalIntentMatching(userPrompt, isVoiceMode)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        try {
            val root = JSONObject()

            val voiceModePrompt = if (isVoiceMode) {
                """
                You are Avani 2.O in LIVE VOICE CONVERSATION mode.
                Behave EXACTLY like a real, living, warm, intelligent human companion speaking naturally on a phone call.
                Speak in natural, conversational, fluent Hindi, Hinglish, or English.
                Use warm human conversational expressions ("Haan bilkul!", "Arey waah!", "Main abhi karti hoon!", "Zaroor!").
                Keep spoken voice answers punchy, natural, and concise (1-2 friendly sentences), without markdown or bullet lists.
                You obey ALL phone commands: open apps, initiate calls, send WhatsApp messages, search web, manage project memory.
                """
            } else {
                """
                You are Avani 2.O in CHAT MODE (just like ChatGPT or Google Gemini).
                Provide thorough, beautifully formatted text responses with markdown, code snippets, clear explanations, and structure.
                Do not include conversational voice fillers. Focus on pristine clarity, formatting, and helpfulness.
                """
            }

            // System instruction
            val systemInstructionObj = JSONObject()
            val sysParts = JSONArray()
            val sysText = JSONObject().apply {
                put("text", """
                    $voiceModePrompt
                    Personality: Intelligent, empathetic, confident, supportive, witty, professional.
                    Languages: Hindi, English, Hinglish. Auto-detect user's language and respond naturally in the same language.
                    
                    You have real Android tools available. When user intent matches a phone or assistant capability, you MUST call the matching function declaration.
                    Available tools:
                    - open_app(appName, packageName)
                    - initiate_call(contactName, phoneNumber)
                    - prepare_message(application, recipient, message)
                    - search_web(query)
                    - create_task(title, description, dueAt, priority)
                    - create_reminder(title, reminderTime)
                    - search_project_memory(query)
                    - create_project(name, description, technologyStack)
                    - update_project_status(projectName, completedTask, pendingTask)
                    - save_personal_memory(category, content)
                    - generate_code(language, requirements)
                    - get_device_capabilities()
                    
                    Current User Context & Memory: $systemMemoryContext
                """.trimIndent())
            }
            sysParts.put(sysText)
            systemInstructionObj.put("parts", sysParts)
            root.put("systemInstruction", systemInstructionObj)

            // Contents array
            val contentsArray = JSONArray()

            // Append recent history turns
            for ((role, text) in recentHistory.takeLast(6)) {
                val turnObj = JSONObject()
                turnObj.put("role", if (role == "user") "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", text))
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }

            // Current user turn
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userPrompt))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            root.put("contents", contentsArray)

            // Tools definitions
            val toolsArray = JSONArray()
            val toolDeclarationObj = JSONObject()
            val funcDeclarations = JSONArray()

            // open_app
            funcDeclarations.put(JSONObject().apply {
                put("name", "open_app")
                put("description", "Open an installed Android app like WhatsApp, YouTube, Chrome, Camera, Settings, etc.")
                val params = JSONObject()
                params.put("type", "OBJECT")
                val props = JSONObject()
                props.put("appName", JSONObject().put("type", "STRING").put("description", "Name of the app e.g. WhatsApp, YouTube, Chrome, Camera, Settings"))
                props.put("packageName", JSONObject().put("type", "STRING").put("description", "Optional Android package name e.g. com.whatsapp"))
                params.put("properties", props)
                params.put("required", JSONArray().put("appName"))
                put("parameters", params)
            })

            // initiate_call
            funcDeclarations.put(JSONObject().apply {
                put("name", "initiate_call")
                put("description", "Initiate phone call or open dialer for a given contact or phone number")
                val params = JSONObject()
                params.put("type", "OBJECT")
                val props = JSONObject()
                props.put("contactName", JSONObject().put("type", "STRING").put("description", "Name of the contact to call, e.g. Mummy, Rahul, Doctor"))
                props.put("phoneNumber", JSONObject().put("type", "STRING").put("description", "Optional direct phone number"))
                params.put("properties", props)
                params.put("required", JSONArray().put("contactName"))
                put("parameters", params)
            })

            // prepare_message
            funcDeclarations.put(JSONObject().apply {
                put("name", "prepare_message")
                put("description", "Prepare or send a message via WhatsApp or SMS to a recipient")
                val params = JSONObject()
                params.put("type", "OBJECT")
                val props = JSONObject()
                props.put("application", JSONObject().put("type", "STRING").put("description", "whatsapp or sms"))
                props.put("recipient", JSONObject().put("type", "STRING").put("description", "Recipient name or phone number"))
                props.put("message", JSONObject().put("type", "STRING").put("description", "Message body text"))
                params.put("properties", props)
                params.put("required", JSONArray().put("application").put("recipient").put("message"))
                put("parameters", params)
            })

            // search_web
            funcDeclarations.put(JSONObject().apply {
                put("name", "search_web")
                put("description", "Search the web or Google for queries, news, or current information")
                val params = JSONObject()
                params.put("type", "OBJECT")
                val props = JSONObject()
                props.put("query", JSONObject().put("type", "STRING").put("description", "Search query"))
                params.put("properties", props)
                params.put("required", JSONArray().put("query"))
                put("parameters", params)
            })

            // create_task
            funcDeclarations.put(JSONObject().apply {
                put("name", "create_task")
                put("description", "Save a personal task or to-do item")
                val params = JSONObject()
                params.put("type", "OBJECT")
                val props = JSONObject()
                props.put("title", JSONObject().put("type", "STRING").put("description", "Task title"))
                props.put("priority", JSONObject().put("type", "STRING").put("description", "Low, Medium, or High"))
                params.put("properties", props)
                params.put("required", JSONArray().put("title"))
                put("parameters", params)
            })

            // create_reminder
            funcDeclarations.put(JSONObject().apply {
                put("name", "create_reminder")
                put("description", "Create a reminder with a specific time")
                val params = JSONObject()
                params.put("type", "OBJECT")
                val props = JSONObject()
                props.put("title", JSONObject().put("type", "STRING").put("description", "Reminder title"))
                props.put("reminderTime", JSONObject().put("type", "STRING").put("description", "Time description e.g. 7 AM tomorrow, in 1 hour"))
                params.put("properties", props)
                params.put("required", JSONArray().put("title"))
                put("parameters", params)
            })

            // search_project_memory
            funcDeclarations.put(JSONObject().apply {
                put("name", "search_project_memory")
                put("description", "Look up saved software project records, status, and pending tasks in Project Memory")
                val params = JSONObject()
                params.put("type", "OBJECT")
                val props = JSONObject()
                props.put("query", JSONObject().put("type", "STRING").put("description", "Project name or topic to search"))
                params.put("properties", props)
                params.put("required", JSONArray().put("query"))
                put("parameters", params)
            })

            // create_project
            funcDeclarations.put(JSONObject().apply {
                put("name", "create_project")
                put("description", "Create and save a new software project record in Project Memory")
                val params = JSONObject()
                params.put("type", "OBJECT")
                val props = JSONObject()
                props.put("name", JSONObject().put("type", "STRING").put("description", "Project name"))
                props.put("description", JSONObject().put("type", "STRING").put("description", "Project overview"))
                props.put("technologyStack", JSONObject().put("type", "STRING").put("description", "Tech stack e.g. Kotlin, Compose, Room"))
                params.put("properties", props)
                params.put("required", JSONArray().put("name").put("description"))
                put("parameters", params)
            })

            // generate_code
            funcDeclarations.put(JSONObject().apply {
                put("name", "generate_code")
                put("description", "Generate production-grade code, architecture, or fix bugs")
                val params = JSONObject()
                params.put("type", "OBJECT")
                val props = JSONObject()
                props.put("language", JSONObject().put("type", "STRING").put("description", "Kotlin, Python, JavaScript, etc."))
                props.put("requirements", JSONObject().put("type", "STRING").put("description", "Detailed coding request"))
                params.put("properties", props)
                params.put("required", JSONArray().put("language").put("requirements"))
                put("parameters", params)
            })

            // get_device_capabilities
            funcDeclarations.put(JSONObject().apply {
                put("name", "get_device_capabilities")
                put("description", "Explain what Avani 2.O can do on the user's Android phone right now")
                put("parameters", JSONObject().put("type", "OBJECT"))
            })

            toolDeclarationObj.put("functionDeclarations", funcDeclarations)
            toolsArray.put(toolDeclarationObj)
            root.put("tools", toolsArray)

            // Generation config
            val genConfig = JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
            }
            root.put("generationConfig", genConfig)

            val body = root.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val respString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiService", "API error: ${response.code} $respString")
                return@withContext executeLocalIntentMatching(userPrompt)
            }

            val respJson = JSONObject(respString)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            if (parts != null && parts.length() > 0) {
                val firstPart = parts.getJSONObject(0)
                if (firstPart.has("functionCall")) {
                    val fnCall = firstPart.getJSONObject("functionCall")
                    val fnName = fnCall.getString("name")
                    val argsObj = fnCall.optJSONObject("args")
                    val argsMap = mutableMapOf<String, Any?>()
                    argsObj?.keys()?.forEach { key ->
                        argsMap[key] = argsObj.get(key)
                    }
                    return@withContext GeminiResponse.FunctionCall(fnName, argsMap)
                } else if (firstPart.has("text")) {
                    val reply = firstPart.getString("text")
                    return@withContext GeminiResponse.Text(reply)
                }
            }

            executeLocalIntentMatching(userPrompt, isVoiceMode)
        } catch (e: Exception) {
            Log.e("GeminiService", "Exception in Gemini call", e)
            executeLocalIntentMatching(userPrompt, isVoiceMode)
        }
    }

    /**
     * Local high-performance intent parser that guarantees full offline and prototype
     * functionality even when network or API key is not yet set!
     */
    fun executeLocalIntentMatching(prompt: String, isVoiceMode: Boolean = false): GeminiResponse {
        val lower = prompt.lowercase().trim()

        // 1. App opening
        if (lower.contains("whatsapp") && (lower.contains("kholo") || lower.contains("open"))) {
            return GeminiResponse.FunctionCall("open_app", mapOf("appName" to "WhatsApp", "packageName" to "com.whatsapp"))
        }
        if (lower.contains("youtube") && (lower.contains("kholo") || lower.contains("open"))) {
            return GeminiResponse.FunctionCall("open_app", mapOf("appName" to "YouTube", "packageName" to "com.google.android.youtube"))
        }
        if (lower.contains("chrome") && (lower.contains("kholo") || lower.contains("open"))) {
            return GeminiResponse.FunctionCall("open_app", mapOf("appName" to "Chrome", "packageName" to "com.android.chrome"))
        }
        if ((lower.contains("camera") || lower.contains("camera")) && (lower.contains("kholo") || lower.contains("open"))) {
            return GeminiResponse.FunctionCall("open_app", mapOf("appName" to "Camera"))
        }
        if (lower.contains("settings") && (lower.contains("kholo") || lower.contains("open"))) {
            return GeminiResponse.FunctionCall("open_app", mapOf("appName" to "Settings"))
        }
        // Project Memory (check before generic kholo)
        if (lower.contains("project") && (lower.contains("status") || lower.contains("kholo") || lower.contains("batao") || lower.contains("pichhla"))) {
            return GeminiResponse.FunctionCall("search_project_memory", mapOf("query" to "Avani"))
        }

        // Calling
        if (lower.contains("call") || lower.contains("call lagao") || lower.contains("phone lagao")) {
            val contact = prompt.replace("call", "", ignoreCase = true)
                .replace("lagao", "", ignoreCase = true)
                .replace("ko", "", ignoreCase = true)
                .replace("phone", "", ignoreCase = true)
                .replace("avani", "", ignoreCase = true)
                .trim()
            return GeminiResponse.FunctionCall("initiate_call", mapOf("contactName" to contact))
        }

        // WhatsApp / Messaging
        if (lower.contains("message") || lower.contains("whatsapp")) {
            return GeminiResponse.FunctionCall("prepare_message", mapOf(
                "application" to "whatsapp",
                "recipient" to "Friend",
                "message" to prompt
            ))
        }

        // Web search
        if (lower.contains("search") || lower.contains("dhoondo") || lower.contains("google")) {
            val query = prompt.replace("search", "", ignoreCase = true)
                .replace("karo", "", ignoreCase = true)
                .replace("dhoondo", "", ignoreCase = true)
                .replace("google", "", ignoreCase = true)
                .replace("mein", "", ignoreCase = true)
                .replace("chrome", "", ignoreCase = true)
                .replace("avani", "", ignoreCase = true)
                .trim()
            return GeminiResponse.FunctionCall("search_web", mapOf("query" to query))
        }

        // Generic app opening
        if (lower.startsWith("open ") || (lower.contains("kholo") && !lower.contains("project"))) {
            val appName = prompt.replace("open", "", ignoreCase = true)
                .replace("kholo", "", ignoreCase = true)
                .replace("app", "", ignoreCase = true)
                .trim()
            return GeminiResponse.FunctionCall("open_app", mapOf("appName" to appName))
        }

        // 6. Tasks & Reminders
        if (lower.contains("yaad dilana") || lower.contains("reminder")) {
            return GeminiResponse.FunctionCall("create_reminder", mapOf("title" to prompt, "reminderTime" to "Tomorrow 7:00 AM"))
        }
        if (lower.contains("task") || lower.contains("kaam")) {
            return GeminiResponse.FunctionCall("create_task", mapOf("title" to prompt, "priority" to "Medium"))
        }

        // 7. Capabilities
        if (lower.contains("kya kar sakti ho") || lower.contains("capabilities") || lower.contains("what can you do")) {
            return GeminiResponse.FunctionCall("get_device_capabilities", emptyMap())
        }

        // 8. Code generation
        if (lower.contains("code") || lower.contains("feature add karo") || lower.contains("error")) {
            return GeminiResponse.FunctionCall("generate_code", mapOf("language" to "Kotlin", "requirements" to prompt))
        }

        // Default conversational Hindi/English intelligent response
        val responseText = if (lower.contains("namaste") || lower.contains("kaise ho") || lower.contains("kya haal")) {
            "नमस्ते! मैं अवनी 2.O हूँ, आपकी पर्सनल एआई असिस्टेंट। मैं ऐप्स खोल सकती हूँ, कॉल और मैसेज तैयार कर सकती हूँ, कोड लिख सकती हूँ और आपके प्रोजेक्ट्स याद रख सकती हूँ। बताइए, आज क्या करना है?"
        } else {
            "Main Avani 2.O hoon! Aapke phone operations, web search, coding assistance, aur project memories ke liye taiyar hoon. Aap mujhse bolkar ya likhkar koi bhi task karwa sakte hain."
        }
        return GeminiResponse.Text(responseText)
    }
}
