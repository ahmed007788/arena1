package com.arena.ai.domain.model

enum class MessageRole { USER, ASSISTANT, SYSTEM }
enum class Modality { TEXT, CODE, SEARCH, IMAGE, MULTIMODAL }
enum class ChatMode { NORMAL, BATTLE, AGENT }

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
    val isArchived: Boolean = false
)

data class Message(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val modelId: String? = null,
    val modelName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val error: String? = null
)

data class AIModel(
    val id: String,
    val provider: String,
    val organization: String,
    val displayName: String,
    val modelId: String,
    val shortName: String
)
