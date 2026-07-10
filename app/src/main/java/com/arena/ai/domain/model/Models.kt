package com.arena.ai.domain.model

data class AIModel(
    val id: String,
    val provider: String,
    val organization: String,
    val displayName: String,
    val modelId: String,
    val shortName: String,
    val capabilities: ModelCapabilities
)

data class ModelCapabilities(
    val input: InputCapabilities,
    val output: OutputCapabilities
)

data class InputCapabilities(
    val text: Boolean,
    val image: Boolean?
)

data class OutputCapabilities(
    val text: Boolean,
    val image: Boolean?
)

enum class Modality {
    TEXT,
    IMAGE,
    VIDEO
}