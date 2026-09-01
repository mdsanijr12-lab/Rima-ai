package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["conversationId"])]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String, // "user", "model", "system"
    val content: String,
    val imageUri: String? = null,
    val attachmentName: String? = null,
    val attachmentType: String? = null, // "image", "document"
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val modelName: String? = null,
    val language: String? = null // "bn", "en", "banglish"
)
