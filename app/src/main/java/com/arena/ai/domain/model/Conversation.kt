package com.arena.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String,
    val title: String,
    val modality: Modality = Modality.TEXT,
    val mode: ChatMode = ChatMode.NORMAL,
    val modelId: String? = null,
    val modelName: String? = null,
    val messages: List<Message> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val tags: List<String> = emptyList()
)

@Serializable
data class Message(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val modelId: String? = null,
    val modelName: String? = null,
    val attachments: List<Attachment> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val error: String? = null
)

@Serializable
enum class MessageRole { USER, ASSISTANT, SYSTEM }

@Serializable
enum class Modality { TEXT, CODE, SEARCH, IMAGE, MULTIMODAL }

@Serializable
enum class ChatMode { NORMAL, BATTLE, AGENT }

@Serializable
data class Attachment(
    val id: String,
    val type: AttachmentType,
    val url: String? = null,
    val localPath: String? = null,
    val name: String? = null,
    val mimeType: String? = null,
    val size: Long = 0
)

@Serializable
enum class AttachmentType { IMAGE, FILE, CODE }