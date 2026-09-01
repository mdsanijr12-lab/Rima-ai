package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.RimaSpeechRecognizer
import com.example.audio.RimaVoiceManager
import com.example.audio.SpeechRecognitionState
import com.example.audio.VoicePlaybackState
import com.example.data.local.RimaDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.model.AppThemeMode
import com.example.data.model.AvailableModels
import com.example.data.model.ResponseLengthMode
import com.example.data.model.SettingsManager
import com.example.data.model.UserSettings
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RimaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = RimaDatabase.getDatabase(application)
    val settingsManager = SettingsManager(application)
    private val repository = ChatRepository(application, database, settingsManager)

    val voiceManager = RimaVoiceManager(application, settingsManager, viewModelScope)
    val speechRecognizer = RimaSpeechRecognizer(application)

    val userSettings: StateFlow<UserSettings> = settingsManager.settings

    val allConversations: StateFlow<List<ConversationEntity>> = repository.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredConversations: StateFlow<List<ConversationEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllConversations()
            else repository.searchConversations(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _currentConversation = MutableStateFlow<ConversationEntity?>(null)
    val currentConversation: StateFlow<ConversationEntity?> = _currentConversation.asStateFlow()

    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentConversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getMessagesForConversation(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Attachments
    private val _attachedImageUri = MutableStateFlow<Uri?>(null)
    val attachedImageUri: StateFlow<Uri?> = _attachedImageUri.asStateFlow()

    private val _attachedDocument = MutableStateFlow<Pair<String, String>?>(null) // (name, content)
    val attachedDocument: StateFlow<Pair<String, String>?> = _attachedDocument.asStateFlow()

    val voicePlaybackState: StateFlow<VoicePlaybackState> = voiceManager.playbackState
    val audioWaveform: StateFlow<List<Float>> = voiceManager.audioWaveform
    val speechRecognitionState: StateFlow<SpeechRecognitionState> = speechRecognizer.state

    init {
        // Automatically start or restore conversation
        viewModelScope.launch {
            allConversations.collect { list ->
                if (_currentConversationId.value == null && list.isNotEmpty()) {
                    selectConversation(list.first().id)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectConversation(id: String) {
        _currentConversationId.value = id
        viewModelScope.launch {
            _currentConversation.value = repository.getConversationById(id)
        }
    }

    fun startNewChat() {
        viewModelScope.launch {
            val newConv = repository.createNewConversation(
                title = "New Chat",
                modelId = userSettings.value.selectedModelId
            )
            _currentConversationId.value = newConv.id
            _currentConversation.value = newConv
            clearAttachments()
            voiceManager.stop()
        }
    }

    fun attachImage(uri: Uri) {
        _attachedImageUri.value = uri
        _attachedDocument.value = null
    }

    fun attachDocument(name: String, content: String) {
        _attachedDocument.value = Pair(name, content)
        _attachedImageUri.value = null
    }

    fun clearAttachments() {
        _attachedImageUri.value = null
        _attachedDocument.value = null
    }

    fun sendMessage(promptText: String) {
        if (promptText.isBlank() && _attachedImageUri.value == null && _attachedDocument.value == null) return
        if (_isGenerating.value) return

        viewModelScope.launch {
            val convId = _currentConversationId.value ?: run {
                val newConv = repository.createNewConversation(
                    title = "New Chat",
                    modelId = userSettings.value.selectedModelId
                )
                _currentConversationId.value = newConv.id
                _currentConversation.value = newConv
                newConv.id
            }

            val imageUri = _attachedImageUri.value
            val docPair = _attachedDocument.value
            clearAttachments()

            _isGenerating.value = true

            val result = repository.sendMessage(
                conversationId = convId,
                userPrompt = promptText,
                imageUri = imageUri,
                documentName = docPair?.first,
                documentContent = docPair?.second,
                overrideModelId = _currentConversation.value?.modelId
            )

            _isGenerating.value = false

            // Update current conversation title if updated
            _currentConversation.value = repository.getConversationById(convId)

            // If auto-read is enabled, speak the answer
            if (userSettings.value.autoReadAnswers && result.isSuccess) {
                val answer = result.getOrNull()?.content
                if (!answer.isNullOrBlank()) {
                    voiceManager.speak(answer)
                }
            }
        }
    }

    fun regenerateLastResponse() {
        val messages = currentMessages.value
        val lastUserMessage = messages.lastOrNull { it.role == "user" } ?: return
        val convId = _currentConversationId.value ?: return

        viewModelScope.launch {
            _isGenerating.value = true
            val result = repository.sendMessage(
                conversationId = convId,
                userPrompt = lastUserMessage.content,
                imageUri = lastUserMessage.imageUri?.let { Uri.parse(it) },
                documentName = lastUserMessage.attachmentName
            )
            _isGenerating.value = false

            if (userSettings.value.autoReadAnswers && result.isSuccess) {
                result.getOrNull()?.content?.let { voiceManager.speak(it) }
            }
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            repository.renameConversation(id, newTitle)
            if (_currentConversationId.value == id) {
                _currentConversation.value = repository.getConversationById(id)
            }
        }
    }

    fun togglePinConversation(id: String, currentPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinConversation(id, !currentPinned)
            if (_currentConversationId.value == id) {
                _currentConversation.value = repository.getConversationById(id)
            }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                val remaining = allConversations.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    selectConversation(remaining.first().id)
                } else {
                    _currentConversationId.value = null
                    _currentConversation.value = null
                }
            }
        }
    }

    fun clearAllChats() {
        viewModelScope.launch {
            repository.clearAll()
            _currentConversationId.value = null
            _currentConversation.value = null
            voiceManager.stop()
        }
    }

    // Voice playback actions
    fun readAloud(text: String) {
        voiceManager.speak(text)
    }

    fun pauseVoice() {
        voiceManager.pause()
    }

    fun resumeVoice() {
        voiceManager.resume()
    }

    fun stopVoice() {
        voiceManager.stop()
    }

    fun replayVoice() {
        voiceManager.replay()
    }

    // Speech to text actions
    fun startSpeechToText() {
        speechRecognizer.startListening(userSettings.value.speechLanguage)
    }

    fun stopSpeechToText() {
        speechRecognizer.stopListening()
    }

    fun resetSpeechState() {
        speechRecognizer.resetState()
    }

    // Settings actions
    fun updateTheme(theme: AppThemeMode) = settingsManager.updateTheme(theme)
    fun updateModel(modelId: String) = settingsManager.updateModel(modelId)
    fun updateResponseLength(length: ResponseLengthMode) = settingsManager.updateResponseLength(length)
    fun updateAutoRead(enabled: Boolean) = settingsManager.updateAutoRead(enabled)
    fun updateSpeechLanguage(lang: String) = settingsManager.updateSpeechLanguage(lang)
    fun updateVoiceSettings(pitch: Float, speed: Float) = settingsManager.updateVoiceSettings(pitch, speed)
    fun updateCustomInstructions(instructions: String) = settingsManager.updateCustomInstructions(instructions)
    fun updateWebSearch(enabled: Boolean) = settingsManager.updateWebSearch(enabled)
    fun updateApiKey(key: String) = settingsManager.updateCustomApiKey(key)

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
        speechRecognizer.stopListening()
    }
}
