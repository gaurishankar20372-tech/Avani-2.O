package com.example

import com.example.gemini.GeminiAssistantService
import com.example.gemini.GeminiResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    private val geminiService = GeminiAssistantService()

    @Test
    fun testWhatsAppLaunchIntent() {
        val result = geminiService.executeLocalIntentMatching("Avani, WhatsApp kholo")
        assertTrue(result is GeminiResponse.FunctionCall)
        val fn = result as GeminiResponse.FunctionCall
        assertEquals("open_app", fn.functionName)
        assertEquals("WhatsApp", fn.arguments["appName"])
    }

    @Test
    fun testYouTubeLaunchIntent() {
        val result = geminiService.executeLocalIntentMatching("YouTube kholo please")
        assertTrue(result is GeminiResponse.FunctionCall)
        val fn = result as GeminiResponse.FunctionCall
        assertEquals("open_app", fn.functionName)
        assertEquals("YouTube", fn.arguments["appName"])
    }

    @Test
    fun testCallIntent() {
        val result = geminiService.executeLocalIntentMatching("Avani, Mummy ko call lagao")
        assertTrue(result is GeminiResponse.FunctionCall)
        val fn = result as GeminiResponse.FunctionCall
        assertEquals("initiate_call", fn.functionName)
    }

    @Test
    fun testWebSearchIntent() {
        val result = geminiService.executeLocalIntentMatching("Chrome mein AI development search karo")
        assertTrue(result is GeminiResponse.FunctionCall)
        val fn = result as GeminiResponse.FunctionCall
        assertEquals("search_web", fn.functionName)
    }

    @Test
    fun testProjectMemoryIntent() {
        val result = geminiService.executeLocalIntentMatching("Avani, mera pichhla project kholo")
        assertTrue(result is GeminiResponse.FunctionCall)
        val fn = result as GeminiResponse.FunctionCall
        assertEquals("search_project_memory", fn.functionName)
    }
}
