package com.example.data.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

enum class ResponseLengthMode {
    SHORT,
    NORMAL,
    DETAILED
}

data class AIModelOption(
    val id: String,
    val name: String,
    val description: String,
    val badge: String,
    val supportsVision: Boolean = true
)

object AvailableModels {
    val RIMA_FLASH = AIModelOption(
        id = "gemini-2.5-flash",
        name = "Rima Flash",
        description = "Lightning-fast answers for everyday questions, math, translation & coding.",
        badge = "Fast & Smart",
        supportsVision = true
    )
    val RIMA_PRO = AIModelOption(
        id = "gemini-2.5-pro",
        name = "Rima Pro",
        description = "Advanced reasoning, complex problem solving, in-depth academic explanations.",
        badge = "High Intelligence",
        supportsVision = true
    )
    val RIMA_CREATIVE = AIModelOption(
        id = "gemini-2.5-flash",
        name = "Rima Vision & Creative",
        description = "Deep image analysis, document scanning, creative writing and idea generation.",
        badge = "Vision & Art",
        supportsVision = true
    )

    val list = listOf(RIMA_FLASH, RIMA_PRO, RIMA_CREATIVE)

    fun find(id: String): AIModelOption {
        return list.firstOrNull { it.id == id }
            ?: when {
                id.contains("pro", ignoreCase = true) -> RIMA_PRO
                else -> RIMA_FLASH
            }
    }
}

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val selectedModelId: String = AvailableModels.RIMA_FLASH.id,
    val responseLength: ResponseLengthMode = ResponseLengthMode.NORMAL,
    val autoReadAnswers: Boolean = false,
    val speechLanguage: String = "auto", // "auto", "bn", "en"
    val voicePitch: Float = 1.0f,
    val voiceSpeed: Float = 1.0f,
    val customInstructions: String = "",
    val enableWebSearch: Boolean = false,
    val customApiKey: String = ""
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("rima_user_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val themeStr = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val theme = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM)

        val modelId = prefs.getString("selected_model", AvailableModels.RIMA_FLASH.id) ?: AvailableModels.RIMA_FLASH.id
        
        val lengthStr = prefs.getString("response_length", ResponseLengthMode.NORMAL.name) ?: ResponseLengthMode.NORMAL.name
        val length = runCatching { ResponseLengthMode.valueOf(lengthStr) }.getOrDefault(ResponseLengthMode.NORMAL)

        val autoRead = prefs.getBoolean("auto_read", false)
        val speechLang = prefs.getString("speech_lang", "auto") ?: "auto"
        val voicePitch = prefs.getFloat("voice_pitch", 1.0f)
        val voiceSpeed = prefs.getFloat("voice_speed", 1.0f)
        val customInstructions = prefs.getString("custom_instructions", "") ?: ""
        val enableWebSearch = prefs.getBoolean("enable_web_search", false)
        val customApiKey = prefs.getString("custom_api_key", "") ?: ""

        return UserSettings(
            themeMode = theme,
            selectedModelId = modelId,
            responseLength = length,
            autoReadAnswers = autoRead,
            speechLanguage = speechLang,
            voicePitch = voicePitch,
            voiceSpeed = voiceSpeed,
            customInstructions = customInstructions,
            enableWebSearch = enableWebSearch,
            customApiKey = customApiKey
        )
    }

    fun updateTheme(theme: AppThemeMode) {
        prefs.edit().putString("theme_mode", theme.name).apply()
        _settings.value = _settings.value.copy(themeMode = theme)
    }

    fun updateModel(modelId: String) {
        prefs.edit().putString("selected_model", modelId).apply()
        _settings.value = _settings.value.copy(selectedModelId = modelId)
    }

    fun updateResponseLength(length: ResponseLengthMode) {
        prefs.edit().putString("response_length", length.name).apply()
        _settings.value = _settings.value.copy(responseLength = length)
    }

    fun updateAutoRead(enabled: Boolean) {
        prefs.edit().putBoolean("auto_read", enabled).apply()
        _settings.value = _settings.value.copy(autoReadAnswers = enabled)
    }

    fun updateSpeechLanguage(lang: String) {
        prefs.edit().putString("speech_lang", lang).apply()
        _settings.value = _settings.value.copy(speechLanguage = lang)
    }

    fun updateVoiceSettings(pitch: Float, speed: Float) {
        prefs.edit()
            .putFloat("voice_pitch", pitch)
            .putFloat("voice_speed", speed)
            .apply()
        _settings.value = _settings.value.copy(voicePitch = pitch, voiceSpeed = speed)
    }

    fun updateCustomInstructions(instructions: String) {
        prefs.edit().putString("custom_instructions", instructions).apply()
        _settings.value = _settings.value.copy(customInstructions = instructions)
    }

    fun updateWebSearch(enabled: Boolean) {
        prefs.edit().putBoolean("enable_web_search", enabled).apply()
        _settings.value = _settings.value.copy(enableWebSearch = enabled)
    }

    fun updateCustomApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key).apply()
        _settings.value = _settings.value.copy(customApiKey = key)
    }
}
