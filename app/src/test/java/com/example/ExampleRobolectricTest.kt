package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.DetectedLanguage
import com.example.util.LanguageDetectorUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Rima AI", appName)
  }

  @Test
  fun `detect Bengali language`() {
    val detected = LanguageDetectorUtil.detectLanguage("কেমন আছো? রিমা এআই")
    assertEquals(DetectedLanguage.BENGALI, detected)
  }

  @Test
  fun `detect Banglish language`() {
    val detected = LanguageDetectorUtil.detectLanguage("kemon acho rima amake ektu help koro")
    assertEquals(DetectedLanguage.BANGLISH, detected)
  }

  @Test
  fun `detect English language`() {
    val detected = LanguageDetectorUtil.detectLanguage("Hello Rima, how are you today?")
    assertEquals(DetectedLanguage.ENGLISH, detected)
  }

  @Test
  fun `detect English for quantum computing prompt`() {
    val detected = LanguageDetectorUtil.detectLanguage("Explain Quantum Computing simply")
    assertEquals(DetectedLanguage.ENGLISH, detected)
  }

  @Test
  fun `test Gemini DTO serialization`() {
    val request = com.example.data.remote.GenerateContentRequest(
      contents = listOf(
        com.example.data.remote.ContentDto(
          role = "user",
          parts = listOf(com.example.data.remote.PartDto(text = "Explain Quantum Computing simply"))
        )
      ),
      generationConfig = com.example.data.remote.GenerationConfigDto(
        temperature = 0.7f,
        topP = 0.95f,
        maxOutputTokens = 2048
      )
    )
    val adapter = com.example.data.remote.GeminiApiClient.moshi.adapter(com.example.data.remote.GenerateContentRequest::class.java)
    val json = adapter.toJson(request)
    org.junit.Assert.assertTrue(json.contains("Explain Quantum Computing simply"))
    org.junit.Assert.assertTrue(json.contains("\"role\":\"user\""))
    org.junit.Assert.assertTrue(json.contains("\"temperature\":0.7"))
  }

  @Test
  fun `test Gemini error parsing`() {
    val rawErrorJson = """{"error":{"code":400,"message":"Invalid request structure: multiturn role sequence must alternate","status":"INVALID_ARGUMENT"}}"""
    val adapter = com.example.data.remote.GeminiApiClient.moshi.adapter(com.example.data.remote.GeminiErrorResponse::class.java)
    val parsed = adapter.fromJson(rawErrorJson)
    assertEquals(400, parsed?.error?.code)
    assertEquals("Invalid request structure: multiturn role sequence must alternate", parsed?.error?.message)
  }

  @Test
  fun `test model resolution`() {
    val flash = com.example.data.model.AvailableModels.find("gemini-2.5-flash")
    assertEquals("gemini-2.5-flash", flash.id)
    val pro = com.example.data.model.AvailableModels.find("gemini-2.5-pro")
    assertEquals("gemini-2.5-pro", pro.id)
  }
}

