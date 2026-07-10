package com.arena.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Arena(
    val slug: String,
    val title: String,
    val description: String,
    val navDescription: String,
    val categories: List<LeaderboardCategory> = emptyList(),
    val hasStyleControl: Boolean = false,
    val hasPricing: Boolean = false,
    val hasScoreFilter: Boolean = false,
    val symmetricCi: Boolean = true,
    val showInNav: Boolean = true
)

@Serializable
data class LeaderboardCategory(
    val id: String,
    val name: String,
    val description: String,
    val metrics: List<LeaderboardMetric> = emptyList()
)

@Serializable
data class LeaderboardMetric(
    val id: String,
    val name: String,
    val description: String,
    val unit: String? = null
)

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val model: AIModel,
    val score: Double,
    val scoreCiLow: Double? = null,
    val scoreCiHigh: Double? = null,
    val wins: Int = 0,
    val losses: Int = 0,
    val ties: Int = 0,
    val battles: Int = 0,
    val organization: String
)

@Serializable
data class LeaderboardSnapshot(
    val arena: Arena,
    val entries: List<LeaderboardEntry>,
    val lastUpdated: String,
    val totalBattles: Int,
    val totalModels: Int
)

@Serializable
data class AgentTool(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val icon: String? = null,
    val isEnabled: Boolean = true
)

@Serializable
enum class AgentStatus { IDLE, WORKING, PAUSED, COMPLETED, ERROR }

@Serializable
data class AgentSession(
    val id: String,
    val conversationId: String,
    val modelId: String,
    val modelName: String,
    val status: AgentStatus,
    val currentTask: String,
    val tools: List<AgentTool>,
    val createdAt: Long
)