package com.arena.ai.domain.repository

import com.arena.ai.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getAllConversations(): Flow<List<Conversation>>
    fun getConversation(id: String): Flow<Conversation?>
    fun searchConversations(query: String): Flow<List<Conversation>>
    suspend fun createConversation(conversation: Conversation): String
    suspend fun updateConversation(conversation: Conversation)
    suspend fun deleteConversation(id: String)
    suspend fun addMessage(conversationId: String, message: Message)
}

interface ChatRepository {
    suspend fun sendMessage(conversationId: String, message: Message, modelId: String, streamCallback: (String) -> Unit): Result<Message>
    fun getChatHistory(conversationId: String): Flow<List<Message>>
    suspend fun stopGeneration()
}

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout()
    suspend fun getAuthToken(): String?
    fun isLoggedIn(): Flow<Boolean>
}

interface LeaderboardRepository {
    fun getLeaderboards(): Flow<List<Arena>>
    fun getLeaderboard(slug: String): Flow<LeaderboardSnapshot?>
    suspend fun refreshLeaderboard(slug: String)
}