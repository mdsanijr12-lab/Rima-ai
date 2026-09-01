package com.example.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.SettingsManager
import com.example.util.LanguageDetectorUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

sealed class VoicePlaybackState {
    object Idle : VoicePlaybackState()
    data class Playing(
        val fullText: String,
        val currentSegment: String,
        val segmentIndex: Int,
        val totalSegments: Int,
        val progress: Float
    ) : VoicePlaybackState()
    data class Paused(
        val fullText: String,
        val segmentIndex: Int,
        val totalSegments: Int
    ) : VoicePlaybackState()
    data class Error(val message: String) : VoicePlaybackState()
}

class RimaVoiceManager(
    private val context: Context,
    private val settingsManager: SettingsManager,
    private val scope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _playbackState = MutableStateFlow<VoicePlaybackState>(VoicePlaybackState.Idle)
    val playbackState: StateFlow<VoicePlaybackState> = _playbackState.asStateFlow()

    // Sound wave levels (0f to 1f) for animated waveform
    private val _audioWaveform = MutableStateFlow<List<Float>>(listOf(0.2f, 0.4f, 0.6f, 0.3f, 0.7f, 0.5f, 0.2f))
    val audioWaveform: StateFlow<List<Float>> = _audioWaveform.asStateFlow()

    private var currentSegments: List<LanguageDetectorUtil.TtsSegment> = emptyList()
    private var currentSegmentIndex = 0
    private var currentFullText = ""
    private var isManuallyPaused = false
    private var waveJob: Job? = null

    private val bengaliLocaleBD = Locale("bn", "BD")
    private val bengaliLocaleIN = Locale("bn", "IN")
    private val englishLocaleUS = Locale.US

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    startWaveformAnimation()
                }

                override fun onDone(utteranceId: String?) {
                    scope.launch(Dispatchers.Main) {
                        if (!isManuallyPaused) {
                            playNextSegment()
                        }
                    }
                }

                override fun onError(utteranceId: String?) {
                    scope.launch(Dispatchers.Main) {
                        stopWaveformAnimation()
                        _playbackState.value = VoicePlaybackState.Error("Voice playback error occurred.")
                    }
                }
            })
        }
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        if (!isTtsInitialized || tts == null) {
            _playbackState.value = VoicePlaybackState.Error("Text-to-Speech is initializing. Please try again in a moment.")
            return
        }

        stop()

        currentFullText = text
        currentSegments = LanguageDetectorUtil.splitIntoTtsSegments(text)
        currentSegmentIndex = 0
        isManuallyPaused = false

        if (currentSegments.isEmpty()) {
            _playbackState.value = VoicePlaybackState.Idle
            return
        }

        playSegment(0)
    }

    private fun playSegment(index: Int) {
        if (index >= currentSegments.size) {
            stop()
            return
        }

        val segment = currentSegments[index]
        currentSegmentIndex = index

        val settings = settingsManager.settings.value
        tts?.setPitch(settings.voicePitch)
        tts?.setSpeechRate(settings.voiceSpeed)

        // Select language
        val targetLocale = if (segment.isBengali) {
            val resBD = tts?.isLanguageAvailable(bengaliLocaleBD) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (resBD >= TextToSpeech.LANG_AVAILABLE) {
                bengaliLocaleBD
            } else {
                val resIN = tts?.isLanguageAvailable(bengaliLocaleIN) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (resIN >= TextToSpeech.LANG_AVAILABLE) bengaliLocaleIN else englishLocaleUS
            }
        } else {
            englishLocaleUS
        }

        tts?.language = targetLocale

        val progress = if (currentSegments.isNotEmpty()) (index + 1).toFloat() / currentSegments.size else 1f
        _playbackState.value = VoicePlaybackState.Playing(
            fullText = currentFullText,
            currentSegment = segment.text,
            segmentIndex = index,
            totalSegments = currentSegments.size,
            progress = progress
        )

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "seg_$index")
        }

        tts?.speak(segment.text, TextToSpeech.QUEUE_FLUSH, params, "seg_$index")
    }

    private fun playNextSegment() {
        val nextIndex = currentSegmentIndex + 1
        if (nextIndex < currentSegments.size) {
            playSegment(nextIndex)
        } else {
            stop()
        }
    }

    fun pause() {
        if (_playbackState.value is VoicePlaybackState.Playing) {
            isManuallyPaused = true
            tts?.stop()
            stopWaveformAnimation()
            _playbackState.value = VoicePlaybackState.Paused(
                fullText = currentFullText,
                segmentIndex = currentSegmentIndex,
                totalSegments = currentSegments.size
            )
        }
    }

    fun resume() {
        if (_playbackState.value is VoicePlaybackState.Paused) {
            isManuallyPaused = false
            playSegment(currentSegmentIndex)
        }
    }

    fun replay() {
        if (currentFullText.isNotBlank()) {
            speak(currentFullText)
        }
    }

    fun stop() {
        isManuallyPaused = false
        tts?.stop()
        stopWaveformAnimation()
        _playbackState.value = VoicePlaybackState.Idle
    }

    private fun startWaveformAnimation() {
        waveJob?.cancel()
        waveJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val waves = List(8) { (20..95).random() / 100f }
                _audioWaveform.value = waves
                delay(120)
            }
        }
    }

    private fun stopWaveformAnimation() {
        waveJob?.cancel()
        waveJob = null
        _audioWaveform.value = listOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f)
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
