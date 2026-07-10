package com.arena.ai.data.remote

import kotlinx.coroutines.delay

data class AIModel(
    val id: String,
    val provider: String,
    val organization: String,
    val displayName: String,
    val modelId: String,
    val shortName: String
)

data class AgentLeaderboardEntry(
    val rank: Int,
    val modelName: String,
    val lab: String,
    val netImprovement: Double,
    val confirmedSuccess: Double,
    val praiseVsComplaint: Double,
    val steerability: Double,
    val bashRecovery: Double,
    val toolHallucination: Double,
    val sessions: Int
)

val realModels = listOf(
    AIModel("claude-fable-5", "anthropic", "Anthropic", "Claude Fable 5", "claude-fable-5", "Claude Fable 5"),
    AIModel("claude-opus-4-8-thinking", "anthropic", "Anthropic", "Claude Opus 4.8 (Thinking)", "claude-opus-4-8-thinking", "Opus 4.8"),
    AIModel("claude-opus-4-7-thinking", "anthropic", "Anthropic", "Claude Opus 4.7 (Thinking)", "claude-opus-4-7-thinking", "Opus 4.7"),
    AIModel("claude-opus-4-7", "anthropic", "Anthropic", "Claude Opus 4.7", "claude-opus-4-7", "Opus 4.7"),
    AIModel("claude-sonnet-5-high", "anthropic", "Anthropic", "Claude Sonnet 5 (High)", "claude-sonnet-5-high", "Sonnet 5"),
    AIModel("claude-opus-4-6", "anthropic", "Anthropic", "Claude Opus 4.6", "claude-opus-4-6", "Opus 4.6"),
    AIModel("claude-opus-4-6-thinking", "anthropic", "Anthropic", "Claude Opus 4.6 Thinking", "claude-opus-4-6-thinking", "Opus 4.6 Th"),
    AIModel("claude-sonnet-4-6", "anthropic", "Anthropic", "Claude Sonnet 4.6", "claude-sonnet-4-6", "Sonnet 4.6"),
    AIModel("claude-opus-4-8", "anthropic", "Anthropic", "Claude Opus 4.8", "claude-opus-4-8", "Opus 4.8"),
    AIModel("gpt-5.5-xhigh", "openai", "OpenAI", "GPT-5.5 (xHigh)", "gpt-5.5-xhigh", "GPT-5.5 xH"),
    AIModel("gpt-5.5-high", "openai", "OpenAI", "GPT-5.5 (High)", "gpt-5.5-high", "GPT-5.5 H"),
    AIModel("gpt-5.4-high", "openai", "OpenAI", "GPT-5.4 (High)", "gpt-5.4-high", "GPT-5.4 H"),
    AIModel("gpt-5.5", "openai", "OpenAI", "GPT-5.5", "gpt-5.5", "GPT-5.5"),
    AIModel("gpt-5.4", "openai", "OpenAI", "GPT-5.4", "gpt-5.4", "GPT-5.4"),
    AIModel("gemini-3.1-pro-preview", "google", "Google", "Gemini 3.1 Pro Preview", "gemini-3.1-pro-preview", "Gemini 3.1"),
    AIModel("gemini-3.5-flash-high", "google", "Google", "Gemini 3.5 Flash (High)", "gemini-3.5-flash-high", "Gemini 3.5"),
    AIModel("gemini-3-flash", "google", "Google", "Gemini 3 Flash", "gemini-3-flash", "Gemini 3"),
    AIModel("gemma-4-31b", "google", "Google", "Gemma 4 31B", "gemma-4-31b", "Gemma 4"),
    AIModel("grok-4.3", "xai", "xAI", "Grok 4.3", "grok-4.3", "Grok 4.3"),
    AIModel("grok-4.3-high", "xai", "xAI", "Grok 4.3 (High)", "grok-4.3-high", "Grok 4.3 H"),
    AIModel("deepseek-v4-pro", "deepseek", "DeepSeek", "DeepSeek V4 Pro", "deepseek-v4-pro", "DeepSeek V4"),
    AIModel("deepseek-v4-flash", "deepseek", "DeepSeek", "DeepSeek V4 Flash", "deepseek-v4-flash", "DeepSeek V4F"),
    AIModel("glm-5.2-max", "z-ai", "Z.ai", "GLM 5.2 (Max)", "glm-5.2-max", "GLM 5.2"),
    AIModel("glm-5.1", "z-ai", "Z.ai", "GLM 5.1", "glm-5.1", "GLM 5.1"),
    AIModel("qwen3.7-max", "alibaba", "Alibaba", "Qwen3.7 Max", "qwen3.7-max", "Qwen 3.7"),
    AIModel("qwen3.7-plus", "alibaba", "Alibaba", "Qwen3.7 Plus", "qwen3.7-plus", "Qwen 3.7+"),
    AIModel("kimi-k2.7-code", "moonshot", "Moonshot", "Kimi K2.7 Code", "kimi-k2.7-code", "Kimi K2.7"),
    AIModel("kimi-k2.6", "moonshot", "Moonshot", "Kimi K2.6", "kimi-k2.6", "Kimi K2.6"),
    AIModel("minimax-m3", "minimax", "MiniMax", "MiniMax M3", "minimax-m3", "MiniMax M3"),
    AIModel("minimax-m2.7", "minimax", "MiniMax", "MiniMax M2.7", "minimax-m2.7", "MiniMax M2.7"),
    AIModel("mimo-v2.5-pro", "xiaomi", "Xiaomi", "Mimo V2.5 Pro", "mimo-v2.5-pro", "Mimo V2.5"),
    AIModel("nemotron-3-ultra", "nvidia", "Nvidia", "Nemotron 3 Ultra", "nemotron-3-ultra", "Nemotron")
)

val realAgentLeaderboard = listOf(
    AgentLeaderboardEntry(1, "Claude Fable 5 (High)", "Anthropic", 14.10, 16.88, 30.94, 12.56, 8.91, 1.24, 16059),
    AgentLeaderboardEntry(2, "Claude Opus 4.8 (Thinking)", "Anthropic", 9.76, 8.28, 18.04, 11.77, 10.03, 0.70, 32490),
    AgentLeaderboardEntry(3, "GPT 5.5 (xHigh)", "OpenAI", 8.90, 7.69, 13.87, 7.18, 14.54, 1.24, 32695),
    AgentLeaderboardEntry(4, "Claude Opus 4.7 (Thinking)", "Anthropic", 8.51, 6.55, 13.34, 9.80, 11.74, 1.13, 33427),
    AgentLeaderboardEntry(5, "Claude Opus 4.7", "Anthropic", 8.22, 6.02, 13.96, 8.97, 10.97, 1.19, 34054),
    AgentLeaderboardEntry(6, "Claude Sonnet 5 (High)", "Anthropic", 8.16, 12.25, 14.50, 3.58, 9.35, 1.11, 22779),
    AgentLeaderboardEntry(7, "GPT 5.5 (High)", "OpenAI", 7.41, 6.41, 8.10, 8.71, 12.58, 1.24, 57971),
    AgentLeaderboardEntry(8, "GPT 5.4 (High)", "OpenAI", 6.73, 6.83, 6.69, 8.96, 9.91, 1.24, 58074),
    AgentLeaderboardEntry(9, "GPT 5.5", "OpenAI", 6.66, 4.79, 7.23, 8.84, 11.21, 1.24, 58633),
    AgentLeaderboardEntry(10, "GLM 5.2 (Max)", "Z.ai", 6.54, 9.46, 13.74, 3.44, 4.82, 1.24, 29649),
    AgentLeaderboardEntry(11, "Claude Opus 4.6", "Anthropic", 6.49, 2.32, 9.17, 8.67, 11.02, 1.24, 33169),
    AgentLeaderboardEntry(12, "Claude Opus 4.8", "Anthropic", 4.75, 7.23, 12.77, 8.83, 10.22, 15.29, 30531),
    AgentLeaderboardEntry(13, "Claude Sonnet 4.6", "Anthropic", 2.74, 0.79, 0.13, 1.66, 11.73, 1.23, 33934),
    AgentLeaderboardEntry(14, "GLM 5.1", "Z.ai", 1.57, 1.67, 0.80, 0.05, 4.07, 1.24, 47828),
    AgentLeaderboardEntry(15, "Qwen3.7 Max", "Alibaba", 0.36, 1.43, 0.52, 5.20, 7.91, 1.02, 6271),
    AgentLeaderboardEntry(16, "Kimi K2.7 Code", "Moonshot", 0.13, 7.70, 0.63, 6.82, 0.86, 1.24, 4870),
    AgentLeaderboardEntry(17, "Gemini 3.1 Pro Preview", "Google", 0.17, 0.72, 0.72, 3.47, 6.98, 1.20, 58057),
    AgentLeaderboardEntry(18, "Qwen3.7 Plus", "Alibaba", 0.43, 1.87, 1.63, 5.20, 5.90, 0.63, 6395),
    AgentLeaderboardEntry(19, "DeepSeek V4 Pro", "DeepSeek", 0.83, 2.34, 3.13, 4.61, 4.94, 0.99, 6680),
    AgentLeaderboardEntry(20, "Gemini 3.5 Flash (High)", "Google", 0.90, 0.98, 3.82, 0.44, 0.13, 1.09, 45997),
    AgentLeaderboardEntry(21, "Kimi K2.6", "Moonshot", 1.61, 2.34, 3.89, 7.33, 8.23, 1.24, 4952),
    AgentLeaderboardEntry(22, "MiniMax M3", "MiniMax", 3.08, 7.66, 9.93, 5.24, 6.46, 0.99, 6413),
    AgentLeaderboardEntry(23, "Mimo V2.5 Pro", "Xiaomi", 3.71, 6.18, 10.28, 5.48, 2.77, 0.63, 6760),
    AgentLeaderboardEntry(24, "DeepSeek V4 Flash", "DeepSeek", 4.65, 9.37, 10.65, 7.33, 4.71, 0.60, 6469),
    AgentLeaderboardEntry(25, "Gemini 3.5 Flash (Medium)", "Google", 7.24, 13.44, 9.32, 8.13, 6.20, 0.87, 6134),
    AgentLeaderboardEntry(26, "Grok 4.3 (High)", "xAI", 7.65, 8.92, 14.41, 6.78, 8.88, 0.74, 38049),
    AgentLeaderboardEntry(27, "Grok Build 0.1", "xAI", 7.87, 5.77, 13.07, 11.38, 9.34, 0.24, 49156),
    AgentLeaderboardEntry(28, "Gemini 3 Flash", "Google", 8.14, 8.77, 11.47, 3.98, 16.56, 0.10, 58454),
    AgentLeaderboardEntry(29, "MiniMax M2.7", "MiniMax", 10.18, 17.12, 14.49, 16.42, 3.74, 0.89, 6604),
    AgentLeaderboardEntry(30, "Nemotron 3 Ultra", "Nvidia", 11.87, 11.23, 7.15, 20.97, 18.97, 1.01, 8814),
    AgentLeaderboardEntry(31, "Gemma 4 31B", "Google", 13.09, 3.00, 4.99, 4.32, 31.07, 22.07, 47760),
    AgentLeaderboardEntry(32, "Grok 4.3", "xAI", 15.57, 11.30, 16.01, 8.34, 43.10, 0.91, 57872)
)

object ArenaApiService {
    
    private const val BASE_URL = "https://arena.ai"
    
    suspend fun sendMessage(modelId: String, message: String): Result<String> {
        delay(1000)
        val model = realModels.find { it.id == modelId }
        return Result.success(generateResponse(message, model))
    }
    
    private fun generateResponse(message: String, model: AIModel?): String {
        return when {
            message.contains("hello", ignoreCase = true) || message.contains("hi", ignoreCase = true) ->
                "Hello! I'm ${model?.displayName ?: "AI"} from arena.ai. How can I help you today?"
            message.contains("who are you", ignoreCase = true) ->
                "I'm an AI assistant powered by ${model?.provider ?: "various AI models"} from arena.ai's official leaderboard."
            message.contains("help", ignoreCase = true) ->
                "I can help with: Writing code, Answering questions, Research, Creative writing, Problem solving."
            message.contains("thanks", ignoreCase = true) || message.contains("thank you", ignoreCase = true) ->
                "You're welcome!"
            message.contains("bye", ignoreCase = true) ->
                "Goodbye!"
            message.contains("leaderboard", ignoreCase = true) ->
                "Top 3 on arena.ai:\n🥇 Claude Fable 5 - Anthropic - 14.10%\n🥈 Claude Opus 4.8 - 9.76%\n🥉 GPT 5.5 - OpenAI - 8.90%"
            else ->
                "I understand you're asking about: \"$message\"\n\nAs an AI from arena.ai, feel free to ask anything!"
        }
    }
    
    suspend fun getAgentLeaderboard(): Result<List<AgentLeaderboardEntry>> {
        delay(500)
        return Result.success(realAgentLeaderboard)
    }
    
    suspend fun getModels(): Result<List<AIModel>> {
        delay(300)
        return Result.success(realModels)
    }
    
    var currentUser: String? = null
    var isAuthenticated: Boolean = false
    
    suspend fun login(email: String, password: String): Result<Boolean> {
        delay(1000)
        if (email == "Ai9900@bjedu.tech" && password == "Ai9900@bjedu.tech") {
            currentUser = email
            isAuthenticated = true
            return Result.success(true)
        }
        return Result.failure(Exception("Invalid credentials"))
    }
    
    fun logout() {
        currentUser = null
        isAuthenticated = false
    }
}