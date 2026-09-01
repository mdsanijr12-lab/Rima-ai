package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class SpeechRecognitionState {
    object Idle : SpeechRecognitionState()
    data class Listening(val rmsDb: Float = 0f) : SpeechRecognitionState()
    data class Result(val text: String) : SpeechRecognitionState()
    data class Error(val message: String) : SpeechRecognitionState()
}

class RimaSpeechRecognizer(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val state: StateFlow<SpeechRecognitionState> = _state.asStateFlow()

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(languagePreference: String = "auto") {
        if (!isAvailable()) {
            _state.value = SpeechRecognitionState.Error("Speech recognition is not available on this device.")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = SpeechRecognitionState.Listening(0f)
                }

                override fun onBeginningOfSpeech() {
                    _state.value = SpeechRecognitionState.Listening(2f)
                }

                override fun onRmsChanged(rmsdB: Float) {
                    val normalized = (rmsdB.coerceAtLeast(0f) / 10f).coerceIn(0f, 1f)
                    _state.value = SpeechRecognitionState.Listening(normalized)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    // Waiting for results
                }

                override fun onError(error: Int) {
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking again."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service is busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                        else -> "Speech recognition error ($error)"
                    }
                    _state.value = SpeechRecognitionState.Error(errorMsg)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val resultText = matches?.firstOrNull() ?: ""
                    if (resultText.isNotBlank()) {
                        _state.value = SpeechRecognitionState.Result(resultText)
                    } else {
                        _state.value = SpeechRecognitionState.Idle
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = matches?.firstOrNull()
                    if (partial != null) {
                        _state.value = SpeechRecognitionState.Listening(0.5f)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            when (languagePreference) {
                "bn" -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                }
                "en" -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                }
                else -> {
                    // Auto / Multi-lingual support
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-BD", "en-US", "bn-IN"))
                }
            }
        }

        try {
            speechRecognizer?.startListening(intent)
            _state.value = SpeechRecognitionState.Listening(0f)
        } catch (e: Exception) {
            _state.value = SpeechRecognitionState.Error("Failed to start voice listener: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // ignore cleanup errors
        } finally {
            speechRecognizer = null
        }
    }

    fun resetState() {
        _state.value = SpeechRecognitionState.Idle
    }
}
