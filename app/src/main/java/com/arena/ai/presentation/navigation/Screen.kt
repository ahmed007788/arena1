package com.arena.ai.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Home : Screen("home", "Chat", Icons.Default.Chat)
    data object Search : Screen("search", "Search", Icons.Default.Search)
    data object Leaderboard : Screen("leaderboard", "Leaderboard", Icons.Default.Leaderboard)
    data object Agent : Screen("agent", "Agent", Icons.Default.SmartToy)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Chat : Screen("chat/{id}", "Chat") {
        fun createRoute(id: String) = "chat/$id"
    }

    companion object {
        val bottomNavItems = listOf(Home, Search, Leaderboard, Agent, Settings)
    }
}