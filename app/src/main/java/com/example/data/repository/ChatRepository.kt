package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.example.data.local.RimaDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.model.AvailableModels
import com.example.data.model.ResponseLengthMode
import com.example.data.model.SettingsManager
import com.example.data.remote.CandidateDto
import com.example.data.remote.ContentDto
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.GeminiErrorResponse
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.GenerationConfigDto
import com.example.data.remote.InlineDataDto
import com.example.data.remote.PartDto
import com.example.util.DetectedLanguage
import com.example.util.LanguageDetectorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

class ChatRepository(
    private val context: Context,
    private val database: RimaDatabase,
    private val settingsManager: SettingsManager
) {
    private val conversationDao = database.conversationDao()
    private val chatMessageDao = database.chatMessageDao()

    fun getAllConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations()

    fun searchConversations(query: String): Flow<List<ConversationEntity>> =
        conversationDao.searchConversations(query)

    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getMessagesForConversation(conversationId)

    suspend fun createNewConversation(
        title: String = "New Chat",
        modelId: String = settingsManager.settings.value.selectedModelId
    ): ConversationEntity = withContext(Dispatchers.IO) {
        val newConv = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            modelId = modelId,
            isPinned = false
        )
        conversationDao.insertConversation(newConv)
        newConv
    }

    suspend fun getConversationById(id: String): ConversationEntity? = withContext(Dispatchers.IO) {
        conversationDao.getConversationById(id)
    }

    suspend fun renameConversation(id: String, newTitle: String) = withContext(Dispatchers.IO) {
        conversationDao.renameConversation(id, newTitle)
    }

    suspend fun togglePinConversation(id: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setPinned(id, isPinned)
    }

    suspend fun deleteConversation(id: String) = withContext(Dispatchers.IO) {
        chatMessageDao.deleteMessagesForConversation(id)
        conversationDao.deleteConversation(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        chatMessageDao.clearAllMessages()
        conversationDao.clearAllConversations()
    }

    suspend fun deleteMessage(id: String) = withContext(Dispatchers.IO) {
        chatMessageDao.deleteMessage(id)
    }

    /**
     * Sends user message to Gemini and returns the saved AI response entity.
     */
    suspend fun sendMessage(
        conversationId: String,
        userPrompt: String,
        imageUri: Uri? = null,
        documentName: String? = null,
        documentContent: String? = null,
        overrideModelId: String? = null
    ): Result<ChatMessageEntity> = withContext(Dispatchers.IO) {
        val settings = settingsManager.settings.value
        val modelId = overrideModelId ?: settings.selectedModelId
        val apiKey = settings.customApiKey.ifBlank { BuildConfig.GEMINI_API_KEY }

        // 1. Detect language of the input
        val cleanPrompt = userPrompt.trim()
        val detectedLang = LanguageDetectorUtil.detectLanguage(if (cleanPrompt.isNotBlank()) cleanPrompt else "Hello")
        val langCode = when (detectedLang) {
            DetectedLanguage.BENGALI -> "bn"
            DetectedLanguage.BANGLISH -> "banglish"
            DetectedLanguage.ENGLISH -> "en"
        }

        // 2. Insert User Message into Room
        val userMessageId = UUID.randomUUID().toString()
        val displayPrompt = if (cleanPrompt.isNotBlank()) {
            cleanPrompt
        } else if (imageUri != null) {
            "Attached Image"
        } else if (documentName != null) {
            "Attached File: $documentName"
        } else {
            "Hello"
        }

        val userMessage = ChatMessageEntity(
            id = userMessageId,
            conversationId = conversationId,
            role = "user",
            content = displayPrompt,
            imageUri = imageUri?.toString(),
            attachmentName = documentName,
            attachmentType = if (imageUri != null) "image" else if (documentName != null) "document" else null,
            timestamp = System.currentTimeMillis(),
            isError = false,
            modelName = AvailableModels.find(modelId).name,
            language = langCode
        )
        chatMessageDao.insertMessage(userMessage)
        conversationDao.touchConversation(conversationId)

        // 3. Build Sanitized Conversation History for multi-turn context (strict user/model alternation)
        val historyMessages = chatMessageDao.getMessagesListForConversation(conversationId)
        val contentsList = buildSanitizedContents(
            history = historyMessages,
            currentUserMessageId = userMessageId,
            currentImageUri = imageUri,
            currentDocumentName = documentName,
            currentDocumentContent = documentContent
        )

        // 4. Build System Instruction for Rima AI
        val systemInstructionText = buildSystemPrompt(settings.responseLength, settings.customInstructions, detectedLang)
        val systemInstruction = ContentDto(
            parts = listOf(PartDto(text = systemInstructionText))
        )

        val generationConfig = GenerationConfigDto(
            temperature = 0.7f,
            topP = 0.95f,
            maxOutputTokens = when (settings.responseLength) {
                ResponseLengthMode.SHORT -> 512
                ResponseLengthMode.NORMAL -> 2048
                ResponseLengthMode.DETAILED -> 4096
            }
        )

        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = generationConfig,
            systemInstruction = systemInstruction
        )

        // 5. Call Real Gemini API with retries and robust error handling
        if (apiKey.isBlank()) {
            val missingKeyError = "Gemini API key is not configured. Please ensure the GEMINI_API_KEY secret is configured in AI Studio Secrets or enter an API key in Settings."
            val errorMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                role = "model",
                content = missingKeyError,
                timestamp = System.currentTimeMillis(),
                isError = true,
                modelName = AvailableModels.find(modelId).name,
                language = langCode
            )
            chatMessageDao.insertMessage(errorMsg)
            return@withContext Result.failure(Exception(missingKeyError))
        }

        var lastException: Exception? = null
        val maxAttempts = 3

        for (attempt in 1..maxAttempts) {
            try {
                val response = GeminiApiClient.service.generateContent(
                    model = modelId,
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val rawAnswer = body.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "I received your request, but could not generate a response text. Please try again."

                    val aiMsg = ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = conversationId,
                        role = "model",
                        content = rawAnswer,
                        timestamp = System.currentTimeMillis(),
                        isError = false,
                        modelName = AvailableModels.find(modelId).name,
                        language = langCode
                    )
                    chatMessageDao.insertMessage(aiMsg)
                    autoRenameFirstTurn(conversationId, displayPrompt)
                    return@withContext Result.success(aiMsg)
                } else {
                    val statusCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "HTTP $statusCode"

                    // If transient server error (429, 500, 503), retry
                    if ((statusCode == 429 || statusCode == 500 || statusCode == 503) && attempt < maxAttempts) {
                        kotlinx.coroutines.delay(1000L * attempt)
                        continue
                    }

                    val friendlyError = formatApiError(statusCode, errorBody)
                    val errorMsg = ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = conversationId,
                        role = "model",
                        content = friendlyError,
                        timestamp = System.currentTimeMillis(),
                        isError = true,
                        modelName = AvailableModels.find(modelId).name,
                        language = langCode
                    )
                    chatMessageDao.insertMessage(errorMsg)
                    return@withContext Result.failure(Exception(friendlyError))
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxAttempts) {
                    kotlinx.coroutines.delay(1000L * attempt)
                }
            }
        }

        val errorText = when (lastException) {
            is java.net.SocketTimeoutException -> "Request Timeout: The AI model took too long to respond. Please check your network connection and retry."
            is java.net.UnknownHostException -> "Network Unavailable: Unable to connect to the Gemini API servers. Please check your internet connection."
            else -> "Connection Error: ${lastException?.localizedMessage ?: "Unable to complete request to Gemini API. Please try again."}"
        }

        val errorMsg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = "model",
            content = errorText,
            timestamp = System.currentTimeMillis(),
            isError = true,
            modelName = AvailableModels.find(modelId).name,
            language = langCode
        )
        chatMessageDao.insertMessage(errorMsg)
        Result.failure(lastException ?: Exception(errorText))
    }

    private fun buildSanitizedContents(
        history: List<ChatMessageEntity>,
        currentUserMessageId: String,
        currentImageUri: Uri?,
        currentDocumentName: String?,
        currentDocumentContent: String?
    ): List<ContentDto> {
        val validMessages = history.filter { !it.isError || it.id == currentUserMessageId }
        val turnsToProcess = if (validMessages.size > 11) {
            validMessages.takeLast(11)
        } else {
            validMessages
        }

        val rawContents = mutableListOf<ContentDto>()

        for (msg in turnsToProcess) {
            val parts = mutableListOf<PartDto>()
            val isCurrentTurn = (msg.id == currentUserMessageId)

            if (isCurrentTurn && currentImageUri != null) {
                val base64Data = encodeUriToBase64(currentImageUri)
                if (!base64Data.isNullOrBlank()) {
                    val mimeType = getMimeType(currentImageUri) ?: "image/jpeg"
                    parts.add(PartDto(inlineData = InlineDataDto(mimeType = mimeType, data = base64Data)))
                }
            }

            var textContent = msg.content.trim()
            if (isCurrentTurn && !currentDocumentContent.isNullOrBlank()) {
                val docHeader = "Attached Document Content (${currentDocumentName ?: "document"}):\n```\n$currentDocumentContent\n```\n\n"
                textContent = if (textContent.isNotBlank()) "$docHeader$textContent" else "${docHeader}Please analyze and summarize the attached document."
            }

            if (textContent.isNotBlank()) {
                parts.add(PartDto(text = textContent))
            } else if (parts.isEmpty()) {
                if (isCurrentTurn && currentImageUri != null) {
                    parts.add(PartDto(text = "Please analyze this image."))
                } else {
                    parts.add(PartDto(text = "Hello"))
                }
            }

            if (parts.isNotEmpty()) {
                val role = if (msg.role == "user") "user" else "model"
                rawContents.add(ContentDto(role = role, parts = parts))
            }
        }

        // Strict alternating validation:
        // Must alternate user -> model -> user -> model -> user
        val alternatingList = mutableListOf<ContentDto>()
        for (item in rawContents) {
            if (alternatingList.isEmpty()) {
                if (item.role == "user") {
                    alternatingList.add(item)
                }
            } else {
                val lastRole = alternatingList.last().role
                if (item.role != lastRole) {
                    alternatingList.add(item)
                } else {
                    // Collapse duplicate consecutive role by keeping the latest turn
                    if (item.role == "user") {
                        alternatingList[alternatingList.lastIndex] = item
                    }
                }
            }
        }

        // Must end with user role
        if (alternatingList.isEmpty() || alternatingList.last().role != "user") {
            val fallbackParts = mutableListOf<PartDto>()
            if (currentImageUri != null) {
                val base64 = encodeUriToBase64(currentImageUri)
                if (!base64.isNullOrBlank()) {
                    fallbackParts.add(PartDto(inlineData = InlineDataDto(mimeType = getMimeType(currentImageUri) ?: "image/jpeg", data = base64)))
                }
            }
            val latestUserMsg = history.lastOrNull { it.id == currentUserMessageId }?.content?.trim()?.ifBlank { "Hello" } ?: "Hello"
            fallbackParts.add(PartDto(text = latestUserMsg))
            return listOf(ContentDto(role = "user", parts = fallbackParts))
        }

        return alternatingList
    }

    private fun getMimeType(uri: Uri): String? {
        return try {
            context.contentResolver.getType(uri) ?: "image/jpeg"
        } catch (e: Exception) {
            "image/jpeg"
        }
    }

    private suspend fun autoRenameFirstTurn(conversationId: String, userPrompt: String) {
        val conv = conversationDao.getConversationById(conversationId)
        if (conv != null && (conv.title == "New Chat" || conv.title.isBlank())) {
            val words = userPrompt.trim().split("\\s+".toRegex())
            val summaryTitle = if (words.size <= 5) {
                userPrompt.trim().take(36)
            } else {
                words.take(5).joinToString(" ").take(36) + "..."
            }
            conversationDao.renameConversation(conversationId, summaryTitle)
        }
    }

    private fun buildSystemPrompt(
        length: ResponseLengthMode,
        customInstructions: String,
        detectedLang: DetectedLanguage
    ): String {
        val lengthInstruction = when (length) {
            ResponseLengthMode.SHORT -> "Keep your responses concise, straight to the point, and direct."
            ResponseLengthMode.NORMAL -> "Provide balanced, well-structured, clear explanations with appropriate detail."
            ResponseLengthMode.DETAILED -> "Provide comprehensive, deeply detailed, step-by-step answers with thorough explanations and examples."
        }

        return """
            You are Rima AI, an intelligent, friendly, modern, and empathetic personal AI assistant.
            Tagline: "Ask Anything. Get Intelligent Answers."
            
            Core Guidelines:
            1. Multilingual Fluency:
               - If the user writes in Bengali (বাংলা), reply in natural, grammatically correct, pleasant Bengali.
               - If the user writes in Banglish (Bengali transliterated in English script like 'kemon acho', 'amake help koro'), understand it fluently and respond warmly in natural Bengali (or Banglish if specifically requested).
               - If the user writes in English, reply in clear, professional English.
               - If the user uses a mixture of Bengali and English, converse seamlessly with bilingual context.
            2. Formatting:
               - Use Markdown headings (##, ###), bullet lists (•), numbered lists (1., 2.), bold text (**term**), and structured sections.
               - For programming and coding, always use formatted code blocks with the exact language name (e.g. ```kotlin, ```python).
               - Use tables where comparative data is useful.
               - Render math calculations cleanly.
            3. Accuracy & Truthfulness:
               - Provide accurate, helpful answers.
               - Never hallucinate or make up fake facts. If information is outside your knowledge, be completely transparent.
            4. Detail Level:
               - $lengthInstruction
            ${if (customInstructions.isNotBlank()) "5. User's Custom Instructions:\n$customInstructions" else ""}
        """.trimIndent()
    }

    private fun formatApiError(code: Int, rawBody: String): String {
        val parsedMsg = try {
            val adapter = GeminiApiClient.moshi.adapter(GeminiErrorResponse::class.java)
            adapter.fromJson(rawBody)?.error?.message
        } catch (e: Exception) {
            null
        }

        val detail = if (!parsedMsg.isNullOrBlank()) {
            "\nDetails: $parsedMsg"
        } else if (rawBody.isNotBlank()) {
            "\nDetails: $rawBody"
        } else ""

        return when (code) {
            400 -> "Invalid Request (400): $detail"
            401, 403 -> "Authentication Error ($code): Please verify your Gemini API key in Settings or AI Studio Secrets.$detail"
            429 -> "Rate Limit Exceeded (429): You have sent too many requests. Please wait a moment and try again.$detail"
            500, 503 -> "Server Unavailable ($code): The AI provider is temporarily busy. Please try again shortly.$detail"
            else -> "Error ($code): An issue occurred while contacting Rima AI.$detail"
        }
    }

    private fun encodeUriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                // Resize if oversized to preserve memory and speed
                val maxDim = 1024
                val scale = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                    val maxOriginal = maxOf(bitmap.width, bitmap.height)
                    maxDim.toFloat() / maxOriginal
                } else 1.0f

                val scaledBitmap = if (scale < 1.0f) {
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scale).toInt(),
                        (bitmap.height * scale).toInt(),
                        true
                    )
                } else bitmap

                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val byteArray = outputStream.toByteArray()
                Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
